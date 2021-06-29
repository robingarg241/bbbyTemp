package com.bbby.aem.core.models.component;

import com.day.cq.dam.api.Asset;
import com.bbby.aem.core.models.component.traits.image.ImageSupport;
import com.bbby.aem.core.models.component.traits.image.Rendition;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Standalone image component.
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/image")
@Exporter(name = "jackson", extensions = "json")
public class Image extends ComponentSlingModel implements ImageSupport {

    @Inject
    @ValueMapValue
    private String linkURL;

    @Inject
    @ValueMapValue
    private boolean newTab;

    @Inject
    @ValueMapValue
    private boolean naturalSize;

    private Asset _asset;
    private List<Rendition> _renditions;
    private Map<String, String> _allRenditions;

    @Override
    public List<Rendition> getRenditions() {
        if (_renditions == null) {
            _renditions = ImageSupport.super.getRenditions();
        }
        return _renditions;
    }

    @Override
    public Map<String, String> getAllRenditions() {
        if (_allRenditions == null) {
            _allRenditions = ImageSupport.super.getAllRenditions();
        }
        return _allRenditions;
    }

    public String getLink() {
        return linkURL;
    }

    public boolean isNewTab() {
        return newTab;
    }

    public boolean isNaturalSize() {
        return naturalSize;
    }
}
