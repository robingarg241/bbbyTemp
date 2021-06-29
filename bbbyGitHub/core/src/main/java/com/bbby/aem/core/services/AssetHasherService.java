package com.bbby.aem.core.services;

import com.bbby.aem.core.models.data.AssetHash;
import org.apache.sling.api.resource.ResourceResolver;

public interface AssetHasherService {
    AssetHash createAssetHash(ResourceResolver resourceResolver, String assetPath) throws Exception;
}
