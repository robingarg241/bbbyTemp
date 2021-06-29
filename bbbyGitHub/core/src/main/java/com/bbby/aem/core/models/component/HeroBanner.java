package com.bbby.aem.core.models.component;

import com.bbby.aem.core.models.component.traits.image.ImageSupport;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import java.util.Map;

/**
 * HeroBanner Component
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/hero-banner")
@Exporter(name = "jackson", extensions = "json")
public class HeroBanner extends ComponentSlingModel implements ImageSupport {

    @Inject
    @ValueMapValue
    private String title;

    @Inject
    @ValueMapValue
    private String superTitle;

    @Inject
    @ValueMapValue
    private String heroBannerTextColor;

    @Inject
    @ValueMapValue
    private String heroBannerAlignment;

    private Map<String, String> _allRenditions;

    public String getTitle() {
        return title;
    }

    public String getSuperTitle() {
        return superTitle;
    }

    public String getHeroBannerTextColor() {
        return heroBannerTextColor;
    }

    public String getHeroBannerAlignment() {
        return heroBannerAlignment;
    }

    @Override
    public String getRenditionMode() {
        return RENDITION_MODE_LANDSCAPE;
    }

    @Override
    public Map<String, String> getAllRenditions() {
        if (_allRenditions == null) {
            _allRenditions = ImageSupport.super.getAllRenditions();
        }
        return _allRenditions;
    }
}
