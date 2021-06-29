package com.bbby.aem.core.api.impl;

import com.bbby.aem.core.api.PDMAPICommand;
import com.bbby.aem.core.api.PDMClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HTTP Client service for calling PDM API
 */
@Component(service = PDMClient.class)
@Designate(ocd = PDMAPIClientConfiguration.class)
public class PDMClientImpl implements PDMClient {

    private static final Logger log = LoggerFactory.getLogger(PDMClientImpl.class);
    private static final Gson gson = new GsonBuilder().create();

    private int socketTimeout;
    private int connectionTimeout;


    private String pdmEndpointBaseUrl;

    @Activate
    protected void activate(PDMAPIClientConfiguration config) {
        this.pdmEndpointBaseUrl = config.submitAssetEndpointBaseUrl();
        socketTimeout = NumberUtils.toInt(config.endpointSocketTimeout(), 10 * 1000);
        connectionTimeout = NumberUtils.toInt(config.endpointConnectionTimeout(), 5 * 1000);

        log.debug("Activating PDM API client service: {}", this);
    }

    @Override
    public int execute(PDMAPICommand command) {

        String responseString = null;

        HttpUriRequest request = createRequest(command);

        try (CloseableHttpClient httpClient = newHttpClient(this.connectionTimeout, this.socketTimeout)) {
            try (CloseableHttpResponse responseObj = httpClient.execute(request)) {

                if (responseObj != null && responseObj.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                    responseString = EntityUtils.toString(responseObj.getEntity());
                    log.info("POST Response: " + responseString);
                    return HttpStatus.SC_OK;
                } else {
                    log.error("Bad response {}: {}", command.getURL(), responseObj);
                    return (responseObj != null) ? responseObj.getStatusLine().getStatusCode() : HttpStatus.SC_INTERNAL_SERVER_ERROR;
                }
            }
        } catch (Exception e) {
            log.error("Error during " + command.getURL(), e);
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
    }

    @Override
    public String getTargetUrl(String cmdUrl) {
        return (new StringBuilder().append(DEFAULT_SCHEMA)
            .append(this.pdmEndpointBaseUrl)
            .append(cmdUrl).toString());
    }

    private HttpUriRequest createRequest(PDMAPICommand command) {
        HttpUriRequest request = null;
        if (StringUtils.equals(command.getMethod(), PDMAPICommand.METHOD_GET)) {

        } else if (StringUtils.equals(command.getMethod(), PDMAPICommand.METHOD_POST)) {
            request = new HttpPost(getTargetUrl(command.getURL()));
            ((HttpPost) request).setEntity(command.getEntity());
        }

        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");

        for (final Header h : command.getHeaders()) {
            request.addHeader(h.getName(), h.getValue());
        }


        return request;
    }

    private static CloseableHttpClient newHttpClient(int connectionTimeout, int socketTimeout) {
        RequestConfig clientConfig = RequestConfig
            .custom()
            .setConnectTimeout(connectionTimeout)
            .setConnectionRequestTimeout(connectionTimeout)
            .setSocketTimeout(socketTimeout)
            .build();

        return HttpClientBuilder.create().setDefaultRequestConfig(clientConfig).build();
    }
}
