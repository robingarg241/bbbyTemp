package com.bbby.aem.core.models.data;

public class PDMMetadataModel {

    private String primaryUPC;

    private String sku;

    private String brand;

    private String colorCode;

    private String colorGroupCode;

    private String product;

    private String associatedWebProductID;

    private String associatedCollectionID;
    
    private String pdmBatchID;

    private String primaryVendorNumber;

    private String primaryVendorName;

    private String vendorDirectToCustomerItem;

    private String priorityFlag;

    private String bbbyWebDisabled;

    private String babyWebDisabled;

    private String caWebDisabled;

    private String bbbyWebOfferedFlag;

    private String babyWebOfferedFlag;

    private String caWebOfferedFlag;

    private String webProductRollUpType;

    private String masterProductDescription;

    private String fastTrackFlag;

    private String pullbackToMerchant;

    public PDMMetadataModel(){

    }

    public PDMMetadataModel(String primaryUPC, String sku, String brand, String colorCode, String colorGroupCode, String product, String associatedWebProductID, String associatedCollectionID, String pdmBatchID, String primaryVendorNumber, String primaryVendorName, String vendorDirectToCustomerItem, String priorityFlag, String bbbyWebDisabled, String babyWebDisabled, String caWebDisabled, String bbbyWebOfferedFlag, String babyWebOfferedFlag, String caWebOfferedFlag, String webProductRollUpType, String masterProductDescription, String fastTrackFlag, String pullbackToMerchant) {
        this.primaryUPC = primaryUPC;
        this.sku = sku;
        this.brand = brand;
        this.colorCode = colorCode;
        this.colorGroupCode = colorGroupCode;
        this.product = product;
        this.associatedWebProductID = associatedWebProductID;
        this.associatedCollectionID = associatedCollectionID;
        this.pdmBatchID = pdmBatchID;
        this.primaryVendorNumber = primaryVendorNumber;
        this.primaryVendorName = primaryVendorName;
        this.vendorDirectToCustomerItem = vendorDirectToCustomerItem;
        this.priorityFlag = priorityFlag;
        this.bbbyWebDisabled = bbbyWebDisabled;
        this.babyWebDisabled = babyWebDisabled;
        this.caWebDisabled = caWebDisabled;
        this.bbbyWebOfferedFlag = bbbyWebOfferedFlag;
        this.babyWebOfferedFlag = babyWebOfferedFlag;
        this.caWebOfferedFlag = caWebOfferedFlag;
        this.webProductRollUpType = webProductRollUpType;
        this.masterProductDescription = masterProductDescription;
        this.fastTrackFlag = fastTrackFlag;
        this.pullbackToMerchant = pullbackToMerchant;
    }

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

    public String getFastTrackFlag() {
        return fastTrackFlag;
    }

    public void setFastTrackFlag(String fastTrackFlag) {
        this.fastTrackFlag = fastTrackFlag;
    }

    public String getPullbackToMerchant() {
        return pullbackToMerchant;
    }

    public void setPullbackToMerchant(String pullbackToMerchant) {
        this.pullbackToMerchant = pullbackToMerchant;
    }
}
