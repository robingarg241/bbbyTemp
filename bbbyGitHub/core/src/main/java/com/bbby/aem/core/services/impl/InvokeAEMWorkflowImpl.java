package com.bbby.aem.core.services.impl;

import javax.jcr.Session;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.services.InvokeAEMWorkflow;
import com.bbby.aem.core.util.CommonConstants;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.model.WorkflowModel;

@Component(
    immediate = true,
    service = InvokeAEMWorkflow.class)
public class InvokeAEMWorkflowImpl implements InvokeAEMWorkflow {

    @Reference
    private WorkflowService workflowService;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void startWorkflow(Session session, String workflowName, String payloadPath) throws WorkflowException {

        //Create a workflow session
        WorkflowSession wfSession = workflowService.getWorkflowSession(session);

        // Get the workflow model
        WorkflowModel wfModel = wfSession.getModel(workflowName);

        // Get the workflow data
        // The first param in the newWorkflowData method is the payloadType.  Just a fancy name to let it know what type of workflow it is working with.
        WorkflowData wfData = wfSession.newWorkflowData(CommonConstants.JCR_PATH, payloadPath);

        // Run the Workflow.
        wfSession.startWorkflow(wfModel, wfData);

        log.debug("Started workflow programatically for "+ workflowName);
    }
}
