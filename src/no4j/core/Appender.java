package no4j.core;

/**
 * Generic log appender. Loggers forward messages to the {@link Appender#log} method
 * to be further used with more advanced, usually framework-specific constructs
 */

public interface Appender {
    void log(LogMessage message);
}
