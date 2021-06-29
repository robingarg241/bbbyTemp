
package com.bbby.aem.core.models.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Sku {

    @SerializedName("primaryUPC")
    @Expose
    private String primaryUPC;
    @SerializedName("sku")
    @Expose
    private String sku;
    @SerializedName("brand")
    @Expose
    private String brand;
    @SerializedName("colorCode")
    @Expose
    private String colorCode;
    @SerializedName("colorGroupCode")
    @Expose
    private String colorGroupCode;
    @SerializedName("product")
    @Expose
    private String product;
    @SerializedName("associatedWebProductID")
    @Expose
    private String associatedWebProductID;
    @SerializedName("associatedCollectionID")
    @Expose
    private String associatedCollectionID;
    @SerializedName("pdmBatchID")
    @Expose
    private String pdmBatchID;
    @SerializedName("primaryVendorNumber")
    @Expose
    private String primaryVendorNumber;
    @SerializedName("primaryVendorName")
    @Expose
    private String primaryVendorName;
    @SerializedName("vendorDirectToCustomerItem")
    @Expose
    private String vendorDirectToCustomerItem;
    @SerializedName("priorityFlag")
    @Expose
    private String priorityFlag;
    @SerializedName("bbbyWebDisabled")
    @Expose
    private String bbbyWebDisabled;
    @SerializedName("babyWebDisabled")
    @Expose
    private String babyWebDisabled;
    @SerializedName("caWebDisabled")
    @Expose
    private String caWebDisabled;
    @SerializedName("bbbyWebOfferedFlag")
    @Expose
    private String bbbyWebOfferedFlag;
    @SerializedName("babyWebOfferedFlag")
    @Expose
    private String babyWebOfferedFlag;
    @SerializedName("caWebOfferedFlag")
    @Expose
    private String caWebOfferedFlag;
    @SerializedName("webProductRollUpType")
    @Expose
    private String webProductRollUpType;
    @SerializedName("masterProductDescription")
    @Expose
    private String masterProductDescription;
    @SerializedName("fastTrackFlag")
    @Expose
    private Object fastTrackFlag;
    @SerializedName("pullbackToMerchant")
    @Expose
    private String pullbackToMerchant;

    public String getPrimaryUPC() {
        return primaryUPC;
    }

    public void setPrimaryUPC(String primaryUPC) {
        this.primaryUPC = primaryUPC;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorGroupCode() {
        return colorGroupCode;
    }

    public void setColorGroupCode(String colorGroupCode) {
        this.colorGroupCode = colorGroupCode;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getAssociatedWebProductID() {
        return associatedWebProductID;
    }

    public void setAssociatedWebProductID(String associatedWebProductID) {
        this.associatedWebProductID = associatedWebProductID;
    }

    public String getAssociatedCollectionID() {
        return associatedCollectionID;
    }

    public void setAssociatedCollectionID(String associatedCollectionID) {
        this.associatedCollectionID = associatedCollectionID;
    }
    
    public String getPdmBatchID() {
        return pdmBatchID;
    }

    public void setPdmBatchID(String pdmBatchID) {
        this.pdmBatchID = pdmBatchID;
    }

    public String getPrimaryVendorNumber() {
        return primaryVendorNumber;
    }

    public void setPrimaryVendorNumber(String primaryVendorNumber) {
        this.primaryVendorNumber = primaryVendorNumber;
    }

    public String getPrimaryVendorName() {
        return primaryVendorName;
    }

    public void setPrimaryVendorName(String primaryVendorName) {
        this.primaryVendorName = primaryVendorName;
    }

    public String getVendorDirectToCustomerItem() {
        return vendorDirectToCustomerItem;
    }

    public void setVendorDirectToCustomerItem(String vendorDirectToCustomerItem) {
        this.vendorDirectToCustomerItem = vendorDirectToCustomerItem;
    }

    public String getPriorityFlag() {
        return priorityFlag;
    }

    public void setPriorityFlag(String priorityFlag) {
        this.priorityFlag = priorityFlag;
    }

    public String getBbbyWebDisabled() {
        return bbbyWebDisabled;
    }

    public void setBbbyWebDisabled(String bbbyWebDisabled) {
        this.bbbyWebDisabled = bbbyWebDisabled;
    }

    public String getBabyWebDisabled() {
        return babyWebDisabled;
    }

    public void setBabyWebDisabled(String babyWebDisabled) {
        this.babyWebDisabled = babyWebDisabled;
    }

    public String getCaWebDisabled() {
        return caWebDisabled;
    }

    public void setCaWebDisabled(String caWebDisabled) {
        this.caWebDisabled = caWebDisabled;
    }

    public String getBbbyWebOfferedFlag() {
        return bbbyWebOfferedFlag;
    }

    public void setBbbyWebOfferedFlag(String bbbyWebOfferedFlag) {
        this.bbbyWebOfferedFlag = bbbyWebOfferedFlag;
    }

    public String getBabyWebOfferedFlag() {
        return babyWebOfferedFlag;
    }

    public void setBabyWebOfferedFlag(String babyWebOfferedFlag) {
        this.babyWebOfferedFlag = babyWebOfferedFlag;
    }

    public String getCaWebOfferedFlag() {
        return caWebOfferedFlag;
    }

    public void setCaWebOfferedFlag(String caWebOfferedFlag) {
        this.caWebOfferedFlag = caWebOfferedFlag;
    }

    public String getWebProductRollUpType() {
        return webProductRollUpType;
    }

    public void setWebProductRollUpType(String webProductRollUpType) {
        this.webProductRollUpType = webProductRollUpType;
    }

    public String getMasterProductDescription() {
        return masterProductDescription;
    }

    public void setMasterProductDescription(String masterProductDescription) {
        this.masterProductDescription = masterProductDescription;
    }

    public Object getFastTrackFlag() {
        return fastTrackFlag;
    }

    public void setFastTrackFlag(Object fastTrackFlag) {
        this.fastTrackFlag = fastTrackFlag;
    }

    public String getPullbackToMerchant() {
        return pullbackToMerchant;
    }

    public void setPullbackToMerchant(String pullbackToMerchant) {
        this.pullbackToMerchant = pullbackToMerchant;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("primaryUPC", primaryUPC).append("sku", sku).append("brand", brand).append("colorCode", colorCode).append("colorGroupCode", colorGroupCode).append("product", product).append("associatedWebProductID", associatedWebProductID).append("associatedCollectionID", associatedCollectionID).append("pdmBatchID", pdmBatchID).append("primaryVendorNumber", primaryVendorNumber).append("primaryVendorName", primaryVendorName).append("vendorDirectToCustomerItem", vendorDirectToCustomerItem).append("priorityFlag", priorityFlag).append("bbbyWebDisabled", bbbyWebDisabled).append("babyWebDisabled", babyWebDisabled).append("caWebDisabled", caWebDisabled).append("bbbyWebOfferedFlag", bbbyWebOfferedFlag).append("babyWebOfferedFlag", babyWebOfferedFlag).append("caWebOfferedFlag", caWebOfferedFlag).append("webProductRollUpType", webProductRollUpType).append("masterProductDescription", masterProductDescription).append("fastTrackFlag", fastTrackFlag).append("pullbackToMerchant", pullbackToMerchant).toString();
    }

}
