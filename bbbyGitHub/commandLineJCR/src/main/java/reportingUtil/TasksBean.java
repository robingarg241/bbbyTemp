package reportingUtil;

import java.util.Date;
import java.text.SimpleDateFormat;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

public class TasksBean {
	
	//2016-01-23T15:29:16.408-08:00
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddkk:mm:ss.SSS");
	
	private String assignee;
	private String completedBy;
	private String name;
	private String nameHierarchy;
	private Date dueDate;
	private String priority;
	private String status;
	private Date startTime;
	private String wfInstanceId;
	private String workItemId;
	private String taskRole;
	private String taskDueDeltaDays;
	
	@XmlAttribute
	public String getAssignee() {
		return assignee;
	}
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	@XmlAttribute
	public String getCompletedBy() {
		return completedBy;
	}
	public void setCompletedBy(String completedBy) {
		this.completedBy = completedBy;
	}
	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@XmlAttribute
	public String getNameHierarchy() {
		return nameHierarchy;
	}
	public void setNameHierarchy(String nameHierarchy) {
		this.nameHierarchy = nameHierarchy;
	}
	@XmlAttribute
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(String dueDate) {
		
		Date date = new Date();
		
		try{

			date = sdf.parse(dueDate.replace("T", ""));
			
		} catch (Exception e){
			//default set in instantiation
		}
		
		this.dueDate = date;
	}
	@XmlAttribute
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@XmlAttribute
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		
		Date date = new Date();
		
		try{

			date = sdf.parse(startTime.replace("T", ""));
			
		} catch (Exception e){
			//default set in instantiation
		}		
		
		this.startTime = date;
		
	}
	@XmlAttribute
	public String getWfInstanceId() {
		return wfInstanceId;
	}
	public void setWfInstanceId(String wfInstanceId) {
		this.wfInstanceId = wfInstanceId;
	}
	
	@XmlAttribute
	public String getWorkItemId() {
		return workItemId;
	}
	public void setWorkItemId(String workItemId) {
		this.workItemId = workItemId;
	}
	
	@XmlAttribute
	public String getTaskRole() {
		return taskRole;
	}
	public void setTaskRole(String taskRole) {
		this.taskRole = taskRole;
	}
	
	@XmlAttribute
	public String getTaskDueDeltaDays() {
		return taskDueDeltaDays;
	}
	public void setTaskDueDeltaDays(String taskDueDeltaDays) {
		this.taskDueDeltaDays = taskDueDeltaDays;
	}
	

}
