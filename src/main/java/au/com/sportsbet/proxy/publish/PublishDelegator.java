package au.com.sportsbet.proxy.publish;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;

import au.com.sportsbet.proxy.model.AsyncRequestResponder;

public interface PublishDelegator {
    AsyncRequestResponder publishRequest(String urlValue, HttpServletRequest request, HttpHeaders httpHeaders,
            String body, boolean isReplayed);

}
