package com.bbby.aem.core.services;


import java.io.InputStream;

import javax.jcr.Session;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.bbby.aem.core.models.data.PdmAsset;
import com.bbby.aem.core.models.data.UploadRequest;
import com.day.cq.replication.ReplicationException;

/**
 * Asset Service.
 * 
 * @author vpokotylo
 */
public interface AssetService {

    /**
     * Saves assets.
     *
     * @param request  the request
     * @param response the response
     * @param bean     the bean
     * @param today    the today
     */
    void saveAssets(SlingHttpServletRequest request, SlingHttpServletResponse response, ResourceResolver resolver, UploadRequest bean, String groupName,
                    String userName, String currentDate, String wrapperPath, InputStream assetInputStream);

    
    /**
     * @param session
     * @param path
     * @throws ReplicationException
     */
    public void reverseReplicate(Session session, String path) throws ReplicationException;
    
    /**
     * Updates DAM Asset UPC metadata based on the data passed from PDM
     * 
     * @param pdmAsset  PDM asset metadata
     * @throws PersistenceException
     */
    void updateAssetMetadata(PdmAsset pdmAsset, ResourceResolver resourceResolver) throws PersistenceException;

	/**
	 * @param session
	 * @param path
	 * @throws ReplicationException
	 */
	void replicate(Session session, String path) throws ReplicationException;

}
