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
 */
public class FileAppender {
    private static final int MIN_ROLL_SIZE = 1024;
    private static final int DEFAULT_ROLL_SIZE = 4 * 1024 * 1024;
    private static final int BUFFER_SIZE = 8192;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_")
            .withZone(ZoneId.systemDefault());

    private static final HashSet<StandardOpenOption> WRITE_OPTIONS = new HashSet<StandardOpenOption>(3) {{
        add(StandardOpenOption.CREATE);
        add(StandardOpenOption.APPEND);
        add(StandardOpenOption.WRITE);
    }};
    private static final HashSet<StandardOpenOption> READ_OPTIONS = new HashSet<StandardOpenOption>(1) {{
        add(StandardOpenOption.READ);
    }};

    private volatile Path outputPath;
    private volatile OutputStream out;
    private final AtomicLong cursor = new AtomicLong(0);

    private volatile boolean isAttached;
    private volatile boolean isRolling;
    private volatile long rollSize = DEFAULT_ROLL_SIZE;

    public FileAppender() {
    }

    /**
     * Consistent with {@link Files#write} without additional fuss
     */
    public synchronized void logToFile(byte[] bytes) {
        try {
            int len = bytes.length;
            int rem = len;
            while (rem > 0) {
                int written = Math.min(rem, BUFFER_SIZE);
                out.write(bytes, len - rem, written);
                cursor.addAndGet(written);
                rem -= written;
            }
            if (isRolling && cursor.get() >= rollSize) {
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
        FileSystemProvider fsProvider = path.getFileSystem().provider();
        return Channels.newOutputStream(fsProvider.newByteChannel(path, WRITE_OPTIONS));
    }

    private static InputStream newFileStreamForReading(Path path) throws IOException {
        FileSystemProvider fsProvider = path.getFileSystem().provider();
        return Channels.newInputStream(fsProvider.newByteChannel(path, READ_OPTIONS));
    }

    public boolean isRolling() {
        return isRolling;
    }

    public void setRolling(boolean enabled) {
        isRolling = enabled;
    }

    public void setRollSize(long bytes) {
        rollSize = Math.max(bytes, MIN_ROLL_SIZE);
    }

    public synchronized void roll() throws IOException {
        String timeFormat = formatter.format(Instant.now());
        String gZipName = timeFormat + outputPath.getFileName() + ".zip";
        compressToGZip(outputPath, outputPath.getParent().resolve(gZipName));
        resetOutputFile();
        cursor.set(0);
    }

    /**
     * This method is responsible for compressing files into GZIPs.
     * There's no exception associated with an already existing file that matches a newly compressed zip,
     * the file will be overwritten.
     */
    private static void compressToGZip(Path pathToCompress, Path gZip) throws IOException {
        InputStream logStream = newFileStreamForReading(pathToCompress);
        GZIPOutputStream gZipOut = new GZIPOutputStream(newFileStreamForWriting(gZip));

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
        detach();
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
