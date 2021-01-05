package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.acceleratetechnology.utils.Compressors;
import net.lingala.zip4j.exception.ZipException;
import org.apache.log4j.Logger;

import java.io.IOException;

public class CompressCommand extends AbstractCommand {
    
    private final Logger logger = Logger.getLogger(CompressCommand.class);
    public static final String DEST_FILE_PARAM = "/destFile";
    public static final String SRC = "/src";

    @Command("-compress")
    public CompressCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws MissedParameterException, ZipException {
        String src = getRequiredAttribute(SRC);
        String dest = getRequiredAttribute(DEST_FILE_PARAM);

        try {
            compress(src, dest);
        } catch (Exception e) {
            logger.error("Error trying to compress.");
        }

        logResponse("Zipped.");
    }

    public void compress(String srcDir, String destDir) throws Exception {
        logger.trace("CompressCommand.compress operation start");
        Compressors.compress(srcDir, destDir);
        logger.trace("CompressCommand.compress operation finish");
    }
}
