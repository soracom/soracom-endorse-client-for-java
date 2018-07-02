package io.soracom.endorse.common;

public class HttpRequestException extends Exception {

	private static final long serialVersionUID = -2463620231343106785L;
	private HttpResponse response;

	public HttpRequestException(HttpResponse response, String message) {
		super(message);
		this.response = response;
	}

	public HttpResponse getResponse() {
		return response;
	}
}
