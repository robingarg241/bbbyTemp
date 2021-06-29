package com.bbby.aem.core.models.component.traits.image;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.bbby.aem.core.models.component.traits.ComponentTrait;
import com.bbby.aem.core.util.ImageRendition;
import org.apache.sling.api.resource.Resource;

import java.util.*;

/**
 * Any components wishing to re-use the Sightly {@code renderImage} or {@code renderBackgroundImageStyleTag} must
 * implement this interface.
 *
 * @author joelepps
 * 6/25/18
 */
public interface ImageSupport extends ComponentTrait {

    int DESKTOP_XL_MIN = 1400;
    int DESKTOP_MIN = 992;
    int TABLET_MIN = 768;

    String RENDITION_MODE_LANDSCAPE = "landscape";
    String RENDITION_MODE_PORTRAIT = "portrait";
    String RENDITION_MODE_ORIGINAL = "original";

    default String getRenditionMode() {
        return getProperties().get("renditionMode", "landscape");
    }

    default String getFileReference() {
        return getProperties().get("fileReference", String.class);
    }

    default String getFileReferenceTablet() {
        String value = getProperties().get("fileReferenceTablet", String.class);
        value = value == null ? getFileReference() : value;

        return value;
    }

    default String getFileReferenceMobile() {
        String value = getProperties().get("fileReferenceMobile", String.class);
        value = value == null ? getFileReferenceTablet() : value;
        value = value == null ? getFileReference() : value;

        return value;
    }

    default int getMaxWidthMobile() {
        return TABLET_MIN - 1;
    }

    default int getMinWidthTablet() {
        return TABLET_MIN;
    }

    default int getMaxWidthTablet() {
        return DESKTOP_MIN - 1;
    }

    default int getMinWidthDesktop() {
        return DESKTOP_MIN;
    }

    default int getMinWidthDesktopLg() {
        return DESKTOP_XL_MIN;
    }

    /*
        Component classes should cache this return value.
     */
    default List<Rendition> getRenditions() {
        if (getRenditionMode() == null) {
            return Collections.emptyList();
        }

        List<Rendition> renditions = new ArrayList<>();
        switch (getRenditionMode()) {
            case RENDITION_MODE_LANDSCAPE:
                // DESKTOP LARGE
                renditions.add(new Rendition(
                    "(min-width: " + DESKTOP_XL_MIN + "px)",
                    ImageRendition.LANDSCAPE_X2.getRendition(getFileReference()) + " 1x, " +
                        ImageRendition.LANDSCAPE_X2.getRendition(getFileReference()) + " 2x"
                ));
                // DESKTOP
                renditions.add(new Rendition(
                    "(min-width: " + DESKTOP_MIN + "px) and (max-width: " + (DESKTOP_XL_MIN - 1) + "px)",
                    ImageRendition.LANDSCAPE_X1.getRendition(getFileReference()) + " 1x, " +
                        ImageRendition.LANDSCAPE_X2.getRendition(getFileReference()) + " 2x"
                ));
                // TABLET
                renditions.add(new Rendition(
                    "(min-width: " + TABLET_MIN + "px) and (max-width: " + (DESKTOP_MIN - 1) + "px)",
                    ImageRendition.PORTRAIT_X2.getRendition(getFileReferenceTablet()) + " 1x, " +
                        ImageRendition.LANDSCAPE_X1.getRendition(getFileReferenceTablet()) + " 2x"
                ));
                // MOBILE
                renditions.add(new Rendition(
                    "(max-width: " + (TABLET_MIN - 1) + "px)",
                    ImageRendition.PORTRAIT_X1.getRendition(getFileReferenceMobile()) + " 1x, " +
                        ImageRendition.PORTRAIT_X2.getRendition(getFileReferenceMobile()) + " 2x"
                ));
                break;
            case RENDITION_MODE_PORTRAIT:
                // DESKTOP
                renditions.add(new Rendition(
                    "(min-width: " + DESKTOP_MIN + "px)",
                    ImageRendition.PORTRAIT_X2.getRendition(getFileReference()) + " 1x, " +
                        ImageRendition.PORTRAIT_X2.getRendition(getFileReference()) + " 2x"
                ));
                // TABLET
                renditions.add(new Rendition(
                    "(min-width: " + TABLET_MIN + "px) and (max-width: " + (DESKTOP_MIN - 1) + "px)",
                    ImageRendition.PORTRAIT_X2.getRendition(getFileReferenceTablet()) + " 1x, " +
                        ImageRendition.PORTRAIT_X2.getRendition(getFileReferenceTablet()) + " 2x"
                ));
                // MOBILE
                renditions.add(new Rendition(
                    "(max-width: " + (TABLET_MIN - 1) + "px)",
                    ImageRendition.PORTRAIT_X1.getRendition(getFileReferenceMobile()) + " 1x, " +
                        ImageRendition.PORTRAIT_X2.getRendition(getFileReferenceMobile()) + " 2x"
                ));
                break;
            case RENDITION_MODE_ORIGINAL:
                // DESKTOP
                renditions.add(new Rendition(
                    "(min-width: " + DESKTOP_MIN + "px)",
                    ImageRendition.ORIGINAL.getRendition(getFileReference()) + " 1x, " +
                        ImageRendition.ORIGINAL.getRendition(getFileReference()) + " 2x"
                ));
                // TABLET
                renditions.add(new Rendition(
                    "(min-width: " + TABLET_MIN + "px) and (max-width: " + (DESKTOP_MIN - 1) + "px)",
                    ImageRendition.ORIGINAL.getRendition(getFileReferenceTablet()) + " 1x, " +
                        ImageRendition.ORIGINAL.getRendition(getFileReferenceTablet()) + " 2x"
                ));
                // MOBILE
                renditions.add(new Rendition(
                    "(max-width: " + (TABLET_MIN - 1) + "px)",
                    ImageRendition.ORIGINAL.getRendition(getFileReferenceMobile()) + " 1x, " +
                        ImageRendition.ORIGINAL.getRendition(getFileReferenceMobile()) + " 2x"
                ));
                break;
            default:
                getLogger().warn("Unrecognized renditionMode {} at {}", getRenditionMode());

        }

        return renditions;
    }

    /*
        Component classes should cache this return value.
     */
    default Map<String, String> getAllRenditions() {
        if (getFileReference() == null && getFileReferenceTablet() == null && getFileReferenceMobile() == null) {
            return Collections.emptyMap();
        }

        Map<String, String> renditions = new HashMap<>();

        for (ImageRendition rendition : ImageRendition.values()) {
            renditions.put("DESKTOP_" + rendition, rendition.getRendition(getFileReference()));
        }
        for (ImageRendition rendition : ImageRendition.values()) {
            renditions.put("TABLET_" + rendition, rendition.getRendition(getFileReferenceTablet()));
        }
        for (ImageRendition rendition : ImageRendition.values()) {
            renditions.put("MOBILE_" + rendition, rendition.getRendition(getFileReferenceMobile()));
        }

        return renditions;
    }

    default String getImageAlt() {
        String value = getProperties().get("imageAlt", String.class);

        if (value == null) {
            value = getAsset().getMetadataValue(DamConstants.DC_DESCRIPTION);
        }

        return value;
    }

    default String getImageTitle() {
        String value = getProperties().get("imageTitle", String.class);

        if (value == null) {
            value = getAsset().getMetadataValue(DamConstants.DC_TITLE);
        }

        return value;
    }

    @JsonIgnore
    default Asset getAsset() {
        Resource imageResource = getResourceResolver().getResource(getFileReference());
        if (imageResource != null) {
            return  imageResource.adaptTo(Asset.class);
        }
        return null;
    }

}
