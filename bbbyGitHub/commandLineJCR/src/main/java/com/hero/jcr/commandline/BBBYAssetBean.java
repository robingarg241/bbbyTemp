package com.hero.jcr.commandline;

/*
 * Utility class to unpack line from input file into model bean
 */

public class BBBYAssetBean {
	
	final int INCLUDE_IN_MIGRATION = 0;
	final int DUPTARGET = 1;
	final int FULLPATH = 2;
	final int FULLNAME = 3;
	final int UPDATED_UNIQUE_FILENAME = 4;
	final int ASSET_TYPE = 5;
	final int DUMMY = 6;
	
	String include_in_migration;
	String duptarget;
	String fullpath;
	String fullname;
	String updated_unique_filename;
	String asset_type;
	String dummy;
	
	public BBBYAssetBean(String[] columns){
		
		include_in_migration = columns[INCLUDE_IN_MIGRATION];
		duptarget = columns[DUPTARGET];
		fullpath = columns[FULLPATH];
		fullname = columns[FULLNAME];
		updated_unique_filename = columns[UPDATED_UNIQUE_FILENAME];
		asset_type = columns[ASSET_TYPE];
		dummy = columns[DUMMY];
		
	}
	
	public BBBYAssetBean(String line){
		
		String columns[] = line.split("\t");
		
		include_in_migration = columns[INCLUDE_IN_MIGRATION];
		duptarget = columns[DUPTARGET];
		fullpath = columns[FULLPATH];
		fullname = columns[FULLNAME];
		updated_unique_filename = columns[UPDATED_UNIQUE_FILENAME];
		asset_type = columns[ASSET_TYPE];
		dummy = columns[DUMMY];
		
	}
	
	public String getInclude_in_migration() {
		return include_in_migration;
	}
	public void setInclude_in_migration(String include_in_migration) {
		this.include_in_migration = include_in_migration;
	}
	public String getDuptarget() {
		return duptarget;
	}
	public void setDuptarget(String duptarget) {
		this.duptarget = duptarget;
	}
	public String getFullpath() {
		return fullpath;
	}
	public void setFullpath(String fullpath) {
		this.fullpath = fullpath;
	}
	
	/* we want either the fullname or the update_filename if it is not blank */
	public String getFullname() {
		
		String retVal = fullname;
		
		if (!updated_unique_filename.equals("")){
			retVal = updated_unique_filename;
		}
		
		return retVal;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getUpdated_unique_filename() {
		return updated_unique_filename;
	}
	public void setUpdated_unique_filename(String updated_unique_filename) {
		this.updated_unique_filename = updated_unique_filename;
	}
	public String getAsset_type() {
		return asset_type;
	}
	public void setAsset_type(String asset_type) {
		this.asset_type = asset_type;
	}
	public String getDummy() {
		return dummy;
	}
	public void setDummy(String dummy) {
		this.dummy = dummy;
	}

}
