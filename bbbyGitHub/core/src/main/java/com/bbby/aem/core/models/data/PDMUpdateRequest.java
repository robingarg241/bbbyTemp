
package com.bbby.aem.core.models.data;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class PDMUpdateRequest {

    @SerializedName("total")
    @Expose
    private String total;
    @SerializedName("assets")
    @Expose
    private List<PdmAsset> assets = null;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<PdmAsset> getAssets() {
        return assets;
    }

    public void setAssets(List<PdmAsset> assets) {
        this.assets = assets;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("total", total).append("assets", assets).toString();
    }

}
