package com.bbby.aem.core.models.component;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Anchor Component
 */

@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/anchor")
@Exporter(name = "jackson", extensions = "json")
public class Anchor extends ComponentSlingModel {

    @Inject
    @ValueMapValue
    private String articleTitleSize;

    @Inject
    @ValueMapValue
    private String articleColumnCount;

    public String getAnchorClass() {
        return "cmp-anchor";
    }

    public String getAnchorLink() {
        return "#" + getAnchorId();
    }

    public String getAnchorId() {
        return getProperties().get("anchorId", String.class);
    }

    public String getAnchorName() {
        return getProperties().get("anchorName", String.class);
    }
}
