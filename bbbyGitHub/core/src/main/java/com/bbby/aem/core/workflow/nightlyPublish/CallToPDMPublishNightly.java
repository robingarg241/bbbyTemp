package com.bbby.aem.core.workflow.nightlyPublish;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
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
import com.bbby.aem.core.api.PDMAPICommand;
import com.bbby.aem.core.api.PDMClient;
import com.bbby.aem.core.api.commands.PublishAssetCommand;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.PartitionUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;

/**
 * @author Karan
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Nightly Publish Call to PDM",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=BBBY APPROVE AND PUBLISH, Call to PDM" })
public class CallToPDMPublishNightly implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Reference
	private PDMClient pdmClient;

	private String initiator = null;
	private ResourceResolver resourceResolver;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		log.info("entering execute of call to pdm");
		String path = workflowData.getPayload().toString();
		Session session = null;
		try {
			boolean isImagesetMove = ServiceUtils.moveImagesetNightly(workflowData.getMetaDataMap().get("moveImages",String.class));
			session = workflowSession.adaptTo(Session.class);
			resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
			String destination = path;
			if (isImagesetMove) {
				destination = getImagesetDestPath(path.substring(path.lastIndexOf("/")+1, path.length()));
			}
			Node node = session.getNode(destination);
			if (workflowData.getMetaDataMap().containsKey("userId")) {
				initiator = workflowData.getMetaDataMap().get("userId", String.class);
				log.info("initiator by userId : "+initiator);
			} else {
				initiator = workItem.getWorkflow().getInitiator();
				log.info("initiator by getInitiator() method : "+initiator);
			}
			//String destination = null;
			Resource image1r = resourceResolver.resolve(destination);
			if (S7SetHelper.isS7Set(image1r) && !isImagesetMove) {
				@SuppressWarnings("deprecation")
				ImageSet imageSet = image1r.adaptTo(ImageSet.class);
				List<Node> assets = ServiceUtils.getImagesetMembers(imageSet, session);
				for (Node memberNode : assets) {
					destination = memberNode.getPath();//ServiceUtils.getDestPath(session, memberNode);
					if (destination != null) {
						makePDMCall(session, memberNode);
					} else {
						log.debug("destination is null " + destination);
					}
				}
			}else{
				destination = node.getPath();//ServiceUtils.getDestPath(session, node);
				if (destination != null) {
					makePDMCall(session, node);
				} else {
					log.debug("destination is null " + destination);
				}
			}
		
		} catch (Exception e) {
			log.error("Unable to execute call to pdm Process step, for path: " + path, e);
			throw new WorkflowException(e.getMessage(), e);
		}
	}

	private void makePDMCall(Session session, Node asset) throws WorkflowException {
		int resCode = -1;

		try {
			//Node movedAsset = session.getNode(destination);
			PDMAPICommand publishCommand = new PublishAssetCommand(PDMAPICommand.METHOD_POST, asset);
			resCode = pdmClient.execute(publishCommand);

			if (resCode == HttpStatus.SC_OK) {
				log.info("Setting operational attributes as response code is 200");
				Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
				JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PDM_CALL_SENT,
						ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
				long count = opmeta.hasProperty(CommonConstants.OPMETA_PDM_CALL_COUNT)
						? opmeta.getProperty(CommonConstants.OPMETA_PDM_CALL_COUNT).getValue().getLong() : 0;
				JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PDM_CALL_COUNT, count + 1);
				JcrUtil.setProperty(opmeta, "pwfpdmCallStatus", "Success");
				JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS, "SUCCESS");
			} else {
				log.info("Unable to set operational attributes as response code is not 200");
				Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
				JcrUtil.setProperty(opmeta, "pwfpdmCallStatus", "PDM Call Failed");
				JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS,
						"FAILED : Status Code " + resCode);
			}
		} catch (Exception e) {
			log.error("PDM Call failed" + e);
			// throw new WorkflowException(e.getMessage(), e);
		}
	}
	
	private String getImagesetDestPath(String nodeName) throws Exception {
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
