package com.bbby.aem.core.workflow.fastTrackMoveAndPublish;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

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
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.PartitionUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;

/**
 * @author Karan
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label= Nightly Publish Assets",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=BBBY APPROVE AND PUBLISH, Publish Imageset/Image" })
public class PublishImageImagesetFastTrack implements WorkflowProcess {

	@Reference
	private Replicator replicator;
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	private ResourceResolver resourceResolver;
	
	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		log.info("entering execute of publish Asset");
		String path = workflowData.getPayload().toString();
		resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
		Session session = null;
		try {
			boolean isImagesetMove = ServiceUtils.moveImagesetNightly(workflowData.getMetaDataMap().get("moveImages",String.class));
			session = workflowSession.adaptTo(Session.class);
			String destination = path;
			if (isImagesetMove) {
				destination = getImagesetDestPath(path.substring(path.lastIndexOf("/")+1, path.length()));
			}
			Node node = session.getNode(destination);

			Resource image1r = resourceResolver.resolve(destination);
			String publishToS7ByUser = "";
			if (S7SetHelper.isS7Set(image1r) && !isImagesetMove) {
				@SuppressWarnings("deprecation")
				ImageSet imageSet = image1r.adaptTo(ImageSet.class);
				Node repometaImageset = JcrPropertiesUtil.getReportingNode(node, session);
				if (repometaImageset != null && repometaImageset.hasProperty(CommonConstants.PUBLISHED_TO_S7_BY_USER)) {
					publishToS7ByUser = repometaImageset.getProperty(CommonConstants.PUBLISHED_TO_S7_BY_USER).getValue().getString();
				}
				List<Node> assets = ServiceUtils.getImagesetMembers(imageSet, session);
				//assets.add(node); // Adding Image set
				if (assets != null) {
					ReplicationOptions replicationOptions =  new ReplicationOptions();
					replicationOptions.setSynchronous(true);
					String[] destArray = new String[assets.size()+1];
					int count = assets.size()-1;
					int index = 0;
					for (Node memberNode : assets) {
						destination = memberNode.getPath();
						log.info("publish Asset : " + destination);
						
						if(index == count){
							destArray[index] = destination +"/jcr:content/related/s7Set";
							index++;
						}
						
						destArray[index] = destination;
						index++;
					}
					
					boolean isPublished = publishAsset(session, destArray, replicationOptions);
					
					for (Node memberNode : assets) {
						destination = memberNode.getPath();
						log.info("publish Asset : " + destination);
						
						String msg = null;
						if (isPublished) {
							msg = "Success";
							Node repometaImage = JcrPropertiesUtil.getReportingNode(memberNode, session);
							if(repometaImage != null){
								JcrUtil.setProperty(repometaImage, CommonConstants.PUBLISHED_TO_S7_BY_USER, publishToS7ByUser);
							}
						} else {
							msg = "PUBLISH_FAILED";
						}

						setOperationalAttribute(memberNode, session, msg);
					}
				}

			} else {
				destination = node.getPath();
				if (destination != null) {
					boolean isPublished = publishAsset(session, destination);
					Node asset = session.getNode(destination);
					if (isPublished) {
						// DAM-511
						setOperationalAttribute(session.getNode(destination), session, "Success");
					} else {
						setOperationalAttribute(session.getNode(destination), session, "PUBLISH_FAILED");
					}
					
				} else {
					log.debug("destination is null " + destination);
				}
			}

		} catch (Exception e) {
			log.error("Unable to complete processing of Publish Asset step, for path: " + path, e);
			throw new WorkflowException(e.getMessage(), e);
		}
	}

	private boolean publishAsset(Session session, String destination) throws WorkflowException {
		log.info("entering publishAsset() method");
		boolean successful = true;
		try {
			log.info("Publish Asset : " + destination);
			replicator.replicate(session, ReplicationActionType.ACTIVATE, destination);
			log.info("Successfully publish asset : " + destination);
		} catch (ReplicationException e) {
			log.error("Unable to publish the asset: " + e);
			successful = false;
			throw new WorkflowException(e.getMessage(), e);
		}
		return successful;
	}
	
	private boolean publishAsset(Session session, String[] destination, ReplicationOptions replicationOptions) throws WorkflowException {
		log.info("entering publishAsset() method");
		boolean successful = true;
		try {
			log.info("Publish Asset : " + destination);
			replicator.replicate(session, ReplicationActionType.ACTIVATE, destination, replicationOptions);
			successful = true;
			log.info("Successfully publish asset : " + destination);
		} catch (ReplicationException e) {
			log.error("Unable to publish the asset: " + e);
			successful = false;
			throw new WorkflowException(e.getMessage(), e);
		}
		return successful;
	}
	
	// DAM-511 : Set Operational Attributes for success/failure.
	private void setOperationalAttribute(Node asset, Session session, String msg) {
		log.debug("entering setOperationalAttribute() method");
		try {
			Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
			JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PC_Executed_Date,
					ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
			JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PC_WF_State, msg);
		} catch (Exception e) {
			log.error("Unable to set Operational attributes " + e.getMessage());
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
