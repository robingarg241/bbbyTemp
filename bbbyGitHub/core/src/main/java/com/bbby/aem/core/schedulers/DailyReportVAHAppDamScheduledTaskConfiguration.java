package com.bbby.aem.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Daily Report On VAH/Approved DAM Assets",
    description = "Report On VAH Assets to identify No UPC, Asset Type and Shot Type Issues AND on Approved DAM to identify all assets are in publish state "
)
public @interface DailyReportVAHAppDamScheduledTaskConfiguration {

    @AttributeDefinition(name = "Enabled", description = "Scheduler only runs if this is checked", type = AttributeType.BOOLEAN)
    boolean enabled() default true;
    
    @AttributeDefinition(name = "Concurrent", description = "Schedule task concurrently", type = AttributeType.BOOLEAN)
    boolean scheduler_concurrent() default false;

    @AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: run week day 4 AM.", type = AttributeType.STRING)
    String scheduler_expression() default "5 1 4 ? * MON-FRI";;
    
}
