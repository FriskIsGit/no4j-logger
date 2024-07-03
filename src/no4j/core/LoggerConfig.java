package no4j.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LoggerConfig {
    /**
     * Whether to write to standard output - if set to false, disables printing. Enabled by default.
     */
    volatile boolean consoleOutputEnabled = true;

    /**
     * Whether to write to file. Disabled by default.
     */
    volatile boolean fileOutputEnabled = true;

    /**
     * Lock for safe concurrent access and swap of the {@link FileAppender}
     */
    final Object appenderLock = new Object();
    /**
     * FileAppender object. Null by default.
     */
    volatile FileAppender fileAppender = null;

    /**
     * Level at or below which to direct output to STDERR instead of STDOUT. Level.ERROR by default.
     */
    volatile Level stdErrLevel = Level.ERROR;

    /**
     * Include method information where log was made. Enabled by default.
     */
    volatile boolean includeMethod = true;

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
    volatile DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    LoggerConfig() {
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

    public void setOutput(File logFile) throws IOException {
        if (logFile == null || logFile.isDirectory()) {
            return;
        }
        safelySwapAppender(new FileAppender(logFile.toPath()));
    }
    public void setOutput(Path logFile) throws IOException {
        if (logFile == null || Files.isDirectory(logFile)) {
            return;
        }
        safelySwapAppender(new FileAppender(logFile));
    }

    private void safelySwapAppender(FileAppender newAppender) throws IOException {
        synchronized (appenderLock) {
            if (fileAppender != null) {
                fileAppender.shutOff();
            }
            fileAppender = newAppender;
        }
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

    public void setRolling(boolean enabled) {
        this.fileAppender.enableRolling(enabled);
    }

    public void setRollingSize(long bytes) {
        this.fileAppender.setMaxSize(bytes);
    }

    public void setStdErrLevel(Level minLevel) {
        stdErrLevel = minLevel;
    }

    public static LoggerConfig create() {
        return new LoggerConfig();
    }

    public LoggerConfig copy() {
        LoggerConfig config = new LoggerConfig();
        config.fileOutputEnabled = this.fileOutputEnabled;
        config.consoleOutputEnabled = this.consoleOutputEnabled;
        config.fileAppender = this.fileAppender;
        config.stdErrLevel = this.stdErrLevel;
        config.includeMethod = this.includeMethod;
        return config;
    }

    @Override
    public String toString() {
        return "LoggerConfig{" +
                "enableConsoleOutput=" + consoleOutputEnabled +
                ", enableFileOutput=" + fileOutputEnabled +
                ", fileOutput=" + fileAppender +
                ", minStdErrLevel=" + stdErrLevel +
                ", includeMethod=" + includeMethod +
                '}';
    }
}
