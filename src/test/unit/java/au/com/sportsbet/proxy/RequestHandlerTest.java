package au.com.sportsbet.proxy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.com.sportsbet.proxy.model.AsyncRequestResponder;
import au.com.sportsbet.proxy.model.HttpPublishResponse;
import au.com.sportsbet.proxy.model.InvokedRequestDetails;
import au.com.sportsbet.proxy.publish.PublishDelegator;

@RunWith(SpringRunner.class)

@ContextConfiguration(classes = { RequestHandlerTest.Config.class })
public class RequestHandlerTest {

	@Autowired
	RequestHandler requestHandler;

	@MockBean
	 PublishDelegator delegator;

	@Captor
	ArgumentCaptor<InvokedRequestDetails> requestDetailsCaptor;

	@Mock
	 AsyncRequestResponder responseMock;

	@Mock
	 HttpPublishResponse httpResponseMock;

	 HttpHeaders httpHeaders = new HttpHeaders();

	

	private String cannedResponse = new String("{ response: \"ok\" }");

	@Before
	public void setup() {
		
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
	}
	
	@Test
	public void testSuccessfulCallWithoutAwaitingResponse() throws Exception {
		MockMvc myMvcMock = MockMvcBuilders.standaloneSetup(requestHandler).build();
		when(delegator.publishRequest(anyString(), any(HttpServletRequest.class), any(HttpHeaders.class), anyString(),
				eq(false))).thenReturn(null);
		myMvcMock.perform(post("/test").content("this is the body content")).andExpect(status().isOk());
		

	}

	@Test
	public void testSuccessfulCallWithWriteBack() throws Exception {
		MockMvc myMvcMock = MockMvcBuilders.standaloneSetup(requestHandler).build();

		when(delegator.publishRequest(anyString(), any(HttpServletRequest.class), any(HttpHeaders.class), anyString(),
				eq(false))).thenReturn(responseMock);

		when(responseMock.getResponse()).thenReturn(httpResponseMock);
		when(httpResponseMock.getBody()).thenReturn(cannedResponse);
		when(httpResponseMock.getStatus()).thenReturn(HttpStatus.OK);
		when(httpResponseMock.getHttpHeaders()).thenReturn(httpHeaders);

		myMvcMock.perform(post("/test").content("this is the body content")).andExpect(status().isOk())
				.andExpect(content().string(cannedResponse));

		verify(responseMock, times(1)).await();

	}

	@Test
	public void testFailedDownstream() throws Exception {
		MockMvc myMvcMock = MockMvcBuilders.standaloneSetup(requestHandler).build();

		when(delegator.publishRequest(anyString(), any(HttpServletRequest.class), any(HttpHeaders.class), anyString(),
				eq(false))).thenReturn(responseMock);

		when(responseMock.getResponse()).thenReturn(httpResponseMock);
		
		when(httpResponseMock.getBody()).thenReturn("Issue calling downstream: Connection Refused");
		when(httpResponseMock.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
		when(httpResponseMock.getHttpHeaders()).thenReturn(httpHeaders);

		myMvcMock.perform(post("/notFoundURI").content("some content here")).andExpect(status().is5xxServerError())
				.andExpect(content().string("Issue calling downstream: Connection Refused"));

		verify(responseMock, times(1)).await();

	}

	
	static class Config {

		@Bean
		public RequestHandler genHandler() {
			return new RequestHandler();
		}

		
	}

}
