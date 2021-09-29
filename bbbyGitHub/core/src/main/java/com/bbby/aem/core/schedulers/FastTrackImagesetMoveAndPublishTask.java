package com.bbby.aem.core.schedulers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.Workflow;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.model.WorkflowModel;
import com.google.gson.annotations.Expose;

/**
 * Move Fast Track Image Sets along with assets in Image Sets from fast track folder in ecomm to approved dam and publish them.
 * @author rgarg
 *
 */
@Component(
    service = Runnable.class
)
@Designate(ocd = FastTrackImagesetMoveAndPublishTaskConfiguration.class)
public class FastTrackImagesetMoveAndPublishTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final String WORKFLOW_MODEL = "/var/workflow/models/fasttrack-move-and-publish";
    
    @Expose
    private boolean enabled;

    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Reference
	private JobManager jobManager;
    
    @Reference
    private SlingSettingsService slingSettingsService;
    
    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    Externalizer externalizer;
    
    private ResourceResolver resourceResolver;
    
    @Reference
    private WorkflowService wfService;
 
    @Activate
    protected void activate(FastTrackImagesetMoveAndPublishTaskConfiguration config) throws LoginException {

        this.enabled = config.enabled();
        
        resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice");
    }

    
    @Override
    public void run() {
        
        if (!slingSettingsService.getRunModes().contains("author")) {
            log.warn("Attempt to run from non-author environment");
            return;
        }
        
        if(!enabled) {
            return;
        }
        
        try {
        	
			moveFastTrackImagesetsApprovedDam();
			
		} catch (WCMException e) {
			e.printStackTrace();
			log.error("Error in moving Fast Track Imagesets from ecomm to approved dam", e.getMessage());
		} finally{
			if(resourceResolver != null)
				resourceResolver.close();
		}
    }
    
	private void moveFastTrackImagesetsApprovedDam() throws WCMException {
		log.debug("Inside moveFastTrackImagesetsApprovedDam() method.");

		String queryString = "SELECT * FROM [dam:Asset] AS N WHERE ISDESCENDANTNODE(N,\"/content/dam/bbby/asset_transitions_folder/e-comm/fasttrack\") AND NAME() LIKE '%_imageset' ";

		log.debug("Executing query {}", queryString);
		
		Iterator<Resource> batchNodes = resourceResolver.findResources(queryString, Query.JCR_SQL2);
		
		List<String> batchNodesList = new ArrayList<String>();
		
		Session session = resourceResolver.adaptTo(Session.class);
		int sizeMembers = 0;
		
		while (batchNodes.hasNext()) {
			Resource batchResource = (Resource) batchNodes.next();
			Node node = batchResource.adaptTo(Node.class);
			log.debug("Adding Fast Track Imageset to the list {}", node);
			try {
				batchNodesList.add(node.getPath());
				ArrayList<String> membersImageset = ServiceUtils.getMembersOfImageset(session,node.getPath());
				sizeMembers = sizeMembers + membersImageset.size();
				log.debug("Member size: {}", sizeMembers);
			} catch (Exception e) {
				log.error("Exception in fetching fast track imageset {}", e.getLocalizedMessage());
			}
		}
		
		for (int i = 0; i < batchNodesList.size(); i++){
			try{
				startWorkflow(batchNodesList.get(i), WORKFLOW_MODEL, "images");
			} catch (Exception e) {
				log.error("Exception processing fast track workflow for images in imageset {}", e.getLocalizedMessage());
			}
		}
		
		try{
			Thread.sleep(10 * 60 * 1000); // timeCounter min pause
		}catch (Exception e){
			log.error("Exception in thread", e.getLocalizedMessage());
		}
		
		for (int i = 0; i < batchNodesList.size(); i++){
			try{
				startWorkflow(batchNodesList.get(i), WORKFLOW_MODEL, "imagesets");
			} catch (Exception e) {
				log.error("Exception processing fast track workflow for imageset {}", e.getLocalizedMessage());
			}
		}
		
	}
	
	private void startWorkflow(String payload, String WORKFLOW_MODEL, String value) throws RepositoryException, LoginException, WorkflowException {

        try(ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "workflow-service")) {

        	Session session = resourceResolver.adaptTo(Session.class);
            WorkflowSession wfSession = wfService.getWorkflowSession(session);

            WorkflowModel model = wfSession.getModel(WORKFLOW_MODEL);
            WorkflowData data = wfSession.newWorkflowData(CommonConstants.JCR_PATH, payload);
            String moveImages = "";
            if(value.equals("images")){
            	moveImages = "moveImages";
            	data.getMetaDataMap().put("moveImages", moveImages);
            } else {
            	moveImages = "moveImageset";
            	data.getMetaDataMap().put("moveImages", moveImages);
            }
            Workflow workflow = wfSession.startWorkflow(model, data);
            
            if(workflow != null) {
                log.info("Successfully started fast track workflow {}", workflow.getId());
            }
        } catch (Exception e) {
            log.error("Failed to start fast track workflow", e);
            throw new WorkflowException(e.getMessage(), e);
            
        }
    }
	
}
