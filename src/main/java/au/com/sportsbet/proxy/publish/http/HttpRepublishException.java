package au.com.sportsbet.proxy.publish.http;

public class HttpRepublishException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 5812478204238092171L;

    public HttpRepublishException() {
        super();
    }

    public HttpRepublishException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public HttpRepublishException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public HttpRepublishException(final String message) {
        super(message);
    }

    public HttpRepublishException(final Throwable cause) {
        super(cause);
    }

}
