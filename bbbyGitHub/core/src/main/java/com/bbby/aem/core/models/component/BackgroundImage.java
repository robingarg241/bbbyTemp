package com.bbby.aem.core.models.component;

import com.bbby.aem.core.models.component.traits.image.ImageSupport;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Background Image component provides control over the background image used at both desktop and mobile screen sizes.
 * <p>
 * Also provides control over height and image focal point.
 *
 * @author joelepps
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class BackgroundImage extends ComponentSlingModel implements ImageSupport {

    /*
        DESKTOP
     */

    @Inject
    @ValueMapValue
    @Default(values = "v-center")
    private String verticalAlignment;

    @Inject
    @ValueMapValue
    @Default(booleanValues = false)
    private boolean desktopUseFixedHeight;

    @Inject
    @ValueMapValue
    @Default(values = "500")
    private String desktopHeight;

    @Inject
    @ValueMapValue
    @Default(values = "px")
    private String desktopHeightUnit;

    @Inject
    @ValueMapValue
    @Default(values = "50")
    private String desktopVertFP;

    @Inject
    @ValueMapValue
    @Default(values = "50")
    private String desktopHorzFP;

    /*
        TABLET
     */

    @Inject
    @ValueMapValue
    private boolean tabletOverrides;

    @Inject
    @ValueMapValue
    @Default(booleanValues = false)
    private boolean tabletUseFixedHeight;

    @Inject
    @ValueMapValue
    private String tabletHeight;

    @Inject
    @ValueMapValue
    private String tabletHeightUnit;

    @Inject
    @ValueMapValue
    @Default(values = "50")
    private String tabletVertFP;

    @Inject
    @ValueMapValue
    @Default(values = "50")
    private String tabletHorzFP;

    /*
        MOBILE
     */

    @Inject
    @ValueMapValue
    private boolean mobileOverrides;

    @Inject
    @ValueMapValue
    @Default(booleanValues = false)
    private boolean mobileUseFixedHeight;

    @Inject
    @ValueMapValue
    private String mobileHeight;

    @Inject
    @ValueMapValue
    private String mobileHeightUnit;

    @Inject
    @ValueMapValue
    @Default(values = "50")
    private String mobileVertFP;

    @Inject
    @ValueMapValue
    @Default(values = "50")
    private String mobileHorzFP;

    public String getVerticalAlignment() {
        return verticalAlignment;
    }

    /*
     * DESKTOP METHODS
     */

    public String getDesktopImageStyles() {
        return " " + desktopHorzFP + "% " + desktopVertFP + "% / cover";
    }

    public String getDesktopHeight() {
        return desktopHeight;
    }

    public String getDesktopHeightUnit() {
        return desktopHeightUnit;
    }

    public boolean getDesktopUseFixedHeight() {
        return desktopUseFixedHeight;
    }

    public String getDesktopHeightCss() {
        if (getDesktopUseFixedHeight()) {
            return getDesktopHeight() + getDesktopHeightUnit();
        }
        return "auto";
    }

    /*
        TABLET METHODS
     */

    public String getTabletImageStyles() {
        if (!tabletOverrides) return getDesktopImageStyles();
        return " " + tabletHorzFP + "% " + tabletVertFP + "% / cover";
    }

    public String getTabletHeight() {
        if (!tabletOverrides) return getDesktopHeight();
        return tabletHeight;
    }

    public String getTabletHeightUnit() {
        if (!tabletOverrides) return getDesktopHeightUnit();
        return tabletHeightUnit;
    }

    public boolean getTabletUseFixedHeight() {
        if (!tabletOverrides) return getDesktopUseFixedHeight();
        return tabletUseFixedHeight;
    }

    public String getTabletHeightCss() {
        if (getTabletUseFixedHeight()) {
            //example 500px
            return getTabletHeight() + getTabletHeightUnit();
        }
        return "auto";
    }

    /*
        MOBILE METHODS
     */

    public String getMobileImageStyles() {
        if (!mobileOverrides) return getDesktopImageStyles();
        return " " + mobileHorzFP + "% " + mobileVertFP + "% / cover";
    }

    public String getMobileHeight() {
        if (!mobileOverrides) return getDesktopHeight();
        return mobileHeight;
    }

    public String getMobileHeightUnit() {
        if (!mobileOverrides) return getDesktopHeightUnit();
        return mobileHeightUnit;
    }

    public boolean getMobileUseFixedHeight() {
        if (!mobileOverrides) return getDesktopUseFixedHeight();
        return mobileUseFixedHeight;
    }

    public String getMobileHeightCss() {
        if (getMobileUseFixedHeight()) {
            //example 500px
            return getMobileHeight() + getMobileHeightUnit();
        }
        return "auto";
    }

}
