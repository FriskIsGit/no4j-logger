package no4j.core;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static no4j.Mocks.mockStderr;
import static no4j.Mocks.mockStdout;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoggerTest {
    @Test
    public void testLoggersAreTheSame() {
        assertEquals(Logger.getLogger("any"), Logger.getLogger("any"));
    }

    @Test
    public void testMessageOutput() {
        Logger logger = getSimpleTestLogger(Level.WARN);
        ByteArrayOutputStream buffer = mockStdout(logger);

        logger.warn("TEST");
        assertTrue(buffer.toString().contains("[WARN] TEST"));
    }

    @Test
    public void testNothingGetsLogged() {
        Logger logger = getSimpleTestLogger(Level.OFF);
        ByteArrayOutputStream buffer = mockStdout(logger);

        logger.debug("d");
        logger.info("i");
        logger.warn("w");
        logger.error("e");
        logger.fatal("f");
        logger.unreachable("u");
        assertEquals(0, buffer.size());
    }

    @Test
    public void testLogFatal() {
        Logger logger = getSimpleTestLogger(Level.FATAL);
        ByteArrayOutputStream buffer = mockStdout(logger);

        logger.debug("d");
        logger.info("i");
        logger.warn("w");
        logger.error("e");
        assertEquals(0, buffer.size());
        logger.fatal("f");
        assertTrue(buffer.size() > 0);
    }

    @Test
    public void testTimeFormatter() {
        Logger logger = getSimpleTestLogger(Level.ALL);
        logger.getConfig().setFormatter(LoggerConfig.TIME_FORMATTER);
        ByteArrayOutputStream buffer = mockStdout(logger);

        logger.info("Time");
        String line = buffer.toString();
        assertEquals(8, line.indexOf(' '));
        String time = line.substring(0, 8);
        String[] split = time.split(":");
        assertEquals(3, split.length);
    }

    @Test
    public void testStderrRedirectAtWarn() {
        Logger logger = Logger.getLoggerWithLevel("test", Level.ALL);
        logger.getConfig().setStdErrLevel(Level.WARN);
        ByteArrayOutputStream outBuffer = mockStdout(logger);
        ByteArrayOutputStream errBuffer = mockStderr(logger);

        logger.info("i");
        assertEquals(errBuffer.size(), 0);
        int stdoutSize = outBuffer.size();
        assertTrue(stdoutSize > 0);
        logger.warn("w");
        assertTrue(errBuffer.size() > 0);
        // Also ensure STDOUT size hasn't changed
        assertEquals(stdoutSize, outBuffer.size());
    }

    @Test
    public void testStderrRedirectEverything() {
        Logger logger = Logger.getLoggerWithLevel("test", Level.ALL);
        logger.getConfig().setStdErrLevel(Level.ALL);
        ByteArrayOutputStream outBuffer = mockStdout(logger);
        ByteArrayOutputStream errBuffer = mockStderr(logger);

        logger.debug("d");
        logger.info("i");
        logger.warn("w");
        logger.fatal("f");
        assertEquals(0, outBuffer.size());
        assertTrue(errBuffer.size() > 0);
    }

    private static Logger getSimpleTestLogger(Level level) {
        Logger logger = Logger.getLoggerWithLevel("test", level);
        LoggerConfig config = logger.getConfig();
        config.setStdErrLevel(Level.OFF);
        config.includeMethod(false);
        config.setLevelPadLength(0);
        config.setMethodPadLength(0);
        return logger;
    }
}
