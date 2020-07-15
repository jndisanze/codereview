package au.com.sportsbet.proxy.publish.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;

import au.com.sportsbet.proxy.model.ActionedRequestDetails;
import au.com.sportsbet.proxy.model.ActionedRequestDetails.RequestState;
import au.com.sportsbet.proxy.model.HttpPublishResponse;
import au.com.sportsbet.proxy.model.InvokedRequestDetails;

/**
 * Used for future- used for waiting for response back from downstream HTTP
 * server
 *
 * @author att.jasonr
 *
 */
public class HttpRepublisherWorker implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(HttpRepublisherWorker.class);

    private final HttpRepublisher publisher;
    private final ActionedRequestDetails requestDetails;
    private final CloseableHttpClient httpClient;
    private final ResponseHandler<HttpPublishResponse> responseHandler;

    public HttpRepublisherWorker(final HttpRepublisher publisher, final ActionedRequestDetails actionedRequest) {
        this.publisher = publisher;
        this.requestDetails = actionedRequest;
       
        this.responseHandler = new HttpRepublishResponseHandler();
        RequestConfig config = RequestConfig.custom()
                  .setConnectTimeout(publisher.getConnectTimeout())
                  .setSocketTimeout(publisher.getRequestTimeout()).build();
        this.httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    public void run() {

        HttpPublishResponse response = null;
        try {

            InvokedRequestDetails srcRequest = requestDetails.getRequest();

            HttpRequestBase requestObject = generateRequestBase();
            URI destinationURI = new URI(
                    publisher.getDestinationHostPort() + requestDetails.getRequest().getUrlValue());

            requestObject.setURI(destinationURI);
            appendHeaders(requestObject, srcRequest.getHttpHeaders());

            do {

                try {
                    LOGGER.info("Sending request on " + publisher.getPublisherName() + " to " + requestObject.getURI());

                    requestDetails.setAction(RequestState.SENDING);
                    requestDetails.setRequestEndTime(publisher.getRequestTimeout());
                    response = httpClient.execute(requestObject, responseHandler);

                    requestDetails.setSuccess(response);
                    LOGGER.info("Response received on " + publisher.getPublisherName() + " Duration:"
                            + requestDetails.getCallDuration() + ") \n Reponse:" + response);
                    break;

                } catch (IOException ioe) {
                    LOGGER.error("Issue calling endpoint on " + publisher.getPublisherName(), ioe);
                    if (requestDetails.updateRetryCount() >= publisher.getRetryPause().length) {
                        requestDetails.setException(ioe);
                    }
                }
                if (!requestDetails.getAction().equals(RequestState.FAILED)) {
                    Thread.currentThread().sleep(publisher.getRetryPause()[requestDetails.getRetryCount() - 1]);
                }
            } while (!requestDetails.getAction().equals(RequestState.FAILED));

        } catch (URISyntaxException
                | HttpRepublishException e) {
            requestDetails.setException(e);
            LOGGER.error("Can't call endpoint, invalid request on " + publisher.getPublisherName(), e);
        } catch (UnsupportedEncodingException e) {
            requestDetails.setException(e);
            LOGGER.error("Issue calling endpoint on " + publisher.getPublisherName(), e);
        } catch (InterruptedException e) {
            requestDetails.setException(e);
            LOGGER.error("Issue calling endpoint - interruped on " + publisher.getPublisherName(), e);

        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOGGER.error("Issue cloising HTTP Client on " + publisher.getPublisherName(), e);
            }
        }

    }

    private HttpRequestBase generateRequestBase() throws HttpRepublishException, UnsupportedEncodingException {
        HttpRequestBase requestObject = null;
        InvokedRequestDetails srcRequest = requestDetails.getRequest();
        String requestMethod = srcRequest.getMethod();

        if (srcRequest.getBody() != null) {
            if ("POST".equals(requestMethod)) {
                requestObject = new HttpPost();
            } else if ("PATCH".equals(requestMethod)) {
                requestObject = new HttpPatch();
            } else if ("PUT".equals(requestMethod)) {
                requestObject = new HttpPut();
            }
            if (requestObject == null) {
                throw new HttpRepublishException("Unsupported method " + requestMethod);
            }

            HttpEntity entity = new StringEntity(srcRequest.getBody());
            ((HttpEntityEnclosingRequestBase) requestObject).setEntity(entity);

        } else {
            if ("GET".equals(requestMethod)) {
                requestObject = new HttpGet();
            }
            if (requestObject == null) {
                throw new HttpRepublishException("Unsupported method " + requestMethod);
            }
        }
        
        return requestObject;
    }

    private void appendHeaders(final HttpRequestBase request, final HttpHeaders httpHeaders) {
        for (String header : httpHeaders.keySet()) {
            // strip content-length as framework needs to set it's own
            if (!header.equalsIgnoreCase("Content-Length")) {
                for (String value : httpHeaders.get(header)) {

                    request.addHeader(header, value);

                }
            }
        }
    }
}
