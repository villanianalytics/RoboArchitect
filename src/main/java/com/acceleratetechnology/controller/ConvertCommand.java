package com.acceleratetechnology.controller;

import static org.apache.poi.ss.usermodel.CellType.BLANK;
import static org.apache.poi.ss.usermodel.CellType.FORMULA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.opencsv.CSVWriter;

import lombok.Cleanup;

/**
 * Convert .csv file to .xlsx file.
 */
public class ConvertCommand extends AbstractCommand {
    /**
     * System logger.
     */
    private final Logger logger = Logger.getLogger(ConvertCommand.class);
    /**
     * Source csv file command line parameter.
     */
    private static final String SRC_FILE_PARAM = "/srcFile";
    /**
     * Destination xlsx file command line parameter.
     */
    private static final String DEST_FILE_PARAM = "/destFile";
    /**
     * CSV file delimiter command line parameter.
     */
    private static final String DELIM_PARAM = "/delim";
    /**
     * Sheetname command line parameter.
     */
    private static final String SHEET_NAME_PARAM = "/sheetname";

    /**
     * Sheetname DEFAULT parameter.
     */
    private static final String DEFAULT_SHEET_NAME = "Sheet1";

    @Command("-convert")
    public ConvertCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws MissedParameterException, IOException {
    	logger.trace("ConvertCommand.execute started");
        String srcFile = getRequiredAttribute(SRC_FILE_PARAM);
        String extension = FilenameUtils.getExtension(srcFile);

        String destFile = getAttribute(DEST_FILE_PARAM);
        logger.debug(destFile);
        if (destFile == null)
        {
            logger.debug("No destFile entered");
            if (extension.equals("xlsx"))
            {
                destFile=FilenameUtils.getFullPath(srcFile) + FilenameUtils.getBaseName(srcFile)+".csv";
                logger.debug("Assigning destFile as "+destFile);
            }
            else if (extension.equals("csv")||extension.equals("txt"))
            {
                destFile=FilenameUtils.getFullPath(srcFile) + FilenameUtils.getBaseName(srcFile)+".xlsx";
                logger.debug("Assigning destFile as "+destFile);
            }
        }


        char delim = getDefaultAttribute(DELIM_PARAM, ",").charAt(0);
        String sheetName = getDefaultAttribute(SHEET_NAME_PARAM, DEFAULT_SHEET_NAME);

        if (extension.equals("csv") || extension.equals("txt")) {
            convertCSVFile(srcFile, destFile, delim, sheetName);
        } else if (extension.equals("xlsx")) {
            convertXLSXFile(srcFile, sheetName, destFile, delim);
        } else {
            throw new MissedParameterException(extension + " is not supported. Supported files extension need to be csv, txt or xlsx");
        }
        logger.info("Converted.");
    }

    /**
     * This method rewrites data from .csv file to .xlsx format.
     *
     * @param srcCSVFile   Source csv file.
     * @param destXLSXFile Destination xlsx file.
     * @param delim        CSV file delimiter.
     * @param sheetName    XLSX page sheetname.
     * @throws IOException Throws if source file is incorrect.
     */
    private void convertCSVFile(String srcCSVFile, String destXLSXFile, char delim, String sheetName) throws IOException {
    	logger.trace("ConvertCommand.convertCSVFile started");
    	logger.trace("Create XLSX file with " + sheetName + "sheetname.");
        @Cleanup XSSFWorkbook workBook = new XSSFWorkbook();
        POIXMLProperties xmlProps = workBook.getProperties();
        POIXMLProperties.CoreProperties coreProps =  xmlProps.getCoreProperties();
        coreProps.setCreator("RoboArchitect by Villani Analytics");
        XSSFSheet sheet = workBook.createSheet(sheetName);
        logger.trace("File created.");

        String currentLine;
        int rowNum = 0;
        logger.trace("Opening source file.");
        Path path = Paths.get(srcCSVFile);
        @Cleanup Scanner fileReader = new Scanner(path.toFile());
        logger.trace("Opened.");

        logger.trace("Start parsing CSV file.");
        while (fileReader.hasNext()) {
            currentLine = fileReader.nextLine();
            logger.trace("Start split " + currentLine + " line by \"" + delim + "\" delimiter.");
            String[] cells = currentLine.split(delim + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            logger.trace("Split.");

            logger.trace("Create " + (rowNum + 1) + " row in XLSX file.");
            XSSFRow currentRow = sheet.createRow(rowNum);
            logger.trace("Created.");

            logger.trace("Start adding split data to row.");
            for (int i = 0; i < cells.length; i++) {
                logger.trace("Start creating " + i + " cell and put there " + cells[i] + " value.");
                currentRow.createCell(i).setCellValue(cells[i]);
                logger.trace("Done.");
            }
            rowNum++;
        }
        logger.trace("Parsed finished.");

        logger.trace("Create and write to destination \"" + destXLSXFile + "\" file.");
        Path destination = Paths.get(destXLSXFile);
        File destinationFile = destination.toAbsolutePath().toFile();
        destinationFile.getParentFile().mkdirs();

        @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
        workBook.write(fileOutputStream);
        logger.trace("Done.");
    }

    /**
     * This method rewrites data from .xlsx file to file with delimiter.
     *
     * @param srcXLSX XLSX source file.
     * @param srcSheet XLSX sheetname.
     * @param targetFile Destination file with delimiters.
     * @param delim Delimiter.
     * @throws IOException Throws if source file is incorrect.
     */
    public void convertXLSXFile(String srcXLSX, String srcSheet, String targetFile, char delim) throws IOException {
    	logger.trace("ConvertCommand.convertXLSXFile started");
    	
        @Cleanup FileInputStream input_document = new FileInputStream(Paths.get(srcXLSX).toFile());
        @Cleanup XSSFWorkbook my_xls_workbook = new XSSFWorkbook(input_document);

        XSSFSheet my_worksheet = my_xls_workbook.getSheet(srcSheet);
        my_worksheet.iterator();
        File file = Paths.get(targetFile).toAbsolutePath().toFile();
        file.getParentFile().mkdirs();

        @Cleanup FileWriter my_csv = new FileWriter(file);
        @Cleanup CSVWriter my_csv_output = new CSVWriter(my_csv, delim,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        int totalColumns = my_worksheet.getRow(0).getPhysicalNumberOfCells();

        int rowTotal = my_worksheet.getLastRowNum();

        if (rowTotal > 0) {
            String[] csvData = new String[totalColumns];
            for (int rowNum = 0; rowNum <= rowTotal; rowNum++) {
                for (int colNum = 0; colNum < totalColumns; colNum++) {
                    Row row = my_worksheet.getRow(rowNum);
                    Cell cell = row.getCell(colNum);
                    if (cell == null) {
                        csvData[colNum] = "";
                    } else {
                        DataFormatter objDefaultFormat = new DataFormatter();

                        String data;
                        if (cell.getCellType() == BLANK) {
                            data = "";
                        } else if (cell.getCellType() == FORMULA) {
                            FormulaEvaluator evaluator = my_xls_workbook.getCreationHelper().createFormulaEvaluator();
                            data = objDefaultFormat.formatCellValue(cell, evaluator);
                        } else {
                            data = objDefaultFormat.formatCellValue(cell, null);
                        }
                        csvData[colNum] = data;
                    }

                    if ((colNum + 1) == totalColumns) {
                        my_csv_output.writeNext(csvData);
                    }
                }
            }
        }
    }
}
