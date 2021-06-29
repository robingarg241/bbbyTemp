package com.bbby.aem.core.models.component.traits.cta;

import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.annotations.Property;
import com.citytechinc.cq.component.annotations.widgets.PathField;
import com.citytechinc.cq.component.annotations.widgets.TextArea;
import com.citytechinc.cq.component.annotations.widgets.TextField;
import com.bbby.aem.core.models.component.ComponentSlingModel;
import com.bbby.aem.core.models.component.traits.ComponentTrait;
import com.bbby.aem.core.models.component.traits.analytics.AnalyticsTrait;
import com.bbby.aem.core.util.SlingModelUtils;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.dialog.classic.multifield.MultiCompositeField;
import com.bbby.aem.dialog.touch.checkbox.CheckBoxTouch;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import java.beans.IntrospectionException;
import java.util.List;
import java.util.Map;

/**
 * CTA trait providing standard minimum set of CTA fields.
 *
 * @author joelepps 2/22/16
 */
public interface CtaTrait extends ComponentTrait {

    interface DialogFields {

        @DialogField(fieldLabel = "CTA", name = "./cta", ranking = 101)
        @MultiCompositeField(maxLimit = 1) // will inspect generic (CtaModel) dialog annotations
        List<CtaModel> ctaList();
    }

    default List<CtaModel> getCtaList() throws ReflectiveOperationException, IntrospectionException {
        Resource ctaListResource = getResource().getChild("cta");
        return SlingModelUtils.buildComponentSlingModels(ctaListResource, getRequest(), CtaModel.class, true);
    }

    @Model(adaptables = { Resource.class, SlingHttpServletRequest.class },
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    class CtaModel extends ComponentSlingModel {

        @DialogField(fieldLabel = "Title", name = "./title", ranking = 100,
            fieldDescription = "The text of the CTA", required = true)
        @TextField
        @Inject
        @ValueMapValue
        private String title;

        @DialogField(fieldLabel = "Link", name = "./link", ranking = 102)
        @PathField(rootPath = "/content/")
        @Inject
        @ValueMapValue
        private String link;

        @DialogField(fieldLabel = "New Tab", name = "./newTab", ranking = 103, value = "true")
        @CheckBoxTouch(text = "New Tab")
        @Inject
        @ValueMapValue
        private String newTab;

        @DialogField(fieldLabel = "Analytics", name = "./analyticsAttributes", ranking = 104,
            fieldDescription = "Example: data-key1=value1,data-key2=value2. Attribute name cannot be '" +
                AnalyticsTrait.DATA_COMPONENT_NAME + "'",
            additionalProperties = {
                @Property(name = "regex", value = "^(?:(?:data-[\\\\-a-z0-9]+=[^,]+),?)*$"),
                @Property(name = "foundation-validation", value = "regex") })
        @TextArea
        @Inject
        @ValueMapValue
        private String analyticsAttributes;

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        public String getNewTab() {
            return newTab;
        }

        public boolean getNewTabBool() {
            return "true".equals(getNewTab());
        }

        public String getAnalyticsAttributes() {
            return analyticsAttributes;
        }

        public Map<String, String> getAnalyticsAttributeMap() {
            List<AnalyticsTrait.AnalyticsAttribute> attributeList = AnalyticsTrait
                .getAnalyticsAttributeList(getAnalyticsAttributes(), ",", "=");
            return AnalyticsTrait.getAnalyticsAttributeMap(attributeList, "ctaItem");
        }

        @Override
        public void postConstruct() {
            link = ServiceUtils.appendLinkExtension(getResourceResolver(), link);
        }

    }

}
