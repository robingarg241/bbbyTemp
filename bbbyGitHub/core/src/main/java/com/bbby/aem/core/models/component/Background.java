package com.bbby.aem.core.models.component;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stand-alone background component providing author's ability to add a background that may encompass multiple
 * components at once, or provide a background to a component without built-in background support.
 * <p>
 * Use of this component should be rare since most every component should already include background support.
 *
 * @author joelepps 3/17/16
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Background extends ComponentSlingModel {

    @Inject
    @ValueMapValue
    @Default(values = "transparent-bg")
    private String backgroundColor;

    @Inject
    @ValueMapValue
    private List<String> visibility;

    @Inject
    @ValueMapValue
    private List<String> backgroundTopSpacing;

    @Inject
    @ValueMapValue
    private List<String> backgroundBottomSpacing;

    @Inject
    @ValueMapValue
    @Default(values = "_none_")
    private String backgroundHorizontalSpacing;

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getBackgroundTopSpacing() {
        if (backgroundTopSpacing == null) return null;

        return backgroundTopSpacing.stream().collect(Collectors.joining(" "));
    }

    public String getBackgroundBottomSpacing() {
        if (backgroundBottomSpacing == null) return null;

        return backgroundBottomSpacing.stream().collect(Collectors.joining(" "));
    }

    public boolean isBackgroundContainerEnabled() {
        // Always print container except for if set to full bleed
        return !"w-100".equals(backgroundHorizontalSpacing);
    }

    public String getBackgroundHorizontalSpacing() {
        return "_none_".equals(backgroundHorizontalSpacing) ? null : backgroundHorizontalSpacing;
    }

    public String getVisibilityClasses() {
        if (visibility == null) return null;

        return visibility.stream().collect(Collectors.joining(" "));
    }
}
