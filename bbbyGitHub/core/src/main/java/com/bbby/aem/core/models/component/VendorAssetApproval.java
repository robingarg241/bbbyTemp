package com.bbby.aem.core.models.component;

import com.citytechinc.cq.component.annotations.Component;
import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.annotations.Tab;
import com.citytechinc.cq.component.annotations.widgets.PathField;
import com.citytechinc.cq.component.annotations.widgets.TextField;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Vendor Asset Approval Component
 */
@Component(value = "Vendor Asset Approval",
    group = ComponentSlingModel.COMPONENT_GROUP_CONTENT,
    disableTargeting = true,
    helpPath = "/editor.html/content/bbby-components/vendor-asset-approval.html",
    tabs = {@Tab(title = "Vendor Asset Approval")})
@Model(adaptables = {Resource.class, SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class VendorAssetApproval extends ComponentSlingModel {



    @DialogField(fieldLabel = "Asset Location", name = "./assetLocation", ranking = 1)
    @PathField(rootPath = "")
    @Inject @ValueMapValue
    private String assetLocation;

    public String getAssetLocation() {return assetLocation;}

}
