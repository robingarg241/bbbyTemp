package com.hero.jcr.commandline;

/*
 * Utility class to unpack line from input file into model bean
 */

public class AssetBean {
	
    final int INCLUDE_IN_MIGRATION = 0;
    final int FULLPATH = 1;
    final int FULLNAME = 2;
    final int UPC = 3;
    final int COLLECTION = 4;
    final int CONCEPT = 5;
    final int ASSET_USE = 6;
    final int CHANNEL = 7;
    final int ASSET_SOURCE = 8;
    final int ASSET_TYPE_LEVEL_4 = 9;
    final int ASSET_TYPE_PRODUCT = 10;
    final int SHOT_TYPE = 11;
    final int SEQUENCING = 12;
    final int HOLIDAY = 13;
    final int ENVIRONMENT = 14;
    final int PRIMARYUPC = 15;
    final int SKU = 16;
    final int BRAND = 17;
    final int COLORCODE = 18;
    final int COLORGROUPCODE = 19;
    final int PRODUCT = 20;
    final int ASSOCIATEDWEBPRODUCTID = 21;
    final int ASSOCIATEDCOLLECTIONID = 22;
    final int DEPARTMENTNAME = 23;
    final int DEPARTMENTNUMBER = 24;
    final int PRIMARYVENDORNUMBER = 25;
    final int PRIMARYVENDORNAME = 26;
    final int VENDORDIRECTTOCUSTOMERITEM = 27;
    final int PRIORITYFLAG = 28;
    final int BBBYWEBDISABLED = 29;
    final int BABYWEBDISABLED = 30;
    final int CAWEBDISABLED = 31;
    final int BBBYWEBOFFEREDFLAG = 32;
    final int BABYWEBOFFEREDFLAG = 33;
    final int CAWEBOFFEREDFLAG = 34;
    final int WEBPRODUCTROLLUPTYPE = 35;
    final int MASTERPRODUCTDESCRIPTION = 36;
    final int FASTTRACKFLAG = 37;
    final int PULLBACKTOMERCHANT = 38;
    final int PDMBATCHID = 39;
    final int SWATCHOVERRIDENAME = 40;
    final int PRIMARYIMAGE = 41;
    final int DUMMY = 42;
	
	String include_in_migration;
	String fullpath;
	String fullname;
	String uPC;
	String collection;
	String concept;
	String asset_Use;
	String channel;
	String asset_Source;
	String asset_Type_Level_4;
	String asset_Type_Product;
	String shot_Type;
	String sequencing;
	String holiday;
	String environment;
	String primaryUPC;
	String sku;
	String brand;
	String colorCode;
	String colorGroupCode;
	String product;
	String associatedWebProductID;
	String associatedCollectionID;
	String departmentName;
	String departmentNumber;
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
	String swatchOverrideName;
	String primaryImage;
	String dummy;


	
	public AssetBean(String[] columns){
		
		include_in_migration = columns[INCLUDE_IN_MIGRATION];
		fullpath = columns[FULLPATH];
		fullname = columns[FULLNAME];
		uPC = columns[UPC];
		collection = columns[COLLECTION];
		concept = columns[CONCEPT];
		asset_Use = columns[ASSET_USE];
		channel = columns[CHANNEL];
		asset_Source = columns[ASSET_SOURCE];
		asset_Type_Level_4 = columns[ASSET_TYPE_LEVEL_4];
		asset_Type_Product = columns[ASSET_TYPE_PRODUCT];
		shot_Type = columns[SHOT_TYPE];
		sequencing = columns[SEQUENCING];
		holiday = columns[HOLIDAY];
		environment = columns[ENVIRONMENT];
		primaryUPC = columns[PRIMARYUPC];
		sku = columns[SKU];
		brand = columns[BRAND];
		colorCode = columns[COLORCODE];
		colorGroupCode = columns[COLORGROUPCODE];
		product = columns[PRODUCT];
		associatedWebProductID = columns[ASSOCIATEDWEBPRODUCTID];
		associatedCollectionID = columns[ASSOCIATEDCOLLECTIONID];
		departmentName = columns[DEPARTMENTNAME];
		departmentNumber = columns[DEPARTMENTNUMBER];
		primaryVendorNumber = columns[PRIMARYVENDORNUMBER];
		primaryVendorName = columns[PRIMARYVENDORNAME];
		vendorDirectToCustomerItem = columns[VENDORDIRECTTOCUSTOMERITEM];
		priorityFlag = columns[PRIORITYFLAG];
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
		pdmBatchID = columns[PDMBATCHID];
		swatchOverrideName = columns[SWATCHOVERRIDENAME];
		primaryImage = columns[PRIMARYIMAGE];
		dummy = columns [DUMMY];

		
	}
	
	public AssetBean(String line){
		
		String columns[] = line.split("\t");
		
		include_in_migration = columns[INCLUDE_IN_MIGRATION];
		fullpath = columns[FULLPATH];
		fullname = columns[FULLNAME];
		uPC = columns[UPC];
        collection = columns[COLLECTION];
		concept = columns[CONCEPT];
		asset_Use = columns[ASSET_USE];
		channel = columns[CHANNEL];
		asset_Source = columns[ASSET_SOURCE];
		asset_Type_Level_4 = columns[ASSET_TYPE_LEVEL_4];
		asset_Type_Product = columns[ASSET_TYPE_PRODUCT];
		shot_Type = columns[SHOT_TYPE];
		sequencing = columns[SEQUENCING];
		holiday = columns[HOLIDAY];
		environment = columns[ENVIRONMENT];
		primaryUPC = columns[PRIMARYUPC];
		sku = columns[SKU];
		brand = columns[BRAND];
		colorCode = columns[COLORCODE];
		colorGroupCode = columns[COLORGROUPCODE];
		product = columns[PRODUCT];
		associatedWebProductID = columns[ASSOCIATEDWEBPRODUCTID];
		associatedCollectionID = columns[ASSOCIATEDCOLLECTIONID];
		departmentName = columns[DEPARTMENTNAME];
		departmentNumber = columns[DEPARTMENTNUMBER];
		primaryVendorNumber = columns[PRIMARYVENDORNUMBER];
		primaryVendorName = columns[PRIMARYVENDORNAME];
		vendorDirectToCustomerItem = columns[VENDORDIRECTTOCUSTOMERITEM];
		priorityFlag = columns[PRIORITYFLAG];
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
		pdmBatchID = columns[PDMBATCHID];
		swatchOverrideName = columns[SWATCHOVERRIDENAME];
		primaryImage = columns[PRIMARYIMAGE];
		dummy = columns [DUMMY];
		
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

	public String getuPC() {
		return uPC;
	}

	public void setuPC(String uPC) {
		this.uPC = uPC;
	}

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }
	
	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public String getAsset_Use() {
		return asset_Use;
	}

	public void setAsset_Use(String asset_Use) {
		this.asset_Use = asset_Use;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getAsset_Source() {
		return asset_Source;
	}

	public void setAsset_Source(String asset_Source) {
		this.asset_Source = asset_Source;
	}

	public String getAsset_Type_Level_4() {
		return asset_Type_Level_4;
	}

	public void setAsset_Type_Level_4(String asset_Type_Level_4) {
		this.asset_Type_Level_4 = asset_Type_Level_4;
	}

	public String getAsset_Type_Product() {
		return asset_Type_Product;
	}

	public void setAsset_Type(String asset_Type_Product) {
		this.asset_Type_Product = asset_Type_Product;
	}

	public String getShot_Type() {
		return shot_Type;
	}

	public void setShot_Type(String shot_Type) {
		this.shot_Type = shot_Type;
	}

	public String getSequencing() {
		return sequencing;
	}

	public void setSequencing(String sequencing) {
		this.sequencing = sequencing;
	}

	public String getHoliday() {
		return holiday;
	}

	public void setHoliday(String holiday) {
		this.holiday = holiday;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
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

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getDepartmentNumber() {
		return departmentNumber;
	}

	public void setDepartmentNumber(String departmentNumber) {
		this.departmentNumber = departmentNumber;
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
	
	public String getSwatchOverrideName() {
		return swatchOverrideName;
	}

	public void setSwatchOverrideName(String swatchOverrideName) {
		this.swatchOverrideName = swatchOverrideName;
	}
	
	public String getPrimaryImage() {
		return primaryImage;
	}

	public void setPrimaryImage(String primaryImage) {
		this.primaryImage = primaryImage;
	}

	public String getDummy() {
		return dummy;
	}

	public void setDummy(String dummy) {
		this.dummy = dummy;
	}


}
