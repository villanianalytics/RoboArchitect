package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.opencsv.CSVWriter;
import lombok.Cleanup;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.relique.jdbc.csv.CsvDriver;

import java.io.*;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.relique.jdbc.csv.CsvDriver.*;

public class QueryDelimCommand extends AbstractCommand {
    private final Logger logger = Logger.getLogger(QueryDelimCommand.class);
    private static final String SRC_FILE = "/srcFile";
    private static final String DELIM = "/delim";
    private static final String QUERY = "/query";
    private static final String DEST_FILE = "/destFile";
    private static final String SUPPRESS_HEADERS = "/suppressHeaders";
    private static final String SKIP_LINES = "/skipLines";
    private static final String SKIP_DATA_LINES = "/skipDataLines";
    private static final String DEFAULT_ZERO_VALUE = "0";
    private static final String DEFAULT_FALSE_VALUE = "false";
    private static final String DEFAULT_COMMA_DELIM = ",";

    @Command("-querydelim")
    public QueryDelimCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws MissedParameterException, IOException, SQLException, ClassNotFoundException {
    	logger.trace("QueryDelimCommand.execute started");
    	
        String srcFile = getRequiredAttribute(SRC_FILE);
        String delimStr = getDefaultAttribute(DELIM, DEFAULT_COMMA_DELIM);
        String query = getRequiredAttribute(QUERY);
        String destinationFile = getAttribute(DEST_FILE);
        String suppressHeaders = getDefaultAttribute(SUPPRESS_HEADERS, DEFAULT_FALSE_VALUE);
        String skipLines = getDefaultAttribute(SKIP_LINES, DEFAULT_ZERO_VALUE);
        String skipDataLines = getDefaultAttribute(SKIP_DATA_LINES, DEFAULT_ZERO_VALUE);

        char delim = delimStr.charAt(0);

        String extension = ("." + FilenameUtils.getExtension(srcFile));
        srcFile = Paths.get(srcFile).toAbsolutePath().toFile().getParent();

        queryFile(srcFile, delim, extension, query, destinationFile, suppressHeaders, skipLines, skipDataLines);
    }


    private void queryFile(String srcFile, char delim, String fileExtension, String fileQuery, String outputFile, String suppressHeaders, String skipLines, String skipDataLines) throws SQLException, IOException, ClassNotFoundException {
        logger.trace("QueryDelimCommand.queryFile started");
    	Class.forName(CsvDriver.class.getCanonicalName());

        Properties props = new Properties();
        props.put(SEPARATOR, delim);
        props.put(FILE_EXTENSION, fileExtension);
        props.put(MISSING_VALUE, "");
        props.put(TRIM_HEADERS, "" + true);
        props.put(CsvDriver.SUPPRESS_HEADERS, suppressHeaders);
        props.put(SKIP_LEADING_LINES, skipLines);
        props.put(SKIP_LEADING_DATA_LINES, skipDataLines);
        props.put(DEFECTIVE_HEADERS, "" + true);

        String url = URL_PREFIX + srcFile;
        @Cleanup Connection conn = DriverManager.getConnection(url, props);

        Statement stmt = conn.createStatement();

        ResultSet results = stmt.executeQuery(fileQuery);

        @Cleanup ByteArrayOutputStream stream = new ByteArrayOutputStream();
        @Cleanup Writer writerString = new OutputStreamWriter(stream);
        @Cleanup CSVWriter writer = new CSVWriter(writerString);
        writer.writeAll(results, true);
        writer.flush();

        String result = stream.toString();

        if (outputFile != null) {
            FileUtils.write(Paths.get(outputFile).toFile(), result, UTF_8);
            logResponse("Finished with success, result written to " + Paths.get(outputFile).getFileName());
        } else {
        	logResponse(result);
        }
    }
}
