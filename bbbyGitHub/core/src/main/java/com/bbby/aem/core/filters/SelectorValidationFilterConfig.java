package com.bbby.aem.core.filters;

import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Selector Validation Filter",
    description = "To enable the filter, include an empty OSGi XML config file as part of the build."
)
public @interface SelectorValidationFilterConfig {
}
