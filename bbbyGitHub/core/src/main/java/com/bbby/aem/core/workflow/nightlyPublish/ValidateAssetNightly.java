package com.bbby.aem.core.workflow.nightlyPublish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
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
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;

/**
 * @author Karan
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=Nightly Publish Validate Asset",
		"Constants.SERVICE_VENDOR=BBBY",
		"Constants.SERVICE_DESCRIPTION=Validate Imageset/Image is exist or not and not from Approve DAM Folder" })
public class ValidateAssetNightly implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private ResourceResolver resourceResolver;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		log.debug("entering execute with payload {}: ", workflowData.getPayload().toString());
		String type = workflowData.getPayloadType();
		resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
		boolean isImagesetMove = ServiceUtils.moveImagesetNightly(workflowData.getMetaDataMap().get("moveImages",String.class));
		log.debug("isImagesetMove:"+isImagesetMove);
		boolean isValidAssetName = true;
		boolean allAssetNameValid = true;
		if (CommonConstants.JCR_PATH.equalsIgnoreCase(type)) {
			String path = workflowData.getPayload().toString();
			Session session = null;

			try {
				session = workflowSession.adaptTo(Session.class);

				// Check asset is exist and it is not from approved dam.
				if (session.itemExists(path) && !path.contains("content/dam/bbby/approved_dam")) {
					workflowData.getMetaDataMap().put("isExist", "true");
					log.info("Asset is exist and not from Approve DAM");

					isValidAssetName = ServiceUtils.validFileName(session.getNode(path).getName());
					if(isValidAssetName) {
						//workflowData.getMetaDataMap().put("isAlreadyRunning", "false");
						Resource image1r = resourceResolver.resolve(path);
						if (S7SetHelper.isS7Set(image1r)) {
							log.debug("publishing imageset");
							workflowData.getMetaDataMap().put("isImageset", "true");
							List<Node> assets = new ArrayList<Node>();
							Node node = session.getNode(path);
							boolean hasReferences = false;
							@SuppressWarnings("deprecation")
							ImageSet imageSet = image1r.adaptTo(ImageSet.class);
							if (imageSet != null) {
								assets = listAssetsOfImageset(imageSet, session, isImagesetMove);
							}
							
							// DAM:1461 - Block the movement for invalid asset name
			                allAssetNameValid = checkAllAssetName(assets);
			                
			                if(!allAssetNameValid){
			                	String msg = "Failed : Invalid Asset in Imageset";
			                	for(Node asset : assets){
			                		Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
			                		JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PC_Executed_Date,
			            					ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
			            			JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PC_WF_State, msg);
			                	}
			                }
							
			                // DAM-511 : image and imageset need to have
							// reference
							if (assets != null && assets.size() > 0 && allAssetNameValid) {
								hasReferences = checkReferences(node, assets, session);
								workflowData.getMetaDataMap().put("assetList", assets);
							}
							if (hasReferences) {
								if(!isImagesetMove){
									setMembersSequence(node, session, assets);
								}
								workflowData.getMetaDataMap().put("hasReferences", "true");
								assets.add(node);
								if (checkAssetsSyncToS7(assets)) {
									workflowData.getMetaDataMap().put("isSyncToS7", "true");
									List<String> pathList = new ArrayList<String>();
									for (Node n : assets) {
										pathList.add(n.getPath());
									}
									workflowData.getMetaDataMap().put("pathList", pathList);
								} else {
									log.info(
											"Unable to start the workflow, One or more assets are not sync with Scene7. Please Check Operational Attribute of added Assets.");
									String msg = "Failed : One or more assets are not sync with Scene7";
									workflowData.getMetaDataMap().put("isSyncToS7", "false");
									workflowData.getMetaDataMap().put("opmsg", msg);
								}
							} else {
								log.info(
										"Unable to start the workflow, One or more assets are not have reference. Please Check Operational Attribute of added Assets.");
								String msg = "Failed : image and imageset need to have reference ";
								workflowData.getMetaDataMap().put("hasReferences", "false");
								workflowData.getMetaDataMap().put("opmsg", msg);
							}

						} else {
							workflowData.getMetaDataMap().put("isImageset", "false");
							workflowData.getMetaDataMap().put("isSyncToS7", "true");
							workflowData.getMetaDataMap().put("hasReferences", "true");
						}
					
					}else{
						log.info("Unable to start the workflow, This payload is not have valid name. Please Check Operational Attribute of added Assets.");
						String msg = "Failed : Invalid Asset Name";
                        workflowData.getMetaDataMap().put("opmsg", msg);
					}
				} else {
					workflowData.getMetaDataMap().put("isExist", "false");
					log.info("Asset is not exist or from Approve DAM");
				}

			} catch (Exception e) {
				log.error("Unable to complete processing the Workflow Process step, for path: " + path, e);
				throw new WorkflowException(e.getMessage(), e);
			}
			
			/*String isAlreadyRunning = workflowData.getMetaDataMap().get("isAlreadyRunning") != null
					? workflowData.getMetaDataMap().get("isAlreadyRunning").toString() : "";*/
			String isSyncToS7 = workflowData.getMetaDataMap().get("isSyncToS7") != null
					? workflowData.getMetaDataMap().get("isSyncToS7").toString() : "";
			String hasReferences = workflowData.getMetaDataMap().get("hasReferences") != null
					? workflowData.getMetaDataMap().get("hasReferences").toString() : "";
			
			if (isValidAssetName && allAssetNameValid) {
				if ("false".equalsIgnoreCase(isSyncToS7) || "false".equalsIgnoreCase(hasReferences)) {
					workflowData.getMetaDataMap().put("checkReferenceStep", "false");
				} else if ("true".equalsIgnoreCase(isSyncToS7) && "true".equalsIgnoreCase(hasReferences)) {
					workflowData.getMetaDataMap().put("checkReferenceStep", "true");
				}
			} else {
				workflowData.getMetaDataMap().put("checkReferenceStep", "false");
			}

			workflowSession.updateWorkflowData(workItem.getWorkflow(), workflowData);
		}

	}

	@SuppressWarnings("deprecation")
	private List<Node> listAssetsOfImageset(ImageSet imageSet, Session session, boolean isImagesetMove) throws Exception {
		log.debug("entering listAssetsOfImageset() method");
		List<Node> assetList = new ArrayList<Node>();
		List<Node> assetPri = new ArrayList<Node>();
		List<Node> assetAlt = new ArrayList<Node>();
		Iterator<Asset> assets = imageSet.getImages();

		// DAM-567 : Getting reporting metadata attribute "ImageSet Created By"
		// of an Imageset.
		Node repometaImageSet = JcrPropertiesUtil.getReportingNode(session.getNode(imageSet.getPath()), session);
		String imageSetCreatedBy = "";
		if (repometaImageSet != null) {
			imageSetCreatedBy = (repometaImageSet.hasProperty(CommonConstants.IMAGESET_CREATED_BY))
					? repometaImageSet.getProperty(CommonConstants.IMAGESET_CREATED_BY).getString() : "";
		}

		int i = 0;
		while (assets.hasNext()) {
			Asset asset = assets.next();
			if (isImagesetMove) {
				if (asset.getPath().contains("content/dam/bbby/approved_dam")) {
					assetList.add(session.getNode(asset.getPath()));
				}
			} else {
				if (!asset.getPath().contains("content/dam/bbby/approved_dam")) {
					// DAM-567 : populating reporting metadata attribute
					// "ImageSet Created By" on all the assets of Imageset.
					Node repometaAsset = JcrPropertiesUtil.getReportingNode(session.getNode(asset.getPath()), session);
					if (repometaAsset != null && !imageSetCreatedBy.contentEquals("")) {
						JcrUtil.setProperty(repometaAsset, CommonConstants.IMAGESET_CREATED_BY, imageSetCreatedBy);
					}

					if ("yes".equalsIgnoreCase(asset.getMetadataValue(CommonConstants.BBBY_PRIMARY_IMAGE))) {
						assetPri.add(session.getNode(asset.getPath()));
						log.info("{} is a primary image in imageset {}", asset.getName(), imageSet.getName());
					} else {
						assetAlt.add(session.getNode(asset.getPath()));
						log.info("{} is alternate image no. {} in imageset {}", asset.getName(), i++,
								imageSet.getName());
					}
				}
			}
		}
		assetList.addAll(assetPri);
		assetList.addAll(assetAlt);
		return assetList;
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
		boolean hasAllReferenceAssets = false;
		boolean hasFinalRefereces = false;
		ArrayList<String> memberList = ServiceUtils.getMembersOfImageset(session, asset.getPath());
		if (memberList != null && memberList.size() > 0) {
			if (memberList.size() == nodes.size()) {
				hasRefereces = true;
				log.info("Reference count matches with node count");
			} else {
				log.info("Reference count is mismatch with node count");
			}
		} else {
			log.info("Do not have reference : 0 references");
		}
		// DAM-1297
		if (hasRefereces) {
			hasAllReferenceAssets = ServiceUtils.checkAllReferenceAssets(nodes, session);
			log.info("hasAllReferenceAssets() method returns: " + hasAllReferenceAssets);
		}
		// DAM-1324 : Reference missing banner changes and PC wf changes
		String damScene7FileStatusValue = ServiceUtils.getMetadataValue(resourceResolver.getResource(asset.getPath()), CommonConstants.DAM_SCENE_7_FILE_STATUS, "");
		if (hasRefereces && hasAllReferenceAssets && StringUtils.isNotBlank(damScene7FileStatusValue)) {
			hasFinalRefereces = true;
		}
		return hasFinalRefereces;
	}
	
	private void setMembersSequence(Node asset, Session session, List<Node> assets) {
		log.debug("entering setMembersSequence() method");
		try {
			String assetPathArray[] = new String[assets.size()];
			int i = 0;
			for (Node n : assets) {
				assetPathArray[i] = n.getPath();
				i++;
			}
			Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
			JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_MEMBERS_SEQUENCE, assetPathArray);
		} catch (Exception e) {
			log.error("Unable to set Members Sequence " + e.getMessage());
		}
	}
	
	private boolean checkAllAssetName(List<Node> assets) throws Exception {
		boolean allAssetNameValid = true;
		for (Node assetName : assets) {
			boolean isValidAssetName = ServiceUtils.validFileName(assetName.getName());
			if (!isValidAssetName) {
				allAssetNameValid = false;
			}
		}
		return allAssetNameValid;
	}
}