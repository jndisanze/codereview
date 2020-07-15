package au.com.sportsbet.proxy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import au.com.sportsbet.proxy.model.AsyncRequestResponder;
import au.com.sportsbet.proxy.model.HttpPublishResponse;
import au.com.sportsbet.proxy.publish.PublishDelegator;

@Controller
public class RequestHandler {
    private static final Logger LOGGER = Logger.getLogger(RequestHandler.class);

    @Autowired
    private PublishDelegator publishDelegator;

    public RequestHandler() {

    }

    @RequestMapping(value = "/GetStatus", method = RequestMethod.GET)
    public ResponseEntity<String> getStatus(
            @RequestParam(value = "workers", defaultValue = "ALL", required = false) final String workerName) {
        // if ("ALL".equalsIgnoreCase(workerName)) {
        // //
        // }
        ResponseEntity<String> entity = new ResponseEntity<String>("DUMMY", HttpStatus.OK);
        return entity;
    }

    @RequestMapping(value = "/Republish", method = RequestMethod.POST, consumes = { "multipart/mixed",
            "multipart/form-data" })
    public ResponseEntity<String> republish(
            @RequestParam(value = "replaySpeed", defaultValue = "nodelay", required = false) final String delay,
            @RequestPart("file") final MultipartFile file) {

        ResponseEntity<String> entity = new ResponseEntity<String>(HttpStatus.NOT_IMPLEMENTED);
        return entity;

    }

    @RequestMapping("/**")
    public ResponseEntity<String> process(final HttpServletRequest request, final HttpServletResponse httpResponse,
            @RequestHeader final HttpHeaders httpHeaders, @RequestBody(required = false) final String body) {

        String urlValue = request.getRequestURI()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        LOGGER.info("Request received on : (" + request.getMethod() + ") " + urlValue);

        AsyncRequestResponder requestResponder = publishDelegator.publishRequest(urlValue, request, httpHeaders, body,
                false);
        if (requestResponder != null) {
            
            try {
                requestResponder.await();
                HttpPublishResponse response = requestResponder.getResponse();
                HttpHeaders newHeaders = new HttpHeaders();

                HttpHeaders reponseHeaders = response.getHttpHeaders();
                String contentType = reponseHeaders.getFirst("Content-Type");
                if (contentType == null) {
                    if (response.getBody() != null) {
                        LOGGER.info("Should not have body but get here:");
                    }
                    else
                    {
                    	    LOGGER.info("No body returned");
                    }
                    ResponseEntity<String> entity = new ResponseEntity<String>(newHeaders, response.getStatus());
                    
                    return entity;
                } else if (contentType.contains("json")) {
                    newHeaders.setContentType(MediaType.APPLICATION_JSON);
                } else if (contentType.contains("xml")) {
                    newHeaders.setContentType(MediaType.TEXT_XML);
                } else {
                    newHeaders.setContentType(MediaType.TEXT_PLAIN);
                }
                ResponseEntity<String> entity = new ResponseEntity<String>(response.getBody(), newHeaders,
                        response.getStatus());
                return entity;
            } catch (InterruptedException e) {

                LOGGER.error("Request interrupted", e);
                ResponseEntity<String> entity = new ResponseEntity<String>(HttpStatus.SERVICE_UNAVAILABLE);
                return entity;

            }

        } else {

            ResponseEntity<String> entity = new ResponseEntity<String>(request.toString(), HttpStatus.OK);
            return entity;
        }
    }

}
