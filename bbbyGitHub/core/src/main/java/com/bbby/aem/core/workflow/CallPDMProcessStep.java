package com.bbby.aem.core.workflow;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
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
import com.bbby.aem.core.api.commands.UpdateAssetCommand;
import com.bbby.aem.core.services.CallPDMService;
import com.bbby.aem.core.services.InvokeAEMWorkflow;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.workflow.impl.CallPDMServiceConfiguration;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;

@Component(service = WorkflowProcess.class, property = {
    "process.label=BBBY Call PDM",
    "Constants.SERVICE_VENDOR=Hero Digital",
    "Constants.SERVICE_DESCRIPTION=Call PDM"})
@Designate(ocd = CallPDMServiceConfiguration.class)

public class CallPDMProcessStep implements WorkflowProcess {

	@Reference
    private Replicator replicator;
	
	@Reference
    CallPDMService callPDMService;

    @Reference
    PDMClient pdmClient;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    public static String TAG_PRODUCT_IMAGE_SINGLE_PRODUCT = "bbby:asset_type/approved_dam/assets/product/images/single_product";
    private String errorMsg = null;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {

        WorkflowData workflowData = workItem.getWorkflowData();
        String type = workflowData.getPayloadType();

        boolean duplicate = workflowData.getMetaDataMap().containsKey("duplicate.asset") ? workflowData.getMetaDataMap().get("duplicate.asset", java.lang.Boolean.class) : false;

        if (duplicate) {
            log.warn("Asset marked as duplicate. Skipping PDM Call");
            return;
        }

        if (CommonConstants.JCR_PATH.equalsIgnoreCase(type)) {
            String path = workflowData.getPayload().toString();
            Session session = null;
            int responceCode;

            try {
                session = workflowSession.adaptTo(Session.class);
                
                if (session.nodeExists(path)) {
	                Node assetNode = (Node) session.getItem(path.replaceAll("/jcr:content.*", ""));
	                boolean isAssetTypeSingleProduct = ServiceUtils.startsWithTag(assetNode, TAG_PRODUCT_IMAGE_SINGLE_PRODUCT);
	              //DAM-1369:Added this condition to avoid call to pdm for pdf assets which are single product.
	                if (!(isAssetTypeSingleProduct && assetNode.getName().toLowerCase().contains(".pdf"))){
		                PDMAPICommand updateAssetCmd = new UpdateAssetCommand(PDMAPICommand.METHOD_POST, assetNode);
		                responceCode = callPDMService.makePDMCall(session, path, updateAssetCmd);
		                log.info("Response Code of asset is: " + responceCode + " and path is: " + path);
		                workItem.getWorkflowData().getMetaDataMap().put("pdmCode", responceCode);
	                } else {
	                	errorMsg = "FAILED : Incorrect asset type for pdf";
	                	Node opmeta = JcrPropertiesUtil.getOperationalNode(assetNode, session);
	    				JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS, errorMsg);
	                }
	                
					//DAM-1374(Auto Move from 'Unreviewed' to 'Reviewed' for videos in approved dam when 'Save & Close')
					if(assetNode.getPath().contains("approved_dam/assets/product/videos/unreviewed")){
						try{
							Node metadataNode = assetNode.getNode(CommonConstants.JCR_CONTENT).getNode("metadata");
							if (metadataNode.hasProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW)
									&& metadataNode.getProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW).getString().contentEquals("yes")) {
								
								String orgPath = assetNode.getPath();
								int position = orgPath.lastIndexOf('/');
								String orgFolderPath = orgPath.substring(0,position);
								String destFolPath = orgFolderPath.replaceAll("unreviewed","reviewed");
								String destPath = orgPath.replaceAll("unreviewed","reviewed");
								JcrUtils.getOrCreateByPath(destFolPath, "sling:Folder", session);
								session.move(orgPath, destPath);
								session.save();
								log.info("Successfully moved video asset to reviewed folder : " + destPath);
								try {
						            replicator.replicate(session, ReplicationActionType.ACTIVATE, destPath);
						            log.info("Successfully published video asset to reviewed folder : " + destPath);
						        } catch (ReplicationException e) {
						            log.error("Failed to publish video asset to reviewed folder: " + e.getMessage());
						        }
							}
						}catch (Exception e) {
							log.error("Unable to move video file from unreviewed to reviewed, for path: " + assetNode.getPath(), e);
						}
					}
				
                }

            } catch (Exception e) {
                log.error("Unable to complete processing the Workflow Process step, for path: " + path, e);
            } finally {
            }
        }

    }
    
}
