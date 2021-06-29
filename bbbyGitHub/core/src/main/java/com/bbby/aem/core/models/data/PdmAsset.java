
package com.bbby.aem.core.models.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class PdmAsset {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("bbbyAssetMetadata")
    @Expose
    private BbbyAssetMetadata bbbyAssetMetadata;
    @SerializedName("pdmMetadata")
    @Expose
    private PdmMetadata pdmMetadata;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BbbyAssetMetadata getBbbyAssetMetadata() {
		return bbbyAssetMetadata;
	}

	public void setBbbyAssetMetadata(BbbyAssetMetadata bbbyAssetMetadata) {
		this.bbbyAssetMetadata = bbbyAssetMetadata;
	}

	public PdmMetadata getPdmMetadata() {
        return pdmMetadata;
    }

    public void setPdmMetadata(PdmMetadata pdmMetadata) {
        this.pdmMetadata = pdmMetadata;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("uuid", uuid).append("bbbyAssetMetadata", bbbyAssetMetadata).append("pdmMetadata", pdmMetadata).toString();
    }

}
