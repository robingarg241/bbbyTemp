package com.bbby.aem.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "BBBY Upload History Cleanup Task",
    description = "BBBY vendor Portal Upload History Cleanup Task"
)
public @interface UploadHistoryCleanupScheduledTaskConfiguration {

    @AttributeDefinition(name = "Enabled", description = "Scheduler only runs if this is checked", type = AttributeType.BOOLEAN)
    boolean enabled() default true;
    
    @AttributeDefinition(name = "Concurrent", description = "Schedule task concurrently", type = AttributeType.BOOLEAN)
    boolean scheduler_concurrent() default false;

    @AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: run every minute.", type = AttributeType.STRING)
    String scheduler_expression() default "0 0 13 * * ?";
    
    @AttributeDefinition(name = "Days to keep", description = "Number of days history should be kept", type = AttributeType.INTEGER)
    int daysTooKeep() default 30;
}
