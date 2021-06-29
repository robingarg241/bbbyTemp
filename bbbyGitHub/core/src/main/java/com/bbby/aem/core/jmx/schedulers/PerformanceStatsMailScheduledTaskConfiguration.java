package com.bbby.aem.core.jmx.schedulers;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "BBBY Performance Stats Mail", description = "BBBY Performance Stats Mail")
public @interface PerformanceStatsMailScheduledTaskConfiguration {

	@AttributeDefinition(name = "Enabled", description = "Scheduler sends mail if this is checked", type = AttributeType.BOOLEAN)
	boolean enabled() default false;
	
	@AttributeDefinition(name = "To", description = "Add Recipient", type = AttributeType.STRING)
	String to();
	
	@AttributeDefinition(name = "CC", description = "Add CC", type = AttributeType.STRING)
	String cc();
	
	@AttributeDefinition(name = "BCC", description = "Add BCC", type = AttributeType.STRING)
	String bcc();
	
	@AttributeDefinition(name = "Subject", description = "Add Subject in the Mail", type = AttributeType.STRING)
	String subject();
	
	@AttributeDefinition(name = "Note", description = "Add Note in Mail body", type = AttributeType.STRING)
	String note();
	
	@AttributeDefinition(name = "Warning", description = "Add Warning in Mail body", type = AttributeType.STRING)
	String warning();
	
	@AttributeDefinition(name = "Signature", description = "Add Signature of the Mail", type = AttributeType.STRING)
	String signature();

	@AttributeDefinition(name = "Concurrent", description = "Schedule task concurrently", type = AttributeType.BOOLEAN)
	boolean scheduler_concurrent() default false;

	@AttributeDefinition(name = "Expression", description = "Cron-job expression. Default: At 04:00:00pm every day.", type = AttributeType.STRING)
	String scheduler_expression() default "0 0 16 * * ?"; //0 * * ? * * Every minute

}
