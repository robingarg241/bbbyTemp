package reportingUtil;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Tasks")
public class NodeReportingBeanList {

	private List<NodeReportingBean> nodeInstances = new ArrayList<NodeReportingBean>();
	
	@XmlElement(name="Task")
	public List<NodeReportingBean> getNodeInstances() {
		return nodeInstances;
	}

	public void setNodeInstances(List<NodeReportingBean> nodeInstances) {
		this.nodeInstances = nodeInstances;
	}

}
