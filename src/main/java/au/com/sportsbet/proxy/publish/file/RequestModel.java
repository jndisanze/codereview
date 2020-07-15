package au.com.sportsbet.proxy.publish.file;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.annotation.JsonIgnore;

import au.com.sportsbet.proxy.model.ActionedRequestDetails;

public class RequestModel {
    private final String path;
    private final String method;
    private final Map<String, String> headers;

    @JsonIgnore
    private final String body;

    public RequestModel(ActionedRequestDetails actionedRequest) {
        super();
        this.path = actionedRequest.getRequest().getUrlValue();
        this.method = actionedRequest.getRequest().getMethod();
        this.headers = new HashMap<String, String>();

        HttpHeaders srcHeaders = actionedRequest.getRequest().getHttpHeaders();
        for (String header : srcHeaders.keySet()) {
            // strip content-length as framework needs to set it's own
            
            for (String value : srcHeaders.get(header)) {
                headers.put(header, value);
            }

        }
        this.body = actionedRequest.getRequest().getBody();
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

}
