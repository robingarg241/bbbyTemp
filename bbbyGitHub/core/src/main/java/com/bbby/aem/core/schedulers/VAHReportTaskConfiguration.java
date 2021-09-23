package com.bbby.aem.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Daily Report On VAH DAM Assets",
    description = "Report On VAH Assets to identify No UPC, Asset Type and Shot Type Issues."
)
public @interface VAHReportTaskConfiguration {

    @AttributeDefinition(name = "Enabled", description = "Scheduler only runs if this is checked", type = AttributeType.BOOLEAN)
    boolean enabled() default true;
    
    @AttributeDefinition(name = "Concurrent", description = "Schedule task concurrently", type = AttributeType.BOOLEAN)
    boolean scheduler_concurrent() default false;

    @AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: run week day 1 AM.", type = AttributeType.STRING)
    String scheduler_expression() default "5 1 1 ? * MON-FRI";
    
}
