package com.bbby.aem.core.workflow.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Project Cleanup Configuration",
    description = "Rate limits the number assets that can be moved at once during the project cleanupworkflow."
)
public @interface BBBYCleanupConfiguration {

    @AttributeDefinition(name = "Batch Size", description = "Batch size for asset processing.")
    int batchSize() default 5;
    
    @AttributeDefinition(name = "Timeout", description = "Time to wait between asset processing to avoid race conditions during asset relationship updates.")
    int timeout() default 500;
    
    @AttributeDefinition(name = "Max Retries to Validate Imageset", description = "Maximum retry for validate the reference of Imageset.")
    int maxRetryValidateImageset() default 100;
    
}