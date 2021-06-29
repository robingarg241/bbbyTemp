package com.bbby.aem.core.services;

import com.day.cq.wcm.api.Page;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Given a page, searches for alternate versions of the same page with different locales.
 * <p>
 * Primary use case for this is the {@code <link>} tags in the {@code <head>} that list all other versions of the
 * current page for different locales.
 *
 * @author joelepps 7/13/17
 */
public interface AltPageResolver {

    /**
     * Search of alternate pages.
     *
     * @param page base version of page
     * @param homePageResourceType the resource type for home pages for this site.
     * @return List of alternate pages, empty list if none found
     */
    @Nonnull
    List<Page> getAltPages(Page page, String homePageResourceType);

}
