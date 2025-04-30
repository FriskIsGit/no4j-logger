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

    public String toJson() {
        return "{" +
                "\"time\":\"" + jsonEncode(time) + '\"' +
                ",\"level\":\"" + jsonEncode(level.name) + '\"' +
                ",\"message\":\"" + jsonEncode(message) + '\"' +
                ",\"method\":\"" + jsonEncode(method) + '\"' +
                '}';
    }

    protected static String jsonEncode(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder escaped = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '/':
                    escaped.append("\\/");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
