package com.bbby.aem.core.models.common;

import com.day.cq.commons.Externalizer;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.bbby.aem.core.services.AltPageResolver;
import com.bbby.aem.core.util.ImageRendition;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provides raw data that is typically used for HTML &lt;meta&gt; tags but can also be used by any piece of
 * functionality that needs meta information for a given resource.
 *
 * @author joelepps
 *
 */
@Model(adaptables = Resource.class)
public class PageMetadata {

    private static final Logger log = LoggerFactory.getLogger(PageMetadata.class);

    private static final String SEO_DESCRIPTION = "seoDescription";
    private static final String SEO_PAGE_TITLE = "seoPageTitle";
    private static final String SEO_PAGE_TITLE_SUFFIX = "seoTitleSuffix";
    private static final String SEO_TWITTER_HANDLE = "seoTwitterHandle";
    private static final String ANALYTICS_PAGE_ID = "pageID";
    private static final String ROBOTS_OPTIONS = "robotsOptions";

    @Inject
    @OSGiService
    private Externalizer externalizer;

    @Inject
    @SlingObject
    private Resource resource;

    @Inject
    @SlingObject
    private ResourceResolver resolver;

    private Page page;
    private ValueMap pageProps;
    private InheritanceValueMap pageInheritProps;

    @PostConstruct
    public void postConstruct() {
        PageManager pageManager = resolver.adaptTo(PageManager.class);
        if (pageManager == null) {
            throw new IllegalStateException("Cannot adapt to PageManager from resource: " + resource);
        }

        this.page = pageManager.getContainingPage(resource);
        if (this.page == null) {
            throw new IllegalStateException("Cannot find page for resource: " + resource);
        }

        this.pageProps = page.getProperties();
        this.pageInheritProps = new HierarchyNodeInheritanceValueMap(page.getContentResource());
    }

    public String getPageID() {
        String pageId = pageProps.get(ANALYTICS_PAGE_ID, String.class);

        log.trace("Page ID: {}", pageId);
        return pageId;
    }

    public String getTitle() {
        String title = pageProps.get(NameConstants.PN_PAGE_TITLE, String.class);
        if (StringUtils.isBlank(title)) title = pageProps.get(NameConstants.PN_TITLE, String.class);
        if (StringUtils.isBlank(title)) title = page.getName();

        log.trace("Title: {}", title);
        return title;
    }

    public String getSeoTitle() {
        String title = pageProps.get(SEO_PAGE_TITLE, String.class);
        String titleSuffix = pageInheritProps.getInherited(SEO_PAGE_TITLE_SUFFIX, "");

        if (StringUtils.isBlank(title)) {
            title = pageProps.get(NameConstants.PN_PAGE_TITLE, String.class);
            if (StringUtils.isBlank(title)) title = pageProps.get(NameConstants.PN_TITLE, String.class);
            if (StringUtils.isBlank(title)) title = page.getName();
        }

        if (!StringUtils.isBlank(titleSuffix)) {
            title += titleSuffix;
        }

        log.trace("SEO Title: {}", title);
        return title;
    }

    public String getDescription() {
        String description = pageProps.get(SEO_DESCRIPTION, String.class);
        if (StringUtils.isBlank(description)) description = pageProps.get(NameConstants.PN_DESCRIPTION, String.class);

        log.trace("Description: {}", description);
        return description;
    }

    public String getLocaleCode() {
        Locale locale = page.getLanguage(false);

        String localeCode = null;
        if (locale != null) {
            localeCode = locale.toLanguageTag();
        }

        log.trace("Locale: {}", localeCode);
        return localeCode;
    }

    public String getCanonicalUrl() {
        String url = externalizer.publishLink(resolver, page.getPath() + ".html");

        log.trace("Canonical URL for {} is {}", page.getPath(), url);
        return url;
    }

    public String getPath() {
        String path = resolver.map(page.getPath()) + ".html";

        log.trace("Path: {}", path);
        return path;
    }

    public String getContentType() {
        String template = pageProps.get(NameConstants.PN_TEMPLATE, String.class);
        if (template != null) {
            int idx = template.indexOf("templates/");
            if (idx > 0) {
                template = template.substring(idx);
            }
        }

        log.trace("Content type: {}", template);
        return template;
    }

    public String getRobotOptions() {
        String robotsValue = pageProps.get(ROBOTS_OPTIONS, String.class);

        if ("default".equals(robotsValue)) robotsValue = null;

        log.trace("Robot Options: {}", robotsValue);
        return robotsValue;
    }

    public String getTwitterSiteHandle() {
        String handle = pageInheritProps.getInherited(SEO_TWITTER_HANDLE, String.class);

        log.trace("Handle: {}", handle);
        return handle;
    }

    public String getImage() {
        String pageImage = pageProps.get("image/fileReference", String.class); // page image

        String imgUrl = null;
        if (pageImage != null) {
            imgUrl = externalizer.publishLink(resolver, ImageRendition.PORTRAIT_X1.getRendition(pageImage));
        }

        log.trace("Image: {}", imgUrl);
        return imgUrl;
    }

    public List<Page> getAltPages() {
        AltPageResolver altPageResolver = resolver.adaptTo(AltPageResolver.class);
        if (altPageResolver == null) return new ArrayList<>();

        return altPageResolver.getAltPages(page, "bbby/components/structure/pageHome");
    }

}
