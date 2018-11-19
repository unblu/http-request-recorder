package com.unblu.tools.hrr.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unblu.tools.hrr.core.JettyConfig;
import com.unblu.tools.hrr.core.JettyServer;
import com.unblu.tools.hrr.core.RequestRecord;
import com.unblu.tools.hrr.core.RequestRecordConverter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "Run the recorder server", mixinStandardHelpOptions = true)
public class RunServer implements Callable<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(RunServer.class);

	@Option(names = { "-p", "--port" }, description = "Server port, a dynamic port is used if not set")
	private Integer port = null;

	@Option(names = { "-l", "--log" }, description = "Log the request in the command")
	private boolean log = false;

	@Option(names = { "-o", "--output" }, description = "Output folder where JSON files corresponding to the requests are stored")
	private Path output = null;

	@Option(names = { "-t", "--tree" }, description = "Store the ouput JSON files in a file tree that corresponds to the request")
	private boolean tree = false;

	@Override
	public Void call() {
		try {
			JettyConfig config = new JettyConfig(port);
			JettyServer jettyServer = new JettyServer(config);
			if (log) {
				jettyServer.onRequest().map(RequestRecordConverter::asLog).subscribe(LOG::info);
			}
			if (output != null) {
				jettyServer.onRequest().subscribe(this::storeRequestAsFile);
			}
			jettyServer.start();
		} catch (Exception e) {
			LOG.error("Error while running server. ", e);
		}
		return null;
	}

	private void storeRequestAsFile(RequestRecord record) {
		String content = RequestRecordConverter.asJson(record);
		long time = System.currentTimeMillis();
		Path file;
		String requestURI = record.getRequestURI();
		if (requestURI.startsWith("/")) {
			requestURI = requestURI.substring(1);
		}
		if (tree) {
			file = output.resolve(requestURI).resolve(time + ".json");
		} else {
			String requestPart = "";
			if (!requestURI.isEmpty()) {
				requestPart = "-" + requestURI.replace('/', '_');
				if (requestPart.length() > 40) {
					requestPart = requestPart.substring(0, 40);
				}
			}
			file = output.resolve(time + requestPart + ".json");
		}
		try {
			Files.createDirectories(file.getParent());
			Files.write(file, content.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			LOG.error("Could not write file: " + file);
		}

	}
}
