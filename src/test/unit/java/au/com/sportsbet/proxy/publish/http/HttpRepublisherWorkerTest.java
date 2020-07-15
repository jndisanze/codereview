package au.com.sportsbet.proxy.publish.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpServletRequest;

import au.com.sportsbet.proxy.model.HttpPublishResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import au.com.sportsbet.proxy.model.ActionedRequestDetails;
import au.com.sportsbet.proxy.model.ActionedRequestDetails.RequestState;
import au.com.sportsbet.proxy.model.InvokedRequestDetails;

@RunWith(SpringRunner.class)
@PrepareForTest(HttpRepublisherWorker.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class HttpRepublisherWorkerTest {

	HttpRepublisherWorker worker;

	@Mock
	HttpServletRequest requestMock;

	@Mock
	CloseableHttpClient httpClientMock;

	@Mock
    HttpPublishResponse mockResponse;

	HttpRepublisher dummyPublisher;

	@Mock
	InvokedRequestDetails requestDetails;

	@Mock
	InvokedRequestDetails invalidRequestDetails;

	
	ActionedRequestDetails actionedRequest;

	
	ActionedRequestDetails actionedRequestWithResponse;

	@Before
	public void setup() {
		// build actionedRequest (

	}

	
	/**
	 * Test if request that is passed through is submitted downstream
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testSuccess() {
		generateValidRequestMock();
		worker = PowerMockito.spy(new HttpRepublisherWorker(dummyPublisher, actionedRequest));
		Whitebox.setInternalState(worker, CloseableHttpClient.class, httpClientMock);

		try {
			when(httpClientMock.execute(any(HttpRequestBase.class), any(ResponseHandler.class)))
					.thenReturn(mockResponse);
		} catch (IOException e) {
			fail("Should not reach here!");
		}
		worker.run();
		assertEquals(RequestState.SUCCESS, actionedRequest.getAction());
		assertNull(actionedRequest.getRequest().getResponse());
	}

	@Test
	public void testSuccessAndPublished() {
		generateValidRequestMock();
		worker = PowerMockito.spy(new HttpRepublisherWorker(dummyPublisher, actionedRequestWithResponse));
		Whitebox.setInternalState(worker, CloseableHttpClient.class, httpClientMock);
		when(requestMock.getMethod()).thenReturn("POST");
		try {
			when(httpClientMock.execute(any(HttpRequestBase.class), any(ResponseHandler.class)))
					.thenReturn(mockResponse);
		} catch (IOException e) {
			fail("Should not reach here!");
		}
		worker.run();
		assertEquals(RequestState.SUCCESS, actionedRequestWithResponse.getAction());
		assertEquals(mockResponse, actionedRequest.getRequest().getResponse());

	}

	/**
	 * Test if request that is passed through is submitted downstream
	 */
	@Test
	public void testRetryTheSuccess() {
		generateValidRequestMock();
		worker = PowerMockito.spy(new HttpRepublisherWorker(dummyPublisher, actionedRequest));
		Whitebox.setInternalState(worker, CloseableHttpClient.class, httpClientMock);
		try {
			when(httpClientMock.execute(any(HttpRequestBase.class), any(ResponseHandler.class)))
					.thenThrow(new IOException("Exception to retry on")).thenReturn(mockResponse);

		} catch (IOException e) {
			fail("Should not reach here!");
		}
		worker.run();
		// verify should attempt to calls
		try {
			verify(httpClientMock, times(2)).execute(any(HttpRequestBase.class), any(ResponseHandler.class));
			assertEquals(RequestState.SUCCESS, actionedRequest.getAction());
		} catch (ClientProtocolException e) {
			fail("Should not throw this exception");
		} catch (IOException e) {
			fail("Should not throw this exception");
		}
	}

	@Test
	public void testRetriesFails() {
		generateValidRequestMock();
		worker = PowerMockito.spy(new HttpRepublisherWorker(dummyPublisher, actionedRequest));
		Whitebox.setInternalState(worker, CloseableHttpClient.class, httpClientMock);
		try {
			when(httpClientMock.execute(any(HttpRequestBase.class), any(ResponseHandler.class)))
					.thenThrow(new IOException("Exception to retry on"))
					.thenThrow(new IOException("Exception to retry on"))
					.thenThrow(new IOException("Exception to retry on"));

		} catch (IOException e) {
			fail("Should not reach here!");
		}
		worker.run();
		// verify should attempt to calls
		try {
			verify(httpClientMock, times(3)).execute(any(HttpRequestBase.class), any(ResponseHandler.class));
			assertEquals(RequestState.FAILED, actionedRequest.getAction());

		} catch (ClientProtocolException e) {
			fail("Should not occur!");
			e.printStackTrace();
		} catch (IOException e) {
			fail("Should be captured in method");
		}
	}

	
	public void testFailsWithoutInvoking() {
		generateInvalidRequestMock();
		worker = new HttpRepublisherWorker(dummyPublisher, actionedRequest);
		

		worker.run();

		// verify should attempt to calls
		assertEquals(RequestState.FAILED, actionedRequest.getAction());

	}

	private void generateValidRequestMock() {
		dummyPublisher = new HttpRepublisher();
		dummyPublisher.setPublisherName("Test republisher");
		dummyPublisher.setWorkerCount(1);
		dummyPublisher.setDestinationHostPort("http://localhost:8080");
		dummyPublisher.setRetryPause(new long[] { 5, 5 }); // use 2 retries with 5 ms gap
		dummyPublisher.setUseAsReponse(false);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		String body = new String("Dummy body");

		when(requestMock.getMethod()).thenReturn("POST");
		requestDetails = new InvokedRequestDetails("/api", requestMock, httpHeaders, body, false);

		requestDetails.setLatch(new CountDownLatch(1));
		actionedRequest = new ActionedRequestDetails(requestDetails, false);
		actionedRequestWithResponse = new ActionedRequestDetails(requestDetails, true);
	}

	private void generateInvalidRequestMock() {
		dummyPublisher = new HttpRepublisher();
		dummyPublisher.setPublisherName("Test republisher");
		dummyPublisher.setWorkerCount(1);
		dummyPublisher.setDestinationHostPort("http://localhost:8080");
		dummyPublisher.setRetryPause(new long[] { 5, 5 }); // use 2 retries with 5 ms gap
		dummyPublisher.setUseAsReponse(false);
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		String body = new String("Dummy body");

		when(requestMock.getMethod()).thenReturn("UNKNOWNMETHOD");
		requestDetails = new InvokedRequestDetails("/api", requestMock, httpHeaders, body, false);

		requestDetails.setLatch(new CountDownLatch(1));
		actionedRequest = new ActionedRequestDetails(requestDetails, false);
		actionedRequestWithResponse = new ActionedRequestDetails(requestDetails, true);
	}

	@TestConfiguration
	static class Config {

	}
}
