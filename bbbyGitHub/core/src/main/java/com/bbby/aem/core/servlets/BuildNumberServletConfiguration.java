package com.bbby.aem.core.servlets;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "BBBY Build Number Servlet",
    description = "Build info that is loaded via OSGi configuration values populated at build time."
)
public @interface BuildNumberServletConfiguration {

    @AttributeDefinition(name = "Build Number")
    String buildNumber();

    @AttributeDefinition(name = "Build Time")
    String buildTime();
}
