package com.unblu.tools.hrr.core;

import com.unblu.tools.hrr.core.internal.Record;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.function.BiConsumer;
import jakarta.servlet.http.HttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;
import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyServerCustomHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(JettyServerCustomHandlerTest.class);

    private static JettyServer server;

    @BeforeAll
    public static void before() throws Exception {
	server = new JettyServer();
    }

    @AfterAll
    public static void after() throws Exception {
	server.stop();
    }

    @Test
    public void testCustomHandler() throws Exception {

	LOG.info("Testing");
	BehaviorSubject<Record> requests$ = BehaviorSubject.create();
	TestObserver<Record> testObserver$ = requests$.test();

	BiConsumer<Request, HttpServletResponse> requestHandler = (req, resp) -> {
	    try {
		LOG.info("Request served: "+req.getMethod()+" "+req.getRequestURI());
		Record record = new Record();
		record.setMethod(req.getMethod());
		record.setRequestURI(req.getRequestURI());
		requests$.onNext(record);
		
		req.setHandled(true);
		resp.flushBuffer();
		resp.getOutputStream().close();
	    } catch (IOException ex) {
		throw new RuntimeException(ex);
	    }
	};

	server.start(requestHandler);
	performPinGetRequest().getInputStream().close();

	testObserver$.awaitCount(1).assertValueAt(0, rec -> {
	    assertThat(rec.getMethod()).isEqualTo("GET");
	    assertThat(rec.getRequestURI()).isEqualTo("/ping");
	    return true;
	});

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
