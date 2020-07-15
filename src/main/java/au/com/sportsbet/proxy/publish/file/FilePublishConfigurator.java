package au.com.sportsbet.proxy.publish.file;

import java.util.List;

import au.com.sportsbet.proxy.publish.PublisherConfigurator;
import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableAutoConfiguration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "filePublishers")
public class FilePublishConfigurator extends PublisherConfigurator<FilePublish> {

    private static final Logger LOGGER = Logger.getLogger(FilePublishConfigurator.class);

    

    @Override
    public List<FilePublish> getPublishers() {
        return super.getPublishers();
    }

    @Override
    public void setPublishers(final List<FilePublish> publishers) {
        super.setPublishers(publishers);
    }

    

}
