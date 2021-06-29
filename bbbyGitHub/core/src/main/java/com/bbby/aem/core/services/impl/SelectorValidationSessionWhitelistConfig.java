package com.bbby.aem.core.services.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Selector Validation User Session Rules",
    description = "Allows all requests through for certain users and/or groups. Needed for admin screens which use " +
        "selectors which would normally not be allowed for end users of a site.")
public @interface SelectorValidationSessionWhitelistConfig {

    @AttributeDefinition(name = "Users", description = "User for which all requests are allowed.")
    String[] users() default {};

    @AttributeDefinition(name = "User Groups", description = "User groups for which all requests are allowed.")
    String[] groups() default {};

}
