package com.bbby.aem.core.workflow.publish;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.bbby.aem.core.util.PartitionUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.workflow.impl.BBBYCleanupConfiguration;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;
import com.day.cq.wcm.commons.ReferenceSearch;

/**
 * @author Robin
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Adjust Reference Before Move Image",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=BBBY APPROVE AND PUBLISH, adjust reference before move to destintion folder" })
@Designate(ocd = BBBYCleanupConfiguration.class)

public class AdjustImageReferenceBeforeMove implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private ResourceResolver resourceResolver;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
		log.info("entering execute of Adjust Reference before actual Moving of Image");
		String path = workflowData.getPayload().toString();
		log.info("Path in AdjustImagereferencebeforeMove is : " + path);
		Session session = null;

		try {
			session = workflowSession.adaptTo(Session.class);
			String destination1 = getImagesetDestPath(path.substring(path.lastIndexOf("/")+1, path.length()));
			Node node1 = session.getNode(destination1);
			//Node node = session.getNode(path);
			String destination = null;
			Resource image1r = resourceResolver.resolve(destination1);
			if (S7SetHelper.isS7Set(image1r)) {
				log.info("AdjustImagereferencebeforeMove imageset is in scene7 ");
				@SuppressWarnings("deprecation")
				ImageSet imageSet = image1r.adaptTo(ImageSet.class);
				List<Node> assets = ServiceUtils.getImagesetMembers(imageSet, session);
				ReferenceSearch referenceSearch = new ReferenceSearch();
				for (Node memberNode : assets) {
					log.info("AdjustImagereferencebeforeMove : " + memberNode.getPath());
					destination = ServiceUtils.getDestPath(session, memberNode);
					if (session.itemExists(memberNode.getPath()) && !memberNode.getName().contains("_imageset")) {
						referenceSearch.adjustReferences(node1, memberNode.getPath(), destination);
						log.info("Reference for asset Adjusted: " + destination);
					}
				}
			} 
		} catch (Exception e) {
			log.error("Unable to complete processing of adjust reference Process step, for path: " + path, e);
			throw new WorkflowException(e.getMessage(), e);
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
