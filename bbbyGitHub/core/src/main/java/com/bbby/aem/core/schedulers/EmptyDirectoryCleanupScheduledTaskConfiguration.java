package com.bbby.aem.core.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "BBBY Empty Directory Cleanup Task", description = "BBBY Empty Directory Cleanup Task")
public @interface EmptyDirectoryCleanupScheduledTaskConfiguration {

	@AttributeDefinition(name = "Enabled Asset Holding", description = "Scheduler runs on Asset Holding Directory if this is checked", type = AttributeType.BOOLEAN)
	boolean enabledHolding() default true;

	@AttributeDefinition(name = "Enabled E-Comm", description = "Scheduler runs on E-Comm Directory if this is checked.", type = AttributeType.BOOLEAN)
	boolean enabledEComm() default true;

	@AttributeDefinition(name = "Days to Keep For Asset Holding", description = "Number of days Asset Holding empty folder should be kept.", type = AttributeType.INTEGER)
	int daysTooKeepHolding() default 1;

	@AttributeDefinition(name = "Days to Keep For E-Comm", description = "Number of days E-Comm empty folder should be kept.", type = AttributeType.INTEGER)
	int daysTooKeepEComm() default 1;
	
	@AttributeDefinition(name = "Max Number of Folders", description = "Max Number of empty folder should be deleted.", type = AttributeType.INTEGER)
	int maxNoOfFolders() default 500;

	@AttributeDefinition(name = "Concurrent", description = "Schedule task concurrently", type = AttributeType.BOOLEAN)
	boolean scheduler_concurrent() default false;

	@AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: Every day at midnight - 12am.", type = AttributeType.STRING)
	String scheduler_expression() default "0 0 0 * * ?";

}
