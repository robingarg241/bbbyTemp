package com.bbby.aem.core.servlets;

import java.io.IOException;
import java.lang.reflect.Type;
import java.rmi.ServerException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.models.data.UploadItem;
import com.bbby.aem.core.models.data.UploadRequest;
import com.bbby.aem.core.services.AssetService;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.LogUtils;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * FileUploadServlet.
 *
 * @author
 */
@Component(
		service = Servlet.class,
	    immediate = true,
	    name = "File Upload Servlet",
	    property = {
	        "sling.servlet.paths=/bin/bedbath/vendor-portal/upload",
	        "sling.servlet.extensions=json",
	        "sling.servlet.methods=GET,POST",
	        "sling.servlet.resourceTypes=cq:Page"
	    })
public class FileUploadServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 2598426539166789515L;

    private final Logger log = LoggerFactory.getLogger(FileUploadServlet.class);

    private static final DateFormat formatter = new SimpleDateFormat(CommonConstants.DATE_FORMAT);

    private static final Type UPLOADLIST_TYPE = new TypeToken<List<UploadItem>>() {}.getType();

    private final String TEMPLATE_BATCH = "/apps/bbby/templates/page-upload-batch";
    
    private final String TEMPLATE_CONTENT = "/apps/bbby/templates/page-content";
    
    private final String MAIL_SUBJECT = "Bed Bath and Beyond Media Submission Confirmation";

    private static final Gson gson = new Gson();
    
    @Reference
    private AssetService assetService;
    
    @Reference
    private JobManager jobManager;
    
    /**
     * The resolver factory.
     */
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    /**
     * Do get.
     *
     * @param request  the request
     * @param response the response
     * @throws ServerException the server exception
     * @throws IOException     Signals that an I/O exception has occurred.
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServerException,
        IOException {
        
        doPost(request, response);
    }


    /**
     * Processing for the upload form submit
     *
     * @param request  the request
     * @param response the response
     * @throws ServerException the server exception
     * @throws IOException     Signals that an I/O exception has occurred.
     */
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) {
  	
        try( ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice")) {
            
        	Session session = resourceResolver.adaptTo(Session.class);
        	String userName = ServiceUtils.getUserName(request);
            UploadRequest uploadRequest = createUploadRequest(request);
            
            if(uploadRequest.hasUploadRejectedItem()) {
            	log.debug("Unable to save rejected file " + uploadRequest.getUploadRejectedItem().getFileName()+" uploaded by " + userName);
            	
            	//DAM-309 : Send submission confirmation email to vendors from vendor portal
                if(request.getParameter("isUploadCompleted") != null){
                	log.info("File batch upload completed " + request.getParameter("isUploadCompleted"));
                	//DAM-1357 : DAM Vendor Portal - Email Sent Success but there is a mismatch (12/02 JCobb@LBGSales.com)
                	String batchNodePath1 = createStructure(uploadRequest, request, session, resourceResolver);
                	saveAcceptedRejectedFiles(request, session, batchNodePath1);
                	assetService.reverseReplicate(session, batchNodePath1);
                	
                	sendMailToVendor(request, session, batchNodePath1);
                }
            }

            if(!uploadRequest.hasUploadItem()) {
            	response.getWriter().write("SUCCESS");
            	return;
            }
            
            
            log.debug("File upload Username " + userName);
            
            String groupName = "admin";
            
            String currentDate = formatter.format(new Date());
            
            String batchNodePath = createStructure(uploadRequest, request, session, resourceResolver);
            
            assetService.reverseReplicate(session, batchNodePath);

            assetService.saveAssets(request, response, resourceResolver, uploadRequest, groupName, userName,
                currentDate, batchNodePath, ServiceUtils.getFileInputStream(request));
            
//            ServiceUtils.saveSession(session);
			if (request.getParameter("isUploadCompleted") != null) {
				//DAM-1357 : DAM Vendor Portal - Email Sent Success but there is a mismatch (12/02 JCobb@LBGSales.com)
				saveAcceptedRejectedFiles(request, session, batchNodePath);
				assetService.reverseReplicate(session, batchNodePath);
			}
            response.getWriter().write("SUCCESS");
            
            //DAM-309 : Send submission confirmation email to vendors from vendor portal
            if(request.getParameter("isUploadCompleted") != null){
            	log.info("File batch upload completed " + request.getParameter("isUploadCompleted"));
            	sendMailToVendor(request, session, batchNodePath);
            }
            resourceResolver.close();
        } catch (Exception e) {
            log.error("Error in creating Asset ::" + e.getMessage(), e);
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
        }
    }


    private String createStructure(UploadRequest bean, SlingHttpServletRequest request, Session session, ResourceResolver resourceResolver) {
        
    	String wrapperPath = null;
        
        try {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            
            String userName = ServiceUtils.getUserName(request);
            String groupName = ServiceUtils.getUserGroup(resolverFactory, userName);
            String groupNamePage = JcrUtil.createValidName(groupName);

            int fileCount = 0;
            if(request.getParameter("fileCount") != null) {
            	fileCount = Integer.parseInt(request.getParameter("fileCount"));
            }
            
            /* create a wrapper under /content/vendor/ */
            createNode(CommonConstants.VENDOR_ROOT_PATH, groupNamePage, pageManager, session);
            
            /* create user name wrapper under /content/vendor/admin */
            String userNodePath = getOrCreateUserNode(CommonConstants.VENDOR_ROOT_PATH + groupNamePage + CommonConstants.FORWARD_SLASH, userName, pageManager, session);
            
            /* now create batch wrapper */
            wrapperPath = getOrCreateBatchNode(userNodePath, bean.getBatchUuid(), bean.getBatchId(), 
                bean, fileCount, pageManager, session, resourceResolver, request);

        } catch (Exception e) {
            log.error("An exception has occured in createStructure function with error: " + e.getMessage(), e);
        }
        return wrapperPath;
    }


    /**
     * Creates or retrieves the root node for the upload batch
     * 
     * @param userGroupFolder
     * @param userNameFolder
     * @param assetFolderName
     * @param batchId
     * @param uploadRequest
     * @param uploadListJsonarray
     * @param pageManager
     * @param session
     * @param resourceResolver
     * @return
     * @throws RepositoryException
     */
    private String getOrCreateBatchNode(String userNodePath, String batchUuid,
                                    String batchId,
                                    UploadRequest uploadRequest, int fileCount,
                                    PageManager pageManager, Session session,
                                    ResourceResolver resourceResolver, SlingHttpServletRequest request) throws RepositoryException{

        //String userFolderPath = CommonConstants.VENDOR_ROOT_PATH + userGroupFolder + "/" + userNameFolder;

        
        String batchPath = userNodePath + "/" + batchUuid;
        String batchContentResourcePath = batchPath
            + CommonConstants.FORWARD_SLASH + CommonConstants.JCR_CONTENT;

        try {
        	
            Page batchPage = null;
            
            if(session.nodeExists(batchPath) ) {
            	
            	return batchPath;
            	
            } else {
            	
            	Resource template = resourceResolver.getResource(TEMPLATE_BATCH);
            	
            	log.debug("Creating Page: {}/{} with template {}", userNodePath, batchUuid, template);
            	batchPage = pageManager.create(userNodePath, batchUuid , TEMPLATE_BATCH, batchUuid);
            } 

            Node batchContentResource = batchPage.getContentResource().adaptTo(Node.class);

            if (batchContentResource == null || !batchContentResource.getPath().equals(batchContentResourcePath)) {

                LogUtils.debugLog(log, "JCR content is null, so we have to create manually", "JCR content");

                batchContentResource = batchPage.adaptTo(Node.class).addNode(CommonConstants.JCR_CONTENT);
//                ServiceUtils.saveSession(session);
            }
            
            if (null != uploadRequest) {
            	
                Node assetFolderContentNode = null;
                
                if (batchContentResource.hasNode(CommonConstants.DATA)) {
                    assetFolderContentNode = batchContentResource.getNode(CommonConstants.DATA);
                } else {
                    log.debug("JCR Data content is null, so we have to create manually", "JCR Data content");

                    assetFolderContentNode = batchContentResource.addNode(CommonConstants.DATA);
//                    ServiceUtils.saveSession(session);
                }

                setNodeProperty(assetFolderContentNode, CommonConstants.BRAND_NAME, uploadRequest.getBatchId());
                //setNodeProperty(batchContentResource,"cq:distribute", true );
                setNodeProperty(batchContentResource,"cq:lastModified", Calendar.getInstance() );
                setNodeProperty(batchContentResource,"createProject", true );
                setNodeProperty(batchContentResource,"filecount", fileCount );
                setNodeProperty(batchContentResource,"projectTitle", getProjectTitleName());
                setNodeProperty(batchContentResource,"vendorEmail", uploadRequest.getEmail());
                setNodeProperty(batchContentResource,"requestedBy", uploadRequest.getRequestedBy());
                setNodeProperty(batchContentResource,"totalFilesCount", uploadRequest.getTotalFilesCount());
                setNodeProperty(batchContentResource,"invalidFiles", uploadRequest.getInvalidFiles());
                setNodeProperty(batchContentResource,"acceptedFiles", uploadRequest.getAcceptedFiles());
                if(uploadRequest.getTotalFilesCount() > 0 && fileCount > 0){
                	setNodeProperty(batchContentResource,"invalidFilesCount", uploadRequest.getTotalFilesCount() - fileCount);
                }
            }
//            ServiceUtils.saveSession(session);
//            ServiceUtils.commitChanges(resourceResolver);

            if(batchPage != null) {
            	log.info("Vendor Upload Batch {} created. File Count expected is {} ", batchPage.getPath(),  fileCount );
            }

        } catch (Exception e) {
            log.error("An exception has occured in method createUploadPage with error: " + e.getMessage(), e);
        }
        return batchPath;
    }

    private String getProjectTitleName() {
        String projectTitle = "";

        try {
            
            projectTitle = formatter.format(new Date());

        } catch (Exception e) {
            log.error("An exception has occured in method getProjectTitleName with error: " + e.getMessage(), e);
        }

        return projectTitle;
    }


    /**
     * @param parentFolder
     * @param nodeName
     * @param pageManager
     * @param session
     */
    private void createNode(String parentFolder, String nodeName, PageManager pageManager, Session session) {
        try {
            String validNodeName = JcrUtil.createValidName(nodeName);
            if (!session.nodeExists(parentFolder + validNodeName)) {
                Page page = pageManager.create(parentFolder, validNodeName, TEMPLATE_CONTENT, validNodeName);
                Node pageContainer = page.getContentResource().adaptTo(Node.class);
                pageContainer.setProperty("cq:distribute", true);
                pageContainer.setProperty("cq:lastModified", Calendar.getInstance());
            }
//            session.save();
        } catch (Exception e) {
            log.error("An exception has occured in createNode method with error: " + e.getMessage(), e);
        }
    }

    /**
     * @param parentFolder
     * @param nodeName
     * @param pageManager
     * @param session
     */
    private String getOrCreateUserNode(String parentFolder, String userName, PageManager pageManager, Session session) {

    	String validNodeName = JcrUtil.createValidName(userName);
        String userNodePath = parentFolder + validNodeName;
        
    	try {
           
            if (!session.nodeExists(userNodePath)) {

                String domain = StringUtils.substringAfter(userName, "@");
            	String bucket = StringUtils.isNotEmpty(domain) ? "_" + domain.substring(0, 1) : "_" + userName.substring(0, 1);
                
            	String bucketPath =  parentFolder + bucket;
            	
            	// create bucket node if it doesn't exist. For example /content/vendor/admin/_t
            	if (!session.nodeExists(bucketPath)) {
            		
            		Page bucketPage = pageManager.create(parentFolder, bucket, TEMPLATE_CONTENT, bucket);
                    Node pageBucketContainer = bucketPage.getContentResource().adaptTo(Node.class);
                    pageBucketContainer.setProperty("cq:distribute", true);
                    pageBucketContainer.setProperty("cq:lastModified", Calendar.getInstance());
            	}
            	
            	userNodePath =  bucketPath + "/" + validNodeName;
            	
            	if (!session.nodeExists(userNodePath)) {
            		
            		Page page = pageManager.create(bucketPath, validNodeName, TEMPLATE_CONTENT, validNodeName);
                    Node pageContainer = page.getContentResource().adaptTo(Node.class);
                    pageContainer.setProperty("cq:distribute", true);
                    pageContainer.setProperty("cq:lastModified", Calendar.getInstance());
            	}

//                session.save();
                
            } 
            
        } catch (Exception e) {
            log.error("An exception has occured in createNode method with error: " + e.getMessage(), e);
        }
    	
		return userNodePath;
    }
    
    
    
    /**
     * Sets the bean.
     *
     * @param request the request
     * @return the bean
     */
    private UploadRequest createUploadRequest(SlingHttpServletRequest request) throws IOException, ServletException {

    		if (!ServletFileUpload.isMultipartContent(request)) {
    	      return null;
    	    }
    	    
    	 	final Map<String, RequestParameter[]> reqParams = request.getRequestParameterMap();
          
            String batchUuid = request.getParameter(CommonConstants.BATCH_UUID);
            String fileOrder = request.getParameter(CommonConstants.FILE_ORDER);

            UploadRequest uploadRequest = new UploadRequest(reqParams);
            
            uploadRequest.setBatchUuid(batchUuid);
            uploadRequest.setFileOrder(fileOrder);
            
    		int totalFilesCount = 0;
    		if (request.getParameter(CommonConstants.TOTAL_FILES_COUNT) != null) {
    			totalFilesCount = Integer.parseInt(request.getParameter(CommonConstants.TOTAL_FILES_COUNT));
    		}
    		uploadRequest.setTotalFilesCount(totalFilesCount);
    		
    		String invalidFiles = request.getParameter(CommonConstants.INVALID_FILES);
    		if(invalidFiles != null && invalidFiles.length() > 0){
    			uploadRequest.setInvalidFiles(invalidFiles);
    		}
    		
    		String acceptedFiles = request.getParameter(CommonConstants.ACCEPTED_FILES);
    		if(acceptedFiles != null && acceptedFiles.length() > 0){
    			uploadRequest.setAcceptedFiles(acceptedFiles);
    		}
    		
            /* check if request has file inside */
            if (reqParams.containsKey("file")) {
                RequestParameter reqParam = reqParams.get("file")[0];
                String filename = reqParam.getFileName();
                
                List<UploadItem> uploadList = gson.fromJson(request.getParameter("uploadList"), UPLOADLIST_TYPE);
                
                
                for(UploadItem item : uploadList) {
                	if(item.getFileName().equals(filename)) {
                		uploadRequest.setUploadItem(item);
                		break;
                	}
                }
                
                //to get the filename from rejected list
                List<UploadItem> uploadRejectedList = gson.fromJson(request.getParameter("uploadRejectedList"), UPLOADLIST_TYPE);
                
                if(uploadRejectedList != null) 
	                for(UploadItem item : uploadRejectedList) {
	                	if(item.getFileName().equals(filename)) {
	                		uploadRequest.setUploadRejectedItem(item);
	                		break;
	                	}
	                }
            }
            
            if(!uploadRequest.hasUploadItem()) {
            	return uploadRequest;
            }
            
            uploadRequest = uploadRequest.buildMetadata();
            
        return uploadRequest;
    }

    
    
    private void setNodeProperty(Node node, String key, String value) throws RepositoryException {
        if(null == node || null == key || null == value) {
            return;
        }
        try {
            if (StringUtils.isNotEmpty(value)) {
                node.setProperty(key, value);
            }
        } catch (Exception e) {
            log.error("An exception has occured while setting property for " + node.getPath()
                + " for property key: " + key + " with value: " + value + " with error: " + e.getMessage(), e);
        }
    }


    private void setNodeProperty(Node node, String key, Calendar value) throws RepositoryException {
        if(null == node || null == key || null == value) {
            return;
        }
        try {
            node.setProperty(key, value);
        } catch (Exception e) {
            log.error("An exception has occured while setting property for " + node.getPath()
                + " for property key: " + key + " with value: " + value + " with error: " + e.getMessage(), e);
        }
    }

    private void setNodeProperty(Node node, String key, int value) throws RepositoryException {
        if(null == node || null == key ) {
            return;
        }
        try {
            node.setProperty(key, value);
        } catch (Exception e) {
            log.error("An exception has occured while setting property for " + node.getPath()
                + " for property key: " + key + " with value: " + value + " with error: " + e.getMessage(), e);
        }
    }

    private void setNodeProperty(Node node, String key, boolean value) throws RepositoryException {
        if(null == node || null == key) {
            return;
        }
        try {
            node.setProperty(key, value);
        } catch (Exception e) {
            log.error("An exception has occured while setting property for " + node.getPath()
                + " for property key: " + key + " with value: " + value + " with error: " + e.getMessage(), e);
        }
    }
    
    private void sendMailToVendor(SlingHttpServletRequest request, Session session, String batchNodePath) throws RepositoryException {
    	boolean isBatchUpload = Boolean.parseBoolean(request.getParameter("batchUpload"));
    	if(isBatchUpload){
    		batchSubmissionMailToVendor(request, session,  batchNodePath);
    	}else{
    		singleSubmissionMailToVendor(request);
    	}
    }
    
	private void batchSubmissionMailToVendor(SlingHttpServletRequest request, Session session, String batchNodePath) throws RepositoryException {
		StringBuilder builder = new StringBuilder("<br>");
		String userName = ServiceUtils.getUserName(request);
		
		String batchContentResourcePath = batchNodePath + CommonConstants.FORWARD_SLASH
				+ CommonConstants.JCR_CONTENT;
		Node batchContentNode = session.getNode(batchContentResourcePath);
		
		int fileCount = 0;
		if (batchContentNode.hasProperty("filecount")) {
			fileCount = (int) batchContentNode.getProperty("filecount").getValue().getLong();
		}
		
		int totalFilesCount = 0;
		if (batchContentNode.hasProperty(CommonConstants.TOTAL_FILES_COUNT)) {
			totalFilesCount = (int) batchContentNode.getProperty(CommonConstants.TOTAL_FILES_COUNT).getValue().getLong();
		}
		
		int rejectedFilesCount = 0;
		if (batchContentNode.hasProperty(CommonConstants.INVALID_FILES_COUNT)) {
			rejectedFilesCount = (int) batchContentNode.getProperty(CommonConstants.INVALID_FILES_COUNT).getValue().getLong();
		}
		
		int failedFileCount = 0;
		if (batchContentNode.hasProperty(CommonConstants.FAIL_FILES_COUNT)) {
			failedFileCount = (int) batchContentNode.getProperty(CommonConstants.FAIL_FILES_COUNT).getValue().getLong();
		}
		
		String invalidFiles = null;
		if (batchContentNode.hasProperty(CommonConstants.INVALID_FILES)) {
			invalidFiles = batchContentNode.getProperty(CommonConstants.INVALID_FILES).getValue().getString().replaceAll(",", " | ");
		}
		
		String acceptedFiles = null;
		if (batchContentNode.hasProperty(CommonConstants.ACCEPTED_FILES)) {
			acceptedFiles = batchContentNode.getProperty(CommonConstants.ACCEPTED_FILES).getValue().getString().replaceAll(",", " | ");
		}
		
		String failedFiles = null;
		if (batchContentNode.hasProperty(CommonConstants.FAILED_FILES)) {
			failedFiles = batchContentNode.getProperty(CommonConstants.FAILED_FILES).getValue().getString().replaceAll(",", " | ");
		}

		String batchid = request.getParameter(CommonConstants.BATCH_ID);
		String email = request.getParameter(CommonConstants.EMAIL);
		String requestedBy = request.getParameter(CommonConstants.REQUESTED_BY);
		
		builder.append("<h1 style=\"text-align: center;\">SUCCESS : Media Submitted</h1>");
		builder.append("<table style=\"border=\"0px\">");
		builder.append("<tbody>");
		builder.append("<tr>");
		builder.append("<td><b>Upload Type :</b></td>");
		builder.append("<td>");
		builder.append("Batch");
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Requested Name :</b></td>");
		builder.append("<td>");
		builder.append(userName);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Request / Batch Id:</b></td>");
		builder.append("<td>");
		builder.append(batchid);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Requested By :</b></td>");
		builder.append("<td>");
		builder.append(requestedBy);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Vendor email address :</b></td>");
		builder.append("<td>");
		builder.append(email);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Total # of Files Uploaded :</b></td>");
		builder.append("<td>");
		builder.append(totalFilesCount);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Total # of Files Accepted :</b></td>");
		builder.append("<td>");
		builder.append(fileCount);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Total # of Files Rejected :</b></td>");
		builder.append("<td>");
		builder.append(rejectedFilesCount);
		builder.append("</td>");
		builder.append("</tr>");
		if(failedFileCount > 0){
			builder.append("<tr>");
			builder.append("<td><font color='red'><b>Total # of Failed to Upload Files :</b></font></td>");
			builder.append("<td><font color='red'>");
			builder.append(failedFileCount);
			builder.append(" (Please resubmit failed files)</font></td>");
			builder.append("</tr>");
		}
		builder.append("</tbody>");
		builder.append("</table>");

		builder.append("<br>");
		builder.append("<br>");

		builder.append("<table style=\" border=\"0px\">");
		builder.append("<caption><b>Media Attached</b></caption>");
		builder.append("<tbody>");
		builder.append("<tr>");
		builder.append("<td><b>Accepted Files :</b></td>");
		builder.append("<td>");
		builder.append(acceptedFiles);
		builder.append("</td>");
		builder.append("</tr>");
		
		if(failedFiles != null && failedFiles.length() > 0){
			builder.append("<tr><td></td><td></td></tr>");
			builder.append("<tr>");
			builder.append("<td><font color='red'><b>Failed to Upload :</b></font></td>");
			builder.append("<td><font color='red'>");
			builder.append(failedFiles);
			builder.append("(Please resubmit failed files)</font></td>");
			builder.append("</tr>");
		}
		
		builder.append("<tr><td></td><td></td></tr>");
		builder.append("<tr>");
		builder.append("<td><b>Rejected Files :</b></td>");
		builder.append("<td>");
		builder.append(invalidFiles);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("</tbody>");
		builder.append("</table>");

		builder.append("<br>");
		builder.append("<br>");
		builder.append("<p>Please contact <a href = 'mailto: BBBYQCPhoto@bedbath.com'>BBBYQCPhoto@bedbath.com</a> if you have questions or require further assistance.</p>");
		builder.append("<p>Thank You,<br>Digital Assets Team <br>Bed Bath & Beyond</p>");
		builder.append("<br>");
		
		final Map<String, Object> props = new HashMap<String, Object>();    	
    	log.info("Queeing the job for sndind mail to vendor " + userName);

		props.put(CommonConstants.TO, userName);
		props.put(CommonConstants.CC, email);
		//props.put(CommonConstants.BCC, userName);
		props.put(CommonConstants.SUBJECT, MAIL_SUBJECT );
		props.put(CommonConstants.MESSAGE, builder.toString());
		
		jobManager.addJob(CommonConstants.SEND_MAIL_TOPIC, props);

	}
	
	private void singleSubmissionMailToVendor(SlingHttpServletRequest request) throws RepositoryException {
		StringBuilder builder = new StringBuilder("<br>");
		String userName = ServiceUtils.getUserName(request);
		
		int fileCount = 0;
		if (request.getParameter("fileCount") != null) {
			fileCount = Integer.parseInt(request.getParameter("fileCount"));
		}

		String invalidFiles = request.getParameter("invalidFiles");
		if(invalidFiles != null && invalidFiles.length() > 0){
			invalidFiles = invalidFiles.replaceAll(",", " | ");
		}
		String acceptedFiles = request.getParameter("acceptedFiles");
		if(acceptedFiles != null && acceptedFiles.length() > 0){
			acceptedFiles = acceptedFiles.replaceAll(",", " | ");
		}
		String batchid = request.getParameter(CommonConstants.BATCH_ID);
		String email = request.getParameter(CommonConstants.EMAIL);
		String requestedBy = request.getParameter(CommonConstants.REQUESTED_BY);
//		String assetUpdate = request.getParameter("assetUpdate");
//		String reasonUpdate = request.getParameter("reasonUpdate");
		String upc = request.getParameter("upc");
		String assetType = request.getParameter("assetType");
		String shotType = request.getParameter("shotType");
//		String sequence = request.getParameter("sequence");
		
		Map<String, RequestParameter[]> reqParams = request.getRequestParameterMap();
		String filename = "";
        
		/* check if request has file inside */
        if (reqParams.containsKey("file")) {
            RequestParameter reqParam = reqParams.get("file")[0];
            filename = reqParam.getFileName();
        }
		
		builder.append("<h1 style=\"text-align: center;\">SUCCESS : Media Submitted</h1>");
		builder.append("<table style=\"border=\"0px\">");
		builder.append("<tbody>");
		builder.append("<tr>");
		builder.append("<td><b>Upload Type :</b></td>");
		builder.append("<td>");
		builder.append("Single");
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Requested Name :</b></td>");
		builder.append("<td>");
		builder.append(userName);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Request / Batch Id:</b></td>");
		builder.append("<td>");
		builder.append(batchid);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Requested By :</b></td>");
		builder.append("<td>");
		builder.append(requestedBy);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Vendor email address :</b></td>");
		builder.append("<td>");
		builder.append(email);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Total # of Files Accepted :</b></td>");
		builder.append("<td>");
		builder.append(fileCount);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>UPC :</b></td>");
		builder.append("<td>");
		builder.append(upc);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Asset Type :</b></td>");
		builder.append("<td>");
		builder.append(assetType);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Shot Type :</b></td>");
		builder.append("<td>");
		builder.append(shotType);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("</tbody>");
		builder.append("</table>");

		builder.append("<br>");
		builder.append("<br>");

		builder.append("<table style=\" border=\"0px\">");
		builder.append("<caption><b>Media Attached</b></caption>");
		builder.append("<tbody>");
		builder.append("<tr>");
		builder.append("<td><b>Accepted Files :</b></td>");
		builder.append("<td>");
		builder.append(filename);
		builder.append("</td>");
		builder.append("</tr>");
		builder.append("</tbody>");
		builder.append("</table>");

		builder.append("<br>");
		builder.append("<br>");
		builder.append("<p>Please contact <a href = 'mailto: BBBYQCPhoto@bedbath.com'>BBBYQCPhoto@bedbath.com</a> if you have questions or require further assistance.</p>");
		builder.append("<p>Thank You,<br>Digital Assets Team <br>Bed Bath & Beyond</p>");
		builder.append("<br>");
		
		final Map<String, Object> props = new HashMap<String, Object>();    	
    	log.info("Queeing the job for sndind mail to vendor " + userName);

		props.put(CommonConstants.TO, userName);
		props.put(CommonConstants.CC, email);
		//props.put(CommonConstants.BCC, userName);
		props.put(CommonConstants.SUBJECT, MAIL_SUBJECT );
		props.put(CommonConstants.MESSAGE, builder.toString());
		
		jobManager.addJob(CommonConstants.SEND_MAIL_TOPIC, props);

	}
    
	// Separated the accepted files, those are not uploaded to the server due to any network issue.
	private HashMap<String, String> getAcceptedRejectedFiles(ArrayList<String> cqPageNameList, String acceptedFiles) {
		String[] fileNames = acceptedFiles.split(",");
		ArrayList<String> acepetedFileNames = new ArrayList<String>();
		ArrayList<String> rejectedFileNames = new ArrayList<String>();

		Set<String> cqPageNameSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		for (String str : cqPageNameList) {
			cqPageNameSet.add(str.trim());
		}

		for (String filename : fileNames) {
			/*String changeFilename = StringUtils.replace(StringUtils.trim(filename), "\u00A0", "");
			changeFilename = StringUtils.replace(changeFilename, " ", "");// remove_spaces
			changeFilename = ServiceUtils.replaceLast(changeFilename, ".", "");// remove_dot
			changeFilename = JcrUtil.createValidName(changeFilename);*/
			String changeFilename = ServiceUtils.buildFilePageName(filename);
			if (cqPageNameSet.contains(changeFilename)) {
				acepetedFileNames.add(filename);
			} else {
				rejectedFileNames.add(filename);
			}
		}

		HashMap<String, String> map = new HashMap<String, String>();
		if (acepetedFileNames != null && acepetedFileNames.size() > 0) {
			map.put("accept", String.join(" | ", acepetedFileNames));
		}
		if (rejectedFileNames != null && rejectedFileNames.size() > 0) {
			map.put("reject", String.join(" | ", rejectedFileNames));
		}
		return map;
	}
	
	private void saveAcceptedRejectedFiles(SlingHttpServletRequest request, Session session, String batchNodePath) {
		try {
			ArrayList<String> cqPageNameList = ServiceUtils.getChildCQPageName(resolverFactory, batchNodePath);

			int fileCount = 0;
			if (request.getParameter(CommonConstants.FILE_COUNT) != null) {
				fileCount = Integer.parseInt(request.getParameter(CommonConstants.FILE_COUNT));
			}

			String acceptedFiles = request.getParameter(CommonConstants.ACCEPTED_FILES);
			String acceptedFiles1 = request.getParameter(CommonConstants.ACCEPTED_FILES);
			if (acceptedFiles != null && acceptedFiles.length() > 0) {
				acceptedFiles = acceptedFiles.replaceAll(",", " | ");
			}

			String failedFiles = null;
			if (fileCount > 0 && cqPageNameList.size() != fileCount && acceptedFiles1 != null
					&& acceptedFiles1.length() > 0) {
				int failFilesCount = fileCount - cqPageNameList.size();
				fileCount = cqPageNameList.size();
				HashMap<String, String> map = getAcceptedRejectedFiles(cqPageNameList, acceptedFiles1);
				acceptedFiles = map.get("accept");
				failedFiles = map.get("reject");

				String batchContentResourcePath = batchNodePath + CommonConstants.FORWARD_SLASH
						+ CommonConstants.JCR_CONTENT;
				Node batchContentNode = session.getNode(batchContentResourcePath);

				setNodeProperty(batchContentNode, CommonConstants.ACCEPTED_FILES, acceptedFiles);
				setNodeProperty(batchContentNode, CommonConstants.FAILED_FILES, failedFiles);
				setNodeProperty(batchContentNode, "filecount", fileCount);
				setNodeProperty(batchContentNode, CommonConstants.FAIL_FILES_COUNT, failFilesCount);

			}
		} catch (RepositoryException e) {
			 log.error("An exception has occured while setting property for " + e.getMessage(), e);
		}

	}
	
}
