package com.hero.jcr.commandline;

public class PDMAssetBean {
	
	String include_in_migration;
	String fullpath;
	String fullname;
	String primaryUPC;
	String sku;
	String brand;
	String colorCode;
	String colorGroupCode;
	String product;
	String associatedWebProductID;
	String associatedCollectionID;
	String primaryVendorNumber;
	String primaryVendorName;
	String vendorDirectToCustomerItem;
	String priorityFlag;
	String bbbyWebDisabled;
	String babyWebDisabled;
	String caWebDisabled;
	String bbbyWebOfferedFlag;
	String babyWebOfferedFlag;
	String caWebOfferedFlag;
	String webProductRollupType;
	String masterProductDescription;
	String fastTrackFlag;
	String pullbackToMerchant;
	String pdmBatchID;
	
	final int INCLUDE_IN_MIGRATION = 0;
	final int FULLPATH = 1;
	final int FULLNAME = 2;
	final int PRIMARYUPC = 3;
	final int SKU = 4;
	final int BRAND = 5;
	final int COLORCODE = 6;
	final int COLORGROUPCODE = 7;
	final int PRODUCT = 8;
	final int ASSOCIATEDWEBPRODUCTID = 9;
	final int ASSOCIATEDCOLLECTIONID = 10;
	final int PRIMARYVENDORNUMBER = 11;
	final int PRIMARYVENDORNAME = 12;
	final int VENDORDIRECTTOCUSTOMERITEM = 13;
	final int PRIORITYFLAG = 14;
	final int BBBYWEBDISABLED = 15;
	final int BABYWEBDISABLED = 16;
	final int CAWEBDISABLED = 17;
	final int BBBYWEBOFFEREDFLAG = 18;
	final int BABYWEBOFFEREDFLAG = 19;
	final int CAWEBOFFEREDFLAG = 20;
	final int WEBPRODUCTROLLUPTYPE = 21;
	final int MASTERPRODUCTDESCRIPTION = 22;
	final int FASTTRACKFLAG = 23;
	final int PULLBACKTOMERCHANT = 24;
	final int PDMBATCHID = 25;
	final int DUMMY = 26;
	
	public PDMAssetBean(AssetBean ab) {
		
		include_in_migration = ab.getInclude_in_migration();
		fullpath = ab.getAsset_Type_Level_4();
		fullname = ab.getFullname();
		primaryUPC = ab.getPrimaryUPC();
		sku = ab.getSku();
		brand = ab.getBrand();
		colorCode = ab.getColorCode();
		colorGroupCode = ab.getColorGroupCode();
		product = ab.getProduct();
		associatedWebProductID = ab.getAssociatedWebProductID();
		associatedCollectionID = ab.getAssociatedCollectionID();
		primaryVendorNumber = ab.getPrimaryVendorNumber();
		primaryVendorName = ab.getPrimaryVendorName();
		priorityFlag = ab.getPriorityFlag();
		bbbyWebOfferedFlag = ab.getBbbyWebOfferedFlag();
		bbbyWebDisabled = ab.getBbbyWebDisabled();
		babyWebDisabled = ab.getBabyWebDisabled();
		caWebDisabled = ab.getCaWebDisabled();
		bbbyWebOfferedFlag = ab.getBbbyWebOfferedFlag();
		babyWebOfferedFlag = ab.getBabyWebOfferedFlag();
		caWebOfferedFlag = ab.getCaWebOfferedFlag();
		webProductRollupType = ab.getWebProductRollupType();
		masterProductDescription = ab.getMasterProductDescription();
		fastTrackFlag = ab.getFastTrackFlag();
		pullbackToMerchant = ab.getPullbackToMerchant();
		vendorDirectToCustomerItem = ab.getVendorDirectToCustomerItem();
		pdmBatchID = ab.getPdmBatchID();
		
	}
	
	public PDMAssetBean(String[] columns) {
		
		include_in_migration = columns[INCLUDE_IN_MIGRATION];
		fullpath = columns[FULLPATH];
		fullname = columns[FULLNAME];
		primaryUPC = columns[PRIMARYUPC];
		sku = columns[SKU];
		brand = columns[BRAND];
		colorCode = columns[COLORCODE];
		colorGroupCode = columns[COLORGROUPCODE];
		product = columns[PRODUCT];
		associatedWebProductID = columns[ASSOCIATEDWEBPRODUCTID];
		associatedCollectionID = columns[ASSOCIATEDCOLLECTIONID];
		primaryVendorNumber = columns[PRIMARYVENDORNUMBER];
		primaryVendorName = columns[PRIMARYVENDORNAME];
		priorityFlag = columns[PRIORITYFLAG];
		bbbyWebOfferedFlag = columns[BBBYWEBOFFEREDFLAG];
		bbbyWebDisabled = columns[BBBYWEBDISABLED];
		babyWebDisabled = columns[BABYWEBDISABLED];
		caWebDisabled = columns[CAWEBDISABLED];
		bbbyWebOfferedFlag = columns[BBBYWEBOFFEREDFLAG];
		babyWebOfferedFlag = columns[BABYWEBOFFEREDFLAG];
		caWebOfferedFlag = columns[CAWEBOFFEREDFLAG];
		webProductRollupType = columns[WEBPRODUCTROLLUPTYPE];
		masterProductDescription = columns[MASTERPRODUCTDESCRIPTION];
		fastTrackFlag = columns[FASTTRACKFLAG];
		pullbackToMerchant = columns[PULLBACKTOMERCHANT];
		vendorDirectToCustomerItem = columns[VENDORDIRECTTOCUSTOMERITEM];
		pdmBatchID = columns[PDMBATCHID];
		
	}	
	
	public PDMAssetBean(String line) {
		
		String[] columns = line.split("\t");
		
		include_in_migration = columns[INCLUDE_IN_MIGRATION];
		fullpath = columns[FULLPATH];
		fullname = columns[FULLNAME];
		primaryUPC = columns[PRIMARYUPC];
		sku = columns[SKU];
		brand = columns[BRAND];
		colorCode = columns[COLORCODE];
		colorGroupCode = columns[COLORGROUPCODE];
		product = columns[PRODUCT];
		associatedWebProductID = columns[ASSOCIATEDWEBPRODUCTID];
		associatedCollectionID = columns[ASSOCIATEDCOLLECTIONID];
		primaryVendorNumber = columns[PRIMARYVENDORNUMBER];
		primaryVendorName = columns[PRIMARYVENDORNAME];
		priorityFlag = columns[PRIORITYFLAG];
		bbbyWebOfferedFlag = columns[BBBYWEBOFFEREDFLAG];
		bbbyWebDisabled = columns[BBBYWEBDISABLED];
		babyWebDisabled = columns[BABYWEBDISABLED];
		caWebDisabled = columns[CAWEBDISABLED];
		bbbyWebOfferedFlag = columns[BBBYWEBOFFEREDFLAG];
		babyWebOfferedFlag = columns[BABYWEBOFFEREDFLAG];
		caWebOfferedFlag = columns[CAWEBOFFEREDFLAG];
		webProductRollupType = columns[WEBPRODUCTROLLUPTYPE];
		masterProductDescription = columns[MASTERPRODUCTDESCRIPTION];
		fastTrackFlag = columns[FASTTRACKFLAG];
		pullbackToMerchant = columns[PULLBACKTOMERCHANT];
		vendorDirectToCustomerItem = columns[VENDORDIRECTTOCUSTOMERITEM];
		pdmBatchID = columns[PDMBATCHID];
		
	}		
	
	public String getFormattedOutput() {
		
		String output = include_in_migration + "\t" +
				fullpath + "\t" +
				fullname + "\t" +
				primaryUPC + "\t" +
				sku + "\t" +
				brand + "\t" +
				colorCode + "\t" +
				colorGroupCode + "\t" +
				product + "\t" +
				associatedWebProductID + "\t" +
				associatedCollectionID + "\t" +
				primaryVendorNumber + "\t" +
				primaryVendorName + "\t" +
				vendorDirectToCustomerItem + "\t" +
				priorityFlag + "\t" +
				bbbyWebDisabled + "\t" +
				babyWebDisabled + "\t" +
				caWebDisabled + "\t" +
				bbbyWebOfferedFlag + "\t" +
				babyWebOfferedFlag + "\t" +
				caWebOfferedFlag + "\t" +
				webProductRollupType + "\t" +
				masterProductDescription + "\t" +
				fastTrackFlag + "\t" +
				pullbackToMerchant + "\t" +
				pdmBatchID + "\t" +
				"dummy\n";
		
		return output;
		
	}
	
	
	public String getInclude_in_migration() {
		return include_in_migration;
	}
	public void setInclude_in_migration(String include_in_migration) {
		this.include_in_migration = include_in_migration;
	}
	public String getFullpath() {
		return fullpath;
	}
	public void setFullpath(String fullpath) {
		this.fullpath = fullpath;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
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
	public String getWebProductRollupType() {
		return webProductRollupType;
	}
	public void setWebProductRollupType(String webProductRollupType) {
		this.webProductRollupType = webProductRollupType;
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

	public String getPdmBatchID() {
		return pdmBatchID;
	}

	public void setPdmBatchID(String pdmBatchID) {
		this.pdmBatchID = pdmBatchID;
	}	
	

}
