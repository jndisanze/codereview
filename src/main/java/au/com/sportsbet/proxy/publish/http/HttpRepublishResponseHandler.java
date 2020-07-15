package au.com.sportsbet.proxy.publish.http;

import java.io.IOException;

import au.com.sportsbet.proxy.model.HttpPublishResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

public class HttpRepublishResponseHandler implements ResponseHandler<HttpPublishResponse> {
	private static final Logger LOGGER = Logger.getLogger(HttpRepublishResponseHandler.class);


	
	public HttpRepublishResponseHandler() {

	}

	@Override
	public HttpPublishResponse handleResponse(HttpResponse response) throws IOException {

		LOGGER.info("Response received..." );
		int status = response.getStatusLine().getStatusCode();

		HttpEntity entity = response.getEntity();
		HttpPublishResponse publishResponse = new HttpPublishResponse(HttpStatus.valueOf(status),
				(entity != null ? EntityUtils.toString(entity) : null), response.getAllHeaders());
		
		return publishResponse;

	}

}
