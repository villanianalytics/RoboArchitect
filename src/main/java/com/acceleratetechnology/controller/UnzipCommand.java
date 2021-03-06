package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.acceleratetechnology.utils.Compressors;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unzip .zip files and put sources to a directory.
 */
public class UnzipCommand extends AbstractCommand {
    /**
     * System logger.
     */
    private Logger logger = Logger.getLogger(UnzipCommand.class);
    /**
     * Source csv file command line parameter.
     */
    private static final String SRC_FILE_PARAM = "/srcFile";
    /**
     * Destination catalog command line parameter.
     */
    public static final String DEST_DIR_PARAM = "/destDir";

    @Command("-unzip")
    public UnzipCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws ZipException, MissedParameterException {
        String srcFile = getRequiredAttribute(SRC_FILE_PARAM);
        String dest = getDefaultAttribute(DEST_DIR_PARAM,"");

        try {
            unzip(srcFile, dest);
        } catch (Exception e) {
            logger.error("Error : " + e.getMessage());
        }

        logResponse("Unzipped.");
    }

    /**
     * Unzip files to destination directory. If directory doesn't exist create it.
     *
     * @param zipFilePath Zip file path.
     * @param destDir     Destination directory.
     * @throws ZipException Throws when zip file wrong or have a password.
     */
    private void unzip(String zipFilePath, String destDir) throws IOException, MissedParameterException, CompressorException, ArchiveException {
        logger.trace("UnzipCommand.Unzip operation start");
        Path dirPath = Paths.get(destDir);
        File dir = dirPath.toAbsolutePath().toFile();
        logger.trace("Check if destination directory \"" + destDir + "\" exists.");
        if (!dir.exists()) {
            logger.trace("Destination directory doesn't exist, so it starts to create it.");
            boolean mkdirs = dir.mkdirs();
            logger.trace("Directory created: " + mkdirs + " Done.");
        }

        Compressors.decompress(zipFilePath, destDir);

        logger.trace("Done.");
        logger.trace("UnzipCommand.Unzip operation finished");
    }
}
