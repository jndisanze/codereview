package au.com.sportsbet.proxy.model;

public interface AsyncRequestResponder {
    void await() throws InterruptedException;

    HttpPublishResponse getResponse();
}
