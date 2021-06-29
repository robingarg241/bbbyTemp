package reportingUtil;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

public class NodeBean {
	
	private String description;
	private String nodeTitle;
	private String 	argTaskDueDeltaDays;
	
	@XmlAttribute
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlAttribute
	public String getTitle() {
		return nodeTitle;
	}
	public void setTitle(String title) {
		this.nodeTitle = title;
	}
	
	@XmlAttribute
	public String getArgTaskDueDeltaDays() {
		return argTaskDueDeltaDays;
	}
	public void setArgTaskDueDeltaDays(String argTaskDueDeltaDays) {
		this.argTaskDueDeltaDays = argTaskDueDeltaDays;
	}

}
