package com.bbby.aem.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Canonical record of all PNG renditions supported by this site.
 *
 * @author joelepps
 *
 */
public enum ImageRenditionPng {

    PORTRAIT_X2(1200, 1200),
    PORTRAIT_X1(600, 600),

    ORIGINAL(0, 0);

    private final int width;
    private final int height;

    ImageRenditionPng(int width, int height) {
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
        // always png
        return imagePath + ".imgw." + width + "." + height + "." + "png";
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
