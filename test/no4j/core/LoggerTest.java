package no4j.core;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import static no4j.Mocks.mockStderr;
import static no4j.Mocks.mockStdout;
import static org.junit.Assert.*;

public class LoggerTest {
    private static final boolean PRINT_COLOR_TESTS = true;
    private static final int AT_INDENT = 2;
    // Output tests
    @Test
    public void testMessageOutput() {
        Logger logger = getTestLogger(Level.WARN);
        ByteArrayOutputStream buffer = mockStdout(logger);

        logger.warn("TEST");
        assertTrue(buffer.toString().contains("[WARN]  TEST"));
    }

    @Test
    public void testCustomLogOutput() {
        Logger logger = getTestLogger(Level.ALL);
        ByteArrayOutputStream buffer = mockStdout(logger);
        Level db = Level.custom(1000, "database");
        assertNotNull(db);

        final String message = "SELECT * FROM users";
        logger.log(message, db);
        String expectedToContain = "[" + db.name + "]  " + message;
        assertTrue(buffer.toString().contains(expectedToContain));
    }

    @SuppressWarnings("all")
    @Test
    public void testGetNullLogger() {
        Logger logger = Logger.getLogger(null);
        assertNull(logger);
    }

    @Test
    public void testNothingGetsLogged() {
        Logger logger = getTestLogger(Level.OFF);
        ByteArrayOutputStream outBuffer = mockStdout(logger), errBuffer = mockStderr(logger);

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
        ByteArrayOutputStream outBuffer = mockStdout(logger), errBuffer = mockStderr(logger);

        logger.log("At 'OFF' nothing is logged", Level.OFF);
        assertEquals(0, outBuffer.size());
        assertEquals(0, errBuffer.size());
    }

    @Test
    public void testNothingGetsLoggedAtNull() {
        Logger logger = getTestLogger(Level.ALL);
        ByteArrayOutputStream outBuffer = mockStdout(logger), errBuffer = mockStderr(logger);

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
    public void testStackTrace() {
        Logger logger = getTestLogger(Level.ALL);
        final String className = "StackTest";
        final int depth = 3;
        Exception exception = new Exception();
        exception.setStackTrace(createStackTrace(className, depth));
        ByteArrayOutputStream buffer = mockStdout(logger);

        logger.stackTrace("Hello stack trace!", exception);
        String[] lines = buffer.toString().split("\n");
        assertEquals(depth, lines.length);
        int firstMethod = lines[0].indexOf(className);
        assertTrue(firstMethod > 0);
        assertEquals(firstMethod + AT_INDENT, lines[1].indexOf("at"));
        assertEquals(firstMethod + AT_INDENT, lines[2].indexOf("at"));
    }

    @Test
    public void testColoredStackTrace() {
        Logger logger = getTestLogger(Level.ALL);
        Color boldRed = Color.fgBold(Color.FG_RED);
        logger.console.enableColor(true);
        logger.console.setError(boldRed);

        final String className = "ColorTest";
        final int depth = 4;
        Exception exception = new Exception();
        exception.setStackTrace(createStackTrace(className, depth));
        ByteArrayOutputStream buffer = mockStdout(logger);

        logger.stackTrace("Colored stack!", exception);
        String[] lines = buffer.toString().split("\n");
        assertEquals(depth, lines.length);
        for (int i = 1; i < depth; i++) {
            String line = lines[i];
            int at = line.indexOf("at");
            assertTrue(at > 0);

            String sgr = trim(line.substring(0, at), '\n', ' ');
            assertEquals(boldRed.sgr, sgr);
        }
    }

    @Test
    public void testTimeFormatter() {
        Logger logger = getTestLogger(Level.ALL);
        logger.getConfig().setFormatter(LoggerConfig.TIME_FORMATTER);
        ByteArrayOutputStream buffer = mockStdout(logger);

        logger.info("Time");
        String line = buffer.toString();
        assertEquals(10, line.indexOf(' '));
        // Presuming it's wrapped in []
        String time = line.substring(1, 9);
        String[] split = time.split(":");
        assertEquals(3, split.length);
    }

    @Test
    public void testStderrRedirectAtWarn() {
        Logger logger = getTestLogger(Level.ALL);
        logger.getConfig().setStdErrLevel(Level.WARN);
        ByteArrayOutputStream outBuffer = mockStdout(logger), errBuffer = mockStderr(logger);

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
        ByteArrayOutputStream outBuffer = mockStdout(logger), errBuffer = mockStderr(logger);

        logger.debug("d");
        logger.info("i");
        logger.warn("w");
        logger.fatal("f");
        assertEquals(0, outBuffer.size());
        assertTrue(errBuffer.size() > 0);
    }

    // Logger storage tests
    @Test
    public void testLoggersAreTheSame() {
        final String name = "same";
        assertEquals(Logger.getLogger(name), Logger.getLogger(name));
    }

    @Test
    public void testLoggerRetention() {
        final String name = "retention";
        Logger previousLogger = Logger.getLogger(name);
        assertTrue(Logger.removeLogger(previousLogger));
        Logger newLogger = Logger.getLogger(name);
        assertNotEquals(previousLogger, newLogger);
        // Removing the previous logger should have no effect now
        assertFalse(Logger.removeLogger(previousLogger));
    }

    // Color tests
    @Test
    public void testNoColor() {
        Logger logger = getTestLogger(Level.INFO);
        final String time = "time", message = "Plain text", method = "method()";
        LogMessage logMessage = new LogMessage(time, Level.INFO, message, method);
        final String expected = "[" + time + ']' + " [" + Level.INFO + "] " + method + ' ' + message + '\n';

        StringBuilder format = logger.formatMessage(logMessage, false);
        String actual = format.toString();
        if (PRINT_COLOR_TESTS) {
            System.out.println(actual);
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testGreenForegroundColor() {
        Color green = Color.fg(Color.FG_GREEN);
        testColor(green, "Green foreground");
    }

    @Test
    public void testYellowForegroundUnderline() {
        Color yellowUnderline = Color.fgUnderline(Color.FG_YELLOW);
        testColor(yellowUnderline, "Yellow underline");
    }

    @Test
    public void testBlackForegroundWhiteBackground() {
        Color mixed = Color.of(Color.FG_BLACK, Color.BG_WHITE);
        testColor(mixed, "Black and white");
    }

    @Test
    public void testCyanFgPurpleBgRGB() {
        Color cyanPurple = Color.rgb(33, 214, 220, 149, 55, 206);
        testColor(cyanPurple, "Cyan and purple RGB");
    }

    private static void testColor(Color color, final String message) {
        Logger logger = getTestLogger(Level.INFO);
        logger.console.setInfo(color);
        final String time = "time", method = "method()";
        LogMessage logMessage = new LogMessage(time, Level.INFO, message, method);

        final String expected = "[" + time + "] " + color + "[" + Level.INFO + "] "
                + method + ' ' + message + Color.RESET + '\n';

        StringBuilder format = logger.formatMessage(logMessage, true);
        String actual = format.toString();
        if (PRINT_COLOR_TESTS) {
            System.out.println(actual);
        }
        assertEquals(expected, actual);
    }

    public static String trim(String str, char ...toTrim) {
        int len = str.length();
        int st = 0;

        while (st < len) {
            if (equalsAny(str.charAt(st), toTrim)) {
                st++;
            } else {
                break;
            }
        }
        while (st < len) {
            if (equalsAny(str.charAt(len-1), toTrim)) {
                len--;
            } else {
                break;
            }
        }
        return str.substring(st, len);
    }

    private static boolean equalsAny(char c, char ...chars) {
        for (char chr : chars) {
            if (c == chr) {
                return true;
            }
        }
        return false;
    }

    private static StackTraceElement[] createStackTrace(String className, int depth) {
        final String fileName = "Test.java";
        StackTraceElement[] elements = new StackTraceElement[depth];
        Random random = new Random();
        for (int i = 0; i < depth; i++) {
            StackTraceElement el = new StackTraceElement(className,
                    "method" + (i+1),
                    fileName,
                    random.nextInt(500)
            );
            elements[i] = el;
        }
        return elements;
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
