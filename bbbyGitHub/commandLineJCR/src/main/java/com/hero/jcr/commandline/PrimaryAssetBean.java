package com.hero.jcr.commandline;

public class PrimaryAssetBean {
	
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
	String departmentName;
	String departmentNumber;
	String swatchOverrideName;
	String primaryImage;
	String primaryUPC;
	String sku;
	
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
    final int DEPARTMENTNAME = 15;
    final int DEPARTMENTNUMBER = 16;
    final int SWATCHOVERRIDENAME = 17;
    final int PRIMARYIMAGE = 18;
    final int SKU = 19;
	
	
	
	public PrimaryAssetBean (AssetBean ab) {
		
		include_in_migration = ab.getInclude_in_migration();
		fullpath = ab.getFullpath();
		fullname = ab.getFullname();
		uPC = ab.getuPC();
		collection = ab.getCollection();
		concept = ab.getConcept();
		asset_Use = ab.getAsset_Use();
		channel = ab.getChannel();
		asset_Source = ab.getAsset_Source();
		asset_Type_Level_4 = ab.getAsset_Type_Level_4();
		asset_Type_Product = ab.getAsset_Type_Product();
		shot_Type = ab.getShot_Type();
		sequencing = ab.getSequencing();
		holiday = ab.getHoliday();
		environment = ab.getEnvironment();
		departmentName = ab.getDepartmentName();
		departmentNumber = ab.getDepartmentNumber();
		swatchOverrideName = ab.getSwatchOverrideName();
		primaryImage = ab.getPrimaryImage();
		sku = ab.getSku();  ////big warning here!  this is a value that is on the detail upc metadata, we are raising it up because of the lack of sku at this level in the data
		
	}
	
	public PrimaryAssetBean (String[] columns) {
		
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
		departmentName = columns[DEPARTMENTNAME];
		departmentNumber = columns[DEPARTMENTNUMBER];
		swatchOverrideName = columns[SWATCHOVERRIDENAME];
		primaryImage = columns[PRIMARYIMAGE];
		sku = columns[SKU];
		
	}
	
	public PrimaryAssetBean (String line) {
		
		String [] columns = line.split("\t");
		
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
		departmentName = columns[DEPARTMENTNAME];
		departmentNumber = columns[DEPARTMENTNUMBER];
		swatchOverrideName = columns[SWATCHOVERRIDENAME];
		primaryImage = columns[PRIMARYIMAGE];
		sku = columns[SKU];
		
	}	
	
	public String getFormattedOutput() {
		
		String output = include_in_migration + "\t" + 
			   fullpath + "\t" + 
				fullname + "\t" + 
				uPC + "\t" + 
				collection + "\t" +
				concept + "\t" + 
				asset_Use + "\t" + 
				channel + "\t" + 
				asset_Source + "\t" + 
				asset_Type_Level_4 + "\t" + 
				asset_Type_Product + "\t" + 
				shot_Type + "\t" + 
				sequencing + "\t" + 
				holiday + "\t" + 
				environment + "\t" + 
				departmentName + "\t" +
				departmentNumber + "\t" +
				swatchOverrideName + "\t" +
				primaryImage + "\t" +
				sku + "\t" + 
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
	public void setAsset_Type_Product(String asset_Type_Product) {
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
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}

}
