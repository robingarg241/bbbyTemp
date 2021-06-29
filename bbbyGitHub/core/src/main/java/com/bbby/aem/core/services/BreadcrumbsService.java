package com.bbby.aem.core.services;

import com.day.cq.wcm.api.Page;
import com.bbby.aem.core.models.common.SimpleLink;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;

/**
 * Service for fetching breadcrumb navigation links.
 *
 * @author joelepps 5/17/16
 *
 */
public interface BreadcrumbsService {

    /**
     * Fetches the breadcrumb navigation links for a given page.
     * <p>
     * Example:
     * <p>
     * {@code /content/acme/us/products/foo/bar}<br>
     * currentPage: bar<br>
     * startLevelAbs: 2<br>
     * stopLevelRel: 1<br>
     * <p>
     * return: [us, products, foo]
     *
     *
     * @param resolver ResourceResolver
     * @param currentPage Current page to build links for
     * @param startLevelAbs The absolute level to start links, zero based and inclusive
     * @param stopLevelRel The relative stop level for links, zero based an inclusive
     * @return Ordered list of navigation items
     */
    List<SimpleLink> getBreadcrumbs(ResourceResolver resolver, Page currentPage, int startLevelAbs, int stopLevelRel);

}
