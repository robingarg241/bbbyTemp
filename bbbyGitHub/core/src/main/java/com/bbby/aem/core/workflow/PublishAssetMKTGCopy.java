package com.bbby.aem.core.workflow;

import javax.jcr.Node;
import javax.jcr.Session;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.bbby.aem.core.util.CommonConstants;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.Replicator;
import com.day.cq.replication.ReplicationException;

/**
 * @author Karan
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Publish Asset MKTGCopy",
		"Constants.SERVICE_VENDOR=BBBY", "Constants.SERVICE_DESCRIPTION=Publish Asset cpied to AD from MKTG" })
public class PublishAssetMKTGCopy implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Reference
	private Replicator replicator;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		log.info("entering execute of add search identifier and tags");
		String path = workflowData.getPayload().toString().replaceAll("/jcr:content.*", "");
		log.info("path is: " + path);
		Session session = null;

		try {
			session = workflowSession.adaptTo(Session.class);
			publishAsset(session, path);
		} catch (Exception e) {
			log.info("Unable to complete processing the Workflow Process step, for path: " + path, e);
			throw new WorkflowException(e.getMessage(), e);
		}

	}

	private void publishAsset(Session session, String destination) throws WorkflowException {
		log.info("entering publishAsset() method");
		
		try {
			log.info("Publish Asset : " + destination);
			replicator.replicate(session, ReplicationActionType.ACTIVATE, destination);
			log.info("Successfully publish asset : " + destination);
		} catch (ReplicationException e) {
			log.error("Unable to publish the asset: " + e);
			throw new WorkflowException(e.getMessage(), e);
		}
		
	}

}