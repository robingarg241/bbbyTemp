package com.bbby.aem.core.workflow.publish;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Session;

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
import com.day.cq.dam.commons.util.S7SetHelper;

/**
 * @author Karan
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Publish Move ImageSet",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=BBBY APPROVE AND PUBLISH, Move Imageset" })
public class MoveImageSet implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private ResourceResolver resourceResolver;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		log.info("entering execute of Move ImageSet");
		String path = workflowData.getPayload().toString();
		Session session = null;
		resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
		try {
			session = workflowSession.adaptTo(Session.class);
			String destination = null;
			Resource image1r = resourceResolver.resolve(path);
			if (S7SetHelper.isS7Set(image1r)) {
				Node node = session.getNode(path);
				Node meta = node.hasNode(CommonConstants.METADATA_NODE) ? node.getNode(CommonConstants.METADATA_NODE) : null;
				String assetType = ServiceUtils.getAssetType(meta);
				destination = "/content/dam/bbby/"
						+ assetType.substring(CommonConstants.BBBY_ASSET_TYPE.length() + 1);
				ArrayList<String> partitions = PartitionUtil.hashFileName(node.getName());
				// create the holding partitions if they don't exist
				destination = destination + "/" + partitions.get(0);
				destination = destination + "/" + partitions.get(1);
				// setting up the final path in the map
				destination = destination + "/" + node.getName();
				
				//Thread.sleep(1 * 60 * 1000);//1 min pause.
				
				session.move(path, destination);
				workflowData.getMetaDataMap().put(node.getName(), destination);
			}
			
		} catch (Exception e) {
			log.error("Unable to complete processing of Move ImageSet Process step, for path: " + path, e);
			throw new WorkflowException(e.getMessage(), e);
		}
		workflowSession.updateWorkflowData(workItem.getWorkflow(), workflowData);
	}
}
