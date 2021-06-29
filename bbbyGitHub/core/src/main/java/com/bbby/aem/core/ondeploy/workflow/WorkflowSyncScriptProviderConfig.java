package com.bbby.aem.core.ondeploy.workflow;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Workflow Sync Script Provider",
    description = "On deploy sync script provider for workflows. This should be manually configured on all author " +
        "instances."
)
public @interface WorkflowSyncScriptProviderConfig {

    @AttributeDefinition(name = "Username", description = "Username for user that has workflow model edit permissions.")
    String username() default "admin";

    @AttributeDefinition(name = "Password", description = "Password for user that has workflow model edit permissions.")
    String password() default "admin";
}
