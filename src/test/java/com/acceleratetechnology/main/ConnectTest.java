package com.acceleratetechnology.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.types.Commandline;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
public class ConnectTest {
	
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
	
	private static MockWebServer mockBackEnd;
	private String configFile;

	@Before
	public void setUp() throws IOException {
		mockBackEnd = new MockWebServer();
		mockBackEnd.start();
		configFile = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config2.properties";
    	String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
    	appendUrlToConfiguration(configFile, baseUrl);
	}
	
    @Test
    public void connectTest() throws Exception {
    	mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
    	
        String command = "-connect /destFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "jsonResponse.json\" /config=\"" + configFile+"\"";
        testConnect(command);

        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        command = "-connect /destFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE_TWO" + File.separator + "jsonResponse.json\" /config=\"" + configFile+ "\"";
        testConnect(command);

        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        command = "-connect /destFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE-TWO" + File.separator + "jsonResponse.json\" /config=\"" + configFile+ "\"";
        testConnect(command);

        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        command = "-connect /destFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE TWO" + File.separator + "jsonResponse.json\" /config=\"" + configFile+ "\"";
        testConnect(command);

        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        command = "-connect /destFile=\"src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json\" /config=\"" + configFile+ "\"";
        testConnect(command);

        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        Path srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE_TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\""+ configFile +"\"";
        testConnect(command);

        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE-TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + configFile + "\"";
        testConnect(command);

        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE+TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + configFile + "\"";
        testConnect(command);

        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE TWO" + File.separator + "jsonResponse.json");
        command = "-connect  /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + configFile + "\"";
        testConnect(command);

        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + configFile + "\"";
        testConnect(command);

        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        Path destFilePath = Paths.get(configFile);
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + destFilePath.toAbsolutePath() + "\"";
        testConnect(command);
    }
	
	public void testConnect(String command) {
		RAMainApplication.main(Commandline.translateCommandline(command));
		
	}
	
	public void appendUrlToConfiguration(String fileName, String url) throws IOException {
		File file = new File(fileName);
		file.createNewFile();
		FileUtils.writeStringToFile(file, "\nurl="+url, StandardCharsets.UTF_8, true);	     
	}
	
	public void cleanDirectory() {
        for (File file : Objects.requireNonNull(RAMainApplicationTest.RESOURCES.listFiles())) {
            if (!file.getName().equals("test.zip") && 
            		!file.getName().equals("test.csv") && 
            		!file.getName().equals("test_mysql.csv") && 
            		!file.getName().equals("test_post.csv") && 
            		!file.getName().equals("config.properties")) {
                //delete file
                FileUtils.deleteQuietly(file);
            }
        }
    }
	
	@After
	public void tearDown() throws IOException {
		mockBackEnd.shutdown();
		cleanDirectory();
	}
}
