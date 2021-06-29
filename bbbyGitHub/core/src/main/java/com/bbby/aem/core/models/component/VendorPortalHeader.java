package com.bbby.aem.core.models.component;

import com.citytechinc.cq.component.annotations.Component;
import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.annotations.Tab;
import com.citytechinc.cq.component.annotations.widgets.CheckBox;
import com.citytechinc.cq.component.annotations.widgets.PathField;
import com.citytechinc.cq.component.annotations.widgets.TextField;
import com.day.cq.commons.Externalizer;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Vendor Portal Header Component
 *
 * @author karthik.koduru
 */
@Component(value = "Vendor Portal Header",
    group = ComponentSlingModel.COMPONENT_GROUP_CONTENT,
    disableTargeting = true,
    helpPath = "/editor.html/content/bbby-components/vendor-portal-header.html",
    tabs = {@Tab(title = "Vendor Portal Header")})
@Model(adaptables = {Resource.class, SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class VendorPortalHeader extends ComponentSlingModel {

	
    @DialogField(fieldLabel = "Home Path", name = "./homePath", ranking = 1)
    @PathField(rootPath = "/content/")
    @Inject
    @ValueMapValue
    private String homePath;

    @DialogField(fieldLabel = "Upload History", name = "./uploadHistory", ranking = 2)
    @PathField(rootPath = "/content/")
    @Inject
    @ValueMapValue
    private String uploadHistory;

    @DialogField(fieldLabel = "Help", name = "./help", ranking = 3)
    @PathField(rootPath = "/content/")
    @Inject
    @ValueMapValue
    private String help;

    @DialogField(fieldLabel = "Reference Documentation", name = "./refDoc", ranking = 3)
    @PathField(rootPath = "/content/")
    @Inject
    @ValueMapValue
    private String refDoc;

    @DialogField(fieldLabel = "Logo Path", name = "./logoPath", ranking = 4)
    @PathField(rootPath = "/content/dam")
    @Inject
    @ValueMapValue
    private String logoPath;

    @Inject
    @OSGiService
    private Externalizer externalizer;
    
    public String getHelpUrl() {
        return help == null ? "#" : help+".html";
    }

    public String getLogoPath() {
        return logoPath;
    }

    public String getHomeUrl() {
        return homePath == null ? "#" : homePath+".html";
    }

    public String getUploadHistoryUrl() {
        return uploadHistory == null ? "#" : uploadHistory+".html";
    }

    public String getRefDocUrl() {
        return refDoc == null ? "#" : refDoc+".html";
    }
    
    public boolean isHome() {
    	return isCurrentPage(homePath);
    }
    
    public boolean isUploadHistory() {
    	return isCurrentPage(uploadHistory);
    }
    
    public boolean isHelp() {
    	return isCurrentPage(help);
    }
    
    public boolean isRefDoc() {
    	return isCurrentPage(refDoc);
    }
    
    private boolean isCurrentPage(String url) {
    	String currentUrl = getCurrentPage().getPath();
    	return url != null && currentUrl.equals(url);
    }
}
