package com.acceleratetechnology.controller;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;
import com.github.villanianalytics.unsql.UnSql;
import com.github.villanianalytics.unsql.UnSql.EXPORT_FORMAT;
import com.github.villanianalytics.unsql.exception.UnSqlException;
import com.google.gson.*;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Connect to any web service by using http response and get a result from there.
 */
public class ConnectCommand extends EncryptDecryptAbstractCommand {
    /**
     * Multiline format data connection method.
     */
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
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
     * Need to add in header for connection method.
     */
    private static final String ACCEPT = "Accept";
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
     * HTTP protocol settings.
     */
    private static final String HTTPS_PROTOCOLS = "https.protocols";
    /**
     * HTTP connection version.
     */
    private static final String TLSV_1_2 = "TLSv1.2";
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
    public static final String JSON_PATH_PARAM = "/jsonPath";
    /**
     * Unsql filter attribute.
     */
    public static final String UNSQL_PATH_PARAM = "/query";
    /**
     * Destination xlsx file command line parameter.
     */
    public static final String DEST_FILE_PARAM = "/destFile";

    @Command("-connect")
    public ConnectCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    @Override
    public void execute() throws IOException, MissedParameterException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException {
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

        String response = getJSONResponse(user, pw, token, url, restMethod, srcFile, body, connectMethod);

        String jsonPath = getAttribute(JSON_PATH_PARAM);
        if (jsonPath != null && !jsonPath.isEmpty()) {
            response = jsonFilter(jsonPath, response);
        }
        
        String unSqlQuery = getAttribute(UNSQL_PATH_PARAM);
        if (unSqlQuery != null && !unSqlQuery.isEmpty()) {
            response = unSqlFilter(unSqlQuery, response, DEST_FILE_PARAM);
        }
        
        if (StringUtils.isEmpty(jsonPath) && StringUtils.isEmpty(unSqlQuery)) {
        	logger.info(response);
        }
        
        String jsonFile = getAttribute(DEST_FILE_PARAM);
        if (jsonFile != null && !jsonFile.isEmpty()) {
            Path jsonDir = Paths.get(jsonFile);
            File file = jsonDir.toFile();

            logger.debug("Write response to file: " + jsonFile);
            FileUtils.write(file, String.valueOf(response), UTF_8);
            logger.info("Done.");
        }
    }

    /**
     * Send request with credentials and get a response. Then writes this response to a file.
     *
     * @param sourceFile       File.
     * @param user             Username.
     * @param pw               Password.
     * @param url              URL.
     * @param restMethod       Method.
     * @param connectionMethod Connection method.
     * @param body             body message.
     * @return full JSON response.
     */
    private String getJSONResponse(String user, String pw, String token, String url, HttpMethod restMethod, File sourceFile, String body, String connectionMethod) throws IOException, MissedParameterException {
        System.setProperty(HTTPS_PROTOCOLS, TLSV_1_2);
        String encoding;
        CredentialsProvider provider;
        HttpClient client;
        if (user != null) {
            encoding = Base64.getEncoder().encodeToString((user + ":" + pw).getBytes(UTF_8));
            provider = new BasicCredentialsProvider();
            provider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(user, pw)
            );
            client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        } else {
            encoding = token;
            client = HttpClientBuilder.create().build();
        }

        HttpResponse response = null;

        if (sourceFile != null && !connectionMethod.equals(MULTIPART_FORM_DATA)) {
            throw new MissedParameterException("You try to send a file with \"" + connectionMethod + "\" but Content-Type need to be multipart/form-data.\nPlease add /connectType=multipart/form-data and try again.");
        }
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        if (sourceFile != null) {
            builder = addFile(sourceFile);
        }
        if (body != null) {
            builder = builder.addTextBody("", body);
        }

        if (restMethod.equals(HttpMethod.POST)) {
            response = makePost(url, connectionMethod, encoding, client, builder);
        } else if (restMethod.equals(HttpMethod.GET)) {
            response = makeGet(url, connectionMethod, encoding, client);
        } else if (restMethod.equals(HttpMethod.HEAD)) {
            response = makeHead(url, connectionMethod, encoding, client);
        } else if (restMethod.equals(HttpMethod.PUT)) {
            response = makePut(url, connectionMethod, encoding, client, builder);
        } else if (restMethod.equals(HttpMethod.PATCH)) {
            response = makePatch(url, connectionMethod, encoding, client);
        } else if (restMethod.equals(HttpMethod.DELETE)) {
            response = makeDelete(url, connectionMethod, encoding, client);
        }

        int httpStatus = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String responseMsg;
        if (entity == null) {
            responseMsg = "";
        } else {
            responseMsg = EntityUtils.toString(entity, UTF_8);
        }

        if (httpStatus < 200 || httpStatus > 300) {
            throw new IOException("HTTP " + httpStatus + " - Error during upload of file: " + responseMsg);
        }

        String responseResult = "";
        if (!responseMsg.isEmpty()) {
            responseResult = prettyJsonFormatter(responseMsg);
        }

        return responseResult;
    }

    /**
     * Send delete request to url by using connection method with user details or token. And get a response.
     *
     * @param url              Web server url.
     * @param connectionMethod Connection method.
     * @param encoding         Encoded username and password, or token.
     * @param client           Client info.
     * @return Web server response
     * @throws IOException throws if input or output exception was.
     */
    private HttpResponse makeDelete(String url, String connectionMethod, String encoding, HttpClient client) throws IOException {
        HttpResponse response;
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader(ACCEPT, connectionMethod);
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, encoding);

        response = client.execute(httpDelete);
        return response;
    }

    /**
     * Send patch request to url by using connection method with user details or token. And get a response.
     *
     * @param url              Web server url.
     * @param connectionMethod Connection method.
     * @param encoding         Encoded username and password, or token.
     * @param client           Client info.
     * @return Web server response
     * @throws IOException throws if input or output exception was.
     */
    private HttpResponse makePatch(String url, String connectionMethod, String encoding, HttpClient client) throws IOException {
        HttpResponse response;
        HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.setHeader(ACCEPT, connectionMethod);
        httpPatch.setHeader(HttpHeaders.AUTHORIZATION, encoding);

        response = client.execute(httpPatch);
        return response;
    }

    /**
     * Send put request to url by using connection method with user details or token. And get a response.
     *
     * @param url              Web server url.
     * @param connectionMethod Connection method.
     * @param encoding         Encoded username and password, or token.
     * @param client           Client info.
     * @return Web server response
     * @throws IOException throws if input or output exception was.
     */
    private HttpResponse makePut(String url, String connectionMethod, String encoding, HttpClient client, MultipartEntityBuilder builder) throws IOException {
        HttpResponse response;
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader(ACCEPT, connectionMethod);
        httpPut.setHeader(HttpHeaders.AUTHORIZATION, encoding);

        if (builder != null) {
            httpPut.setEntity(builder.build());
        }

        response = client.execute(httpPut);
        return response;
    }

    /**
     * Send head request to url by using connection method with user details or token. And get a response.
     *
     * @param url              Web server url.
     * @param connectionMethod Connection method.
     * @param encoding         Encoded username and password, or token.
     * @param client           Client info.
     * @return Web server response
     * @throws IOException throws if input or output exception was.
     */
    private HttpResponse makeHead(String url, String connectionMethod, String encoding, HttpClient client) throws IOException {
        HttpResponse response;
        HttpHead httpHead = new HttpHead(url);
        httpHead.setHeader(ACCEPT, connectionMethod);
        httpHead.setHeader(HttpHeaders.AUTHORIZATION, encoding);

        response = client.execute(httpHead);
        return response;
    }

    /**
     * Send get request to url by using connection method with user details or token. And get a response.
     *
     * @param url              Web server url.
     * @param connectionMethod Connection method.
     * @param encoding         Encoded username and password, or token.
     * @param client           Client info.
     * @return Web server response
     * @throws IOException throws if input or output exception was.
     */
    private HttpResponse makeGet(String url, String connectionMethod, String encoding, HttpClient client) throws IOException {
        HttpResponse response;
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(ACCEPT, connectionMethod);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, encoding);

        response = client.execute(httpGet);
        return response;
    }

    /**
     * Send post request to url by using connection method with user details or token. And get a response.
     *
     * @param url              Web server url.
     * @param connectionMethod Connection method.
     * @param encoding         Encoded username and password, or token.
     * @param client           Client info.
     * @return Web server response
     * @throws IOException throws if input or output exception was.
     */
    private HttpResponse makePost(String url, String connectionMethod, String encoding, HttpClient client, MultipartEntityBuilder builder) throws IOException {
        HttpResponse response;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(ACCEPT, connectionMethod);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, encoding);

        if (builder != null) {
            httpPost.setEntity(builder.build());
        }

        response = client.execute(httpPost);
        return response;
    }

    /**
     * Attache file to request body.
     *
     * @param sourceFile Source file.
     * @return multipart form with file.
     */
    private MultipartEntityBuilder addFile(File sourceFile) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        // fileParamName should be replaced with parameter name your REST API expect.
        builder.addPart("file", new FileBody(sourceFile));
        return builder;
    }

    /**
     * Format JSON to a pretty formatted text.
     *
     * @param response JSON.
     * @return formatted JSON.
     */
    public String prettyJsonFormatter(String response) {
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
        logger.debug("Read Json body " + jsonBody + " jsonPath " + jsonPath);
        String results = JsonPath.read(jsonBody, jsonPath).toString();
        logger.debug("Done.");
        return results;
    }

    /**
     * Return part from JSON and format it.
     *
     * @param jsonPath JSON path.
     * @param json     JSON text.
     * @return Formatted part from JSON.
     */
    public String jsonFilter(String jsonPath, String json) {
        String formattedMessage = prettyJsonFormatter(getJsonPath(json, jsonPath));
        logger.info(formattedMessage);
        return formattedMessage;
    }
    
    /**
     * Return filtered json by sql query
     *
     * @param jsonPath JSON path.
     * @param json     JSON text.
     * @return Filtered Json.
     * @throws MissedParameterException 
     */
    public String unSqlFilter(String query, String json, String destFile) throws MissedParameterException {
        String filteredJson = "";
        UnSql unsql = new UnSql(json);
        
        try {
        	filteredJson = unsql.executeQuery(query, getExportFormat(destFile));
		} catch (UnSqlException e) {
			throw new MissedParameterException(e.getMessage());
		}
        
        logger.info(filteredJson);
        return filteredJson;
    }
    
    /**
     *  Return unsql export format
     * @param destFile
     * @return
     */
    private UnSql.EXPORT_FORMAT getExportFormat(String destFile) {
    	if (destFile != null && destFile.endsWith(".xml")) {
    		return EXPORT_FORMAT.XML;
    	}
    	
    	if (destFile != null && destFile.endsWith(".json")) {
    		return EXPORT_FORMAT.JSON;
    	}
    	
    	if (destFile != null && destFile.endsWith(".txt")) {
    		return EXPORT_FORMAT.VALUES;
    	}
    	
    	return EXPORT_FORMAT.VALUES;
    }

    private enum HttpMethod {
        GET, HEAD, POST, PUT, PATCH, DELETE;
    }
}
