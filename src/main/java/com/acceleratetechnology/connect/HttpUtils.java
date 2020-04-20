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


public class HttpUtils {

	private Request.Builder builder;
	
	public HttpUtils(String url) {
		this.builder = new Request.Builder();

		builder.url(url);
	}
	
	public void addHeadersAuth(String username, String password) {
		String credential = Credentials.basic(username, password);
		builder.addHeader("Authorization", credential);
	}
	
	public void addHeadersAuth(String token) {
		builder.addHeader("Authorization", "Basic " + token);
	}
	
	public void addHeadersContent(String type) {
		builder.addHeader("Content-Type", type);
	}
	
	public RequestBody createBody(String type, String body) {
		MediaType mediaType = MediaType.parse(type);
		return RequestBody.create(body, mediaType);
	}
	
	public RequestBody createBody(File file) {
		MediaType mediaType = MediaType.parse("multipart/*");
		
		return new MultipartBody.Builder()
				        .setType(MultipartBody.FORM)
				        .addFormDataPart("", file.getName(), RequestBody.create(file, mediaType))
				        .build();
	}
	
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
	
	public Response get() throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.build();
	
		return client.newCall(request).execute();
	}
	
	public Response post(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("POST", body).build();
	
		return client.newCall(request).execute();
	}
	
	public Response put(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("PUT", body).build();
	
		return client.newCall(request).execute();
	}
	
	public Response head(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("HEAD", body).build();
	
		return client.newCall(request).execute();
	}
	
	public Response delete(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("DELETE", body).build();
	
		return client.newCall(request).execute();
	}
	
	public Response patch(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("PATCH", body).build();
	
		return client.newCall(request).execute();
	}
}
