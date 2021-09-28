package com.bbby.aem.core.jobs.impl;

import com.adobe.granite.asset.api.AssetManager;
import com.bbby.aem.core.services.AssetService;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.replication.ReplicationStatus;
import com.day.crx.JcrConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.*;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Job consumer that downloads images to DAM and links them to product asset node.
 * More info https://sling.apache.org/documentation/bundles/apache-sling-eventing-and-job-handling.html
 *
 * @author vpokotylo
 *
 */
@Component(immediate = true, service = JobConsumer.class,
    property = {"job.topics=" + CommonConstants.ASSET_MOVE_TOPIC})
public class AssetMoveJobConsumerImpl implements JobConsumer {

    private static final Logger log = LoggerFactory.getLogger(AssetMoveJobConsumerImpl.class);

    protected static final String DEFAULT_FOLDER_TYPE = "sling:Folder";

    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Reference
    private ResourceResolverFactory resourceFactory;

    @Reference
    private AssetService assetService;

    @Override
	public JobResult process(final Job job) {

		log.debug("Starting a job " + job.getTopic());
		try (ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resourceFactory, "writeservice")) {

			String assetPath = (String) job.getProperty("assetPath");

			if (StringUtils.isEmpty(assetPath)) {
				log.warn("Empty asset path");
				return JobConsumer.JobResult.CANCEL;
			}

			Resource assetWrapper = moveAsset(assetPath, resourceResolver);


			if(assetWrapper != null) {
				Session session = resourceResolver.adaptTo(Session.class);

				log.debug("Replicating {}", assetWrapper.getPath());
				assetService.replicate(session, assetWrapper.getPath());

				Resource batch = assetWrapper.getParent();

				log.debug("Batch node {}", batch);

				if(batch != null) {
					log.debug("Replicating Batch node {}", assetWrapper.getPath());
					assetService.replicate(session, batch.getPath());
				}
			}

			//  commit changes...
			if(resourceResolver.hasChanges())
				resourceResolver.commit();

		} catch (LoginException e) {
			log.error("Error obtaining resource resolver job consumer");
			return JobConsumer.JobResult.CANCEL;
		} catch (Exception e) {
			log.error("Exception processing asset move request", e.getLocalizedMessage());
			return JobConsumer.JobResult.FAILED;

		}

		return JobConsumer.JobResult.OK;
	}

    /**
     * @param assetPath
     * @param resourceResolver
     */
    private Resource moveAsset(String assetPath, ResourceResolver resourceResolver ) {

    	Resource assetWrapper = null;

    	try {

            String projectTitle = "";
            String holdingBasePath = "/content/dam/bbby/asset_transitions_folder/vendor/vendor_assets_holding";

            Session session = resourceResolver.adaptTo(Session.class);
            AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);


            Resource asset = resourceResolver.getResource(assetPath);
            Node assetJcrContentNode = session.getNode(assetPath + "/" + JcrConstants.JCR_CONTENT);

            // need to reset these. reverse replication agents sets them when the node is created
            JcrUtil.setProperty(assetJcrContentNode, ReplicationStatus.NODE_PROPERTY_LAST_REPLICATED, null);
            JcrUtil.setProperty(assetJcrContentNode, ReplicationStatus.NODE_PROPERTY_LAST_REPLICATED_BY, null);
            JcrUtil.setProperty(assetJcrContentNode, ReplicationStatus.NODE_PROPERTY_LAST_REPLICATION_ACTION, null);


            if(asset == null || !asset.getResourceType().equals("dam:Asset")) {
            	log.warn("Move Assets invoked incorrectly on {}", assetPath);
            	return null;
            }

            // ms18rs09_terno_altajpg
            // - jcr:content
            // --- MS18RS09_TERNO_ALT_A.jpg

            Resource assetWrapperJcrContent = asset.getParent();

            if( assetWrapperJcrContent != null) {
            	assetWrapper = assetWrapperJcrContent.getParent();
            }

            if(assetWrapper == null) {
            	log.warn("Asset wrapper node missing for {}", assetPath);
            	return null;
            }


            Node assetWrapperContentNode = assetWrapperJcrContent.adaptTo(Node.class);

			if (assetWrapperContentNode.hasProperty("moveAsset")) {

				Node assetNode = asset.adaptTo(Node.class);

				// this is a date-based folder under asset holding
				String targetHoldingPath = getOrCreateTargetPath(session, assetNode, holdingBasePath);

                Property projectTitleProp = assetWrapperContentNode.getProperty("projectTitle");
                if (projectTitleProp != null) {
                	projectTitle = projectTitleProp.getString();
                    createFolderNode(targetHoldingPath + "/" + projectTitle, resourceResolver);
				}

                String fileName = buildFileName(projectTitle, asset.getPath());
                String targetPath = targetHoldingPath + "/" + projectTitle + "/" + fileName;

				String sourcePath = asset.getPath();

				assetManager.copyAsset(sourcePath, targetPath);

//				session.save();

				assetManager.removeAsset(sourcePath);

				assetWrapperContentNode.setProperty("completed", true);
                String fastTrackBatchDateStr = null;
				if(assetWrapperContentNode.hasProperty(CommonConstants.JCR_CREATED)) {
                    Date fastTrackBatchDate = assetWrapperContentNode.getProperty(CommonConstants.JCR_CREATED).getDate().getTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyy-MM-dd-hh-mm");
                    fastTrackBatchDateStr = dateFormat.format(fastTrackBatchDate);
                }

            	//DAM-320 : populated reporting metadata attribute "Vendors Assets Holding Entry Date".
                Node repometa = JcrUtil.createPath(targetPath + "/" + CommonConstants.REL_ASSET_REPORTING_METADATA, JcrConstants.NT_UNSTRUCTURED, session);
                if (repometa != null) {
    				JcrUtil.setProperty(repometa, CommonConstants.VAH_ENTRY_DATE, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
    			}

                Node operationalmeta = JcrUtil.createPath(targetPath + "/" + CommonConstants.OPERATIONAL_METADATA_NODE, JcrConstants.NT_UNSTRUCTURED, session);
                if (operationalmeta != null) {
                    String isFasttrackAsset = assetJcrContentNode.hasProperty( CommonConstants.BBBY_FAST_TRACK_ASSET) ? assetJcrContentNode.getProperty( CommonConstants.BBBY_FAST_TRACK_ASSET).getString() : "No";
                    JcrUtil.setProperty(operationalmeta, CommonConstants.BBBY_FAST_TRACK_ASSET, isFasttrackAsset);
                    String isSharedAsset = assetJcrContentNode.hasProperty( CommonConstants.BBBY_SHARED_ASSET) ? assetJcrContentNode.getProperty( CommonConstants.BBBY_SHARED_ASSET).getString() : "no";
                    JcrUtil.setProperty(operationalmeta, CommonConstants.BBBY_SHARED_ASSET, isSharedAsset);
                    if(fastTrackBatchDateStr != null) {
                        JcrUtil.setProperty(operationalmeta, CommonConstants.FAST_TRACK_BATCH_DATE, fastTrackBatchDateStr);
                    }
                }

                log.debug("Uploaded asset {} moved to -> {}. Vendor filename is {}, batch ID is {} ", sourcePath, targetPath,
                    fileName, projectTitle);

				session.refresh(true);
//				session.save();


			} else {
				log.debug("Skipping asset move. moveAsset not set on ", assetWrapperContentNode.getPath());
			}
        } catch (Exception e) {
            log.error("An exception has occured in moveAsset method with error: " + e.getMessage(), e);
        }

    	return assetWrapper;
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
//        r.commit();
        r.refresh();
        return true;
    }

    /**
     * @param session
     * @param node
     * @return
     */
    private String getOrCreateTargetPath(Session session, Node node, String rootTarget) {

    	String targetPath = null;

		try {
			Calendar createdDate = node.getProperty(JcrConstants.JCR_CREATED).getDate();

			String dayFormatted = dateFormatter.format(createdDate.getTime());

			String datePath = rootTarget + "/" + dayFormatted;
			if (!session.nodeExists(datePath)) {
				log.debug("Creating " + datePath);
				JcrUtil.createPath(datePath, "sling:Folder", session);
//				session.save();
			}


			targetPath = datePath;

		} catch (Exception e) {

			String message = e.getMessage();
			log.error(message);

			e.printStackTrace();

		}

        return targetPath;
    }



    private String buildFileName(String projectTitle, String path) {
        String fileName = "";

        try {
            fileName = StringUtils.substringAfterLast(path, "/");

        } catch (Exception e) {
            log.error("An error has occured in extractFileName: " + e.getMessage(), e);
        }

        return projectTitle != null ? projectTitle + "_" + fileName : fileName;
    }
}
