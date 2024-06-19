package no4j.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public class Logger {
    private final String name;

    private static final int STACK_INDEX = 3;
    private static final Logger globalLogger = new Logger("global");

    LoggerConfig config = LoggerConfig.create();
    /**
     * Logging level to apply when logging to standard out and the log file
     */
    Level loggingLevel = Level.OFF;

    Logger(String name) {
        this.name = name;
    }

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
     * If a logger with given name is not found, a new logger is created and returned.
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

    public void exception(Throwable throwable) {
        logMessage(throwable.getMessage(), Level.ERROR);
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
        String output = formatMessage(level, message, method);
        if (config.writeToConsole) {
            if (level.value > config.stdErrLevel.value) {
                System.out.print(output);
            } else {
                System.err.print(output);
            }
        }

        if (config.writeToFile && config.fileOutput != null) {
            try {
                Files.write(config.fileOutput, output.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public String formatMessage(Level level, String message, String method) {
        Instant instant = Instant.now();
        StringBuilder format = new StringBuilder(100);
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

    public void inheritProperties(Logger logger) {
        this.loggingLevel = logger.loggingLevel;
        this.config = logger.config.copy();
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
