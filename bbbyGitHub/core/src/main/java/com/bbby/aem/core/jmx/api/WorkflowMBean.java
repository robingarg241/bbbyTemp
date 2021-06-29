package com.bbby.aem.core.jmx.api;

import javax.management.DynamicMBean;
import javax.management.openmbean.TabularData;

import com.adobe.granite.jmx.annotation.Description;

@Description("Example MBean that exposes Workflow model properties.")
public interface WorkflowMBean extends DynamicMBean {

	public TabularData purgeCompleted(String model, Integer numberOfDays, Boolean dryRun);

	public TabularData purgeActive(String model, Integer numberOfDays, Boolean dryRun);

	public int countStaleWorkflows(String model);

	public TabularData restartStaleWorkflows(String model, Boolean dryRun);

	public TabularData fetchModelList();

	public int countRunningWorkflows(String model);

	public int countCompletedWorkflows(String model);

	public TabularData listRunningWorkflowsPerModel();

	public TabularData listCompletedWorkflowsPerModel();

	public TabularData returnWorkflowQueueInfo();

	public TabularData returnWorkflowJobTopicInfo();

	public int returnFailedWorkflowCount(String model);

	public TabularData returnFailedWorkflowCountPerModel();

	public TabularData terminateFailedInstances(boolean restartInstance, boolean dryRun, String model);

	public TabularData retryFailedWorkItems(boolean dryRun, String model);

}			