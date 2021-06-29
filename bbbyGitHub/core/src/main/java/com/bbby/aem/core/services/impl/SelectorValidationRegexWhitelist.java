package com.bbby.aem.core.services.impl;

import com.bbby.aem.core.services.SelectorValidationFilterExtension;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Adds supplemental logic to the {@link com.bbby.aem.core.filters.SelectorValidationFilter} that checks resource type
 * based requests against regular expression rules.
 * <p>
 * Regular expression is applied against the entire Sling Selector string of the request (and not each selector
 * individually).
 *
 * @author joelepps
 * 7/9/18
 */
@Component(
    immediate = true,
    service = SelectorValidationFilterExtension.class)
@Designate(ocd = SelectorValidationRegexWhitelistConfig.class)
public class SelectorValidationRegexWhitelist implements SelectorValidationFilterExtension {

    private static final Logger log = LoggerFactory.getLogger(SelectorValidationRegexWhitelist.class);

    private static final String SLING_SERVLET_DEFAULT = "sling/servlet/default";

    private Map<String, List<Pattern>> patterns;

    @Activate
    protected void activate(SelectorValidationRegexWhitelistConfig config) {
        String[] rules = config.rules();
        patterns = new HashMap<>();
        for (String rule : rules) {
            try {
                String[] split = StringUtils.splitByWholeSeparator(rule, ":::");
                String resourceType = split[0];
                String regex = split[1];
                Pattern pattern = Pattern.compile(regex);

                if (!patterns.containsKey(resourceType)) {
                    patterns.put(resourceType, new ArrayList<>());
                }
                patterns.get(resourceType).add(pattern);
            } catch (Exception e) {
                log.error("Bad configuration: " + rule, e);
            }
        }
    }

    @Override
    public boolean isValid(SlingHttpServletRequest request) {
        String resourceType = request.getResource().getResourceType();

        // Get patterns for resource type of request
        List<Pattern> pList = getPatterns(resourceType);
        // Get patterns for catch all default resource type
        pList.addAll(getPatterns(SLING_SERVLET_DEFAULT));

        String selectorString = request.getRequestPathInfo().getSelectorString();

        if (selectorString == null) {
            log.trace("Ignoring request: {}", request.getPathInfo());
            return false;
        }

        for (Pattern p : pList) {
            if (p.matcher(selectorString).matches()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the Sling Selector regular expressions that should be applied against the given {@code resourceType}.
     *
     * @param resourceType resource type
     * @return list of patterns, never null
     */
    protected List<Pattern> getPatterns(String resourceType) {
        // protect against modifications by returning new array.
        return new ArrayList<>(patterns.getOrDefault(resourceType, Collections.emptyList()));
    }
}
