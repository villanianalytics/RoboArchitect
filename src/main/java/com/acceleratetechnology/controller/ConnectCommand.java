package com.acceleratetechnology.controller;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.acceleratetechnology.connect.HttpUtils;
import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.github.villanianalytics.unsql.UnSql;
import com.github.villanianalytics.unsql.UnSql.EXPORT_FORMAT;
import com.github.villanianalytics.unsql.exception.UnSqlException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.jayway.jsonpath.JsonPath;

import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Connect to any web service by using http response and get a result from there.
 */
public class ConnectCommand extends EncryptDecryptAbstractCommand {
    /**
     * Body of request attribute.
     */
    private static final String BODY = "/body";
    /**
     * Connection type attribute.
     */
    private static final String CONNECT_TYPE = "/mediaType";
    /**
     * Default connect type.
     */
    private static final String APPLICATION_JSON = "application/json";
    /**
     * New line regex.
     */
    private static final String NEW_LINE_REGEX = "(\\\\n)";
    /**
     * Tab regex.
     */
    private static final String TAB_REGEX = "(\\\\t)";
    /**
     * New line string symbol.
     */
    private static final String NEW_LINE_STRING = "\n";
    /**
     * Tab string symbol.
     */
    private static final String TAB_STRING = "\t";
    /**
     * Token attribute.
     */
    private static final String TOKEN = "/token";
    /**
     * System logger.
     */
    private Logger logger = Logger.getLogger(ConnectCommand.class);
    /**
     * HTTP method attribute.
     */
    private static final String HTTP_ATTRIBUTE = "/reqType";
    /**
     * Default HTTP method connection.
     */
    private static final String DEFAULT_HTTP_METHOD = "GET";
    /**
     * Input file with JSON.
     */
    private static final String SRC_FILE = "/srcFile";
    /**
     * User field in connect property file.
     */
    public static final String USER_NAME_PARAMETER = "user";
    /**
     * Connect url property field.
     */
    private static final String URL_PARAMETER = "url";
    /**
     * Field with Http connection method.
     */
    private static final String HTTP_METHOD_PARAMETER = "httpmethod";
    /**
     * Json filter attribute.
     */
    public static final String JSON_PARAM = "/jsonPath";
    /**
     * Unsql filter attribute.
     */
    public static final String UNSQL_PARAM = "/query";
    /**
     * Destination xlsx file command line parameter.
     */
    public static final String DEST_FILE_PARAM = "/destFile";
    
    /** The Constant DELIMITER. */
    public static final String DELIMITER ="/delimiter";
    
    /** The Constant HEADERS. */
    public static final String HEADERS ="/headers";
    
    /** The delimiter value. */
    private String delimiterValue = ",";

	/** The headers flag. */
	private boolean headersFlag = false;

	
	private static final String XML_FILE = ".xml";
	private static final String JSON_FILE = ".json";
	private static final String TEXT_FILE = ".txt";
	
	
    /**
     * Instantiates a new connect command.
     *
     * @param args the args
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MissedParameterException the missed parameter exception
     */
    @Command("-connect")
    public ConnectCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    /**
     * Execute.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws MissedParameterException the missed parameter exception
     * @throws IllegalBlockSizeException the illegal block size exception
     * @throws NoSuchPaddingException the no such padding exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException the invalid key exception
     * @throws BadPaddingException the bad padding exception
     */
    @Override
    public void execute() throws IOException, MissedParameterException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException {
    	logger.trace("ConnectCommand.execute started");
    	
    	String url = getAttribute(URL_PARAMETER);
        if (url == null) {
            throw new MissedParameterException("You missed \"url\" in a config file. Please add it and then repeat.");
        }
        String user = getAttribute(USER_NAME_PARAMETER);
        String pw;
        String token;
        if (user != null) {
            pw = getPassword();
            token = null;
        } else {
            token = getAttribute(TOKEN);
            pw = null;
        }
        String httpMethod = getFromConfOrAttribute(HTTP_METHOD_PARAMETER, HTTP_ATTRIBUTE);
        if (httpMethod == null) {
            httpMethod = DEFAULT_HTTP_METHOD;
        }
        String inputJSON = getAttribute(SRC_FILE);
        String body = getAttribute(BODY);

        if (body != null) {
            body = body.replaceAll(NEW_LINE_REGEX, NEW_LINE_STRING)
                    .replaceAll(TAB_REGEX, TAB_STRING);
        }
        String connectMethod = getDefaultAttribute(CONNECT_TYPE, APPLICATION_JSON);

        File srcFile = null;
        if (inputJSON != null) {
            srcFile = Paths.get(inputJSON).toFile();
        }

        HttpMethod restMethod;

        try {
            restMethod = HttpMethod.valueOf(httpMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MissedParameterException("Connection method \"" + httpMethod + "\" is not supported.");
        }

        HttpUtils httpUtil = createUtilsWithHeaders(url, user, pw, token, connectMethod);
        String response = getJSONResponse(httpUtil, restMethod, srcFile, body, connectMethod);

        String jsonPath = getAttribute(JSON_PARAM);
        if (jsonPath != null && !jsonPath.isEmpty()) {
            response = jsonFilter(jsonPath, response);
        }
        
        String unSqlQuery = getAttribute(UNSQL_PARAM);
        if (unSqlQuery != null && !unSqlQuery.isEmpty()) {
        	String destFile = getAttribute(DEST_FILE_PARAM);
            String delimiterAttr = getAttribute(DELIMITER);
            String headersAttr = getAttribute(HEADERS);
            
            if (StringUtils.isEmpty(destFile) || !destFile.endsWith(".txt")) {
            	if (!StringUtils.isEmpty(delimiterAttr)) logger.warn("Delimiter paramenter will be ingnore, it should only be used with .txt files");
            	if (!StringUtils.isEmpty(headersAttr)) logger.warn("Headers paramenter will be ingnore, it should only be used with .txt files");
            } else {
            	if (!StringUtils.isEmpty(delimiterAttr)) setDelimiterValue(delimiterAttr);
            	if (!StringUtils.isEmpty(headersAttr)) setHeadersFlag(true);
            }
            
            if (unSqlQuery.endsWith(".txt")) {
            	unSqlQuery = readQueryFile(unSqlQuery);
            }
            
            response = unSqlFilter(unSqlQuery, response, destFile);
        }
        
        logResponse(response);
        
        String jsonFile = getAttribute(DEST_FILE_PARAM);
        if (jsonFile != null && !jsonFile.isEmpty()) {
            Path jsonDir = Paths.get(jsonFile);
            File file = jsonDir.toFile();

            logger.debug("Write response to file: " + jsonFile);
            FileUtils.write(file, String.valueOf(response), UTF_8);
            logResponse("Response wrriten to file : " + jsonDir.getFileName());
        }
    }
    
    
    private HttpUtils createUtilsWithHeaders(String url, String user, String pw, String token, String connectionMethod) {
    	logger.trace("ConnectCommand.createUtilsWithHeaders started");
    	HttpUtils httpUtil = new HttpUtils(url);
    	
    	if (user != null ) {
    		httpUtil.addHeadersAuth(user, pw);
    	}
    	
    	if (token != null) {
    		httpUtil.addHeadersAuth(token);
    	}
    	
    	httpUtil.addHeadersContent(connectionMethod);
    	
    	return httpUtil;
    }
    
    
    private String getJSONResponse(HttpUtils httpUtil, HttpMethod restMethod, File sourceFile, String body, String connectionMethod) throws IOException {
    	logger.trace("ConnectCommand.getJSONResponse started");
    	RequestBody requestBody = null;
    	if (sourceFile != null) {
    		requestBody = httpUtil.createBody(sourceFile);
    	}
    	if (body != null ) {
    		requestBody = httpUtil.createBody(connectionMethod, body);
    	}
    	
    	Response response = httpUtil.doCall(restMethod.toString(), requestBody);
    	int status = response.code();
    	String responseBody = response.body().string();
    	
    	if (status < 200 || status > 300) {
             throw new IOException("HTTP " + response.code() + " - Error during upload of file: " + responseBody);
        }
    	
    	if (!responseBody.isEmpty()) {
           return prettyJsonFormatter(responseBody);
        }
    	
    	return responseBody;
    } 
 
    /**
     /**
     * Format JSON to a pretty formatted text.
     *
     * @param response JSON.
     * @return formatted JSON.
     */
    public String prettyJsonFormatter(String response) {
    	logger.trace("ConnectCommand.prettyJsonFormatter started");
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement element;
        String result;
        try {
            element = JsonParser.parseString(response);
            result = gson.toJson(element);
        } catch (JsonSyntaxException e) {
            result = response;
        }

        return result;
    }

    /**
     * Return part from JSON.
     *
     * @param jsonBody JSON text.
     * @param jsonPath JSON path.
     * @return Part of a JSON.
     */
    public String getJsonPath(String jsonBody, String jsonPath) {
    	logger.trace("ConnectCommand.getJsonPath started");
        return JsonPath.read(jsonBody, jsonPath).toString();
    }

    /**
     * Return part from JSON and format it.
     *
     * @param jsonPath JSON path.
     * @param json     JSON text.
     * @return Formatted part from JSON.
     */
    public String jsonFilter(String jsonPath, String json) {
    	logger.trace("ConnectCommand.jsonFilter started");
        return prettyJsonFormatter(getJsonPath(json, jsonPath));
    }
    
    /**
     * Return filtered json by sql query.
     *
     * @param query the query
     * @param json     JSON text.
     * @param destFile the dest file
     * @return Filtered Json.
     * @throws MissedParameterException the missed parameter exception
     */
    public String unSqlFilter(String query, String json, String destFile) throws MissedParameterException {
    	logger.trace("ConnectCommand.unSqlFilter started");
    	String results = "";
       
    	try {
        	if (destFile != null && destFile.endsWith(XML_FILE)) {
        		results = new UnSql(json)
        				.withExportFormat(EXPORT_FORMAT.XML)
        				.execute(query);
        		
        	} else  if (destFile != null && destFile.endsWith(JSON_FILE)) {
        		results = new UnSql(json)
        				.withExportFormat(EXPORT_FORMAT.JSON)
        				.execute(query);
        		
        	} else  if (destFile != null && destFile.endsWith(TEXT_FILE)) {
        		results = new UnSql(json)
        				.withExportFormat(EXPORT_FORMAT.VALUES)
        				.withRowDelimiter(delimiterValue)
        				.withHeaders(headersFlag)
        				.execute(query);
        		
        	} else {
        		results = new UnSql(json)
        				.withExportFormat(EXPORT_FORMAT.VALUES)
        				.withRowDelimiter(delimiterValue)
        				.withHeaders(headersFlag)
        				.execute(query);
        	}
  		} catch (UnSqlException e) {
			throw new MissedParameterException(e.getMessage());
		}
        
        logger.info(results);
        return results;
    }
    
    private String readQueryFile(String filePath) throws IOException {
    	logger.trace("ConnectCommand.readQueryFile started");
    	String query = FileUtils.readFileToString(Paths.get(filePath).toFile(), UTF_8);
    	
    	return query.replaceAll("\\r|\\n", "");
    }

    /**
     * Checks if is headers flag.
     *
     * @return true, if is headers flag
     */
    public boolean isHeadersFlag() {
		return headersFlag;
	}

	/**
	 * Sets the headers flag.
	 *
	 * @param headersFlag the new headers flag
	 */
	public void setHeadersFlag(boolean headersFlag) {
		this.headersFlag = headersFlag;
	}

	/**
	 * Gets the delimiter value.
	 *
	 * @return the delimiter value
	 */
	public String getDelimiterValue() {
		return delimiterValue;
	}

	/**
	 * Sets the delimiter value.
	 *
	 * @param delimiterValue the new delimiter value
	 */
	public void setDelimiterValue(String delimiterValue) {
		this.delimiterValue = delimiterValue;
	}

	/**
	 * The Enum HttpMethod.
	 */
	private enum HttpMethod {
         /** The get. */
	     GET, 
		 /** The head. */
		 HEAD, 
		 /** The post. */
		 POST, 
		 /** The put. */
		 PUT, 
		 /** The patch. */
		 PATCH, 
		 /** The delete. */
		 DELETE
    }
}
