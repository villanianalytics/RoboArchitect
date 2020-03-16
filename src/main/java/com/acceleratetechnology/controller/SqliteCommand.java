package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.opencsv.*;
import lombok.Cleanup;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.sqlite.JDBC;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;

import static com.opencsv.CSVWriter.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Create, add or run SQL requests in SQLite Database.
 */
public class SqliteCommand extends AbstractCommand {
    /**
     * Run SQL command option. It could be {@link #CREATE_DB_OPTION}, {@link #IMPORT_TABLE_OPTION},
     * {@link #QUERY_DB_OPTION}.
     */
    private static final String OP_PARAMETER = "/op";
    /**
     * Database file.
     */
    private static final String DB_NAME_PARAMETER = "/db";
    /**
     * Source file.
     */
    private static final String SRC_FILE_PARAMETER = "/srcFile";
    /**
     * Database table name.
     */
    private static final String TABLE_PARAMETER = "/table";
    /**
     * Delimiter of specified file. By default it is {@link #DEFAULT_DELIM}.
     */
    private static final String DELIM_PARAMETER = "/delim";
    /**
     * Mode to add or overwrite database. By default it is {@link #DEFAULT_MODE}
     */
    private static final String MODE_PARAMETER = "/mode";
    /**
     * SQL request.
     */
    private static final String QUERY_PARAMETER = "/query";
    /**
     * Print SQL response.
     */
    private static final String RETURN_PARAMETER = "/return";
    /**
     * Destination file for SQL response.
     */
    private static final String DEST_FILE_PARAMETER = "/destFile";
    /**
     * If header need to be skipped in SQL response.
     */
    private static final String DEST_HEADER_PARAMETER = "/header";
    /**
     * Add data to the end of data base.
     */
    private static final String DEFAULT_MODE = "APPEND";
    /**
     * Default false answer.
     */
    private static final String DEFAULT_FALSE = "FALSE";
    /**
     * Default true answer.
     */
    private static final String DEFAULT_TRUE = "TRUE";
    /**
     * Default delim for delimited files.
     */
    private static final String DEFAULT_DELIM = ",";
    /**
     * Create database option of -sqlite command. Need to create new Database.
     */
    private static final String CREATE_DB_OPTION = "createDB";
    /**
     * Import table data option. Need to add data to Database.
     */
    private static final String IMPORT_TABLE_OPTION = "importTable";
    /**
     * Run SQL option for Database.
     */
    private static final String QUERY_DB_OPTION = "queryDB";
    /**
     * Overwrite database mode. Need to create and overwrite new database table.
     */
    private static final String OVERWRITE = "OVERWRITE";
    /**
     * File key for HashMap.
     */
    private static final String FILE = "file";
    /**
     * Delim key for HashMap.
     */
    private static final String DELIM = "delim";
    /**
     * String data type. Need to indicate Strings in Database.
     */
    private static final String VARCHAR_2_255 = " VARCHAR2(255)";
    /**
     * Double quotes need to surround data from csv file.
     */
    private static final String DOUBLE_QUOTES = "\"";
    /**
     * SQLite package directory.
     */
    private static final String JDBC_SQLITE = "jdbc:sqlite:";

    /**
     * System logger.
     */
    private Logger logger = Logger.getLogger(ConnectCommand.class);


    @Command("-sqlite")
    public SqliteCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws MissedParameterException, SQLException, IOException, ClassNotFoundException {
        String opCmd = getRequiredAttribute(OP_PARAMETER);
        String opDB = getRequiredAttribute(DB_NAME_PARAMETER);

        Class.forName(JDBC.class.getCanonicalName());
        File db = Paths.get(opDB).toAbsolutePath().toFile();
        logger.debug("File path is created: " + db.getParentFile().mkdirs());
        opDB = db.getCanonicalPath();

        switch (opCmd) {
            case CREATE_DB_OPTION:
                createDB(opDB);
                break;
            case IMPORT_TABLE_OPTION: {
                String srcFile = getRequiredAttribute(SRC_FILE_PARAMETER);
                String tableName = getRequiredAttribute(TABLE_PARAMETER);
                String delim = getDefaultAttribute(DELIM_PARAMETER, DEFAULT_DELIM);
                String updateMode = getDefaultAttribute(MODE_PARAMETER, DEFAULT_MODE);

                importTable(opDB, tableName, srcFile, delim.charAt(0), updateMode);
                break;
            }
            case QUERY_DB_OPTION: {
                String query = getRequiredAttribute(QUERY_PARAMETER);
                String returnString = getDefaultAttribute(RETURN_PARAMETER, DEFAULT_TRUE);
                String destFile = getAttribute(DEST_FILE_PARAMETER);
                String delim = getDefaultAttribute(DELIM_PARAMETER, DEFAULT_DELIM);
                String headerFlag = getDefaultAttribute(DEST_HEADER_PARAMETER, DEFAULT_FALSE);

                try {
                    File queryFile = Paths.get(query).toFile();
                    if (queryFile.exists() && queryFile.isFile()) {
                        query = FileUtils.readFileToString(queryFile, UTF_8);
                    }
                } catch (InvalidPathException ignored) { }


                HashMap<String, String> outFile = new HashMap<>();
                if (destFile != null) {
                    outFile.put(FILE, destFile);
                    outFile.put(DELIM, delim);
                } else {
                    outFile = null;
                }

                boolean returnFlag = isTrue(returnString);
                boolean header = isTrue(headerFlag);

                executeQuery(opDB, query, returnFlag, outFile, header);
                break;
            }
        }
    }

    private boolean isTrue(String returnString) {
        return returnString.equalsIgnoreCase("Y") || returnString.equalsIgnoreCase("YES") || returnString.equalsIgnoreCase("TRUE") || returnString.equalsIgnoreCase("T") || returnString.equals("1");
    }

    /**
     * Create empty Database. If database directory is not exist it will create it.
     *
     * @param dbName Database name.
     * @throws SQLException throws when database access error or other errors.
     */
    public void createDB(String dbName) throws SQLException {
        @Cleanup Connection c = DriverManager.getConnection(JDBC_SQLITE + dbName);
        logger.debug("Creating database " + dbName);
        logger.info("Successfully created database");
    }

    /**
     * Run SQL request on a Database and write response to a console or specified file if needed.
     *
     * @param dbName     Database file.
     * @param query      SQL request.
     * @param returnFlag If return response to a console or file.
     * @param outFile    Output File HashMap with file, delim and skip headers.
     * @param header     Skip header or not.
     * @throws SQLException throws when database access error or other errors.
     * @throws IOException  thrown in case of an I/O error.
     */
    public void executeQuery(String dbName, String query, boolean returnFlag, HashMap<String, String> outFile, boolean header) throws SQLException, IOException {
        @Cleanup Connection c = DriverManager.getConnection(JDBC_SQLITE + dbName);
        c.setAutoCommit(false);
        logger.debug("Opened database successfully");
        @Cleanup Statement stmt = c.createStatement();
        ResultSet rs = null;

        logger.debug("Executing " + query);
        if (returnFlag) {
            rs = stmt.executeQuery(query);
        } else {
            stmt.executeUpdate(query);
        }

        logger.debug("Statement Executed");
        c.commit();
        logger.debug("Statement Committed");

        if (returnFlag) {
            //File Logic
            boolean printHeader;
            @Cleanup ByteArrayOutputStream stream = null;
            @Cleanup Writer writerString = null;
            String delim;
            if (outFile == null || outFile.isEmpty()) {
                logger.debug("No output file specified");
                printHeader = header;
                stream = new ByteArrayOutputStream();
                writerString = new OutputStreamWriter(stream);
                delim = DEFAULT_DELIM;
            } else {
                printHeader = header;
                File file = Paths.get(outFile.get(FILE)).toAbsolutePath().toFile();
                logger.debug("File path was created: " + file.getParentFile().mkdirs());
                writerString = new FileWriter(file);
                delim = outFile.get(DELIM);
            }

            @Cleanup CSVWriter my_csv_output = new CSVWriter(writerString, delim.charAt(0),
                    NO_QUOTE_CHARACTER,
                    DEFAULT_ESCAPE_CHARACTER,
                    DEFAULT_LINE_END);

            int numResultCols = rs.getMetaData().getColumnCount();
            logger.debug(numResultCols);
            String[] csvRow = new String[numResultCols];

            if (printHeader) {
                for (int i = 1; i <= numResultCols; i++) {
                    csvRow[i - 1] = rs.getMetaData().getColumnName(i);
                    if (numResultCols == (i)) {
                        my_csv_output.writeNext(csvRow);
                    }
                }
            }

            while (rs.next()) {
                for (int i = 1; i <= numResultCols; i++) {
                    csvRow[i - 1] = rs.getString(i);

                    if (numResultCols == (i)) {
                        my_csv_output.writeNext(csvRow);
                    }
                }
            }
            my_csv_output.flush();

            if (stream != null) {
                logger.info(stream);
            }
        }
    }

    /**
     * Create a table, or rewrite data in Database, or add data to it.
     *
     * @param dbName     Database file.
     * @param tableName  Table name.
     * @param fileName   File with delimiter.
     * @param delim      File delimiter.
     * @param updateMode Update mode.
     * @throws SQLException throws when database access error or other errors.
     * @throws IOException  thrown in case of an I/O error
     */
    public void importTable(String dbName, String tableName, String fileName, char delim, String updateMode) throws SQLException, IOException {
        @Cleanup Connection c = DriverManager.getConnection(JDBC_SQLITE + dbName);
        c.setAutoCommit(false);
        logger.debug("Opened database successfully");

        int columnCount = 0;

        CSVParser parser = new CSVParserBuilder().withSeparator(delim).build();
        BufferedReader br = Files.newBufferedReader(Paths.get(fileName), UTF_8);
        CSVReader csvReader = new CSVReaderBuilder(br).withCSVParser(parser)
                .build();

        String[] header = csvReader.readNext();
        if (header != null) {
            columnCount = header.length;
        }

        String[] nextRecord;
        if (updateMode.equalsIgnoreCase(OVERWRITE)) {
            executeQuery(dbName, "Drop Table if exists " + tableName, false, null, false);

            //create table  based on file
            //Logic to create table based on # of columns with header as column names
            StringBuilder tableCreate = new StringBuilder();
            for (int i = 0; i < columnCount; i++) {
                tableCreate.append(DOUBLE_QUOTES).append(header[i]).append(DOUBLE_QUOTES).append(VARCHAR_2_255);
                if (i != (columnCount - 1)) {
                    tableCreate.append(DEFAULT_DELIM);
                }

            }
            executeQuery(dbName, "Create table " + tableName + " (" + tableCreate + ")", false, null, false);
        }


        while ((nextRecord = csvReader.readNext()) != null) {
            @Cleanup Statement stmt = c.createStatement();
            String sql = "INSERT INTO " + tableName + " VALUES (" + "'" + String.join("','", nextRecord) + "'" + ");";
            logger.debug(sql);
            stmt.executeUpdate(sql);

            c.commit();
        }

        logger.debug("Statement Executed");

        c.commit();
        logger.info("Statement Committed");
    }
}
