package com.bbby.aem.core.models.component;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * This is the generic one-size-fits-all component used whenever there is a need for any combination of title, rich
 * text, and cta.
 *
 * @author joelepps
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/text-jumbo")
@Exporter(name = "jackson", extensions = "json")
public class TextJumbo extends ComponentSlingModel {

    @Inject
    @ValueMapValue
    private String title;

    @Inject
    @ValueMapValue
    private String textJumboTitleSize;

    @Inject
    @ValueMapValue
    private String textJumboAlignment;

    public String getTitle() {
        return title;
    }

    public String getTextJumboTitleSize() {
        return textJumboTitleSize;
    }

    public String getTextJumboAlignment() {
        return textJumboAlignment;
    }

    public boolean isRichText() {
        return true;
    }
}
