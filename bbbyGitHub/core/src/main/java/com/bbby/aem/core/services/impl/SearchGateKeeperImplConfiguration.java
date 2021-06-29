package com.bbby.aem.core.services.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Search Gate Keeper",
    description = "Rate limits the number of concurrent searches that can be executed at once."
)
public @interface SearchGateKeeperImplConfiguration {

    @AttributeDefinition(name = "Max Searches", description = "The maximum number of concurrent search requests.")
    int maxSearches() default 5;
}
