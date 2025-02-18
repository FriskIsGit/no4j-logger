package no4j.extensions;

import no4j.core.*;

import java.io.File;
import java.time.format.DateTimeFormatter;

/**
 * Class designed for creating loggers without knowing the internals
 */
public class LoggerBuilder {

    private final Logger logger;
    private final LoggerConfig config;
    private final FileAppender appender;
    private final Console console;

    private LoggerBuilder(Logger logger) {
        this.logger = logger;
        this.config = logger.getConfig();
        this.appender = logger.getAppender();
        this.console = logger.getConsole();
    }

    public static LoggerBuilder ofName(String loggerName) {
        Logger logger = Logger.getLogger(loggerName);
        return new LoggerBuilder(logger);
    }

    public static LoggerBuilder unreachable(String loggerName) {
        Logger logger = Logger.getLoggerWithLevel(loggerName, Level.UNREACHABLE);
        return new LoggerBuilder(logger);
    }

    public static LoggerBuilder fatal(String loggerName) {
        Logger logger = Logger.getLoggerWithLevel(loggerName, Level.FATAL);
        return new LoggerBuilder(logger);
    }

    public static LoggerBuilder error(String loggerName) {
        Logger logger = Logger.getLoggerWithLevel(loggerName, Level.ERROR);
        return new LoggerBuilder(logger);
    }

    public static LoggerBuilder warning(String loggerName) {
        Logger logger = Logger.getLoggerWithLevel(loggerName, Level.WARN);
        return new LoggerBuilder(logger);
    }

    public static LoggerBuilder info(String loggerName) {
        Logger logger = Logger.getLoggerWithLevel(loggerName, Level.INFO);
        return new LoggerBuilder(logger);
    }

    public static LoggerBuilder debug(String loggerName) {
        Logger logger = Logger.getLoggerWithLevel(loggerName, Level.DEBUG);
        return new LoggerBuilder(logger);
    }

    public static LoggerBuilder all(String loggerName) {
        Logger logger = Logger.getLoggerWithLevel(loggerName, Level.ALL);
        return new LoggerBuilder(logger);
    }

    public LoggerBuilder level(Level level) {
        logger.setLoggingLevel(level);
        return this;
    }

    public LoggerBuilder file(String logFile) {
        logger.setOutput(new File(logFile));
        config.enableFileOutput(true);
        return this;
    }

    public LoggerBuilder rollAtBytes(long bytes) {
        appender.setRollSize(bytes);
        appender.setRolling(true);
        return this;
    }

    public LoggerBuilder rollAtKilobytes(long kilobytes) {
        appender.setRollSize(kilobytes * 1024);
        appender.setRolling(true);
        return this;
    }

    public LoggerBuilder rollAtMegabytes(int megabytes) {
        appender.setRollSize((long) megabytes * 1024 * 1024);
        appender.setRolling(true);
        return this;
    }

    public LoggerBuilder maxMessageLength(int length) {
        config.setMaxMessageLength(length);
        return this;
    }

    public LoggerBuilder methodPadLength(int length) {
        config.setMethodPadLength(length);
        return this;
    }

    public LoggerBuilder levelPadLength(int length) {
        config.setLevelPadLength(length);
        return this;
    }

    public LoggerBuilder formatter(DateTimeFormatter formatter) {
        config.setFormatter(formatter);
        return this;
    }

    public LoggerBuilder timeFormatter() {
        config.setFormatter(LoggerConfig.TIME_FORMATTER);
        return this;
    }

    public LoggerBuilder fullDateFormatter() {
        config.setFormatter(LoggerConfig.FULL_DATE_FORMATTER);
        return this;
    }

    public LoggerBuilder unreachableColor(Color color) {
        console.setUnreachable(color);
        console.enableColor(true);
        return this;
    }

    public LoggerBuilder fatalColor(Color color) {
        console.setFatal(color);
        console.enableColor(true);
        return this;
    }

    public LoggerBuilder errorColor(Color color) {
        console.setError(color);
        console.enableColor(true);
        return this;
    }

    public LoggerBuilder warningColor(Color color) {
        console.setWarning(color);
        console.enableColor(true);
        return this;
    }

    public LoggerBuilder infoColor(Color color) {
        console.setInfo(color);
        console.enableColor(true);
        return this;
    }

    public LoggerBuilder debugColor(Color color) {
        console.setDebug(color);
        console.enableColor(true);
        return this;
    }

    public LoggerBuilder customColor(Color custom) {
        console.setCustom(custom);
        console.enableColor(true);
        return this;
    }

    public LoggerBuilder consoleOutput(boolean enable) {
        config.enableConsoleOutput(enable);
        return this;
    }

    public Logger getLogger() {
        return logger;
    }
}

