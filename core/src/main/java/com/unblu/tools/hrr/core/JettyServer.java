package com.unblu.tools.hrr.core;

import java.io.IOException;
import java.net.URI;
import java.util.function.BiConsumer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unblu.tools.hrr.core.internal.RecordHandler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

public class JettyServer {
	private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);

	private Server server;
	private JettyConfig config;
	private Subject<RequestRecord> onRecords$;
	private ConnectableObservable<RequestRecord> records$;
	private Disposable recordingSubscription;

	public JettyServer() {
		this(JettyConfig.defaultConfig());
	}

	public JettyServer(JettyConfig config) {
		this.config = config;
		this.onRecords$ = PublishSubject.<RequestRecord>create().toSerialized();
		this.records$ = onRecords$.replay(1);
	}

	/**
	 * Starts the server using a custom handler for the requests. When starting the
	 * server this way, the server will not emit on {@link #onRequest()} nor on
	 * {@link #getRequest()}.
	 * 
	 * @param requestHandler
	 *            the handler
	 * @throws Exception
	 */
	public void start(final BiConsumer<Request, HttpServletResponse> requestHandler) throws Exception {
		int port;
		if (config.getPort().isPresent()) {
			port = config.getPort().get().intValue();
		} else {
			port = 0;
		}
		server = new Server(port);
		server.setHandler(new AbstractHandler() {
			@Override
			public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
				requestHandler.accept(baseRequest, response);
			}
		});
		server.start();
		LOG.info("Server running at: {}", getURI().toString());
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

	/**
	 * Set a graceful stop time. The StatisticsHandler must be configured so that
	 * open connections can be tracked for a graceful shutdown.
	 * 
	 * @param stopTimeout
	 *            the graceful stop time
	 */
	public void setStopTimeout(long stopTimeout) {
		server.setStopTimeout(stopTimeout);
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