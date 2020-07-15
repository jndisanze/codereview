package au.com.sportsbet.proxy.publish.http;

import java.util.List;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import au.com.sportsbet.proxy.publish.PublisherConfigurator;

@Component
@EnableAutoConfiguration
@EnableConfigurationProperties
// @PropertySource(value = "file:${proxyConfig}")
@ConfigurationProperties(prefix = "httpPublishers")
public class HttpRepublisherConfigurator extends PublisherConfigurator<HttpRepublisher> {

    

    @Override
    public List<HttpRepublisher> getPublishers() {
        return super.getPublishers();
    }

    @Override
    public void setPublishers(final List<HttpRepublisher> publishers) {
        super.setPublishers(publishers);
    }

}
