package com.bbby.aem.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Daily Publish Report On Approved DAM Assets",
    description = "Report on Approved DAM to identify all assets are in publish state "
)
public @interface DailyPublishedAssetsTaskConfiguration {

    @AttributeDefinition(name = "Enabled", description = "Scheduler only runs if this is checked", type = AttributeType.BOOLEAN)
    boolean enabled() default true;
    
    @AttributeDefinition(name = "Concurrent", description = "Schedule task concurrently", type = AttributeType.BOOLEAN)
    boolean scheduler_concurrent() default false;

    @AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: run week day 1:30 AM.", type = AttributeType.STRING)
    String scheduler_expression() default "5 30 1 ? * MON-FRI";
    
    @AttributeDefinition(name = "Days to keep", description = "Number of days Unpublished Assets should be shown", type = AttributeType.INTEGER)
    int daysTooKeep() default 30;
    
}
