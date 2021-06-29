package com.bbby.aem.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Canonical record of all web/JPEG renditions supported by this site.
 * <p>
 * The JavaScript version of this file is
 * {@code /apps/bbby/components/content/image/imageRendition.js}
 * <p>
 * Any changes to the Java or JavaScript version of this file should be propagated to both.
 *
 * @author joelepps
 *
 */
public enum ImageRendition {

    LANDSCAPE_X2(3600, 3600),
    LANDSCAPE_X1(1920, 1920),
    PORTRAIT_X2(1200, 1200),
    PORTRAIT_X1(600, 600),

    ORIGINAL(0, 0);

    private final int width;
    private final int height;

    ImageRendition(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public String getRendition(String imagePath) {
        if (StringUtils.isBlank(imagePath)) return imagePath;
        if (imagePath.contains("://")) return imagePath;

        if (this == ORIGINAL) {
            String ext = getExtension(imagePath);
            return imagePath + ".imgo." + ext;
        }
        // web images should always be jpg extension
        return imagePath + ".imgw." + width + "." + height + "." + "jpg";
    }

    private String getExtension(String imagePath) {
        if (imagePath != null && imagePath.contains(".png")) {
            return "png";
        } else if (imagePath != null && imagePath.contains(".jpg")) {
            return "jpg";
        } else if (imagePath != null && imagePath.contains(".svg")) {
            return "svg";
        } else {
            return "jpg"; // default
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
