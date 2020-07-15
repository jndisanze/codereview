package au.com.sportsbet.proxy.publish;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import au.com.sportsbet.proxy.model.ActionedRequestDetails;
import au.com.sportsbet.proxy.model.InvokedRequestDetails;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class RequestPublisher implements Runnable {
    private final Logger LOGGER = Logger.getLogger(RequestPublisher.class);

    private final int DEFAULT_MAX_SIZE = 50;

    public String publisherName;
    private BlockingQueue<InvokedRequestDetails> incomingRequests;
    private AtomicInteger errorCount = new AtomicInteger(0);
    private AtomicInteger processedCount = new AtomicInteger(0);
    private int workerCount = 1;
    private ExecutorService executor;
    private boolean useAsReponse;
    private int maxQueueSize = DEFAULT_MAX_SIZE;

    public RequestPublisher() {
        super();
        publisherName = "Unknown" + this.getClass().getSimpleName();
        this.incomingRequests = new LinkedBlockingQueue<InvokedRequestDetails>(maxQueueSize);

    }

 

    /**
     * starts up executor pool, registers back this list of known publishers
     */
    @PostConstruct
    public void startPublisher() {
        // worker pool
        executor = Executors.newFixedThreadPool(workerCount);
        for (int i = 0; i < workerCount; i++) {
            new Thread(this, "Distributor_" + publisherName + "_" + i).start();
        }

    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void run() {
        LOGGER.info("Distributor thread started for :" + publisherName);
        for (;;) {
            try {
                InvokedRequestDetails requestToWorkOn = incomingRequests.take();
                if (requestToWorkOn != null) {
                    ActionedRequestDetails actionedRequest = genActionedRequest(requestToWorkOn);
                    submitRequest(actionedRequest);
                }
            } catch (InterruptedException ie) {
                LOGGER.info("Thread interrupted");
            }
        }
    }

    private ActionedRequestDetails genActionedRequest(InvokedRequestDetails requestToWorkOn) {
        ActionedRequestDetails actionedRequest = new ActionedRequestDetails(requestToWorkOn, this.isUseAsReponse());
        return actionedRequest;
    }

    public abstract void submitRequest(final ActionedRequestDetails actionedRequest);

    public void setPublisherName(final String publisherName) {
        this.publisherName = publisherName;
    }

    /**
     * adds request to queue - pushing off oldest if no room in queue - keep synchronised
     * so if there are multiple waiting we remove multiple. Do not want to block on put
     * thus causing main thread to halt for request handler. Enforce serial addition.
     * @param details
     */
    public void publish(final InvokedRequestDetails details) {

        try {
            synchronized (new Object()) {
                // if no capcity- make room keep (even if consumer starts again being ok again)
                if (incomingRequests.remainingCapacity() == 0) {
                    // do a poll not take (non-block)
                    InvokedRequestDetails requestToIgnore = incomingRequests.poll();
                    if (requestToIgnore != null) {
                        genActionedRequest(requestToIgnore)
                                .setException(new PublisherException("Queue full, oldest entry purged"));
                    }
                }
                incomingRequests.put(details);
            }

        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Issue adding request to publisher " + this.publisherName + " - " + e.getMessage());
            }
        }
    }

    public PublisherState getPublisherState() {
        return new PublisherState(this);
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void addError() {
        errorCount.incrementAndGet();
    }

    public void addProcessed() {
        processedCount.incrementAndGet();
    }

    public int getErrorCount() {
        return errorCount.get();
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(final int workerCount) {
        this.workerCount = workerCount;
    }

    public boolean isUseAsReponse() {
        return useAsReponse;
    }

    public void setUseAsReponse(final boolean useAsReponse) {
        this.useAsReponse = useAsReponse;
    }
    public int getMaxQueueSize() {
         return maxQueueSize;
     }

     public void setMaxQueueSize(int maxQueueSize) {
         this.maxQueueSize = maxQueueSize;
     }
}
