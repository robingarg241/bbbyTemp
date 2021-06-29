package com.bbby.aem.core.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.models.data.PDMUpdateRequest;
import com.bbby.aem.core.models.data.PdmAsset;
import com.bbby.aem.core.services.AssetService;
import com.bbby.aem.core.util.CommonConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


@Component(
    service = Servlet.class,
    immediate = true,
    name = "Update Asset Metadata Servlet",
    property = {
        ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/bedbath/assets/update",
        ServletResolverConstants.SLING_SERVLET_METHODS + "=GET,POST"
    })
public class AssetUpdateServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 2598426539166789512L;
	
	public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	
//	private static final String SERVICE_ACCOUNT_IDENTIFIER = "pdm-content-service";
	private final Logger log = LoggerFactory.getLogger(AssetUpdateServlet.class);

    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Reference
    private AssetService assetService;
    
    @Reference
    private JobManager jobManager;
    
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServerException,
        IOException {
        
        doPost(request, response);
    }


    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        try {
            //ResourceResolver resourceResolver = request.getResourceResolver();
            
        	JsonObject responseJson = new JsonObject();
        	PDMUpdateRequest updateRequest;
            
            String json = this.extractPayloadJson(request);
            
            log.debug("raw request " + json);
            
            if(StringUtils.isEmpty(json)) {
            	log.warn("JSOn payload is empty");
            	response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            	return;
            }
           
            updateRequest = GSON.fromJson(json, PDMUpdateRequest.class);
 
            log.debug("parsed request " + updateRequest);
            
            // Queue update requests
            updateRequest.getAssets().stream().forEach(x -> startAssetUpdateJobPerAsset(x));
            //startAssetUpdateJob(updateRequest.getAssets());
           // startAssetUpdateJobPerAsset(updateRequest.getAssets());
            response.setContentType("application/json");
            response.setStatus(SlingHttpServletResponse.SC_ACCEPTED);
            
            responseJson.addProperty("response", "request accepted");
            responseJson.addProperty("total", updateRequest.getAssets().size());
            
            response.getWriter().write(responseJson.toString());
        } catch (Exception e) {
            log.error("Error in creating Asset ::" + e.getMessage(), e);
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param request
     * @return
     * @throws IOException
     */
    private String extractPayloadJson(SlingHttpServletRequest request) throws IOException {
    	
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }
    
    /**
     * Queues a job to download product asset. 
     * 
     * @param productId
     * @param assets
     * @param imageNode
     * @param ctx
     * @throws RepositoryException
     */
    private void startAssetUpdateJob(List<PdmAsset> assets) {

        final Map<String, Object> props = new HashMap<String, Object>();

        log.info("Queeing the job for assets list");
        //props.put("batchId", productId);

        String assetsJson = GSON.toJson(assets);
        props.put("assetsMetadata", assetsJson);
        
        jobManager.addJob(CommonConstants.ASSET_UPDATE_TOPIC, props);
    }
    
	/**
	 * Queues a job to Update product asset PDM Metadata.
	 * 
	 * @param pdmAsset
	 */
	private void startAssetUpdateJobPerAsset(PdmAsset pdmAsset) {
		List<PdmAsset> assets = new ArrayList<PdmAsset>();
		assets.add(pdmAsset);
		
		String assetsJson = GSON.toJson(assets);
		
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put("assetsMetadata", assetsJson);
		
		log.info("Queeing the job for asset with UUID : " + pdmAsset.getUuid());
		jobManager.addJob(CommonConstants.ASSET_UPDATE_TOPIC, props);
	}
}


