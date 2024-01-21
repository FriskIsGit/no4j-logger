package no4j.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Global singleton configuration class
 */
public class PropertiesConfiguration {
    private static PropertiesConfiguration config;
    private static final String LOGGER_NAME = ".name"; // unique name

    private static final String LOGGER_LEVEL = ".level"; // integer/level name
    private static final String LOGGER_CONSOLE_ENABLED = ".console.enabled"; // boolean
    private static final String LOGGER_FILE_ENABLED = ".file.enabled"; // boolean
    private static final String LOGGER_STDERR_LEVEL = ".stderr.level"; // integer/level name
    private static final String LOGGER_FILE = ".file.out"; // file path

    private static final String LOGGER_INHERIT = ".inherit"; // existing logger name

    private static final String CONFIGURATION_FILENAME = "no4j.properties"; // file path

    ArrayList<Logger> loggers = new ArrayList<>();

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
    public static void configure() {
        File file = new File(CONFIGURATION_FILENAME);
        if (!file.exists()) {
            file = new File("src/main/resources/" + CONFIGURATION_FILENAME);
        }
        if (!file.exists()) {
            System.err.println("Unable to locate no4j.properties");
            return;
        }
        if (!file.canRead()) {
            System.err.println("Unable to read no4j.properties");
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
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

        // 2. Read logger configurations
        for(HashMap.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith(LOGGER_STDERR_LEVEL)) {
                String loggerSymbol = key.substring(0, key.length() - LOGGER_STDERR_LEVEL.length());
                String loggerName = symbolToName.get(loggerSymbol);
                Logger logger = configuration.getLogger(loggerName);
                if (logger == null) {
                    continue;
                }
                String value = entry.getValue();
                Level loggingLevel = Level.byName(value);
                if (loggingLevel == null) {
                    continue;
                }
                logger.config.minStdErrLevel = Level.byName(value);
            }
            else if (key.endsWith(LOGGER_LEVEL)) {
                String loggerSymbol = key.substring(0, key.length() - LOGGER_LEVEL.length());
                String loggerName = symbolToName.get(loggerSymbol);
                Logger logger = configuration.getLogger(loggerName);
                if (logger == null) {
                    continue;
                }
                String value = entry.getValue();
                Level loggingLevel = Level.byName(value);
                if (loggingLevel == null) {
                    continue;
                }
                logger.loggingLevel = Level.byName(value);
            }
            else if (key.endsWith(LOGGER_CONSOLE_ENABLED)) {
                String loggerSymbol = key.substring(0, key.length() - LOGGER_CONSOLE_ENABLED.length());
                String loggerName = symbolToName.get(loggerSymbol);
                Logger logger = configuration.getLogger(loggerName);
                if (logger == null) {
                    continue;
                }
                logger.config.writeToConsole = Boolean.parseBoolean(entry.getValue());
            }
            else if (key.endsWith(LOGGER_FILE_ENABLED)) {
                String loggerSymbol = key.substring(0, key.length() - LOGGER_FILE_ENABLED.length());
                String loggerName = symbolToName.get(loggerSymbol);
                Logger logger = configuration.getLogger(loggerName);
                if (logger == null) {
                    continue;
                }
                logger.config.writeToFile = Boolean.parseBoolean(entry.getValue());
            }
            else if (key.endsWith(LOGGER_FILE)) {
                String loggerSymbol = key.substring(0, key.length() - LOGGER_FILE.length());
                String loggerName = symbolToName.get(loggerSymbol);
                Logger logger = configuration.getLogger(loggerName);
                if (logger == null) {
                    continue;
                }
                logger.config.setFileOutput(new File(entry.getValue()));
            }
        }

        // 3. Inherit properties
        for(HashMap.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (!key.endsWith(LOGGER_INHERIT)) {
                continue;
            }
            String loggerSymbol = key.substring(0, key.length() - LOGGER_INHERIT.length());
            String loggerName = symbolToName.get(loggerSymbol);
            Logger toLogger = configuration.getLogger(loggerName);
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
    public static void reconfigure() {
        Logger.getGlobalLogger().setLoggingLevel(Level.OFF);
        configure();
    }
}
