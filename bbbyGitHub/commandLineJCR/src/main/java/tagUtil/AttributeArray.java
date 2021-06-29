package tagUtil;

import java.util.ArrayList;
import java.util.List;

public class AttributeArray {

	private List<String> tagList = new ArrayList<String>();

	private String nodeName;
	private String upc;
	private String collection;
	private String sku;
	private String departmentName;
	private String departmentNumber;
	private String swatchOverrideName;
	private String primaryImage;
	

	public String toString() {
		
		
		String retVal = "Node Name: " + nodeName + "\n" ;
		retVal += "\tUPC: " + upc + "\n";
		retVal += "\tSKU: " + sku + "\n";
		retVal += "\tCollection: " + collection + "\n";
		retVal += "\tDepartmentName: " + departmentName + "\n";
		retVal += "\tDepartmentNumber: " + departmentNumber + "\n";
		retVal += "\tSwatchOverrideName: " + swatchOverrideName + "\n";
		retVal += "\tPrimaryImage: " + primaryImage + "\n";
		
		for (String tag : tagList){
			
			retVal += "\tTag: " + tag + "\n";
			
		}
		
		return retVal;
		
	}

	public String getCollection() {
		return collection;
	}


	public void setCollection(String collection) {
		this.collection = collection;
	}

	public List<String> getTagList() {
		return tagList;
	}


	public void setTagList(List<String> tagList) {
		this.tagList = tagList;
	}


	public String getNodeName() {
		return nodeName;
	}


	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}


	public String getUpc() {
		return upc;
	}


	public void setUpc(String upc) {
		this.upc = upc;
	}


	public String getSku() {
		return sku;
	}


	public void setSku(String sku) {
		this.sku = sku;
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
	
}
