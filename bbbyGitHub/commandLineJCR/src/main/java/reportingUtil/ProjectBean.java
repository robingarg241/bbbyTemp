package reportingUtil;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="Projects")
public class ProjectBean {
	
	private String projectTitle;
	private String projectPath;
	private String dateCreated;
	private String description;
	private String requester;
	private String status;
	private String proposed_workflows;
	private String bi_values;
	
	private List<WorkflowInstanceBean> workflows;
	
	@XmlAttribute
	public String getTitle() {
		return projectTitle;
	}
	public void setTitle(String title) {
		this.projectTitle = title;
	}
	@XmlAttribute
	public String getProjectPath() {
		return projectPath;
	}
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}
	
	@XmlElement(name="Workflows")
	public List<WorkflowInstanceBean> getWorkflows() {
		return workflows;
	}
	public void setWorkflows(List<WorkflowInstanceBean> workflows) {
		this.workflows = workflows;
	}
	
	@XmlAttribute
	public String getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	@XmlAttribute
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlAttribute
	public String getRequester() {
		return requester;
	}
	public void setRequester(String requester) {
		this.requester = requester;
	}
	
	@XmlAttribute
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@XmlAttribute
	public String getProposed_workflows() {
		return proposed_workflows;
	}
	public void setProposed_workflows(String proposed_workflows) {
		this.proposed_workflows = proposed_workflows;
	}
	
	@XmlAttribute
	public String getBi_values() {
		return bi_values;
	}
	public void setBi_values(String bi_values) {
		this.bi_values = bi_values;
	}

}
