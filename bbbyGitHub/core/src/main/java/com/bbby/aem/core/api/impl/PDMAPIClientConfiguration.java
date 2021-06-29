package com.bbby.aem.core.api.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "PDM API Client",
    description = "PDM API Client Configuration"
)
public @interface PDMAPIClientConfiguration {
    @AttributeDefinition(name = "Submit Asset Endpoint URL", description = "PDM API endpoint to submit asset")
    String submitAssetEndpointBaseUrl();

    @AttributeDefinition(name = "Socket timeout", description = "PDM API socket timeout")
    String endpointSocketTimeout();

    @AttributeDefinition(name = "Connection timeout", description = "PDM API connection timeout")
    String endpointConnectionTimeout();
}
