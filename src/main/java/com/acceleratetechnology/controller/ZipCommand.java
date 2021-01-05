package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.acceleratetechnology.utils.Compressors;
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

        try {
            zip(src, dest);
        } catch (Exception e) {
            logger.error("ERROR - " + e.getMessage());
        }
        logResponse("Zipped.");
    }

    /**
     * Archives file or directory to a file and put it to a directory.
     *
     * @param srcDir  Source directory.
     * @param destDir Destination file directory.
     * @throws ZipException Zip exception.
     */
    private void zip(String srcDir, String destDir) throws Exception {
        logger.trace("ZipCommand.Zip operation start");

        Path destPath = Paths.get(destDir);
        File destFile = destPath.toAbsolutePath().toFile();
        File parentFile = destFile.getParentFile();

        logger.trace("Check if destination directory \"" + destDir + "\" exists.");
        if (parentFile != null && !parentFile.exists()) {
            logger.trace("Start to create destination directory.");
            if (parentFile.mkdirs()) {
                logger.trace("Created.");
            }
        }
        logger.trace("Checked.");

        Compressors.compress(srcDir, destDir);

        logger.trace("Done.");
        logger.trace("Checked.");
    }
}
