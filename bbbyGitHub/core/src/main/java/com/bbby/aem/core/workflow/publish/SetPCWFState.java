package com.bbby.aem.core.workflow.publish;

import javax.jcr.Node;
import javax.jcr.Session;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;

/**
 * @author Sandeep
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Set Operational Attributes",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=Cleanup project assets, tag and move to general population" })
public class SetPCWFState implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		log.info("entering execute of publish Asset");
		String path = workflowData.getPayload().toString();
		Session session = null;
		try {
			session = workflowSession.adaptTo(Session.class);
			Node node = session.getNode(path);
			String msg = workflowData.getMetaDataMap().get("opmsg", String.class);

			if (msg != null) {
				setOperationalAttribute(node, session, msg);
			} else {
				log.debug("opmsg is null " + msg);
			}
		} catch (Exception e) {
			log.error("Unable to complete processing of Create Folder Process step, for path: " + path, e);
			throw new WorkflowException(e.getMessage(), e);
		}
	}

	// DAM-511 : Set Operational Attributes for success/failure.
	private void setOperationalAttribute(Node asset, Session session, String msg) throws WorkflowException {
		log.debug("entering setOperationalAttribute() method");
		try {
			Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
			JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PC_Executed_Date,
					ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
			JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PC_WF_State, msg);
			log.info("set Operational attributes successful.");
		} catch (Exception e) {
			log.error("Set Operational attributes failed. " + e);
			// throw new WorkflowException(e.getMessage(), e);
		}
	}
}
