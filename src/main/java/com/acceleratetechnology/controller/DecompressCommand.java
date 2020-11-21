package com.acceleratetechnology.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;

import net.lingala.zip4j.exception.ZipException;

public class DecompressCommand extends AbstractCommand {
    
    private final Logger logger = Logger.getLogger(DecompressCommand.class);
    
    private static final String SRC_FILE_PARAM = "/srcFile";
    public static final String DEST_DIR_PARAM = "/destDir";
   
    @Command("-decompress")
    public DecompressCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws MissedParameterException, ZipException {
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
    	ArchiveInputStream ais = null;
        
    	ArchiveStreamFactory asf = new ArchiveStreamFactory();
       
        String extension = FileNameUtils.getExtension(srcFile);
        FileInputStream fis = new FileInputStream(new File(srcFile));
        
        if (extension.toLowerCase().endsWith("tgz") || extension.toLowerCase().endsWith("gz")) {
        	CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, fis);
        	ais = asf.createArchiveInputStream(new BufferedInputStream(cis));
        } else {
        	ais = asf.createArchiveInputStream(fis);
        }
        
        File outputFile = new File(dest);
        if (!outputFile.exists())
            outputFile.mkdirs();
        
        ArchiveEntry nextEntry;
        while ((nextEntry = ais.getNextEntry()) != null) {
            File ftemp = new File(dest, nextEntry.getName());
            if (nextEntry.isDirectory()) {
                ftemp.mkdir();
            } else {
                FileOutputStream fos = FileUtils.openOutputStream(ftemp);
                IOUtils.copy(ais, fos);
                fos.close();
            }
        }
        ais.close();
        fis.close();
    }

}
