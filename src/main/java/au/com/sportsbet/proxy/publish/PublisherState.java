package au.com.sportsbet.proxy.publish;

public class PublisherState {
    private final String publisherName;
    private final int publishedCount;
    private final int errorCount;

    public PublisherState(final RequestPublisher publisher) {
        super();
        this.publisherName = publisher.getPublisherName();
        this.errorCount = publisher.getErrorCount();
        this.publishedCount = publisher.getProcessedCount();

    }

    public String getPublisherName() {
        return publisherName;
    }

    public int getPublishedCount() {
        return publishedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

}
