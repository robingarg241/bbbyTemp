package com.bbby.aem.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Servlet that returns static build info.
 * <p>
 * When debugging an issue this servlet provides a quick answer to the question of
 * <b>"Did the new code get picked up?"</b>.
 * <p>
 * AEM will sometimes fail to load new versions of bundles.
 * <p>
 * Below are troubleshooting options that can be taken if the version returned by this servlet is older than expected.
 *
 * <ul>
 *     <li>Restart AEM</li>
 *     <li>Re-deploy package</li>
 *     <li>Delete deployed JAR files in crx/de and re-deploy package</li>
 * </ul>
 *
 *  @author joelepps
 *         1/17/17
 */
@Component(
    service = Servlet.class,
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    property = {
        "sling.servlet.paths=/bin/bbby/buildNumber.txt",
        "sling.servlet.methods=GET"
    })
@Designate(ocd = BuildNumberServletConfiguration.class)
public class BuildNumberServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 3620532288346703072L;

    // This value is filled in at Maven build time.
    private static final String JAVA_BUILD_NUMBER = "${project.version}";
    // This value is filled in at Maven build time.
    private static final String JAVA_BUILD_TIME = "${timestamp}";

    private String osgiBuildNumber;
    private String osgiBuildTime;

    @Activate
    public void activate(BuildNumberServletConfiguration config) {
        osgiBuildNumber = config.buildNumber();
        osgiBuildTime = config.buildTime();
    }

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request,
                         @Nonnull SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setHeader("X-Robots-Tag", "noindex");
        response.getOutputStream().println("Java Build Number: " + JAVA_BUILD_NUMBER);
        response.getOutputStream().println("Java Build Time: " + JAVA_BUILD_TIME);
        response.getOutputStream().println("OSGi Build Number: " + osgiBuildNumber);
        response.getOutputStream().println("OSGi Build Time: " + osgiBuildTime);
        response.getOutputStream().close();
    }
}
