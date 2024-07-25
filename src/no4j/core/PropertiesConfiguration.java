package no4j.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Global singleton configuration class
 */
public class PropertiesConfiguration {
    private static PropertiesConfiguration config;
    private static final String LOGGER_NAME = ".name"; // unique name

    private static final String LOGGER_LEVEL = ".level"; // integer/level name
    private static final String LOGGER_MESSAGE_MAX_LEN = ".msg.length"; // integer value

    private static final String LOGGER_CONSOLE_ENABLED = ".console.enabled"; // boolean
    private static final String LOGGER_FILE_ENABLED = ".file.enabled"; // boolean
    private static final String LOGGER_FILE = ".file.out"; // file path
    private static final String LOGGER_FILE_ROLLING_SIZE = ".file.rolling.size"; // size in bytes
    private static final String LOGGER_FILE_ROLLING_ENABLED = ".file.rolling.enabled"; // boolean
    private static final String LOGGER_STDERR_LEVEL = ".stderr.level"; // integer/level name
    private static final String DATE_PATTERN = ".date.pattern"; // date format pattern
    private static final String DATE_ZONE = ".date.zone"; // date zone (Instant requires zone)

    private static final String LOGGER_INHERIT = ".inherit"; // existing (symbolic) logger name

    private static final String CONFIGURATION_FILENAME = "no4j.properties"; // file path

    private static final ZoneId UTC0 = ZoneId.ofOffset("UTC", ZoneOffset.UTC);

    List<Logger> loggers = Collections.synchronizedList(new ArrayList<>());

    /**
     * Returns the configuration singleton
     */
    public static PropertiesConfiguration get() {
        if (config == null) {
            config = new PropertiesConfiguration();
        }
        return config;
    }

    /**
     * Initializes framework according to the properties file
     */
    public static void configure() throws IOException {
        configure(CONFIGURATION_FILENAME);
    }

    /**
     * Initializes framework according to the properties file denoted by given path argument
     */
    public static void configure(String propertiesPath) throws IOException {
        File file = new File(propertiesPath);
        if (!file.exists()) {
            file = new File("src/main/resources/" + propertiesPath);
        }
        if (!file.exists()) {
            System.err.println("Unable to locate no4j.properties");
            return;
        }
        if (!file.canRead()) {
            System.err.println("Unable to read no4j.properties");
            return;
        }

        List<String> lines = Files.readAllLines(file.toPath());
        PropertiesConfiguration configuration = new PropertiesConfiguration();
        HashMap<String, String> properties = readProperties(lines);

        HashMap<String, String> symbolToName = new HashMap<>();
        // 1. Create logger symbols
        for(HashMap.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith(LOGGER_NAME)) {
                String loggerSymbol = key.substring(0, key.length() - LOGGER_NAME.length());
                String name = entry.getValue();
                symbolToName.put(loggerSymbol, entry.getValue());
                configuration.loggers.add(new Logger(name));
            }
        }

        Logger internalLogger = Logger.getInternalLogger();
        // 2. Read logger configurations
        for(HashMap.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith(LOGGER_STDERR_LEVEL)) {
                Logger logger = configuration.getConfigLogger(key, LOGGER_STDERR_LEVEL, symbolToName);
                if (logger == null) {
                    continue;
                }
                String value = entry.getValue();
                Level stdErrLevel = Level.byName(value);
                if (stdErrLevel == null) {
                    internalLogger.error("The level '" + value + "' does not match any default logging levels.");
                    continue;
                }
                logger.config.stdErrLevel = stdErrLevel;
            }
            else if (key.endsWith(LOGGER_LEVEL)) {
                Logger logger = configuration.getConfigLogger(key, LOGGER_LEVEL, symbolToName);
                if (logger == null) {
                    continue;
                }
                String value = entry.getValue();
                Level loggingLevel = Level.byName(value);
                if (loggingLevel == null) {
                    internalLogger.error("The level '" + value + "' does not match any default logging levels.");
                    continue;
                }
                logger.loggingLevel = loggingLevel;
            }
            else if (key.endsWith(LOGGER_MESSAGE_MAX_LEN)) {
                Logger logger = configuration.getConfigLogger(key, LOGGER_MESSAGE_MAX_LEN, symbolToName);
                if (logger == null) {
                    continue;
                }
                String value = entry.getValue();
                logger.config.maxMessageLength = Integer.parseInt(value);
            }
            else if (key.endsWith(LOGGER_CONSOLE_ENABLED)) {
                Logger logger = configuration.getConfigLogger(key, LOGGER_CONSOLE_ENABLED, symbolToName);
                if (logger == null) {
                    continue;
                }
                logger.config.consoleOutputEnabled = Boolean.parseBoolean(entry.getValue());
            }
            else if (key.endsWith(LOGGER_FILE_ENABLED)) {
                Logger logger = configuration.getConfigLogger(key, LOGGER_FILE_ENABLED, symbolToName);
                if (logger == null) {
                    continue;
                }
                logger.config.fileOutputEnabled = Boolean.parseBoolean(entry.getValue());
            }
            else if (key.endsWith(LOGGER_FILE)) {
                Logger logger = configuration.getConfigLogger(key, LOGGER_FILE, symbolToName);
                if (logger == null) {
                    continue;
                }
                File logFile = new File(entry.getValue());
                logger.setOutput(logFile);
            }
            else if (key.endsWith(LOGGER_FILE_ROLLING_SIZE)) {
                Logger logger = configuration.getConfigLogger(key, LOGGER_FILE_ROLLING_SIZE, symbolToName);
                if (logger == null) {
                    continue;
                }
                long sizeInBytes = Long.parseLong(entry.getValue());
                logger.fileAppender.setRollSize(sizeInBytes);
            }
            else if (key.endsWith(LOGGER_FILE_ROLLING_ENABLED)) {
                Logger logger = configuration.getConfigLogger(key, LOGGER_FILE_ROLLING_ENABLED, symbolToName);
                if (logger == null) {
                    continue;
                }
                boolean isRolling = Boolean.parseBoolean(entry.getValue());
                logger.fileAppender.setRolling(isRolling);
            }
            else if (key.endsWith(DATE_PATTERN)) {
                Logger logger = configuration.getConfigLogger(key, DATE_PATTERN, symbolToName);
                if (logger == null) {
                    continue;
                }
                String pattern = entry.getValue();
                logger.config.formatter = DateTimeFormatter.ofPattern(pattern).withZone(UTC0);
            }
            else if (key.endsWith(DATE_ZONE)) {
                Logger logger = configuration.getConfigLogger(key, DATE_ZONE, symbolToName);
                if (logger == null) {
                    continue;
                }
                String zoneIdentifier = entry.getValue();
                logger.config.formatter = logger.config.formatter.withZone(ZoneId.of(zoneIdentifier));
            } else if (!key.endsWith(LOGGER_NAME) && !key.endsWith(LOGGER_INHERIT)) {
                internalLogger.warn("Unrecognized key suffix in '" + key + '\'');
            }
        }

        // 3. Inherit properties
        for(HashMap.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (!key.endsWith(LOGGER_INHERIT)) {
                continue;
            }
            Logger toLogger = configuration.getConfigLogger(key, LOGGER_INHERIT, symbolToName);
            if (toLogger == null) {
                continue;
            }
            Logger fromLogger = configuration.getLogger(entry.getValue());
            if (fromLogger == null) {
                continue;
            }
            toLogger.inheritProperties(fromLogger);
        }

        // System.out.println(properties);
        PropertiesConfiguration.config = configuration;
    }

    private Logger getConfigLogger(String key, String suffix, HashMap<String, String> symbolToName) {
        String loggerSymbol = key.substring(0, key.length() - suffix.length());
        String loggerName = symbolToName.get(loggerSymbol);
        return this.getLogger(loggerName);
    }

    private Logger getLogger(String name) {
        for(Logger logger : loggers) {
            if (logger.getName().equals(name)) {
                return logger;
            }
        }
        return null;
    }

    private static HashMap<String, String> readProperties(List<String> lines) {
        HashMap<String, String> properties = new HashMap<>();
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            if (line.charAt(0) == '#') {
                continue;
            }
            int equal = line.indexOf('=');
            if (equal < 1 || equal + 1 >= line.length()) {
                continue;
            }
            String propertyName = line.substring(0, equal).trim();
            String propertyValue = line.substring(equal + 1).trim();
            properties.put(propertyName, propertyValue);
        }
        return properties;
    }

    /**
     * Resets framework and configures
     */
    public static void reconfigure() throws IOException {
        Logger.getGlobalLogger().setLoggingLevel(Level.OFF);
        configure();
    }

    /**
     * Resets framework and reconfigures according to the properties file denoted by given path argument
     */
    public static void reconfigure(String propertiesPath) throws IOException {
        Logger.getGlobalLogger().setLoggingLevel(Level.OFF);
        configure(propertiesPath);
    }
}
