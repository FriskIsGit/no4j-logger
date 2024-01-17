package no4j.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class Logger {
    private final String name;

    private static final int STACK_INDEX = 3;
    private static final Logger globalLogger = new Logger("global");

    LoggerConfig config = LoggerConfig.create();
    Level loggingLevel = Level.OFF;

    Logger(String name) {
        this.name = name;
    }

    public static Logger getGlobalLogger() {
        return globalLogger;
    }

    public static Logger getAnonymousLogger() {
        return new Logger(null);
    }

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
        if (this.loggingLevel.value == Level.OFF_VALUE || this.loggingLevel.value < level.value) {
            return;
        }
        String method = "";
        if (config.includeMethod) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            if (STACK_INDEX < stack.length) {
                method = stack[STACK_INDEX].toString();
            }
        }
        String output = "[" + level + "] " + method + " " + message + '\n';
        if (config.writeToConsole) {
            if (level.value > config.minStdErrLevel.value) {
                System.out.print(output);
            } else {
                System.err.print(output);
            }
        }

        if (config.writeToFile && config.fileOutput != null) {
            try {
                Files.write(config.fileOutput.toPath(), output.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        //System.out.println("LEVEL: " + new Object(){}.getClass().getEnclosingMethod().getName());
    }

    public void inheritProperties(Logger logger) {
        this.loggingLevel = logger.loggingLevel;
        this.config = logger.config.clone();
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
