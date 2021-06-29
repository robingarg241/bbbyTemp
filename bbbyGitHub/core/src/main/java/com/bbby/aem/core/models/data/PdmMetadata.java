
package com.bbby.aem.core.models.data;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class PdmMetadata {

    @SerializedName("skus")
    @Expose
    private List<Sku> skus = null;

    public List<Sku> getSkus() {
        return skus;
    }

    public void setSkus(List<Sku> skus) {
        this.skus = skus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("skus", skus).toString();
    }

}
