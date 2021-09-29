package com.bbby.aem.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Fast Track Imageset Move And Publish Task",
    description = "Fast Track Imageset Move And Publish Task"
)
public @interface FastTrackImagesetMoveAndPublishTaskConfiguration {

    @AttributeDefinition(name = "Enabled", description = "Scheduler only runs if this is checked", type = AttributeType.BOOLEAN)
    boolean enabled() default false;

    @AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: run every day early morning at - 4 am.", type = AttributeType.STRING)
    String scheduler_expression() default "0 0 4 * * ?";
    
}
