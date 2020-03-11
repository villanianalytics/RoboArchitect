package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Archive file or directory to .zip.
 */
public class ZipCommand extends AbstractCommand {
    /**
     * System logger.
     */
    private final Logger logger = Logger.getLogger(ZipCommand.class);
    /**
     * Destination xlsx file command line parameter.
     */
    public static final String DEST_FILE_PARAM = "/destFile";
    /**
     * Source directory attribute.
     */
    public static final String SRC = "/src";

    @Command("-zip")
    public ZipCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws MissedParameterException, ZipException {
        String src = getRequiredAttribute(SRC);
        String dest = getRequiredAttribute(DEST_FILE_PARAM);

        zip(src, dest);
        logger.info("Zipped.");
    }

    /**
     * Archives file or directory to a file and put it to a directory.
     *
     * @param srcDir  Source directory.
     * @param destDir Destination file directory.
     * @throws ZipException Zip exception.
     */
    private void zip(String srcDir, String destDir) throws ZipException {
        logger.debug("Zip operation start");

        Path srcPath = Paths.get(srcDir);
        Path destPath = Paths.get(destDir);

        File srcFile = srcPath.toFile();
        File destFile = destPath.toAbsolutePath().toFile();

        File parentFile = destFile.getParentFile();

        logger.debug("Check if destination directory \"" + destDir + "\" exists.");
        if (parentFile != null && !parentFile.exists()) {
            logger.debug("Start to create destination directory.");
            if (parentFile.mkdir()) {
                logger.debug("Created.");
            }
        }
        logger.debug("Checked.");

        ZipFile zipFile = new ZipFile(destFile);
        logger.debug("Check directory to zip");
        if (srcFile.isDirectory()) {
            logger.debug("Adding file.");
            zipFile.addFolder(srcFile);
        } else {
            logger.debug("Adding directory to zip");
            zipFile.addFile(srcFile);
        }
        logger.debug("Done.");
        logger.debug("Checked.");
        logger.debug("Zip operation finished");
    }
}
