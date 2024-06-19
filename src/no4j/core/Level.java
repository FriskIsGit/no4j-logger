package no4j.core;

import java.io.Serializable;
import java.util.Locale;

/**
 * The {@code Level} class represents different logging levels.
 * Level.OFF < Level.ALL
 */
public class Level implements Serializable {
    /**
     * This level turns off a logger.
     */
    public static final Level OFF = new Level(0, "OFF");
    /**
     * The <code>UNREACHABLE</code> and is intended to mark unreachable parts of code.
     */
    public static final Level UNREACHABLE = new Level(1, "UNREACHABLE");
    public static final Level FATAL = new Level(20, "FATAL");
    public static final Level ERROR = new Level(30, "ERROR");
    public static final Level WARN = new Level(40, "WARN");
    public static final Level INFO = new Level(50, "INFO");
    public static final Level DEBUG = new Level(60, "DEBUG");
    public static final Level ALL = new Level(Integer.MAX_VALUE, "ALL");

    protected static final int OFF_VALUE = Level.OFF.value;

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
        return new Level(value, name);
    }

    /**
     * Returns a level corresponding to its value, if not found null is returned
     */
    public static Level toLevel(final int val) {
        switch (val) {
            case 0:
                return OFF;
            case 1:
                return Level.UNREACHABLE;
            case 20:
                return Level.FATAL;
            case 30:
                return Level.ERROR;
            case 40:
                return Level.WARN;
            case 50:
                return Level.INFO;
            case 60:
                return Level.DEBUG;
            case 70:
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
