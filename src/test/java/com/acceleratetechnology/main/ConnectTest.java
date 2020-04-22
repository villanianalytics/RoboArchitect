package com.acceleratetechnology.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	private String baseUrl;
	private File file;

	@Before
	public void setUp() throws IOException {
		mockBackEnd = new MockWebServer();
		mockBackEnd.start();
		configFile = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config2.properties";
    	baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
    	createFile(configFile);
    	appendUrlToConfiguration("\nurl=" + baseUrl);
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

        String body = "{}";
        createFile(configFile);
        appendUrlToConfiguration("\nurl=" + baseUrl);
        appendUrlToConfiguration("\nhttpmethod=PATCH");
        
        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE+TWO" + File.separator + "jsonResponse.json");
        command = "-connect /body=\""+body+"\" /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + configFile + "\"";
        testConnect(command);

        createFile(configFile);
        appendUrlToConfiguration("\nurl=" + baseUrl);
        appendUrlToConfiguration("\nhttpmethod=HEAD");
        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE TWO" + File.separator + "jsonResponse.json");
        command = "-connect /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + configFile + "\"";
        testConnect(command);

        createFile(configFile);
        appendUrlToConfiguration("\nurl=" + baseUrl);
        appendUrlToConfiguration("\nhttpmethod=PUT");
        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json");
        command = "-connect /body=\""+body+"\" /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + configFile + "\"";
        testConnect(command);

        createFile(configFile);
        appendUrlToConfiguration("\nurl=" + baseUrl);
        appendUrlToConfiguration("\nhttpmethod=POST");
        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        Path destFilePath = Paths.get(configFile);
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json");
        command = "-connect /body=\""+body+"\" /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + destFilePath.toAbsolutePath() + "\"";
        testConnect(command);
        
        createFile(configFile);
        appendUrlToConfiguration("\nurl=" + baseUrl);
        appendUrlToConfiguration("\nhttpmethod=DELETE");
        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json");
        command = "-connect /body=\""+body+"\" /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + configFile + "\"";
        testConnect(command);
        
        createFile(configFile);
        appendUrlToConfiguration("\nurl=" + baseUrl);
        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json");
        command = "-connect /token=\"test\" /body=\""+body+"\" /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + configFile + "\"";
        testConnect(command);
        
        createFile(configFile);
        appendUrlToConfiguration("\nurl=" + baseUrl);
        appendUrlToConfiguration("\nuser=test");
        appendUrlToConfiguration("\npassword=test");
        mockBackEnd.enqueue(new MockResponse().setBody(jsonResponse).addHeader("Content-Type", "application/json"));
        srcFilePath = Paths.get("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test" + File.separator + "ONE&TWO" + File.separator + "jsonResponse.json");
        command = "-connect /body=\""+body+"\" /destFile=\"" + srcFilePath.toAbsolutePath() + "\" /config=\"" + configFile + "\"";
        testConnect(command);
    }
	
	public void testConnect(String command) {
		RAMainApplication.main(Commandline.translateCommandline(command));	
	}
	
	public void appendUrlToConfiguration(String text) throws IOException {
		FileUtils.writeStringToFile(file, text, StandardCharsets.UTF_8, true);	     
	}
	
	public void createFile(String fileName) throws IOException {
		file = new File(fileName);
		if (!file.createNewFile()) cleanDirectory();
		file.createNewFile();
	}
	
	public void cleanDirectory() {
		FileUtils.deleteQuietly(file);
    }
	
	@After
	public void tearDown() throws IOException {
		mockBackEnd.shutdown();
		cleanDirectory();
	}
}
