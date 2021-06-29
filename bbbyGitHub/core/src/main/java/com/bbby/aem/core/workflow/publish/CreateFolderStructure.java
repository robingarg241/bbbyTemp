package com.bbby.aem.core.workflow.publish;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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
import com.bbby.aem.core.util.PartitionUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.workflow.impl.BBBYCleanupConfiguration;    

/**
 * @author Sandeep
 *
 */
@Component(service = WorkflowProcess.class, property = {
    "process.label=Publish Create Folder Structure",
    "Constants.SERVICE_VENDOR=BBBY",
    "Constants.SERVICE_DESCRIPTION=Cleanup project assets, tag and move to general population"})
@Designate(ocd = BBBYCleanupConfiguration.class)


public class CreateFolderStructure implements WorkflowProcess {


    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private long batchSize = 10;

    private long timeout = 500;
    
    
    private ResourceResolver resourceResolver = null;
    
    @Activate
    public synchronized void activate(BBBYCleanupConfiguration config) {
        
        this.batchSize=config.batchSize();
        this.timeout=config.timeout();
        
    }

    @SuppressWarnings("unchecked")
	@Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        WorkflowData workflowData = workItem.getWorkflowData();
        workflowData.getPayloadType();
        resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
    
        log.info("entering execute of folder creation");
        if(workflowData.getMetaDataMap().get("validstatus").toString().equalsIgnoreCase("false")){
        	 log.info("can't execute this step as validations are not successful.");
        }
        
        else{
        	String path = workflowData.getPayload().toString();
            Session session = null;
            
            try {
                session = workflowSession.adaptTo(Session.class);
                List<Node> assets = new ArrayList<Node>();		
				assets = (List<Node>) workflowData.getMetaDataMap().get("assetkey");		
				createDestFolder(session, assets);		

            } catch (Exception e) {
                log.error("Unable to complete processing of Create Folder Process step, for path: " + path, e);
              //  throw new WorkflowException(e.getMessage(), e);
            }
        
        }
    }
    
 
	
    private void createDestFolder(Session session,List<Node> assets ) throws Exception {
    	
     for (Node asset : assets) {
     String destination = "/content/dam/bbby/"
    					+ ServiceUtils.getAssetType(asset.getNode(CommonConstants.METADATA_NODE)).substring(CommonConstants.BBBY_ASSET_TYPE.length() + 1); 
     ArrayList<String> partitions = PartitionUtil.hashFileName(asset.getName());
     //create the holding partitions if they don't exist
     destination = destination + "/" + partitions.get(0);
     destination = destination + "/" + partitions.get(1);
     JcrUtils.getOrCreateByPath(destination, "sling:Folder", session);	
    }	
 	log.info("destination folder created...");
 }
		
}
