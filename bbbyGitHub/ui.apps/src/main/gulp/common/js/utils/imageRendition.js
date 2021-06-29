'use strict';

/**
 * Module is the JS equivalent of com.bbby.aem.core.util.ImageRendition
 *
 * Provides utility methods to convert an image path to one with rendition selectors.
 *
 * Any changes to the Java or JavaScript version of this file should be propagated to both.
 */

export default {
    LANDSCAPE_X2: new Rendition(3600, 3600),
    LANDSCAPE_X1: new Rendition(1920, 1920),
    PORTRAIT_X2: new Rendition(1200, 1200),
    PORTRAIT_X1: new Rendition(600, 600),

    ORIGINAL: new Rendition(0, 0)
}

function Rendition(width, height) {
    this.width = width;
    this.height = height;
}

Rendition.prototype.getRendition = function(imagePath) {
    if (!imagePath) return imagePath;

    if (this.width == 0 || this.height == 0) {
        var ext = getExtension(imagePath);
        return imagePath + '.imgo.' + ext;
    }

    // web images should always be jpg extension
    return imagePath + ".imgw." + this.width + "." + this.height + "." + "jpg";
};

function getExtension(imagePath) {
    if (imagePath && imagePath.indexOf(".png") > 0) {
        return "png";
    } else if (imagePath && imagePath.indexOf(".jpg") > 0) {
        return "jpg";
    } else if (imagePath && imagePath.indexOf(".svg") > 0) {
        return "svg";
    } else {
        return "jpg"; // default
    }
}
