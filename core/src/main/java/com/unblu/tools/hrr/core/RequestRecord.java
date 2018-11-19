package com.unblu.tools.hrr.core;

import java.util.List;
import java.util.Map;

public interface RequestRecord {

	String getMethod();

	String getRequestURI();

	Map<String, List<String>> getParameters();

	Map<String, String> getHeaders();

	String getBody();
}
