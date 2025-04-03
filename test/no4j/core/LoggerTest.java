package no4j.core;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static no4j.Mocks.mockStderr;
import static no4j.Mocks.mockStdout;
import static org.junit.Assert.*;

public class LoggerTest {

    @Test
    public void testMessageOutput() {
        Logger logger = getTestLogger(Level.WARN);
        ByteArrayOutputStream buffer = mockStdout(logger);

        logger.warn("TEST");
        assertTrue(buffer.toString().contains("[WARN] TEST"));
    }

    @Test
    public void testNothingGetsLogged() {
        Logger logger = getTestLogger(Level.OFF);
        ByteArrayOutputStream outBuffer = mockStdout(logger);
        ByteArrayOutputStream errBuffer = mockStderr(logger);

        logger.debug("d");
        logger.info("i");
        logger.warn("w");
        logger.error("e");
        logger.fatal("f");
        logger.unreachable("u");
        assertEquals(0, outBuffer.size());
        assertEquals(0, errBuffer.size());
    }

    @Test
    public void testNothingGetsLoggedAtOff() {
        Logger logger = getTestLogger(Level.ALL);
        ByteArrayOutputStream outBuffer = mockStdout(logger);
        ByteArrayOutputStream errBuffer = mockStderr(logger);
        logger.log("At 'OFF' nothing is logged", Level.OFF);
        assertEquals(0, outBuffer.size());
        assertEquals(0, errBuffer.size());
    }

    @Test
    public void testNothingGetsLoggedAtNull() {
        Logger logger = getTestLogger(Level.ALL);
        ByteArrayOutputStream outBuffer = mockStdout(logger);
        ByteArrayOutputStream errBuffer = mockStderr(logger);
        logger.log("At 'null' nothing is logged", null);
        assertEquals(0, outBuffer.size());
        assertEquals(0, errBuffer.size());
    }

    @Test
    public void testLogFatal() {
        Logger logger = getTestLogger(Level.FATAL);
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
        Logger logger = getTestLogger(Level.ALL);
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
        Logger logger = getTestLogger(Level.ALL);
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
        Logger logger = getTestLogger(Level.ALL);
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

    @Test
    public void testLoggersAreTheSame() {
        assertEquals(Logger.getLogger("same"), Logger.getLogger("same"));
    }

    @Test
    public void testLoggerRetention() {
        Logger previousLogger = Logger.getLogger("retention");
        assertTrue(Logger.removeLogger(previousLogger));
        Logger newLogger = Logger.getLogger("retention");
        assertNotEquals(previousLogger, newLogger);
        // Removing the previous logger should have no effect now
        assertFalse(Logger.removeLogger(previousLogger));
    }

    private static Logger getTestLogger(Level level) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLoggingLevel(level);
        LoggerConfig config = logger.getConfig();
        config.setStdErrLevel(Level.OFF);
        config.includeMethod(false);
        config.setLevelPadLength(0);
        config.setMethodPadLength(0);
        return logger;
    }
}
