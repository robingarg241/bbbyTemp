package com.bbby.aem.core.workflow.nightlyPublish;

import java.util.ArrayList;

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
import com.bbby.aem.core.util.PartitionUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;

/**
 * @author Karan
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Nightly Publish Set Operational Attributes",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=BBBY APPROVE AND PUBLISH, Set Operational Attributes" })
public class SetOperationalAttrubutesNightly implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		workflowData.getPayloadType();
		log.info("entering execute of set operation attribute for nightly process");
		Session session = workflowSession.adaptTo(Session.class);
		String path = workflowData.getPayload().toString();
		String opmessage = (String) workflowData.getMetaDataMap().get("opmsg");
		try {
			if(!session.nodeExists(path)){
				path = getDestPath(path.substring(path.lastIndexOf("/")+1, path.length()));
			}
			
			if(session.nodeExists(path)){
				setOperationalAttribute(session.getNode(path), session, opmessage);
			}
		} catch (Exception e) {
			log.error("Unable to complete processing the set Operational attributes Process step, for path: " + path,
					e);
		}
	}

	// DAM-511 : Set Operational Attributes for success/failure.
	private void setOperationalAttribute(Node node, Session session, String msg) {
		log.debug("entering setOperationalAttribute() method");
		try {
			Node opmeta = JcrPropertiesUtil.getOperationalNode(node, session);
			JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PC_Executed_Date,
					ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
			JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PC_WF_State, msg);
			log.info("set Operational attributes successful.");
		} catch (Exception e) {
			log.error("Set Operational attributes failed. " + e);
			// throw new WorkflowException(e.getMessage(), e);
		}
	}
	
	private String getDestPath(String nodeName) throws Exception {
		log.info("Get Dest Name : " + nodeName);
		String destination = null;
		if (nodeName.toLowerCase().contains("imageset")){
			destination = "/content/dam/bbby/approved_dam/assets/product/images/image_sets";
		}else{
			destination = "/content/dam/bbby/approved_dam/assets/product/images/single_product";
		}
		
		ArrayList<String> partitions = PartitionUtil.hashFileName(nodeName);
		// create the holding partitions if they don't exist
		destination = destination + "/" + partitions.get(0);
		destination = destination + "/" + partitions.get(1);
		// setting up the final path in the map
		destination = destination + "/" + nodeName;
		return destination;
	}
}
