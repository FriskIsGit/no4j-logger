package no4j.core;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

/**
 * Responsible for writing to files, rolling behavior and compression.
 * This class uses a synchronized file output stream.
 *
 */
public class FileAppender {
    private static final int DEFAULT_MAX_SIZE = 4 * 1024 * 1024;
    private static final int BUFFER_SIZE = 8192;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_")
            .withZone(ZoneId.systemDefault());

    private volatile Path outputPath;
    private final OutputStream out;
    private final AtomicLong cursor = new AtomicLong(0);

    private volatile boolean isRolling;
    private volatile long maxSize = DEFAULT_MAX_SIZE;

    public FileAppender(Path path) throws IOException {
        outputPath = path;
        refreshCursor();
        out = newFileStreamForWriting(outputPath);
    }

    /**
     * Consistent with {@link Files#write} without additional fuss
     */
    protected void logToFile(byte[] bytes) throws IOException {
        int len = bytes.length;
        int rem = len;
        while (rem > 0) {
            int written = Math.min(rem, BUFFER_SIZE);
            out.write(bytes, (len-rem), written);
            cursor.addAndGet(written);
            rem -= written;
        }
        if (isRolling && cursor.get() >= maxSize) {
            roll();
        }
        out.flush();
        // out.close();
    }

    private void refreshCursor() throws IOException {
        if (Files.isRegularFile(outputPath)) {
            cursor.set(Files.size(outputPath));
        } else {
            cursor.set(0);
        }
    }

    private static OutputStream newFileStreamForWriting(Path path) throws IOException {
        HashSet<StandardOpenOption> options = new HashSet<StandardOpenOption>(3) {{
            add(StandardOpenOption.CREATE);
            add(StandardOpenOption.APPEND);
            add(StandardOpenOption.WRITE);
        }};
        FileSystemProvider fsProvider = path.getFileSystem().provider();
        return Channels.newOutputStream(fsProvider.newByteChannel(path, options));
    }

    private static InputStream newFileStreamForReading(Path path) throws IOException {
        HashSet<StandardOpenOption> options = new HashSet<StandardOpenOption>(1) {{
            add(StandardOpenOption.READ);
        }};
        FileSystemProvider fsProvider = path.getFileSystem().provider();
        return Channels.newInputStream(fsProvider.newByteChannel(path, options));
    }

    public void enableRolling(boolean enabled) {
        isRolling = enabled;
    }

    public void setMaxSize(long bytes) {
        maxSize = bytes;
    }

    public void roll() throws IOException {
        String format = formatter.format(Instant.now());
        compressToGZip(outputPath, format + outputPath.getFileName() + ".zip");
        resetOutputFile();
        cursor.set(0);
    }

    private static void compressToGZip(Path pathToCompress, String gZip) throws IOException {
        InputStream logStream = newFileStreamForReading(pathToCompress);
        GZIPOutputStream gZipOut = new GZIPOutputStream(newFileStreamForWriting(Paths.get(gZip)));

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = logStream.read(buffer)) > 0) {
            gZipOut.write(buffer, 0, bytesRead);
        }
        logStream.close();
        gZipOut.close();
    }

    public void resetOutputFile() throws IOException {
        RandomAccessFile file = new RandomAccessFile(outputPath.toFile(), "rw");
        file.setLength(0);
        file.close();
    }

    public void shutOff() throws IOException {
        out.close();
    }
}
