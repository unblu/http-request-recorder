package com.unblu.tools.hrr.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

public class JettyServerTest {
	private static final Logger LOG = LoggerFactory.getLogger(JettyServerTest.class);

	private static JettyServer server;

	// tag::start[]
	@BeforeAll
	public static void before() throws Exception {
		server = new JettyServer();
		server.start();
	}
	// end::start[]

	// tag::stop[]
	@AfterAll
	public static void after() throws Exception {
		server.stop();
	}
	// end::stop[]

	@Test
	public void testGetRequest() throws Exception {
		// tag::example-getRequest[]
		//Access the request recorder:
		Observable<RequestRecord> observable$ = server.getRequest();

		//Perform a GET Request:
		HttpURLConnection con = performPinGetRequest();

		//Simple subscription: log the HTTP record as INFO log
		//Retrieves still the last record
		observable$.map(RequestRecordConverter::asLog).subscribe(LOG::info);

		//Perform assertions on the server response:
		int status = con.getResponseCode();
		assertThat(status).isEqualTo(200);
		String content = new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining("\n"));
		assertThat(content).isEqualTo("{ \"status\": \"ok\"}");

		//Perform assertions on the recorded server event:
		observable$.test().assertSubscribed().assertNoErrors().assertValueCount(1).assertValueAt(0, record -> {
			assertThat(record.getMethod()).isEqualTo("GET");
			assertThat(record.getRequestURI()).isEqualTo("/ping");
			return true;
		});
		// end::example-getRequest[]
	}

	@Test
	public void testOnRequest() throws Exception {
		// tag::example-onRequest[]
		//Access the request recorder:
		Observable<RequestRecord> observable$ = server.onRequest();

		//create test observer
		TestObserver<RequestRecord> testObserver = observable$.test();

		//Simple subscription: log the HTTP record as INFO log
		observable$.map(RequestRecordConverter::asLog).subscribe(LOG::info);

		//Perform a GET Request:
		HttpURLConnection con = performPinGetRequest();

		//Perform assertions on the server response:
		int status = con.getResponseCode();
		assertThat(status).isEqualTo(200);
		String content = new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining("\n"));
		assertThat(content).isEqualTo("{ \"status\": \"ok\"}");

		//Perform assertions on the recorded server event:
		testObserver.assertSubscribed().assertNoErrors().assertValueCount(1).assertValueAt(0, record -> {
			assertThat(record.getMethod()).isEqualTo("GET");
			assertThat(record.getRequestURI()).isEqualTo("/ping");
			return true;
		});
		// end::example-onRequest[]
	}

	private HttpURLConnection performPinGetRequest() throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(server.getURI().toString() + "ping");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json");
		con.setConnectTimeout(5000);
		con.setReadTimeout(5000);
		con.setInstanceFollowRedirects(false);
		return con;
	}
}
