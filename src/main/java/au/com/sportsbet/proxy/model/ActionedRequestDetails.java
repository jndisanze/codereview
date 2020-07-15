package au.com.sportsbet.proxy.model;

import org.springframework.http.HttpStatus;

public class ActionedRequestDetails {

    public enum RequestState {
        SENDING, RETRYING, FAILED, SUCCESS, IGNORED
    };

    private final InvokedRequestDetails request;
    private HttpStatus httpStatus;
    private Throwable exception;
    private RequestState action;
    private final long requestStartTime;
    private long requestEndTime = 0L;
    private int retryCount = 0;
    private boolean isUseAsResponse;

    public ActionedRequestDetails(final InvokedRequestDetails request, final boolean isUseAsResponse) {
        super();
        this.request = request;
        this.action = RequestState.SENDING;
        this.httpStatus = null;
        this.exception = null;
        this.retryCount = 0;
        this.isUseAsResponse = isUseAsResponse;
        this.requestStartTime = System.currentTimeMillis();
    }

    public void setHttpStatus(final HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public void setException(final Throwable exception) {
        this.exception = exception;
    }

    public void setAction(final RequestState action) {
        this.action = action;
    }

    public void setRequestEndTime(final long requestEndTime) {
        this.requestEndTime = requestEndTime;
    }

    public void setRetryCount(final int retryCount) {
        this.retryCount = retryCount;
    }

    public int updateRetryCount() {
        return this.retryCount++;
    }

    public InvokedRequestDetails getRequest() {
        return request;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Throwable getException() {
        return exception;
    }

    public RequestState getAction() {
        return action;
    }

    public long getRequestStartTime() {
        return requestStartTime;
    }

    public long getRequestEndTime() {
        return requestEndTime;
    }

    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Used to set this request to "IGNORED" if this is being proxied back
     */
    public void setIgnored() {
        this.setAction(RequestState.IGNORED);
        this.requestEndTime = System.currentTimeMillis();
        if (isUseAsResponse) {
            this.getRequest().setIgnored();
        }
    }

    /**
     * Used to pass through exception back to client if this request is being used
     * to proxy back
     */
    public void setException(final Exception e) {
        this.setAction(RequestState.FAILED);
        this.requestEndTime = System.currentTimeMillis();
        if (isUseAsResponse) {
            request.setException(e);
        }
    }

    public void setSuccess(final HttpPublishResponse response) {
        this.setAction(RequestState.SUCCESS);
        this.requestEndTime = System.currentTimeMillis();

        if (isUseAsResponse) {
            request.setResponse(response);
        }
    }

    /**
     * Used to pass through exception back to client if this request is being used
     * to proxy back
     */
    public void setSuccess() {
        this.setAction(RequestState.SUCCESS);
        this.requestEndTime = System.currentTimeMillis();
        if (isUseAsResponse) {
            // treat as ignored (shouldn't get here!)
            this.getRequest().setIgnored();
        }
    }

    public long getCallDuration() {
        return requestEndTime - requestStartTime;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Actioned request: State -" + action.toString());
        buff.append("/n");
        buff.append("Underlying request:\n");
        buff.append(getRequest());

        return buff.toString();
    }

}
