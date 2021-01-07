package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import net.lingala.zip4j.exception.ZipException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FerpCommand extends AbstractCommand {

    private final Logger logger = Logger.getLogger(FerpCommand.class);

    public static final String FILE = "/file";
    public static final String FIND = "/find";
    public static final String REPLACE = "/replace";

    @Command("-ferp")
    public FerpCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws MissedParameterException, ZipException {
        String filePath = getRequiredAttribute(FILE);
        String find = getRequiredAttribute(FIND);
        String replace = getRequiredAttribute(REPLACE);

        try {
            ferp(filePath, find, replace);
        } catch (Exception e) {
            logger.error("Error " + e.getMessage());
        }

        logResponse("Ferp.");
    }

    private void ferp(String filePath, String find, String replace) throws Exception {
        logger.trace("Ferp Started");

        File srcFile = Paths.get(filePath).toFile();
        if (!srcFile.isFile()) {
            logger.error("Invalid File");
            throw new Exception("Invalid File");
        }

        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        content = content.replaceAll(find, replace);
        Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));

        logger.trace("Done.");
    }
}