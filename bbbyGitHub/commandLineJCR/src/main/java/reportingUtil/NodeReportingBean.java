package reportingUtil;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

public class NodeReportingBean {
	
//	NodeBean workflowNode;
//	TasksBean taskNode;
	
//	public NodeBean getWorkflowNode() {
//		return workflowNode;
//	}
	public void setWorkflowNode(NodeBean workflowNode) {
		//this.workflowNode = workflowNode;
		
		node_description = workflowNode.getDescription();
		node_title = workflowNode.getTitle();
		node_argTaskDueDeltaDays = workflowNode.getArgTaskDueDeltaDays();
		
	}
//	public TasksBean getTaskNode() {
//		return taskNode;
//	}
	
	public void setTaskNode(TasksBean taskNode) {
		//this.taskNode = taskNode;
		
		task_assignee = taskNode.getAssignee();
		task_completedBy = taskNode.getCompletedBy();
		task_name = taskNode.getName();
		task_nameHierarchy = taskNode.getNameHierarchy();
		task_dueDate = taskNode.getDueDate();
		task_priority = taskNode.getPriority();
		task_status = taskNode.getStatus();
		task_startTime = taskNode.getStartTime();
		task_wfInstanceId = taskNode.getWfInstanceId();
		task_workItemId = taskNode.getWorkItemId();
		
	}

	//Extracted variables
	
	private String node_description;
	private String node_title;
	private String 	node_argTaskDueDeltaDays;	
	
	private String task_assignee;
	private String task_completedBy;
	private String task_name;
	private String task_nameHierarchy;
	private Date task_dueDate;
	private String task_priority;
	private String task_status;
	private Date task_startTime;
	private String task_wfInstanceId;
	private String task_workItemId;

	@XmlAttribute
	public String getNode_description() {
		return node_description;
	}
	@XmlAttribute
	public String getNode_title() {
		return node_title;
	}
	@XmlAttribute
	public String getNode_argTaskDueDeltaDays() {
		return node_argTaskDueDeltaDays;
	}
	@XmlAttribute
	public String getTask_assignee() {
		return task_assignee;
	}

	@XmlAttribute
	public String getTask_completedBy() {
		return task_completedBy;
	}
	@XmlAttribute
	public String getTask_name() {
		return task_name;
	}
	@XmlAttribute
	public String getTask_nameHierarchy() {
		return task_nameHierarchy;
	}
	@XmlAttribute
	public Date getTask_dueDate() {
		return task_dueDate;
	}
	@XmlAttribute
	public String getTask_priority() {
		return task_priority;
	}
	@XmlAttribute
	public String getTask_status() {
		return task_status;
	}
	@XmlAttribute
	public Date getTask_startTime() {
		return task_startTime;
	}
	@XmlAttribute
	public String getTask_wfInstanceId() {
		return task_wfInstanceId;
	}
	@XmlAttribute
	public String getTask_workItemId() {
		return task_workItemId;
	}
	
	
}
