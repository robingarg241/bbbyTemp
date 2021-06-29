package com.bbby.aem.core.models.component;

import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sharing Component
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Sharing extends ComponentSlingModel {
    private static final Logger log = LoggerFactory.getLogger(Sharing.class);

    private final String PN_SOCIAL_MEDIA = "socialMedia";

    private final String PV_TWITTER = "twitter";

    public Boolean getTwitterEnabled() {
        boolean twitterEnabled = false;
        try {
            Page currentPage = getCurrentPage();
            String[] socialMedia = currentPage.getContentResource().getValueMap().get(PN_SOCIAL_MEDIA, String[].class);
            twitterEnabled = ArrayUtils.contains(socialMedia, PV_TWITTER);
        } catch (Exception e) {
            log.error("An exception has occured when getting isTwitterEnabled value : " + e.getMessage(), e);
        }
        return twitterEnabled;
    }
}
