package com.acceleratetechnology.controller;

import static com.opencsv.CSVWriter.DEFAULT_ESCAPE_CHARACTER;
import static com.opencsv.CSVWriter.DEFAULT_LINE_END;
import static com.opencsv.CSVWriter.NO_QUOTE_CHARACTER;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.acceleratetechnology.jdbc.ApplicationJdbc;
import com.acceleratetechnology.jdbc.ApplicationJdbcFactory;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import lombok.Cleanup;

/**
 * Create, add or run SQL requests in SQLite Database.
 */
public class SqlCommand extends AbstractCommand {
	
	/**
	 * System logger.
	 */
	private Logger logger = Logger.getLogger(SqlCommand.class);
	/**
	 * Run SQL command option. It could be {@link #CREATE_DB_OPTION},
	 * {@link #IMPORT_TABLE_OPTION}, {@link #QUERY_DB_OPTION}.
	 */
	private static final String OP_PARAMETER = "/op";
	/**
	 * Connection Type.
	 */
	private static final String CONN_PARAMETER = "/connection";
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
	 * Double quotes need to surround data from csv file.
	 */
	private static final String DOUBLE_QUOTES = "\"";
	/**
	 * SQLite package directory.
	 */
	private static final String JDBC_SQLITE = "jdbc:sqlite:";

	/**
	 * Default true answer.
	 */
	private static final String DEFAULT_CONN = "SQLITE";

	@Command("-sql")
	public SqlCommand(String[] args) throws IOException, MissedParameterException {
		super(args);
	}

	@Override
	public void execute() throws MissedParameterException, SQLException, IOException, ClassNotFoundException {
    	logger.trace("SqlCommand.execute operation start");

		String opCmd = getRequiredAttribute(OP_PARAMETER);
		String opDB = getRequiredAttribute(DB_NAME_PARAMETER);
		String dbConnection = getDefaultAttribute(CONN_PARAMETER, DEFAULT_CONN);
		
		if (dbConnection.equalsIgnoreCase("sqlite")) {
			File db = Paths.get(opDB).toAbsolutePath().toFile();
			logger.trace("File path is created: " + db.getParentFile().mkdirs());
			opDB = db.getCanonicalPath();
			dbConnection = JDBC_SQLITE + opDB;
		}
		
		ApplicationJdbc jdbcConnection = ApplicationJdbcFactory.getInstance(dbConnection);
		jdbcConnection.initializeWithUrl(dbConnection);
		
		switch (opCmd) {
		case CREATE_DB_OPTION:
			createDB(jdbcConnection, opDB);
			break;
		case IMPORT_TABLE_OPTION:
			String srcFile = getRequiredAttribute(SRC_FILE_PARAMETER);
			String tableName = getRequiredAttribute(TABLE_PARAMETER);
			String delim = getDefaultAttribute(DELIM_PARAMETER, DEFAULT_DELIM);
			String updateMode = getDefaultAttribute(MODE_PARAMETER, DEFAULT_MODE);

			importTable(jdbcConnection, opDB, tableName, srcFile, delim.charAt(0), updateMode);
			
			break;
		case QUERY_DB_OPTION:
			String query = getRequiredAttribute(QUERY_PARAMETER);
			String returnString = getDefaultAttribute(RETURN_PARAMETER, DEFAULT_TRUE);
			String destFile = getAttribute(DEST_FILE_PARAMETER);
			String delimDbOption = getDefaultAttribute(DELIM_PARAMETER, DEFAULT_DELIM);
			String headerFlag = getDefaultAttribute(DEST_HEADER_PARAMETER, DEFAULT_FALSE);

			try {
				File queryFile = Paths.get(query).toFile();
				if (queryFile.exists() && queryFile.isFile()) {
					query = FileUtils.readFileToString(queryFile, UTF_8);
				}
			} catch (InvalidPathException e) {
				logger.trace(e.getMessage());
			}

			HashMap<String, String> outFile = new HashMap<>();
			if (destFile != null) {
				outFile.put(FILE, destFile);
				outFile.put(DELIM, delimDbOption);
			} else {
				outFile = null;
			}

			boolean header = isTrue(headerFlag);

			if (isTrue(returnString)) {
				executeQuery(jdbcConnection, opDB, query, outFile, header);
			} else {
				executeUpdate(jdbcConnection, opDB, query);
			}
			
			break;
		default:
			break;
		}
	}

	private boolean isTrue(String returnString) {
		logger.trace("SqlCommand.isTrue operation start");
		return returnString.equalsIgnoreCase("Y") || returnString.equalsIgnoreCase("YES")
				|| returnString.equalsIgnoreCase("TRUE") || returnString.equalsIgnoreCase("T")
				|| returnString.equals("1");
	}

	public void createDB(ApplicationJdbc jdbc, String dbName) throws SQLException, ClassNotFoundException {
		logger.trace("SqlCommand.createDB operation start");
		jdbc.createDb(dbName);
		logResponse("Successfully created database " + dbName);
	}

	public void executeQuery(ApplicationJdbc jdbc, String dbName, String query, Map<String, String> outFile, boolean header) throws IOException, SQLException, ClassNotFoundException {
		logger.trace("SqlCommand.executeQuery operation start");
		logger.trace("Executing " + query);
		List<String[]> results = jdbc.executeQuery(dbName, query);
		logger.trace("Statement Executed");

		boolean printHeader;
		@Cleanup
		ByteArrayOutputStream stream = null;
		@Cleanup
		Writer writerString = null;
		String delim;
		if (outFile == null || outFile.isEmpty()) {
			logger.trace("No output file specified");
			printHeader = header;
			stream = new ByteArrayOutputStream();
			writerString = new OutputStreamWriter(stream);
			delim = DEFAULT_DELIM;
		} else {
			printHeader = header;
			File file = Paths.get(outFile.get(FILE)).toAbsolutePath().toFile();
			logger.trace("File path was created: " + file.getParentFile().mkdirs());
			writerString = new FileWriter(file);
			delim = outFile.get(DELIM);
		}
		
		writeOutput(results, printHeader, writerString, delim.charAt(0));

		if (stream != null) {
			logResponse(stream.toString());
		} else {
			logResponse("Query execute with success, no results found");
		}
	}
	
	private void writeOutput(List<String[]> results, boolean printHeader, Writer writerString, char delim) throws IOException {
		logger.trace("SqlCommand.writeOutput operation start");
		try (CSVWriter myCsvOutput = new CSVWriter(writerString, delim, NO_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER, DEFAULT_LINE_END)) {	
			int numResultCols = results.get(0).length;
			logger.trace(numResultCols);
			String[] csvRow = new String[numResultCols];
			if (printHeader) {
				for (int i = 0; i < numResultCols; i++) {
					csvRow[i] = results.get(0)[i];
					if (numResultCols == (i + 1)) {
						myCsvOutput.writeNext(csvRow);
					}
				}
			}
	
			for (int j = 1; j < results.size(); j++) {
				String[] result = results.get(j);
	
				for (int i = 0; i < numResultCols; i++) {
					csvRow[i] = result[i];
					if (numResultCols == (i + 1)) {
						myCsvOutput.writeNext(csvRow);
					}
				}
			}
			
			myCsvOutput.flush();
		}
	}

	public void executeUpdate(ApplicationJdbc jdbc, String dbName, String query) throws SQLException, ClassNotFoundException {
		logger.trace("SqlCommand.executeUpdate operation start");
		jdbc.executeUpdate(dbName, query);
		logResponse("Statement Executed with success");
	}

	public void importTable(ApplicationJdbc applicationJdbc, String dbName, String tableName, String fileName,
			char delim, String updateMode) throws IOException, SQLException, ClassNotFoundException {
		logger.trace("SqlCommand.importTable operation start");
		int columnCount = 0;

		CSVParser parser = new CSVParserBuilder().withSeparator(delim).build();
		BufferedReader br = Files.newBufferedReader(Paths.get(fileName), UTF_8);
		CSVReader csvReader = new CSVReaderBuilder(br).withCSVParser(parser).build();

		String[] header = csvReader.readNext();
		if (header != null) {
			columnCount = header.length;
		}

		if (updateMode.equalsIgnoreCase(OVERWRITE)) {
			applicationJdbc.dropTable(dbName, tableName);

			StringBuilder tableCreate = new StringBuilder();
			for (int i = 0; i < columnCount; i++) {
				tableCreate.append(DOUBLE_QUOTES).append(header[i]).append(DOUBLE_QUOTES)
						.append(applicationJdbc.getDataType());
				if (i != (columnCount - 1)) {
					tableCreate.append(DEFAULT_DELIM);
				}
			}

			applicationJdbc.createTable(dbName, tableName, tableCreate.toString());
		}

		String[] nextRecord;

		while ((nextRecord = csvReader.readNext()) != null) {
			applicationJdbc.executeUpdate(dbName,
					"INSERT INTO " + tableName + " VALUES (" + "'" + String.join("','", nextRecord) + "'" + ")");
		}
		
		logResponse("Import completed with success");
	}
}
