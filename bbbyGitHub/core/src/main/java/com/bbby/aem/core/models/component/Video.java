package com.bbby.aem.core.models.component;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Video Component that support YouTube.
 */

@Model(adaptables = { SlingHttpServletRequest.class, Resource.class },
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/video")
public class Video extends ComponentSlingModel{
    private static final String PROVIDER_VIMEO = "vimeo";

    @Inject
    @SlingObject
    @Optional
    private SlingHttpServletRequest request;

    @Inject
    @ValueMapValue
    private String videoProvider;

    @Inject
    @ValueMapValue
    private String videoId;

    @Inject
    @ValueMapValue
    private String autoplay;

    public String getVideoProvider() {
        return videoProvider;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getAutoplay() {
        return autoplay;
    }

    /**
     * @return {@code true} if vimeo script needs to be added to page, {@code false} if script has already been
     *         included.
     */
    public boolean getIncludeVimeoScript() {
        if (PROVIDER_VIMEO.equals(getVideoProvider())) {
            if (request == null) {
                getLogger().debug("Request is null, cannot determine vimeo script include: {}", getResource());
                return true;
            }
            String key = Video.class.getName() + ".vimeo.script.loaded";

            String loaded = (String) request.getAttribute(key);
            if (loaded == null) {
                request.setAttribute(key, "true");
                return true;
            }
        }

        return false;
    }

}
