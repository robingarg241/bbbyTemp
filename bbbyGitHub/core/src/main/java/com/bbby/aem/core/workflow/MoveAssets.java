package com.bbby.aem.core.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.workflow.impl.MoveAssetsConfiguration;
import com.day.cq.commons.Externalizer;


/**
 * Moves the assets from reverse replicated batch to designated DMZ folder.
 *
 * @author vpokotylo
 */

@Component(service = WorkflowProcess.class, property = {
	    "process.label=BBBY Move Assets to DMZ",
	    "Constants.SERVICE_VENDOR=Hero Digital",
	    "Constants.SERVICE_DESCRIPTION=Moves vendor portal assets to DMZ"})
@Designate(ocd = MoveAssetsConfiguration.class)
public class MoveAssets implements WorkflowProcess {
    
	@Reference
    private ResourceResolverFactory resolverFactory;

    private Logger log = LoggerFactory.getLogger(this.getClass());
   
    @Reference
    private JobManager jobManager;
    
    @Reference
    private Externalizer externalizer;

    @Override
    public void execute (WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) {
        
    	try {
                   	
            String assetPath = workItem.getWorkflowData().getPayload().toString();
            
            //String holdingBasePath = metaDataMap.get("PROCESS_ARGS", "/content/dam/bbby/asset_transitions_folder/vendor/vendor_assets_holding");
 
            final Map<String, Object> props = new HashMap<String, Object>();    	
        	        	log.info("Queeing the job for moving " + assetPath);
            
            props.put("assetPath", assetPath);
            
            jobManager.addJob(CommonConstants.ASSET_MOVE_TOPIC, props);

			
        } catch (Exception e) {
            log.error("An exception has occured in moveAsset method with error: " + e.getMessage(), e);
        }
    }

    

}
