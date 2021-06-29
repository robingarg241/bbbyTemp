package com.bbby.aem.core.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
import com.bbby.aem.core.api.PDMAPICommand;
import com.bbby.aem.core.api.PDMClient;
import com.bbby.aem.core.api.commands.PublishAssetCommand;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.PartitionUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.workflow.impl.BBBYCleanupConfiguration;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;    

/**
 * @author jochen (RIP) ANT
 * 03/11/19
 * 
 * This code has some fixes to make sure that relationships are maintained after the move.  If the moves
 *  are done too quickly they tend to break. So in the code there is a system for slowing down the move
 *  too make sure that the process does not break the renditions.
 *  
 *  This code was modified to support versioning of assets.
 * 
 */

@Component(service = WorkflowProcess.class, property = {
    "process.label=BBBY Project Cleanup Process",
    "Constants.SERVICE_VENDOR=Hero Digital",
    "Constants.SERVICE_DESCRIPTION=Cleanup project assets, tag and move to general population"})
@Designate(ocd = BBBYCleanupConfiguration.class)

//@Service(WorkflowProcess.class)
//@Component(label = "bbby Project Cleanup Step", metatype = true)
//@Properties({@Property(name = Constants.SERVICE_DESCRIPTION, value = "Cleanup project assets, tag and move to general population"),
//    @Property(name = Constants.SERVICE_VENDOR, value = "Hero Digital"),
//    @Property(name = "process.label", value = "bbby Project Cleanup Step")})
public class BBBYProjectCleanup implements WorkflowProcess {
    
/*    @Reference
    private ResourceResolverFactory resolverFactory;*/
//    private BBBYCleanupConfiguration config;

    @Reference
    private Replicator replicator;

    @Reference
    private PDMClient pdmClient;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String ASSET_TYPE_TAG_ROOT = "bbby:asset_type";
    
    private String initiator = null;

    // Static list of tag domains
    final List<String> tagDomains = Arrays.asList(
        "bbby:"
    );

//    @Property(label = "Batch Size", longValue = 10, description = "Batch size for asset processing")
//    public static final String BATCH_SIZE = "batch.size";
    private long batchSize = 10;
//
//    @Property(label = "Timeout", longValue = 500, description = "Time to wait between asset processing to avoid race conditions during asset relationship updates")
//    public static final String TIMEOUT = "timeout";
    private long timeout = 500;
    
    private long maxRetryValidateImageset = 100;
    
    private ResourceResolver resourceResolver = null;
    
    private static String workflowName = "/var/workflow/models/project_cleanup";
    
//    @Activate
//    public void activate(ComponentContext context) {
//        log.error("!@#$E%RT^Y&U*IORFTIJKOL");
//        Dictionary<String, ?> properties = context.getProperties();
//        this.batchSize = Long.parseLong(properties.get(BATCH_SIZE).toString());
//        this.timeout = Long.parseLong(properties.get(TIMEOUT).toString());
//    }
    
    @Activate
    public synchronized void activate(BBBYCleanupConfiguration config) {
        
        this.batchSize=config.batchSize();
        this.timeout=config.timeout();
        this.maxRetryValidateImageset=config.maxRetryValidateImageset();
        log.debug("Max Retries to Validate Imageset : " +  this.maxRetryValidateImageset);
        log.debug("Timeout : " +  this.timeout);
        
    }

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        WorkflowData workflowData = workItem.getWorkflowData();
        log.debug("entering execute with payload {}: ", workflowData.getPayload().toString());
        String type = workflowData.getPayloadType();
        resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
        
        log.debug("entering execute: " + type);

        if (CommonConstants.JCR_PATH.equalsIgnoreCase(type)) {
            String path = workflowData.getPayload().toString();
            Session session = null;
            
            try {
                session = workflowSession.adaptTo(Session.class);
                //Check asset is exist and it is not from approved dam.
                if(session.itemExists(path)&& !path.contains("content/dam/bbby/approved_dam")){
					Node templateNode = (Node) session.getItem(path);

					if (workflowData.getMetaDataMap().containsKey("userId")) {
						initiator = workflowData.getMetaDataMap().get("userId", String.class);
					} else {
						initiator = workItem.getWorkflow().getInitiator();
					}
					boolean isValidAssetName = ServiceUtils.validFileName(session.getNode(path).getName());
					if(isValidAssetName) {
					if (!checkSameWorkflowNotRunning(path, workItem.getWorkflow().getId())) {

						if (templateNode.hasProperty("./projectPath")) {

							Value[] projectPaths = templateNode.getProperty("./projectPath").getValues();

							String projectPath = projectPaths[0].getString();
							log.debug("Project Path: " + projectPath);

							Node projectNode = (Node) session.getItem(projectPath + "/jcr:content");
							log.debug("Project Node Path: " + projectNode.getPath());

							String projectTemplate = projectNode.getProperty("./cq:template").getValue().toString();
							log.debug("Project Template:" + projectTemplate);

							String damFolderPath = projectNode.getProperty("./damFolderPath").getValue().toString();
							log.debug("DAM Folder Path:" + damFolderPath);

							processProject(session, projectNode, damFolderPath);

						} else {

							// Ok, is this a folder or an individual asset?

							if (templateNode.isNodeType("dam:Asset")) {

								log.debug("Individual Asset: " + path);
								processAsset(session, path);

							} else {

								log.debug("DAM Folder Path: " + path);
								processProject(session, null, path);

							}

						}
					} else {
						String msg = "Failed : Already Running BBBY Project CleanUp Workflow";
						setOperationalAttribute(session.getNode(path), session, msg);
					}
					} else {
						String msg = "Failed : Invalid Asset Name";
						setOperationalAttribute(session.getNode(path), session, msg);
					}
				} else {
					log.debug("DAM Asset does not exist at Path {} ", path);
				}

            } catch (Exception e) {
                // If an error occurs that prevents the Workflow from completing/continuing - Throw a WorkflowException
                // and the WF engine will retry the Workflow later (based on the AEM Workflow Engine configuration).
                log.error("Unable to complete processing the Workflow Process step, for path: " + path, e);
            } /*finally {
                if (session != null) {
                    try {
                        session.save();
                    } catch (RepositoryException e) {
                        log.error("An error occurred: ", e);
                    }
                }
            }*/
        }else{
        	log.debug("Unable to execute workflow, type of payload is not " + CommonConstants.JCR_PATH);
        }
    }
    
    //New Improved! adds the ability to process one asset at a time
    //this should unlock the ability to run project cleanup from the asset screen
    
	private void processAsset(Session session, String assetPath) throws Exception {
		log.debug("entering processAsset() method");
		List<Node> assets = new ArrayList<Node>();
		HashMap<String, javax.jcr.Property> projectAttributes = new HashMap<>();
		
		boolean hasReferences = false;

		Node asset = session.getNode(assetPath);
		Resource image1r = resourceResolver.resolve(assetPath);
		if (S7SetHelper.isS7Set(image1r)) {
			log.debug("publishing imageset");
			@SuppressWarnings("deprecation")
			ImageSet imageSet = image1r.adaptTo(ImageSet.class);
			if (imageSet != null) {
				assets = listAssetsOfImageset(imageSet, session);
			}
			//DAM-511 : image and imageset need to have reference 
			if (assets != null && assets.size() > 0) {
				hasReferences = checkReferences(asset, assets, session);
			}
			if (hasReferences) {
				setMembersSequence(asset, session, assets);
				assets.add(asset);
				if (checkAssetsSyncToS7(assets)) {
					moveAssets(session, projectAttributes, assets);
				} else {
					log.info("Unable to start Project CleanUp Workflow, One or more assets are not sync with Scene7. Please Check Operational Attribute of added Assets.");
					String msg = "Failed : One or more assets are not sync with Scene7";
					setOperationalAttribute(asset, session, msg);
				}
			} else {
				log.info("Unable to start Project CleanUp Workflow, One or more assets are not have reference. Please Check Operational Attribute of added Assets.");
				String msg = "Failed : image and imageset need to have reference ";
				setOperationalAttribute(asset, session, msg);
			}
		} else {
			log.debug("publishing other than imageset");
			assets.add(asset);
			moveAssets(session, projectAttributes, assets);
		} 
	}

    
    private void processProject(Session session, Node projectNode, String damFolderPath) throws Exception {
    	log.debug("entering processProject() method");
        // Retrieve attributes from Project (these should be prefaced by "bbby");

        List<Node> assets = getProjectAssets(session, damFolderPath);
        HashMap<String, javax.jcr.Property> projectAttributes = new HashMap<>();

        //if we are actually dealing with a project then do project stuff, otherwise use blank defaults (asset transition folder)
        if (projectNode != null) {

            projectAttributes = getNodeAttributes(projectNode);

            // Retrieve DAM assets contained within the damFolderPath
            assets = getProjectAssets(session, damFolderPath);

            List<String> tagList = getProjectTags(projectAttributes);

            Calendar projectExpireDate = getProjectExpireDate(projectAttributes);

            applyTagsToAssets(session, assets, tagList, projectExpireDate);
        }

        moveAssets(session, projectAttributes, assets);

    }

    private HashMap<String, javax.jcr.Property> getNodeAttributes(Node node) {
    	log.debug("entering getNodeAttributes() method");
        HashMap<String, javax.jcr.Property> attributes = new HashMap<String, javax.jcr.Property>();
        log.debug("------------------attributes----------------------");

        try {
            PropertyIterator it = node.getProperties();
            while (it.hasNext()) {
                javax.jcr.Property p = (javax.jcr.Property) it.next();
                if (p.getName().startsWith("bbby") || p.getName().startsWith("jcr:title") || p.getName().startsWith("project")) {
                    attributes.put(p.getName(), p);
                }
            }
        } catch (RepositoryException e) {
            log.error("failed to get attributes");
            log.error(e.getMessage());
        }

        log.debug("------------------end attributes----------------------");
        return attributes;
    }

    private List<Node> getProjectAssets(Session session, String damFolderPath) {
    	log.debug("entering getProjectAssets() method");
        List<Node> assetNodes = new ArrayList<Node>();
        String q = "SELECT * FROM [dam:Asset] AS s WHERE ISDESCENDANTNODE([" + damFolderPath + "])";
        Query myQuery;

        try {
            myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
            QueryResult result = myQuery.execute();
            NodeIterator ni = result.getNodes();

            while (ni.hasNext()) {
                Node n = (Node) ni.next();
                if (!"cover".equals(n.getName()) && !n.getPath().contains("subasset")) {
                    assetNodes.add(n);
                    log.debug("Asset: " + n.getPath());
                }
            }
        } catch (InvalidQueryException e) {
            log.error("An error occurred: ", e);
        } catch (RepositoryException e) {
            log.error("An error occurred: ", e);
        }

        return assetNodes;
    }

    /*
     * If the project attributes contain an expire date (not required in the project setup) then return it as a Calendar
     * Otherwise return a null;
     */

    private Calendar getProjectExpireDate(HashMap<String, javax.jcr.Property> attributes){
    	log.debug("entering getProjectExpireDate() method");
        Calendar expireDate = null;
        for (String key : attributes.keySet()){
            log.warn(key);
        }
        try{
            javax.jcr.Property expireDateP = attributes.get("project.expireDate");
            expireDate = expireDateP.getDate();

        } catch (Exception e){
            expireDate = null;
            log.error("An error occurred: ", e);
        }

        return expireDate;

    }

    private List<String> getProjectTags(HashMap<String, javax.jcr.Property> attributes) throws Exception {
    	log.debug("entering getProjectTags() method");
        List<String> tags = new ArrayList<String>();

        for (String key : attributes.keySet()) {

            javax.jcr.Property p = attributes.get(key);

            //Look for multiples first
            if (p.isMultiple()) {
                Value[] values = p.getValues();

                for (int i = 0; i < values.length; i++) {
                    String tag = values[i].toString();
                    if (isTagValue(tag)) {
                        tags.add(tag);
                    }
                }
            } else {
                Value value = p.getValue();
                String tag = value.getString();
                if (isTagValue(tag)) {
                    tags.add(tag);
                }
            }
        }

        log.debug("------------------start tags---------------");
        for (String tag : tags) {
            log.debug("Tag: " + tag);
        }

        log.debug("------------------end tags---------------");
        return tags;
    }

    private Boolean isTagValue(String tag) {
    	log.debug("entering isTagValue() method");
        for (String prefix : tagDomains) {
            //we should never move asset types from project to asset
            //if (tag.startsWith(prefix) && !tag.startsWith(ASSET_TYPE_TAG_ROOT)) {
            if (tag.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    private void applyTagsToAssets(Session session, List<Node> assets, List<String> tagList, Calendar projectExpireDate) {
    	log.debug("entering applyTagsToAssets() method");
    	// Get all the existing tags on the asset
        int index = 0;
        for (Node n : assets) {
            List<String> tagArray = new ArrayList<String>();

            // Deep copy the array
            List<String> assetTagList = new ArrayList<String>(tagList);

            try {

                Node metaNode = n.getNode(CommonConstants.METADATA_NODE);

                if (metaNode.hasProperty(CommonConstants.CQ_TAGS)) {
                    javax.jcr.Property tagProperty = metaNode.getProperty(CommonConstants.CQ_TAGS);
                    Value[] tags = tagProperty.getValues();

                    for (Value tagValue : tags) {
                        tagArray.add(tagValue.getString());
                    }
                }

            } catch (PathNotFoundException e) {
                log.warn("An error occurred: Cannot find asset");
                log.error("An error occurred: ", e);
            } catch (RepositoryException e) {
                log.warn("An error occurred: Cannot find cq:tags, this is an expected event for non fit assets");
                log.error("An error occurred: ", e);
            }

            // We might have an empty tagArray, as there might be no tags on the original asset
            for (String tag : tagArray) {
                if (!assetTagList.contains(tag)) {
                    assetTagList.add(tag);
                }
            }

            // Try and set the value on the metadata subnode
            try {

                Node metaNode = n.getNode(CommonConstants.METADATA_NODE);
                metaNode.setProperty("./cq:tags", (String[]) assetTagList.toArray(new String[assetTagList.size()]));

                if(projectExpireDate != null){

                    metaNode.setProperty("./prism:expirationDate", projectExpireDate);

                }

                if (++index % batchSize == 0) {
                    log.debug("Persisting batch {} to {}", index - batchSize, index);
                    //session.save();
                }
            } catch (Exception e) {
                log.error("An error occurred: ", e);
            }

        }
    }

    private ArrayList<String> moveAssets(Session session, HashMap<String, javax.jcr.Property> projectAttributes, List<Node> assets) {
    	log.debug("entering moveAssets() method");
        ArrayList<String> successfulMigration = new ArrayList<String>();
        int index = 0;
        for (Node asset : assets) {

            try {

                boolean isValidAsset = true;
                String assetType = null;

//                String year = "";
//                String month = "";
                
                // DAM:1461 - Block the movement for invalid asset name
                boolean allAssetNameValid = checkAllAssetName(assets);
    			if(allAssetNameValid) {
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
                    }else{
                    	log.debug("Node not moved, CQ tags is null");
                    }
                    
                    if (assetType == null) {
                    	log.debug("Node not moved, asset type tags is null");
                        isValidAsset = false;
                    }

                }else{
                	log.debug("Metadate node is null");
                }

                if (isValidAsset) {
                	log.debug("Its a valid Asset");

                    // Move asset
                    String destination = "/content/dam/bbby/" + assetType.substring(ASSET_TYPE_TAG_ROOT.length() + 1);
                    
                    //add partition data here
                    //we are going to need to check for the existence of the partitions first, and create them if they don't exist
                    //prior to the actual move.
                    
                    ArrayList<String> partitions = PartitionUtil.hashFileName(asset.getName());
                    
                    //create the holding partitions if they don't exist
                    destination = destination + "/" + partitions.get(0);
                    destination = destination + "/" + partitions.get(1);
                    Node destpathnode = JcrUtils.getOrCreateByPath(destination, "sling:Folder", session);
                    log.debug("Destination folder created {}", destination);
                    log.debug("513. session.save() : Before ");
                    session.save();
                    log.debug("515. session.save() : After ");
                    destination = destpathnode.getPath() +  "/" + asset.getName();

                    String projectName = null;
                    if (projectAttributes.containsKey("jcr:title")) {
                        javax.jcr.Property projectNameProp = projectAttributes.get("jcr:title");
                        projectName = projectNameProp.getString();
                    }

                    if (projectName != null){

                        meta.setProperty("projectName", projectName);

                    }

                    if (!destination.equals("")) {
                    	log.debug("destination{} is not null", destination);

                        if (projectName != null){
                            meta.setProperty("projectMigration", "Success");
                        } else {
                            meta.setProperty("assetTransitionMigration", "Success");
                        }

                        String origin = asset.getPath();

                        try {
                            
							if (session.nodeExists(destination) && session.nodeExists(origin)) {
								log.debug("Node is already exist in destination location {}", destination);
								log.debug("Executed for versioning the asset");
								VersionManager versionManager = session.getWorkspace().getVersionManager();

								log.debug("548. session.save() : Before ");
								session.save();
								log.debug("550. session.save() : After ");
								// must persist changes before version can be
								// created
								log.debug("must persist changes before version can be created");
								versionManager.checkin(destination);
								versionManager.checkout(destination);

								// Update asset
								session.removeItem(destination + "/jcr:content");
								log.debug("Node{}/jcr:content is deleted.", origin);
								session.move(origin + "/jcr:content", destination + "/jcr:content");
								log.debug("560. session.save() : Before ");
								// session.save();
								log.debug("562. session.save() : After ");
								log.debug("563. Node is moved from {}/jcr:content to {}/jcr:content", origin,
										destination);
								// Delete asset from project
								session.removeItem(origin);
								session.save();
								log.debug("566. Node{} is deleted.", origin);

							}
							
                            session.move(origin, destination);
                            session.refresh(true);//keep pending changes and merge them with latest copy
                            log.debug("580. session.save() : Before ");
                            if(session.hasPendingChanges()) {
                            	session.save();
                            }
                            log.debug("582. session.save() : After ");
                            log.debug("583. Node is moved from {} to {}", origin, destination);
                            successfulMigration.add(destination);
                           
                            boolean isPublished = publishAsset(session, destination);
                          
                            //  if (isPublished) { 
                            	
                        	int resCode = -1;
                        	
                        	//DAM-320 : populated reporting metadata attribute "Published to S7 By User".
                        	Node repometa = JcrPropertiesUtil.getReportingNode(asset, session);
							if (repometa != null) {
								JcrUtil.setProperty(repometa, CommonConstants.PUBLISHED_TO_S7_BY_USER, initiator);
								JcrUtil.setProperty(repometa, "imageApprovedDate", ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMATTER_FULL));
							}else{
								log.debug("Unable to set reporting attributes as repo node is null");
							}
                        	
                            Node movedAsset = session.getNode(destination);
                            PDMAPICommand publishCommand = new PublishAssetCommand(PDMAPICommand.METHOD_POST, movedAsset);
                            resCode = pdmClient.execute(publishCommand);
                            
                            if(resCode == HttpStatus.SC_OK){
                            	log.debug("Setting operational attributes as response code is 200");
                            	Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
                            	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PDM_CALL_SENT, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
                            	long count = opmeta.hasProperty(CommonConstants.OPMETA_PDM_CALL_COUNT)? opmeta.getProperty(CommonConstants.OPMETA_PDM_CALL_COUNT).getValue().getLong() : 0;
                            	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PDM_CALL_COUNT, count+1);
                            	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS, "SUCCESS");
                            }else{
                            	log.debug("Unable to set operational attributes as response code is not 200");
                            	Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
                				JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_LAST_PDM_CALL_STATUS, "FAILED : Status Code "+resCode);
                            }
                            
                            if (isPublished) {
                                //DAM-511 
                    			setOperationalAttribute(asset, session, "Success");
                            }else{
                            	setOperationalAttribute(asset, session, "PUBLISH_FAILED");
                            }
                            
                            session.refresh(true);//keep pending changes and merge them with latest copy
                			log.debug("611. session.save() : Before ");
                			if(session.hasPendingChanges()) {
                            	session.save();
                            }
                            log.debug("613. session.save() : After ");
                         //   }

                        } catch (Exception e) {
                        	log.error("An error occurred: ", e);
                            meta.setProperty("projectMigration", "Asset not moved, " + e.getClass().getSimpleName() + " in move itself: " + e.getMessage());
                        }
                    } else {
                    	log.debug("Node not moved, destination location is blank or asset type tags oddly malformed");
                    	meta.setProperty("projectMigration", "Node not moved, asset type tags oddly malformed");
                    }
                } else {
                	log.debug("Node not moved, asset type tags oddly malformed or missing+");
                	meta.setProperty("projectMigration", "Node not moved, asset type tags oddly malformed or missing+");
                }
                
                if(session.hasPendingChanges()) {
                	session.save();
                }
                
                if (++index % batchSize == 0) {
                    log.debug("Persisting batch {} to {} with image {}", index - batchSize, index, asset.getName());
                   // session.save();
                }else{
                	log.debug("633. Continue publishing the assets with batch {} to {} with image {}", index - batchSize, index, asset.getName());
                }
    			}
    			else {
    				String msg = "Failed : Invalid Asset in Imageset";
    	    		setOperationalAttribute(asset, session, msg);
    			}
            } catch (Exception e) {
                log.error("An error occurred: ", e);
            }
        }

        return successfulMigration;

    }
    
	private boolean checkAllAssetName(List<Node> assets) throws Exception {
	    boolean allAssetNameValid = true;
        for (Node assetName : assets) {
        boolean isValidAssetName = ServiceUtils.validFileName(assetName.getName());
        if(!isValidAssetName) {
		allAssetNameValid = false;
        }	
        }
		return allAssetNameValid;
	}

    private Boolean publishAsset(Session session, String destination) {
    	log.debug("entering publishAsset() method");
        Boolean successful = true;

        try {
            replicator.replicate(session, ReplicationActionType.ACTIVATE, destination);
            log.debug("Successfully publish asset : " + destination);
        } catch (ReplicationException e) {
            log.error("Failed to publish asset: " + e.getMessage());
            successful = false;
        }
        return successful;
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
	
	//DAM-511 : Set Operational Attributes for success/failure.
	private void setOperationalAttribute(Node asset, Session session, String msg) {
		log.debug("entering setOperationalAttribute() method");
		try {
			Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
			JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PC_Executed_Date,
					ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
			JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PC_WF_State, msg);
			session.save();
		} catch (Exception e) {
			log.error("Unable to set Operational attributes " + e.getMessage());
		}
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
			session.save();
		} catch (Exception e) {
			log.error("Unable to set Members Sequence " + e.getMessage());
		}
	}
	
	// DAM-511 : Check references of imageset in every payload.
	private boolean checkReferences(Node asset, List<Node> nodes, Session session) throws Exception {
		log.debug("entering checkReferences() method");
		boolean hasRefereces = false;
		boolean hasAllReferenceAssets = false;
		boolean hasFinalRefereces = false;
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
		//DAM-1297
		if (hasRefereces) {
			hasAllReferenceAssets = ServiceUtils.checkAllReferenceAssets(nodes, session);
			log.info("checkAllReferenceAssets() method returns: " + hasAllReferenceAssets);
		}
		
		//DAM-1324 : Reference missing banner changes and PC wf changes
		String damScene7FileStatusValue = ServiceUtils.getMetadataValue(resourceResolver.getResource(asset.getPath()), CommonConstants.DAM_SCENE_7_FILE_STATUS, "");
		
		if (hasRefereces && hasAllReferenceAssets && StringUtils.isNotBlank(damScene7FileStatusValue)){
			hasFinalRefereces = true;
		}
		return hasFinalRefereces;
	}
}
