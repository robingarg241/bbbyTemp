package com.bbby.aem.core.filters;

import com.day.cq.wcm.api.WCMMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles page level redirectTarget support.
 *
 * @author joelepps
 * 3/2/18
 */
@Component(
    service = Filter.class,
    property = {
        "service.ranking=500",
        "sling.filter.scope=REQUEST"
    })
public class RedirectTargetFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(RedirectTargetFilter.class);

    @Reference
    private SlingSettingsService slingSettings;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        WCMMode mode = WCMMode.fromRequest(request);

        if (mode == WCMMode.DISABLED) {
            if (request instanceof SlingHttpServletRequest && response instanceof SlingHttpServletResponse) {
                SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
                SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;

                if ("GET".equals(slingRequest.getMethod())) {

                    Resource resource = slingRequest.getResource();
                    ValueMap valueMap = resource.getValueMap();

                    String redirectTarget = valueMap.get("jcr:content/redirectTarget", String.class);

                    if (StringUtils.isNotBlank(redirectTarget)) {
                        if (redirectTarget.startsWith("/")) {
                            redirectTarget = map(slingRequest, redirectTarget);
                        }

                        log.debug("Sending redirect: {}", redirectTarget);
                        slingResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                        slingResponse.setHeader("Location", redirectTarget);

                        return; // REDIRECT
                    }
                }
            }
        }

        // If here, no redirect took place, continue filter chain
        chain.doFilter(request, response);
    }

    private String map(SlingHttpServletRequest request, String redirectTarget) {
        String url = request.getResourceResolver().map(request, redirectTarget);
        if (url == null) return redirectTarget;

        url += url.contains(".html") || url.endsWith("/") ? "" : ".html";

        boolean wcmModeDisabled = WCMMode.fromRequest(request) == WCMMode.DISABLED;
        boolean isPublish = slingSettings.getRunModes().contains("publish");
        if (wcmModeDisabled && !isPublish) {
            url += (url.contains("?") ? "&" : "?") + "wcmmode=disabled";
        }

        return url;
    }

    @Override
    public void destroy() {

    }
}
