package com.bbby.aem.core.services.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "BBBY Asset Service Configuration",
    description = "Allows all requests through for certain users and/or groups. Needed for admin screens which use " +
        "selectors which would normally not be allowed for end users of a site.")
public @interface AssetServiceConfig {

    @AttributeDefinition(name = "Product Assset Types", description = "Mapping of product types to tags.")
    String[] assetTypes() default {};

}
