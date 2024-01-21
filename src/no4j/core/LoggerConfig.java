package no4j.core;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LoggerConfig {
    /**
     * Whether to write to standard output - if set to false, disables printing. Enabled by default.
     */
    volatile boolean writeToConsole = true;

    /**
     * Whether to write to file. Disabled by default.
     */
    volatile boolean writeToFile = true;

    /**
     * Log file object to write logs to. Null by default.
     */
    volatile File fileOutput = null;

    /**
     * Minimum level to direct output to STDERR instead of STDOUT. Level.ERROR by default.
     */
    volatile Level minStdErrLevel = Level.ERROR;

    /**
     * Include method information where log was made. Enabled by default.
     */
    volatile boolean includeMethod = true;

    /**
     * The number of additional spaces counting from 0th character of the logging level's name
     * to include in the formatted string. Minimum value: 4
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
    volatile DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    LoggerConfig() {
    }

    public void writeToConsole(boolean enabled) {
        writeToConsole = enabled;
    }

    public void writeToFile(boolean enabled) {
        writeToFile = enabled;
    }

    public void includeMethod(boolean enabled) {
        includeMethod = enabled;
    }

    public void setFileOutput(File logFile) {
        if (logFile == null) {
            return;
        }
        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile()) {
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileOutput = logFile;
    }

    public void setLevelPadLength(int length) {
        if (length < 4) {
            length = 4;
        }
        levelPadLength = length;
    }

    public void setMethodPadLength(int length) {
        if (length < 23) {
            length = 23;
        }
        methodPadLength = length;
    }
    public void setFormatter(DateTimeFormatter formatter) {
        if (formatter != null) {
            this.formatter = formatter;
        }
    }

    public void setMinStdErrLevel(Level minLevel) {
        minStdErrLevel = minLevel;
    }

    public static LoggerConfig create() {
        return new LoggerConfig();
    }

    public LoggerConfig clone() {
        LoggerConfig config = new LoggerConfig();
        config.writeToFile = this.writeToFile;
        config.writeToConsole = this.writeToConsole;
        config.fileOutput = this.fileOutput;
        config.minStdErrLevel = this.minStdErrLevel;
        config.includeMethod = this.includeMethod;
        return config;
    }

    @Override
    public String toString() {
        return "LoggerConfig{" +
                "writeToConsole=" + writeToConsole +
                ", writeToFile=" + writeToFile +
                ", fileOutput=" + fileOutput +
                ", minStdErrLevel=" + minStdErrLevel +
                ", includeMethod=" + includeMethod +
                '}';
    }
}
