package com.bbby.aem.core.util;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.PayloadMap;
import com.adobe.granite.workflow.exec.Workflow;
import com.bbby.aem.core.models.data.DropzoneUploadRequest;
import com.bbby.aem.core.models.data.UploadItem;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.AssetReferenceSearch;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Assorted mix of helper methods.
 *
 * @author joelepps
 *
 */
public class ServiceUtils {

    private static final Logger log = LoggerFactory.getLogger(ServiceUtils.class);

    /** The universal date string format **/
    private static final TimeZone VENDOR_PORTAL_TIMEZONE = TimeZone.getTimeZone("UTC");
    private static final SimpleDateFormat DATE_FORMAT_YYYY_MM_DD = new SimpleDateFormat("MM/dd/yyyy");
    private static final TimeZone CALIFORNIA_TIMEZONE = TimeZone.getTimeZone("PST");
    private static final String DAM_ROOT = "/content/dam";

    public static String REJECT_REASON_DEFAULT = "not-set";
    public static String REJECT_REASON_LABEL_DEFAULT = "Not Set";
    /**
     * Parses out a page's jcr:content node.
     * <p>
     * If jcr:content is not on the path, it's assumed path is to the page itself and jcr:content is appended.
     * <p>
     * This method does not validate existence of returned path.
     *
     * @param path Path to evaluate
     * @return jcr:content path
     */
    public static String getJcrContentPath(String path) {
        if (StringUtils.isBlank(path)) return null;

        if (path.endsWith(CommonConstants.JCR_CONTENT)) return path;

        int idx = path.indexOf("/jcr:content");
        if (idx >= 0) {
            return path.substring(0, idx + "/jcr:content".length());
        }

        idx = path.indexOf("/jcr:frozenNode");
        if (idx >= 0) {
            return path.substring(0, idx + "/jcr:frozenNode".length());
        }

        if (!path.endsWith("/")) path += "/";
        return path + CommonConstants.JCR_CONTENT;
    }

    /**
     * Append the html extension to a {@code url}.
     *
     * @param resolver ResourceResolver
     * @param url URL to evaluate
     * @return URL with extension (if needed)
     */
    @Nullable
    public static String appendLinkExtension(ResourceResolver resolver, String url) {
        if (url == null) return null;

        Resource resource = resolver.getResource(url);
        String result = url;
        if (resource != null) {
            Page page = resource.adaptTo(Page.class);
            if (page != null) {
                result = url + ".html";
            }
        }

        log.trace("Append link extension: {} -> {}", url, result);
        return result;
    }

    /**
     * Pick the first non-null and non-empty string.
     *
     * @param strings Strings to coalesce
     * @return first non-null, non-empty string
     */
    public static String coalesce(String... strings) {
        if (strings == null || strings.length == 0) return null;
        for (String s : strings) {
            if (!StringUtils.isBlank(s)) return s;
        }
        return null;
    }

    /**
     * Concatenate multiple strings into one. Skips any null or empty strings.
     *
     * @param delimiter Delimeter string
     * @param strings Array of strings
     *
     * @return Concatonated string
     */
    public static String concat(String delimiter, String... strings) {
        if (strings == null || strings.length == 0) return null;
        if (StringUtils.isBlank(delimiter)) delimiter = ",";

        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            if (StringUtils.isNotBlank(s)) {
                sb.append(s).append(delimiter);
            }
        }

        if (sb.length() > 0) {
            // fence post, remove trailing delimiter
            return sb.substring(0, sb.length() - delimiter.length());
        } else {
            return null;
        }
    }

    /**
     * Checks if a {@code candidate} tag is a descendant of the {@code parent} tag. {@code candidate} does not have to
     * be an immediate child of {@code parent}.
     * <p>
     * If true, then returned value is the corresponding immediate child tag of {@code parent}. If false, then returned
     * value is null.
     * <p>
     * Example: <br>
     * {@code parent: tagfamily:united-states}<br>
     * {@code candidate: tagfamily:united-states/california/san-francisco/tenderloin}<br>
     * {@code return: tagfamily:united-states/california}
     *
     * @param parent parent tag
     * @param candidate candidate child tag
     * @return child tag of parent or null
     */
    public static Tag isDescendantTag(Tag parent, Tag candidate) {
        if (parent == null || candidate == null) return null;

        String candidateId = candidate.getTagID();
        String parentId = parent.getTagID();

        if (candidateId.startsWith(parentId) && candidateId.length() != parentId.length()) {
            Tag currentCandidate = candidate;
            while (currentCandidate != null) {
                Tag parentCandidate = currentCandidate.getParent();

                if (parent.equals(parentCandidate)) {
                    return currentCandidate;
                }

                currentCandidate = parentCandidate;
            }
        }

        return null;
    }

    public static boolean isGhostNode(Resource r) {
        if (r == null) return false;
        return "wcm/msm/components/ghost".equals(r.getResourceType());
    }
    /**
     * Gets the user group.
     *
     * @param resolverFactory the resolver factory
     * @param userName the user name
     * @return the user group
     */
    public static String getUserGroup(ResourceResolverFactory resolverFactory, String userName)
    {   
        return "admin";
    }

    /**
     * Gets the user name.
     *
     * @param request the request
     * @return the user name
     */
    public static String getUserName(SlingHttpServletRequest request)
    {
        String userId = null;
        
        ResourceResolver resourceResolver = request.getResourceResolver();
        if(resourceResolver != null) {
        	Session session = resourceResolver.adaptTo(Session.class);
            userId = session.getUserID();
        }
        
        
        return userId != null ? userId : "admin";

    }

   
    public static String getUserPagePath(String userName, String groupName, ResourceResolver resourceResolver)
    {
    	String parentPath = CommonConstants.VENDOR_ROOT_PATH + groupName; 
    
    	Session session = resourceResolver.adaptTo(Session.class);
    	
    	String validNodeName = JcrUtil.createValidName(userName);
        String userNodePath = parentPath + "/" + validNodeName;
        
		try {

			if (!session.nodeExists(userNodePath)) {
				
				String domain = StringUtils.substringAfter(userName, "@");
				String bucket = StringUtils.isNotEmpty(domain) ? "_" + domain.substring(0, 1) : "_" + userName.substring(0, 1);

				userNodePath = parentPath + "/" + bucket + "/" + validNodeName;
			}
			
			
			
		} catch (Exception e) {
			log.error("An exception has occured in createNode method with error: " + e.getMessage(), e);
		}

    	return userNodePath;
    }
    
    /**
     * This method replaces [^alphanumerics] with a special char
     * in order to produce a readable and consistent node name
     * for the JCR.
     *
     * @param stringToChange the domain name of the office.
     * @return a String with all non-alphanumeric chars changed
     * to a special char.
     */
    public static String replaceNonAlphanumerics(String stringToChange) {
        return (stringToChange == null) ? null : stringToChange.replaceAll(":[0-9]+", "")
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase()
            .trim();
    }

    public static String getVendorPortalProjectTitle (String vendorNameNoSpaces, String batchName) {
        return ServiceUtils.replaceNonAlphanumerics(vendorNameNoSpaces) + "_" + "_" + batchName;
    }

    public static String getProjectPath(Session session) {
        String projectPath = "";

        try {
            Node projectFolder = JcrUtils.getOrCreateByPath("/content/projects/vendor_portal_projects", "sling:Folder", session);
            projectFolder.setProperty("jcr:title", "Vendor Portal Projects");
            projectFolder.setProperty("sling:resourceType", "cq/gui/components/projects/admin/card/foldercard");
            session.save();
            projectPath = projectFolder.getPath();
        } catch (Exception e) {
            log.error("An error has occured while attempting to get project path with error: " + e.getMessage(), e);

        }

        return projectPath;
    }

    public static Calendar getDate(String date) {
        Calendar c = Calendar.getInstance(VENDOR_PORTAL_TIMEZONE);

        try {
            SimpleDateFormat dateFormat = DATE_FORMAT_YYYY_MM_DD;
            dateFormat.setTimeZone(CALIFORNIA_TIMEZONE);
            Date d = dateFormat.parse(date);
            c.setTime(d);
        } catch (Exception e) {
            log.error("Error in creating calendar object with string : " + date + " with error: " + e.getMessage(), e);
            log.error("Due to error, calendar default to today's date: " + c.getTime().toString());
        }

        return c;
    }
    
    public static String getCurrentDateStr(String format) {
    	String date = null;

        try {
        	date = new SimpleDateFormat(format).format(new java.util.Date());
        } catch (Exception e) {
            log.error("Error in creating Current Date object with string : " + date + " with error: " + e.getMessage(), e);
        }

        return date;
    }

    public static String getDateStr(Calendar c, String format) {
    	String date = null;
        try {
        	date = new SimpleDateFormat(format).format(c.getTimeInMillis());
        } catch (Exception e) {
            log.error("Error in creating Current Date object with string : " + date + " with error: " + e.getMessage(), e);
        }

        return date;
    }

    public static String extractMonth(String date) {
        String month = "1";

        try {
            month = new Integer(Integer.parseInt(date.split("/")[0])).toString();
        } catch (Exception e) {
            log.error("Error in extracting month with string : " + date + " with error: " + e.getMessage(), e);
        }

        return month;
    }

    public static String extractYear(String date) {
        String year = "";

        try {
            year = date.split("/")[2];
        } catch (Exception e) {
            log.error("Error in extracting year with string : " + date + " with error: " + e.getMessage(), e);
        }

        return year;
    }

    public static Map<String, Object> getWriterServiceMap() {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(ResourceResolverFactory.SUBSERVICE, "writeservice");
        return param;
    }

    public static void saveSession(Session session) throws RepositoryException {
        try {
            session.save();
        } catch (Exception e) {
        	log.error("Session save error: {}", e.getLocalizedMessage());
            session.refresh(false);
        }
    }

    public static void commitChanges(ResourceResolver resourceResolver) {
        try {
            resourceResolver.commit();
        } catch (Exception e) {
            resourceResolver.refresh();
        }
    }

    public static Page createPage(Session session, PageManager pageManager, String wrapperPath, String pageName, String template) {
        Page newPage = null;
        try {
            newPage = pageManager.getPage(wrapperPath + "/" + pageName);
            if(null == newPage) {
                newPage = pageManager.create(wrapperPath, pageName, template, pageName);
            }
            session.save();
        } catch (Exception e) {

        }
        return newPage;
    }

    public static InputStream getFileInputStream(SlingHttpServletRequest request) {
        InputStream fileInputStream = null;
        try {
            Map<String, RequestParameter[]> reqParams = request.getRequestParameterMap();
            RequestParameter reqParam = reqParams.get("file")[0];
            fileInputStream = reqParam.getInputStream();
        } catch (Exception e) {
            log.error("An exception has occured in getFileInputStream fucntion with exception error: " + e.getMessage(), e);
        }
        return fileInputStream;
    }

    public static UploadItem getUploadItem(DropzoneUploadRequest dropzoneUploadRequest) {
        UploadItem item = new UploadItem();
        try {
            Integer index = new Integer(dropzoneUploadRequest.getFileOrder());
            item = dropzoneUploadRequest.getUploadList().get(index);
        } catch (Exception e) {
            log.error("An exception has occured in getUploadItem fucntion with exception error: " + e.getMessage(), e);
        }
        return item;
    }

    public static List<Node> getProjectAssets(Session session, String damFolderPath) {
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

    public static  ResourceResolver getResourceResolverFromSession(final Session session,
                                                            final ResourceResolverFactory resourceResolverFactory) {

        try {
            return resourceResolverFactory.getResourceResolver(Collections.singletonMap("user.jcr.session", (Object) session));
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param resolverFactory
     * @param subServiceName
     * @return
     * @throws LoginException
     */
    public static ResourceResolver getResourceResolver(ResourceResolverFactory resolverFactory,
                                                       String subServiceName) throws LoginException {
        Map<String, Object> authInfo = new HashMap<String, Object>();
        authInfo.put(ResourceResolverFactory.SUBSERVICE, subServiceName);
        ResourceResolver resolver = resolverFactory.getServiceResourceResolver(authInfo);
        return resolver;
    }
    
    /**
     * Checks run modes in an attempt to determine if current environment is pre-production.
     *
     * @param slingSettingsService
     * @return default is {@code false} unless runmode "dev", "qa", or "uat" is found.
     */
    public static boolean isPreProd(SlingSettingsService slingSettingsService) {
        Set<String> runModes = slingSettingsService.getRunModes();
        if (runModes.contains("production") || runModes.contains("prod") ||
            (!runModes.contains("dev") && !runModes.contains("qa") && !runModes.contains("uat"))) {
            log.trace("Running in production mode");
            return false;
        } else {
            log.trace("Running in pre-prod mode");
            return true;
        }
    }
    
    public static String getProjectCleanupMonthName(String monthNumber){

        String monthString;
        switch (monthNumber) {
            case "01":  monthString = "01-JAN";
                break;
            case "1":  monthString = "01-JAN";
                break;
            case "02":  monthString = "02-FEB";
                break;
            case "2":  monthString = "02-FEB";
                break;
            case "03":  monthString = "03-MAR";
                break;
            case "3":  monthString = "03-MAR";
                break;
            case "04":  monthString = "04-APR";
                break;
            case "4":  monthString = "04-APR";
                break;
            case "05":  monthString = "05-MAY";
                break;
            case "5":  monthString = "05-MAY";
                break;
            case "06":  monthString = "06-JUN";
                break;
            case "6":  monthString = "06-JUN";
                break;
            case "07":  monthString = "07-JUL";
                break;
            case "7":  monthString = "07-JUL";
                break;
            case "08":  monthString = "08-AUG";
                break;
            case "8":  monthString = "08-AUG";
                break;
            case "09":  monthString = "09-SEP";
                break;
            case "9":  monthString = "09-SEP";
                break;
            case "10": monthString = "10-OCT";
                break;
            case "11": monthString = "11-NOV";
                break;
            case "12": monthString = "12-DEC";
                break;
            default: monthString = "NA";
                break;
        }

        return monthString;


    }
    
	public static String getExcelVersion(String excelPath, ResourceResolverFactory resolverFactory) {

		String version = "";
		ResourceResolver resolver =  null;
		try {
			resolver = getResourceResolver(resolverFactory, "writeservice");
			Node node = resolver.getResource(excelPath).adaptTo(Node.class);
			Node metadataNode = node.getNode(CommonConstants.METADATA_NODE);
			if(metadataNode.hasProperty("dc:description")){
				version = metadataNode.getProperty("dc:description").getString();
			}
		} catch (Exception e) {
			log.error("An exception has occured in getExcelVersion fucntion with exception error: " + e.getMessage(),e);
		} finally {
			if(resolver != null)
				resolver.close();
		}

		return version;
	}
	
	public static List<Workflow> listRunningWorkflowonPayload(String assetPath, boolean excludeSystemWorkflows,
			ResourceResolver resourceResolver) {
		List<Workflow> workflows =  null;
		PayloadMap payloadMap = resourceResolver.adaptTo(PayloadMap.class);
		if (payloadMap.isInWorkflow(assetPath, excludeSystemWorkflows)) {
			workflows = payloadMap.getWorkflowInstances(assetPath, excludeSystemWorkflows);
		}else{
			log.debug("No workflow instance is running on payload{}", assetPath);
		}
		return workflows;
	}
	
	public static boolean isInWorkflow(String assetPath, boolean excludeSystemWorkflows,
			ResourceResolver resourceResolver) {
		PayloadMap payloadMap = resourceResolver.adaptTo(PayloadMap.class);
		return payloadMap.isInWorkflow(assetPath, excludeSystemWorkflows);
	}
	
	public static Map<String, Asset> listReferences(Node node, ResourceResolver resourceResolver) {
		AssetReferenceSearch search = new AssetReferenceSearch(node, DAM_ROOT, resourceResolver);
		Map<String, Asset> result = search.search();
		return result;
	}
	
	public static ArrayList<String> getMembersOfImageset(Session session, String rootPath) {
		ArrayList<String> memberList = new ArrayList<String>();
		try {
			rootPath = rootPath +"/jcr:content/related/s7Set/sling:members";
			String q = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([" + rootPath
					+ "])";
			Query myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
			QueryResult result = myQuery.execute();
			NodeIterator ni = result.getNodes();
			while (ni.hasNext()) {
				memberList.add(ni.nextNode().getPath());
			}
		} catch (Exception e) {
			log.debug("Error in getMembersOfImageset(), could not find: " + rootPath + " : " + e.getMessage());
		}
		return memberList;
	}
	
	public static long getAssetsCountInChildDirectory(ResourceResolver resourceResolver, String rootPath) {
		int count = 0;
		try {
			String q = "SELECT * FROM [dam:Asset] AS s WHERE ISCHILDNODE([" + rootPath
					+ "])";
			Instant start = Instant.now();
			//Query myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
			//QueryResult result = myQuery.execute();
			int index =0;
			Iterator<Resource> result = resourceResolver.findResources(q, Query.JCR_SQL2);
			while(result.hasNext()){
				result.next();
				index++;
			}
			
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis(); // in_millis
			log.debug("Total Time to fetch Asset count : " + timeElapsed + " MilliSecond or "+ TimeUnit.MILLISECONDS.toSeconds(timeElapsed) + " Second");
			
			//NodeIterator ni = result.getNodes();
			return index;
		} catch (Exception e) {
			log.debug("Error, could not find: " + rootPath + " : " + e.getMessage());
		}
		return count;
	}
	
	public static long getChildDirectoryCount(ResourceResolver resourceResolver, String rootPath) {
		int count = 0;
		try {
			String q = "SELECT * FROM [nt:base] AS s WHERE ISCHILDNODE([" + rootPath
					+ "]) and ([jcr:primaryType] = 'sling:Folder' or [jcr:primaryType] = 'sling:OrderedFolder')";
			Instant start = Instant.now();
			//Query myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
			//QueryResult result = myQuery.execute();
			int index =0;
			Iterator<Resource> result = resourceResolver.findResources(q, Query.JCR_SQL2);
			while(result.hasNext()){
				result.next();
				index++;
			}
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis(); // in_millis
			log.info("Total Time to fetch Directory count: " + timeElapsed + " MilliSecond or "+ TimeUnit.MILLISECONDS.toSeconds(timeElapsed) + " Second");
			//NodeIterator ni = result.getNodes();
			return index;
		} catch (Exception e) {
			log.debug("Error, could not find: " + rootPath + " : " + e.getMessage());
		}
		return count;
	}
	
	public static String getChildCount(ResourceResolver resourceResolver, String rootPath) {
		String count = "";

		long assetCount = getAssetsCountInChildDirectory(resourceResolver, rootPath);
		log.info("Asset count is "+assetCount);
		/*if (assetCount > 0) {
			if (assetCount == 1) {
				count = assetCount + " Asset";
			} else {
				count = assetCount + " Assets";
			}
		} else {
			count = "No Asset";
		}*/
		count = assetCount + " A";
		long directoryCount = getChildDirectoryCount(resourceResolver, rootPath);
		log.info("Directory count is "+assetCount);
		/*if (directoryCount > 0) {
			if (directoryCount == 1) {
				count = count + ", " + directoryCount + " Directory";
			} else {
				count = count + ", " + directoryCount + " Directories";
			}
		} else {
			count = count + ", No Directory";
		}*/
		count = count + ", " + directoryCount + " D";
		return count;
	}
	
	public static boolean startsWithTag(Node node, String tagValue) throws ValueFormatException, IllegalStateException, RepositoryException {
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
	
	public static boolean isRejectedAsset(Node metadataNode) throws RepositoryException {
		boolean isRejectedAsset = false;
		if (metadataNode.hasProperty(CommonConstants.BBBY_REJECTION_REASON)) {
			String rejectReason = metadataNode.getProperty(CommonConstants.BBBY_REJECTION_REASON).getString();
			if (StringUtils.isNotEmpty(rejectReason) && !rejectReason.equalsIgnoreCase(REJECT_REASON_DEFAULT) && !rejectReason.equalsIgnoreCase(REJECT_REASON_LABEL_DEFAULT)) {
				isRejectedAsset = true;
			}
		}
		return isRejectedAsset;
	}
	
	public static boolean checkReferences(Resource resource, Session session) throws Exception {
		log.debug("entering checkReferences() method");
		boolean hasRefereces = false;
		boolean hasAllReferenceAssets = false;
		boolean hasFinalRefereces = false;
		ArrayList<String> memberList = getMembersOfImageset(session, resource.getPath());
		List<Node> nodes = listAssetsOfImageset(resource, session);
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
		//DAM-1297
		if (hasRefereces) {
			hasAllReferenceAssets = checkAllReferenceAssets(nodes, session);
			log.info("hasAllReferenceAssets() method returns: " + hasAllReferenceAssets);
		}
		
		//DAM-1324 : Reference missing banner changes and PC wf changes
		String damScene7FileStatusValue = getMetadataValue(resource, CommonConstants.DAM_SCENE_7_FILE_STATUS, "");
		if (hasRefereces && hasAllReferenceAssets && StringUtils.isNotBlank(damScene7FileStatusValue)){
			hasFinalRefereces = true;
			log.info("hasFinalRefereces() method returns: " + hasFinalRefereces);
		}else{
			log.info("hasFinalReferences() method yields false result.");
		}
		return hasFinalRefereces;
	}
	
	public static boolean checkAllReferenceAssets(List<Node> nodes ,Session session) throws Exception {
		log.info("Inside checkAllReferenceAssets() method.");
		boolean hasAllReferenceAssets = true;
		for (int i=0; i<nodes.size(); i++){
			Node node = nodes.get(i);
			String path = node.getPath();
			log.info("Root path node for checkAllReferenceAssets() method is" + node);
			try {
				String query = "SELECT * FROM [nt:unstructured] AS s WHERE s.[dam:resolvedPath]= '%s' OR s.[sling:resource]= '%s'" ;
				String q = String.format(query, path, path);
				log.info("Query executed is:" + q);
				Query myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
				QueryResult result = myQuery.execute();
				NodeIterator ni = result.getNodes();
				if(ni.getSize()<=0 ){
					log.info("In checkAllReferenceAssets() method, query gives zero result.");
					hasAllReferenceAssets = false;
					break;
				}
			} catch (Exception e) {
				log.info("checkAllReferenceAssets() method fails due to exception" + e);
				hasAllReferenceAssets = false;
			}
		}
		return hasAllReferenceAssets;
	}
	
	public static List<Node> listAssetsOfImageset(Resource resource, Session session) throws Exception {
		log.debug("entering listAssetsOfImageset() method");
		@SuppressWarnings("deprecation")
		ImageSet imageSet = resource.adaptTo(ImageSet.class);
		List<Node> assetList = new ArrayList<Node>();
		Iterator<Asset> assets = imageSet.getImages();

		while (assets.hasNext()) {
			Asset asset = assets.next();
			assetList.add(session.getNode(asset.getPath()));
			log.info("{} is a image in imageset {}", asset.getName(), imageSet.getName());
		}
		return assetList;
	}
	
	public static Object getBooleanValue(String propValue) {
        if (StringUtils.equalsIgnoreCase("Yes", propValue))
            return true;
        else if (StringUtils.equalsIgnoreCase("No", propValue))
            return false;
        else
            return "";
    }

	public static String getTagValue(String tagName, Node metadataNode) throws RepositoryException {
        String tagValue = null;
        if (metadataNode.hasProperty(CommonConstants.CQ_TAGS)) {
            Property tagProp = metadataNode.getProperty(CommonConstants.CQ_TAGS);
            Value[] tags = tagProp.getValues();
            for (Value tagVal : tags) {
                String tag = tagVal.getString();
                if (StringUtils.startsWith(tag, tagName)) {
                    tagValue = WordUtils.capitalize(StringUtils.substringAfterLast(tag, "/").replace("_", " "));
                }
            }
        }
        return tagValue;
    }
	
    /**
     *Method to extract property of an resource
     */
	public static String getMetadataValue(Resource resource, String prop, String defaultValue) {
        String value = null;
        Resource contentResource = resource.getChild(JcrConstants.JCR_CONTENT);
        if (contentResource != null) {
                Resource metadataResource = contentResource.getChild(DamConstants.METADATA_FOLDER);
                if (metadataResource != null) {
                	ValueMap props = metadataResource.getValueMap();
                	value = props.get(prop, defaultValue);
                }
        }
        return value;
    }
	
	public static boolean isTagPresentMutipleTimes(String tagName, Node metadataNode) throws RepositoryException {
		ArrayList<String> tagValueList = new ArrayList<String>();
		boolean isTagPresentMutipleTimes = false;
		if (metadataNode.hasProperty(CommonConstants.CQ_TAGS)) {
			Property tagProp = metadataNode.getProperty(CommonConstants.CQ_TAGS);
			Value[] tags = tagProp.getValues();
			for (Value tagVal : tags) {
				String tag = tagVal.getString();
				if (StringUtils.startsWith(tag, tagName)) {
					tagValueList.add(tag);
				}
			}
		}
		if (tagValueList.size() > 1) {
			isTagPresentMutipleTimes = true;
		}
		return isTagPresentMutipleTimes;
	}
	
    //DAM-112 : Folder Level Mandatory Metadata and also use for call to PDM from VAH folder
	public static String getUPCorCqTagsMissingMessage(Node resNode) {
		String value = null;

		boolean hasUPC = true;
		boolean hasSKU = true;
		boolean hasAssetType;
		boolean hasShotType;
		boolean isAssetTypePresentMutiple;
		boolean isShotTypePresentMutiple;
		try {
			//Node resNode = resource.adaptTo(Node.class);
			Node metadataNode = resNode.getNode(CommonConstants.METADATA_NODE);
			
			//String upcValue = getMetadataValue(resource, CommonConstants.BBBY_UPC, null);
			//String skuValue = getMetadataValue(resource, CommonConstants.BBBY_SKU, null);
			
			String upcValue = (metadataNode.hasProperty(CommonConstants.BBBY_UPC)) ? metadataNode.getProperty(CommonConstants.BBBY_UPC).getString() : null;
			String skuValue = (metadataNode.hasProperty(CommonConstants.BBBY_SKU)) ? metadataNode.getProperty(CommonConstants.BBBY_SKU).getString() : null;
			if(resNode.getPath().contains("/content/dam/marketing")){
				hasAssetType = startsWithTag(resNode, CommonConstants.MARKETING_ASSET_TYPE);
				hasShotType = startsWithTag(resNode, CommonConstants.MARKETING_SHOT_TYPE);
			} else{
				hasAssetType = startsWithTag(resNode, CommonConstants.BBBY_ASSET_TYPE);
				hasShotType = startsWithTag(resNode, CommonConstants.BBBY_SHOT_TYPE);
			}

			if (upcValue == null || upcValue == "") {
				hasUPC = false;
			}

			if (skuValue == null || skuValue == "") {
				hasSKU = false;
			}
			if(resNode.getPath().contains("/content/dam/marketing")){
				isAssetTypePresentMutiple = isTagPresentMutipleTimes(CommonConstants.MARKETING_ASSET_TYPE, metadataNode);
				isShotTypePresentMutiple = isTagPresentMutipleTimes(CommonConstants.MARKETING_SHOT_TYPE, metadataNode);
			} else{
				isAssetTypePresentMutiple = isTagPresentMutipleTimes(CommonConstants.BBBY_ASSET_TYPE, metadataNode);
				isShotTypePresentMutiple = isTagPresentMutipleTimes(CommonConstants.BBBY_SHOT_TYPE, metadataNode);
			}
			
			if (!hasUPC && !hasSKU && !hasAssetType && !hasShotType) {
				value = "UPC/SKU & Asset/Shot Type Missing ...";
			} else if (!hasUPC && !hasSKU && !hasAssetType) {
				value = "UPC/SKU & Asset Type Missing ...";
			} else if (!hasUPC && !hasSKU) {
				value = "UPC/SKU Missing ...";
			} else if (!hasAssetType) {
				value = "Asset Type Missing ...";
			} else if(isAssetTypePresentMutiple && isShotTypePresentMutiple){
				value = "Multiple Asset & Shot Type ...";
			} else if(isAssetTypePresentMutiple){
				value = "Multiple Asset Type ...";
			} else if (!hasShotType) {
				if(!resNode.getPath().contains("/content/dam/marketing")){ // shot type check not required for marketing assets.
					String assetType = getTagValue(CommonConstants.BBBY_ASSET_TYPE, metadataNode);
					if ("Single Product".equalsIgnoreCase(assetType)) {
						value = "Shot Type Missing ...";
					}
				}
			} else if(isShotTypePresentMutiple){
				value = "Multiple Shot Type ...";
			} 

		} catch (Exception e) {
			log.debug("Error in getUPCorCqTagsMissingMessage() method " + e);
		}
		return value;
	}
	
    public static String getHostName() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Exception while getting Host name",e);
        }
        return hostname;
    }
    
    public static String getColorSpace(Resource assetResource) {
    	BufferedImage image = null;
        String colorSpace = "";
        try{
	        Asset asset = assetResource.adaptTo(Asset.class);
	        InputStream stream = asset.getOriginal().getStream();
            image = ImageIO.read(stream);
            ColorSpace cs = image.getColorModel().getColorSpace();
            colorSpace = AssetHasherUtils.getColorSpaceName(cs.getType());
            stream.close();
        }catch(Exception e){
        }finally{
        	if(colorSpace.contentEquals("")){
        		colorSpace = "not-readable";
        	}
        }
        return colorSpace;
    }
    
    public static String getAssetType(Node metadataNode) throws RepositoryException {
        String assetType = null;
        if (metadataNode.hasProperty(CommonConstants.CQ_TAGS)) {
            Property tagProp = metadataNode.getProperty(CommonConstants.CQ_TAGS);
            Value[] tags = tagProp.getValues();
            for (Value tagVal : tags) {
                String tag = tagVal.getString();
                if (StringUtils.startsWith(tag, CommonConstants.BBBY_ASSET_TYPE)) {
                   assetType = tag;
                }
            }
        }
        return assetType;
    }
    
	public static boolean checkForDupsForMarketing(String fileName, String path, ResourceResolverFactory resolverFactory) throws Exception {
		boolean dupCheck = false;
		ResourceResolver resourceResolver = getResourceResolver(resolverFactory, "writeservice");
		// We need to query the dam to see if any other asset has this
		// filename. if it does we move the current asset to the dups folder
		String q = "SELECT * FROM [dam:Asset] AS s WHERE ISDESCENDANTNODE([/content/dam]) " + "and NAME() = \""
				+ fileName + "\"";

		Iterator<Resource> batchNodes = resourceResolver.findResources(q, Query.JCR_SQL2);
		while (batchNodes.hasNext()) {
			dupCheck = true;
			Resource batchResource = (Resource) batchNodes.next();
			Node node = batchResource.adaptTo(Node.class);
			if (node.getParent().getPath().equals(path)) {
				dupCheck = false;
				log.info("Duplicate Asset is on same path...");
				break;
			}
		}
		return dupCheck;
	}
	
	public static List<Node> getImagesetMembers(ImageSet imageSet, Session session) throws Exception {
		log.info("entering listAssetsOfImageset() method");
		List<Node> assetList = new ArrayList<Node>();
		List<Node> assetPri = new ArrayList<Node>();
		List<Node> assetAlt = new ArrayList<Node>();
		Iterator<Asset> assets = imageSet.getImages();

		int i = 0;
		while (assets.hasNext()) {
			log.info("In ServiceUtils getImagesetMembers() method has assets.");
			Asset asset = assets.next();
			if ("yes".equalsIgnoreCase(asset.getMetadataValue(CommonConstants.BBBY_PRIMARY_IMAGE))) {
				assetPri.add(session.getNode(asset.getPath()));
				log.info("{} is a primary image in imageset {}", asset.getName(), imageSet.getName());
			} else {
				assetAlt.add(session.getNode(asset.getPath()));
				log.info("{} is alternate image no. {} in imageset {}", asset.getName(), i++, imageSet.getName());
			}

		}
		assetList.addAll(assetPri);
		assetList.addAll(assetAlt);
		return assetList;
	}
	
	public static String getDestPath(Session session, Node node) throws Exception {
		log.info("Get Dest Path : " + node.getPath());
		String destination = null;
		Node meta = node.hasNode(CommonConstants.METADATA_NODE) ? node.getNode(CommonConstants.METADATA_NODE) : null;
		String assetType = ServiceUtils.getAssetType(meta);
		if (assetType != null) {
			destination = "/content/dam/bbby/" + assetType.substring(CommonConstants.BBBY_ASSET_TYPE.length() + 1);
			ArrayList<String> partitions = PartitionUtil.hashFileName(node.getName());
			// create the holding partitions if they don't exist
			destination = destination + "/" + partitions.get(0);
			destination = destination + "/" + partitions.get(1);
			// setting up the final path in the map
			destination = destination + "/" + node.getName();
		} else {
			log.debug("AssetType is null");
		}
		return destination;
	}
	
	public static ArrayList<String> getChildCQPageName(ResourceResolverFactory resolverFactory, String rootPath) {
		ArrayList<String> assets = new ArrayList<String>();
		try {
			ResourceResolver resourceResolver = getResourceResolver(resolverFactory, "writeservice");
			String q = "SELECT * FROM [nt:base] AS s WHERE ISCHILDNODE([" + rootPath
					+ "]) and [jcr:primaryType] = 'cq:Page' ";
			Iterator<Resource> batchNodes = resourceResolver.findResources(q, Query.JCR_SQL2);
			while (batchNodes.hasNext()) {
				Resource batchResource = (Resource) batchNodes.next();
				Node node = batchResource.adaptTo(Node.class);
				assets.add(node.getName());
			}
			
		} catch (Exception e) {
			log.debug("Error, could not find: " + rootPath + " : " + e.getMessage());
		}
		return assets;
	}
	
	public static boolean hasValidUPCorSKU(Node assetNode) throws RepositoryException {
		Node metadataNode = assetNode.getNode(CommonConstants.METADATA_NODE);
	        boolean hasValidUPCorSKU = true;
	        if (metadataNode.hasProperty(CommonConstants.BBBY_UPC)) {
	        	String upcValue = (metadataNode.hasProperty(CommonConstants.BBBY_UPC)) ? metadataNode.getProperty(CommonConstants.BBBY_UPC).getString() : null;
	        	if (upcValue != null && !upcValue.equals("")) {
					String[] upcList = upcValue.split(",");
					for (int i = 0; i < upcList.length; i++) {
						if (!StringUtils.isNumeric(upcList[i].trim()) || upcList[i].trim().length() > 15 ) {
							hasValidUPCorSKU = false;
							log.info("Not Valid for PDM Call due to UPC length : "+upcList[i].length());
							
						}
					}
				}
	        }
	        
	        if (metadataNode.hasProperty(CommonConstants.BBBY_SKU)){
	        	String skuValue = (metadataNode.hasProperty(CommonConstants.BBBY_SKU)) ? metadataNode.getProperty(CommonConstants.BBBY_SKU).getString() : null;
				if (skuValue != null && !skuValue.equals("")) {
					String[] skuList = skuValue.split(",");
					for (int i = 0; i < skuList.length; i++) {
						if (!StringUtils.isNumeric(skuList[i].trim()) || skuList[i].trim().length() > 12) {
							hasValidUPCorSKU = false;
							log.info("Not Valid for PDM Call due to SKU length : "+skuList[i].length());
							
						}
					}
				}
	        }
	        return hasValidUPCorSKU;
	    }
	
	//To check weather string is numeric or not
	public static boolean isNumeric(String str) { 
		boolean isValidNum = false;
		if (str == null) {
			return isValidNum; 
		}
		else{
		  try {  
			if(str.contains("d")|| str.contains("f") || str.contains("D") || str.contains("F")){ 
				return isValidNum;
			}
			else{
		    Double.parseDouble(str); 
		    isValidNum = true;
			}
		  } catch(NumberFormatException e){  
		    return isValidNum;  
		  }  
		  return isValidNum;
		}
	}
	
    public static boolean isNullOrEmpty(String str) {
        if(str != null && !str.trim().isEmpty())
            return false;
        return true;
    }
    
    public static double calculatePercentage(double obtained, double total) {
        return obtained * 100 / total;
    }
    
    public static double calculateValueByPercentage(double obtained, double percentage) {
        return obtained * percentage / 100;
    }
    
    public static String replaceLast(String text, String searchString, String replacement) {
        int pos = text.lastIndexOf(searchString); 
        if (pos > -1) { 
           return text.substring(0, pos) 
            + replacement
            + text.substring(pos + searchString.length(), text.length()); 
        } else 
           return text;
    }
    
    // check filename has no special char and space
    public static boolean validFileName(String assetName) {
    	String name = null;
    	if(assetName.contains(".")) {
    		name = assetName.substring(0,assetName.lastIndexOf("."));
    	}
    	else {
    		name = assetName;
    	}
    	Pattern my_pattern = Pattern.compile("[^a-z0-9_-]", Pattern.CASE_INSENSITIVE);
        Matcher my_match = my_pattern.matcher(name);
        boolean check = my_match.find();
        if (check)	{
        	return false;
    	}
        return true;
    }
    
	public static boolean moveImagesetNightly(String value) {
		boolean isImageset = false;
		if ("moveImageset".equalsIgnoreCase(value)) {
			isImageset = true;
		}
		return isImageset;
	}
	
	public static boolean isCollectionResource(ResourceResolverFactory resolverFactory, String resourcePath) {
		boolean isCollectionResource = false;
		try (ResourceResolver resourceResolver = getResourceResolver(resolverFactory, "writeservice")) {
			Resource resource = resourceResolver.getResource(resourcePath);
			if ("dam/collection".equalsIgnoreCase(resource.getResourceType())) {
				isCollectionResource = true;
			}
		} catch (LoginException e) {
			log.error("Exception in ServiceUtils.isCollectionResource method ", e);
		}
		return isCollectionResource;
	}
	
	public static boolean isMarketingUser(ResourceResolverFactory resolverFactory, String userId) {
		boolean isMarketingUser = true;
		//Todo:
	       try (ResourceResolver resourceResolver = getResourceResolver(resolverFactory, "writeservice")) {
	    	   UserManager userManager = resourceResolver.adaptTo(UserManager.class);
	    	   Authorizable auth = userManager.getAuthorizable(userId);
				Iterator<Group> groups = auth.memberOf();
		       while(groups.hasNext())
		       {
		          Group group=(Group)groups.next();
		          log.info("group.getID().."+group.getID());
		          log.info("group.getPrincipal().getName().."+group.getPrincipal().getName());
		        } 
			} catch (Exception e) {
				log.error("Exception in ServiceUtils.isMarketingUser method ", e);
			}
		return isMarketingUser;
	}
	
	public static String buildFilePageName(String filename) {
        String filePageName = filename;
        try {
            String pageNameExt = FilenameUtils.getExtension(filename);
            String pageNameNoExt = FilenameUtils.removeExtension(filename);
            String pageNameNoSpaces = pageNameNoExt.replaceAll("\\s", "");
            String pageNameWithExt = pageNameNoSpaces + pageNameExt;
            filePageName = JcrUtil.createValidName(pageNameWithExt);
        } catch (Exception e) {
            log.error("An exception has occured in buildFilePageName method with error: " + e.getMessage(), e);
        }
        return filePageName;
    }
}
