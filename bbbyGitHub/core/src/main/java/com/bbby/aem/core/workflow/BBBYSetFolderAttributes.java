package com.bbby.aem.core.workflow;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Date;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;

import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.workflow.impl.BBBYAssetHasherConfiguration;
import com.bbby.aem.core.workflow.impl.BBBYAssetSorterConfiguration;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;    

import com.day.cq.dam.commons.util.S7SetHelper;
import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.model.WorkflowModel;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.s7dam.set.ImageSet;

import java.util.HashMap;

import com.day.cq.dam.api.Asset;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;

/**
 * @author ANT
 * 07/05/19
 * 
 * This code is used to sort assets to specific sub-directories (node) based on information received from PDM
 *  
 * 
 */

@Component(service = WorkflowProcess.class, property = {
    "process.label=BBBY Set Folder Attributes",
    "Constants.SERVICE_VENDOR=Hero Digital",
    "Constants.SERVICE_DESCRIPTION=BBBY adds autotag and image preset to newly created folders"})
@Designate(ocd = BBBYAssetHasherConfiguration.class)

public class BBBYSetFolderAttributes implements WorkflowProcess {
    
    @Reference
    private WorkflowService workflowService;
	
    @Reference
    private ResourceResolverFactory resolverFactory;
    private BBBYAssetHasherConfiguration config;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    // Static list of tag domains
    final List<String> tagDomains = Arrays.asList(
        "bbby:"
    );
    
    final String productImageTag = "bbby:asset_type/approved_dam/assets/product/image";
    final String collateralTag = "bbby:asset_type/approved_dam/assets/product/collateral";
    
    final String projectCleanupWorkflow = "/var/workflow/models/project_cleanup0";

    private int timeout;
    
    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
    	
        WorkflowData workflowData = workItem.getWorkflowData();
        String type = workflowData.getPayloadType();
        

        if (CommonConstants.JCR_PATH.equalsIgnoreCase(type)) {
            String path = workflowData.getPayload().toString().replaceAll("/jcr:content.*", "");
            
            //nip this in the bud we are only checking for duplicates for assets in these two folders
            Session session = workflowSession.adaptTo(Session.class);
            ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
            
            try {
            	
            	Resource r = resourceResolver.getResource(path);
            	
            	if(r != null) {

            		Node node = r.adaptTo(Node.class);
            		
            		Resource contentResource = r.getChild(JcrConstants.JCR_CONTENT);

            		Node content = null;
            		
            		if(contentResource == null) {
            			
            			content = node.addNode("./jcr:content", "nt:unstructured");
            			
            		} else {
            			
                		content = contentResource.adaptTo(Node.class);
            			
            		}
            		
                	
                	content.setProperty("autotag", true);
                	content.setProperty("imageProfile", "/conf/global/settings/dam/adminui-extension/imageprofile/standard image smart crop");
                    content.setProperty("videoProfile", "/libs/settings/dam/dm/presets/video/Adaptive_Video_Encoding");
                    
                	
                	session.save();
            	}
            	
            	
            } catch (Exception e) {
            	
            	log.error("Could not set attributes on folder {}", path, e);
            	
            }
            
        }    
    
    }
 
}
