package no4j.core;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FileAppenderTest {

    @Test
    public void testSuccessfulLogToFile() throws IOException {
        FileAppender appender = new FileAppender();
        Path testPath = Paths.get("test/resources/test_log.txt");
        appender.attach(testPath);
        assertTrue(appender.isAttached());

        long sizeBefore = Files.size(testPath);
        appender.logToFile("Test log 1".getBytes(StandardCharsets.UTF_8));
        long sizeAfter = Files.size(testPath);
        assertTrue(sizeAfter > sizeBefore);

        appender.detach();
        assertFalse(appender.isAttached());
        appender.logToFile("Test log 2".getBytes(StandardCharsets.UTF_8));
        long currentSize = Files.size(testPath);
        assertEquals(sizeAfter, currentSize);
        Files.delete(testPath);
    }
}
