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
    /**
     * System logger.
     */
    private Logger logger = Logger.getLogger(HelpCommand.class);

    @Command("-help")
    public HelpCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() {
        logger.info(CONVERT);
        logger.info(UNZIP);
        logger.info(ZIP);
        logger.info(PASSWORD);
        logger.info(CONNECT);
        logger.info(JSON_PATH);
        logger.info(EMAIL);
        logger.info(QUERY_DELIM);
        logger.info(SQL);
    }
}
