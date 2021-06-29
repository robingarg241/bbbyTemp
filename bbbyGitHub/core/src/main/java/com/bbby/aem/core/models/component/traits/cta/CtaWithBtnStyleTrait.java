package com.bbby.aem.core.models.component.traits.cta;

import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.annotations.Option;
import com.citytechinc.cq.component.annotations.widgets.Selection;
import com.bbby.aem.core.models.component.traits.ComponentTrait;
import com.bbby.aem.core.util.SlingModelUtils;
import com.bbby.aem.dialog.classic.multifield.MultiCompositeField;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import java.util.List;

/**
 * Alternate CTA trait that allows authors to select the CTA button style.
 *
 * @author joelepps 8/18/16
 */
public interface CtaWithBtnStyleTrait extends ComponentTrait {

    interface DialogFields {

        @DialogField(fieldLabel = "CTA List", name = "./cta", ranking = 101)
        @MultiCompositeField(maxLimit = 1) // will inspect generic (CtaModel) dialog annotations
        List<CtaModelWithBtnStyle> ctaList();

    }

    default List<CtaModelWithBtnStyle> getCtaList() {
        Resource ctaListResource = getResource().getChild("cta");
        return SlingModelUtils.buildComponentSlingModels(ctaListResource, getRequest(), CtaModelWithBtnStyle.class,
            true);
    }

    @Model(adaptables = { Resource.class, SlingHttpServletRequest.class },
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    class CtaModelWithBtnStyle extends CtaTrait.CtaModel {

        private static final String DEFAULT = "_default_";
        private static final String LINK = "btn-link";

        @DialogField(fieldLabel = "Style", name = "./style", defaultValue = DEFAULT, ranking = 102.5)
        @Selection(type = "select", options = {
            @Option(text = "Button", value = DEFAULT),
            @Option(text = "Link", value = LINK),
        })
        @Inject
        @ValueMapValue
        private String style;

        public String getCtaStyle() {
            if (DEFAULT.equals(style)) return null;
            return style;
        }

    }

}
