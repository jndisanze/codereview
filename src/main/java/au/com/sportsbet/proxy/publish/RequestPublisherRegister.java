package au.com.sportsbet.proxy.publish;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import au.com.sportsbet.proxy.model.AsyncRequestResponder;
import au.com.sportsbet.proxy.model.InvokedRequestDetails;

@Component
public class RequestPublisherRegister implements PublisherRegister, PublishDelegator {
    private static final Logger LOGGER = Logger.getLogger(RequestPublisherRegister.class);

    private final List<RequestPublisher> republishers;

    private boolean hasResponsePublisher = false;

    public RequestPublisherRegister() {
        republishers = new ArrayList<RequestPublisher>();
    }

    public List<RequestPublisher> getRepublishers() {
        return republishers;
    }

    @Override
    public AsyncRequestResponder publishRequest(final String urlValue, final HttpServletRequest request, final HttpHeaders httpHeaders,
            final String body, final boolean isReplayed) {

        InvokedRequestDetails requestDetails = new InvokedRequestDetails(urlValue, request, httpHeaders, body,
                isReplayed);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Request details:\n" + requestDetails);
        }

        LOGGER.info("Publishing message to " + republishers.size() + " downstream publishers");
        republishers.stream().forEach(item -> item.publish(requestDetails));

        if (hasResponsePublisher) {
            requestDetails.setLatch(new CountDownLatch(1));
            return (AsyncRequestResponder) requestDetails;
        } else {
            return null;
        }
    }

    @Override
    public void addRequestPublisher(RequestPublisher publisher) throws PublisherSetupException {

        if (publisher.isUseAsReponse()) {
            if (hasResponsePublisher) {
                throw new PublisherSetupException(
                        "Cannot have more than one response publisher- please check configuration for \""
                                + publisher.getPublisherName() + "\"");
            } else {
                hasResponsePublisher = true;
            }
        }
        republishers.add(publisher);
    }

}
