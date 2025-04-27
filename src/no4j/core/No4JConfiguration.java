package no4j.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Global singleton configuration class
 */
public class No4JConfiguration {
    public static final String VERSION = "1.2.0";
    private static No4JConfiguration config;

    private static final String LOGGER_NAME = "name"; // unique name
    private static final String LOGGER_LEVEL = "level"; // integer/level name
    private static final String LOGGER_MESSAGE_MAX_LEN = "msg_length"; // integer value
    private static final String LOGGER_MESSAGE_METHOD = "msg_method"; // boolean
    private static final String LOGGER_MESSAGE_LINE_NUMBER = "msg_line_number"; // boolean
    private static final String LOGGER_MESSAGE_PACKAGE = "msg_package"; // boolean
    private static final String LOGGER_MESSAGE_STACK_TRACE_DEPTH = "msg_stack_trace_depth"; // integer
    private static final String LOGGER_CONSOLE_USE_COLOR = "console_use_color"; // boolean
    private static final String LOGGER_CONSOLE_ENABLED = "console_enabled"; // boolean
    private static final String LOGGER_FILE_ENABLED = "file_enabled"; // boolean
    private static final String LOGGER_FILE = "file_out"; // file path
    private static final String LOGGER_FILE_ROLLING_SIZE = "file_rolling_size"; // size in bytes
    private static final String LOGGER_FILE_ROLLING_ENABLED = "file_rolling_enabled"; // boolean
    private static final String LOGGER_STDERR_LEVEL = "stderr_level"; // integer/level name
    private static final String DATE_PATTERN = "date_pattern"; // date format pattern
    private static final String DATE_ZONE = "date_zone"; // date zone (Instant requires zone)

    private static final String LOGGER_INHERIT = "inherit"; // existing (symbolic) logger name

    private static final String CONFIGURATION_FILENAME = "no4j.ini"; // file path

    private static final ZoneId UTC0 = ZoneId.ofOffset("UTC", ZoneOffset.UTC);

    final List<Logger> loggers = Collections.synchronizedList(new ArrayList<>());

    private No4JConfiguration() {}

    /**
     * Returns the configuration singleton
     */
    public static No4JConfiguration get() {
        if (config == null) {
            config = new No4JConfiguration();
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
        Logger internalLogger = Logger.getInternalLogger();

        File file = new File(propertiesPath);
        if (!file.exists()) {
            file = new File("src/main/resources/" + propertiesPath);
        }
        if (!file.exists()) {
            internalLogger.error("Unable to locate " + CONFIGURATION_FILENAME);
            return;
        }
        if (!file.canRead()) {
            internalLogger.error("Unable to read " + CONFIGURATION_FILENAME);
            return;
        }

        List<String> lines = Files.readAllLines(file.toPath());
        No4JConfiguration configuration = new No4JConfiguration();
        HashMap<String, HashMap<String, String>> symbolToProperties = readIniFile(lines);

        List<String> inheritable = new ArrayList<>();
        // 1. Create loggers from symbols and properties. Gather inheritable loggers.
        for (String loggerSymbol : symbolToProperties.keySet()) {
            HashMap<String, String> properties = symbolToProperties.get(loggerSymbol);

            String loggerName = properties.get(LOGGER_NAME);
            if (loggerName == null) {
                internalLogger.warn("No name specified for logger [" + loggerSymbol + "]. Skipping");
                continue;
            }
            
            if (properties.containsKey(LOGGER_INHERIT)) {
                inheritable.add(loggerSymbol);
            }
            Logger logger = new Logger(loggerName);
            configureLogger(logger, properties);
            configuration.loggers.add(logger);
        }

        // 2. Inherit properties
        for (String symbol : inheritable) {
            HashMap<String, String> properties = symbolToProperties.get(symbol);
            String inheritFromSymbol = properties.get(LOGGER_INHERIT);

            HashMap<String, String> fromProperties = symbolToProperties.get(inheritFromSymbol);
            if (fromProperties == null) {
                internalLogger.error("Unsatisfied inheritance: from '" + inheritFromSymbol + "' to '" + symbol + "'");
                continue;
            }

            Logger fromLogger = configuration.getLogger(fromProperties.get(LOGGER_NAME));
            Logger toLogger = configuration.getLogger(properties.get(LOGGER_NAME));
            if (fromLogger == null || toLogger == null) {
                internalLogger.error("Cannot derive properties between two loggers as one of them is null. " +
                        "This shouldn't be possible");
                continue;
            }
            toLogger.inheritProperties(fromLogger);
        }

        // System.out.println(properties);
        No4JConfiguration.config = configuration;
    }

    private static void configureLogger(Logger logger, HashMap<String, String> properties) {
        Logger internalLogger = Logger.getInternalLogger();
        for (HashMap.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            switch (key) {
                case LOGGER_LEVEL:
                case LOGGER_STDERR_LEVEL:
                    Level level = Level.byName(value);
                    if (level == null) {
                        internalLogger.error("The level '" + value + "' does not match any default logging levels.");
                        continue;
                    }
                    if (key.equals(LOGGER_LEVEL)) {
                        logger.loggingLevel = level;
                    } else {
                        logger.config.stdErrLevel = level;
                    }
                    break;
                case LOGGER_MESSAGE_MAX_LEN:
                    try {
                        int maxLength = Integer.parseInt(value);
                        logger.config.setMaxMessageLength(maxLength);
                    } catch (NumberFormatException e) {
                        internalLogger.exception(e);
                    }
                    break;
                case LOGGER_CONSOLE_ENABLED:
                    logger.config.consoleOutputEnabled = Boolean.parseBoolean(value);
                    break;
                case LOGGER_CONSOLE_USE_COLOR:
                    logger.console.enableColor(Boolean.parseBoolean(value));
                    break;
                case LOGGER_FILE_ENABLED:
                    logger.config.fileOutputEnabled = Boolean.parseBoolean(value);
                    break;
                case LOGGER_MESSAGE_METHOD:
                    logger.config.includeMethod = Boolean.parseBoolean(value);
                    break;
                case LOGGER_MESSAGE_LINE_NUMBER:
                    logger.config.includeLineNumber = Boolean.parseBoolean(value);
                    break;
                case LOGGER_MESSAGE_PACKAGE:
                    logger.config.includePackage = Boolean.parseBoolean(value);
                    break;
                case LOGGER_MESSAGE_STACK_TRACE_DEPTH:
                    try {
                        logger.config.maxStackTraceDepth = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        internalLogger.exception(e);
                    }
                    break;
                case LOGGER_FILE:
                    File logFile = new File(value);
                    logger.setOutput(logFile);
                    break;
                case LOGGER_FILE_ROLLING_SIZE:
                    try {
                        long sizeInBytes = Long.parseLong(value);
                        logger.fileAppender.setRollSize(sizeInBytes);
                    } catch (NumberFormatException e) {
                        internalLogger.exception(e);
                    }
                    break;
                case LOGGER_FILE_ROLLING_ENABLED:
                    boolean isRolling = Boolean.parseBoolean(value);
                    logger.fileAppender.setRolling(isRolling);
                    break;
                case DATE_PATTERN:
                    logger.config.formatter = DateTimeFormatter.ofPattern(value).withZone(UTC0);
                    break;
                case DATE_ZONE:
                    ZoneId zoneId = ZoneId.of(value);
                    logger.config.formatter = logger.config.formatter.withZone(zoneId);
                    break;
                case LOGGER_NAME:
                case LOGGER_INHERIT:
                    break;
                default:
                    internalLogger.warn("Unrecognized key suffix in '" + key + '\'');
            }
        }
    }

    // This method is only used internally during configuration, so it's single-threaded
    private Logger getLogger(String name) {
        for (Logger logger : loggers) {
            if (logger.getName().equals(name)) {
                return logger;
            }
        }
        return null;
    }

    private static HashMap<String, HashMap<String, String>> readIniFile(List<String> lines) {
        Logger internalLogger = Logger.getInternalLogger();
        HashMap<String, HashMap<String, String>> symbolToProperties = new HashMap<>();
        String currentSymbol = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }

            int lastIndex = line.length() - 1;
            if (line.charAt(0) == '[' && line.charAt(lastIndex) == ']') {
                if (line.length() == 2) {
                    internalLogger.error("Empty logger declaration symbol '[]' (line " + (i+1) + ")");
                    continue;
                }
                currentSymbol = line.substring(1, lastIndex);
                continue;
            }
            if (currentSymbol == null) {
                internalLogger.warn("Untied declaration '" + line + "' (line " + (i+1) + ")");
                continue;
            }
            int equal = line.indexOf('=');
            if (equal < 1 || equal + 1 >= line.length()) {
                internalLogger.error("Invalid key=value pair '" + line + "' (line: " + (i+1) + ")");
                continue;
            }
            String propertyName = line.substring(0, equal).trim();
            String propertyValue = line.substring(equal + 1).trim();

            HashMap<String, String> properties = symbolToProperties.computeIfAbsent(currentSymbol, k -> new HashMap<>());
            properties.put(propertyName, propertyValue);
        }
        return symbolToProperties;
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

    public static String getVersion() {
        return VERSION;
    }
}
