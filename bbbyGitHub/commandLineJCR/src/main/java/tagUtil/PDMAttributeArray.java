package tagUtil;

import java.util.List;

public class PDMAttributeArray {

	private String nodeName;
	private String upc;
	private List<String> tagList;
	

	public String toString() {
		
		String retVal = "Node Name: " + nodeName + "\n" ;
		retVal += "\tUPC: " + upc + "\n";
		
		for (String tag : tagList){
			
			retVal += "\tTag: " + tag + "\n";
			
		}
		
		return retVal;
		
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
	
}
