package com.bbby.aem.core.services.impl;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.models.data.AssetHash;
import com.bbby.aem.core.services.AssetHasher;
import com.bbby.aem.core.services.AssetHasherFactory;
import com.bbby.aem.core.services.AssetHasherService;
import com.bbby.aem.core.util.CommonConstants;
import com.day.cq.dam.api.Asset;

@Component(
    immediate = true,
    service = AssetHasherService.class)
public class AssetHasherServiceImpl implements AssetHasherService {


    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public AssetHash createAssetHash(ResourceResolver resourceResolver, String assetPath) throws Exception {

        //nip this in the bud we are only checking for duplicates for assets in these three folders
    	if ( !assetPath.contains("bbby/asset_transitions_folder/vendor/vendor_assets_holding") ) {
            return null;
        }
        AssetHash assetHash = null;

        Resource assetResource = resourceResolver.getResource(assetPath);

        if (assetResource == null) {
            log.warn("Unable to locate asset ");
            return null;
        }
        Asset asset = assetResource.adaptTo(Asset.class);

        // Skip if assetHash is already set
        if (StringUtils.isNotBlank(asset.getMetadataValue(CommonConstants.BBBY_ASSET_HASH))) {
            log.warn("Asset hash already set, skipping");
            return null;
        }

        Node assetNode = assetResource.adaptTo(Node.class);

        String mimeType = asset.getMimeType();

        AssetHasher assetHasher = AssetHasherFactory.createAssetHasher(assetResource, mimeType);

        assetHash = assetHasher.buildAssetHash(assetResource);

//            if (mimeType.contentEquals("image/jpeg") || mimeType.contentEquals("image/png") || mimeType.contentEquals("image/tiff")) {
//
//                hash = buildImageHash(assetResource);
//
//            } else {
//
//                hash = buildNonImageHash(assetResource);
//
//            }

        if (assetHash == null) {
            log.warn("Unable to create asset hash value for {} ", assetResource.getPath());
            return null;
        }

        log.debug("Hash code for {} is {}", asset.getPath(), assetHash.toString());

        /*Node metadataNode = assetNode.getNode(CommonConstants.METADATA_NODE);
        metadataNode.setProperty(CommonConstants.BBBY_ASSET_HASH, assetHash.getAssetHashCode());
        metadataNode.setProperty(CommonConstants.BBBY_ASSET_HASH_SEED, assetHash.getAssetHash());*/

        return assetHash;

//            boolean duplicate = checkForDups(session, assetNode, assetHash);
//
//            workflowData.getMetaDataMap().put("duplicate.asset", duplicate);

    }
}
