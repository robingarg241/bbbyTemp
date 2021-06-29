package com.bbby.aem.core.workflow.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Call PDM Step Configuration",
    description = "Paths for different folders where call to pdm launcher is configured."
)
public @interface CallPDMServiceConfiguration {

    @AttributeDefinition(name = "Internal Folder Path", description = "Internal Folder Path")
    String internalFolderPath() default "/content/dam/bbby/asset_transitions_folder/internal";

    @AttributeDefinition(name = "Vendor Assets Holding Folder Path", description = "Vendor Assets Holding Folder Path")
    String vendorAssetsHoldingFolderPath() default "/content/dam/bbby/asset_transitions_folder/vendor/vendor_assets_holding";

    @AttributeDefinition(name = "E-Comm Folder Path", description = "E-Comm Folder Path")
    String ecommFolderPath() default "/content/dam/bbby/asset_transitions_folder/e-comm";

    @AttributeDefinition(name = "Approved DAM Folder Path", description = "Approved DAM Folder Path")
    String approvedDAMFolderPath() default "/content/dam/bbby/approved_dam";
    
    @AttributeDefinition(name = "Marketing Folder Path", description = "Marketing Folder Path")
    String marketingFolderPath() default "/content/dam/marketing";

}
