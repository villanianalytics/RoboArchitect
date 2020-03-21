package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import lombok.Cleanup;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

/**
 * Command with attributes.
 */
public abstract class AbstractCommand {
    /**
     * System logger.
     */
    private Logger logger;
    /**
     * Settings for logger.
     */
    private static final String LOG_PROPERTIES_FILE = "log4j.properties";
    /**
     * Output logger file key.
     */
    private static final String LOG_FILE_PROPERTY_SETTINGS = "log4j.appender.file.File";
    /**
     * Logs file attribute.
     */
    private static final String LOG_ATTRIBUTE = "/log";
    /**
     * Loader for local project files.
     */
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    /**
     * Config file attribute.
     */
    private static final String CONFIG_ATTRIBUTE = "/config";
    /**
     * All attributes and there values.
     */
    private HashMap<String, String> attributes;

    public AbstractCommand(String[] args) throws IOException, MissedParameterException {
        attributes = new HashMap<>();

        initParam(args);
        addProperties();
        writeLogsToFile();
    }

    /**
     * Command execution must be realized here.
     */
    public abstract void execute() throws Exception;

    /**
     * Change logging file destination property if needed.
     *
     * @throws IOException thrown in case of an I/O error
     */
    private void writeLogsToFile() throws IOException, MissedParameterException {
        if (attributes.containsKey(LOG_ATTRIBUTE)) {
            String logFile = attributes.get(LOG_ATTRIBUTE);
            if (!logFile.isEmpty()) {
                System.setProperty("log.file", logFile);
                initLogFile(logFile);
            } else {
                throw new MissedParameterException("You enter empty log file.");
            }
        } else {
            System.setProperty("log.file", "logs.log");
            initLogFile("logs.log");
        }
        logger = Logger.getLogger(AbstractCommand.class);
        logger.debug("Changed.");
    }

    private void initLogFile(String logFile) throws IOException {
        Properties properties = new Properties();
        @Cleanup InputStream resourceAsStream = loader.getResourceAsStream(LOG_PROPERTIES_FILE);
        properties.load(resourceAsStream);
        properties.put(LOG_FILE_PROPERTY_SETTINGS, logFile);
        String logLevel = getAttribute("/logLevel");
        if (logLevel != null) {
            properties.put("log4j.appender.stdout.Threshold", logLevel);
            properties.put("log4j.appender.file.Threshold", logLevel);
        }
        else
        {
            properties.put("log4j.appender.stdout.Threshold", "INFO");
            properties.put("log4j.appender.file.Threshold", "INFO");
        }
        PropertyConfigurator.configure(properties);
    }

    private void addProperties() throws MissedParameterException, IOException {
        if (attributes.containsKey(CONFIG_ATTRIBUTE)) {
            Properties properties = new Properties();
            File configFile = Paths.get(attributes.get(CONFIG_ATTRIBUTE)).toFile();
            if (configFile.exists() && configFile.isFile()) {
                @Cleanup FileReader reader = new FileReader(configFile);
                properties.load(reader);

                for (String key : properties.stringPropertyNames()) {
                    attributes.put(key, properties.getProperty(key));
                }
            } else {
                throw new MissedParameterException("Config file \"" + configFile + "\" is missed or wrong.");
            }
        }
    }

    /**
     * Get value from attribute if it is not null or from config file.
     *
     * @param configKey field of a config file.
     * @param attribute attribute.
     * @return value of an attribute.
     */
    public String getFromConfOrAttribute(String configKey, String attribute) {
        String value;
        String attr = attributes.get(attribute);
        String field = attributes.get(configKey);
        if (attr != null) {
            value = attr;
        } else {
            value = field;
        }
        return value;
    }

    /**
     * Check if Commands contain expected parameter. If it contains return value of this parameter.
     *
     * @param args Commands.
     */
    private void initParam(String[] args) {
        for (int i = 1; i < args.length; i++) {
            int equalIndex = args[i].indexOf('=');
            if (equalIndex != -1) {
                HashMap<String, String> splitValue = getAttributeAndValue(args, i, equalIndex);
                attributes.putAll(splitValue);
            } else {
                attributes.put(args[i], "");
            }
        }
    }

    /**
     * Split input attribute by index.
     *
     * @param args       Commands
     * @param i          Needful attribute by a number.
     * @param equalIndex First equal string index.
     * @return Split attribute.
     */
    private static HashMap<String, String> getAttributeAndValue(String[] args, int i, int equalIndex) {
        HashMap<String, String> splitValue = new HashMap<>();
        if (equalIndex != (args[i].length() - 1)) {
            splitValue.put(args[i].substring(0, equalIndex), args[i].substring(equalIndex + 1));
        } else {
            splitValue.put(args[i].substring(0, equalIndex), "");
        }
        return splitValue;
    }

    /**
     * Get value from an attribute.
     *
     * @param attribute Attribute.
     * @return value of an attribute.
     */
    public String getAttribute(String attribute) {
        return attributes.get(attribute);
    }

    /**
     * Get attribute value, if value is missed it returns null.
     *
     * @param attribute attribute.
     * @return attribute value.
     * @throws MissedParameterException when required data.
     */
    public String getRequiredAttribute(String attribute) throws MissedParameterException {
        logger.debug("Start getting required \"" + attribute + "\"");
        if (attributes.containsKey(attribute)) {
            String value = attributes.get(attribute);
            logger.debug("Value \"" + value + "\" was gotten");
            if (value.isEmpty()) {
                throw new MissedParameterException("Attribute " + attribute + " is required but it was empty.");
            }
            return value;
        } else {
            throw new MissedParameterException("Attribute " + attribute + " was missed.");
        }
    }

    public String getDefaultAttribute(String attribute, String defaultValue) {
        String value = getAttribute(attribute);
        return (value == null) ? defaultValue : value;
    }
}
