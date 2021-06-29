package com.bbby.aem.core.models.component.traits.image;

public class Rendition {
    private final String media;
    private final String srcset;

    public Rendition(String media, String srcset) {
        this.media = media;
        this.srcset = srcset;
    }

    public String getMedia() {
        return media;
    }

    public String getSrcset() {
        return srcset;
    }
}
