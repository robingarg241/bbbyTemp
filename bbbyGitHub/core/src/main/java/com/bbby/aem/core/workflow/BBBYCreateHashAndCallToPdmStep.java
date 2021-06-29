package com.bbby.aem.core.workflow;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

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
import com.bbby.aem.core.models.data.AssetHash;
import com.bbby.aem.core.services.AssetHasherService;
import com.bbby.aem.core.services.CallPDMService;
import com.bbby.aem.core.util.AssetHasherUtils;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.workflow.WorkflowService;

@Component(service = WorkflowProcess.class, property = {
    "process.label=Create Hash and Call to PDM",
    "Constants.SERVICE_VENDOR=Offshore Team",
    "Constants.SERVICE_DESCRIPTION=Listens to the state of the asset."})

public class BBBYCreateHashAndCallToPdmStep implements WorkflowProcess {
    
    @Reference
    private WorkflowService workflowService;
	
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Reference
    private AssetHasherService assetHasherService;

    @Reference
    private CallPDMService callPDMService;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public static String TAG_PRODUCT_COLLATERAL1 = "bbby:asset_type/approved_dam/assets/product/collateral/lighting_facts";
    public static String TAG_PRODUCT_COLLATERAL2 = "bbby:asset_type/approved_dam/assets/product/collateral/energy_guides";
    public static String TAG_PRODUCT_COLLATERAL3 = "bbby:asset_type/approved_dam/assets/product/collateral/prop_65";
    public static String TAG_PRODUCT_IMAGE_SINGLE_PRODUCT = "bbby:asset_type/approved_dam/assets/product/images/single_product";
    private String errorMsg = null;
    
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

            AssetHash assetHash = null;
            boolean duplicate = true;
            assetHash = assetHasherService.createAssetHash(resourceResolver, assetPath);

            Resource assetResource = resourceResolver.getResource(assetPath);
            Node assetNode = assetResource.adaptTo(Node.class);
            Node metadataNode = assetNode.getNode(CommonConstants.METADATA_NODE);
            
            // ba38752: adding color space as a metadata attribute as defined in DAM-441.
            String colorSpace = ServiceUtils.getColorSpace(assetResource);
            metadataNode.setProperty(CommonConstants.BBBY_COLOR_SPACE, colorSpace);
            if(!metadataNode.hasProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW)){
            	metadataNode.setProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW, "no");
            }
            
            if (assetHash != null || assetPath.contains("bbby/asset_transitions_folder/internal")) {
            	
            	if(assetPath.contains("bbby/asset_transitions_folder/internal")){
            		duplicate = AssetHasherUtils.checkForDupsForInternal(session, assetNode);
            	}
            	
            	else{
            		
            		boolean isAssetTypeCollateral = startsWithTag(metadataNode, TAG_PRODUCT_COLLATERAL1, TAG_PRODUCT_COLLATERAL2, TAG_PRODUCT_COLLATERAL3);
            		if(isAssetTypeCollateral){
            			duplicate = false; //DAM-1024(To allow same images with asset type as Collateral)
            		}else{
            			duplicate = AssetHasherUtils.checkForDups(session, assetNode, assetHash.getAssetHashCode());
            		}
	                if(!duplicate){
		                metadataNode.setProperty(CommonConstants.BBBY_ASSET_HASH, assetHash.getAssetHashCode());
		                metadataNode.setProperty(CommonConstants.BBBY_ASSET_HASH_SEED, assetHash.getAssetHash());
	                }
	                
            	}
            	
            }
            
            if (duplicate) {
                log.warn("Asset marked as duplicate or asset is not in vendor asset holding or internal folder. Skipping PDM Call");
                workflowData.getMetaDataMap().put("duplicate.asset", true);
                return;
            }
            
            boolean isAssetTypeSingleProduct = ServiceUtils.startsWithTag(assetNode, TAG_PRODUCT_IMAGE_SINGLE_PRODUCT);
            //DAM-1369:Added this condition to avoid call to pdm for pdf assets which are single product.
            if (!(isAssetTypeSingleProduct && assetNode.getName().toLowerCase().contains(".pdf"))){
            	callPDM(session, assetPath);
            } else {
            	errorMsg = "FAILED : Incorrect asset type for pdf";
            	Node opmeta = JcrPropertiesUtil.getOperationalNode(assetNode, session);
				JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS, errorMsg);
            }

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
    
    private boolean startsWithTag(Node meta, String tagValue1, String tagValue2, String tagValue3) throws ValueFormatException, IllegalStateException, RepositoryException {
    	log.debug("entering startsWithTag() method");
    	if(meta == null) {
    		return false;
    	}
    	if(!meta.hasProperty(CommonConstants.CQ_TAGS)){
    		return false;
    	}
    	Property prop = meta.getProperty(CommonConstants.CQ_TAGS);
    	if(prop == null) return false;
        Value[] values = prop.getValues();
        if (values!=null){
	        for (Value val : values) {
	          String tag = val.getString();
	            if (tag != null && (tag.startsWith(tagValue1) || tag.startsWith(tagValue2) || tag.startsWith(tagValue3))) {
	            return true;
	          }
	        }
        }
        return false;
    }
}
