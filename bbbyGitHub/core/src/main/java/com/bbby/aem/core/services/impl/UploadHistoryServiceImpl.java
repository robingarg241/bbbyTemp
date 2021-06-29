package com.bbby.aem.core.services.impl;

import com.bbby.aem.core.models.data.UploadHistoryItem;
import com.bbby.aem.core.services.UploadHistoryService;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.UploadHistoryDateComparator;
import com.day.cq.wcm.api.Page;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The Class UploadHistoryImpl.
 * 
 * @author karthik.koduru, hero-digital
 */
@Component(
	    immediate = true,
	    service = UploadHistoryService.class)
public class UploadHistoryServiceImpl implements UploadHistoryService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UploadHistoryServiceImpl.class);

    /**
     * The Constant BATCHES.
     */
    private static final String BATCHES = "batches";


    @Override
    public JSONObject getBatchesJSON(SlingHttpServletRequest request, String pagePath, ResourceResolver resolver)
        throws AccessDeniedException, ValueFormatException, PathNotFoundException, ItemNotFoundException,
        RepositoryException, ParseException, JSONException {
        JSONObject obj = new JSONObject();
        List<UploadHistoryItem> uploadHistoryItemsList = new ArrayList<UploadHistoryItem>();
        String userName = ServiceUtils.getUserName(request);
        LOG.info("Resource Resolver::" + resolver);
        if (null != resolver.getResource(pagePath)) {
            getBatches(resolver, obj, uploadHistoryItemsList, pagePath, userName);
        }
        return obj;
    }

    /**
     * Gets the batches.
     *
     * @param resolver               the resolver
     * @param obj                    the obj
     * @param uploadHistoryItemsList the upload history items list
     * @param pagePath               the page path
     * @return the batches
     * @throws PathNotFoundException the path not found exception
     * @throws RepositoryException   the repository exception
     * @throws JSONException         the JSON exception
     * @throws ValueFormatException  the value format exception
     * @throws ParseException        the parse exception
     */
    @SuppressWarnings("unchecked")
    private void getBatches(ResourceResolver resolver, JSONObject obj, List<UploadHistoryItem> uploadHistoryItemsList, String pagePath, String userName)
        throws PathNotFoundException, RepositoryException, JSONException, ValueFormatException, ParseException {
        SimpleDateFormat xmlDateFormat = new SimpleDateFormat(CommonConstants.DATE_FORMATTER);

        try {
            final Resource rootPageResource = resolver.resolve(pagePath);

            final Page rootPage = rootPageResource.adaptTo(Page.class);

            final Iterator<Page> itr = rootPage.listChildren();

            while (itr.hasNext()) {
                try {
                    final Page batchPage = itr.next();
                    int count = 0;
                    final Resource batchResource = batchPage.adaptTo(Resource.class);
                    UploadHistoryItem item = new UploadHistoryItem();
                    String batchName = getProjectTitle(batchResource);

                    if(null==batchName)
                        continue;

                    item.setBatchID(userName+"--"+batchName);
                    item.setAdobeUUID(batchPage.getTitle());
                    Date batchDate = getBatchDate(batchResource);
                    item.setUploadDate(batchDate);
                    item.setUploadDisplayDate(convertToDisplayDate(item.getUploadDate()));
                    if (null != batchResource.getChild(CommonConstants.JCR_CONTENT)) {
                        Node jcrContentNode = batchResource.getChild(CommonConstants.JCR_CONTENT).adaptTo(Node.class);
                        if (jcrContentNode.hasNode(CommonConstants.DATA)) {
                            if (propertyExists(jcrContentNode, CommonConstants.REQUESTED_BY)) {
                                item.setRequestedBy(jcrContentNode.getProperty(CommonConstants.REQUESTED_BY).getString());
                            }
                        }
                        
						if (jcrContentNode.hasProperty("filecount")) {
							item.setNoOfFiles((int)jcrContentNode.getProperty("filecount").getValue().getLong());
						} else {
							Iterator<Resource> nodeItr = batchResource.listChildren();
							while (nodeItr.hasNext()) {
								Node nextNode = nodeItr.next().adaptTo(Node.class);
								if (!nextNode.getName().equals(CommonConstants.JCR_CONTENT)) {
									count = count + 1;
								}
							}
							item.setNoOfFiles(count);
						}
						
						if (jcrContentNode.hasProperty(CommonConstants.TOTAL_FILES_COUNT)) {
							item.setTotalNoOfFiles((int)jcrContentNode.getProperty(CommonConstants.TOTAL_FILES_COUNT).getValue().getLong());
						} else{
							item.setTotalNoOfFiles(item.getNoOfFiles());
						}
						
						if (jcrContentNode.hasProperty(CommonConstants.INVALID_FILES_COUNT)) {
							item.setNoOfInvalidFiles((int)jcrContentNode.getProperty(CommonConstants.INVALID_FILES_COUNT).getValue().getLong());
						} else{
							item.setNoOfInvalidFiles(0);
						}
						
						if (jcrContentNode.hasProperty(CommonConstants.FAIL_FILES_COUNT)) {
							item.setNoOfFailedToUploadFiles((int)jcrContentNode.getProperty(CommonConstants.FAIL_FILES_COUNT).getValue().getLong());
						} else{
							item.setNoOfFailedToUploadFiles(0);
						}
                    }
                    uploadHistoryItemsList.add(item);
                } catch (Exception e) {
                    LOG.info(":::::::::: failed to upload because : " + e.getMessage(), e);

                }
            }

            LOG.info("batchItemsList Size::" + uploadHistoryItemsList.size());
            if (uploadHistoryItemsList.size() > 0) {
                obj.put(BATCHES, createUploadHistoryJsonArrayFromList(uploadHistoryItemsList));
            }
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
        }
    }

    private Date getBatchDate (Resource batchPageResource) {
        Date batchDate = null;
        try {
            Resource batchPageJcrContentResource = batchPageResource.getChild(CommonConstants.JCR_CONTENT);
            batchDate = batchPageJcrContentResource.adaptTo(Node.class).getProperty("cq:lastModified").getDate().getTime();
        } catch (Exception e) {
            LOG.error("An exception has occured in getBatchDate function with error: " + e.getMessage(), e);
        }
        return batchDate;
    }

    private String getProjectTitle (Resource batchPageResource) {
        String projectTitle = null;
        try {
            Resource batchPageJcrContentResource = batchPageResource.getChild(CommonConstants.JCR_CONTENT);
            projectTitle = batchPageJcrContentResource.adaptTo(Node.class).getProperty("projectTitle").getString();
        } catch (Exception e) {
            LOG.error(batchPageResource.getPath() + "\n\nAn exception has occured in getBatchDate function with error: " + e.getMessage(), e);
        }
        return projectTitle;
    }

    /**
     * Creates the batch json array from list.
     *
     * @param uploadHistoryItemsList the upload history items list
     * @return the JSON array
     * @throws JSONException  the JSON exception
     * @throws ParseException the parse exception
     */
    private JSONArray createUploadHistoryJsonArrayFromList(List<UploadHistoryItem> uploadHistoryItemsList) throws JSONException,
        ParseException {
        Collections.sort(uploadHistoryItemsList, new UploadHistoryDateComparator());
        JSONArray jsonArray = new JSONArray();
        for (UploadHistoryItem uploadHistoryItem : uploadHistoryItemsList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("adobeUUID", uploadHistoryItem.getAdobeUUID());
            jsonObject.put("batchID", uploadHistoryItem.getBatchID());
            jsonObject.put("requestedBy", uploadHistoryItem.getRequestedBy());
            jsonObject.put("uploadDisplayDate", uploadHistoryItem.getUploadDisplayDate());
            jsonObject.put("totalNoOfFiles", uploadHistoryItem.getTotalNoOfFiles());
            jsonObject.put("noOfFiles", uploadHistoryItem.getNoOfFiles());
            jsonObject.put("noOfInvalidFiles", uploadHistoryItem.getNoOfInvalidFiles());
            jsonObject.put("noOfFailedToUploadFiles", uploadHistoryItem.getNoOfFailedToUploadFiles());
            jsonArray.add(jsonObject);
        }
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

