package com.bbby.aem.core.services;

import com.bbby.aem.core.models.data.AssetHash;
import org.apache.sling.api.resource.Resource;

public interface AssetHasher {
    AssetHash buildAssetHash(Resource assetResource);
}
