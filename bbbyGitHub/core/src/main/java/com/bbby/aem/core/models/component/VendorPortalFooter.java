package com.bbby.aem.core.models.component;

import com.citytechinc.cq.component.annotations.Component;
import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.annotations.Tab;
import com.citytechinc.cq.component.annotations.widgets.PathField;
import com.citytechinc.cq.component.annotations.widgets.TextField;
import com.bbby.aem.core.util.ServiceUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Vendor Portal Footer Component
 */
@Component(value = "Vendor Portal Footer",
    group = ComponentSlingModel.COMPONENT_GROUP_CONTENT,
    disableTargeting = true,
    helpPath = "/editor.html/content/bbby-components/vendor-portal-footer.html",
    tabs = {@Tab(title = "Vendor Portal Footer")})
@Model(adaptables = {Resource.class, SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class VendorPortalFooter extends ComponentSlingModel {

    @DialogField(fieldLabel = "Home Path", name = "./homePath", ranking = 1)
    @PathField(rootPath = "/content/")
    @Inject
    @ValueMapValue
    private String homePath;


    @DialogField(fieldLabel = "Logo Path", name = "./logoPath", ranking = 2)
    @PathField(rootPath = "/content/dam")
    @Inject
    @ValueMapValue
    private String logoPath;

    @DialogField(fieldLabel = "File Format", name = "./fileFormat", ranking = 3)
    @PathField(rootPath = "/content/")
    @Inject
    @ValueMapValue
    private String fileFormat;

    public String getHomePath() {
        return homePath == null ? "#" : homePath+".html";
    }

    public String getFileFormat() {
        return fileFormat == null ? "#" : fileFormat+".html";
    }
    public String getLogoPath() {
        return logoPath;
    }

    @Override
    public void postConstruct() {

        fileFormat = ServiceUtils.appendLinkExtension(getResourceResolver(), fileFormat);
        logoPath = ServiceUtils.appendLinkExtension(getResourceResolver(), logoPath);
    }


}
