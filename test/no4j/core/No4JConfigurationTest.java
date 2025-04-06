package no4j.core;

import no4j.Mocks;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class No4JConfigurationTest {

    @Test
    public void testSimpleInheritance() throws IOException {
        No4JConfiguration.configure("test/resources/inheritance.ini");
        assertEquals(2, No4JConfiguration.get().loggers.size());
        Logger logger1 = Logger.getLogger("test-logger1");
        Logger logger2 = Logger.getLogger("test-logger2");
        List<Logger> loggerList = Arrays.asList(logger1, logger2);

        for (Logger logger : loggerList) {
            LoggerConfig config = logger.getConfig();
            FileAppender appender = logger.getAppender();

            assertEquals(Level.WARN, logger.getLogLevel());
            assertEquals(100, config.maxMessageLength);
            assertFalse(config.includeMethod);
            assertFalse(config.includeLineNumber);
            assertTrue(config.includePackage);
            assertEquals(ZoneId.of("UTC-4"), config.formatter.getZone());
            assertFalse(config.fileOutputEnabled);
            assertEquals(4096, appender.getRollSize());
            assertTrue(appender.isRolling());
        }
    }

    @Test
    public void testInvalidConfig() throws IOException {
        Logger internal = Logger.getInternalLogger();
        internal.getConfig().setStdErrLevel(Level.OFF);
        ByteArrayOutputStream buffer = Mocks.mockStdout(internal);

        No4JConfiguration.configure("test/resources/invalid_config.ini");
        assertEquals(0, No4JConfiguration.get().loggers.size());
        // Assume there's some warnings about invalid properties
        assertTrue(buffer.size() > 0);
    }
}
