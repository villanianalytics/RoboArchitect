package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Get part of a JSON from a file.
 */
public class JsonPathCommand extends AbstractCommand {
    private Logger logger = Logger.getLogger(JsonPathCommand.class);
    /**
     * Source csv file command line parameter.
     */
    public static final String SRC_FILE_PARAM = "/srcFile";
    /**
     * Destination xlsx file command line parameter.
     */
    public static final String DEST_FILE_PARAM = "/destFile";
    /**
     * Json filter attribute.
     */
    public static final String JSON_PATH_PARAM = "/jsonPath";

    @Command("-jsonpath")
    public JsonPathCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws IOException, MissedParameterException {
        jsonpath();
    }

    /**
     * Initializes all required fields and filters JSON from a file.
     *
     * @throws MissedParameterException Throws if expected command is wrong or missed.
     * @throws IOException              thrown in case of an I/O error
     */
    private void jsonpath() throws MissedParameterException, IOException {
        String srcFile = getRequiredAttribute(SRC_FILE_PARAM);
        String jsonPath = getRequiredAttribute(JSON_PATH_PARAM);
        String destFile = getAttribute(DEST_FILE_PARAM);

        jsonFileFilter(srcFile, jsonPath, destFile);
    }

    /**
     * Reads file with JSON and filters it and write to file if destination file is not empty.
     *
     * @param srcFile  Source file with JSON.
     * @param jsonPath JSON path filter pattern.
     * @param destFile Destination result file.
     * @throws IOException thrown in case of an I/O error
     */
    private void jsonFileFilter(String srcFile, String jsonPath, String destFile) throws IOException {
        String json = FileUtils.readFileToString(Paths.get(srcFile).toFile(), UTF_8);
        String formattedJson = jsonFilter(jsonPath, json);
        if (destFile != null && !destFile.isEmpty()) {
            Path destPath = Paths.get(destFile);
            File file = destPath.toAbsolutePath().toFile();
            File destination = file.getParentFile();
            if (destination.exists()) {
                logger.debug("Destination of a file " + destination + " does not exist so began to create it.");
                boolean mkdir = destination.mkdirs();
                logger.debug("Destination file directory was created: " + mkdir);
            }

            FileUtils.write(file, formattedJson, UTF_8);
        }
        logger.info("Json filtering done.");
    }

    private String prettyJsonFormatter(String response) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement element = JsonParser.parseString(response);
        return gson.toJson(element);
    }

    private String getJsonPath(String jsonBody, String jsonPath) {
        logger.debug("Read Json body " + jsonBody + " jsonPath " + jsonPath);
        String results = JsonPath.read(jsonBody, jsonPath).toString();
        logger.debug("Done.");
        return results;
    }

    private String jsonFilter(String jsonPath, String json) {
        String formattedMessage = prettyJsonFormatter(getJsonPath(json, jsonPath));
        logger.info(formattedMessage);
        return formattedMessage;
    }
}
