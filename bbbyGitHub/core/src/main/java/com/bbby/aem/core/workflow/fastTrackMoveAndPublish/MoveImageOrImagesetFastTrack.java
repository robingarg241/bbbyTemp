package com.bbby.aem.core.workflow.fastTrackMoveAndPublish;

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
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.workflow.impl.BBBYCleanupConfiguration;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;
import com.day.cq.wcm.commons.ReferenceSearch;

/**
 * @author Karan
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Fast Track Move Image",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=Fast Track move to destintion folder" })
@Designate(ocd = BBBYCleanupConfiguration.class)

public class MoveImageOrImagesetFastTrack implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private ResourceResolver resourceResolver;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
		log.info("entering execute of Move Image");
		String path = workflowData.getPayload().toString();
		Session session = null;

		try {
			session = workflowSession.adaptTo(Session.class);
			Node node = session.getNode(path);
			String destination = null;
			Resource image1r = resourceResolver.resolve(path);
			boolean isImagesetMove = ServiceUtils.moveImagesetNightly(workflowData.getMetaDataMap().get("moveImages",String.class));
			if (S7SetHelper.isS7Set(image1r) && !isImagesetMove) {
				@SuppressWarnings("deprecation")
				ImageSet imageSet = image1r.adaptTo(ImageSet.class);
				List<Node> assets = ServiceUtils.getImagesetMembers(imageSet, session);
				ReferenceSearch referenceSearch = new ReferenceSearch();
				for (Node memberNode : assets) {
					log.info("Move Asset (adjustReferences) : " + memberNode.getPath());
					destination = ServiceUtils.getDestPath(session, memberNode);
					log.info("adjustReferences Destination to move Asset : " + destination);
					if (session.itemExists(memberNode.getPath()) && !memberNode.getName().contains("_imageset")) {
						referenceSearch.adjustReferences(node, memberNode.getPath(), destination);
						log.info("References Adjusted");
						session.move(memberNode.getPath(), destination);
						log.info("Asset moved");
					}
				}
				
			} else {
				log.info("Move Asset : " + path);
				destination = ServiceUtils.getDestPath(session, node);
				session.move(path, destination);
			}

		} catch (Exception e) {
			log.error("Unable to complete processing of Create Folder Process step, for path: " + path, e);
			throw new WorkflowException(e.getMessage(), e);
		}
	}
}
