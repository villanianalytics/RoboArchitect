package com.acceleratetechnology.controller;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.github.villanianalytics.unsql.UnSql;
import com.github.villanianalytics.unsql.UnSql.EXPORT_FORMAT;
import com.github.villanianalytics.unsql.exception.UnSqlException;

/**
 * The Class UnSqlCommand.
 */
public class UnSqlCommand extends AbstractCommand {
    
    /** The logger. */
    private Logger logger = Logger.getLogger(UnSqlCommand.class);
    
    /** The Constant SRC_FILE_PARAM. */
    public static final String SRC_FILE_PARAM = "/srcFile";
  
    /** The Constant DEST_FILE_PARAM. */
    public static final String DEST_FILE_PARAM = "/destFile";
   
    /** The Constant QUERY_PATH_PARAM. */
    public static final String QUERY_PATH_PARAM = "/query";
    
    /** The Constant DELIMITER. */
    public static final String DELIMITER ="/delimiter";
    
    /** The Constant HEADERS. */
    public static final String HEADERS ="/headers";
    
    /** The delimiter value. */
    private String delimiterValue = ",";

	/** The headers flag. */
	private boolean headersFlag = false;
	
	/** The Constant XML_FILE. */
	private static final String XML_FILE = ".xml";
	
	/** The Constant JSON_FILE. */
	private static final String JSON_FILE = ".json";
	
	/** The Constant TEXT_FILE. */
	private static final String TEXT_FILE = ".txt";
	

    /**
     * Instantiates a new un sql command.
     *
     * @param args the args
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MissedParameterException the missed parameter exception
     */
    @Command("-unsql")
    public UnSqlCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    /**
     * Execute.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MissedParameterException the missed parameter exception
     */
    @Override
    public void execute() throws IOException, MissedParameterException {
    	unsql();
    }

    /**
     * Unsql.
     *
     * @throws MissedParameterException the missed parameter exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void unsql() throws MissedParameterException, IOException {
        String srcFile = getRequiredAttribute(SRC_FILE_PARAM);
        String query = getRequiredAttribute(QUERY_PATH_PARAM);
        String destFile = getAttribute(DEST_FILE_PARAM);
        String delimiterAttr = getAttribute(DELIMITER);
        String headersAttr = getAttribute(HEADERS);
        
        if (StringUtils.isEmpty(destFile) || !destFile.endsWith(TEXT_FILE)) {
        	if (!StringUtils.isEmpty(delimiterAttr)) logger.warn("Delimiter paramenter will be ingnore, it should only be used with .txt files");
        	if (!StringUtils.isEmpty(headersAttr)) logger.warn("Headers paramenter will be ingnore, it should only be used with .txt files");
        	
        } else {
        	if (!StringUtils.isEmpty(delimiterAttr)) setDelimiterValue(delimiterAttr);
        	if (!StringUtils.isEmpty(headersAttr)) setHeadersFlag(true);
        }
        
        if (!(srcFile.endsWith(XML_FILE) || srcFile.endsWith(JSON_FILE))) {
        	throw new MissedParameterException("Attribute " + SRC_FILE_PARAM + " is not in a valid format.");
        }
        
        if (StringUtils.isEmpty(query)) {
        	throw new MissedParameterException("Attribute " + QUERY_PATH_PARAM + " is empty.");
        }
        
        if (query.endsWith(TEXT_FILE)) {
        	query = readQueryFile(query);
        }
        
        unSqlFilter(srcFile, query, destFile);
    }

    
    /**
     * Un sql filter.
     *
     * @param srcFile the src file
     * @param query the query
     * @param destFile the dest file
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MissedParameterException the missed parameter exception
     */
    private void unSqlFilter(String srcFile, String query, String destFile) throws IOException, MissedParameterException {
        String raw = FileUtils.readFileToString(Paths.get(srcFile).toFile(), UTF_8);
        String results = getResults(raw, query, destFile);
      
        if (destFile != null && !destFile.isEmpty()) {
            Path destPath = Paths.get(destFile);
            File file = destPath.toAbsolutePath().toFile();
            File destination = file.getParentFile();
            if (destination.exists()) {
                logger.debug("Destination of a file " + destination + " does not exist so began to create it.");
                boolean mkdir = destination.mkdirs();
                logger.debug("Destination file directory was created: " + mkdir);
            }
            FileUtils.write(file, results, UTF_8);
        }
        
        logger.info("UnSQL query done.");
    }
    
    /**
     * Read query file.
     *
     * @param filePath the file path
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private String readQueryFile(String filePath) throws IOException {
    	String query = FileUtils.readFileToString(Paths.get(filePath).toFile(), UTF_8);
    	
    	return query.replaceAll("\\r|\\n", "");
    }
    
    /**
     * Run query.
     *
     * @param raw the raw
     * @param query the query
     * @param destFile the dest file
     * @return the string
     * @throws UnSqlException the un sql exception
     */
    private String runQuery(String raw, String query, String destFile) throws UnSqlException {
    	String results = "";
   
    	if (destFile != null && destFile.endsWith(XML_FILE)) {
    		results = new UnSql(raw)
    				.withExportFormat(EXPORT_FORMAT.XML)
    				.execute(query);
    	
    	} else  if (destFile != null && destFile.endsWith(JSON_FILE)) {
    		results = new UnSql(raw)
    				.withExportFormat(EXPORT_FORMAT.JSON)
    				.execute(query);
    	} else  if (destFile != null && destFile.endsWith(TEXT_FILE)) {
    		results = new UnSql(raw)
    				.withExportFormat(EXPORT_FORMAT.VALUES)
    				.withRowDelimiter(delimiterValue)
    				.withHeaders(headersFlag)
    				.execute(query);
    	} else {
    		results = new UnSql(raw)
    				.withExportFormat(EXPORT_FORMAT.VALUES)
    				.withRowDelimiter(delimiterValue)
    				.withHeaders(headersFlag)
    				.execute(query);
    	}
    	
    	return results;
    }
    
    /**
     * Gets the results.
     *
     * @param raw the raw
     * @param query the query
     * @param destFile the dest file
     * @return the results
     * @throws MissedParameterException the missed parameter exception
     */
    private String getResults(String raw, String query, String destFile) throws MissedParameterException {
    	String results = "";
        
        try {
        	results = runQuery(raw, query, destFile);
		} catch (UnSqlException e) {
			throw new MissedParameterException(e.getMessage());
		}
        
        logger.info(results);
        
        return results;
    }

	/**
	 * Sets the delimiter value.
	 *
	 * @param delimiterValue the new delimiter value
	 */
	public void setDelimiterValue(String delimiterValue) {
		this.delimiterValue = delimiterValue;
	}

	/**
	 * Sets the headers flag.
	 *
	 * @param headersFlag the new headers flag
	 */
	public void setHeadersFlag(boolean headersFlag) {
		this.headersFlag = headersFlag;
	}
}
