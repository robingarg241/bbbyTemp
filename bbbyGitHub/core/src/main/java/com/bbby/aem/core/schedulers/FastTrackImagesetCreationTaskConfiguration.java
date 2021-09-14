package com.bbby.aem.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "BBBY Fast Track Imageset Creation Task",
    description = "BBBY Fast Track Imageset Creation Task"
)
public @interface FastTrackImagesetCreationTaskConfiguration {

    @AttributeDefinition(name = "Enabled", description = "Scheduler only runs if this is checked", type = AttributeType.BOOLEAN)
    boolean enabled() default false;

    @AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: run every day at midnight - 12 am.", type = AttributeType.STRING)
    String scheduler_expression() default "0 0 0 * * ?";
    
}
