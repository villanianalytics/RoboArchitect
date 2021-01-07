package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.acceleratetechnology.utils.Compressors;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;

public class DecompressCommand extends AbstractCommand {
    
    private final Logger logger = Logger.getLogger(DecompressCommand.class);
    
    private static final String SRC_FILE_PARAM = "/srcFile";
    public static final String DEST_DIR_PARAM = "/destDir";
   
    @Command("-decompress")
    public DecompressCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws MissedParameterException {
    	String srcFile = getRequiredAttribute(SRC_FILE_PARAM);
        String dest = getDefaultAttribute(DEST_DIR_PARAM, "");
        
        try {
			decompress(srcFile, dest);
		} catch (FileNotFoundException e) {
			logger.error("File not found. Exception message: " + e.getMessage());
		} catch (ArchiveException e) {
			logger.error("File format not supported. Exception message: " + e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (CompressorException e) {
			logger.error("Compress exception. Exception message: " + e.getMessage());
		}
        
        logResponse("Decompressed.");
    }

    private void decompress(String srcFile, String dest) throws IOException, ArchiveException, CompressorException {
        logger.info("Decompress started");
        Compressors.decompress(srcFile, dest);
        logger.info("Decompress finished");
    }
}
