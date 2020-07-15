package au.com.sportsbet.proxy.publish.http;

import java.util.regex.Pattern;

import au.com.sportsbet.proxy.publish.RequestPublisher;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Scope;

import au.com.sportsbet.proxy.model.ActionedRequestDetails;

@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "httpPublishers.publishers")
public class HttpRepublisher extends RequestPublisher {
	private static final Logger LOGGER = Logger.getLogger(HttpRepublisher.class);
	private static final int DEFAULT_REQUEST_TIMEOUT = 10000;
	private static final int DEFAULT_CONNECT_TIMEOUT = 2000;

	private String destinationHostPort;

	private boolean rewritePath = false;
	private String pathRewriteString = null;
	private String pathFilter = null;
	private Pattern pathPattern = null;

	private long[] retryPause = new long[0]; // if you want 3 reties one second apart = 1000,1000,1000 or 2 retries but
												// "throttled" then 1000,5000
	private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	private int requestTimeout = DEFAULT_REQUEST_TIMEOUT;

	public HttpRepublisher() {
		super();

	}

	@Override
	public void submitRequest(final ActionedRequestDetails actionedRequest) {
		LOGGER.info("Request being actioned on " + this.getPublisherName());

		if (pathPattern != null && !pathPattern.matcher(actionedRequest.getRequest().getUrlValue()).matches()) {
			actionedRequest.setIgnored();
		} else {
			HttpRepublisherWorker worker = new HttpRepublisherWorker(this, actionedRequest);

			super.getExecutor().submit(worker);

		}
	}

	public String getPathFilter() {
		return pathFilter;
	}

	public void setPathFilter(final String pathFilter) {
		this.pathFilter = pathFilter;
		this.pathPattern = Pattern.compile(pathFilter);
	}

	public long[] getRetryPause() {
		return retryPause;
	}

	public void setRetryPause(final long[] retryPause) {
		this.retryPause = retryPause;
	}

	public void setPathRewriteString(final String pathRewriteString) {
		this.rewritePath = true;
		this.pathRewriteString = pathRewriteString;
	}

	@Required
	public String getDestinationHostPort() {
		return destinationHostPort;
	}

	public void setDestinationHostPort(final String destinationHostPort) {
		this.destinationHostPort = destinationHostPort;
	}

	public boolean isRewritePath() {
		return rewritePath;
	}

	public void setRewritePath(final boolean rewritePath) {
		this.rewritePath = rewritePath;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(final int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public String getPathRewriteString() {
		return pathRewriteString;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

}
