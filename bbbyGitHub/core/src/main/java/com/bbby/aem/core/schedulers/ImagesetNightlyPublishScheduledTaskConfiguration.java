package com.bbby.aem.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "BBBY Imageset Nightly Publish Task",
    description = "BBBY Imageset Nightly Publish Task"
)
public @interface ImagesetNightlyPublishScheduledTaskConfiguration {

    @AttributeDefinition(name = "Enabled", description = "Scheduler only runs if this is checked", type = AttributeType.BOOLEAN)
    boolean enabled() default false;

    @AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: run every day at midnight - 12 am.", type = AttributeType.STRING)
    String scheduler_expression() default "0 0 0 * * ?";
    
    @AttributeDefinition(name = "Days to run", description = "Number of days for which imagesets has to be taken", type = AttributeType.INTEGER)
    int daysToRun() default 2;
    
    @AttributeDefinition(name = "Number of Assets", description = "Number of assets expected to move per minute.Please add a postive number greater than 0", type = AttributeType.INTEGER)
    int numberOfAssets() default 70;
    
    @AttributeDefinition(name = "User Email", description = "Email of the user on which daily reports of imagesets has to be sent.", type = AttributeType.STRING)
    String userEmail() default "robin.garg@idc.bedbath.com";
}
