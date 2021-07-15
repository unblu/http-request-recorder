package com.unblu.tools.hrr.core;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unblu.tools.hrr.core.internal.RecordHandler;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.PublishSubject;

public class JettyServer {
	private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);

	private Server server;
	private JettyConfig config;
	private PublishSubject<RequestRecord> onRecords$;
	private ConnectableObservable<RequestRecord> records$;
	private Disposable recordingSubscription;

	public JettyServer() {
		this(JettyConfig.defaultConfig());
	}

	public JettyServer(JettyConfig config) {
		this.config = config;
		this.onRecords$ = PublishSubject.create();
		this.records$ = onRecords$.replay(1);
	}

	public void start() throws Exception {
		int port;
		if (config.getPort().isPresent()) {
			port = config.getPort().get().intValue();
		} else {
			port = 0;
		}
		if (recordingSubscription != null) {
			recordingSubscription.dispose();
		}
		recordingSubscription = records$.connect();
		server = new Server(port);
		server.setHandler(new RecordHandler(onRecords$));
		server.start();
		LOG.info("Server running at: " + getURI().toString());
	}

	public void stop() throws Exception {
		if (recordingSubscription != null) {
			recordingSubscription.dispose();
			recordingSubscription = null;
		}
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
		return onRecords$.hide();
	}

	public Observable<RequestRecord> getRequest() {
		return records$.hide();
	}
}