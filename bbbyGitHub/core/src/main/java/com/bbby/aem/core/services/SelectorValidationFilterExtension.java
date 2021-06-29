package com.bbby.aem.core.services;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Provides an extension point for the {@link com.bbby.aem.core.filters.SelectorValidationFilter} allowing for
 * additional custom validation logic to be supplied.
 *
 * @author joelepps
 * 7/9/18
 */
public interface SelectorValidationFilterExtension {

    /**
     * Checks if the request should be allowed through based on the included selectors.
     *
     * @param request request to validate
     * @return {@code true} if request should be allowed through, {@code false} otherwise
     */
    boolean isValid(SlingHttpServletRequest request);

}
