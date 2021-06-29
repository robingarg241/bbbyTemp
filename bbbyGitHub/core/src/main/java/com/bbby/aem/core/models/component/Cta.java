package com.bbby.aem.core.models.component;

import com.bbby.aem.core.util.SlingModelUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import java.util.List;

/**
 * Standalone CTA component.
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Cta extends ComponentSlingModel {

    public List<CtaLink> getCtaList() {
        Resource ctaListResource = getResource().getChild("cta");
        return SlingModelUtils.buildComponentSlingModels(ctaListResource, CtaLink.class);
    }

}
