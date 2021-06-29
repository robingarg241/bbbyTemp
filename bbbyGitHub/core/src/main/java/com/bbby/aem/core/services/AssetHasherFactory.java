package com.bbby.aem.core.services;

import com.bbby.aem.core.services.impl.ImageAssetHasher;
import com.bbby.aem.core.services.impl.NonImageAssetHasher;
import org.apache.sling.api.resource.Resource;

public class AssetHasherFactory {


    public static AssetHasher createAssetHasher(Resource assetResource, String mimeType) {
        if (mimeType.contentEquals("image/jpeg") || mimeType.contentEquals("image/png") || mimeType.contentEquals("image/tiff")) {
            return new ImageAssetHasher();
        } else {
            return new NonImageAssetHasher();
        }
    }
}
