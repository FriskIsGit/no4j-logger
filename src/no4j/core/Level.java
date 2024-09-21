package no4j.core;

import java.io.Serializable;
import java.util.Locale;

/**
 * The {@code Level} class represents different logging levels.
 * Level.OFF < Level.ALL
 */
public class Level implements Serializable {

    protected static final int OFF_VALUE = 0;

    protected static final int UNREACHABLE_VALUE = 1;
    protected static final int FATAL_VALUE = 20;
    protected static final int ERROR_VALUE = 30;
    protected static final int WARN_VALUE = 40;
    protected static final int INFO_VALUE = 50;
    protected static final int DEBUG_VALUE = 60;

    protected static final int ALL_VALUE = Integer.MAX_VALUE;

    /**
     * This level turns off a logger.
     */
    public static final Level OFF = new Level(OFF_VALUE, "OFF");
    /**
     * The <code>UNREACHABLE</code> level is intended to mark unreachable parts of code.
     */
    public static final Level UNREACHABLE = new Level(UNREACHABLE_VALUE, "UNREACHABLE");
    /**
     * The <code>FATAL</code> level is intended for unrecoverable errors.
     */
    public static final Level FATAL = new Level(FATAL_VALUE, "FATAL");
    /**
     * The <code>ERROR</code> level is intended for recoverable errors.
     */
    public static final Level ERROR = new Level(ERROR_VALUE, "ERROR");
    /**
     * The <code>WARN</code> level is intended for non-threatening errors or unexpected parameters.
     */
    public static final Level WARN = new Level(WARN_VALUE, "WARN");
    /**
     * The <code>INFO</code> level is intended purely for informational purposes.
     */
    public static final Level INFO = new Level(INFO_VALUE, "INFO");
    /**
     * The <code>DEBUG</code> level is intended for logging implementation specific finer details
     */
    public static final Level DEBUG = new Level(DEBUG_VALUE, "DEBUG");
    /**
     * The <code>ALL</code> level logs all messages
     */
    public static final Level ALL = new Level(ALL_VALUE, "ALL");


    public final int value;
    public final String name;

    protected Level(int value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * Creates a new instance of Level
     */

    public static Level custom(int value, String name) {
        if (value <= OFF_VALUE) {
            return null;
        }
        return new Level(value, name);
    }

    /**
     * Returns a level corresponding to its value, if not found null is returned
     */
    public static Level toLevel(final int val) {
        switch (val) {
            case OFF_VALUE:
                return OFF;
            case UNREACHABLE_VALUE:
                return Level.UNREACHABLE;
            case FATAL_VALUE:
                return Level.FATAL;
            case ERROR_VALUE:
                return Level.ERROR;
            case WARN_VALUE:
                return Level.WARN;
            case INFO_VALUE:
                return Level.INFO;
            case DEBUG_VALUE:
                return Level.DEBUG;
            case ALL_VALUE:
                return Level.ALL;
            default:
                return null;
        }
    }

    public static Level byName(String level) {
        switch (level.toUpperCase(Locale.ENGLISH)) {
            case "OFF":
                return OFF;
            case "UNREACHABLE":
                return Level.UNREACHABLE;
            case "FATAL":
                return Level.FATAL;
            case "ERROR":
                return Level.ERROR;
            case "WARN":
                return Level.WARN;
            case "INFO":
                return Level.INFO;
            case "DEBUG":
                return Level.DEBUG;
            case "ALL":
                return Level.ALL;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}