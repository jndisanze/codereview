package au.com.sportsbet.proxy.publish;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import au.com.sportsbet.proxy.model.AsyncRequestResponder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
//want main bean tested to be put back to a known state at start of each test
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RequestPublisherRegisterTest {

	@Autowired
	RequestPublisherRegister requestPublisher;

	@Mock
	RequestPublisher testPublisher;

	@Mock
	private HttpServletRequest request;

	@Spy
	List<RequestPublisher> republishersSpy;

	HttpHeaders httpHeaders = new HttpHeaders();

	@Before
	public void setup() {
		requestPublisher.getRepublishers().clear();
		republishersSpy = requestPublisher.getRepublishers();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testRegistration() {
		try {
			republishersSpy = requestPublisher.getRepublishers();
			when(testPublisher.isUseAsReponse()).thenReturn(false);

			requestPublisher.addRequestPublisher(testPublisher);

			assertEquals(1, republishersSpy.size());

		} catch (PublisherSetupException e) {
			fail("Should not have an exception");
		}
	}

	@Test
	public void testRegistrationOneRepub() {
		try {
			when(testPublisher.isUseAsReponse()).thenReturn(true);
			requestPublisher.addRequestPublisher(testPublisher);

		} catch (PublisherSetupException e) {
			fail("Should not have an exception");
		}
	}

	@Test
	public void testRegistrationFailOnTwoUseAsResponse() {
		try {
			when(testPublisher.isUseAsReponse()).thenReturn(true).thenReturn(false).thenReturn(true);
			when(testPublisher.getPublisherName()).thenReturn("Dummy").thenReturn("Dummy").thenReturn("Dummy");
			requestPublisher.addRequestPublisher(testPublisher);
			requestPublisher.addRequestPublisher(testPublisher);
			requestPublisher.addRequestPublisher(testPublisher);

		} catch (PublisherSetupException e) {
			assertEquals(2, republishersSpy.size());

		}
	}

	@Test
	public void testPublishRequestWithNoResponseBack() {
		try {
			when(testPublisher.isUseAsReponse()).thenReturn(false);
			when(testPublisher.getPublisherName()).thenReturn("Dummy");

			requestPublisher.addRequestPublisher(testPublisher);
			AsyncRequestResponder response = requestPublisher.publishRequest("Test", request, httpHeaders, "testBody",
					false);
			assertNull(response);

		} catch (PublisherSetupException e) {
			fail("Should not have exception");

		}
	}

	@Test
	public void testPublishRequestWithResponseBack() {
		try {
			when(testPublisher.isUseAsReponse()).thenReturn(true);
			when(testPublisher.getPublisherName()).thenReturn("Dummy");

			requestPublisher.addRequestPublisher(testPublisher);
			AsyncRequestResponder response = requestPublisher.publishRequest("Test", request, httpHeaders, "testBody",
					false);
			assertNotNull(response);

		} catch (PublisherSetupException e) {
			fail("Should not have exception");

		}
	}

	@Test
	public void testPublishRequestWithExpectedResponseBack() {

	}

	@TestConfiguration
	static class Config {

		@Bean
		public RequestPublisherRegister genHandler() {
			return new RequestPublisherRegister();
		}

	}

}
