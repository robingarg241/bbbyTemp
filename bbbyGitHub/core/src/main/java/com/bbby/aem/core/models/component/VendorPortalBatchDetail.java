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
 * Vendor Portal Batch Detail Component
 *
 * @author karthik.koduru
 */
@Component(value = "Vendor Portal Batch Detail",
    group = ComponentSlingModel.COMPONENT_GROUP_CONTENT,
    disableTargeting = true,
    helpPath = "/editor.html/content/bbby-components/vendor-portal-batch-detail.html",
    tabs = {@Tab(title = "Vendor Portal Batch Detail")})
@Model(adaptables = {Resource.class, SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class VendorPortalBatchDetail extends ComponentSlingModel {

    @DialogField(fieldLabel = "Redirect Path", name = "./redirectPath", ranking = 1)
    @PathField(rootPath = "/content/")
    @Inject
    @ValueMapValue
    private String redirectPath;

    public String getRedirectPath() {
        return redirectPath;
    }
}
