package au.com.sportsbet.proxy.publish;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PublisherConfigurator<T extends RequestPublisher> {
    protected final Logger LOGGER = Logger.getLogger(PublisherConfigurator.class);

    public List<T> publishers;

    @Autowired
    public PublisherRegister register;

    public PublisherConfigurator() {
        super();
    }

    public List<T> getPublishers() {
        return publishers;
    }

    public void setPublishers(final List<T> publishers) {
        this.publishers = publishers;
    }

    @PostConstruct
    public void init() {
        if (publishers != null) {

            for (T pub : publishers) {
                LOGGER.info("starting publisher :" + pub.getPublisherName());
                try {

                    register.addRequestPublisher(pub);
                } catch (PublisherSetupException e) {
                    throw new BeanInitializationException("Failed to configure publishers. Please check config for :"
                            + pub.getPublisherName() + " . See cause", e);
                }
                pub.startPublisher();
            }
        }
    }
}
