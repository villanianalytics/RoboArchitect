package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Help command.
 */
public class HelpCommand extends AbstractCommand {
    private static final String CONVERT = "-convert         Convert .csv file to .xlsx, log command is optional.\n\n\t" +
            "/srcFile=\"CSV_FILE\" /destFile=\"XLSX_FILE\" /delim=\"DELIM_SYMBOL_OF_CSV_FILE\"\n\t" +
            "/sheetname=\"SHEET_NAME\" [/log=\"output.txt\"]\n";
    private static final String UNZIP = "-unzip             Unzip .zip file\n\n\t" +
            "/srcFile=\"SOURCE_FILE\" /destDir=\"DESTINATION_PATH\" [/log=\"output.txt\"]\n";
    private static final String FERP = "-ferp             Find and replace \n\n\t" +
            "/file=\"SOURCE_FILE\" /find=\"FIND_TEXT\" /replace=\"REPLACE_TEXT\" [/log=\"output.txt\"]\n";
    private static final String COMPRESS = "-compress             Generic compresser\n\n\t" +
            "/src=\"SOURCE\" /destFile=\"DESTINATION_FILE\" [/log=\"output.txt\"]\n";
    private static final String DECOMPRESS = "-decompress             Generic decompresser\n\n\t" +
            "/srcFile=\"SOURCE_FILE\" /destDir=\"DESTINATION_PATH\" [/log=\"output.txt\"]\n";
    private static final String ZIP = "-zip             Zip file or directory\n\n\t" +
            "/src=\"SOURCE_FILE\" /destFile=\"DESTINATION_PATH\" [/log=\"output.txt\"]\n";
    private static final String PASSWORD = "-password             Write encrypt password to file\n\n\t" +
            "/passwordFile=\"DESTINATION_OF_PASS_FILE\" [/log=\"output.txt\"]\n";
    private static final String CONNECT = "-connect             Get JSON response\n\n\t" +
            "[/destFile=\"DESTINATION_FILE\"] /config=\"config.properties\" [/jsonPath=\"PATTERN\"] [/passwordFile=\"Password.txt\"] [/log=\"output.txt\"]\n";
    private static final String JSON_PATH = "-jsonpath          Get part of JSON by pattern\n\n\t" +
            "/srcFile=\"SOURCE_FILE\" [/destFile=\"Destination file\"] [/log=\"output.txt\"]\n";
    private static final String EMAIL = "-email                 Send an email to another email\n\n\t" +
            "/config=\"CONFIG.PROPERTIES\" [/subject=\"SUBJECT\"] [/body=\"BODY\"]\n";

    private static final String QUERY_DELIM = "-querydelim                 Send an email to another email\n\n\t" +
            "/srcFile=\"DELIMITED_FILE\" /query=\"SQL_QUERY\" [/delim=\"DELIMITER\"] [/destFile=\"FILE\"]\n" +
            "\t[/suppressHeaders=\"false or true\"] [/skipLines=\"NUMBER\"] [/skipDataLines=\"NUMBER\"] [/log=\"LOG_FILE\"]\n";

    private static final String SQL = "-sql                 Create SQL database, create table and fill it by values from delimited file\n\n\t" +
            "/connection=\"jdbc connection string. For SQLite, enter SQLITE\"\n" +
            "/op=\"createDB or importTable or queryDB\" /db=\"Database.db\"\n" +
            "\t[/srcFile=\"CSV_FILE\"] [/table=\"TABLE_NAME\"] [/delim=\"DELIMITER\"] [/mode=\"OVERWRITE or APPEND\"]\n" +
            "\t[/query=\"SQL_QUERY\"] [/return=\"Y or N\"] [/destFile=\"FILE\"] [/header=\"Y or N\"] [/log=\"LOG_FILE\"]\n" +
            "\t";

    private static final String UNSQL = "-unsql                 Do SQL in json and xml files\n\n\t" +
            "/srcFile/=\"Source file in json or xml format\"\n" +
            "/destFile/=\"Destination file in json, xml or text format\"\n" +
            "/query/=\"SQL query\"\n" +
            "[/delimiter=\"delimiter character\"] [/headers=\"Y or N\"] [/log=\"LOG_FILE\"] \n" +
            "\t";
    private static final String HIERARCHY = "-hierarchy                 Convert hierarchy of a file from parent-child to level-based and vice versa\n\n\t" +
            "/srcFile/=\"Source file in xls,txt or xlsx format\"\n\t" +
            "/destFile/=\"Destination file in xls,txt or xlsx format\"\n\t" +
            "/totalAttrib/=\"Total number of attributes in Destination file. REQUIRED ONLY FOR LEVEL-BASED back to PARENT-CHILD conversion, int value i.e 7\"\n\t" +
            "[/convertType=\"Convert type i.e lb to level-based and pc to parent-child. Default is lb\"]\n\t"+
            "[/srcDelim=\"Source file delimiter. Default is , \"]\n\t[/destDelim=\"Destination file delimiter Default is , \"] \n\t" +
            "[/headerFlag=\"if header is present in source file i.e Y or N\"]\n\t" +
            "[/customHeader=\"to specify custom header in output file in comma seperated values  i.e Parent,Child,Attrib1,attrib2,...\"]\n\t" +
            "[/parentColIndex=\"to specify custom header in output file in comma seperated values  i.e Parent,Child,Attrib1,attrib2,...\"]\n\t" +
            "[/childColIndex=\"to specify custom header in output file in comma seperated values  i.e Parent,Child,Attrib1,attrib2,...\"]\n\t" +
            "\t";

    /**
     * System logger.
     */
    private final Logger logger = Logger.getLogger(HelpCommand.class);

    @Command("-help")
    public HelpCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() {
    	logger.trace("HelpCommand.execute started");
        logger.info(CONVERT);
        logger.info(UNZIP);
        logger.info(ZIP);
        logger.info(PASSWORD);
        logger.info(CONNECT);
        logger.info(JSON_PATH);
        logger.info(EMAIL);
        logger.info(QUERY_DELIM);
        logger.info(SQL);
        logger.info(UNSQL);
        logger.info(HIERARCHY);
        logger.info(DECOMPRESS);
        logger.info(COMPRESS);
        logger.info(FERP);
    }
}
