package com.unblu.tools.hrr.core.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.unblu.tools.hrr.core.RequestRecord;

public class Record implements RequestRecord {

	private String method;
	private String requestURI;
	private Map<String, List<String>> parameters;
	private Map<String, String> headers = new LinkedHashMap<>();
	private String body;

	@Override
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	@Override
	public Map<String, List<String>> getParameters() {
		return Collections.unmodifiableMap(parameters);
	}

	public void setParameters(Map<String, List<String>> parameters) {
		this.parameters = parameters;
	}

	@Override
	public Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}

	public void addHeader(String headerName, String headerValue) {
		headers.put(headerName, headerValue);
	}

	@Override
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}
