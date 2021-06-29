package com.bbby.aem.core.api.impl;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.api.AssetDropsLimit;
import com.bbby.aem.core.util.ServiceUtils;

@Component(service = AssetDropsLimit.class)
@Designate(ocd = AssetDropsLimitConfiguration.class)
public class AssetDropsLimitImpl implements AssetDropsLimit {

	private static final Logger log = LoggerFactory.getLogger(AssetDropsLimitImpl.class);

	private boolean enableConfig;
	private int restrictedNumberOfImages;
	
    /**
     * The resolver factory.
     */
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    String excelPath = "/content/dam/bedbath/vendor-portal/BBB_Metadata_Template_Final_v1.0.xlsx";

	@Activate
	protected void activate(AssetDropsLimitConfiguration config) {
		this.enableConfig = config.enableConfig();
		
		if(this.enableConfig){
			this.restrictedNumberOfImages = config.restrictedNumberOfImages();
		}else{
			this.restrictedNumberOfImages = 400;
		}

		log.debug("Activating Asset Drops Limit service: {}", this);
	}

	@Override
	public int getAssetDropsLimit() {
		return restrictedNumberOfImages;
	}

	@Override
	public boolean enableConfig() {
		return enableConfig;
	}

	@Override
	public String getExcelVersion() {
		return ServiceUtils.getExcelVersion(excelPath, resolverFactory);
	}
}
