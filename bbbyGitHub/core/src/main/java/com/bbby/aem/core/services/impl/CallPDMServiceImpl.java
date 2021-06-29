package com.bbby.aem.core.services.impl;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.api.PDMAPICommand;
import com.bbby.aem.core.api.PDMClient;
import com.bbby.aem.core.services.CallPDMService;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.workflow.impl.CallPDMServiceConfiguration;
import com.day.cq.commons.jcr.JcrUtil;

@Component(immediate = true, service = CallPDMService.class, property = {
    "process.label=BBBY Call PDM Service",
    "Constants.SERVICE_VENDOR=Hero Digital",
    "Constants.SERVICE_DESCRIPTION=Call PDM Service"})
@Designate(ocd = CallPDMServiceConfiguration.class)
public class CallPDMServiceImpl implements CallPDMService {
	
	private static final Logger log = LoggerFactory.getLogger(CallPDMServiceImpl.class);

    @Reference
    PDMClient pdmClient;

    private String internalFolderPath;
    private String vendorAssetsHoldingFolderPath;
    private String ecommFolderPath;
    private String approvedDAMFolderPath;
    private String marketingFolderPath;
    
    private String errorMsg = null;

    @Activate
    public synchronized void activate(CallPDMServiceConfiguration config) {

        this.internalFolderPath = config.internalFolderPath();
        this.vendorAssetsHoldingFolderPath = config.vendorAssetsHoldingFolderPath();
        this.ecommFolderPath = config.ecommFolderPath();
        this.approvedDAMFolderPath = config.approvedDAMFolderPath();
        this.marketingFolderPath = config.marketingFolderPath(); 
    }

    @Override
    public int makePDMCall(Session session, String assetPath, PDMAPICommand pdmapiCommand) throws Exception {

    	log.info("PDM Call trigger : "+assetPath);
    	errorMsg = null;
    	
       
    	Node assetNode = (Node) session.getItem(assetPath.replaceAll("/jcr:content.*", ""));

        Node metadataNode = assetNode.getNode(CommonConstants.METADATA_NODE);
        
        Node jcrNode = assetNode.getNode(CommonConstants.JCR_CONTENT_NODE);
        
        boolean isProcessed = false;
        //DAM-1461: Block the pdm call for invalid asset name in internal folder.
        boolean isValidAssetName = ServiceUtils.validFileName(assetNode.getName());
        if(!isValidAssetName) {
        errorMsg = "FAILED : Invalid Asset Name";
        }
        
        //DAM-683 : Ideally PDM calls shouldn't have been made for unProcessed vendor upload assets
        if(jcrNode.hasProperty(CommonConstants.DAM_ASSET_STATE) && CommonConstants.PROCESSED.equalsIgnoreCase(jcrNode.getProperty(CommonConstants.DAM_ASSET_STATE).getValue().getString())){
        	isProcessed = true;
        } else {
        	errorMsg = "FAILED : Not Processed";
        }
        
        int resCode = -1;

        if (metadataNode != null) {
            if (assetPath.startsWith(this.internalFolderPath)) {
            	if(!(metadataNode.hasProperty(CommonConstants.BBBY_SKU) || metadataNode.hasProperty(CommonConstants.BBBY_UPC))){
            		errorMsg = "FAILED : UPC/SKU Missing ";
            	}
                if ((metadataNode.hasProperty(CommonConstants.BBBY_SKU) || metadataNode.hasProperty(CommonConstants.BBBY_UPC)) && hasAssetType(metadataNode) && isValidForPDMCall(assetNode) && hasValidUPCorSKU(metadataNode) && isProcessed && isValidAssetName) {
//                    PDMAPICommand updateAssetCmd = new UpdateAssetCommand(PDMAPICommand.METHOD_POST, assetNode);
                    resCode = pdmClient.execute(pdmapiCommand);
                }
            } else if (assetPath.startsWith(this.vendorAssetsHoldingFolderPath)) {
            	if(!metadataNode.hasProperty(CommonConstants.BBBY_UPC)){
            		errorMsg = "FAILED : UPC Missing ";
            	}
                if (metadataNode.hasProperty(CommonConstants.BBBY_UPC) && hasAssetType(metadataNode) && isValidForPDMCall(assetNode) && isProcessed) {
//                    PDMAPICommand updateAssetCmd = new UpdateAssetCommand(PDMAPICommand.METHOD_POST, assetNode);
                    resCode = pdmClient.execute(pdmapiCommand);
                }
            } else if (assetPath.startsWith(this.ecommFolderPath)) {
				boolean isRejected = ServiceUtils.isRejectedAsset(metadataNode);
				// PDMAPICommand updateAssetCmd = new UpdateAssetCommand(PDMAPICommand.METHOD_POST, assetNode);
				if (!isRejected) {
					  if(isValidAssetName) {
						  resCode = pdmClient.execute(pdmapiCommand);
					  }
				} else {
					errorMsg = "FAILED : Rejected asset.";
				}
            } else if (assetPath.startsWith(this.approvedDAMFolderPath)) {
//                PDMAPICommand updateAssetCmd = new UpdateAssetCommand(PDMAPICommand.METHOD_POST, assetNode);
            	 if(isValidAssetName) {
					  resCode = pdmClient.execute(pdmapiCommand);
				  }
            } else if (assetPath.startsWith(this.marketingFolderPath)) { //for marketing assets
            	if(!(metadataNode.hasProperty(CommonConstants.BBBY_SKU) || metadataNode.hasProperty(CommonConstants.BBBY_UPC))){
            		errorMsg = "FAILED : UPC/SKU Missing ";
            	}
	            if ((metadataNode.hasProperty(CommonConstants.BBBY_SKU) || metadataNode.hasProperty(CommonConstants.BBBY_UPC)) && hasValidUPCorSKU(metadataNode) && isValidForPDMCall(assetNode) && hasAssetTypeMarketing(metadataNode) && isProcessed) {
	            	resCode = pdmClient.execute(pdmapiCommand);
	            }
            } 
        }
        
        if(resCode == HttpStatus.SC_OK){
        	Node opmeta = JcrPropertiesUtil.getOperationalNode(assetNode, session);
        	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PDM_CALL_SENT, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
        	long count = opmeta.hasProperty(CommonConstants.OPMETA_PDM_CALL_COUNT)? opmeta.getProperty(CommonConstants.OPMETA_PDM_CALL_COUNT).getValue().getLong():0;
        	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PDM_CALL_COUNT, count+1);
        	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS, "SUCCESS");
        }else{
			if (errorMsg != null) {
				Node opmeta = JcrPropertiesUtil.getOperationalNode(assetNode, session);
				JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS, errorMsg);
			}else{
				Node opmeta = JcrPropertiesUtil.getOperationalNode(assetNode, session);
	        	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS, "FAILED : Status Code "+resCode);
			}
        }
        return resCode;
    }

    private boolean hasAssetType(Node metadataNode) throws RepositoryException {
        boolean hasAssetType = false;
        if (metadataNode.hasProperty(CommonConstants.CQ_TAGS)) {
            Property tagProp = metadataNode.getProperty(CommonConstants.CQ_TAGS);
            Value[] tags = tagProp.getValues();
            for (Value tagVal : tags) {
                String tag = tagVal.getString();
                if (StringUtils.startsWith(tag, CommonConstants.BBBY_ASSET_TYPE)) {
                    hasAssetType = true;
                }
            }
        }
        if(!hasAssetType){
        	errorMsg = "FAILED : Asset Type is missing.";
        }
        return hasAssetType;
    }
    
    //Method for checking whether asset type is of marketing or not.
    private boolean hasAssetTypeMarketing(Node metadataNode) throws RepositoryException {
        boolean hasAssetType = false;
        if (metadataNode.hasProperty(CommonConstants.CQ_TAGS)) {
            Property tagProp = metadataNode.getProperty(CommonConstants.CQ_TAGS);
            Value[] tags = tagProp.getValues();
            for (Value tagVal : tags) {
                String tag = tagVal.getString();
                if (StringUtils.startsWith(tag, CommonConstants.MARKETING_ASSET_TYPE)) {
                    hasAssetType = true;
                }
            }
        }
        if(!hasAssetType){
        	errorMsg = "FAILED : Asset Type is missing.";
        }
        return hasAssetType;
    }
    
    private boolean hasValidUPCorSKU(Node metadataNode) throws RepositoryException {
        boolean hasValidUPCorSKU = true;
        if (metadataNode.hasProperty(CommonConstants.BBBY_UPC)) {
        	String upcValue = (metadataNode.hasProperty(CommonConstants.BBBY_UPC)) ? metadataNode.getProperty(CommonConstants.BBBY_UPC).getString() : null;
			if (upcValue != null) {
				String[] upcList = upcValue.split(",");
				for (int i = 0; i < upcList.length; i++) {
					if (!StringUtils.isNumeric(upcList[i].trim()) || upcList[i].trim().length() > 15 ) {
						hasValidUPCorSKU = false;
						log.info("Not Valid for PDM Call due to UPC length : "+upcList[i].length());
						errorMsg = "FAILED : UPC length is more than 15 char or UPC is not a numeric value.";
					}
				}
			}
        }
        
        if (metadataNode.hasProperty(CommonConstants.BBBY_SKU)){
        	String skuValue = (metadataNode.hasProperty(CommonConstants.BBBY_SKU)) ? metadataNode.getProperty(CommonConstants.BBBY_SKU).getString() : null;
			if (skuValue != null) {
				String[] skuList = skuValue.split(",");
				for (int i = 0; i < skuList.length; i++) {
					if (!StringUtils.isNumeric(skuList[i].trim()) || skuList[i].trim().length() > 12) {
						hasValidUPCorSKU = false;
						log.info("Not Valid for PDM Call due to SKU length : "+skuList[i].length());
						errorMsg = "FAILED : SKU length is more than 12 char or SKU is not a numeric value.";
					}
				}
			}
        }
        return hasValidUPCorSKU;
    }
    
    private boolean isValidForPDMCall(Node node) throws RepositoryException {
    	boolean isValidForPDMCall = false;
    	
    	String upcCqTagsMissingMsg = ServiceUtils.getUPCorCqTagsMissingMessage(node);
    	if(upcCqTagsMissingMsg == null){
    		isValidForPDMCall = true;
    	}else{
    		log.info("Not Valid for PDM Call due to : "+upcCqTagsMissingMsg);
    		errorMsg = "FAILED : " + upcCqTagsMissingMsg;
    	}
    	
        return isValidForPDMCall;
    }
}
