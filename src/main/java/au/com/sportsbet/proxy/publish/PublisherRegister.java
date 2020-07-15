package au.com.sportsbet.proxy.publish;

public interface PublisherRegister {
    void addRequestPublisher(RequestPublisher publisher) throws PublisherSetupException;

}
