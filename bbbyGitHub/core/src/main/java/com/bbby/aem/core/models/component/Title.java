package com.bbby.aem.core.models.component;

import com.bbby.aem.core.util.ServiceUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Title Component
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/title")
@Exporter(name = "jackson", extensions = "json")
public class Title extends ComponentSlingModel {

    @Inject
    @ValueMapValue
    private String newTab;

    @Inject
    @ValueMapValue
    private String linkURL;

    public String getNewTab() {
        return newTab;
    }

    public String getLinkUrl() {
        return ServiceUtils.appendLinkExtension(getResourceResolver(), linkURL);
    }
}
