package com.unblu.tools.hrr.core.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.unblu.tools.hrr.core.RequestRecord;

import io.reactivex.subjects.Subject;

public class RecordHandler extends AbstractHandler {

	private Subject<RequestRecord> records$;

	public RecordHandler(Subject<RequestRecord> records$) {
		this.records$ = records$;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Record record = convertToRecord(baseRequest);
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("{ \"status\": \"ok\"}");
		response.flushBuffer();
		baseRequest.setHandled(true);
		records$.onNext(record);
	}

	private Record convertToRecord(Request request) throws IOException {
		Record record = new Record();
		record.setMethod(request.getMethod());
		record.setRequestURI(request.getRequestURI());

		Map<String, String[]> parameterMap = request.getParameterMap();
		Map<String, List<String>> parameters = parameterMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> Arrays.asList(e.getValue())));
		record.setParameters(parameters);

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			record.addHeader(headerName, request.getHeader(headerName));
		}

		String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		record.setBody(body);

		return record;
	}
}