package com.bbby.aem.core.services;

import com.day.cq.workflow.WorkflowException;

import javax.jcr.Session;

public interface InvokeAEMWorkflow {
    public void startWorkflow(Session session, String workflowName, String payloadPath) throws WorkflowException;
}
