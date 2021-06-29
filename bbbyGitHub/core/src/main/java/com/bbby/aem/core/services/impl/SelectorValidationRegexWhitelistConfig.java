package com.bbby.aem.core.services.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Selector Validation Whitelist Rules",
    description = "Whitelist of regular expression exceptions for the Sling Selector Validation logic."
)
public @interface SelectorValidationRegexWhitelistConfig {

    @AttributeDefinition(name = "Rules", description = "Mapping of resource type to selector whitelist regex rule. Example: cq:ClientLibraryFolder:::ACSHASH.*")
    String[] rules() default {
        "sling/servlet/default:::thumb\\.(140\\.100|319\\.319|48\\.48)",
        "cq:Dialog:::(infinity|overlay\\.infinity|[0-10])",
        "cq:Page:::scaffolding"
    };

}
