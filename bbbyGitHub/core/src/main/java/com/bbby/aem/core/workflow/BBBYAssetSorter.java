package com.bbby.aem.core.workflow;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
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
import com.bbby.aem.core.api.commands.RejectAssetCommand;
import com.bbby.aem.core.services.InvokeAEMWorkflow;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.workflow.impl.BBBYAssetSorterConfiguration;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;
import com.day.cq.replication.Replicator;

/**
 * @author jochen (RIP) ANT
 * 03/11/19
 * 
 * This code is used to sort assets to specific sub-directories (node) based on information received from PDM
 *  
 * 
 */

@Component(service = WorkflowProcess.class, property = {
    "process.label=BBBY Asset Sorter",
    "Constants.SERVICE_VENDOR=Hero Digital",
    "Constants.SERVICE_DESCRIPTION=BBBY Sort Assets into directories based on PDM Metadata"})
@Designate(ocd = BBBYAssetSorterConfiguration.class)

public class BBBYAssetSorter implements WorkflowProcess {

    public static String TAG_PRODUCT_IMAGE = "bbby:asset_type/approved_dam/assets/product/images";

    public static String TAG_PRODUCT_VIDEOS = "bbby:asset_type/approved_dam/assets/product/videos/unreviewed";

    public static String TAG_PRODUCT_COLLATERAL = "bbby:asset_type/approved_dam/assets/product/collateral";

    public static String TAG_PRODUCT_3D_RENDERS = "bbby:asset_type/approved_dam/assets/product/3d_renders";

    public static String TAG_NON_PRODUCT = "bbby:asset_type/approved_dam/assets/non-product";

    public static String TAG_CONCEPT_CREATIVE_BRAND = "bbby:asset_type/approved_dam/assets/concept_creative_brand_assets";

    public static String TAG_INSTITUTIONAL = "bbby:asset_type/approved_dam/assets/institutional";

    public static String TAG_CONTENT = "bbby:asset_type/approved_dam/content";

    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    
    public static String REJECT_REASON_DEFAULT = "not-set";

    private enum ASSET_TYPE {
        PRODUCT_IMAGE, PRODUCT_COLLEATERAL_VIDEO, NON_PRODUCT
    }
    
	@Reference
    private ResourceResolverFactory resolverFactory;
//    private BBBYAssetSorterConfiguration config;
	
    @Reference
    private Replicator replicator;

    @Reference
    private PDMClient pdmClient;

    @Reference
    private InvokeAEMWorkflow wfService;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    // Static list of tag domains
    final List<String> tagDomains = Arrays.asList(
        "bbby:"
    );

    private String assetTransitionsInternalFolder;
    private String assetTransitionsVendorFolder;
//    private String unmatchedFolder;
    private String notassignedFolder;
//    private String nonProductFolder;
    private String ecommFolder;
//    private String projCleanupModel;
    private String bbbyProductCollateralAssetModel;
    
//    private final int UNMATCHED = 0;
//    private final int NOTASSIGNED = 1;
//    private final int ECOMM = 2;
    
    @Activate
    public synchronized void activate(BBBYAssetSorterConfiguration config) {
        
        this.assetTransitionsInternalFolder=config.assetTransitionsInternalFolder();
        this.assetTransitionsVendorFolder=config.assetTransitionsVendorFolder();
   //     this.unmatchedFolder=config.unmatchedFolder();
        this.notassignedFolder=config.unassignedFolder();
   //     this.nonProductFolder = config.nonProductFolder();
        this.ecommFolder=config.ecommFolder();
    //    this.projCleanupModel = config.projCleanupModel();
        this.bbbyProductCollateralAssetModel = config.bbbyProductCollateralAssetModel();
    }

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
    	
        WorkflowData workflowData = workItem.getWorkflowData();
        log.debug("entering execute with payload {}: ", workflowData.getPayload().toString());
        String type = workflowData.getPayloadType();
        log.debug("payload type is {}: ", type);
        Session session = workflowSession.adaptTo(Session.class);
        boolean duplicate = workflowData.getMetaDataMap().containsKey("duplicate.asset") ? workflowData.getMetaDataMap().get("duplicate.asset", java.lang.Boolean.class) : false;
        log.debug("payload is dublicate: " + duplicate);
        
        if (CommonConstants.JCR_PATH.equalsIgnoreCase(type) && !duplicate) {
            String path = workflowData.getPayload().toString();
            
            
            //We have a bunch of logic based on the location of the event
            boolean internalAsset = false;
            boolean vendorAsset = false;
            boolean eCommAsset = false;
            
            if (path.startsWith(this.assetTransitionsInternalFolder)) {
            	internalAsset = true;
            } else if (path.startsWith(this.assetTransitionsVendorFolder)) {
                vendorAsset = true;
            } else if (path.startsWith(this.ecommFolder)) {
                eCommAsset = true;
            }
            
            boolean metadataPath = false;
            boolean upcmetadataPath = false;
            
			if (path.contains("/upcmetadata")) {
				upcmetadataPath = true;
			}
			if (path.contains("/metadata")) {
				metadataPath = true;
			}
            
            //The payload returned could be one of the following:
            //1) .../jcr:content/metadata; this would be for a listener looking for rejections
            //2) .../jcr:content/upcmetadata; this would be for PDM data updates
            
            try {
                
                if (!session.nodeExists(path)) {
					log.warn("looks like the asset already moved : " + path);
					return;
                }
                
                //This could be a metadata node or a upcmetadata node
                Node metadataNode = (Node) session.getItem(path);
                
                //this is the root asset node
                Node node = (Node) session.getItem(path.replaceAll("/jcr:content.*", ""));

                // Is this a product image or a collateral asset (Video or PDF, for instance)
//                boolean isProductImage = startsWithTag(node, TAG_PRODUCT_IMAGE);
    			
                NodeType nodeType = node.getPrimaryNodeType();
                
                if(nodeType.getName().equalsIgnoreCase("dam:Asset")) {
                	
                	//First check to see if this has been rejected (./jcr:content/metadata/bbby:rejectionReason)
                	//this will only occur on a metadata node
                	
                	if (metadataPath) {
                		Node repometa = JcrPropertiesUtil.getReportingNode(node, session);                		

                		String initiator = "";
                        if (workflowData.getMetaDataMap().containsKey("userId")) {
            				initiator = workflowData.getMetaDataMap().get("userId", String.class);
            				log.info("initiator by userId : "+initiator);
            			} else {
            				initiator = workItem.getWorkflow().getInitiator();
            				log.info("initiator by getInitiator() method : "+initiator);
            			}
                		
                        if (ServiceUtils.isRejectedAsset(metadataNode)) {
                        	log.debug("payload{} is rejected by {}: " , path, initiator);
                        	int resCode = -1;
                        	
                            //DAM-320 : populated reporting metadata attribute "Rejected By" and "Rejected Date".
                            
							if (repometa != null) {
								JcrUtil.setProperty(repometa, CommonConstants.REJECTED_BY, initiator);
								JcrUtil.setProperty(repometa, CommonConstants.REJECTED_DATE, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
							}

                            if (vendorAsset) {

                                moveAssetToRejectQueue(session, this.assetTransitionsVendorFolder + "/rejected_vendor_assets", node);

                            } else if (internalAsset) {
                                moveAssetToRejectQueue(session, this.assetTransitionsInternalFolder + "/rejected_internal_assets", node);
                            } else if (eCommAsset) {
                                moveAssetToRejectQueue(session, this.ecommFolder + "/rejects_folder", node);
                            }
                            
                            try {
                                PDMAPICommand rejectAssetCmd = new RejectAssetCommand(PDMAPICommand.METHOD_POST, node);
                                resCode = pdmClient.execute(rejectAssetCmd);
                                log.debug("Response code PDM call : " + resCode);
                            } catch (Exception ex) {
                                log.error("Error calling PDM for Reject Reason" + ex);
                                throw new WorkflowException(ex.getMessage(), ex);
                            }
                            
                            if(resCode == HttpStatus.SC_OK){
                            	Node opmeta = JcrPropertiesUtil.getOperationalNode(node, session);
                            	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PDM_CALL_SENT, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
                            	long count = opmeta.hasProperty(CommonConstants.OPMETA_PDM_CALL_COUNT)? opmeta.getProperty(CommonConstants.OPMETA_PDM_CALL_COUNT).getValue().getLong():0;
                            	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PDM_CALL_COUNT, count+1);
                            	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS, "SUCCESS");
                            }else{
                            	log.debug("Unable to set operational attributes as response code is not 200");
                            	Node opmeta = JcrPropertiesUtil.getOperationalNode(node, session);
                				JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS, "FAILED : Status Code "+resCode);
                            }
                        }
                            
                    	if (metadataNode.hasProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW)
                                && metadataNode.getProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW).getString().contentEquals("yes")) {
                            	
                        	//DAM-320 : populated reporting metadata attribute "Content Ops Reviewed Date".
                            if (repometa != null) {
                            	String contentOpsStr = repometa.hasProperty(CommonConstants.CONTENT_OPS_REVIEWED_DATE)?repometa.getProperty(CommonConstants.CONTENT_OPS_REVIEWED_DATE).getString():null;
            					if (contentOpsStr == null || contentOpsStr.isEmpty()) {
            						JcrUtil.setProperty(repometa, CommonConstants.CONTENT_OPS_REVIEWED_DATE, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
            					}
                            }
                    	}
                    	
                    	// if this is not a product image and content ops review was set to "yes", move it
                    	// no need to continue the flow if this returns true
                        if (handleProductCollateralAsset(session, node)) {
                    		return;
                    	}                    	               	
                    	// DAM:1361- Setting Nightly Publish Date for imageset when imageset is in ecomm folder and is scheduled for movement to approved dam and publish.
                        if (eCommAsset && metadataNode.getPath().contains("_imageset") && metadataNode.hasProperty(CommonConstants.BBBY_IMAGESET_NIGHTLY_PUBLISH)){
                        	if(metadataNode.getProperty(CommonConstants.BBBY_IMAGESET_NIGHTLY_PUBLISH).getString().contentEquals("yes")) {
	                            if (repometa != null) {
	                            	String imagesetNightlyPublishDate = repometa.hasProperty(CommonConstants.IMAGESET_NIGHTLY_PUBLISH_DATE)?repometa.getProperty(CommonConstants.IMAGESET_NIGHTLY_PUBLISH_DATE).getString():null;
	            					if (imagesetNightlyPublishDate == null || imagesetNightlyPublishDate.isEmpty()) {
	            						JcrUtil.setProperty(repometa, CommonConstants.IMAGESET_NIGHTLY_PUBLISH_DATE, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
	            						JcrUtil.setProperty(repometa, CommonConstants.PUBLISHED_TO_S7_BY_USER, initiator);
	            					}
	                            }
                        	}else{
                        		if (repometa != null) {
	                            	String imagesetNightlyPublishDate = repometa.hasProperty(CommonConstants.IMAGESET_NIGHTLY_PUBLISH_DATE)?repometa.getProperty(CommonConstants.IMAGESET_NIGHTLY_PUBLISH_DATE).getString():null;
	            					if (imagesetNightlyPublishDate != null) {
	            						JcrUtil.setProperty(repometa, CommonConstants.IMAGESET_NIGHTLY_PUBLISH_DATE, "");
	            						JcrUtil.setProperty(repometa, CommonConstants.PUBLISHED_TO_S7_BY_USER, "");
	            					}
	                            }
                        	}
                    	}
                        
                       // This functionality is not valid for imagesets.
						if (!metadataNode.getPath().contains("_imageset") && metadataNode.hasProperty(CommonConstants.BBBY_PRIMARY_IMAGE)) {
							if (metadataNode.getProperty(CommonConstants.BBBY_PRIMARY_IMAGE).getString().contentEquals("yes")) {
							// DAM:1461 - Block the imageset creation for invalid file name 
								if (!metadataNode.hasProperty(CommonConstants.BBBY_IMAGE_SET_NAME)) {
									boolean validAssetName = ServiceUtils.validFileName(metadataNode.getParent().getParent().getName());
							        if (!validAssetName) {
							        	metadataNode.setProperty(CommonConstants.BBBY_PRIMARY_IMAGE, "no");
							        	log.debug("Unable to create ImageSet as asset name is not valid: "+metadataNode.getParent().getParent().getName());
							    	}
							        else {
							        	createPrimaryImageSet(workflowSession, node, metadataNode, initiator);
							        } 
								} else {
									log.debug("Unable to create ImageSet as bbby:imageSetName is not null");
								}
							}
						}
                	}
                	
                	//check to see if the asset has an assetMetadata node
                	//Exclude items in the rejects_folder and the duplicates folder from consideration.
                	// Exclude assets that are in e-comm, they will not auto-move
                	if (upcmetadataPath && 
                		!node.getPath().toLowerCase().contains("rejected_vendor_assets") && 
                		!node.getPath().toLowerCase().contains("duplicate_vendor_assets") &&
                		!node.getPath().toLowerCase().contains("rejects_folder") &&
                		!eCommAsset) {
                		
                		Node bbbyAssetMetadataNode = node.getNode(CommonConstants.METADATA_NODE);
                		String departmentNumber = (bbbyAssetMetadataNode.hasProperty(CommonConstants.BBBY_DEPARTMENT_NUMBER)) ? bbbyAssetMetadataNode.getProperty(CommonConstants.BBBY_DEPARTMENT_NUMBER).getString() : "0";  //assuming that dept is the same across UPCs;
                		
                		String isFastTrackScreenAsset = "No";
                		Node operationalmeta = JcrPropertiesUtil.getOperationalNode(node, session);
                		if(operationalmeta!=null){
                			isFastTrackScreenAsset = (operationalmeta.hasProperty(CommonConstants.BBBY_FAST_TRACK_ASSET)) ? operationalmeta.getProperty(CommonConstants.BBBY_FAST_TRACK_ASSET).getString() : "No";  //This attribute is used for determining whether the asset comes from fast track screen or not.
                		}
                		String assetUpdate = JcrUtils.getStringProperty(bbbyAssetMetadataNode, CommonConstants.BBBY_ASSET_UPDATE, "");
                        
                		
                		Node upcMetadataNode = node.getNode(CommonConstants.PDM_METADATA_NODE);
                		
                		NodeIterator upcNodes = upcMetadataNode.getNodes("upc-*");
                		
                		boolean containsUPC = false;
                        boolean containsSKU = false;
                		//boolean containsCollection = false;
                        boolean containsAssociatedCollectionId = false;
                        boolean containsAssociatedWebProductId = false;
                        boolean isBBBYWebOfferedFlag = false;
                        boolean isBABYWebOfferedFlag = false;
                        boolean isCAWebOfferedFlag = false;
                        
                		String webProductId = null;
                		String collectionId = null;
                		
//                		ArrayList<Integer> moveValue = new ArrayList<Integer>(); 
                		
                		boolean isFastTrackPhoto = false;
                		
                		while (upcNodes.hasNext()) {
                			
                			Node upcNode = (Node) upcNodes.next();

                			String upc = (upcNode.hasProperty(CommonConstants.PRIMARY_UPC)) ? upcNode.getProperty(CommonConstants.PRIMARY_UPC).getString() : "";
                            String sku = (upcNode.hasProperty(CommonConstants.SKU)) ? upcNode.getProperty(CommonConstants.SKU).getString() : "";
                			int containsCollections = (upcNode.hasProperty(CommonConstants.ASSOCIATED_COLLECTION_ID)) ? upcNode.getProperty(CommonConstants.ASSOCIATED_COLLECTION_ID).getValues().length : 0;
                			int containsWebProductId = (upcNode.hasProperty(CommonConstants.ASSOCIATED_WEB_PRODUCT_ID)) ? upcNode.getProperty(CommonConstants.ASSOCIATED_WEB_PRODUCT_ID).getValues().length : 0;
                			String bbbyWebOfferedFlag = (upcNode.hasProperty(CommonConstants.BBBY_WEB_OFEERED_FLAG)) ? upcNode.getProperty(CommonConstants.BBBY_WEB_OFEERED_FLAG).getValue().toString() : "No";
                			String babyWebOfferedFlag = (upcNode.hasProperty(CommonConstants.BABY_WEB_OFEERED_FLAG)) ? upcNode.getProperty(CommonConstants.BABY_WEB_OFEERED_FLAG).getValue().toString() : "No";
                			String caWebOfferedFlag = (upcNode.hasProperty(CommonConstants.CA_WEB_OFEERED_FLAG)) ? upcNode.getProperty(CommonConstants.CA_WEB_OFEERED_FLAG).getValue().toString() : "No";
                			
                			String fastTrackFlag = (upcNode.hasProperty(CommonConstants.FAST_TRACK_FLAG)) ? upcNode.getProperty(CommonConstants.FAST_TRACK_FLAG).getValue().toString() : "No";
                			if (fastTrackFlag.equalsIgnoreCase("Yes")) {
                            	isFastTrackPhoto = true;
                            }
                		
                            //create the sieve here
                			
                			if (!upc.contentEquals("")) {
                			
                				containsUPC = true; 
                				
                			}
                            if (!sku.contentEquals("")) {

                                containsSKU = true;

                            }
                            
                            if (bbbyWebOfferedFlag.equalsIgnoreCase("Yes")) {
                            	isBBBYWebOfferedFlag = true;
                            }
                            if (babyWebOfferedFlag.equalsIgnoreCase("Yes")) {
                            	isBABYWebOfferedFlag = true;
                            }
                            if (caWebOfferedFlag.equalsIgnoreCase("Yes")) {
                            	isCAWebOfferedFlag = true;
                            }
                			
                            if (containsCollections != 0) {
                            	containsAssociatedCollectionId = true;
                                
                            	if(StringUtils.isEmpty(collectionId)) {
                            		Value[] collectionIds =  upcNode.getProperty(CommonConstants.ASSOCIATED_COLLECTION_ID).getValues();
                            		collectionId = collectionIds[0].getString();
                            	}
                            }
                                

                            if (containsWebProductId != 0) {
                            	containsAssociatedWebProductId = true;
                            	
                            	if(StringUtils.isEmpty(webProductId)) {
                            		Value[] webProductIds =  upcNode.getProperty(CommonConstants.ASSOCIATED_WEB_PRODUCT_ID).getValues();
                            		webProductId = webProductIds[0].getString();
                            	}
                            	
                            }

                            // TODO: Cleanup Unmatched code. BD-201
                		}

                		if (vendorAsset) {
                            moveVendorAsset(node, session, departmentNumber, containsUPC, containsSKU, 
                            		containsAssociatedCollectionId, collectionId, containsAssociatedWebProductId, webProductId, 
                            		isBBBYWebOfferedFlag, isBABYWebOfferedFlag, isCAWebOfferedFlag, isFastTrackScreenAsset, isFastTrackPhoto, assetUpdate);
                        } else {
                            moveInternalAsset(node, session, departmentNumber, containsUPC, containsSKU,
                            		containsAssociatedCollectionId, collectionId, 
                            		containsAssociatedWebProductId, webProductId, isBBBYWebOfferedFlag, isBABYWebOfferedFlag, isCAWebOfferedFlag);
                        }
                	}
                	
                	//asset did not qualify because it had no UPC values
                	
                }

            
            } catch (Exception e) {
                // If an error occurs that prevents the Workflow from completing/continuing - Throw a WorkflowException
                // and the WF engine will retry the Workflow later (based on the AEM Workflow Engine configuration).
                log.error("Unable to complete processing the Workflow Process step, for path: " + path, e);
                throw new WorkflowException(e.getMessage(), e);
            } 
        }
    }

    /**
     * @param assetJCRNode
     * @param session
     * @param departmentNumber
     * @param containsUPC
     * @param containsSKU
     * @param containsassociatedCollectionID
     * @param containsWebProductID
     * @throws ValueFormatException
     * @throws RepositoryException
     * @throws com.day.cq.workflow.WorkflowException
     */
    private void moveInternalAsset(Node assetJCRNode, Session session, String departmentNumber, boolean containsUPC, boolean containsSKU, 
								   boolean containsassociatedCollectionID, String collectionId, boolean containsWebProductID, String productId, boolean isBBBYWebOfferedFlag, boolean isBABYWebOfferedFlag, boolean isCAWebOfferedFlag)
								   throws ValueFormatException, RepositoryException, com.day.cq.workflow.WorkflowException {
    	
    	log.debug("entering moveInternalAsset() method");
    	ASSET_TYPE assetType = getAssetType(assetJCRNode);

        boolean contains_UPC_OR_SKU = containsUPC || containsSKU;
//        boolean contains_WebProductID_OR_CollectionID = containsWebProductID || containsassociatedCollectionID;
        boolean isWebOfferedFlag = isBBBYWebOfferedFlag || isBABYWebOfferedFlag || isCAWebOfferedFlag;
		if (!isWebOfferedFlag){
			log.warn("None of the web offered flags is true for the asset:" + assetJCRNode.getPath());
		}
        if (assetType != null) {
            if (assetType == ASSET_TYPE.PRODUCT_IMAGE && !assetJCRNode.getName().toLowerCase().contains(".pdf") ) {
                if (contains_UPC_OR_SKU && isWebOfferedFlag) {
                    moveAssetToEcomm(session, departmentNumber, assetJCRNode, containsassociatedCollectionID, collectionId, productId);
                }
            } else if (assetType == ASSET_TYPE.PRODUCT_COLLEATERAL_VIDEO) {
                if (contains_UPC_OR_SKU && isWebOfferedFlag) {
                    handleProductCollateralAsset(session, assetJCRNode);
                }
            } else if (assetType == ASSET_TYPE.NON_PRODUCT) {
                //asset remains where it is, it was modified, but not in a way we care about
            	log.warn("None of the web offered flags is true for the asset:" + assetJCRNode.getPath());
            }
        }
    }

    private void moveVendorAsset(Node assetJCRNode, Session session, String departmentNumber,
                                 boolean containsUPC, boolean containsSKU, boolean containsassociatedCollectionID, String collectionId,
                                 boolean containsWebProductID, String productId, boolean isBBBYWebOfferedFlag, boolean isBABYWebOfferedFlag, boolean isCAWebOfferedFlag,
                                 String isFastTrackScreenAsset, boolean isFastTrackPhoto, String assetUpdate)
                                 throws ValueFormatException, RepositoryException, com.day.cq.workflow.WorkflowException {
        ASSET_TYPE assetType = getAssetType(assetJCRNode);
        boolean contains_WebProductID_OR_CollectionID = containsWebProductID || containsassociatedCollectionID;
        boolean isWebOfferedFlag = isBBBYWebOfferedFlag || isBABYWebOfferedFlag || isCAWebOfferedFlag;
        if (!isWebOfferedFlag){
			log.warn("None of the web offered flags is true for the asset:" + assetJCRNode.getPath());
		}
        if (assetType != null) {
            if (assetType == ASSET_TYPE.PRODUCT_IMAGE) {
                if (containsUPC && !contains_WebProductID_OR_CollectionID) {
                	log.warn("Unable to move PRODUCT_IMAGE, Both WebProductID and CollectionID have a null value, at least one of them should have value to move the asset " + assetJCRNode.getPath());
//                    moveAssetToNotAssigned(session, assetJCRNode);
                } else if (containsUPC && isWebOfferedFlag) {
                	if(isFastTrackScreenAsset.equalsIgnoreCase("yes") && isFastTrackPhoto && !assetUpdate.equalsIgnoreCase("yes")){
                		Node metadataNodeFastTrack = assetJCRNode.getNode(CommonConstants.METADATA_NODE);
                		boolean isImageSetExists = false;
                		isImageSetExists = ServiceUtils.isImagesetExist(session, metadataNodeFastTrack);
                		if(!isImageSetExists){
                			moveAssetToFastTrack(session, assetJCRNode);
                		}else if(!assetJCRNode.getName().toLowerCase().contains(".pdf")){
                    		moveAssetToEcomm(session, departmentNumber, assetJCRNode, containsassociatedCollectionID,
                            		collectionId, productId);
                    	}
                	}else if(!assetJCRNode.getName().toLowerCase().contains(".pdf")){
                		moveAssetToEcomm(session, departmentNumber, assetJCRNode, containsassociatedCollectionID,
                        		collectionId, productId);
                	}
                }
            } else if (assetType == ASSET_TYPE.PRODUCT_COLLEATERAL_VIDEO) {
                if (containsUPC && !contains_WebProductID_OR_CollectionID) {
                	log.warn("Unable to move PRODUCT_COLLEATERAL_VIDEO, Both WebProductID and CollectionID have a null value, at least one of them should have value to move the asset " + assetJCRNode.getPath());
//                    moveAssetToNotAssigned(session, assetJCRNode);
                } else if (containsUPC && isWebOfferedFlag) {
                    handleProductCollateralAsset(session, assetJCRNode);
                }
            } else if (assetType == ASSET_TYPE.NON_PRODUCT) {
                //asset remains where it is, it was modified, but not in a way we care about
            	log.warn("It is non product, it remains where it is " + assetJCRNode.getPath());
            }
        }
    }

    private ASSET_TYPE getAssetType(Node assetJCRNode) throws ValueFormatException, RepositoryException {
    	log.debug("entering getAssetType() method");
        if (startsWithTag(assetJCRNode, TAG_PRODUCT_IMAGE)) {
            return ASSET_TYPE.PRODUCT_IMAGE;
        } else if (startsWithTag(assetJCRNode, TAG_PRODUCT_COLLATERAL) || startsWithTag(assetJCRNode, TAG_PRODUCT_VIDEOS) || startsWithTag(assetJCRNode, TAG_PRODUCT_3D_RENDERS)) {
            return ASSET_TYPE.PRODUCT_COLLEATERAL_VIDEO;
        } else if (startsWithTag(assetJCRNode, TAG_NON_PRODUCT) || startsWithTag(assetJCRNode, TAG_CONTENT) || startsWithTag(assetJCRNode, TAG_INSTITUTIONAL) || startsWithTag(assetJCRNode, TAG_CONCEPT_CREATIVE_BRAND)) {
            return ASSET_TYPE.NON_PRODUCT;
        }

        return null;

    }


    private boolean startsWithTag(Node node, String tagValue) throws ValueFormatException, IllegalStateException, RepositoryException {
    	log.debug("entering startsWithTag() method");
    	Node meta = node.getNode(CommonConstants.METADATA_NODE);
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
	            if (tag != null && tag.startsWith(tagValue)) {
	            return true;
	          }
	        }
        }
        return false;
    }
    
    /**
     * @param workflowSession
     * @param node
     * @param metadataNode
     * @throws WorkflowException 
     */
    private void createPrimaryImageSet(WorkflowSession workflowSession, Node node, Node metadataNode, String initiator) throws WorkflowException {
    	log.debug("entering createPrimaryImageSet() method");
    	//TODO add tags to imageset
        
        Session session = workflowSession.adaptTo(Session.class);
        ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class); 
        
        try {

            String fileNameFull = node.getPath();
            
            Node directoryNode = node.getParent();
            
            Path p = Paths.get(fileNameFull);
            String fileName = p.getFileName().toString();
            
            String directory = directoryNode.getPath();

            Resource damResource = resourceResolver.resolve(directory);
            Resource image1r = resourceResolver.resolve(fileNameFull);
            Asset asset = image1r.adaptTo(Asset.class);
            
            //ImageSet is not supposed to be publicly accessible, this might change in the future
            
            
            String imageSetName = fileName.substring(0, fileName.lastIndexOf(".")) + "_imageset";
            String imagesetPath = directory+"/"+imageSetName;
            try {
                @SuppressWarnings("deprecation")
                ImageSet imageset = S7SetHelper.createS7ImageSet(damResource, imageSetName, null);
                log.debug(imageset.getPath());
                imageset.add(asset);
                imagesetPath = imageset.getPath();
            } catch (Exception e) {
                log.error("failed to create ImageSet as it already Exist", e);
            }
            
            Node imageMeta = session.getNode(imagesetPath).getNode(CommonConstants.METADATA_NODE);
            metadataNode.setProperty(CommonConstants.BBBY_IMAGE_SET_NAME, imageSetName);
            
            //brand the imageset with the asset tags and decorators
            
            ArrayList<String> tagList = getImagesetTags(node);
            
            imageMeta.setProperty(CommonConstants.CQ_TAGS, (String[]) tagList.toArray(new String[tagList.size()]));
            
            //./jcr:content/metadata/bbby:primaryUpc
            //./jcr:content/metadata/bbby:sku
            
            String primaryUPC = (metadataNode.hasProperty(CommonConstants.BBBY_UPC)) ? metadataNode.getProperty(CommonConstants.BBBY_UPC).getString() : "";
            String sku = (metadataNode.hasProperty(CommonConstants.BBBY_SKU)) ? metadataNode.getProperty(CommonConstants.BBBY_SKU).getString() : "";

            imageMeta.setProperty(CommonConstants.BBBY_UPC, primaryUPC);
            imageMeta.setProperty(CommonConstants.BBBY_SKU, sku);
            imageMeta.setProperty(CommonConstants.BBBY_ASSET_UPDATE, "no");
            imageMeta.setProperty("dc:title", imageSetName);
            
          //DAM-567 : populated reporting metadata attribute "ImageSet Created By".
            Node repometa = JcrPropertiesUtil.getReportingNode(session.getNode(imagesetPath), session);
            if (repometa != null) {
            	log.debug("setting IMAGESET_CREATED_BY {} : {} ", imagesetPath, initiator);
				JcrUtil.setProperty(repometa, CommonConstants.IMAGESET_CREATED_BY, initiator);
			}

//            session.save();
            
        } catch (Exception e) {
            
            log.error("failed to create ImageSet", e);
            throw new WorkflowException(e.getMessage(), e);
            
        }
        
    }
    
    private ArrayList<String> getImagesetTags(Node node) throws WorkflowException {
    	log.debug("entering getImagesetTags() method");
    	ArrayList<String> tagList = new ArrayList<String>();
    	
    	//We always want to move the image set to this directory. It overwrites the images asset_type
    	tagList.add("bbby:asset_type/approved_dam/assets/product/images/image_sets");
    	
    	try {
    		
    		Node meta = node.getNode(CommonConstants.METADATA_NODE);
    		
    		Property tagP = meta.getProperty(CommonConstants.CQ_TAGS);
    		
    		Value[] tagV = tagP.getValues();
    		
    		//Add other tags to the image set
    		for (Value v : tagV) {
    			
    			if (!v.getString().contains("asset_type")) {
    				
        			tagList.add(v.getString());
    				
    			}
    			
    			
    		}
    		
    	} catch (Exception e) {
    		
    		log.error("Unable to determine asset tags", e);
    		throw new WorkflowException(e.getMessage(), e);
    		
    	}
    	
    	return tagList;
    }
    
	
    
    /**
     * @param session
     * @param node
     */
    @SuppressWarnings("unused")
	private void moveAssetToNotAssigned(Session session, Node node) {
    	log.debug("entering moveAssetToNotAssigned() method");
    	try {

    		String origin = node.getPath();

			Path p = Paths.get(origin);
			String file = p.getFileName().toString();
    		
    		Node dateFolder = getOrCreateDatePath(session, node, this.notassignedFolder);
    		
    		String vendorPath = getOrCreateVendorPath(session, node, dateFolder.getPath());
    		//Save the session to confirm that folders created successfully.
            session.save();
    		
    		String destination = vendorPath + "/" + file;
    		
        	session.move(origin, destination);
    		
    		
    	} catch (Exception e) {
    		
    		String message = e.getMessage();
    		log.error(message);
    		
    		e.printStackTrace();
    		
    	}

    }

    /**
     * @param session
     * @param node
     * @return
     */
    private Node getOrCreateDatePath(Session session, Node node, String rootPath) {
    	log.debug("entering getOrCreateDatePath() method");
    	Node targetFolder = null;
    	
		try {
			Calendar createdDate = Calendar.getInstance();
			//Calendar createdDate = node.getProperty(CommonConstants.JCR_CREATED).getDate();

			String dayFormatted = dateFormatter.format(createdDate.getTime());
			targetFolder = JcrUtil.createPath(rootPath + "/" + dayFormatted, "sling:Folder", session);
			
			
		} catch (Exception e) {

			String message = e.getMessage();
			log.error(message);

			e.printStackTrace();

		}
        
        return targetFolder;
    }
    
    
    /**
     * @param session
     * @param node
     * @param rootPath
     * @return
     */
    private String getOrCreateVendorPath(Session session, Node node, String rootPath) {
    	log.debug("entering getOrCreateVendorPath() method");
    	String targetPath = null;
    	
		try {

			String validVendorName = "vendor-undefined";
			String vendor = null;
			
			if(node.hasProperty("jcr:content/upcmetadata/upc-0/primaryVendorName")) {
				
				vendor = node.getProperty("jcr:content/upcmetadata/upc-0/primaryVendorName").getString();
				validVendorName = JcrUtil.createValidName(vendor,
						JcrUtil.HYPHEN_LABEL_CHAR_MAPPING, "-");
				
			} 
			
			String vendorPath = rootPath + "/" + validVendorName;

			if (!session.nodeExists(vendorPath)) {
				log.debug("Creating " + vendorPath);
				Node vendorFolder = JcrUtil.createPath(vendorPath, "sling:Folder", session);
				if (vendorFolder != null && StringUtils.isNotEmpty(vendor)) {
					vendorFolder.setProperty("jcr:title", vendor);
				}

//				session.save();
			}
			
			targetPath = vendorPath;
			
		} catch (Exception e) {

			String message = e.getMessage();
			log.error(message);

			e.printStackTrace();

		}
        
        return targetPath;
    }
    
    private boolean handleProductCollateralAsset(Session session, Node node)
        throws RepositoryException, com.day.cq.workflow.WorkflowException {
    	log.debug("entering handleProductCollateralAsset() method");
        ASSET_TYPE asset_type = getAssetType(node);
        if (asset_type != ASSET_TYPE.PRODUCT_COLLEATERAL_VIDEO) {
			return false;
		}
		
		Node metadataNode = node.getNode(CommonConstants.METADATA_NODE);

        if (startsWithTag(node, TAG_PRODUCT_COLLATERAL)) {
            if (metadataNode.hasProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW)
                && metadataNode.getProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW).getString().contentEquals("yes")) {
            	
                if (hasUpcAndWebOfferedFlags(node)) {
//				moveToNonProduct(session, node, true);
                	log.debug("Start bbby-product-collateral-asset workflow on payload{}", node.getPath());
                    wfService.startWorkflow(session, this.bbbyProductCollateralAssetModel, node.getPath());
                    return true;
                }
            }
        } else if (startsWithTag(node, TAG_PRODUCT_VIDEOS)) {
            if (hasUpcWebOfferedFlagNotWebDisabled(node)) {
            	log.debug("Start bbby-product-collateral-asset workflow on payload{}", node.getPath());
                wfService.startWorkflow(session, this.bbbyProductCollateralAssetModel, node.getPath());
                return true;
            }
        } else if (startsWithTag(node, TAG_PRODUCT_3D_RENDERS)) {
            if (hasUpcAndWebOfferedFlags(node)) {
            	log.debug("Start bbby-product-collateral-asset workflow on payload{}", node.getPath());
                wfService.startWorkflow(session, this.bbbyProductCollateralAssetModel, node.getPath());
                return true;
            }
        }

		return false;
	}
    
	/**
	 * @param node
	 * 
	 * @return
	 * @throws ValueFormatException
	 * @throws PathNotFoundException
	 * @throws RepositoryException
	 */
    private boolean hasUpcAndWebOfferedFlags(Node node) throws ValueFormatException, PathNotFoundException, RepositoryException {
    	log.debug("entering hasUpcAndWebOfferedFlags() method");
		boolean result = false;
		if(node.hasNode(CommonConstants.PDM_METADATA_NODE)){
			Node upcMetadataNode = node.getNode(CommonConstants.PDM_METADATA_NODE);
			NodeIterator upcNodes = upcMetadataNode.getNodes("upc-*");
			boolean containsUPC = false;
	        boolean containsSKU = false;
	        boolean isBBBYWebOfferedFlag = false;
	        boolean isBABYWebOfferedFlag = false;
	        boolean isCAWebOfferedFlag = false;
			
			while (upcNodes.hasNext()) {
				Node upcNode = (Node) upcNodes.next();
				String upc = (upcNode.hasProperty(CommonConstants.PRIMARY_UPC)) ? upcNode.getProperty(CommonConstants.PRIMARY_UPC).getString() : "";
	            String sku = (upcNode.hasProperty(CommonConstants.SKU)) ? upcNode.getProperty(CommonConstants.SKU).getString() : "";
	            String bbbyWebOfferedFlag = (upcNode.hasProperty(CommonConstants.BBBY_WEB_OFEERED_FLAG)) ? upcNode.getProperty(CommonConstants.BBBY_WEB_OFEERED_FLAG).getValue().toString() : "No";
    			String babyWebOfferedFlag = (upcNode.hasProperty(CommonConstants.BABY_WEB_OFEERED_FLAG)) ? upcNode.getProperty(CommonConstants.BABY_WEB_OFEERED_FLAG).getValue().toString() : "No";
    			String caWebOfferedFlag = (upcNode.hasProperty(CommonConstants.CA_WEB_OFEERED_FLAG)) ? upcNode.getProperty(CommonConstants.CA_WEB_OFEERED_FLAG).getValue().toString() : "No";
				
	//			int containsCollections = (upcNode.hasProperty(CommonConstants.ASSOCIATED_COLLECTION_ID)) ? upcNode.getProperty(CommonConstants.ASSOCIATED_COLLECTION_ID).getValues().length : 0;
	//			int containsWebProductId = (upcNode.hasProperty("associatedWebProductID")) ? upcNode.getProperty("associatedWebProductID").getValues().length : 0;
				
				if (!upc.contentEquals("")) {
					containsUPC = true; 
				}
	            if (!sku.contentEquals("")) {
	                containsSKU = true;
	            }
	            if (bbbyWebOfferedFlag.equalsIgnoreCase("Yes")) {
                	isBBBYWebOfferedFlag = true;
                }
                if (babyWebOfferedFlag.equalsIgnoreCase("Yes")) {
                	isBABYWebOfferedFlag = true;
                }
                if (caWebOfferedFlag.equalsIgnoreCase("Yes")) {
                	isCAWebOfferedFlag = true;
                }	
			}
			if ((containsUPC || containsSKU) && (isBBBYWebOfferedFlag || isBABYWebOfferedFlag || isCAWebOfferedFlag)) {
				result = true;
			} 
		}
		log.debug("hasUpcAndWebOfferedFlags() method returns " + result);
		return result;
	}
    
    private boolean hasUpcWebOfferedFlagNotWebDisabled(Node node) throws ValueFormatException, PathNotFoundException, RepositoryException {
    	log.debug("entering hasUpcWebOfferedFlagNotWebDisabled() method");
		boolean result = false;
		if(node.hasNode(CommonConstants.PDM_METADATA_NODE)){
			Node upcMetadataNode = node.getNode(CommonConstants.PDM_METADATA_NODE);
			NodeIterator upcNodes = upcMetadataNode.getNodes("upc-*");
			boolean containsUPC = false;
	        boolean containsSKU = false;
	        boolean isBBBYWebOfferedFlag = false;
	        boolean isBABYWebOfferedFlag = false;
	        boolean isCAWebOfferedFlag = false;
			boolean isBBBYWebDisabled = false;
            boolean isBABYWebDisabled = false;
            boolean isCAWebDisabled = false;
			
			while (upcNodes.hasNext()) {
				Node upcNode = (Node) upcNodes.next();
				String upc = (upcNode.hasProperty(CommonConstants.PRIMARY_UPC)) ? upcNode.getProperty(CommonConstants.PRIMARY_UPC).getString() : "";
	            String sku = (upcNode.hasProperty(CommonConstants.SKU)) ? upcNode.getProperty(CommonConstants.SKU).getString() : "";
	            String bbbyWebOfferedFlag = (upcNode.hasProperty(CommonConstants.BBBY_WEB_OFEERED_FLAG)) ? upcNode.getProperty(CommonConstants.BBBY_WEB_OFEERED_FLAG).getValue().toString() : "No";
    			String babyWebOfferedFlag = (upcNode.hasProperty(CommonConstants.BABY_WEB_OFEERED_FLAG)) ? upcNode.getProperty(CommonConstants.BABY_WEB_OFEERED_FLAG).getValue().toString() : "No";
    			String caWebOfferedFlag = (upcNode.hasProperty(CommonConstants.CA_WEB_OFEERED_FLAG)) ? upcNode.getProperty(CommonConstants.CA_WEB_OFEERED_FLAG).getValue().toString() : "No";
				String bbbyWebDisabled = (upcNode.hasProperty(CommonConstants.BBBY_WEB_DISABLED)) ? upcNode.getProperty(CommonConstants.BBBY_WEB_DISABLED).getValue().toString() : "No";
    			String babyWebDisabled = (upcNode.hasProperty(CommonConstants.BABY_WEB_DISABLED)) ? upcNode.getProperty(CommonConstants.BABY_WEB_DISABLED).getValue().toString() : "No";
    			String caWebDisabled = (upcNode.hasProperty(CommonConstants.CA_WEB_DISABLED)) ? upcNode.getProperty(CommonConstants.CA_WEB_DISABLED).getValue().toString() : "No";
    			
    			if (!upc.contentEquals("")) {
					containsUPC = true; 
				}
	            if (!sku.contentEquals("")) {
	                containsSKU = true;
	            }
	            if (bbbyWebOfferedFlag.equalsIgnoreCase("Yes")) {
                	isBBBYWebOfferedFlag = true;
                }
                if (babyWebOfferedFlag.equalsIgnoreCase("Yes")) {
                	isBABYWebOfferedFlag = true;
                }
                if (caWebOfferedFlag.equalsIgnoreCase("Yes")) {
                	isCAWebOfferedFlag = true;
                }	
    			if (bbbyWebDisabled.equalsIgnoreCase("Yes")) {
                	isBBBYWebDisabled = true;
                }
                if (babyWebDisabled.equalsIgnoreCase("Yes")) {
                	isBABYWebDisabled = true;
                }
                if (caWebDisabled.equalsIgnoreCase("Yes")) {
                	isCAWebDisabled = true;
                }
			}
			if ((containsUPC || containsSKU) && ((isBBBYWebOfferedFlag && !isBBBYWebDisabled) || (isBABYWebOfferedFlag && !isBABYWebDisabled) || (isCAWebOfferedFlag && !isCAWebDisabled))) {
				result = true;
			} 
		}
		log.debug("hasUpcWebOfferedFlagNotWebDisabled() method returns " + result);
		return result;
	}
	


    private void moveAssetToEcomm(Session session, String departmentNumber, Node node, 
    		boolean containsCollection, String collectionId, String webProductId) {
    	log.debug("entering moveAssetToEcomm() method");
    	try {
    		
    		//DAM-320 : populated reporting metadata attribute "Ecommerce Entry Date".
    		Node repometa = JcrPropertiesUtil.getReportingNode(node, session);
			if (repometa != null) {
				JcrUtil.setProperty(repometa, CommonConstants.ECOMM_ENTRY_DATE, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
			}

    		String origin = node.getPath();
    		
    		Path p = Paths.get(origin);
    		String file = p.getFileName().toString();
            Node metadataNode = node.getNode(CommonConstants.METADATA_NODE);
            Node targetNode = null;

            String assetUpdate = JcrUtils.getStringProperty(metadataNode, CommonConstants.BBBY_ASSET_UPDATE, "");

            if (containsCollection)
                targetNode = createTargetNode(assetUpdate, departmentNumber, collectionId, session);
            else
                targetNode = createTargetNode(assetUpdate, departmentNumber, webProductId, session);
            
          //Save the session to confirm that folders created successfully.
            session.save();
    		String destination = targetNode.getPath() + "/" + file;
    		
        	session.move(origin, destination);
    		
    		
    	} catch (Exception e) {
    		
    		String message = e.getMessage();
    		log.error(message);
    		
    		e.printStackTrace();
    		
    	}
    
    }

    /**
     * create's target node based on collection or web product id
     *
     * @param assetUpdate
     * @param departmentNumber
     * @param id
     * @param session
     * @throws RepositoryException
     */
    private Node createTargetNode(String assetUpdate, String departmentNumber, String id, Session session) throws RepositoryException {
    	
        Node targetNode = null;
        if (assetUpdate.equalsIgnoreCase("yes"))
            targetNode = JcrUtil.createPath(this.ecommFolder + "/" + departmentNumber + "/update/" + id, "sling:Folder", session);
        else if (assetUpdate.equalsIgnoreCase("no"))
            targetNode = JcrUtil.createPath(this.ecommFolder + "/" + departmentNumber + "/new/" + id, "sling:Folder", session);
        return targetNode;
    }


    private void moveAssetToRejectQueue(Session session, String rejectMoveTarget, Node node) {
    	log.debug("entering moveAssetToRejectQueue() method");
        try {

            String origin = node.getPath();

            Path p = Paths.get(origin);
            String file = p.getFileName().toString();


            Node dateFolder = getOrCreateDatePath(session, node, rejectMoveTarget);
          //Save the session to confirm that folders created successfully.
            session.save();
            String destination = dateFolder.getPath() + "/" + file;

            session.move(origin, destination);
            log.warn("Asset has been rejected and moved to location:"+destination);


        } catch (Exception e) {

            String message = e.getMessage();
            log.error(message);

            e.printStackTrace();

        }

    }
    
    private void moveAssetToFastTrack(Session session, Node node){
    	log.debug("entering moveAssetToFastTrack() method");
    	try {
    		
    		//DAM-320 : populated reporting metadata attribute "Ecommerce Entry Date".
    		Node repometa = JcrPropertiesUtil.getReportingNode(node, session);
			if (repometa != null) {
				JcrUtil.setProperty(repometa, CommonConstants.ECOMM_ENTRY_DATE, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
			}
    		String origin = node.getPath();
    		Path p = Paths.get(origin);
    		String file = p.getFileName().toString();
            Node metadataNode = node.getNode(CommonConstants.METADATA_NODE);
            Node targetNode = null;
            String batchId = (metadataNode.hasProperty("bbby:batchID")) ? metadataNode.getProperty("bbby:batchID").getString() : ""; 
    		//String sequenceNumber = (metadataNode.hasProperty(CommonConstants.BBBY_SEQUENCE)) ? metadataNode.getProperty(CommonConstants.BBBY_SEQUENCE).getString() : ""; 
    		
    		String isSharedAsset = "No";
    		Node operationalmeta = JcrPropertiesUtil.getOperationalNode(node, session);
    		if(operationalmeta!=null){
    			isSharedAsset = (operationalmeta.hasProperty(CommonConstants.BBBY_SHARED_ASSET)) ? operationalmeta.getProperty(CommonConstants.BBBY_SHARED_ASSET).getString() : "No";  //This attribute is used for determining whether the asset is shared or not.
    		}
    		
            targetNode = createTargetNodeFastTrack(session, isSharedAsset, batchId);
          //Save the session to confirm that folders created successfully.
            session.save();
    		String destination = targetNode.getPath() + "/" + file;
        	session.move(origin, destination);
    	} catch (Exception e) {	
    		String message = e.getMessage();
    		log.error(message); 		
    		e.printStackTrace();   		
    	}
    }
    
	private Node createTargetNodeFastTrack(Session session, String isSharedAsset, String batchId) throws RepositoryException {
        Node targetNode = null;
    	if(isSharedAsset.equalsIgnoreCase("yes")){
    		targetNode = JcrUtil.createPath(this.ecommFolder + "/fasttrack/shared/" + batchId , "sling:Folder", session);
    	}else{
    		targetNode = JcrUtil.createPath(this.ecommFolder + "/fasttrack/nonshared" + batchId, "sling:Folder", session);
    	}
        return targetNode;
    }
	
}
