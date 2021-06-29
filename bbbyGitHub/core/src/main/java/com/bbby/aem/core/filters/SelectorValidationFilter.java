package com.bbby.aem.core.filters;

import com.bbby.aem.core.services.SelectorValidationFilterExtension;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This filter aims to mitigate Sling Selector based Denial of Service (DOS) attacks against AEM.
 * <p>
 * This is done by inspecting resource based requests and verifying that the selectors included are expected. If deemed
 * to be invalid, a 404 response is returned.
 * <p>
 * Supplementary validation logic can be supplied via {@link SelectorValidationFilterExtension}.
 * <p>
 * <a href=
 * "http://blogs.adobe.com/contentmanagement/2013/05/17/page-level-selectors-validation-to-prevent-dos-attacks/"> More
 * Information </a>
 * <p>
 * To enable this filter, include an empty OSGi XML configuration for this service as part of the build.
 *
 * @author joelepps 7/9/18
 */
@Component(
    service = Filter.class,
    // This service will be disabled if no configuration exists.
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    property = {
        "service.ranking=9999",
        "sling.filter.scope=REQUEST"
    })
@Designate(ocd = SelectorValidationFilterConfig.class)
public class SelectorValidationFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SelectorValidationFilter.class);

    private BundleContext bundleContext;

    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        bind = "bindExtension",
        unbind = "unbindExtension")
    private volatile List<SelectorValidationFilterExtension> extensions = new CopyOnWriteArrayList<>();

    @Reference
    private SlingSettingsService slingSettingsService;

    @Activate
    protected void activate() {
        this.bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
        extensions.clear();
    }

    @Override
    public void doFilter(ServletRequest request,
        ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        StopWatch timer = new StopWatch();
        timer.start();

        if (request instanceof SlingHttpServletRequest && response instanceof SlingHttpServletResponse) {
            SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
            SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) response;

            RequestPathInfo requestPathInfo = slingRequest.getRequestPathInfo();
            Set<String> requestSelectors = new HashSet<>(Arrays.asList(requestPathInfo.getSelectors()));

            if ("GET".equalsIgnoreCase(slingRequest.getMethod()) && !requestSelectors.isEmpty()) {

                //
                // START VALIDATION CHECKS
                //

                boolean isValidRequest = checkValidationExtensions(slingRequest, extensions);

                if (!isValidRequest) {
                    String resourceType = slingRequest.getResource().getResourceType();
                    isValidRequest = checkRegisteredServices(resourceType, requestSelectors, bundleContext);
                }

                if (!isValidRequest) {
                    // sling/servlet/default is the catch all resource type
                    isValidRequest = checkRegisteredServices("sling/servlet/default", requestSelectors, bundleContext);
                }

                //
                // END VALIDATION CHECKS
                //

                timer.stop();

                if (!isValidRequest) {
                    log.info("404: Unrecognized selectors: {} in {}", slingRequest.getPathInfo(), timer);
                    slingResponse.setStatus(404);
                    return;
                } else {
                    log.trace("Valid request: {} in {}", slingRequest.getPathInfo(), timer);
                }
            }
        }

        // Continue filter chain
        chain.doFilter(request, response);
    }

    protected static boolean checkValidationExtensions(SlingHttpServletRequest request,
        Collection<SelectorValidationFilterExtension> extensions) {

        for (SelectorValidationFilterExtension extension : extensions) {
            if (extension.isValid(request)) {
                log.trace("{} passes {}", request.getPathInfo(), extension.getClass());
                return true;
            }
        }

        return false;
    }

    protected static boolean checkRegisteredServices(String resourceType,
        Set<String> selectors,
        BundleContext bundleContext) {

        List<ServiceReference<?>> refs = getServiceReferences(bundleContext, selectors);
        for (ServiceReference<?> ref : refs) {

            Set<String> serviceSelectors = convertToSet(ref.getProperty("sling.servlet.selectors"));
            Set<String> serviceResourceTypes = convertToSet(ref.getProperty("sling.servlet.resourceTypes"));

            if (serviceResourceTypes.contains(resourceType) && serviceSelectors.containsAll(selectors)) {
                log.trace("Valid Request: {}, {} -> {}", resourceType, selectors, ref.getProperty("pid"));
                return true;
            }
        }

        return false;
    }

    private static List<ServiceReference<?>> getServiceReferences(BundleContext bundleContext, Set<String> selectors) {
        List<ServiceReference<?>> refs = new ArrayList<>();

        for (String selector : selectors) {
            if (StringUtils.isBlank(selector)) continue;

            try {
                // Get all Servlets which are registered for the given selector
                ServiceReference<?>[] refArr = bundleContext.getAllServiceReferences(
                    Servlet.class.getCanonicalName(),
                    "(sling.servlet.selectors=" + selector + ")");

                if (refArr != null) {
                    refs.addAll(Arrays.asList(refArr));
                }

            } catch (InvalidSyntaxException e) {
                log.debug("Ignoring selector " + selector, e);
            }
        }

        return refs;
    }

    private static Set<String> convertToSet(Object object) {
        if (object == null) return Collections.emptySet();

        if (object instanceof String) {
            return Collections.singleton((String) object);
        } else if (object instanceof String[]) {
            return new HashSet<>(Arrays.asList((String[]) object));
        } else {
            log.warn("Unexpected type " + object.getClass() + ", " + object);
            return Collections.emptySet();
        }
    }

    protected void bindExtension(SelectorValidationFilterExtension extension) {
        extensions.add(extension);
    }

    protected void unbindExtension(SelectorValidationFilterExtension extension) {
        extensions.remove(extension);
    }
}
