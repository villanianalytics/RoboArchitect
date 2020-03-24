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

public class UnSqlCommand extends AbstractCommand {
    private Logger logger = Logger.getLogger(UnSqlCommand.class);
    
    public static final String SRC_FILE_PARAM = "/srcFile";
  
    public static final String DEST_FILE_PARAM = "/destFile";
   
    public static final String QUERY_PATH_PARAM = "/query";
    
    public static final String DELIMITER ="/delimiter";
    
    public static final String HEADERS ="/headers";
    
    private String delimiterValue = ",";

	private boolean headersFlag = false;

    @Command("-unsql")
    public UnSqlCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws IOException, MissedParameterException {
    	unsql();
    }

    private void unsql() throws MissedParameterException, IOException {
        String srcFile = getRequiredAttribute(SRC_FILE_PARAM);
        String query = getRequiredAttribute(QUERY_PATH_PARAM);
        String destFile = getAttribute(DEST_FILE_PARAM);
        String delimiterAttr = getAttribute(DELIMITER);
        String headersAttr = getAttribute(HEADERS);
        
        if (StringUtils.isEmpty(destFile) || !destFile.endsWith(".txt")) {
        	if (!StringUtils.isEmpty(delimiterAttr)) logger.warn("Delimiter paramenter will be ingnore, it should only be used with .txt files");
        	if (!StringUtils.isEmpty(headersAttr)) logger.warn("Headers paramenter will be ingnore, it should only be used with .txt files");
        	
        } else {
        	if (!StringUtils.isEmpty(delimiterAttr)) setDelimiterValue(delimiterAttr);
        	if (!StringUtils.isEmpty(headersAttr)) setHeadersFlag(true);
        }
        
        if (!(srcFile.endsWith(".xml") || srcFile.endsWith(".json"))) {
        	throw new MissedParameterException("Attribute " + SRC_FILE_PARAM + " is not in a valid format.");
        }
        
        unSqlFilter(srcFile, query, destFile);
    }

    
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
    
    private String runQuery(String raw, String query, String destFile) throws UnSqlException {
    	String results = "";
   
    	if (destFile != null && destFile.endsWith(".xml")) {
    		results = new UnSql(raw).withExportFormat(EXPORT_FORMAT.XML).execute(query);
    	} else  if (destFile != null && destFile.endsWith(".json")) {
    		results = new UnSql(raw).withExportFormat(EXPORT_FORMAT.JSON).execute(query);
    	} else  if (destFile != null && destFile.endsWith(".txt")) {
    		results = new UnSql(raw).withExportFormat(EXPORT_FORMAT.VALUES).withRowDelimiter(delimiterValue).withHeaders(headersFlag).execute(query);
    	} else {
    		results = new UnSql(raw).withExportFormat(EXPORT_FORMAT.VALUES).withRowDelimiter(delimiterValue).withHeaders(headersFlag).execute(query);
    	}
    	
    	return results;
    }
    
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

	public void setDelimiterValue(String delimiterValue) {
		this.delimiterValue = delimiterValue;
	}

	public void setHeadersFlag(boolean headersFlag) {
		this.headersFlag = headersFlag;
	}
}
