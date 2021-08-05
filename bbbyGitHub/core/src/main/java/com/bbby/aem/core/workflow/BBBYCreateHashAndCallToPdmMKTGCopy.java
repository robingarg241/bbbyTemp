package com.bbby.aem.core.workflow;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
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
import com.bbby.aem.core.api.commands.UpdateAssetCommand;
import com.bbby.aem.core.services.AssetHasherService;
import com.bbby.aem.core.services.CallPDMService;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.workflow.WorkflowService;

@Component(service = WorkflowProcess.class, property = {
    "process.label=Create Hash and Call to PDM",
    "Constants.SERVICE_VENDOR=Offshore Team",
    "Constants.SERVICE_DESCRIPTION=Listens to the state of the asset."})

public class BBBYCreateHashAndCallToPdmMKTGCopy implements WorkflowProcess {
    
    @Reference
    private WorkflowService workflowService;
	
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Reference
    private AssetHasherService assetHasherService;

    @Reference
    private CallPDMService callPDMService;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
    	
        WorkflowData workflowData = workItem.getWorkflowData();
        String type = workflowData.getPayloadType();

        if (CommonConstants.JCR_PATH.equalsIgnoreCase(type)) {
            String path = workflowData.getPayload().toString().replaceAll("/jcr:content.*", "");
            ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
            
            try {
            	Resource r = resourceResolver.getResource(path);
            	if(r != null) {
            		Node node = r.adaptTo(Node.class);
            		Node nodeJcr = node.getNode("./jcr:content");
        			log.info("Asset state is processed for: {}", nodeJcr);
        			try {
                        createHash(path, resourceResolver, workflowData);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Exception creating hash for asset {}", e.getLocalizedMessage());
                    }
        		}
            } catch (Exception e) {
            	
            	log.error("Asset is not processed at path: {}", path, e);           	
            }          
        }       
    }
    
    private void createHash(String assetPath, ResourceResolver resourceResolver, WorkflowData workflowData) throws RepositoryException, LoginException {


        try  {
            Session session = resourceResolver.adaptTo(Session.class);

            Resource assetResource = resourceResolver.getResource(assetPath);
            Node assetNode = assetResource.adaptTo(Node.class);
            Node metadataNode = assetNode.getNode(CommonConstants.METADATA_NODE);
            
            // ba38752: adding color space as a metadata attribute as defined in DAM-441.
            String colorSpace = ServiceUtils.getColorSpace(assetResource);
            metadataNode.setProperty(CommonConstants.BBBY_COLOR_SPACE, colorSpace);
            if(!metadataNode.hasProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW)){
            	metadataNode.setProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW, "no");
            }
			metadataNode.setProperty(CommonConstants.BBBY_ASSET_UPDATE, "no");
			if (metadataNode.hasProperty(CommonConstants.CQ_TAGS)) {
				metadataNode.getProperty(CommonConstants.CQ_TAGS).remove();
			}
			String[] tags = { "bbby:asset_type/approved_dam/assets/product/images/single_product",
					"bbby:shot_type/environment" };
			metadataNode.setProperty(CommonConstants.CQ_TAGS, tags);
			log.info("Tags added");
			Node opmeta = JcrPropertiesUtil.getOperationalNode(assetNode , session);
			if (opmeta.hasProperty("isMarketingAsset")) {
				opmeta.getProperty("isMarketingAsset").remove();
			}
            
            callPDM(session, assetPath);

        } catch (Exception e) {
            log.error("Failed to start workflow", e);
        }
    }

    private void callPDM(Session session, String assetPath) throws Exception {
        int responceCode;

        Node assetNode = (Node) session.getItem(assetPath.replaceAll("/jcr:content.*", ""));
        PDMAPICommand updateAssetCmd = new UpdateAssetCommand(PDMAPICommand.METHOD_POST, assetNode);
        responceCode = callPDMService.makePDMCall(session, assetPath, updateAssetCmd);

        log.debug(String.format("PDM Call response for asset %1$s is %2$s", assetPath, responceCode));
    }
    
}
