package com.bbby.aem.core.workflow.publish;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
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

/**
 * @author Karan
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Publish Create Folder For Image",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=Cleanup project assets, tag and move to general population" })

public class CreateFolderForImageset implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());


	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		log.info("entering execute of folder creation for Imageset");
		String path = workflowData.getPayload().toString();
		Session session = null;

		try {
			session = workflowSession.adaptTo(Session.class);
			Node node = session.getNode(path);
			Node meta = node.hasNode(CommonConstants.METADATA_NODE) ? node.getNode(CommonConstants.METADATA_NODE)
					: null;
			String assetType = ServiceUtils.getAssetType(meta);
			if (assetType != null) {
				String destination = "/content/dam/bbby/"
						+ assetType.substring(CommonConstants.BBBY_ASSET_TYPE.length() + 1);
				ArrayList<String> partitions = PartitionUtil.hashFileName(node.getName());
				// create the holding partitions if they don't exist
				destination = destination + "/" + partitions.get(0);
				destination = destination + "/" + partitions.get(1);
				JcrUtils.getOrCreateByPath(destination, "sling:Folder", session);
			} else {
				log.debug("AssetType is null");
			}
		} catch (Exception e) {
			log.error("Unable to complete processing of Create Folder Process step, for path: " + path, e);
			throw new WorkflowException(e.getMessage(), e);
		}

	}

}
