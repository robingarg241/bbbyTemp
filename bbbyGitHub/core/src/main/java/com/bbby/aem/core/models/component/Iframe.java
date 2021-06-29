package com.bbby.aem.core.models.component;

import com.bbby.aem.core.util.ServiceUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Iframe Component
 */

@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/iframe")
@Exporter(name = "jackson", extensions = "json")
public class Iframe extends ComponentSlingModel {

    private static final String SRCTYPE_SRC = "src";
    private static final String SRCTYPE_SRC_DOC = "srcdoc";

    @Inject
    @ValueMapValue
    private String name;

    @Inject
    @ValueMapValue
    private String src;

    @Inject
    @ValueMapValue
    private String srcdoc;

    @Inject
    @ValueMapValue
    private String srcType;

    @Inject
    @ValueMapValue
    private String width;

    @Inject
    @ValueMapValue
    private String height;

    @Inject
    @ValueMapValue
    private String allowForms;

    @Inject
    @ValueMapValue
    private String allowPointerLock;

    @Inject
    @ValueMapValue
    private String allowPopups;

    @Inject
    @ValueMapValue
    private String allowSameOrigin;

    @Inject
    @ValueMapValue
    private String allowScripts;

    @Inject
    @ValueMapValue
    private String allowTopNavigation;

    @Inject
    @ValueMapValue
    private String widthPercent;

    @Inject
    @ValueMapValue
    private String heightPercent;

    public String getSrc() {
        return ServiceUtils.appendLinkExtension(getResourceResolver(), src);
    }

    public String getSrcdoc() {
        return srcdoc;
    }

    public String getSrcType() {
        return srcType;
    }

    public Boolean getIsSrc() {
        return srcType.equals(SRCTYPE_SRC);
    }

    public Boolean getIsSrcdoc() {
        return srcType.equals(SRCTYPE_SRC_DOC);
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public String getAllowForms() {
        return allowForms;
    }

    public String getAllowPointerLock() {
        return allowPointerLock;
    }

    public String getAllowPopups() {
        return allowPopups;
    }

    public String getAllowSameOrigin() {
        return allowSameOrigin;
    }

    public String getAllowScripts() {
        return allowScripts;
    }

    public String getAllowTopNavigation() {
        return allowTopNavigation;
    }

    public String getName() {
        return name;
    }

    public String getWidthPercent() {
        return widthPercent;
    }

    public String getHeightPercent() {
        return heightPercent;
    }
}
