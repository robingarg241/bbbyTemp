package com.bbby.aem.core.workflow.publish;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.ResourceResolver;
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
import com.bbby.aem.core.api.PDMAPICommand;
import com.bbby.aem.core.api.PDMClient;
import com.bbby.aem.core.api.commands.PublishAssetCommand;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.workflow.impl.BBBYCleanupConfiguration;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.replication.Replicator;    

/**
 * @author Sandeep
 *
 */
@Component(service = WorkflowProcess.class, property = {
    "process.label=Publish call to PDM",
    "Constants.SERVICE_VENDOR=BBBY",
    "Constants.SERVICE_DESCRIPTION=Cleanup project assets, tag and move to general population"})
@Designate(ocd = BBBYCleanupConfiguration.class)


public class CallToPDMStep implements WorkflowProcess {

	 @Reference
	    private Replicator replicator;
	 
	 @Reference
	    private PDMClient pdmClient;
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private String initiator = null;

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
        
        log.info("entering execute of call to pdm step");
        if(workflowData.getMetaDataMap().get("validstatus").toString().equalsIgnoreCase("false")){
        	 log.info("can't execute this step as validations are not successful.");
        }
        
        else{
        	String path = workflowData.getPayload().toString();
        	if (workflowData.getMetaDataMap().containsKey("userId")) {
				initiator = workflowData.getMetaDataMap().get("userId", String.class);
				log.info("initiator by userId : "+initiator);
			} else {
				initiator = workItem.getWorkflow().getInitiator();
				log.info("initiator by getInitiator() method : "+initiator);
			}
            Session session = null;
            
            try {
                session = workflowSession.adaptTo(Session.class);	
						ArrayList<String> destpath = new ArrayList<String>();
						List<Node> assets = new ArrayList<Node>();	
					    assets = (List<Node>) workflowData.getMetaDataMap().get("assetkey");
					    
						destpath = (ArrayList<String>) workflowData.getMetaDataMap().get("destkey");
							  for (int counter = 0; counter < destpath.size(); counter++) { 	
							  makePDMCall(session,destpath.get(counter),assets.get(counter));
					       
					      }   
            } catch (Exception e) {
                log.error("Unable to complete processing of Call To PDM Process step, for path: " + path, e);
             // throw new WorkflowException(e.getMessage(), e);
                
            }
        
        }
    }
    
    private void makePDMCall(Session session, String destination, Node asset) throws WorkflowException{
    		int resCode = -1;
 
		try {
			
			Node repometa = JcrPropertiesUtil.getReportingNode(asset, session);
			if (repometa != null) {
				JcrUtil.setProperty(repometa, CommonConstants.PUBLISHED_TO_S7_BY_USER, initiator);
			}else{
				log.debug("Unable to set reporting attributes as repo node is null");
			}
			
			 Node movedAsset = session.getNode(destination);
			PDMAPICommand publishCommand = new PublishAssetCommand(PDMAPICommand.METHOD_POST, movedAsset);
	          resCode = pdmClient.execute(publishCommand);
	          
	          if(resCode == HttpStatus.SC_OK){
	          	log.info("Setting operational attributes as response code is 200");
	          	Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
	          	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PDM_CALL_SENT, ServiceUtils.getCurrentDateStr(CommonConstants.DATE_FORMAT));
	          	long count = opmeta.hasProperty(CommonConstants.OPMETA_PDM_CALL_COUNT)? opmeta.getProperty(CommonConstants.OPMETA_PDM_CALL_COUNT).getValue().getLong() : 0;
	          	JcrUtil.setProperty(opmeta, CommonConstants.OPMETA_PDM_CALL_COUNT, count+1);
	        	JcrUtil.setProperty(opmeta, "pwfpdmCallStatus", "Success");
	          }else{
	          	log.info("Unable to set operational attributes as response code is not 200");
	        	Node opmeta = JcrPropertiesUtil.getOperationalNode(asset, session);
	        	JcrUtil.setProperty(opmeta, "pwfpdmCallStatus", "PDM Call Failed");
	          	// set operational attribute as failed. 
	          	//Create new opertional attribute pdm call in publish wf. success value and failed value with error
	          }
		} catch (Exception e) {
			log.error("PDM Call failed"+e);
		//	throw new WorkflowException(e.getMessage(), e);
	
		}
          
     
    }
}
