package com.acceleratetechnology.main;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tools.ant.types.Commandline;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.internal.CheckExitCalled;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.acceleratetechnology.controller.ConnectCommand;
import com.acceleratetechnology.controller.EncryptDecryptAbstractCommand;

import lombok.Cleanup;

/**
 * End to end test of RARobotUtilities.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({System.class, FileUtils.class, EncryptDecryptAbstractCommand.class, HttpClientBuilder.class, ConnectCommand.class, EntityUtils.class})
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
public class RAMainApplicationTest {
    static {
        System.setProperty("log.file", "logs.log");
        System.setProperty("line.separator", "\n");
    }

    /**
     * Test recourse directory.
     */
    public static final String SRC_TEST_RESOURCES = "src" + File.separator + "test" + File.separator + "resources";
    /**
     * Resource file.
     */
    public static final File RESOURCES = Paths.get(SRC_TEST_RESOURCES).toFile();
    private static final String jsonResponse = "{\n" +
                                               "  \"field1\": \"aa11\",\n" +
                                               "  \"field2\": [\n" +
                                               "    \"aa22\"\n" +
                                               "  ],\n" +
                                               "  \"structField\": {\n" +
                                               "    \"sf1\": [\n" +
                                               "      \"aaa11\",\n" +
                                               "      \"test\"\n" +
                                               "    ]," +
                                               "\n" +
                                               "    \"sf2\": {\n" +
                                               "      \"aaa22\": {\n" +
                                               "        \"test\": [\n" +
                                               "          1,\n" +
                                               "          2,\n" +
                                               "          3,\n" +
                                               "          4\n" +
                                               "        ]\n" +
                                               "      }\n" +
                                               "    }\n" +
                                               "  }\n" +
                                               "}";
    
    private static final String jsonResponseUnsql = "{\n" +
									            "  \"field1\": \"aa11\",\n" +
									            "  \"field2\": [\n" +
									            "    \"aa22\"\n" +
									            "  ],\n" +
									            "  \"structField\": {\n" +
									            "    \"sf1\": [\n" +
									            "      \"aaa11\",\n" +
									            "      \"test\"\n" +
									            "    ]," +
									            "\n" +
									            "    \"sf2\": {\n" +
									            "      \"val1\": \"test1\",\n"+
									            "      \"val2\": \"test2\",\n"+
									            "      \"aaa22\": {\n" +
									            "        \"test\": [\n" +
									            "          1,\n" +
									            "          2,\n" +
									            "          3,\n" +
									            "          4\n" +
									            "        ]\n" +
									            "      }\n" +
									            "    }\n" +
									            "  },\"employees\":[\n" + 
									            "    {\"email\": \"test\"},\n" + 
									            "    {\"name\":\"Bob\", \"email\":\"bob32@gmail.com\"}\n" +
									            "	]\n" +
									            "}";
    /**
     * Path to file which is expected.
     */
    public static Path EXPECTED_FILE_PATH;
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private PrintStream out = new PrintStream(outputStream);
    private PrintStream originOut = new PrintStream(System.out);

    public static String csvFile = "1,2,3,4,5,6,7,8,9\n" +
                                   "a,b,c,d,e,f,g,h,i\n" +
                                   "aa,bb,cc,dd,ee,ff,gg,hh,ii\n" +
                                   "ab,cd,ef,gh,ij,kl,mn,op,qr";

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    @Mock
    Console console;
    @Mock
    CloseableHttpClient client;
    @Mock
    CloseableHttpResponse response;
    @Mock
    HttpClientBuilder builder;
    @Mock
    StatusLine statusLine;
    @Mock
    HttpEntity entity;

    @AfterClass
    public static void clean() throws IOException {
        FileUtils.write(Paths.get("logs.log").toFile(), "", UTF_8);
    }

    @After
    public void reset() {
        outputStream.reset();
    }

    @Test
    public void unzipTest() throws IOException {
        Path srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test.zip");
        Path destFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test");
        EXPECTED_FILE_PATH = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "test.csv");
        String command = "-unzip /srcFile=\"" + srcFilePath.toAbsolutePath() + "\" /destDir=\"" + destFilePath.toAbsolutePath() + "\"";
        testUnzip(command);

        command = "-unzip /srcFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test.zip\" /destDir=\"" + destFilePath.toAbsolutePath() + "\"";
        testUnzip(command);

        command = "-unzip /srcFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test.zip\" /destDir=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test\"";
        testUnzip(command);

        command = "-unzip /srcFile=\"" + srcFilePath.toAbsolutePath() + "\" /destDir=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test\"";
        testUnzip(command);

        File source = srcFilePath.toFile();
        File dest = Paths.get("").toAbsolutePath().toFile();
        FileUtils.copyFileToDirectory(source, dest);
        command = "-unzip /srcFile=\"test.zip\" /destDir=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test\"";
        testUnzip(command);

        EXPECTED_FILE_PATH = Paths.get("test.csv");
        command = "-unzip /srcFile=\"test.zip\" /destDir=\"\"";
        testUnzip(command);
        FileUtils.deleteQuietly(Paths.get("test.zip").toFile());
    }

    @Test
    public void convertTest() throws IOException {
        testConvert("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test.csv", "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "test.xlsx");

        testConvert("test.csv", "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "test.xlsx");

        testConvert("test.csv", "test.xlsx");

        testConvert("test.csv", "src" + File.separator + "test" + File.separator + "resources" + File.separator + "test try spaces" + File.separator + "test.xlsx");
    }


    @Test
    public void passwordTest() throws IOException {

        EXPECTED_FILE_PATH = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "pswd.txt");
        testPassword();

        EXPECTED_FILE_PATH = Paths.get("src" + File.separator + "main" + File.separator + "resources" + File.separator + "pswd.txt").toAbsolutePath();
        testPassword();


        EXPECTED_FILE_PATH = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "pswd.txt");
        testPassword();

        EXPECTED_FILE_PATH = Paths.get("src" + File.separator + "main" + File.separator + "resources" + File.separator + "test" + File.separator + "pswd.txt").toAbsolutePath();
        testPassword();
        FileUtils.deleteDirectory(Paths.get("src" + File.separator + "main" + File.separator + "resources" + File.separator + "test").toFile());

        EXPECTED_FILE_PATH = Paths.get("pswd.txt");
        testPassword();

        EXPECTED_FILE_PATH = Paths.get("pswd.txt").toAbsolutePath();
        testPassword();
    }

  
    public void connectTest() throws Exception {
        String command = "-connect /destFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "jsonResponse.json\" /config=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties\"";
        testConnect(command);

        command = "-connect /destFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE_TWO" + File.separator + "jsonResponse.json\" /config=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties\"";
        testConnect(command);

        command = "-connect /destFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE-TWO" + File.separator + "jsonResponse.json\" /config=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties\"";
        testConnect(command);

        command = "-connect /destFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE TWO" + File.separator + "jsonResponse.json\" /config=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties\"";
        testConnect(command);

        command = "-connect /destFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json\" /config=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties\"";
        testConnect(command);

        Path srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE_TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties\"";
        testConnect(command);

        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE-TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties\"";
        testConnect(command);

        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE+TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties\"";
        testConnect(command);

        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties\"";
        testConnect(command);

        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties\"";
        testConnect(command);

        Path destFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "config.properties");
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + destFilePath.toAbsolutePath() + "\"";
        testConnect(command);
    }

    @Test
    public void zipTest() throws IOException {
        EXPECTED_FILE_PATH = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "zipTest.zip");
        String command = "-zip /src=\"src/main\" /destFile=src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "zipTest.zip";
        testZip(command);

        Path srcFilePath = Paths.get("src/main");
        command = "-zip /src=" + srcFilePath.toAbsolutePath() + " /destFile=src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "zipTest.zip";
        testZip(command);

        Path destFilePath = Paths.get("src/test/resources/test/zipTest.zip");
        command = "-zip /src=" + srcFilePath.toAbsolutePath() + " /destFile=" + destFilePath.toAbsolutePath();
        testZip(command);

        File source = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test.csv").toFile();
        File dest = Paths.get("").toAbsolutePath().toFile();
        FileUtils.copyFileToDirectory(source, dest);

        command = "-zip /src=test.csv /destFile=src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "zipTest.zip";
        testZip(command);
        FileUtils.deleteQuietly(Paths.get("test.csv").toFile());
    }

    @Test
    public void queryTest() throws IOException {
        testQuery("select *", "");

        testQuery("select * from test", "\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\"\n" +
                                        "\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\",\"h\",\"i\"\n" +
                                        "\"aa\",\"bb\",\"cc\",\"dd\",\"ee\",\"ff\",\"gg\",\"hh\",\"ii\"\n" +
                                        "\"ab\",\"cd\",\"ef\",\"gh\",\"ij\",\"kl\",\"mn\",\"op\",\"qr\"");

        testQuery("select * from test where \"3\"='c' or \"4\"='gh'", "\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\"\n" +
                                                                      "\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\",\"h\",\"i\"\n" +
                                                                      "\"ab\",\"cd\",\"ef\",\"gh\",\"ij\",\"kl\",\"mn\",\"op\",\"qr\"");
        System.setOut(originOut);
    }

    @Test
    public void testUnSQL() throws IOException {
    	testUnSQL("select field1 from *", "aa11");

        testUnSQL("select field2 from *", "aa22");

        testUnSQL("select sf1 from structField", "aaa11,test");
        
        testUnSQL("select sf2 from structfield", "1,2,3,4");
        
        testUnSQL("select field2 from *", "field2\naa22", "true", ",");

        testUnSQL("select field1, field2 from *", "field1,field2\naa11,aa22", "true", ",");
       
    	testUnSQL("select * from employees", "email|name\ntest|\nbob32@gmail.com|Bob", "true", "|");
    	
        testUnSQL("select * from structfield", "sf1[0]|sf1[1]|val1|val2|test[0]|test[1]|test[2]|test[3]\naaa11|test|test1|test2|1|2|3|4", "true", "|");
        
        testUnSQL("select * from structfield.sf2.aaa22.test", "4|||\n||3|\n|||2\n|1||", "", "|");
   
        testUnSQL("select * from structfield.sf2.aaa22", "1|2|3|4", "", "|");
        
        testUnSQL("select val1, aaa22.test, val2 from structfield.sf2", "val1|aaa22.test|val2\ntest1|1,2,3,4|test2", "true", "|");
        
        testUnSQL("select sf2, sf1 from structfield", "sf2|sf1\ntest1,test2,1,2,3,4|aaa11,test", "true", "|");
        
        System.setOut(originOut);
    }
    
    @Test
    public void testJSONParser() throws IOException {

        testJSONParse("$.field1", "\"aa11\"");

        testJSONParse("$.field2", "[\n" +
                                  "  \"aa22\"\n" +
                                  "]");

        testJSONParse("$.structField.sf1", "[\n" +
                                           "  \"aaa11\",\n" +
                                           "  \"test\"\n" +
                                           "]");
        testJSONParse("$.structField.sf2", "{\n" +
                                           "  \"aaa22\": {\n" +
                                           "    \"test\": [\n" +
                                           "      1,\n" +
                                           "      2,\n" +
                                           "      3,\n" +
                                           "      4\n" +
                                           "    ]\n" +
                                           "  }\n" +
                                           "}");
        System.setOut(originOut);
    }

    @Test
    public void invalidCommandTest() throws IOException {
        testInvalidCommand("", "Please enter a command. For more detail write -help");
        testInvalidCommand("-abcd", "Sorry but input command \"-abcd\" is not supported. Type -help to see commands.");

        testInvalidCommand("-convert", "Attribute /srcFile was missed.");
        testInvalidCommand("-jsonpath", "Attribute /srcFile was missed.");
        testInvalidCommand("-unsql", "Attribute /srcFile was missed.");
        testInvalidCommand("-zip", "Attribute /src was missed.");
        testInvalidCommand("-querydelim /srcFile=\"src/test/resources/test.csv\"", "Attribute /query was missed.");

        testInvalidCommand("-zip /src", "Attribute /src is required but it was empty.");
        testInvalidCommand("-zip /src /destFile", "Attribute /src is required but it was empty.");
        testInvalidCommand("-zip /src /destFile=test.test", "Attribute /src is required but it was empty.");

        testInvalidCommand("-zip /src=\"src/test/resources/test.csv\"", "Attribute /destFile was missed.");
        testInvalidCommand("-zip /src=\"src/test/resources/test.csv\" /destFile", "Attribute /destFile is required but it was empty.");
       //testInvalidCommand("-zip /src=\"src/test/resources/test\" /destFile=test.test", "File does not exist: src\\test\\resources\\test");

        testInvalidCommand("-connect", "You missed \"url\" in a config file. Please add it and then repeat.");
    }

    @Test
    public void testSQLiteCreate() throws IOException {
        testSQLite("sqlite", "testDB.db");
        testSQLite("sqlite", "src/test/resources/testDB.db"); 
    }
    
    private void testSQLite(String jdbcConnection, String db) throws IOException {
        RAMainApplication.main(Commandline.translateCommandline("-sql /connection=\"" + jdbcConnection + "\" /op=createDB /db=\"" + db + "\""));

        File expectedFile = Paths.get(db).toFile();
        assertTrue(expectedFile.exists());
        //assertEquals(0, expectedFile.length());

        RAMainApplication.main(Commandline.translateCommandline("-sql /connection=\"" + jdbcConnection + "\" /op=importTable /db=\"" + db + "\" /mode=OVERWRITE /table=test /srcFile=src/test/resources/test.csv"));

        System.setOut(out);
        Properties properties = new Properties();
        @Cleanup FileReader reader = new FileReader(Paths.get("src/main/resources/log4j.properties").toFile());
        properties.load(reader);
        PropertyConfigurator.configure(properties);
        RAMainApplication.main(Commandline.translateCommandline("-sql /connection=\"" + jdbcConnection + "\" /op=queryDB /db=\"" + db + "\" /query=\"SELECT * from test\" /header=true"));

        String actual = outputStream.toString().trim();

        Assert.assertEquals(csvFile, actual);

        FileUtils.forceDelete(expectedFile);

        System.setOut(originOut);
        outputStream.reset();
    }

    private void testJSONParse(String jsonPattern, String expected) throws IOException {
        System.setOut(out);
        Properties properties = new Properties();
        @Cleanup FileReader reader = new FileReader(Paths.get("src/main/resources/log4j.properties").toFile());
        properties.load(reader);
        PropertyConfigurator.configure(properties);

        File srcFile = Paths.get("test.json").toFile();
        FileUtils.write(srcFile, jsonResponse, UTF_8);

        RAMainApplication.main(new String[]{"-jsonpath", "/srcFile=test.json", "/jsonPath=" + jsonPattern});

        String actual = outputStream.toString().trim();

        Assert.assertEquals((expected + "\nJson filtering done."), actual);
        outputStream.reset();
        System.setOut(out);

        File destFile = Paths.get("out.txt").toFile();
        RAMainApplication.main(new String[]{"-jsonpath", "/srcFile=test.json", "/destFile=" + destFile, "/jsonPath=" + jsonPattern});

        String json = FileUtils.readFileToString(destFile, UTF_8);
        assertEquals(expected, json);

        FileUtils.deleteQuietly(srcFile);
        FileUtils.deleteQuietly(destFile);
        outputStream.reset();
    }
    
    
    private void testUnSQL(String query, String expected) throws IOException {
        System.setOut(out);
        Properties properties = new Properties();
        @Cleanup FileReader reader = new FileReader(Paths.get("src/main/resources/log4j.properties").toFile());
        properties.load(reader);
        PropertyConfigurator.configure(properties);

        File srcFile = Paths.get("test.json").toFile();
        FileUtils.write(srcFile, jsonResponse, UTF_8);

        RAMainApplication.main(new String[]{"-unsql", "/srcFile=test.json", "/query=" + query});

        String actual = outputStream.toString().trim();

        Assert.assertEquals((expected + "\nUnSQL query done."), actual);
        outputStream.reset();
        System.setOut(out);

        File destFile = Paths.get("out.txt").toFile();
        RAMainApplication.main(new String[]{"-unsql", "/srcFile=test.json", "/destFile=" + destFile, "/query=" + query});

        String json = FileUtils.readFileToString(destFile, UTF_8);
        assertEquals(expected, json);

        FileUtils.deleteQuietly(srcFile);
        FileUtils.deleteQuietly(destFile);
        outputStream.reset();
    }
    
	private void testUnSQL(String query, String expected, String headers, String delimiter) throws IOException {
		Properties properties = new Properties();
		@Cleanup
		FileReader reader = new FileReader(Paths.get("src/main/resources/log4j.properties").toFile());
		properties.load(reader);
		PropertyConfigurator.configure(properties);

		File srcFile = Paths.get("test.json").toFile();
		FileUtils.write(srcFile, jsonResponseUnsql, UTF_8);

		File destFile = Paths.get("out.txt").toFile();
		RAMainApplication.main(new String[] { "-unsql", "/srcFile=test.json", "/destFile=" + destFile,
				"/query=" + query, "/delimiter=" + delimiter, "/headers=" + headers });

		String json = FileUtils.readFileToString(destFile, UTF_8);
		assertEquals(expected, json);

		FileUtils.deleteQuietly(srcFile);
		FileUtils.deleteQuietly(destFile);
		outputStream.reset();
	}

    private void testInvalidCommand(String command, String expected) throws IOException {
    	outputStream.reset();
        System.setOut(out);
        Properties properties = new Properties();
        @Cleanup FileReader reader = new FileReader(Paths.get("src/main/resources/log4j.properties").toFile());
        properties.load(reader);
        properties.put("log4j.appender.file.File", "logs.log");
        PropertyConfigurator.configure(properties);

        File srcFile = Paths.get("test.csv").toFile();
        FileUtils.write(srcFile, csvFile, UTF_8);

        exit.expectSystemExitWithStatus(1);
        try {
            RAMainApplication.main(Commandline.translateCommandline(command));
        } catch (CheckExitCalled e) {
            String actual = outputStream.toString().trim();
            Assert.assertEquals(expected, actual);
            outputStream.reset();
            System.setOut(out);
            outputStream.reset();
        }
    }

    private void testQuery(String sqlRequest, String expected) throws IOException {
        System.setOut(out);
        System.setProperty("log.file", "logs.log");
        Properties properties = new Properties();
        @Cleanup FileReader reader = new FileReader(Paths.get("src/main/resources/log4j.properties").toFile());
        properties.load(reader);
        PropertyConfigurator.configure(properties);

        File srcFile = Paths.get("test.csv").toFile();
        FileUtils.write(srcFile, csvFile, UTF_8);

        RAMainApplication.main(new String[]{"-querydelim", "/srcFile=test.csv", "/query=" + sqlRequest});

        String actual = outputStream.toString().trim();
        Assert.assertEquals(expected, actual);
        outputStream.reset();
        System.setOut(out);

        File destFile = Paths.get("out.txt").toFile();
        RAMainApplication.main(new String[]{"-querydelim", "/srcFile=test.csv", "/query=" + sqlRequest, "/destFile=" + destFile});

        actual = FileUtils.readFileToString(destFile, UTF_8).trim();
        assertEquals(expected, actual);

        File queryFile = Paths.get("query.txt").toFile();
        FileUtils.write(queryFile, sqlRequest, UTF_8);

        outputStream.reset();
        FileUtils.deleteQuietly(queryFile);
        FileUtils.deleteQuietly(srcFile);
        FileUtils.deleteQuietly(destFile);
    }

    private void testZip(String command) {
        RAMainApplication.main(Commandline.translateCommandline(command));

        File file = EXPECTED_FILE_PATH.toFile();
        assertTrue(file.exists());
        cleanDirectory();
    }

    private void testConnect(String command) throws Exception {
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn(builder);
        PowerMockito.when(builder.setDefaultCredentialsProvider(Mockito.any())).thenReturn(builder);
        PowerMockito.when(builder.build()).thenReturn(client);
        PowerMockito.when(client.execute(Mockito.any())).thenReturn(response);
        PowerMockito.when(response.getStatusLine()).thenReturn(statusLine);
        PowerMockito.when(statusLine.getStatusCode()).thenReturn(200);

        PowerMockito.when(response.getEntity()).thenReturn(entity);
        PowerMockito.mockStatic(EntityUtils.class);
        PowerMockito.when(EntityUtils.toString(entity, UTF_8)).thenReturn(jsonResponse);

        RAMainApplication.main(Commandline.translateCommandline(command));
        cleanDirectory();
    }

    private void testPassword() {
        String command = "-password /passwordFile=\"" + EXPECTED_FILE_PATH.toFile() + "\"";
        PowerMockito.mockStatic(System.class);

        PowerMockito.when(System.console()).thenReturn(getConsole());

        String password = "test";
        PowerMockito.when(console.readPassword(" ", "Enter")).thenReturn(password.toCharArray());

        RAMainApplication.main(Commandline.translateCommandline(command));
        File passwordFile = EXPECTED_FILE_PATH.toFile();
        assertTrue(passwordFile.exists());
        FileUtils.deleteQuietly(EXPECTED_FILE_PATH.toFile());
        //assertEquals(password, decrypt(passwordFile));

        cleanDirectory();
    }

    public static void cleanDirectory() {
        for (File file : Objects.requireNonNull(RAMainApplicationTest.RESOURCES.listFiles())) {
            if (!file.getName().equals("test.zip") && 
            		!file.getName().equals("test.csv") && 
            		!file.getName().equals("test_mysql.csv") && 
            		!file.getName().equals("test_post.csv") && 
            		!file.getName().equals("config.properties") &&
            		!file.getName().equals("newdb.accdb")) {
                //delete file
                FileUtils.deleteQuietly(file);
            }
        }
    }

    private Console getConsole() {
        return console;
    }


    private void testUnzip(String command) {
        RAMainApplication.main(Commandline.translateCommandline(command));

        File file = EXPECTED_FILE_PATH.toFile();
        assertTrue(file.exists());
        FileUtils.deleteQuietly(EXPECTED_FILE_PATH.toFile());
        cleanDirectory();
    }

    private void testConvert(String srcFile, String destFile) throws IOException {
        Path srcFilePath = Paths.get(srcFile);
        Path destFilePath = Paths.get(destFile);

        if (!srcFilePath.toFile().exists()) {
            File file = srcFilePath.toAbsolutePath().toFile();
            FileUtils.write(file, csvFile, UTF_8);
        }

        RAMainApplication.main(Commandline.translateCommandline("-convert /srcFile=\"" + srcFilePath.toAbsolutePath() + "\" /destFile=\"" + destFilePath.toAbsolutePath() + "\" /delim=\",\" /sheetname=\"ABCD\""));

        File file = destFilePath.toFile();
        assertTrue(file.exists());

        File startFile = srcFilePath.toFile();
        srcFilePath = destFilePath;
        String extension = FilenameUtils.getExtension(destFilePath.toString());
        if (extension.equals("xlsx")) {
            destFilePath = Paths.get("test.csv");
        } else {
            destFilePath = Paths.get("test.xlsx");
        }
        RAMainApplication.main(Commandline.translateCommandline("-convert /srcFile=\"" + srcFilePath.toAbsolutePath() + "\" /destFile=\"" + destFilePath.toAbsolutePath() + "\" /sheetname=\"ABCD\""));


        String expectedContent = FileUtils.readFileToString(startFile, UTF_8).trim();
        String actualContent = FileUtils.readFileToString(destFilePath.toFile(), UTF_8).trim();

        expectedContent = expectedContent.replaceAll("\r\n", "\n");
        actualContent = actualContent.replaceAll("\r\n", "\n");
        assertEquals(expectedContent, actualContent);

        File logFile = Paths.get("test.log").toFile();
        RAMainApplication.main(Commandline.translateCommandline("-convert /srcFile=\"" + srcFilePath.toAbsolutePath() + "\" /destFile=\"" + destFilePath.toAbsolutePath() + "\" /sheetname=\"ABCD\" /delim=\";\" /log=\" " + logFile + "\""));

        assertEquals(expectedContent, actualContent);
        assertTrue(logFile.exists());

        Properties properties = new Properties();
        @Cleanup FileReader reader = new FileReader(Paths.get("src/main/resources/log4j.properties").toFile());
        properties.load(reader);
        PropertyConfigurator.configure(properties);

        FileUtils.deleteQuietly(logFile);

        File recourseCSV = Paths.get("src/test/resources/test.csv").toFile();
        if (!startFile.equals(recourseCSV)) {
            FileUtils.deleteQuietly(startFile);
        }
        if (!destFilePath.toFile().equals(recourseCSV)) {
            FileUtils.deleteQuietly(destFilePath.toFile());
        }
        if (!srcFilePath.toFile().equals(recourseCSV)) {
            FileUtils.deleteQuietly(srcFilePath.toFile());
        }

        cleanDirectory();
    }
}
