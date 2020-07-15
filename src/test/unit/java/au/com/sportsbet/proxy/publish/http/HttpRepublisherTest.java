package au.com.sportsbet.proxy.publish.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import au.com.sportsbet.proxy.model.ActionedRequestDetails;
import au.com.sportsbet.proxy.model.InvokedRequestDetails;

@RunWith(SpringRunner.class)
@PrepareForTest(HttpRepublisher.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class HttpRepublisherTest {

	@Captor
	ArgumentCaptor<HttpRepublisherWorker> republisherWorkerCaptor;

	@Autowired
	HttpRepublisher republisher;

	@Mock
	ExecutorService executorMock;

	@Mock
	ActionedRequestDetails actionedRequest;

	@Mock
	private HttpServletRequest httpRequest;

	@Mock
	private HttpHeaders httpHeaders;

	/**
	 * Test if request that is passed through is submitted downstream
	 */
	@Test
	public void testSubmitRequest() {

		HttpRepublisher testerRepublisher = PowerMockito.spy(republisher);
		Whitebox.setInternalState(testerRepublisher, ExecutorService.class, executorMock);

		testerRepublisher.submitRequest(actionedRequest);
		verify(executorMock).submit(republisherWorkerCaptor.capture());

		HttpRepublisherWorker resultWorker = republisherWorkerCaptor.getValue();
		HttpRepublisher publisher = Whitebox.getInternalState(resultWorker, HttpRepublisher.class);
		ActionedRequestDetails requestDetails = Whitebox.getInternalState(resultWorker, ActionedRequestDetails.class);

		assertEquals(actionedRequest, requestDetails);
		assertEquals(publisher, testerRepublisher);

	}


	/**
	 * Test if request that is passed through is submitted downstream
	 */
	@Test
	public void testFilteredRequestIsExecuted() {

		HttpRepublisher testerRepublisher = PowerMockito.spy(republisher);
		testerRepublisher.setPathFilter("^/api.*");
		Whitebox.setInternalState(testerRepublisher, ExecutorService.class, executorMock);
		//force API to not match pattern
		InvokedRequestDetails dummyRequestDetails = new InvokedRequestDetails("/api", httpRequest, httpHeaders,
				"dummyBody", false);

		when(actionedRequest.getRequest()).thenReturn(dummyRequestDetails);
		testerRepublisher.submitRequest(actionedRequest);
		verify(executorMock).submit(republisherWorkerCaptor.capture());

		HttpRepublisherWorker resultWorker = republisherWorkerCaptor.getValue();
		HttpRepublisher publisher = Whitebox.getInternalState(resultWorker, HttpRepublisher.class);
		ActionedRequestDetails requestDetails = Whitebox.getInternalState(resultWorker, ActionedRequestDetails.class);

		assertEquals(actionedRequest, requestDetails);
		assertEquals(publisher, testerRepublisher);

	}
	/**
	 * Test if request that is passed through is submitted downstream
	 */
	@Test
	public void testFilteredRequest() {

		HttpRepublisher testerRepublisher = PowerMockito.spy(republisher);
		testerRepublisher.setPathFilter("^/api.*");
		Whitebox.setInternalState(testerRepublisher, ExecutorService.class, executorMock);
		//force API to not match pattern
		InvokedRequestDetails dummyRequestDetails = new InvokedRequestDetails("/notAPI", httpRequest, httpHeaders,
				"dummyBody", false);

		when(actionedRequest.getRequest()).thenReturn(dummyRequestDetails);
		testerRepublisher.submitRequest(actionedRequest);
		//it should never get onto executor
		verify(actionedRequest,times(1)).setIgnored();

	}

	@TestConfiguration
	static class Config {

		@Bean
		public HttpRepublisher getHttpReublisher() {
			return new HttpRepublisher();
		}

	}
}
