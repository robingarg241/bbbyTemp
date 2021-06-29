package com.bbby.aem.core.models.data;

import org.apache.commons.lang.StringUtils;

public class AssetHash {

    private String fileName;
    private String fileType;
    private String fileSize;
    private String dimensions;
    private String aspectRatio;
    private String colorSpace;
    private String assetHash;
    private String dpi;

    public AssetHash(String fileName,
                     String fileType,
                     String fileSize,
                     String dimensions,
                     String aspectRatio,
                     String colorSpace,
                     String dpi) {

        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.dimensions = dimensions;
        this.aspectRatio = aspectRatio;
        this.colorSpace = colorSpace;
        this.dpi = dpi;

    }

    public AssetHash(String fileName,
                     String fileType,
                     String fileSize) {

        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;

    }

    public String getAssetHash() {

        StringBuilder data = new StringBuilder();
        if (StringUtils.isNotEmpty(fileName)) {
            data.append(fileName + "|");
        }

        if (StringUtils.isNotEmpty(fileType)) {
            data.append(fileType + "|");
        }

        if (StringUtils.isNotEmpty(fileSize)) {
            data.append(fileSize + "|");
        }

        if (StringUtils.isNotEmpty(dimensions)) {
            data.append(dimensions + "|");
        }

        if (StringUtils.isNotEmpty(aspectRatio)) {
            data.append(aspectRatio + "|");
        }

        if (StringUtils.isNotEmpty(colorSpace)) {
            data.append(colorSpace + "|");
        }

        if (StringUtils.isNotEmpty(dpi)) {
            data.append(dpi + "|");
        }

        return data.toString();
    }

    public String getAssetHashCode() {
        return new Integer(getAssetHash().hashCode()).toString();
    }
}
