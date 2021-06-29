package com.bbby.aem.core.models.component;

import com.bbby.aem.core.models.component.traits.image.ImageSupport;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Text Image Two Column Component
 * <p>
 * Supports standard use case of responsive component that renders text in one column and image in another.
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/text-image-two-column")
@Exporter(name = "jackson", extensions = "json")
public class TextImageTwoColumn extends ComponentSlingModel implements ImageSupport {

    public static final String RENDITION_IMAGE_RIGHT = "imageRight";
    public static final String RENDITION_IMAGE_LEFT = "imageLeft";

    @Inject
    @ValueMapValue
    private String title;

    @Inject
    @ValueMapValue
    private String superTitle;

    @Inject
    @ValueMapValue
    private String textImage2ColumnRendition;

    public String getTitle() {
        return title;
    }

    public String getSuperTitle() {
        return superTitle;
    }

    public String getTextImage2ColumnRendition() {
        return textImage2ColumnRendition;
    }

    public String getTextColClasses() {
        String rendition = getTextImage2ColumnRendition();
        if (RENDITION_IMAGE_RIGHT.equals(rendition)) {
            return "order-md-1";
        }
        return null;
    }

    public String getImageColClasses() {
        String rendition = getTextImage2ColumnRendition();
        if (RENDITION_IMAGE_RIGHT.equals(rendition)) {
            return "order-md-12";
        }
        return null;
    }

    @Override
    public String getRenditionMode() {
        return RENDITION_MODE_PORTRAIT;
    }
}
