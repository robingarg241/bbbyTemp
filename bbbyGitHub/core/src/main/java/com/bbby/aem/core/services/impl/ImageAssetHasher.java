package com.bbby.aem.core.services.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.models.data.AssetHash;
import com.bbby.aem.core.services.AssetHasher;
import com.bbby.aem.core.util.CommonConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;

public class ImageAssetHasher implements AssetHasher {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public AssetHash buildAssetHash(Resource assetResource) {

    // vv: removing color space and image related attributes from dup check
        AssetHash assetHash = null;
  //      BufferedImage image = null;
  //      String colorSpace = "";

        if (assetResource == null) {
            return null;
        }
        
        Asset asset = assetResource.adaptTo(Asset.class);

        //InputStream stream = asset.getOriginal().getStream();
/*
        try {
            image = ImageIO.read(stream);

            ColorSpace cs = image.getColorModel().getColorSpace();
            colorSpace = AssetHasherUtils.getColorSpaceName(cs.getType());
            stream.close();

        } catch (IOException e) {
            log.error("Error building hash for {}", assetResource.getPath());
            return null;
        }
*/
        // File Name
        String fileName = StringUtils.isNotBlank(asset.getMetadataValue(CommonConstants.BBBY_UPLOADED_ASSET_NAME)) ? asset.getMetadataValue(CommonConstants.BBBY_UPLOADED_ASSET_NAME) : null;

        if (fileName == null) {

            Path p = Paths.get(assetResource.getPath());
            fileName = p.getFileName().toString();

        }

        //there may or may not be a DIP, default is -1
        Long dpi = asset.getMetadata("dam:Physicalheightindpi") != null ? (Long) asset.getMetadata("dam:Physicalheightindpi") : -1L;

        String fileFormat = StringUtils.isNotBlank(asset.getMetadataValue("dam:Fileformat")) ? asset.getMetadataValue("dam:Fileformat") : "";

//        Long width = (long) image.getWidth();
//        Long height = (long) image.getHeight();

        Long fileSize = asset.getMetadata(DamConstants.DAM_SIZE) != null ? (Long) asset.getMetadata(DamConstants.DAM_SIZE) : 0;

//        long gcd = AssetHasherUtils.gcd(width, height);

//        Long aspectWidth = width / gcd;
//        Long aspectHeight = height / gcd;

//        assetHash = new AssetHash(fileName,
//            fileFormat,
//            fileSize.toString(),
//            width.toString() + "x" + height.toString(),
//            aspectWidth.toString() + "x" + aspectHeight.toString(),
//            colorSpace,
//            dpi.toString());
        
        assetHash = new AssetHash(fileName,
                fileFormat,
                fileSize.toString(),
                "",
                "",
                "",
                dpi.toString());

        return assetHash;
    }
}
