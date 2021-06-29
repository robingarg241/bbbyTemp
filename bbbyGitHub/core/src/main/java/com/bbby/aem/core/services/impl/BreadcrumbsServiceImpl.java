package com.bbby.aem.core.services.impl;

import com.day.cq.wcm.api.Page;
import com.bbby.aem.core.models.common.SimpleLink;
import com.bbby.aem.core.services.BreadcrumbsService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation for {@link BreadcrumbsService}
 *
 * @author joelepps 5/17/16
 */
@Component(immediate = true, service = BreadcrumbsService.class)
public class BreadcrumbsServiceImpl implements BreadcrumbsService {

    private static final Logger log = LoggerFactory.getLogger(BreadcrumbsServiceImpl.class);

    public static final String HIDE_IN_BREADCRUMBS = "hideInBreadcrumbs";

    @Override
    public List<SimpleLink> getBreadcrumbs(ResourceResolver resolver, Page currentPage, int startLevelAbs,
        int stopLevelRel) {
        List<SimpleLink> navigationItems = new ArrayList<SimpleLink>();

        if (currentPage == null || resolver == null) {
            log.debug("Cannot build breadcrumbs with currentPage {}, resolver {}", currentPage, resolver);
            return navigationItems;
        }

        log.trace("Building breadcrumbs from {}, {}, {}", currentPage.getPath(), startLevelAbs, stopLevelRel);

        int level = startLevelAbs;
        int currentLevel = currentPage.getDepth();

        while (level < currentLevel - stopLevelRel) {
            Page trail = currentPage.getAbsoluteParent(level);
            if (trail == null) {
                log.warn("Could not get abs parent of {} at level {}", currentPage.getPath(), level);
                break;
            }

            Resource trailResource = resolver.resolve(trail.getPath());
            SimpleLink navItem = trailResource.adaptTo(SimpleLink.class);
            if (navItem == null) {
                log.warn("Could not adapt page to navigation item: {}", trailResource);
                break;
            }

            if (!shouldSkip(trail, navItem)) {
                navigationItems.add(navItem);
            }

            level++;
        }

        log.debug("Built dynamic breadcrumbs {}", navigationItems);

        return navigationItems;
    }

    private boolean shouldSkip(Page page, SimpleLink navItem) {
        if (StringUtils.isBlank(navItem.getTitle())) {
            log.info("Missing title. Skipping breadcrumb for {}", page.getPath());
            return true;
        }
        if (BooleanUtils.toBoolean(page.getProperties().get(HIDE_IN_BREADCRUMBS, Boolean.class))) {
            log.debug("{} = true, Skipping breadcrumb for {}", HIDE_IN_BREADCRUMBS, page.getPath());
            return true;
        }
        return false;
    }
}
