package com.unblu.tools.hrr.core;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RequestRecordConverter {

	public static String asLog(RequestRecord record) {
		return "Request:" + System.getProperty("line.separator") + asText(record);
	}

	public static String asText(RequestRecord record) {
		String newline = System.getProperty("line.separator");

		StringBuilder sb = new StringBuilder();
		sb.append("[" + record.getMethod() + "] " + record.getRequestURI() + newline);
		Map<String, List<String>> parameters = record.getParameters();
		if (!parameters.isEmpty()) {
			sb.append("Parameters:" + newline);
			for (Entry<String, List<String>> e : parameters.entrySet()) {
				sb.append(" - " + e.getKey() + ": " + String.join(", ", e.getValue()) + newline);
			}
		}
		Map<String, String> headers = record.getHeaders();
		if (!headers.isEmpty()) {
			sb.append("Headers:" + newline);
			for (Entry<String, String> e : headers.entrySet()) {
				sb.append(" - " + e.getKey() + ": " + e.getValue() + newline);
			}
		}
		String body = record.getBody();
		if (!body.isEmpty()) {
			sb.append("Body:" + newline);
			sb.append(body + newline);
		}
		return sb.toString();
	}

	public static String asJson(RequestRecord record) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(record);
	}

}
