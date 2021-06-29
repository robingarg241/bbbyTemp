package com.bbby.aem.core.filters;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.util.CommonConstants;
import com.day.cq.dam.api.DamConstants;
 
@Component(
        immediate = true,
        service = Filter.class,
        name = "Experience AEM Request Name Change Filter for CreateAssetServlet Streaming Requests",
        property = {
                Constants.SERVICE_RANKING + ":Integer=-99",
                "sling.filter.scope=REQUEST"
        })

public class BBBYChangeFileNameFilter implements Filter {
	 static String assetPath = null;
	 static String OrgFileName = "";
	 static ResourceResolver resourceResolver;
	 static Session session;
	  static HashMap<String, String> hmap;
     private static Logger log = LoggerFactory.getLogger(BBBYChangeFileNameFilter.class);
 
    public void init(FilterConfig filterConfig) throws ServletException {
    	
    }
 
    @SuppressWarnings("unchecked")
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
    	Instant start = Instant.now();
        if (!(request instanceof SlingHttpServletRequest) || !(response instanceof SlingHttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
 
        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;     
        if (!StringUtils.equals("POST", slingRequest.getMethod()) || !isCreateAssetRequest(slingRequest) ) {
            chain.doFilter(request, response);
            return;
        }
         hmap = new HashMap<String, String>();  
         assetPath = slingRequest.getRequestPathInfo().getResourcePath();
         resourceResolver = slingRequest.getResourceResolver();
         session = resourceResolver.adaptTo(Session.class);
                 
        if(assetPath.contains("/dam/marketing")){  
        boolean status = checkFilterEnableDisable("/content/dam/marketing");
        log.info("Filter is Enabled.. "+status);
        if(!status){
          	 chain.doFilter(request, response);
             return;	
        }
   	    
        Iterator parts = (Iterator)request.getAttribute("request-parts-iterator");
        if( (parts == null) || !parts.hasNext()){
            chain.doFilter(request, response);
            return;
        }
 
        List<Part> otherParts = new ArrayList<Part>();
        Part part = null;
        while(parts.hasNext()) {
            part = (Part) parts.next();
            otherParts.add(new BBBYFileNameRequestPart(part));
        }
        request.setAttribute("request-parts-iterator", otherParts.iterator());
        request.setAttribute("hmap-tag-orgname", hmap);
        chain.doFilter(request, response);
        hmap = (HashMap<String, String>) request.getAttribute("hmap-tag-orgname");
        setOriginalNametoAsset(hmap);
        }
         else{
        	  chain.doFilter(request, response);
              return;
         }
        Instant end = Instant.now();
        log.info("Time taken for one request...."+Duration.between(start, end).toMillis());
       
    }
    
    private boolean checkFilterEnableDisable(String path){
    	boolean status = false;
      try  {
	       Resource assetResource = resourceResolver.getResource(path);
           Node assetNode = assetResource.adaptTo(Node.class);
           if(assetNode.hasNode(CommonConstants.METADATA_NODE)){
           Node metadataNode = assetNode.getNode(CommonConstants.METADATA_NODE);
           if(metadataNode.hasProperty("namefilter")){
           String val = metadataNode.getProperty("namefilter").getString();
           if(val.equalsIgnoreCase("Enable")){
        	   status = true;
           }
           }
            }
	}
         catch (Exception e) {
        	log.error("Failed to check the status", e);
         }
         return status;
    } 
    
    private void setOriginalNametoAsset(HashMap<String, String> map){
    	log.info("Execution of setOriginalNametoAsset() Method");
    	for (Entry<String, String> set : map.entrySet()) {
		   String path = set.getKey();
		    String nameval = set.getValue();
			try  {
            Resource assetResource = resourceResolver.getResource(path);
            Node assetNode = assetResource.adaptTo(Node.class);
            if(assetNode.hasNode(CommonConstants.JCR_CONTENT_NODE)){
          //  Node jcrNode = assetNode.getNode(CommonConstants.JCR_CONTENT_NODE);
          //  jcrNode.setProperty("jcr:title", nameval);
            
            if(assetNode.hasNode(CommonConstants.METADATA_NODE)){
                Node metadataNode = assetNode.getNode(CommonConstants.METADATA_NODE);
                log.info("Path for Metadata Node : " + metadataNode.getPath());
                metadataNode.setProperty(CommonConstants.BBBY_UPLOADED_ASSET_NAME, nameval);
                metadataNode.setProperty(DamConstants.DC_TITLE, nameval);
                log.info("Original Asset Name added");
              //For marketing assets, setting default value of asset type as marketing.
                    Set<String> tags = new HashSet<String>();
                    tags.add("marketing:asset_type/marketing");
                    metadataNode.setProperty(CommonConstants.CQ_TAGS, tags.toArray(new String[0]));
                    log.info("Marketing Tag added");		 
            	 }
            }
                session.save();
        } 
			catch (Exception e) {
        	log.error("Failed to save original name to asset", e);
        }
    	}
    }
 
    private boolean isCreateAssetRequest(SlingHttpServletRequest slingRequest){
        String[] selectors = slingRequest.getRequestPathInfo().getSelectors();
        if(ArrayUtils.isEmpty(selectors) || (selectors.length > 1)){
            return false;
        }
        return selectors[0].equals("createasset");
    }
 
    public void destroy() {
    }
 
    private static class BBBYFileNameRequestPart implements Part {
        private final Part part;
        private final InputStream inputStream;
 
        public BBBYFileNameRequestPart(Part part) throws IOException {
            this.part = part;
 
            if(!isFileNamePart(part)){
                this.inputStream = new ByteArrayInputStream(IOUtils.toByteArray(part.getInputStream()));
            }else{
                this.inputStream = this.getFileNameAdjustedStream(part);
            }
        }
 
        private InputStream getFileNameAdjustedStream(Part part) throws IOException{
        	log.info("in getFileNameAdjustedStream Method");
            String fileName = null;
            String Ext =""; 
            try{
            fileName = IOUtils.toString(part.getInputStream(), "UTF-8");
            OrgFileName = fileName; 
           
            if(fileName == null){
                fileName = "";
            }
            	Ext = fileName.substring(fileName.lastIndexOf(".")).trim();
            	fileName = fileName.substring(0,fileName.lastIndexOf(".")).replaceAll("[^a-zA-Z0-9_-]", "_")+Ext;
            	log.info("Uploaded file name changed to : " + fileName);
                boolean duplicate = false;   
                duplicate = checkForDupsForMarketing(fileName,assetPath);  	
			    if (duplicate) {
            		 fileName = ZonedDateTime.now().toString().replaceAll("[^0-9-]", "-").substring(0,19)+"-"+fileName.substring(0,fileName.lastIndexOf("."))+Ext;
            		 log.info("New Name with TimeStamp..."+fileName);
            	 }
            	 assetPath = assetPath+"/"+fileName;
            	 log.info("Asset Path : " + assetPath);
            	 log.info("Orginal File Name : " + OrgFileName);
                 hmap.put(assetPath, OrgFileName);
            }
            catch(Exception e){
            	log.error("Failed to change name..."+e);            	
            }
            
            return new ByteArrayInputStream(fileName.getBytes());
        }
                
     private boolean checkForDupsForMarketing(String fileName, String path) throws Exception {
        	boolean dupCheck = false;
  
        	//We need to query the dam to see if any other asset has this filename. if it does we move the current asset to the dups folder
        	String q = "SELECT * FROM [dam:Asset] AS s WHERE ISDESCENDANTNODE([/content/dam]) " +
        			   "and NAME() = \"" + fileName + "\"";
        	Iterator<Resource> batchNodes = resourceResolver.findResources(q, Query.JCR_SQL2);
    			while (batchNodes.hasNext()) {
    				dupCheck = true;
    				Resource batchResource = (Resource) batchNodes.next();
    				Node node = batchResource.adaptTo(Node.class);
    				if(node.getParent().getPath().equals(path)){
    					dupCheck = false;
    					log.info("Duplicate Asset exists on same path...");
    					break;
    				}
    			}
            return dupCheck;
        }

        private boolean isFileNamePart(Part part){
            return ("fileName".equals(part.getName()));
        }
 
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }
 
        public  String getContentType() {
            return part.getContentType();
        }
 
        public String getName() {
            return part.getName();
        }
 
        public long getSize() {
            return 0;
        }
 
        public  void write(String s) throws IOException {
            throw new UnsupportedOperationException("Writing parts directly to disk is not supported by this implementation, use getInputStream instead");
        }
 
        public  void delete() throws IOException {
        }
 
        public  String getHeader(String headerName) {
            return part.getHeader(headerName);
        } 
 
        public  Collection<String> getHeaders(String headerName) {
            return part.getHeaders(headerName);
        }
 
        public  Collection<String> getHeaderNames() {
            return part.getHeaderNames();
        }
 
       public String getSubmittedFileName() {
            return part.getSubmittedFileName();
        }
    }
}