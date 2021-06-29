package com.bbby.aem.core.servlets;

import com.bbby.aem.core.models.data.AssetMetadata;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;
import java.rmi.ServerException;
import java.util.HashMap;
import java.util.Map;

@Component(
    service = Servlet.class,
    immediate = true,
    name = "Retrieve Asset Metadata Servlet",
    property = {
        ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/bedbath/assets/metadata",
        ServletResolverConstants.SLING_SERVLET_METHODS + "=GET"
    })
public class AssetMetadataServlet extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = 2598426539166789512L;
	
	private static final Gson GSON = new GsonBuilder().create();
	 
	private static final String SERVICE_ACCOUNT_IDENTIFIER = "pdm-content-service";
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetMetadataServlet.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServerException, IOException {

        String uuid = request.getParameter("id");

        try {
            ResourceResolver resourceResolver = getResourceResolver();

            Asset asset = DamUtil.getAssetFromID(resourceResolver,uuid);
            
            AssetMetadata assetMetadata = new AssetMetadata(asset, resourceResolver);

            response.getWriter().write(assetMetadata.toJson());

        } catch (RepositoryException e) {
            LOGGER.error("Error trying to parse asset metadata to json",e);
        } catch (Exception e) {
            LOGGER.error("Error in AssetMetadata class ",e);
        }
    }



    private ResourceResolver getResourceResolver() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(ResourceResolverFactory.SUBSERVICE, SERVICE_ACCOUNT_IDENTIFIER);
        ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(params);
        return resourceResolver;
    }

}


