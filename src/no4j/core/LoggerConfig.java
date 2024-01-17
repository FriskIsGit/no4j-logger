package no4j.core;

import java.io.File;
import java.io.IOException;

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

    LoggerConfig() {}

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
