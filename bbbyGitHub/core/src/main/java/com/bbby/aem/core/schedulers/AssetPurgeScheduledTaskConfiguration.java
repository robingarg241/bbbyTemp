package com.bbby.aem.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "BBBY Asset Purge Task",
    description = "BBBY Asset Purge Task"
)
public @interface AssetPurgeScheduledTaskConfiguration {

    @AttributeDefinition(name = "Enabled", description = "Scheduler only runs if this is checked", type = AttributeType.BOOLEAN)
    boolean enabled() default true;
    
    @AttributeDefinition(name = "Concurrent", description = "Schedule task concurrently", type = AttributeType.BOOLEAN)
    boolean scheduler_concurrent() default false;

    @AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: run every Friday at 11pm.", type = AttributeType.STRING)
    String scheduler_expression() default "0 0 23 ? * FRI";
    
    @AttributeDefinition(name = "Days to keep (rejected)", description = "Number of days rejected assets should be kept", type = AttributeType.INTEGER)
    int daysTooKeepRejected() default 180;
    
    @AttributeDefinition(name = "Days to keep (duplicate)", description = "Number of days duplicate assets should be kept", type = AttributeType.INTEGER)
    int daysTooKeepDuplicate() default 180;
    
    @AttributeDefinition(name = "Max count", description = "Total count in a paticular root path for assets to get deleted from it should be less than max count.", type = AttributeType.INTEGER)
	int maxCount() default 5000;
}
