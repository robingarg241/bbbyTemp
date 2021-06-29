package com.bbby.aem.core.jmx.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "BBBY Performance Stats", description = "BBBY Performance Stats")
public @interface PerformanceStatsScheduledTaskConfiguration {

	@AttributeDefinition(name = "Enabled", description = "Scheduler runs on Workflows if this is checked", type = AttributeType.BOOLEAN)
	boolean enabled() default true;

	@AttributeDefinition(name = "Concurrent", description = "Schedule task concurrently", type = AttributeType.BOOLEAN)
	boolean scheduler_concurrent() default false;

	@AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: Every 30 minutes.", type = AttributeType.STRING)
	String scheduler_expression() default "0 */30 * ? * *"; //0 * * ? * * Every minute

}
