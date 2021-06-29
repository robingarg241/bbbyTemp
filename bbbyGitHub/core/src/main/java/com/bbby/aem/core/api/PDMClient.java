package com.bbby.aem.core.api;


public interface PDMClient {

    String DEFAULT_SCHEMA = "https://";

    int execute(PDMAPICommand command);

    String getTargetUrl(String cmdUrl);
}
