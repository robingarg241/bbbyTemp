package com.bbby.aem.core.models.common;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.bbby.aem.core.models.component.ComponentSlingModel;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Generic model for standard HREF link.
 * <p>
 * This class is an instance of a {@link ComponentSlingModel} that can be constructed from two kinds of resources.
 *
 * <ol>
 * <li>Page or jcr:content node</li>
 * <li>Static properties from nt:unstructured node</li>
 * </ol>
 *
 * When constructed from a static nt:unstructured node, the node must have the following properties:
 * <ul>
 * <li>title</li>
 * <li>link</li>
 * <li>newTab</li>
 * </ul>
 *
 * @author joelepps 4/1/16
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class SimpleLink extends ComponentSlingModel {

    private static final Logger log = LoggerFactory.getLogger(SimpleLink.class);

    public static final String DISPLAY_ONLY = "navDisplayOnly";

    @Inject
    @Required
    private Resource resourcePage;

    @Inject
    @Self
    @Required
    private Resource self;

    @Inject
    @ValueMapValue
    private String title;

    @Inject
    @ValueMapValue
    private String link;

    @Inject
    @ValueMapValue
    private String newTab;

    private String navigationTitle;

    public String getTitle() {
        return title;
    }

    public String getNavigationTitle() {
        return navigationTitle;
    }

    public String getLink() {
        return link;
    }

    public String getNewTab() {
        return newTab;
    }

    public boolean getNewTabBool() {
        return "true".equals(newTab);
    }

    @Override
    public void postConstruct() throws Exception {
        super.postConstruct();

        String primaryType = self.getValueMap().get("jcr:primaryType", String.class);

        if ("cq:PageContent".equals(primaryType) || "cq:Page".equals(primaryType)) {
            initFromResourcePage();
        } else {
            initFromStaticProperties();
        }
    }

    private void initFromResourcePage() {
        String nodeName = resourcePage.getName();
        String nodePath = resourcePage.getPath();
        Resource jcrContent = resourcePage.getChild(CommonConstants.JCR_CONTENT);
        if (jcrContent == null) {
            log.warn("Cannot initialize simple link from {}", resourcePage);
            return;
        }

        ValueMap valueMap = jcrContent.getValueMap();
        this.title = buildTitle(valueMap, nodeName);
        this.navigationTitle = title;
        this.link = buildLink(nodePath, valueMap);
    }

    private void initFromStaticProperties() {
        String rawLink = link;
        link = ServiceUtils.appendLinkExtension(getResourceResolver(), link);

        if (title == null) {
            PageManager pageManager = getResourceResolver().adaptTo(PageManager.class);
            if (pageManager != null) {
                Page page = pageManager.getContainingPage(rawLink);
                if (page != null) {
                    navigationTitle = ServiceUtils.coalesce(page.getNavigationTitle(), page.getPageTitle(),
                        page.getTitle());
                    title = ServiceUtils.coalesce(page.getPageTitle(), page.getNavigationTitle(), page.getTitle());
                }
            }
        } else {
            navigationTitle = title;
        }
    }

    private static String buildTitle(ValueMap jcrContentValueMap, String fallback) {
        String title = jcrContentValueMap.get(NameConstants.PN_NAV_TITLE, String.class);
        if (StringUtils.isBlank(title)) title = jcrContentValueMap.get(NameConstants.PN_PAGE_TITLE, String.class);
        if (StringUtils.isBlank(title)) title = jcrContentValueMap.get("metadata/dc:title", String.class);
        if (StringUtils.isBlank(title)) title = jcrContentValueMap.get("metadata/pdf:Title", String.class);
        if (StringUtils.isBlank(title)) title = jcrContentValueMap.get(NameConstants.PN_TITLE, String.class);
        if (StringUtils.isBlank(title)) title = fallback;
        return title;
    }

    private static String buildLink(String path, ValueMap jcrContentValueMap) {
        if (jcrContentValueMap.get(DISPLAY_ONLY, false)) {
            return null;
        }
        return path + ".html";
    }

}
