package com.bbby.aem.core.models.common;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Sling model used when getting inheritable page properties.
 *
 * @author joelepps 10/3/16
 */
@Model(adaptables = Resource.class)
public class InheritedPageProps {

    private static final Logger log = LoggerFactory.getLogger(InheritedPageProps.class);

    @Inject
    @SlingObject
    private Resource resource;

    @Nullable
    private InheritanceValueMap iValueMap;

    @PostConstruct
    public void postConstruct() {
        ResourceResolver resolver = resource.getResourceResolver();
        PageManager pageManager = resolver.adaptTo(PageManager.class);
        if (pageManager != null) {
            Page page = pageManager.getContainingPage(resource);
            if (page != null) {
                Resource contentResource = page.getContentResource();
                if (contentResource != null) {
                    iValueMap = new HierarchyNodeInheritanceValueMap(contentResource);
                }
            }
        }
    }

    public String get(String key) {
        return get(key, null);
    }

    public <T> T get(String key, T defaultValue) {
        if (iValueMap == null) {
            log.warn("Cannot get {}, iValueMap is not initialized");
            return null;
        }

        T value = iValueMap.getInherited(key, defaultValue);

        // Allows for suppression of value, even if default is defined
        return "null".equals(value) ? null : value;
    }

    public String getHeaderPath() {
        return get("headerPath");
    }

    public String getFooterPath() {
        return get("footerPath");
    }

}
