package au.com.sportsbet.proxy.model;

import org.apache.http.Header;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class HttpPublishResponse {
    private final HttpStatus status;
    private final String body;
    private final long responseTime;
    private final HttpHeaders httpHeaders;

    public HttpPublishResponse(final HttpStatus statusCode, final String body, final Header[] responseHeaders) {
        super();
        this.status = statusCode;
        this.body = body;
        this.httpHeaders = new HttpHeaders();
        if (responseHeaders != null) {
            for (Header header : responseHeaders) {
                this.httpHeaders.set(header.getName(), header.getValue());
            }
        }
        responseTime = System.currentTimeMillis();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Response code: "+status.toString() +"\n");
        buff.append("Headers:\n");
        buff.append(httpHeaders);
        buff.append("\n");
        if (this.getBody() != null) {
            buff.append(this.getBody());
        }
        return buff.toString();
    }
}
