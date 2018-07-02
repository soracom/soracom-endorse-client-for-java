package io.soracom.endorse.common;

public class HttpRequestException extends Exception {

	private static final long serialVersionUID = -2463620231343106785L;
	private String requestUrl;
	private HttpResponse response;

	public HttpRequestException(String requestUrl,HttpResponse response, String message) {
		super(message);
		this.requestUrl = requestUrl;
		this.response = response;
	}

	public HttpResponse getResponse() {
		return response;
	}
	
	public String getRequestUrl() {
		return requestUrl;
	}
	
	@Override
	public String getMessage() {
		return toString();		
	}

	@Override
	public String toString() {
		StringBuilder error = new StringBuilder();
		String message = super.getMessage();
		if(message != null) {
			error.append(message+" ");
		}
		if(requestUrl != null) {
			error.append("requestUrl:" + requestUrl);
		}
		if(response != null) {
			error.append("responseStatus:" + response.getCode());
			if (response.getContents() != null) {
				error.append(" responseContents:" + response.getContents());
			} else {
				error.append(" responseContents:null");
			}
		}
		return error.toString();
	}
}
