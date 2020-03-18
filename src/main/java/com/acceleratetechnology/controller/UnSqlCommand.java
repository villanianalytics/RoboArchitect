package com.acceleratetechnology.controller;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
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
        
        if (!(srcFile.endsWith(".xml") || srcFile.endsWith(".json"))) {
        	throw new MissedParameterException("Attribute " + SRC_FILE_PARAM + " is not in a valid format.");
        }
        
        if (!(destFile.endsWith(".xml") || destFile.endsWith(".json"))) {
        	throw new MissedParameterException("Attribute " + DEST_FILE_PARAM + " is not in a valid format.");
        }

        unSqlFilter(srcFile, query, destFile);
    }

    
    private void unSqlFilter(String srcFile, String query, String destFile) throws IOException, MissedParameterException {
        String raw = FileUtils.readFileToString(Paths.get(srcFile).toFile(), UTF_8);
        String formattedJson = "";
        if (raw != null && !raw.isEmpty()) {
            Path destPath = Paths.get(destFile);
            File file = destPath.toAbsolutePath().toFile();
            File destination = file.getParentFile();
            if (destination.exists()) {
                logger.debug("Destination of a file " + destination + " does not exist so began to create it.");
                boolean mkdir = destination.mkdirs();
                logger.debug("Destination file directory was created: " + mkdir);
            }
            
            
            UnSql unsql = new UnSql(raw);
           
            try {
				formattedJson = unsql.executeQuery(query, getExportFormat(destFile));
			} catch (UnSqlException e) {
				throw new MissedParameterException(e.getMessage());
			}
            
            FileUtils.write(file, formattedJson, UTF_8);
        }
        
        logger.info("UnSQL query done.");
    }
    
    private UnSql.EXPORT_FORMAT getExportFormat(String destFile) {
    	return destFile.endsWith(".xml") ? EXPORT_FORMAT.XML : EXPORT_FORMAT.JSON;
    }
}
