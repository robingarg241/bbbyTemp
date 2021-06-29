package com.bbby.aem.core.workflow.publish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.workflow.impl.BBBYCleanupConfiguration;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;    

/**
 * @author Sandeep
 *
 */
@Component(service = WorkflowProcess.class, property = {
    "process.label=Publish Validation",
    "Constants.SERVICE_VENDOR=BBBY",
    "Constants.SERVICE_DESCRIPTION=Cleanup project assets, tag and move to general population"})
@Designate(ocd = BBBYCleanupConfiguration.class)


public class CheckAllValidation implements WorkflowProcess {


    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String ASSET_TYPE_TAG_ROOT = "bbby:asset_type";

    private ResourceResolver resourceResolver = null;
    
    private static String workflowName = "/var/workflow/models/bbby-approve-and-publish";

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        WorkflowData workflowData = workItem.getWorkflowData();
        log.debug("entering execute with payload {}: ", workflowData.getPayload().toString());
        String type = workflowData.getPayloadType();
        resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
        log.info("entering execute of validation");
        boolean validation = true;
        String opmessage = null;
        if (CommonConstants.JCR_PATH.equalsIgnoreCase(type)) {
            String path = workflowData.getPayload().toString();
            Session session = null;
            
            try {
                session = workflowSession.adaptTo(Session.class);
                
                //Check asset is exist and it is not from approved dam.
                if(session.itemExists(path)&& !path.contains("content/dam/bbby/approved_dam")){
                	log.info("asset exist and it is not from approved dam");
					  
                	//Check same workflow is running or not.
					if (!checkSameWorkflowNotRunning(path, workItem.getWorkflow().getId())) {
						log.info("same wf is not running");
						List<Node> assets = new ArrayList<Node>();
						boolean hasReferences = false;
						Node asset = session.getNode(path);
						Resource image1r = resourceResolver.resolve(path);
						 //Check for imageset
						if (S7SetHelper.isS7Set(image1r)) {
							log.info("publishing imageset");
							@SuppressWarnings("deprecation")
							ImageSet imageSet = image1r.adaptTo(ImageSet.class);
							if (imageSet != null) {
								assets = listAssetsOfImageset(imageSet, session);
							}
							//DAM-511 : image and imageset need to have reference 
							if (assets != null && assets.size() > 0) {
								hasReferences = checkReferences(asset, assets, session);
							}
							assets.add(asset);
							if (hasReferences) {
								log.info("reference check successful");
								 //Check for scene 7 sync
								if (checkAssetsSyncToS7(assets)) {
									log.info("scene 7 sync check successful");
								} else {
									validation = false;
									log.info("Unable to start Project CleanUp Workflow, One or more assets are not sync with Scene7. Please Check Operational Attribute of added Assets.");
									opmessage = "Failed : One or more assets are not sync with Scene7";
									
								}
							} else {
								validation = false;
								log.info("Unable to start Project CleanUp Workflow, One or more assets are not have reference. Please Check Operational Attribute of added Assets.");
								opmessage = "Failed : image and imageset need to have reference";
							
							}
						} else {
							log.info("its not an imageset");
							assets.add(asset);
							
						} 
						workflowData.getMetaDataMap().put("assetkey", assets);
						//Check for cq tags
					boolean istagvalid = checktags(assets);
					if(istagvalid){
						log.info("tags and assetype check is valid");
					}
					else{
						validation = false;
						log.info("tags and assetype check is not valid");
						opmessage = "Failed : tag and assettype validation not successful";
					}
						
					} else {
						validation = false;
						opmessage = "Failed : Already Running BBBY Project CleanUp Workflow";
						
					}
				} else {
					validation = false;
					opmessage = "Failed :DAM Asset does not exist at Path";
					log.info("DAM Asset does not exist at Path {} ", path);
				}

            } catch (Exception e) {
                log.error("Unable to complete processing the Workflow Process step, for path: " + path, e);
            }
        }else{
        	validation = false;
        	opmessage = "Failed : type of payload is not"+ CommonConstants.JCR_PATH;
        	log.info("Unable to execute workflow, type of payload is not " + CommonConstants.JCR_PATH);
        }
    	
        if(validation){
        	   workflowData.getMetaDataMap().put("validstatus", true);
  		  log.info("all validations are successful"+workflowData.getMetaDataMap().get("validstatus"));
		}
		else{
			   workflowData.getMetaDataMap().put("validstatus", false);
			log.info("validations step failed"+workflowData.getMetaDataMap().get("validstatus"));
		}
      workflowData.getMetaDataMap().put("opmsg", opmessage);  
      workflowSession.updateWorkflowData(workItem.getWorkflow(), workflowData);
    }
	
	//DAM-511 : No other instance of the Project Clean Up WF on the same Asset should be running.
	private boolean checkSameWorkflowNotRunning(String path, String workflowId) throws Exception {
		log.debug("entering checkSameWorkflowNotRunning() method");
		boolean isAlreadyRunning = false;
		List<Workflow> workflows = ServiceUtils.listRunningWorkflowonPayload(path, true, resourceResolver);
		if (workflows != null && workflows.size() > 0) {
			for (Workflow workflow : workflows) {
				if(workflow.getWorkflowModel().getId().equalsIgnoreCase(workflowName) && ! workflow.getId().equalsIgnoreCase(workflowId) && workflow.getState().equalsIgnoreCase("RUNNING")){
					isAlreadyRunning = true;
					log.info("Already Running BBBY Project CleanUp Workflow with Id {} on path {}", workflow.getId(), path);
				}
			}
		}
		return isAlreadyRunning; 
	}
	

	private boolean checkAssetsSyncToS7(List<Node> nodes) throws Exception {
		log.debug("entering checkAssetsSyncToS7() method");
		boolean isSync = true;
		for (Node node : nodes) {
			String payload = node.getPath();
			Node meta = node.getNode(CommonConstants.METADATA_NODE);
			if (meta.hasProperty(CommonConstants.DAM_SCENE_7_FILENAME)) {
				log.info("Already sync with Scene7 : path {}", payload);
			} else {
				log.info("Not synced with Scene7 : path {}", payload);
				isSync = false;
			}
		}
		return isSync;
	}
	
	// DAM-511 : Check references of imageset in every payload.
	private boolean checkReferences(Node asset, List<Node> nodes, Session session) throws Exception {
		log.debug("entering checkReferences() method");
		boolean hasRefereces = false;
		ArrayList<String> memberList = ServiceUtils.getMembersOfImageset(session, asset.getPath());
		if (memberList != null && memberList.size() > 0) {
			if(memberList.size() == nodes.size()){
				hasRefereces = true;
				log.info("Reference count matches with node count");
			}else{
				log.info("Reference count is mismatch with node count");
			}
		} else {
			log.info("Do not have reference : 0 references");
		}
		return hasRefereces;
	}
	
	private boolean checktags(List<Node> assets) throws WorkflowException{
		boolean finalstatus = true;
		for (Node asset : assets) {
			 try { 
				 boolean isValidAsset = true;
	                String assetType = null;
	                Node meta = asset.getNode(CommonConstants.METADATA_NODE);
	                if (meta != null) {
	                    // Check for valid asset type
	                    if (meta.hasProperty(CommonConstants.CQ_TAGS)) {
	                        javax.jcr.Property tagsProperty = meta.getProperty(CommonConstants.CQ_TAGS);
	                        if (tagsProperty != null) {
	                            Value[] values = tagsProperty.getValues();
	                            for (Value value : values) {
	                                if (value.getString().startsWith(ASSET_TYPE_TAG_ROOT)) {
	                                    if (assetType == null) {
	                                        assetType = value.getString();
	                                    } else {
	                                        log.error("Asset has not been moved since too many asset types have been specified {}", asset.getPath());
	                                        meta.setProperty("projectMigration", "Asset has not been moved since too many asset types have been specified");
	                                        isValidAsset = false;
	                                    }
	                                }
	                            }
	                        }
	                    }	else{
	                    	finalstatus = false;
	                    	log.info("Node not moved, CQ tags is null");
	                    }
	                  
	                    if (assetType == null) {
	                    	finalstatus = false;
	                    	log.debug("Node not moved, asset type tags is null");
	                    	isValidAsset = false;
	                    }

	                }	                
	                else{
	                	finalstatus = false;
	                	log.debug("Metadate node is null");
	                }

	                if (isValidAsset) {
	                	
	                } else {
	                	finalstatus = false;
	                	log.debug("Node not moved, asset type tags oddly malformed or missing+");
	                	meta.setProperty("projectMigration", "Node not moved, asset type tags oddly malformed or missing+");
	                }
	             
	            } catch (Exception e) {
	            	log.error("CQ Tags Validation failed.."+e);
	           // throw new WorkflowException(e.getMessage(), e);
	            }
	        }
		 
		return finalstatus;
	}
	@SuppressWarnings("deprecation")
	private List<Node> listAssetsOfImageset(ImageSet imageSet, Session session) throws Exception {
		log.debug("entering listAssetsOfImageset() method");
		List<Node> assetList = new ArrayList<Node>();
		List<Node> assetPri = new ArrayList<Node>();
		List<Node> assetAlt = new ArrayList<Node>();
		Iterator<Asset> assets = imageSet.getImages();
		
		//DAM-567 : Getting reporting metadata attribute "ImageSet Created By" of an Imageset.
		Node repometaImageSet = JcrPropertiesUtil.getReportingNode(session.getNode(imageSet.getPath()), session);
		String imageSetCreatedBy = "";
		if (repometaImageSet != null) {
			imageSetCreatedBy = (repometaImageSet.hasProperty(CommonConstants.IMAGESET_CREATED_BY)) ? repometaImageSet.getProperty(CommonConstants.IMAGESET_CREATED_BY).getString() : "";
        }
		
		int i = 0;
		while (assets.hasNext()) {
			Asset asset = assets.next();
			if(!asset.getPath().contains("content/dam/bbby/approved_dam")){
				//DAM-567 : populating reporting metadata attribute "ImageSet Created By" on all the assets of Imageset.
				Node repometaAsset = JcrPropertiesUtil.getReportingNode(session.getNode(asset.getPath()), session);
				if (repometaAsset != null && !imageSetCreatedBy.contentEquals("")) {
					JcrUtil.setProperty(repometaAsset, CommonConstants.IMAGESET_CREATED_BY, imageSetCreatedBy);
				}
				
				if ("yes".equalsIgnoreCase(asset.getMetadataValue(CommonConstants.BBBY_PRIMARY_IMAGE))) {
					assetPri.add(session.getNode(asset.getPath()));
					log.info("{} is a primary image in imageset {}" ,asset.getName(), imageSet.getName());
				}else{
					assetAlt.add(session.getNode(asset.getPath()));
					log.info("{} is alternate image no. {} in imageset {}" ,asset.getName(), i++, imageSet.getName());
				}
			}
		}
		assetList.addAll(assetPri);
		assetList.addAll(assetAlt);
		return assetList;
	}
		
	
}
