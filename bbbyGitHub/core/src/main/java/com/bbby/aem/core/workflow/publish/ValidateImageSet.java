package com.bbby.aem.core.workflow.publish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;

/**
 * @author Karan
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Publish Validate ImageSet",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=BBBY APPROVE AND PUBLISH, validate imageset" })
public class ValidateImageSet implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private ResourceResolver resourceResolver;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		log.info("entering execute of validate Imageset");
		String path = workflowData.getPayload().toString();
		Session session = null;
		resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
		try {
			session = workflowSession.adaptTo(Session.class);
			Resource image1r = resourceResolver.resolve(path);
			if (S7SetHelper.isS7Set(image1r)) {
				List<Node> assets = new ArrayList<Node>();
				Node node = session.getNode(path);
				@SuppressWarnings("deprecation")
				ImageSet imageSet = image1r.adaptTo(ImageSet.class);
				if (imageSet != null) {
					assets = listAssetsOfImageset(imageSet, session);
				}
				if (validateReferences(node, assets, session)) {
					workflowData.getMetaDataMap().put("isImagesetValid", "true");
				} else {
					workflowData.getMetaDataMap().put("isImagesetValid", "false");
					throw new WorkflowException("Unable to complete processing of Validate Imageset Process step, for path: " + path);
				}
				workflowSession.updateWorkflowData(workItem.getWorkflow(), workflowData);
			}
		} catch (Exception e) {
			log.error("Unable to complete processing of Validate Imageset Process step, for path: " + path, e);
			throw new WorkflowException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("deprecation")
	private List<Node> listAssetsOfImageset(ImageSet imageSet, Session session) throws Exception {
		log.debug("entering listAssetsOfImageset() method");
		List<Node> assetList = new ArrayList<Node>();
		Iterator<Asset> assets = imageSet.getMembers();

		while (assets.hasNext()) {
			Asset asset = assets.next();
			if (!asset.getPath().contains("content/dam/bbby/approved_dam")) {
				assetList.add(session.getNode(asset.getPath()));
			}
		}
		return assetList;
	}

	// DAM-511 : Check references of imageset in every payload.
	private boolean validateReferences(Node node, List<Node> nodes, Session session) throws Exception {
		log.debug("entering checkReferences() method");
		boolean hasRefereces = false;
		List<Node> assetList = new ArrayList<Node>();
		ArrayList<String> memberList = ServiceUtils.getMembersOfImageset(session, node.getPath());
		if (memberList != null && memberList.size() > 0) {
			for (String member : memberList) {
				Node memberNode = session.getNode(member);
				String memberResourceValue = memberNode.hasProperty("sling:resource")
						? memberNode.getProperty("sling:resource").getString() : "";
				if (!memberResourceValue.contains("content/dam/bbby/approved_dam")) {
					assetList.add(session.getNode(member));
				}
			}
			if (assetList.size() == nodes.size()) {
				hasRefereces = true;
				log.info("Reference count matches with node count");
			} else {
				log.info("Reference count is mismatch with node count");
			}
		} else {
			log.info("Do not have reference : 0 references");
		}
		return hasRefereces;
	}
}
