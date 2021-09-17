package com.bbby.aem.core.services.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.settings.SlingSettingsService;
import org.json.JSONArray;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.models.data.MetadataRow;
import com.bbby.aem.core.models.data.PdmAsset;
import com.bbby.aem.core.models.data.Sku;
import com.bbby.aem.core.models.data.UploadItem;
import com.bbby.aem.core.models.data.UploadRequest;
import com.bbby.aem.core.services.AssetService;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.LogUtils;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentFilter;
import com.day.cq.replication.AgentNotFoundException;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;



/**
 * @author vpokotylo
 *
 */
@Component(
	    immediate = true,
	    service = AssetService.class)
	@Designate(ocd = AssetServiceConfig.class)
public class AssetServiceImpl implements AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetServiceImpl.class);

    /**
     * The template.
     */
    private final String template = "/apps/bbby/templates/page-content";

    private final String VENDOR_TAG = "bbby:asset_source/vendor";

    private static final String REL_ASSET_UPCMETADATA = "jcr:content/upcmetadata";

    protected static final String DEFAULT_FOLDER_TYPE = "sling:Folder";

    public static final AgentFilter REV_AGENT_FILTER = new AgentFilter()
    {
      public boolean isIncluded(Agent agent) {
        return agent.getConfiguration().isTriggeredOnDistribute();
      }
    };

    public static final AgentFilter REPLICATE_AGENT_FILTER = new AgentFilter()
    {
      public boolean isIncluded(Agent agent) {
    	 return ("durbo".equals(agent.getConfiguration().getSerializationType())
                  || "binary-less".equals(agent.getConfiguration().getSerializationType()))
              // is not reverse replication agent
              && !agent.getConfiguration().usedForReverseReplication()
              && !agent.getConfiguration().isSpecific();
      }
    };

    /**
     * The resolver factory.
     */
    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    protected Replicator replicator;

    @Reference
    private SlingSettingsService slingSettingsService;

    @Reference
    private MimeTypeService mimeTypeService;

    private Map<String, String>  assetTypes = new HashMap<String, String>();

    private Map<String, String>  shotTypes = new HashMap<String, String>();

    //private ResourceResolver resourceResolver;

    @Activate
    protected void activate(AssetServiceConfig config) throws LoginException {
    	log.info("In activate method");
    	try( ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice")) {
    		log.info("In try block got ResourceResolver");

        	assetTypes = Arrays.stream(config.assetTypes()).collect(Collectors.toMap(x -> x.toLowerCase().substring(0, StringUtils.indexOf(x, ";")), x -> x.substring(StringUtils.indexOf(x, ";") + 1)));
        	if(assetTypes.size() == 0){
        		log.info("assetTypes map is Empty");
        	}

        	TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
        	log.info("TagManager : " + tagManager);
        	Tag shotTypeTag = tagManager.resolve(CommonConstants.BBBY_SHOT_TYPE);
        	log.info("shotTypeTag : " + shotTypeTag);

        	if(shotTypeTag != null) {
        		Iterator<Tag> iter = shotTypeTag.listChildren();
           	 	iter.forEachRemaining( tag -> {
           	 		shotTypes.put(tag.getTitle().toLowerCase(), tag.getTagID());
           	 	});
        	}else{
        		log.info("Shot Type Tag is NULL");
        	}
        	if(shotTypes.size() == 0){
        		log.info("shotTypes map is Empty");
        	}
    	} catch (Exception e) {
            log.error("Error in creating Asset :: {}" + e.getMessage(), e);
            e.printStackTrace();
    	}

    }



    /* (non-Javadoc)
     * @see com.bbby.aem.core.services.AssetService#saveAssets(org.apache.sling.api.SlingHttpServletRequest, org.apache.sling.api.SlingHttpServletResponse, com.bbby.aem.core.models.data.UploadRequest, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.io.InputStream, com.bbby.aem.core.models.data.DropzoneUploadRequest)
     */
    public void saveAssets(SlingHttpServletRequest request, SlingHttpServletResponse response, ResourceResolver resolver,
                           UploadRequest uploadRequest, String groupName, String userName, String currentDate, String batchNode,
                           InputStream assetInputStream) {
        try {


            PageManager pageManager = resolver.adaptTo(PageManager.class);
            Session session = resolver.adaptTo(Session.class);
            JSONArray uploadListJsonarray = new JSONArray(request.getParameter(CommonConstants.UPLOAD_LIST));
            Map<String, RequestParameter[]> reqParams = request.getRequestParameterMap();

            /* check if request has file inside */
            if (reqParams.containsKey("file")) {
                RequestParameter reqParam = reqParams.get("file")[0];
                String filename = reqParam.getFileName();

                log.info("Saving uploaded file ... " + filename);
                String filePageName = ServiceUtils.buildFilePageName(filename);
                String filePagePath = batchNode + CommonConstants.FORWARD_SLASH + filePageName;


                /* if file exists, check if the file page has been created, or does the file already exists */
                if (!session.nodeExists(filePagePath)) {
                    Page assetWrapperPage = pageManager.create(batchNode, filePageName, template, filePageName);
                    Node assetWrapperPageContainer = assetWrapperPage.getContentResource().adaptTo(Node.class);

                    setHistoryRequiredMetadata(assetWrapperPageContainer, uploadRequest, reqParam.getSize());
                    setAssetWrapperPageMetadata(assetWrapperPageContainer, filename);

//                    session.save();

                    Asset asset = saveAssetToDam(resolver, session, groupName, userName, assetInputStream,
                        filePagePath, filename, uploadListJsonarray, uploadRequest, reqParam.getSize(), currentDate);

                    reverseReplicate(session, assetWrapperPageContainer.getPath());

                    if(asset != null) {
                    	reverseReplicate(session, asset.getPath());
                    }

                }
            }


        } catch(AgentNotFoundException ae) {

        	log.error("Reverse Replication agent not found", ae.getLocalizedMessage());
        }
        catch (Exception e) {
            log.error("An exception has occured in saveAssets method with error: " + e.getMessage(), e);
            response.setStatus(SlingHttpServletResponse.SC_NOT_ACCEPTABLE);
        }
    }



    /**
     * @param folderPath
     * @param r
     * @return
     * @throws RepositoryException
     * @throws PersistenceException
     */
    protected boolean createFolderNode(String folderPath, ResourceResolver r) throws RepositoryException, PersistenceException {
        Session s = r.adaptTo(Session.class);
        if (s.nodeExists(folderPath)) {
            return false;
        }

        String name = StringUtils.substringAfterLast(folderPath, "/");
        String parentPath = StringUtils.substringBeforeLast(folderPath, "/");
       // createFolderNode(parentPath, r);

        s.getNode(parentPath).addNode(name, DEFAULT_FOLDER_TYPE);
        r.commit();
        r.refresh();
        return true;
    }


    /* (non-Javadoc)
     * @see com.bbby.aem.core.services.AssetService#reverseReplicate(javax.jcr.Session, java.lang.String)
     */
    @Override
    public void reverseReplicate(Session session, String path) throws ReplicationException {

    	if (slingSettingsService.getRunModes().contains("author")) {
            log.warn("Attempt to run reverse replication from author environment");
            return;
        }

    	ReplicationOptions opts = new ReplicationOptions();
        opts.setFilter(REV_AGENT_FILTER);
        opts.setSynchronous(true);
        opts.setSuppressVersions(true);
        opts.setSuppressStatusUpdate(true);
        log.debug("Reverse replicating {}", path);

        replicator.replicate(session, ReplicationActionType.ACTIVATE, path, opts);
    }

    /* (non-Javadoc)
     * @see com.bbby.aem.core.services.AssetService#reverseReplicate(javax.jcr.Session, java.lang.String)
     */
    @Override
    public void replicate(Session session, String path) throws ReplicationException {

    	ReplicationOptions opts = new ReplicationOptions();
        opts.setFilter(REPLICATE_AGENT_FILTER);
        opts.setSuppressVersions(true);
        opts.setSuppressStatusUpdate(true);

        log.debug("Replicating {}", path);
        replicator.replicate(session, ReplicationActionType.ACTIVATE, path, opts);
    }

    private void setAssetWrapperPageMetadata(Node assetWrapperPageContainer, String filename) {
        try {
            //assetWrapperPageContainer.setProperty("cq:distribute", true);
            assetWrapperPageContainer.setProperty("cq:lastModified", Calendar.getInstance());
            assetWrapperPageContainer.setProperty("moveAsset", true);
            assetWrapperPageContainer.setProperty("completed", false);
            assetWrapperPageContainer.setProperty("fileExt", FilenameUtils.getExtension(filename));
            assetWrapperPageContainer.setProperty("fileName", FilenameUtils.removeExtension(filename).replaceAll("\\s", ""));

            try {
                LogUtils.debugLocation(log, "AssetServiceImpl", "saveAssets-176");

                Node assetWrapper = assetWrapperPageContainer.getParent();
                Node batchWrapper = assetWrapper.getParent();
                Node batchJcrContent = batchWrapper.getNode(CommonConstants.JCR_CONTENT);

                if(batchJcrContent.hasProperty(CommonConstants.BBBY_FAST_TRACK_ASSET)) {
                    assetWrapperPageContainer.setProperty(CommonConstants.BBBY_FAST_TRACK_ASSET, batchJcrContent.getProperty(CommonConstants.BBBY_FAST_TRACK_ASSET).getString());
                }
                Node assetJcrContentData;
                if (assetWrapperPageContainer.hasNode(CommonConstants.DATA)) {
                    assetJcrContentData = assetWrapperPageContainer.getNode(CommonConstants.DATA);
                } else {
                    assetJcrContentData = assetWrapperPageContainer.addNode(CommonConstants.DATA);
                }

                Node batchJcrContentData = batchJcrContent.getNode(CommonConstants.DATA);

                transferJcrcontent(batchJcrContent, assetWrapperPageContainer);
                transferJcrcontentData(batchJcrContentData, assetJcrContentData);
            } catch (Exception e) {
                log.error("An exception has occured in setAssetWrapperPageMetadata method " +
                    "when applying create project related metadata with error: " + e.getMessage(), e);
            }


        } catch (Exception e) {
            log.error("An exception has occured in setAssetWrapperPageMetadata method with error: " + e.getMessage(), e);
        }
    }

    private void transferJcrcontent(Node fromNode, Node toNode) {
        try {
            toNode.setProperty("createProject", true );
            toNode.setProperty("filecount", fromNode.getProperty("filecount").getLong() );
            toNode.setProperty("projectTitle", fromNode.getProperty("projectTitle").getString() );
        } catch (Exception e) {
            log.error("An exception has occured in transferJcrcontent method with error: " + e.getMessage(), e);
        }
    }

    /**
     * @param fromNode
     * @param toNode
     */
    private void transferJcrcontentData(Node fromNode, Node toNode) {
        try {

            if (fromNode.hasProperty(CommonConstants.BRAND_NAME)) {
                toNode.setProperty(CommonConstants.BRAND_NAME, fromNode.getProperty(CommonConstants.BRAND_NAME).getString());
        	}

            if(fromNode.hasProperty(CommonConstants.NOTES)) {
                toNode.setProperty(CommonConstants.NOTES, fromNode.getProperty(CommonConstants.NOTES).getString());
            }

        } catch (Exception e) {
            log.error("An exception has occured in transferJcrcontentData method with error: " + e.getMessage(), e);
        }
    }

    /* this function applies metadata that is required for the history page to function properly */
    private void setHistoryRequiredMetadata(Node assetWrapperPageContainer, UploadRequest uploadRequest, long filesize) {

    	try {
            UploadItem currentItem = uploadRequest.getUploadItem();
            assetWrapperPageContainer.setProperty(CommonConstants.BBBY_WIDTH, currentItem.getWidth());
            assetWrapperPageContainer.setProperty(CommonConstants.BBBY_HEIGHT, currentItem.getHeight());
            assetWrapperPageContainer.setProperty(CommonConstants.BBBY_SIZE, filesize);
            assetWrapperPageContainer.setProperty(CommonConstants.BBBY_COLOR_SPACE, currentItem.getColorSpace());
            assetWrapperPageContainer.setProperty(CommonConstants.BBBY_SHARED_ASSET, currentItem.getSharedAsset());

        } catch (Exception e) {
            log.error("An exception has occured in setHistoryRequiredMetadata method with error: " + e.getMessage(), e);
        }
    }

    private Asset saveAssetToDam(ResourceResolver resourceResolver, Session session, String groupName, String userName, InputStream inputStream,
                                String filePagePath, String fileName, JSONArray uploadListJsonarray, UploadRequest uploadRequest, long fileSize, String currentDate) {


    	Asset asset = null;

    	try {

            AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);

            String assetLocation = filePagePath + CommonConstants.FORWARD_SLASH + CommonConstants.JCR_CONTENT + CommonConstants.FORWARD_SLASH + stripSpecialChars(fileName);

            if (null != inputStream) {

            	String mimeType = mimeTypeService.getMimeType(fileName);

                asset = assetManager.createAsset(assetLocation, inputStream, mimeType, true);


                applyAssetMetadata(resourceResolver, asset, uploadRequest, userName, currentDate);

                log.info("Uploaded asset {} saved. Vendor filename is {} ", assetLocation, fileName);
            }
        } catch (Exception e) {
            log.error("An exception has occured in saveAssetToDam method with error: " + e.getMessage(), e);
        }
    	return asset;
    }


    /**
     * @param resourceResolver
     * @param asset
     * @param uploadRequest
     * @param userName
     * @param currentDate
     */
    private void applyAssetMetadata(ResourceResolver resourceResolver, Asset asset, UploadRequest uploadRequest, String userName, String currentDate) {

    	try {

    		Node assetNode = (Node) asset.adaptTo(Node.class);

            Node metadataNode = assetNode.getNode("jcr:content/metadata");
            metadataNode.setProperty("bbby:batchID", uploadRequest.getBatchId());
            metadataNode.setProperty("bbby:vendorPortalID", userName+"--"+currentDate);
            metadataNode.setProperty("bbby:requestedBy", uploadRequest.getRequestedBy());

            String fileName = uploadRequest.getUploadItem().getFileName();
            MetadataRow metadataRow = uploadRequest.getMetadata(fileName);

			if (metadataRow == null) {
				// DAM: 1401 - Vendor assets coming into AEM without asset metadata
				log.info("metdatRow is null so removing space/junk from filename");
				String newFileName = StringUtils.replace(StringUtils.trim(fileName), "\u00A0", "");
				metadataRow = uploadRequest.getMetadata(newFileName);
				if (metadataRow == null) {
					log.error("Metadata is missing for " + fileName);
					return;
				}
			}

            if(metadataRow.getUpc() != null){
            	log.info("Primary UPC is " + metadataRow.getUpc() + " for " + fileName + ".");
            }else{
            	log.info("Primary UPC is null for " + fileName + ".");
            }

            metadataNode.setProperty(CommonConstants.BBBY_CONTENT_OPS_REVIEW, "no");
            metadataNode.setProperty(CommonConstants.BBBY_UPC, metadataRow.getUpc());
            metadataNode.setProperty(CommonConstants.BBBY_UPLOADED_ASSET_NAME, fileName);

            if(StringUtils.isNotEmpty(metadataRow.getAdditionalUPC())) {
            	metadataNode.setProperty(CommonConstants.BBBY_ADDITIONAL_UPC, metadataRow.getAdditionalUPC());
            }

            metadataNode.setProperty(CommonConstants.BBBY_ASSET_UPDATE, metadataRow.isAssetUpdate() ? "yes" : "no");


            if(StringUtils.isNotEmpty(metadataRow.getReasonForUpdate())) {
            	metadataNode.setProperty("bbby:reasonForAssetUpdate", metadataRow.getReasonForUpdate().toLowerCase());
            }

            metadataNode.setProperty("bbby:srtProvided", metadataRow.isSrtProvided() ? "yes" : "no");
            metadataNode.setProperty("bbby:bbbyToCreateSRT", metadataRow.isBbbyToCreateSRT() ? "yes" : "no");

            metadataNode.setProperty(CommonConstants.BBBY_SEQUENCE, metadataRow.getSequence());
            metadataNode.setProperty("bbby:vendorEmail", uploadRequest.getEmail());
            log.debug("File upload Vendor Email " + uploadRequest.getEmail()!=null?uploadRequest.getEmail():"");
            metadataNode.setProperty("bbby:jobID", ServiceUtils.getVendorPortalProjectTitle(uploadRequest.getBatchId(), currentDate));

            // Assign tags
            Set<String> tags = new HashSet<String>();
            if(metadataRow.getAssetType() != null && assetTypes.containsKey(metadataRow.getAssetType().toLowerCase())) {
            	log.info("Asset Type is " + metadataRow.getAssetType() + " for " + fileName + ".");
            	tags.add(assetTypes.get(metadataRow.getAssetType().toLowerCase()));
            }else if(metadataRow.getAssetType() == null){
            	log.error("Asset Type is missing for " + fileName + ". Asset Type entered is null");
            }else{
            	log.error("Asset Type is missing for " + fileName + ". Asset Type entered is " + metadataRow.getAssetType());
            }

            //DAM-947 : Add restriction in vendor portal to not provide shot type for Collaterals, Videos
            if(metadataRow.getAssetType() != null && "Product Images".equalsIgnoreCase(metadataRow.getAssetType())){
	            if(metadataRow.getShotType() != null && metadataRow.getShotType() != "" && shotTypes.containsKey(metadataRow.getShotType().toLowerCase())) {
	            	log.info("Shot Type is " + metadataRow.getShotType() + " for " + fileName + ".");
	            	tags.add(shotTypes.get(metadataRow.getShotType().toLowerCase()));
	            }else if(StringUtils.isNotEmpty(metadataRow.getShotType())){
	            	tags.add("bbby:shot_type/"+metadataRow.getShotType().toLowerCase().replaceAll(" ", "_"));
	            	log.error("Shot Type is missing for " + fileName + ". Shot Type entered is " + metadataRow.getShotType());
	            }else{
	            	log.error("Shot Type is missing for " + fileName + ". Shot Type entered is null/Blank");
	            }
            }else{
            	log.error("Shot Type is not populated for " + fileName + ". Asset Type entered is not Product Images");
            }
            tags.add(VENDOR_TAG);

            metadataNode.setProperty(CommonConstants.CQ_TAGS, tags.toArray(new String[0]));

//            assetNode.getSession().save();

        } catch (Exception e) {
            log.error("An exception has occured in applyAssetMetadata method with error: " + e.getMessage(), e);
        }
    }


    private String stripSpecialChars(String fileName) {
    	String finalFileName;
    	try{
	        String ext = FilenameUtils.getExtension(fileName);
	      //DAM-528 : make the entire file name lower case.
	        ext = ext.toLowerCase();
	        String name = FilenameUtils.getBaseName(fileName);
	        //DAM-528 : make the entire file name lower case.
	        name = name.toLowerCase();
	        finalFileName = name.replaceAll("[^A-Za-z0-9-_]", "") + "." + ext;
    	}catch (Exception e){
    		finalFileName = fileName;
    		log.error("Critical error has occured in stripSpecialChars method with error: " + e.getMessage(), e);
    	}
    	return finalFileName;
    }


    public String getTempAssetWrapperName (String originalFileName) {
        String tempAssetWrapperName = null;
        try {
            String pageNameExt = FilenameUtils.getExtension(originalFileName);
            String pageNameNoExt = FilenameUtils.removeExtension(originalFileName);
            String pageNameNoSpaces = pageNameNoExt.replaceAll("\\s", "");
            String pageNameWithExt = pageNameNoSpaces + pageNameExt;
            tempAssetWrapperName = "TEMP_" + JcrUtil.createValidName(pageNameWithExt);
        } catch (Exception e) {
            log.error("An exception has occured in getTempAssetWrapperName method with error : " + e.getMessage(), e);
        }
        return tempAssetWrapperName;
    }

	@Override
	public void updateAssetMetadata(PdmAsset pdmAsset,  ResourceResolver resourceResolver) throws PersistenceException {
		Session session = resourceResolver.adaptTo(Session.class);

		log.debug("Update from PDM: " + pdmAsset);

		if(pdmAsset == null || StringUtils.isEmpty(pdmAsset.getUuid())) {
			return;
		}

        try {

            Asset originalAsset = DamUtil.getAssetFromID(resourceResolver, pdmAsset.getUuid());

            if(originalAsset == null) {
            	log.warn("No asset found for UUID = " + pdmAsset.getUuid());
            	return;
            }
            String assetPath = originalAsset.getPath();

            // Delete the  upcmetadata node if exists

           // Node originalAssetJcrContentNode = session.getNode(assetPath + "/" + JcrConstants.JCR_CONTENT);
          //  Resource originalAssetJcrContentResource = resourceResolver.getResource(originalAssetJcrContentNode.getPath());
          //  ValueMap vmOriginalAssetJcrContent = originalAssetJcrContentResource.adaptTo(ModifiableValueMap.class);

            Resource upcMetadataResource = resourceResolver.getResource(assetPath + "/" + REL_ASSET_UPCMETADATA);
            if(upcMetadataResource != null) {
            	resourceResolver.delete(upcMetadataResource);
            }


            Node upcMetadataNode = JcrUtil.createPath(assetPath + "/" + REL_ASSET_UPCMETADATA, JcrConstants.NT_UNSTRUCTURED, resourceResolver.adaptTo(Session.class));

            //resourceResolver.refresh();

            List<Sku> skus = pdmAsset.getPdmMetadata().getSkus();
            boolean isBBBYWebOfferedFlag = false;
            boolean isBABYWebOfferedFlag = false;
            boolean isCAWebOfferedFlag = false;
            boolean containsAssociatedCollectionId = false;
            boolean containsAssociatedWebProductId = false;

            for(int i=0; i < skus.size(); i++) {
            	int containsCollections = 0;
            	int containsWebProductId = 0;
            	Sku sku = skus.get(i);
            	Node upcNode = JcrUtil.createPath(assetPath + "/" + REL_ASSET_UPCMETADATA + "/" + "upc-" + i, JcrConstants.NT_UNSTRUCTURED, resourceResolver.adaptTo(Session.class));
            	Resource upcNodeResource = resourceResolver.getResource(upcNode.getPath());
            	setMetadata(upcNodeResource, sku);

            	//This logic has been applied to check whether any of these flags is true and associatedCollectionId/webProductId is there for avoiding "bbby Sorting hat" workflow launcher in case none of them is true.
            	String bbbyWebOfferedFlag = sku.getBbbyWebOfferedFlag();
    			String babyWebOfferedFlag = sku.getBabyWebOfferedFlag();
    			String caWebOfferedFlag = sku.getCaWebOfferedFlag();
    			if(StringUtils.isNotBlank(sku.getAssociatedCollectionID())) {
    				containsCollections = convertToMulti(sku.getAssociatedCollectionID()).length;
    			}
    			if(StringUtils.isNotBlank(sku.getAssociatedWebProductID())) {
    				containsWebProductId = convertToMulti(sku.getAssociatedWebProductID()).length;
    			}
    			if (bbbyWebOfferedFlag != null && bbbyWebOfferedFlag.equalsIgnoreCase("Yes")) {
                	isBBBYWebOfferedFlag = true;
                }
                if (babyWebOfferedFlag != null && babyWebOfferedFlag.equalsIgnoreCase("Yes")) {
                	isBABYWebOfferedFlag = true;
                }
                if (caWebOfferedFlag != null && caWebOfferedFlag.equalsIgnoreCase("Yes")) {
                	isCAWebOfferedFlag = true;
                }
                if (containsCollections != 0) {
                	containsAssociatedCollectionId = true;
                }
                if (containsWebProductId != 0) {
                	containsAssociatedWebProductId = true;
                }

            }
            boolean isWebOfferedFlag = isBBBYWebOfferedFlag || isBABYWebOfferedFlag || isCAWebOfferedFlag;
            boolean hasAssociatedId = containsAssociatedCollectionId || containsAssociatedWebProductId;


            /*if(pdmAsset.getBbbyAssetMetadata()!=null && pdmAsset.getBbbyAssetMetadata().getAssetsPDMData()!=null){
	            List<SkuBbbyAssetMetadata> skusBbby = pdmAsset.getBbbyAssetMetadata().getAssetsPDMData();
	            SkuBbbyAssetMetadata skuBbbyAssetMetadata = skusBbby.get(0);
	            Node bbbyAssetMetadataNode = session.getNode(assetPath + "/" + JcrConstants.JCR_CONTENT + "/metadata");
	            JcrUtil.setProperty(bbbyAssetMetadataNode, CommonConstants.BBBY_DEPARTMENT_NAME, skuBbbyAssetMetadata.getDepartmentName());
	            JcrUtil.setProperty(bbbyAssetMetadataNode, CommonConstants.BBBY_DEPARTMENT_NUMBER, skuBbbyAssetMetadata.getDepartmentNumber());
            }*/

            Node bbbyAssetMetadataNode = session.getNode(assetPath + "/" + JcrConstants.JCR_CONTENT + "/metadata");
            Resource bbbyAssetMetadataResource = resourceResolver.getResource(bbbyAssetMetadataNode.getPath());
            ValueMap vmBbbyAssetMetadata = bbbyAssetMetadataResource.adaptTo(ModifiableValueMap.class);

            Node opmeta = JcrPropertiesUtil.getOperationalNode(session.getNode(assetPath), session);
            Resource opmetaResource = resourceResolver.getResource(opmeta.getPath());
            ValueMap vmOpmeta = opmetaResource.adaptTo(ModifiableValueMap.class);
            vmOpmeta.put(CommonConstants.OPMETA_PDM_WEB_OFFERED_FLAGS, isWebOfferedFlag);
            vmOpmeta.put(CommonConstants.OPMETA_HAS_PDM_ASSOCIATED_ID, hasAssociatedId);
            vmOpmeta.put(CommonConstants.OPMETA_PDM_CALL_RECEIVED, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));

			if (pdmAsset.getBbbyAssetMetadata() != null) {
				if (pdmAsset.getBbbyAssetMetadata().getParentDepartmentName() != null)
					vmBbbyAssetMetadata.put(CommonConstants.BBBY_DEPARTMENT_NAME, pdmAsset.getBbbyAssetMetadata().getParentDepartmentName());
				if (pdmAsset.getBbbyAssetMetadata().getParentDepartmentNumber() != null)
					vmBbbyAssetMetadata.put(CommonConstants.BBBY_DEPARTMENT_NUMBER,	pdmAsset.getBbbyAssetMetadata().getParentDepartmentNumber());
			}

     //       vmOriginalAssetJcrContent.put(JcrConstants.JCR_LASTMODIFIED, new Date());

            //DAM-904 : modified the UPCMetadate node to trigger BBBY sorting hat only once.
            JcrUtil.setProperty(upcMetadataNode, CommonConstants.LAST_PDM_METADATA_UPDATE, new Date().toString());

            if (resourceResolver.hasChanges()) {
            	resourceResolver.commit();
            }
            //JcrUtil.setProperty(originalAssetJcrContentNode, JcrConstants.JCR_LAST_MODIFIED_BY, lastModifiedBy);

        } catch (RepositoryException e) {
            log.error("Could not create a new version of the asset", e);
            throw new PersistenceException(e.getMessage());
        }
	}

	/**
	 * Sets UPC metadata on the asset node
	 *
	 * @param upcNode
	 * @param sku
	 * @return
	 * @throws RepositoryException
	 */
	private ValueMap setMetadata(Resource upcNode, Sku sku) throws RepositoryException {
		ValueMap vm = upcNode.adaptTo(ModifiableValueMap.class);
		if(sku.getPrimaryUPC()!=null){
			vm.put(CommonConstants.PRIMARY_UPC,sku.getPrimaryUPC());
		}
		if(sku.getSku()!=null){
			vm.put(CommonConstants.SKU,sku.getSku());
		}
		if(sku.getBrand()!=null){
			vm.put(CommonConstants.PDM_BRAND,sku.getBrand());
		}
		if(sku.getColorCode()!=null){
			vm.put(CommonConstants.COLOR_CODE,sku.getColorCode());
		}
		if(sku.getColorGroupCode()!=null){
			vm.put(CommonConstants.COLOR_GROUP_CODE,sku.getColorGroupCode());
		}
		if(sku.getProduct()!=null){
			vm.put(CommonConstants.PRODUCT,sku.getProduct());
		}
		if(sku.getAssociatedWebProductID()!=null && StringUtils.isNotBlank(sku.getAssociatedWebProductID())) {
			vm.put(CommonConstants.ASSOCIATED_WEB_PRODUCT_ID,convertToMulti(sku.getAssociatedWebProductID()));
		}
		if(sku.getAssociatedCollectionID()!=null && StringUtils.isNotBlank(sku.getAssociatedCollectionID())) {
			vm.put(CommonConstants.ASSOCIATED_COLLECTION_ID,convertToMulti(sku.getAssociatedCollectionID()));
		}
		if(sku.getPdmBatchID()!=null){
			vm.put(CommonConstants.PDM_BATCH_ID,sku.getPdmBatchID());
		}
		if(sku.getPrimaryVendorNumber()!=null){
			vm.put(CommonConstants.PRIMARY_VENDOR_NUMBER,sku.getPrimaryVendorNumber());
		}
		if(sku.getPrimaryVendorName()!=null){
		 vm.put(CommonConstants.PRIMARY_VENDOR_NAME,sku.getPrimaryVendorName());
		}
		if(sku.getVendorDirectToCustomerItem()!=null){
			vm.put(CommonConstants.VENDOR_DIRECT_TO_CUSTOMER_ITEM,sku.getVendorDirectToCustomerItem());
		}
		if(sku.getPriorityFlag()!=null){
			vm.put(CommonConstants.PRIORITY_FLAG,sku.getPriorityFlag());
		}
		if(sku.getBbbyWebDisabled()!=null){
			vm.put(CommonConstants.BBBY_WEB_DISABLED,sku.getBbbyWebDisabled());
		}
		if(sku.getBabyWebDisabled()!=null){
			vm.put(CommonConstants.BABY_WEB_DISABLED,sku.getBabyWebDisabled());
		}
		if(sku.getCaWebDisabled()!=null){
			vm.put(CommonConstants.CA_WEB_DISABLED,sku.getCaWebDisabled());
		}
		if(sku.getBbbyWebOfferedFlag()!=null){
			vm.put(CommonConstants.BBBY_WEB_OFEERED_FLAG,sku.getBbbyWebOfferedFlag());
		}
		if(sku.getBabyWebOfferedFlag()!=null){
			vm.put(CommonConstants.BABY_WEB_OFEERED_FLAG,sku.getBabyWebOfferedFlag());
		}
		if(sku.getCaWebOfferedFlag()!=null){
			vm.put(CommonConstants.CA_WEB_OFEERED_FLAG,sku.getCaWebOfferedFlag());
		}
		if(sku.getWebProductRollUpType()!=null){
			vm.put(CommonConstants.WEB_PRODUCT_ROLL_UP_TYPE,sku.getWebProductRollUpType());
		}
		if(sku.getMasterProductDescription()!=null){
			vm.put(CommonConstants.MASTER_PRODUCT_DESCRIPTION,sku.getMasterProductDescription());
		}
		if(sku.getFastTrackFlag()!=null){
			vm.put(CommonConstants.FAST_TRACK_FLAG,sku.getFastTrackFlag());
		}
		if(sku.getPullbackToMerchant()!=null){
			vm.put(CommonConstants.PULLBACK_TO_MERCHANT,sku.getPullbackToMerchant());
		}
		return vm;
	}

	/**
	 * Converts comma-separated list to an array to store as multi-value property in JCR
	 *
	 * @param value
	 * @return
	 */
	private String[] convertToMulti(final String value) {
        String[] result;

        result = Arrays.stream(value.split(";"))
    			.filter(Objects::nonNull)
    			.toArray(String[]::new);

        return result;
    }
}
