package com.bbby.aem.core.servlets;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.PartitionUtil;

@Component(name = "Marketing AD Copy Servlet", immediate = true, service = Servlet.class, property = {
		"sling.servlet.methods=POST", "sling.servlet.paths=/bin/bbby/copy-mktg" })
public class CopyMktgToADServlet extends SlingAllMethodsServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private ResourceResolver resourceResolver = null;

	private static String workflowName_s7 = "/var/workflow/models/scene7-with-publish";

	@Override
	protected void doPost(final SlingHttpServletRequest req, final SlingHttpServletResponse resp)
			throws ServletException, IOException {
		log.info("Copy Mktg to AD Servlet Post method");
		resourceResolver = req.getResourceResolver();
		Session session = resourceResolver.adaptTo(Session.class);
		String searchId = req.getParameter("./copy-mktg");
		log.info("Search Identifier..." + searchId);
		String assets = req.getParameter("./assets");
		String[] assetsArray = assets.split("\\r?\\n");
		for (String assetPath : assetsArray) {
			try {
				if (session.itemExists(assetPath) && assetPath.contains("content/dam/marketing")) {
					Node templateNode = (Node) session.getItem(assetPath);
					if (templateNode.isNodeType("dam:Asset")) {
						String dest = finalAssetNameFolderCreation(assetPath, session);
						copyAssetToAD(assetPath, dest, session, searchId);
					} else {
						log.info("It's not dam asset ");
					}

				} else {
					log.info("Not exist in marketing" + assetPath);
				}
			} catch (Exception e) {
				log.error("Unable to complete processing " + e);
			}
		}

	}

	private String finalAssetNameFolderCreation(String assetPath, Session session) throws Exception {
		log.info("Individual Asset: " + assetPath);
		Node asset = session.getNode(assetPath);
		String assetName = asset.getName();
		String newName = "m-" + ZonedDateTime.now().toString().replaceAll("[^0-9-]", "-").substring(0, 16) + "_"
				+ assetName;
		ArrayList<String> partitions = PartitionUtil.hashFileName(newName);
		String destination = "/content/dam/bbby/approved_dam/assets/product/images/single_product";
		// create the holding partitions if they don't exist
		destination = destination + "/" + partitions.get(0);
		destination = destination + "/" + partitions.get(1);
		Node destpathnode = JcrUtils.getOrCreateByPath(destination, "sling:Folder", session);
		session.save();
		destination = destpathnode.getPath() + "/" + newName;
		log.info("destination Asset: " + destination);
		return destination;
	}

	private void copyAssetToAD(String assetPath, String dest, Session session, String searchId) throws Exception {
		Node asset = session.getNode(assetPath);
		String assetName = asset.getName();
		if (!session.nodeExists(dest)) {
			Workspace workspace = session.getWorkspace();
			workspace.copy(assetPath, dest);
			session.save();
			if (session.nodeExists(dest + "/jcr:content/metadata")) {
				Node metadataNode = session.getNode(dest + "/jcr:content/metadata");
				log.info("Path for Metadata Node : " + metadataNode.getPath());
				metadataNode.setProperty(CommonConstants.BBBY_UPLOADED_ASSET_NAME, assetName);
				metadataNode.setProperty(CommonConstants.BBBY_ASSET_UPDATE, "no");
				metadataNode.setProperty("bbby:instructions", searchId);
				String[] tags = { "bbby:asset_type/approved_dam/assets/product/images/single_product",
						"bbby:shot_type/silhouette" };
				metadataNode.setProperty(CommonConstants.CQ_TAGS, tags);
				log.info("Tags added");
			}
			session.save();
			// execute scene7 with publish workflow
			executeScene7withPublishWorkflow(dest);

		} else {
			log.info("Node already exist at.. " + dest);
		}

	}

	private void executeScene7withPublishWorkflow(String dest) throws Exception {
		WorkflowSession workflowSession_s7 = resourceResolver.adaptTo(WorkflowSession.class);
		WorkflowModel workflowModel_s7 = workflowSession_s7.getModel(workflowName_s7);
		WorkflowData workflowData_s7 = workflowSession_s7.newWorkflowData("JCR_PATH", dest);
		Map<String, Object> workflowMetadata_s7 = new HashMap<>();
		workflowSession_s7.startWorkflow(workflowModel_s7, workflowData_s7, workflowMetadata_s7);
		log.info("Scene 7 with Publish Workflow executed");
	}

}