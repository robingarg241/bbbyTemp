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
 * Vendor Portal History Component
 *
 * @author karthik.koduru
 */
@Component(value = "Vendor Portal History",
    group = ComponentSlingModel.COMPONENT_GROUP_CONTENT,
    disableTargeting = true,
    helpPath = "/editor.html/content/bbby-components/vendor-portal-history.html",
    tabs = {@Tab(title = "Vendor Portal History")})
@Model(adaptables = {Resource.class, SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class VendorPortalHistory extends ComponentSlingModel {

    @DialogField(fieldLabel = "BatchDetails Path", name = "./batchDetailsPath", ranking = 1)
    @PathField(rootPath = "/content/")
    @Inject
    @ValueMapValue
    private String batchDetailsPath;

    @DialogField(fieldLabel = "Redirect Path", name = "./redirectPath", ranking = 2)
    @PathField(rootPath = "/content/")
    @Inject
    @ValueMapValue
    private String redirectPath;

    public String getRedirectPath() {
        return redirectPath;
    }

    public String getBatchDetailsPath() {
        return batchDetailsPath;
    }

}
