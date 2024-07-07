package no4j.core;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.file.*;
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
    private static final int MIN_SIZE = 1024;
    private static final int DEFAULT_MAX_SIZE = 4 * 1024 * 1024;
    private static final int BUFFER_SIZE = 8192;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_")
            .withZone(ZoneId.systemDefault());

    private volatile Path outputPath;
    private volatile OutputStream out;
    private final AtomicLong cursor = new AtomicLong(0);

    private volatile boolean isAttached;
    private volatile boolean isRolling;
    private volatile long maxSize = DEFAULT_MAX_SIZE;

    public FileAppender() {}

    /**
     * Consistent with {@link Files#write} without additional fuss
     */
    public synchronized void logToFile(byte[] bytes) {
        try {
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
        } catch (IOException e) {
            Logger.getInternalLogger().exception(e);
            isAttached = false;
        }
    }

    private void refreshCursor() throws IOException {
        if (Files.isRegularFile(outputPath)) {
            cursor.set(Files.size(outputPath));
        } else {
            cursor.set(0);
        }
    }

    /**
     * Consistent with {@link Files#newOutputStream} without additional fuss
     */
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

    public boolean isRolling() {
        return isRolling;
    }

    public void setRolling(boolean enabled) {
        isRolling = enabled;
    }

    public void setRollSize(long bytes) {
        maxSize = Math.max(bytes, MIN_SIZE);
    }

    public synchronized void roll() throws IOException {
        String format = formatter.format(Instant.now());
        compressToGZip(outputPath, format + outputPath.getFileName() + ".zip");
        resetOutputFile();
        cursor.set(0);
    }

    /**
     * This method is responsible for compressing files into GZIPs.
     * There's no exception associated with an already existing file that matches a newly compressed zip,
     * the file will be overwritten.
     */
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

    private void resetOutputFile() throws IOException {
        RandomAccessFile file = new RandomAccessFile(outputPath.toFile(), "rw");
        file.setLength(0);
        file.close();
    }

    /**
     * If during execution the handle is invalidated (the file is deleted or unhooked) it will automatically detach
     */
    public boolean isAttached() {
        return isAttached;
    }

    public synchronized void attach(Path path) throws IOException {
        outputPath = path;
        refreshCursor();
        out = newFileStreamForWriting(outputPath);
        isAttached = true;
    }

    public synchronized void reattach() throws IOException {
        refreshCursor();
        out = newFileStreamForWriting(outputPath);
        isAttached = true;
    }

    /**
     * Release file handle. Consecutive calls to this function have no effect
     */
    public synchronized void detach() throws IOException {
        if (!isAttached) {
            return;
        }
        isAttached = false;
        out.close();
        out = null;
    }

    @Override
    protected void finalize() throws Throwable {
        out.close();
    }
}
