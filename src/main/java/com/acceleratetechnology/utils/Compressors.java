package com.acceleratetechnology.utils;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Compressors {

    public static void compress(String srcDir, String destDir) throws Exception {
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
            throw e;
        }
    }

    private static List<File> fileToCompress(String srcPath) {
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

    private static ArchiveOutputStream createArchiveOutputStream(File destFile) throws Exception {
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

    public static void decompress(String srcFile, String dest) throws ArchiveException, IOException, CompressorException {
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
