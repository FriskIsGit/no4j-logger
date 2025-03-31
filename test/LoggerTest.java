import no4j.core.Console;
import no4j.core.Level;
import no4j.core.Logger;
import no4j.core.LoggerConfig;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
        assertEquals(0, buffer.toString().length());
    }

    @Test
    public void testLogFatal() {
        Logger logger = getSimpleTestLogger(Level.FATAL);
        ByteArrayOutputStream buffer = mockStdout(logger);

        logger.debug("d");
        logger.info("i");
        logger.warn("w");
        logger.error("e");
        assertEquals(0, buffer.toString().length());
        logger.fatal("f");
        assertTrue(buffer.toString().length() > 0);
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

    private static Logger getSimpleTestLogger(Level level) {
        Logger logger = Logger.getLoggerWithLevel("test", level);
        LoggerConfig config = logger.getConfig();
        config.setStdErrLevel(Level.OFF);
        config.includeMethod(false);
        config.setLevelPadLength(0);
        config.setMethodPadLength(0);
        return logger;
    }

    private static ByteArrayOutputStream mockStdout(Logger logger) {
        Console console = logger.getConsole();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(buffer);
        console.setStdOut(outStream);
        return buffer;
    }
}
