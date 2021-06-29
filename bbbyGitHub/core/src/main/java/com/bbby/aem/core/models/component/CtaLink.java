package com.bbby.aem.core.models.component;

import com.bbby.aem.core.util.ServiceUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 *
 * @author joelepps
 * 6/22/18
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CtaLink extends ComponentSlingModel {

    @Inject
    @ValueMapValue
    private String title;

    @Inject
    @ValueMapValue
    private String link;

    @Inject
    @ValueMapValue
    private boolean newTab;

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public boolean getNewTab() {
        return newTab;
    }

    @Override
    public void postConstruct() {
        link = ServiceUtils.appendLinkExtension(getResourceResolver(), link);
    }

}
