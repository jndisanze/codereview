package au.com.sportsbet.proxy.publish.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

import au.com.sportsbet.proxy.publish.RequestPublisher;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Scope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.sportsbet.proxy.model.ActionedRequestDetails;

/**
 * reads requests
 *
 * @author att.jasonr
 *
 */
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "filePublishers.publishers")
public class FilePublish extends RequestPublisher {
    private static final Logger LOGGER = Logger.getLogger(FilePublish.class);

    private String path;
    private String prefix;
    private String suffix;
    private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private File basePath;

    public FilePublish() {
        super();
    }

    @PostConstruct
    public void init() throws IOException {
        basePath = new File(path);
        if (!basePath.exists()) {
            if (!basePath.mkdirs()) {
                throw new IOException("Unable to create file publisher directory");

            }
        }
    }

    @Override
    public void submitRequest(final ActionedRequestDetails actionedRequest) {
        File outputFile = new File(basePath,
                (prefix != null ? prefix : "") + timestampFormat.format(new Date()) + (suffix != null ? suffix : ""));
        File outputFileInfo = new File(basePath, (prefix != null ? prefix : "") + timestampFormat.format(new Date())
                + (suffix != null ? suffix : "") + ".info");
        try {
            RequestModel model = new RequestModel(actionedRequest);

            new ObjectMapper().writeValue(outputFileInfo, model);
            if (model.getBody() != null) {
                OutputStream dumpBody = new FileOutputStream(outputFile);

                // most messages small :/ can change if need be
                dumpBody.write(model.getBody().getBytes());
                dumpBody.flush();
                dumpBody.close();
                actionedRequest.setSuccess();
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to process model to " + outputFile, e);
            actionedRequest.setException(e);
        } catch (IOException e) {
            LOGGER.error("Failed to write to " + outputFile, e);
            actionedRequest.setException(e);
        }

    }

    public String getPath() {
        return path.toString();
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void setUseAsReponse(final boolean useAsReponse) {
        if (useAsReponse) {
            throw new BeanInitializationException(
                    "Cannot use this type of publisher as a response generator. Please check config for : "
                            + this.getPublisherName());
        }
    }

}
