package com.bbby.aem.core.services.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.models.data.UploadHistoryDetailsItem;
import com.bbby.aem.core.services.UploadHistoryDetailsService;
import com.bbby.aem.core.util.CommonConstants;

// TODO: Auto-generated Javadoc

/**
 * The Class UploadHistoryDetailsServiceImpl.
 * @author karthik.koduru, hero-digital
 */
@Component(
    immediate = true,
    service = UploadHistoryDetailsService.class)
public class UploadHistoryDetailsServiceImpl implements UploadHistoryDetailsService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UploadHistoryDetailsServiceImpl.class);

    @Override
    public JSONObject getBatchDetailsJSON(SlingHttpServletRequest request, String userPath, String batchID, ResourceResolver resolver)
        throws AccessDeniedException, ValueFormatException, PathNotFoundException, ItemNotFoundException,
        RepositoryException, ParseException, JSONException {
        JSONObject obj = new JSONObject();
        LOG.info("Resource Resolver::" + resolver);
        getBatchDetails(resolver, obj, userPath, batchID);

        return obj;
    }

    @SuppressWarnings("unchecked")
    private void getBatchDetails(ResourceResolver resolver, JSONObject obj, String userPath, String batchID)
        throws PathNotFoundException, RepositoryException, JSONException, ValueFormatException, ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonConstants.DATE_FORMATTER);

        final Resource userPathResource = resolver.resolve(userPath);

        LOG.error("############# userBatchResource : " + userPathResource.getPath() + " ####################");
        LOG.error("############# batchID : " + batchID + " ####################");

        Resource batchResource = getBatchResource(userPathResource, batchID);

        LOG.error("############# batchResource : " + batchResource.getPath() + " ####################");

        int count = 0;
        if (null != batchResource.getChild(CommonConstants.JCR_CONTENT)) {
            Node jcrContentNode = batchResource.getChild(CommonConstants.JCR_CONTENT).adaptTo(Node.class);
            List<UploadHistoryDetailsItem> fileitemsList = new ArrayList<UploadHistoryDetailsItem>();
            obj.put("batchID", batchID);
            if (jcrContentNode.hasNode(CommonConstants.DATA)) {
                Node dataNode = jcrContentNode.getNode(CommonConstants.DATA);
                obj.put("adobeUUID", batchResource.getName());
                if (propertyExists(jcrContentNode, CommonConstants.REQUESTED_BY)) {
                    obj.put(CommonConstants.REQUESTED_BY, jcrContentNode.getProperty(CommonConstants.REQUESTED_BY).getString());
                }
            }
            
            if (jcrContentNode.hasProperty("filecount")) {
            	obj.put("noOfFiles", jcrContentNode.getProperty("filecount").getValue().getLong());
			} else {
				Iterator<Resource> nodeItr = batchResource.listChildren();
				while (nodeItr.hasNext()) {
					Node nextNode = nodeItr.next().adaptTo(Node.class);
					if (!nextNode.getName().equals(CommonConstants.JCR_CONTENT)) {
						count = count + 1;
					}
				}
				obj.put("noOfFiles", count);
			}
			
			if (jcrContentNode.hasProperty(CommonConstants.TOTAL_FILES_COUNT)) {
				obj.put("totalNoOfFiles", jcrContentNode.getProperty(CommonConstants.TOTAL_FILES_COUNT).getValue().getLong());
			} else{
				obj.put("totalNoOfFiles", obj.get("noOfFiles"));
			}
			
			if (jcrContentNode.hasProperty(CommonConstants.INVALID_FILES)) {
				obj.put("rejectedFiles", jcrContentNode.getProperty(CommonConstants.INVALID_FILES).getValue().getString());
			} 
			
			if (jcrContentNode.hasProperty(CommonConstants.FAILED_FILES)) {
				obj.put("failedToUploadFiles", jcrContentNode.getProperty(CommonConstants.FAILED_FILES).getValue().getString());
			}
			
			if (jcrContentNode.hasProperty(CommonConstants.INVALID_FILES_COUNT)) {
				obj.put("noOfRejectedFiles", jcrContentNode.getProperty(CommonConstants.INVALID_FILES_COUNT).getValue().getLong());
			} 
			
			if (jcrContentNode.hasProperty(CommonConstants.FAIL_FILES_COUNT)) {
				obj.put("noOfFailedFiles", jcrContentNode.getProperty(CommonConstants.FAIL_FILES_COUNT).getValue().getLong());

			}
            
            //code to handle each file batch
            if (batchResource.hasChildren()) {
                Iterator<Resource> childResource = batchResource.listChildren();
                while (childResource.hasNext()) {
                    Resource childResourcePage = childResource.next();

                    if (!childResourcePage.getName().equals(CommonConstants.JCR_CONTENT)) {
                        Node childPageNode = childResourcePage.adaptTo(Node.class);
                        LOG.info("childPageNode   " + childPageNode.getPath());
                        Iterator<Resource> grandchildResource = resolver.listChildren(childResourcePage);
                        while (grandchildResource.hasNext()) {
                            Resource fileResource = grandchildResource.next();
                            Node childNode = fileResource.adaptTo(Node.class);
                            LOG.info("childNode  " + childNode);

                            UploadHistoryDetailsItem item1 = new UploadHistoryDetailsItem();
                            if (childNode.hasProperty(CommonConstants.BBBY_WIDTH) && StringUtils.isNotEmpty(childNode.getProperty(CommonConstants.BBBY_WIDTH).getString())) {
                                item1.setWidth(childNode.getProperty(CommonConstants.BBBY_WIDTH).getString());
                            }
                            if (childNode.hasProperty(CommonConstants.BBBY_HEIGHT) && StringUtils.isNotEmpty(childNode.getProperty(CommonConstants.BBBY_HEIGHT).getString())) {
                                item1.setHeight(childNode.getProperty(CommonConstants.BBBY_HEIGHT).getString());
                            }
                            if (childNode.hasProperty(CommonConstants.BBBY_SIZE) && StringUtils.isNotEmpty(childNode.getProperty(CommonConstants.BBBY_SIZE).getString())) {
                                item1.setSize(childNode.getProperty(CommonConstants.BBBY_SIZE).getString());
                                LOG.info("bbby size property details" + childNode.getProperty(CommonConstants.BBBY_SIZE).getString());
                            }
                            if (childNode.hasProperty(CommonConstants.BBBY_COLOR_SPACE) && StringUtils.isNotEmpty(childNode.getProperty(CommonConstants.BBBY_COLOR_SPACE).getString())) {
                                item1.setColorSpace(childNode.getProperty(CommonConstants.BBBY_COLOR_SPACE).getString());
                            }
                            if (childNode.hasProperty("fileExt") && StringUtils.isNotEmpty(childNode.getProperty("fileExt").getString())) {
                                item1.setFileExtension(childNode.getProperty("fileExt").getString());
                                LOG.info("fileExt property details" + childNode.getProperty("fileExt").getString());
                            }
                            if (childNode.hasProperty("fileName") && StringUtils.isNotEmpty(childNode.getProperty("fileName").getString())) {
                                item1.setFileName(childNode.getProperty("fileName").getString());
                                LOG.info("fileName property details" + childNode.getProperty("fileName").getString());
                            }
                            fileitemsList.add(item1);
                            count = count + 1;
                            //obj.put("noOfFiles", count);
                            LOG.info("item size::" + item1);
                            LOG.info("item printing::" + fileitemsList.toArray());
                        }
                    }
                }
            }
            if (fileitemsList.size() > 0) {
                obj.put("files", createFilesJsonArrayFromList(fileitemsList));
            }
        }

    }

    private Resource getBatchResource(Resource userPathResource, String batchSubmitted) {
        Resource batchResource = null;
        String[] batchSubmittedArray = batchSubmitted.split("--");
        String batchID = batchSubmittedArray[1];
        try {
            Iterator<Resource> batchesIterator = userPathResource.getChildren().iterator();
            while(batchesIterator.hasNext()) {
                Resource batch = batchesIterator.next();
                Resource batchJcrContentResource = batch.getChild(CommonConstants.JCR_CONTENT);
                if(batchJcrContentResource!=null) {
                    Node batchJcrContentNode = batchJcrContentResource.adaptTo(Node.class);
                    if (batchJcrContentNode.hasProperty("projectTitle") && batchJcrContentNode.getProperty("projectTitle").getString().equals(batchID)) {
                        return batch;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("An exception has occured in getBatchResource function with exception : " + e.getMessage(), e);
        }
        return batchResource;
    }

    /**
     *
     * @param uploadHistoryDetailsItemsList
     * @return
     * @throws JSONException
     * @throws ParseException
     */
    private JSONArray createFilesJsonArrayFromList(List<UploadHistoryDetailsItem> uploadHistoryDetailsItemsList) throws JSONException, ParseException {
        JSONArray jsonArray = new JSONArray();
        for (UploadHistoryDetailsItem fileItem : uploadHistoryDetailsItemsList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fileName", fileItem.getFileName());
            jsonObject.put("fileExtension", fileItem.getFileExtension());
            jsonObject.put("width", fileItem.getWidth());
            jsonObject.put("height", fileItem.getHeight());
            jsonObject.put("size", fileItem.getSize());
            jsonObject.put("colorSpace", fileItem.getColorSpace());
            jsonArray.put(jsonObject);
        }
        LOG.info("printing json::" + jsonArray);
        LOG.info("printing json tostring::" + jsonArray.toString());
        return jsonArray;
    }

    /**
     * Property Exists.
     *
     * @param node         the node
     * @param propertyName the property name
     * @return true, if successful
     * @throws RepositoryException the repository exception
     */
    private boolean propertyExists(Node node, String propertyName) throws RepositoryException {

        return node.hasProperty(propertyName) ? true : false;

    }

    /**
     * Convert to display date.
     *
     * @param date the date
     * @return the string
     * @throws ParseException the parse exception
     */
    private String convertToDisplayDate(Date date) throws ParseException {
        SimpleDateFormat sd2 = new SimpleDateFormat(CommonConstants.DISPLAY_DATE_FORMATTER);
        return sd2.format(date);
    }

}
