package au.com.sportsbet.proxy.model;

import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class InvokedRequestDetails implements AsyncRequestResponder {
    private final String urlValue;

    private final HttpServletRequest request;
    private final String method;

    private final HttpHeaders httpHeaders;
    private final String body;
    private final long requestTimeStamp;

    @JsonIgnore
    private HttpPublishResponse response = null;

    @JsonIgnore
    private Exception issueException;

    @JsonIgnore
    private CountDownLatch latch = null;

    @JsonIgnore
    private final boolean isReplayed;

    public InvokedRequestDetails(final String urlValue, final HttpServletRequest request,
           final HttpHeaders httpHeaders, final String body, final boolean isReplayed) {
        super();
        this.urlValue = urlValue;
        this.request = request;
        this.httpHeaders = httpHeaders;
        this.body = body;
        this.requestTimeStamp = System.currentTimeMillis();
        this.isReplayed = isReplayed;
        this.method = request.getMethod();

    }

    public String getUrlValue() {
        return urlValue;
    }

    public boolean isReplayed() {
        return isReplayed;
    }

    public long getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public String getBody() {
        return body;
    }

    public String getMethod() {
        return method;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Call: (" + method + ") " + urlValue);

        buffer.append("\n");
        buffer.append("Headers:");
        for (String header : httpHeaders.keySet()) {
            buffer.append("  " + header + ":" + httpHeaders.get(header));
            buffer.append("\n");
        }
        if (body != null) {
            buffer.append("Body:");
            buffer.append("\n");
            buffer.append(body);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    @Override
    public HttpPublishResponse getResponse() {
        return response;
    }

    public void setLatch(final CountDownLatch latch) {
        this.latch = latch;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setResponse(final HttpPublishResponse response) {
        this.response = response;
        this.latch.countDown();
    }

    public void setException(final Exception e) {
        this.issueException = e;
        Header[] dummyHeaders = new Header[1];
        if (request.getContentType()!=null)
        {
        	    dummyHeaders[0] = new BasicHeader("Content-Type", request.getContentType());
        }
        else
        {
        	    dummyHeaders[0] = new BasicHeader("Content-Type", "text/plain");
        }
        this.response = new HttpPublishResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Issue calling downstream: " + e.getMessage(), dummyHeaders);
        this.latch.countDown();
    }

    /**
     * set response to a dummy body, and return but setting content Type as source
     */
    public void setIgnored() {
        Header[] dummyHeaders = new Header[1];
        dummyHeaders[0] = new BasicHeader("Content-Type", request.getHeader("Content-Type"));
        this.response = new HttpPublishResponse(HttpStatus.NO_CONTENT, null, dummyHeaders);
        this.latch.countDown();
    }

    @Override
    public void await() throws InterruptedException {
        this.latch.await();

    }

}
