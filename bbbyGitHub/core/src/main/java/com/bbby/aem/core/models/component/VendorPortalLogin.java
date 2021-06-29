package com.bbby.aem.core.models.component;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Vendor Portal Login Component
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/vendor-portal-login")
@Exporter(name = "jackson", extensions = "json")
public class VendorPortalLogin extends ComponentSlingModel {

	@Inject
    @ValueMapValue
    private String rootPath;
	
	@Inject
    @ValueMapValue
    private String redirectPath;

	@Inject
    @ValueMapValue
    private String backgroundImage;
	
	public String getRootPath() {
        return rootPath;
    }
	
    public String getRedirectPath() {
        return redirectPath;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }
}
