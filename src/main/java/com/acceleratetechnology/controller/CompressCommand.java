package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

    private void compress(String srcDir, String destDir) throws Exception {
        logger.trace("CompressCommand.compress operation start");

        Path destPath = Paths.get(destDir);
        File destFile = destPath.toAbsolutePath().toFile();
        destFile.createNewFile();

        List<File> filesToArchive = fileToCompress(srcDir);
        try (ArchiveOutputStream o = createArchiveOutputStream(destFile)) {
            for (File f : filesToArchive) {
                ArchiveEntry entry = o.createArchiveEntry(f, f.getAbsoluteFile().getName());
                o.putArchiveEntry(entry);
                if (f.isFile()) {
                    try (InputStream i = Files.newInputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
                o.closeArchiveEntry();
            }
            o.finish();
        } catch (IOException e) {
            logger.error("Error compressing.");
        }
    }

    private List<File> fileToCompress(String srcPath) {
        List<File> newFiles = new ArrayList<>();

        File folder = new File(srcPath);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                newFiles.add(file);
            }
        }

        return newFiles;
    }

    private ArchiveOutputStream createArchiveOutputStream(File destFile) throws Exception {
        String file = destFile.getAbsolutePath().substring(destFile.getAbsolutePath().lastIndexOf("/"));
        String extension = file.substring(file.indexOf("."));

        if (".zip".equalsIgnoreCase(extension)) {
            return new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP,
                    new FileOutputStream(destFile));
        }
        if (".tar".equalsIgnoreCase(extension)) {
            return new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR,
                    new FileOutputStream(destFile));
        }
        if (".7z".equalsIgnoreCase(extension)) {
            return new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.SEVEN_Z,
                    new FileOutputStream(destFile));
        }
        if (".jar".equalsIgnoreCase(extension)) {
            return new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.JAR,
                    new FileOutputStream(destFile));
        }
        if (".tar.gz".equalsIgnoreCase(extension)) {
            OutputStream gzo = new GzipCompressorOutputStream(new FileOutputStream(destFile));
            return  new TarArchiveOutputStream(gzo);
        }

        throw new Exception("Invalid destination file");
    }
}
