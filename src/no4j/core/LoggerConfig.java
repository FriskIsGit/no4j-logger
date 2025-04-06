package no4j.core;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LoggerConfig {
    public static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    /**
     * Maximum number of characters allowed in a message. Messages exceeding the limit are trimmed.
     */
    volatile int maxMessageLength = Integer.MAX_VALUE - 32;

    /**
     * Whether to write to standard output - if set to false, disables printing. Enabled by default.
     */
    volatile boolean consoleOutputEnabled = true;

    /**
     * Whether to write to file. Disabled by default.
     */
    volatile boolean fileOutputEnabled = true;

    /**
     * Level at or below which to direct output to STDERR instead of STDOUT. Level.ERROR by default.
     */
    volatile Level stdErrLevel = Level.ERROR;

    /**
     * Include method information where log was made (if available). Enabled by default.
     */
    volatile boolean includeMethod = true;

    /**
     * Include line number in method information (if available). Enabled by default.
     */
    volatile boolean includeLineNumber = true;

    /**
     * Include package prefix before class name in method information. Disabled by default.
     */
    volatile boolean includePackage = false;

    /**
     * The number of additional spaces counting from 0th character of the logging level's name
     * to include in the formatted string. Set with {@link #setLevelPadLength}. Minimum value: 4
     */
    volatile int levelPadLength = 14;

    /**
     * The number of additional spaces counting from 0th character of the method's trace
     * to include in the formatted string. Minimum value: 23
     */
    volatile int methodPadLength = 30;

    /**
     * The date formatter to use for formatting the time of the log
     */
    volatile DateTimeFormatter formatter = FULL_DATE_FORMATTER;

    LoggerConfig() {
    }

    public void setMaxMessageLength(int messageLength) {
        if (0 <= messageLength && messageLength <= Integer.MAX_VALUE - 32) {
            maxMessageLength = messageLength;
        }
    }

    public void enableConsoleOutput(boolean enabled) {
        consoleOutputEnabled = enabled;
    }

    public void enableFileOutput(boolean enabled) {
        fileOutputEnabled = enabled;
    }

    public void includeMethod(boolean enabled) {
        includeMethod = enabled;
    }

    public void setLevelPadLength(int length) {
        if (length < 4) {
            length = 4;
        }
        levelPadLength = length;
    }

    public void setMethodPadLength(int length) {
        if (length < 0) {
            length = 0;
        }
        methodPadLength = length;
    }

    public void setFormatter(DateTimeFormatter formatter) {
        if (formatter != null) {
            this.formatter = formatter;
        }
    }

    public void setStdErrLevel(Level minLevel) {
        stdErrLevel = minLevel;
    }

    public static LoggerConfig create() {
        return new LoggerConfig();
    }

    public LoggerConfig copy() {
        LoggerConfig config = new LoggerConfig();
        config.maxMessageLength = maxMessageLength;
        config.consoleOutputEnabled = consoleOutputEnabled;
        config.fileOutputEnabled = fileOutputEnabled;
        config.formatter = formatter;
        config.stdErrLevel = stdErrLevel;

        config.includeLineNumber = includeLineNumber;
        config.includeMethod = includeMethod;
        config.includePackage = includePackage;
        config.methodPadLength = methodPadLength;
        config.levelPadLength = levelPadLength;

        return config;
    }
}
