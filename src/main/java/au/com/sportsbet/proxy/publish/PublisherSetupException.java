package au.com.sportsbet.proxy.publish;

public class PublisherSetupException extends Exception {

    public PublisherSetupException() {
        super();
    }

    public PublisherSetupException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PublisherSetupException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public PublisherSetupException(final String message) {
        super(message);
    }

    public PublisherSetupException(final Throwable cause) {
        super(cause);
    }

}
