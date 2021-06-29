package com.bbby.aem.core.models.component;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Simple component providing a list of page links.
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PageList extends ComponentSlingModel {

    @Inject
    @ValueMapValue
    private boolean newTab;

    public boolean isNewTab() {
        return newTab;
    }

}
