package no4j.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * The central class used for logging
 */
public class Logger {
    private final String name;

    private static final int STACK_INDEX = 3;

    private static final Logger globalLogger = new Logger("global");
    private static final Logger internalLogger;

    static {
        internalLogger = new Logger("internal");
        internalLogger.setLoggingLevel(Level.WARN);
        internalLogger.config.methodPadLength = 80;
    }

    /**
     * <code>FileAppender</code> object. One per logger.
     */
    final FileAppender fileAppender = new FileAppender();

    /**
     * <code>Console</code> object. One per logger.
     */
    final Console console = Console.newDefault();

    LoggerConfig config = LoggerConfig.create();

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
     * Creates an anonymous logger. The newly created logger is not stored in the list of loggers.
     */
    public static Logger getAnonymousLogger() {
        return new Logger(null);
    }

    /**
     * Returns a valid logger as long as the name is not null.
     * If logger with the given name is not found, a new logger is created, stored in the list of loggers, then returned.
     * @return logger with the given name or <tt>null</tt>
     */
    public static Logger getLogger(String name) {
        if (name == null) {
            return null;
        }
        PropertiesConfiguration configuration = PropertiesConfiguration.get();
        for (Logger logger : configuration.loggers) {
            if (logger.name.equals(name)) {
                return logger;
            }
        }
        Logger logger = new Logger(name);
        configuration.loggers.add(logger);
        return logger;
    }

    public static Logger getLoggerWithLevel(String name, Level level) {
        Logger logger = getLogger(name);
        logger.setLoggingLevel(level);
        return logger;
    }

    /**
     * Removes a logger from the list of loggers.
     * @return <tt>true</tt> if the list contained the specified logger, otherwise <tt>false</tt>.
     */
    public static boolean removeLogger(Logger logger) {
        if (logger == null) {
            return false;
        }
        PropertiesConfiguration configuration = PropertiesConfiguration.get();
        return configuration.loggers.remove(logger);
    }

    public static int loggerCount() {
        return PropertiesConfiguration.get().loggers.size();
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

    public void exception(Throwable throwable) {
        logMessage(throwable.toString(), Level.ERROR);
    }

    private void logMessage(String message, Level level) {
        if (this.loggingLevel.value == Level.OFF_VALUE || level == null || this.loggingLevel.value < level.value) {
            return;
        }
        String method = "";
        if (config.includeMethod) {
            // This is not guaranteed to work in which case method will be empty
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            if (STACK_INDEX < stack.length) {
                method = stack[STACK_INDEX].toString();
            }
        }
        if (message != null && message.length() > config.maxMessageLength) {
            message = message.substring(0, config.maxMessageLength);
        }
        String output = formatMessage(level, message, method);
        if (config.consoleOutputEnabled) {
            if (level.value > config.stdErrLevel.value) {
                console.outPrint(output, level);
            } else {
                console.errPrint(output, level);
            }
        }

        if (config.fileOutputEnabled && fileAppender.isAttached()) {
            byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
            fileAppender.logToFile(bytes);
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
            internalLogger.exception(e);
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
            internalLogger.exception(e);
        }
    }

    /**
     * Releases the file handle (the output stream)
     */
    public void detachOutput() {
        try {
            fileAppender.detach();
        } catch (IOException e) {
            internalLogger.exception(e);
        }
    }

    public boolean isAttached() {
        return fileAppender.isAttached();
    }

    public FileAppender getAppender() {
        return fileAppender;
    }

    public Console getConsole() {
        return console;
    }

    public String formatMessage(Level level, String message, String method) {
        Instant instant = Instant.now();
        StringBuilder format = new StringBuilder(128);
        String levelName = level.toString();

        String time = config.formatter.format(instant);
        format.append(time);
        format.append(' ');
        format.append('[').append(levelName).append(']');
        int levelPadLen = config.levelPadLength - levelName.length();
        padWithSpaces(format, levelPadLen);
        format.append(method);
        format.append(' ');
        int methodPadLen = config.methodPadLength - method.length();
        padWithSpaces(format, methodPadLen);
        format.append(message);
        format.append('\n');
        return format.toString();
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
        this.console.inheritColors(logger.console);
    }

    @Override
    public String toString() {
        return "Logger{" +
                "name='" + name + '\'' +
                ", config=" + config +
                ", loggingLevel=" + loggingLevel +
                '}';
    }
}
