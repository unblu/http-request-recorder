package com.unblu.tools.hrr.core;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unblu.tools.hrr.core.internal.RecordHandler;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class JettyServer {
	private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);

	private Server server;
	private JettyConfig config;
	private BehaviorSubject<RequestRecord> records;

	public JettyServer() {
		this(JettyConfig.defaultConfig());
	}

	public JettyServer(JettyConfig config) {
		this.config = config;
		this.records = BehaviorSubject.create();
	}

	public void start() throws Exception {
		int port;
		if (config.getPort().isPresent()) {
			port = config.getPort().get().intValue();
		} else {
			port = 0;
		}
		server = new Server(port);
		server.setHandler(new RecordHandler(records));
		server.start();
		LOG.info("Server running at: " + getURI().toString());
	}

	public void stop() throws Exception {
		if (server != null) {
			server.stop();
			server = null;
			LOG.info("Server stoped");
		}
	}

	public URI getURI() {
		return server.getURI();
	}

	public Observable<RequestRecord> onRequest() {
		return records;
	}
}