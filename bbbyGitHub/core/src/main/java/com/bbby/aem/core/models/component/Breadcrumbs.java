package com.bbby.aem.core.models.component;

import com.day.cq.wcm.api.designer.Style;
import com.bbby.aem.core.models.common.SimpleLink;
import com.bbby.aem.core.services.BreadcrumbsService;
import com.bbby.aem.core.util.SlingModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * Breadcrumbs Component that supports a static list of links are dynamic list based on current page hierarchy.
 * <p>
 * Authorable configurations are pull from component first and fallback to design dialog.
 */

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/breadcrumbs")
@Exporter(name = "jackson", extensions = "json")
public class Breadcrumbs extends ComponentSlingModel{

    private static final Logger log = LoggerFactory.getLogger(Breadcrumbs.class);

    private static final String STATIC = "staticFields";

    @Inject
    @ValueMapValue
    private String listType;

    @Inject
    @ValueMapValue
    private String breadcrumbsTextColor;

    public String getListType() {
        return listType;
    }

    public String getBreadcrumbsTextColor() {
        return breadcrumbsTextColor;
    }

    /*
     * NON-AUTHOR INJECTIONS
     */

    @Inject
    @OSGiService
    private BreadcrumbsService breadcrumbsService;

    @Inject
    private Style currentStyle;

    private List<SimpleLink> navigationItems;

    public List<SimpleLink> getNavigationItems() {
        return navigationItems;
    }

    @Override
    public void postConstruct() throws Exception {
        if (null!=listType && listType.equals(STATIC)) {
            navigationItems = SlingModelUtils.buildComponentSlingModels(getResource().getChild("staticPagePaths"), SimpleLink.class);
        } else {
            int startLevelAbs = getConfig("startLevelAbs", 2, Integer.class);
            int stopLevelRel = getConfig("stopLevelRel", 0, Integer.class);

            log.debug("Building breadcrumbs with startLevel {}, stopLevel {}", startLevelAbs, stopLevelRel);

            navigationItems = breadcrumbsService.getBreadcrumbs(getResourceResolver(), getCurrentPage(), startLevelAbs,
                stopLevelRel);
        }
    }

    /**
     * Get property with preference to current node followed by current style.
     *
     */
    private <T> T getConfig(String key, T defaultValue, Class<T> clazz) {
        T nodeProp = getProperties().get(key, clazz);
        if (nodeProp != null) {
            if (clazz.equals(String.class)) {
                if (!StringUtils.isBlank(nodeProp + "")) {
                    return nodeProp;
                }
            } else {
                return nodeProp;
            }
        }

        T styleProp = currentStyle.get(key, defaultValue);
        log.trace("Using design fallback: {} = {}", key, styleProp);
        return styleProp;
    }
}
