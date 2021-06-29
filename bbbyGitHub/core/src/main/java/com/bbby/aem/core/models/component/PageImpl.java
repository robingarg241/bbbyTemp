package com.bbby.aem.core.models.component;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ContainerExporter;
import com.adobe.cq.wcm.core.components.models.NavigationItem;
import com.adobe.cq.wcm.core.components.models.Page;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.via.ResourceSuperType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Map;

/**
 * Page model containing project specific customizations.
 *
 * @author joelepps
 * 7/30/18
 */
@Model(
    adaptables = {SlingHttpServletRequest.class},
    adapters = {Page.class, ContainerExporter.class},
    resourceType = {"bbby/components/structure/page"}
)
public class PageImpl implements Page {

    @Self
    @Via(type = ResourceSuperType.class)
    private Page basePage;

    // =======================
    // Begin Unchanged Methods
    // =======================

    @Override
    public String getLanguage() {
        return basePage.getLanguage();
    }

    @Override
    public Calendar getLastModifiedDate() {
        return basePage.getLastModifiedDate();
    }

    @Override
    public String[] getKeywords() {
        return basePage.getKeywords();
    }

    @Override
    public String getDesignPath() {
        return basePage.getDesignPath();
    }

    @Override
    public String getStaticDesignPath() {
        return basePage.getStaticDesignPath();
    }

    @Override
    public Map<String, String> getFavicons() {
        return basePage.getFavicons();
    }

    @Override
    public String getTitle() {
        return basePage.getTitle();
    }

    @Override
    public String[] getClientLibCategories() {
        return basePage.getClientLibCategories();
    }

    @Override
    public String getTemplateName() {
        return basePage.getTemplateName();
    }

    @Override
    public String getCssClassNames() {
        return basePage.getCssClassNames();
    }

    @Nullable
    @Override
    public NavigationItem getRedirectTarget() {
        return basePage.getRedirectTarget();
    }

    @Override
    public boolean hasCloudconfigSupport() {
        return basePage.hasCloudconfigSupport();
    }

    @Nonnull
    @Override
    public String[] getExportedItemsOrder() {
        return basePage.getExportedItemsOrder();
    }

    @Nonnull
    @Override
    public Map<String, ? extends ComponentExporter> getExportedItems() {
        return basePage.getExportedItems();
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return basePage.getExportedType();
    }

    @Nullable
    @Override
    public String getAppResourcesPath() {
        return basePage.getAppResourcesPath();
    }
}
