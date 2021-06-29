package reportingUtil;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="List")
public class WorkflowInstanceBean {
	
	private String modelid;
	private String workflowid;
	
	List<NodeReportingBean> nodes;
	
	@XmlAttribute
	public String getModelid() {
		return modelid;
	}
	public void setModelid(String modelid) {
		this.modelid = modelid;
	}
	@XmlAttribute
	public String getWorkflowid() {
		return workflowid;
	}
	public void setWorkflowid(String workflowid) {
		this.workflowid = workflowid;
	}
	
	@XmlElement(name="Tasks")
	public List<NodeReportingBean> getNodes() {
		return nodes;
	}
	public void setNodes(List<NodeReportingBean> nodes) {
		this.nodes = nodes;
	}

}
