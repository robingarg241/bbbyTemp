package com.bbby.aem.core.workflow.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
    name = "BBBY Asset Hasher Configuration",
    description = "Configuration parameters for Asset Hashin based on Asset Metadata."
)
public @interface BBBYAssetHasherConfiguration {

    @AttributeDefinition(name = "Timeout", description = "Time to wait between asset processing to avoid race conditions during asset relationship updates.")
    int timeout() default 500;

    
}