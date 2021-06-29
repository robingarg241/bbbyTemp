package com.bbby.aem.core.workflow.nightlyPublish;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
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
import com.bbby.aem.core.util.PartitionUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;

/**
 * @author Karan
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Nightly Publish Create Folder",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=BBBY APPROVE AND PUBLISH, Created destination folder" })
public class CreateFolderForImageNightly implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private ResourceResolver resourceResolver;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		log.info("entering execute of folder creation");
		String path = workflowData.getPayload().toString();
		Session session = null;
		resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
		try {
			session = workflowSession.adaptTo(Session.class);
			Node node = session.getNode(path);
			Resource image1r = resourceResolver.resolve(path);
			if (S7SetHelper.isS7Set(image1r)) {
				@SuppressWarnings("deprecation")
				ImageSet imageSet = image1r.adaptTo(ImageSet.class);
				List<Node> assets = ServiceUtils.getImagesetMembers(imageSet, session);
				for (Node memberNode : assets) {
					createDestFolder(session, memberNode, workflowData);
				}
			}
			//Actual payload
			createDestFolder(session, node, workflowData);
			
			workflowSession.updateWorkflowData(workItem.getWorkflow(), workflowData);
		} catch (Exception e) {
			log.error("Unable to complete processing of Create Folder Process step, for path: " + path, e);
			throw new WorkflowException(e.getMessage(), e);
		}
	}

	private void createDestFolder(Session session, Node node, WorkflowData workflowData) throws Exception {
		log.info("Create Dest Folder : "+node.getPath());
		Node meta = node.hasNode(CommonConstants.METADATA_NODE) ? node.getNode(CommonConstants.METADATA_NODE) : null;
		String assetType = ServiceUtils.getAssetType(meta);
		if (assetType != null) {
			String destination = "/content/dam/bbby/"
					+ assetType.substring(CommonConstants.BBBY_ASSET_TYPE.length() + 1);
			ArrayList<String> partitions = PartitionUtil.hashFileName(node.getName());
			// create the holding partitions if they don't exist
			destination = destination + "/" + partitions.get(0);
			destination = destination + "/" + partitions.get(1);
			JcrUtils.getOrCreateByPath(destination, "sling:Folder", session);
			// setting up the final path in the map
			destination = destination + "/" + node.getName();
			workflowData.getMetaDataMap().put(node.getName(), destination);
		} else {
			log.debug("AssetType is null");
		}
	}

}
