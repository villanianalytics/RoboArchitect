package com.acceleratetechnology.connect;

import java.io.File;
import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * The Class HttpUtils.
 */
public class HttpUtils {

	/** The builder. */
	private Request.Builder builder;
	
	/**
	 * Instantiates a new http utils.
	 *
	 * @param url the url
	 */
	public HttpUtils(String url) {
		this.builder = new Request.Builder();

		builder.url(url);
	}
	
	/**
	 * Adds the headers auth.
	 *
	 * @param username the username
	 * @param password the password
	 */
	public void addHeadersAuth(String username, String password) {
		String credential = Credentials.basic(username, password);
		builder.addHeader("Authorization", credential);
	}
	
	/**
	 * Adds the headers auth.
	 *
	 * @param token the token
	 */
	public void addHeadersAuth(String token) {
		builder.addHeader("Authorization", "Basic " + token);
	}
	
	/**
	 * Adds the headers content.
	 *
	 * @param type the type
	 */
	public void addHeadersContent(String type) {
		builder.addHeader("Content-Type", type);
	}
	
	/**
	 * Creates the body.
	 *
	 * @param type the type
	 * @param body the body
	 * @return the request body
	 */
	public RequestBody createBody(String type, String body) {
		MediaType mediaType = MediaType.parse(type);
		return RequestBody.create(body, mediaType);
	}
	
	/**
	 * Creates the body.
	 *
	 * @param file the file
	 * @return the request body
	 */
	public RequestBody createBody(File file) {
		MediaType mediaType = MediaType.parse("multipart/*");
		
		return new MultipartBody.Builder()
				        .setType(MultipartBody.FORM)
				        .addFormDataPart("", file.getName(), RequestBody.create(file, mediaType))
				        .build();
	}
	
	/**
	 * Do call.
	 *
	 * @param method the method
	 * @param body the body
	 * @return the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Response doCall(String method, RequestBody body) throws IOException {
		Response response = null;
		switch (method) {
			case "GET":
				response = get();
				break;
			case "POST":
				response = post(body);
				break;
			case "HEAD":
				response = head(body);
				break;
			case "PUT":
				response = put(body);
				break;
			case "PATCH":
				response = patch(body);
				break;
			case "DELETE":
				response = delete(body);
				break;
		default:
			break;
		}
		
		return response;
		
	}
	
	/**
	 * Gets the.
	 *
	 * @return the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Response get() throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.build();
	
		return client.newCall(request).execute();
	}
	
	/**
	 * Post.
	 *
	 * @param body the body
	 * @return the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Response post(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("POST", body).build();
	
		return client.newCall(request).execute();
	}
	
	/**
	 * Put.
	 *
	 * @param body the body
	 * @return the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Response put(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("PUT", body).build();
	
		return client.newCall(request).execute();
	}
	
	/**
	 * Head.
	 *
	 * @param body the body
	 * @return the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Response head(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("HEAD", body).build();
	
		return client.newCall(request).execute();
	}
	
	/**
	 * Delete.
	 *
	 * @param body the body
	 * @return the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Response delete(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("DELETE", body).build();
	
		return client.newCall(request).execute();
	}
	
	/**
	 * Patch.
	 *
	 * @param body the body
	 * @return the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Response patch(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("PATCH", body).build();
	
		return client.newCall(request).execute();
	}
}
