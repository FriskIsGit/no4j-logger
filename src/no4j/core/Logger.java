package no4j.core;

import no4j.extensions.LoggerBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The central class used for logging
 */
public class Logger {
    private final String name;

    private static final int STACK_INDEX = 3;

    private static final Logger internalLogger = LoggerBuilder.warning("internal")
            .methodPadLength(64)
            .getLogger();
    private static final Logger globalLogger = new Logger("global");

    /**
     * <code>FileAppender</code> object. One per logger.
     */
    final FileAppender fileAppender = new FileAppender();

    /**
     * <code>Console</code> object. One per logger.
     */
    final Console console = Console.newDefault();

    /**
     * <code>Appender</code> objects. Many per logger.
     */
    final List<Appender> appenders = new ArrayList<>();

    LoggerConfig config = LoggerConfig.create();

    ExceptionHandler handler = e -> Logger.getInternalLogger().exception(e);

    /**
     * Logging level to apply for logging to occur (applies to printing and file output)
     */
    Level loggingLevel = Level.OFF;

    Logger(String name) {
        this.name = name;
    }

    /**
     * The logging framework logger. Can be turned off just like any other logger.
     * Sometimes it's useful to know what causes errors internally
     */
    public static Logger getInternalLogger() {
        return internalLogger;
    }

    /**
     * Casual global logger
     */
    public static Logger getGlobalLogger() {
        return globalLogger;
    }

    /**
     * Creates an anonymous logger. The newly created logger is not stored in the list of loggers. Useful for testing.
     */
    public static Logger getAnonymousLogger() {
        return new Logger(null);
    }

    /**
     * Returns a valid logger as long as the name is not null.
     * If logger with the given name is not found, a new logger is created, stored in the list of loggers, then returned.
     *
     * @return logger with the given name or <tt>null</tt>
     */
    public static Logger getLogger(String name) {
        if (name == null) {
            return null;
        }
        No4JConfiguration configuration = No4JConfiguration.get();
        List<Logger> loggers = configuration.loggers;
        synchronized (loggers) {
            for (Logger logger : loggers) {
                if (logger.name.equals(name)) {
                    return logger;
                }
            }
        }

        Logger logger = new Logger(name);
        loggers.add(logger);
        return logger;
    }

    public static Logger getLoggerWithLevel(String name, Level level) {
        Logger logger = getLogger(name);
        logger.setLoggingLevel(level);
        return logger;
    }

    /**
     * Calls {@link Logger#getLogger}.
     * Then modifies it by enabling colored logging, reducing padding to 0 and shortening date format.
     * @return logger with the given name or <tt>null</tt>
     */
    public static Logger getPrettyLogger(String name) {
        Logger logger = getLogger(name);
        logger.getConsole().enableColor(true);
        LoggerConfig config = logger.getConfig();
        if (config.formatter == LoggerConfig.FULL_DATE_FORMATTER) {
            config.setFormatter(LoggerConfig.TIME_FORMATTER);
        }
        config.includeMethod(false);
        config.setLevelPadLength(0);
        config.setMethodPadLength(0);
        return logger;
    }

    /**
     * Removes a logger from the list of loggers.
     *
     * @return <tt>true</tt> if the list contained the specified logger, otherwise <tt>false</tt>.
     */
    public static boolean removeLogger(Logger logger) {
        if (logger == null) {
            return false;
        }
        No4JConfiguration configuration = No4JConfiguration.get();
        return configuration.loggers.remove(logger);
    }

    public static int loggerCount() {
        return No4JConfiguration.get().loggers.size();
    }

    public String getName() {
        return name;
    }

    public void setLoggingLevel(Level level) {
        loggingLevel = level;
    }

    public Level getLogLevel() {
        return loggingLevel;
    }

    public LoggerConfig getConfig() {
        return config;
    }

    /**
     * Attempts to log only if the supplied condition is true
     */
    public void logIf(boolean condition, String message, Level level) {
        if (condition) {
            logMessage(message, level);
        }
    }

    public void log(String message, Level level) {
        logMessage(message, level);
    }

    public void unreachable(String message) {
        logMessage(message, Level.UNREACHABLE);
    }

    public void fatal(String message) {
        logMessage(message, Level.FATAL);
    }

    public void error(String message) {
        logMessage(message, Level.ERROR);
    }

    public void warn(String message) {
        logMessage(message, Level.WARN);
    }

    public void info(String message) {
        logMessage(message, Level.INFO);
    }

    public void debug(String message) {
        logMessage(message, Level.DEBUG);
    }

    public void debug(Object object) {
        logMessage(object.toString(), Level.DEBUG);
    }

    public <T> void debug(T[] array) {
        logMessage(Arrays.toString(array), Level.DEBUG);
    }

    public void exception(Throwable throwable) {
        logMessage(throwable.toString(), Level.ERROR);
    }

    /**
     * Logs stack trace up to depth specified by {@link LoggerConfig#maxStackTraceDepth}
     */
    public void stackTrace(String message, Throwable throwable) {
        if (this.loggingLevel.value < Level.ERROR_VALUE) {
            return;
        }
        String time = config.formatter.format(Instant.now());
        StackTraceElement[] stack = throwable.getStackTrace();
        String firstMethod = stack.length > 0 ? stack[0].toString() : "";

        LogMessage logMessage = new LogMessage(time, Level.ERROR, message, firstMethod, stack);
        writeMessage(logMessage);
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        if (handler != null) {
            this.handler = handler;
        }
    }

    private void logMessage(String message, Level level) {
        if (level == null || level.value <= Level.OFF_VALUE || this.loggingLevel.value < level.value) {
            return;
        }
        Instant now = Instant.now();
        String method = "";
        if (config.includeMethod) {
            // This is not guaranteed to work in which case method will be empty
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            if (STACK_INDEX < stack.length) {
                method = stackElementToMethod(stack[STACK_INDEX]);
            }
        }
        if (message != null && message.length() > config.maxMessageLength) {
            message = message.substring(0, config.maxMessageLength);
        }
        String time = config.formatter.format(now);
        LogMessage logMessage = new LogMessage(time, level, message, method);
        writeMessage(logMessage);
    }

    public StringBuilder formatMessage(LogMessage msg, boolean applyColor) {
        Level level = msg.level;
        Color color = applyColor ? console.getColorByLevel(level) : null;
        applyColor = color != null;

        StringBuilder format = new StringBuilder(128);
        String levelName = msg.level.toString();

        format.append("[");
        format.append(msg.time);
        format.append("] ");

        if (applyColor) {
            format.append(color);
        }
        format.append("[");
        format.append(levelName).append("] ");

        int levelPadLen = config.levelPadLength - 1 - levelName.length();
        padWithSpaces(format, levelPadLen);

        int indent = format.length() - (applyColor ? color.sgr.length() : 0);

        format.append(msg.method);
        format.append(' ');
        int methodPadLen = config.methodPadLength - 1 - msg.method.length();
        padWithSpaces(format, methodPadLen);
        format.append(msg.message);
        if (applyColor) {
            format.append(Color.RESET);
        }
        format.append('\n');
        // Append stacktrace starting from index 1
        if (msg.stack != null) {
            if (applyColor) {
                appendRestOfStackTrace(msg.stack, format, indent + 2, color);
            } else {
                appendRestOfStackTrace(msg.stack, format, indent + 2, null);
            }
        }

        return format;
    }

    private void appendRestOfStackTrace(StackTraceElement[] stack, StringBuilder format, int indent, Color color) {
        boolean applyColor = color != null;
        for (int i = 1; i < stack.length && i < config.maxStackTraceDepth; i++) {
            padWithSpaces(format, indent);
            String element = stackElementToMethod(stack[i]);
            if (applyColor) {
                format.append(color);
            }
            format.append("at ").append(element);
            if (applyColor) {
                format.append(Color.RESET);
            }
            format.append('\n');
        }
    }

    // Based on the original StackTraceElement.toString()
    private String stackElementToMethod(StackTraceElement el) {
        StringBuilder format = new StringBuilder();

        if (config.includePackage) {
            format.append(el.getClassName());
        } else {
            String simpleClass = toSimpleClassName(el.getClassName());
            format.append(simpleClass);
        }
        format.append('.');
        format.append(el.getMethodName());

        if (el.isNativeMethod()) {
            format.append("(Native Method)");
        } else {
            String fileName = el.getFileName();
            int lineNumber = el.getLineNumber();

            format.append((fileName != null && lineNumber >= 0 && config.includeLineNumber) ?
                    "(" + fileName + ":" + lineNumber + ")" :
                    (fileName != null ?  "("+fileName+")" : "(Unknown Source)"));
        }

        return format.toString();
    }

    private static String toSimpleClassName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    }

    private void writeMessage(LogMessage logMessage) {
        if (config.consoleOutputEnabled) {
            String format = formatMessage(logMessage, console.isColorEnabled()).toString();
            if (logMessage.level.value > config.stdErrLevel.value) {
                console.outPrint(format);
            } else {
                console.errPrint(format);
            }
        }

        if (config.fileOutputEnabled && fileAppender.isAttached()) {
            StringBuilder format = formatMessage(logMessage, false);
            // There's no way to get bytes from a StringBuilder directly
            byte[] bytes = format.toString().getBytes(StandardCharsets.UTF_8);
            fileAppender.logToFile(bytes);
        }

        for (Appender appender : appenders) {
            appender.log(logMessage);
        }
    }

    /**
     * Sets log file output using a {@link File} argument.
     * The file given must not be null or a directory.
     * This creates a new handle which can be released with {@link #detachOutput}
     */
    public void setOutput(File logFile) {
        if (logFile == null || logFile.isDirectory()) {
            return;
        }
        try {
            fileAppender.attach(logFile.toPath());
        } catch (IOException e) {
            handler.handle(e);
        }
    }

    /**
     * Sets log file output using a {@link Path} argument.
     * The path given must not be null or represent a directory.
     * This creates a new handle which can be released with {@link #detachOutput}
     */
    public void setOutput(Path logFile) {
        if (logFile == null || Files.isDirectory(logFile)) {
            return;
        }
        try {
            fileAppender.attach(logFile);
        } catch (IOException e) {
            handler.handle(e);
        }
    }

    /**
     * Releases the file handle (the output stream)
     */
    public void detachOutput() {
        try {
            fileAppender.detach();
        } catch (IOException e) {
            handler.handle(e);
        }
    }

    public boolean isAttached() {
        return fileAppender.isAttached();
    }

    public FileAppender getAppender() {
        return fileAppender;
    }

    public void addAppender(Appender appender) {
        appenders.add(appender);
    }

    public Console getConsole() {
        return console;
    }

    private static void padWithSpaces(StringBuilder builder, int padLength) {
        for (int i = 0; i < padLength; i++) {
            builder.append(' ');
        }
    }

    /**
     * All properties are inherited except the {@link FileAppender} as writing to the same file
     * from different loggers could be undesired. Loggers are individually synchronized.
     */
    public void inheritProperties(Logger logger) {
        this.loggingLevel = logger.loggingLevel;
        this.config = logger.config.copy();

        this.fileAppender.setRolling(logger.fileAppender.isRolling());
        this.fileAppender.setRollSize(logger.fileAppender.getRollSize());

        this.console.enableColor(logger.console.isColorEnabled());
        this.console.inheritColors(logger.console);
    }

    @Override
    public String toString() {
        return "Logger{" +
                "name='" + name + '\'' +
                ", loggingLevel=" + loggingLevel +
                ", config=" + config +
                '}';
    }
}
