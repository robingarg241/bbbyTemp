package com.bbby.aem.core.api.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * @author Karan Kumar
 * 
 * This interface represents an OSGi configuration which can be found at - 
 * ./system/console/configMgr
 */

@ObjectClassDefinition(
    name = "Vendor Portal Asset Limit Config",
    description = "This configuration reads the values to Restrict Number of Asset Drops"
)
public @interface AssetDropsLimitConfiguration {
	
	/**
	 * This is a checkbox property which will indicate of the configuration is
	 * executed or not
	 * 
	 * @return {@link Boolean}
	 */
	@AttributeDefinition(
			name = "Enable config", 
			description = "This property indicates whether the configuration values will taken into account or not", 
			type = AttributeType.BOOLEAN)
	public boolean enableConfig();
    
	
	@AttributeDefinition(
			name = "Number of Images", 
			description = "Restricted Number of Images", 
			type = AttributeType.INTEGER)
	public int restrictedNumberOfImages();
}
