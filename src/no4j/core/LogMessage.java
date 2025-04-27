package no4j.core;

import java.io.Serializable;

public class LogMessage implements Serializable {
    public String time;
    public Level level;
    public String message;
    public String method;
    public StackTraceElement[] stack;

    public LogMessage(String time, Level level, String message, String method) {
        this.time = time;
        this.level = level;
        this.message = message;
        this.method = method;
    }

    public LogMessage(String time, Level level, String message, String method, StackTraceElement[] stack) {
        this.time = time;
        this.level = level;
        this.message = message;
        this.method = method;
        this.stack = stack;
    }
}
