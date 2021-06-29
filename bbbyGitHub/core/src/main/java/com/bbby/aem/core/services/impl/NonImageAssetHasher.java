package com.bbby.aem.core.services.impl;

import com.bbby.aem.core.models.data.AssetHash;
import com.bbby.aem.core.services.AssetHasher;
import com.bbby.aem.core.util.CommonConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;

public class NonImageAssetHasher implements AssetHasher {
    @Override
    public AssetHash buildAssetHash(Resource assetResource) {
        AssetHash assetHash = null;

        if (assetResource == null) {
            return null;
        }

        Asset asset = assetResource.adaptTo(Asset.class);

        // File Name
        String fileName = StringUtils.isNotBlank(asset.getMetadataValue(CommonConstants.BBBY_UPLOADED_ASSET_NAME)) ? asset.getMetadataValue(CommonConstants.BBBY_UPLOADED_ASSET_NAME) : null;

        if (fileName == null) {

            Path p = Paths.get(assetResource.getPath());
            fileName = p.getFileName().toString();

        }

        String fileFormat = StringUtils.isNotBlank(asset.getMetadataValue("dam:Fileformat")) ? asset.getMetadataValue("dam:Fileformat") : "";

        Long fileSize = asset.getMetadata(DamConstants.DAM_SIZE) != null ? (Long) asset.getMetadata(DamConstants.DAM_SIZE) : 0;

        assetHash = new AssetHash(fileName, fileFormat, fileSize.toString());

        return assetHash;
    }
}
