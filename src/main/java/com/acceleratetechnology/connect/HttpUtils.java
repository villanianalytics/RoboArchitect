package com.acceleratetechnology.connect;

import java.io.File;
import java.io.IOException;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

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
		return RequestBody.create(mediaType, body);
	}
	
	public RequestBody createBody(File file) {
		return new MultipartBuilder()
			      .type(MultipartBuilder.FORM)
			      .addPart(RequestBody.create(MediaType.parse("multipart/form-data"), file)).build();
	}
	
	public Response post(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("POST", body).build();
	
		return client.newCall(request).execute();
	}
	
	public Response post(RequestBody body) throws IOException {
		OkHttpClient client = new OkHttpClient();
		
		Request request = builder.method("GET").build();
	
		return client.newCall(request).execute();
	}
	
}
