package com.bbby.aem.core.workflow;

import javax.jcr.Node;
import javax.jcr.Session;

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
import com.bbby.aem.core.services.CallPDMService;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.workflow.WorkflowService;

@Component(service = WorkflowProcess.class, property = {
    "process.label=BBBY Marketing",
    "Constants.SERVICE_VENDOR=Offshore Team",
    "Constants.SERVICE_DESCRIPTION=Listens to the state of the asset."})

public class BBBYMarketingStep implements WorkflowProcess {
    
    @Reference
    private WorkflowService workflowService;
	
    @Reference
    private ResourceResolverFactory resolverFactory;
    
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
            	Resource resource = resourceResolver.getResource(path);
            	if(resource != null && path.contains("content/dam/marketing")) {
            		Node node = resource.adaptTo(Node.class);
            		Node nodeJcr = node.getNode("./jcr:content");
        			log.info("Asset state is processed for: {}", nodeJcr);
        			Session session = resourceResolver.adaptTo(Session.class);
        			try {
        	            Node metadataNode = node.getNode(CommonConstants.METADATA_NODE);
        	            String colorSpace = ServiceUtils.getColorSpace(resource);
        	            metadataNode.setProperty(CommonConstants.BBBY_COLOR_SPACE, colorSpace);
        	            // DAM -1507 - Auto AEM Orientation - Added the orientation based on width and height
        	            setMarketingOrientation(metadataNode);
        	            if(node.hasNode(CommonConstants.METADATA_IPTC_NODE)){
	        	            Node iptcMetadataNode = node.getNode(CommonConstants.METADATA_IPTC_NODE);
	        	            String creatorPhone = "";
	        	            String creatorEmail = "";
	        	            String creatorWebsite = "";
	        	            if(iptcMetadataNode.hasProperty("Iptc4xmpCore:CiTelWork")){
	        	            	creatorPhone = iptcMetadataNode.getProperty("Iptc4xmpCore:CiTelWork").getValue().toString();
	        	            	metadataNode.setProperty(CommonConstants.BBBY_UPC, creatorPhone);
	        	            }
	        	            if(iptcMetadataNode.hasProperty("Iptc4xmpCore:CiEmailWork")){
	        	            	creatorEmail = iptcMetadataNode.getProperty("Iptc4xmpCore:CiEmailWork").getValue().toString();
	        	            	metadataNode.setProperty(CommonConstants.BBBY_SKU, creatorEmail);
	        	            }   
	        	            if(iptcMetadataNode.hasProperty("Iptc4xmpCore:CiUrlWork")){
	        	            	creatorWebsite = iptcMetadataNode.getProperty("Iptc4xmpCore:CiUrlWork").getValue().toString();
	        	            	metadataNode.setProperty(CommonConstants.BBBY_DUMMY_SKU, creatorWebsite);
	        	            }   
        	            }
        	            callPDM(session, path);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Exception in bbby marketing step{}", e.getLocalizedMessage());
                    }
        		}
            } catch (Exception e) {
            	
            	log.error("Asset is not processed at path: {}", path, e);           	
            }          
        }       
    }
    
    private void callPDM(Session session, String assetPath) throws Exception {
        int responceCode;

        Node assetNode = (Node) session.getItem(assetPath.replaceAll("/jcr:content.*", ""));
        PDMAPICommand updateAssetCmd = new UpdateAssetCommand(PDMAPICommand.METHOD_POST, assetNode);
        responceCode = callPDMService.makePDMCall(session, assetPath, updateAssetCmd);

        log.debug(String.format("PDM Call response for asset %1$s is %2$s", assetPath, responceCode));
    }
    
    private void setMarketingOrientation(Node metadataNode) throws Exception {
    	if(metadataNode.hasProperty("tiff:ImageLength") && metadataNode.hasProperty("tiff:ImageWidth")) {
    	 long height = metadataNode.getProperty("tiff:ImageLength").getLong();
		 long width = metadataNode.getProperty("tiff:ImageWidth").getLong();
		 log.info("Height is.. : "+ height +" width is.."+width);
		 
		 if(height>width) {
			 metadataNode.setProperty("bbby:orientation", "Portrait");  
		 }
		 else if(height<width) {
			 metadataNode.setProperty("bbby:orientation", "Landscape"); 	 
		 }
		 else {
			 metadataNode.setProperty("bbby:orientation", "Square"); 
		 }
    	}
    	else {
    		log.info("height and width property not present in metadata");
    	}
    }
   
}
