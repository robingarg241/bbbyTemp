package com.bbby.aem.core.workflow.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

@ObjectClassDefinition(
    name = "BBBY Asset Sorter Configuration",
    description = "Configuration parameters for Asset Sorting based on PDM Metadata."
)
public @interface BBBYAssetSorterConfiguration {

    @AttributeDefinition(name = "Asset Transition Folder Internal", description = "Location of the asset transitions folder")
    String assetTransitionsInternalFolder() default "/content/dam/bbby/asset_transitions_folder/internal";
    
    @AttributeDefinition(name = "Asset Transition Folder Vendor", description = "Location of the asset transitions folder")
    String assetTransitionsVendorFolder() default "/content/dam/bbby/asset_transitions_folder/vendor";
    
    @AttributeDefinition(name = "Unmatched Folder Location", description = "Location of the unmatched asset folder")
    String unmatchedFolder() default "/content/dam/bbby/asset_transitions_folder/vendor/unmatched_upc";
    
    @AttributeDefinition(name = "Unassigned Asset Folder Locaton", description = "Location of the unmatched asset folder")
    String unassignedFolder() default "/content/dam/bbby/asset_transitions_folder/vendor/not_assigned";
    
    @AttributeDefinition(name = "Non-Product (Collateral) Folder Locaton", description = "Location of collateral assets")
    String nonProductFolder() default "/content/dam/bbby/asset_transitions_folder/internal/non-product";
    
    @AttributeDefinition(name = "EComm Asset Folder Location", description = "Location of the ecomm asset folder")
    String ecommFolder() default "/content/dam/bbby/asset_transitions_folder/e-comm";

    @AttributeDefinition(name = "Project Cleanup Workflow Model", description = "Project Cleanup Workflow Model")
    String projCleanupModel() default "/var/workflow/models/project_cleanup";
    
    @AttributeDefinition(name = "Product Collateral Asset Model", description = "Product Collateral Asset Model")
    String bbbyProductCollateralAssetModel() default "/var/workflow/models/bbby-product-collateral-asset";

    @AttributeDefinition(name = "Timeout", description = "Time to wait between asset processing to avoid race conditions during asset relationship updates.")
    int timeout() default 500;

    
}
