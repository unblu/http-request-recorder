package com.unblu.tools.hrr.core;

import java.io.IOException;
import java.net.URI;
import java.util.function.BiConsumer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.ee9.nested.AbstractHandler;
import org.eclipse.jetty.ee9.nested.ContextHandler;
import org.eclipse.jetty.ee9.nested.Handler;
import org.eclipse.jetty.ee9.nested.Request;
import org.eclipse.jetty.server.Server;
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
	 * {@link #getRequest()}. Every request is marked as handled once the handler
	 * returns.
	 * 
	 * @param requestHandler
	 *            the handler
	 * @throws Exception
	 */
	public void start(final BiConsumer<HttpServletRequest, HttpServletResponse> requestHandler) throws Exception {
		int port;
		if (config.getPort().isPresent()) {
			port = config.getPort().get().intValue();
		} else {
			port = 0;
		}
		server = createServer(port, new AbstractHandler() {
			@Override
			public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
				requestHandler.accept(request, response);
				baseRequest.setHandled(true);
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
		server = createServer(port, new RecordHandler(onRecords$));
		server.start();
		LOG.info("Server running at: " + getURI().toString());
	}

	private static Server createServer(int port, Handler handler) {
		Server server = new Server(port);
		ContextHandler context = new ContextHandler("/");
		context.setHandler(handler);
		server.setHandler(context);
		return server;
	}

	public void stop() throws Exception {
		stop(null);
	}

	/**
	 * Stops the server setting a specific graceful stop time. The StatisticsHandler
	 * must be configured so that open connections can be tracked for a graceful
	 * shutdown.
	 * 
	 * @param stopTimeout
	 *            the graceful stop time
	 */
	public void stop(Long stopTimeout) throws Exception {
		if (recordingSubscription != null) {
			recordingSubscription.dispose();
			recordingSubscription = null;
		}
		if (server != null) {
			if (stopTimeout != null) {
				server.setStopTimeout(stopTimeout);
			}
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