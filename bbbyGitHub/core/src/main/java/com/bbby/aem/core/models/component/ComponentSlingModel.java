package com.bbby.aem.core.models.component;

import com.adobe.cq.export.json.ComponentExporter;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.commons.WCMUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.bbby.aem.core.models.component.traits.ComponentTrait;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sling Model based implementation of the {@link ComponentTrait}.
 * <p>
 * Implementations must add the {@link Model} annotation.
 *
 * @author joelepps
 * @see <a href="https://sling.apache.org/documentation/bundles/models.html">Sling Models</a>
 */
public abstract class ComponentSlingModel implements ComponentTrait, ComponentExporter {

    public static final String COMPONENT_GROUP_CONTENT = "BBBY Content";
    public static final String COMPONENT_GROUP_MISC = "BBBY Misc";
    public static final String COMPONENT_GROUP_HIDDEN = ".hidden";

    private Map<String, String> debugModel;
    private Set<Class<?>> debugTypes;

    @Inject
    @Required
    @SlingObject
    private Resource resource;

    @Inject
    @Required
    @SlingObject
    private ResourceResolver resourceResolver;

    @Inject
    @Required
    @OSGiService
    private SlingSettingsService slingSettingsService;

    @Inject
    @Optional
    @SlingObject
    private SlingHttpServletRequest request;

    @Inject
    @Optional
    private Page currentPage; // requires ACS Commons

    private Logger _logger;

    /**
     * Initialization method necessary for Sling Sling models.
     */
    protected ComponentSlingModel() {
        // All fields will be set dynamically via Sling Models.
    }

    @Override
    @Nonnull
    @JsonIgnore
    public Resource getResource() {
        return resource;
    }

    @Override
    @Nonnull
    @JsonIgnore
    public ValueMap getProperties() {
        return getResource().getValueMap();
    }

    @Override
    @Nonnull
    @JsonIgnore
    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    @Override
    @Nullable
    public SlingHttpServletRequest getRequest() {
        return request;
    }

    @Override
    @Nonnull
    @JsonIgnore
    public Logger getLogger() {
        if (_logger == null) {
            _logger = LoggerFactory.getLogger(getClass());
        }
        return _logger;
    }

    @Nullable
    @Override
    @JsonIgnore
    public Page getCurrentPage() {
        if (currentPage == null) {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            if (pageManager != null) {
                currentPage = pageManager.getContainingPage(getResource());
            }
        }
        return currentPage;
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return resource.getResourceType();
    }

    @PostConstruct
    @Override
    public void postConstruct() throws Exception {
        // stub for subclasses to inherit
    }

    /*
     * START DEBUGGING HELPER CODE Helpful during component development. Should never be invoked by production code.
     */

    /**
     * Refer to the following for instructions on use:
     * <p>
     * {@code /apps/bbby/components/content/_debugging/_debugging.html}
     */
    @JsonIgnore
    public Map<String, String> getDebugModel() throws Exception {
        initDebug();
        return debugModel;
    }

    /**
     * When this Sling Model is instantiated from a {@link Resource} (typically via {@code resource.adaptTo(...)}) it
     * will not have a {@link SlingHttpServletRequest object set. <p> This method is provided so that you can manually
     * set the request object prior to invoking methods that may require the request object.
     *
     * @param request {@link SlingHttpServletRequest} object.
     */
    public void setRequest(SlingHttpServletRequest request) {
        this.request = request;
        if (request != null) {
            this.currentPage = WCMUtils.getComponentContext(request).getPage();
        }
    }

    /**
     * Refer to the following for instructions on use:
     * <p>
     * {@code /apps/bbby/components/content/_debugging/_debugging.html}
     */
    @JsonIgnore
    public List<String> getDebugModelKeys() throws Exception {
        List<String> keys = new ArrayList<>(getDebugModel().keySet());
        Collections.sort(keys);
        return keys;
    }

    /**
     * Refer to the following for instructions on use:
     * <p>
     * {@code /apps/bbby/components/content/_debugging/_debugging.html}
     */
    @JsonIgnore
    public List<String> getDebugTypes() throws Exception {
        initDebug();
        return debugTypes.stream().map(Object::toString).sorted().collect(Collectors.toList());
    }

    private void initDebug() throws Exception {
        if (debugModel != null && debugTypes != null) return;

        Map<String, String> model = new HashMap<>();
        Set<Class<?>> types = new HashSet<>();

        ComponentModelDebugHelper.appendToDebugModel("model", getClass(), this, model, types);

        this.debugModel = model;
        this.debugTypes = types;
    }

}
