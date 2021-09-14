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
 * Create fast track Image Sets and add fast track assets to it.
 * @author rgarg
 *
 */
@Component(
    service = Runnable.class
)
@Designate(ocd = FastTrackImagesetCreationTaskConfiguration.class)
public class FastTrackImagesetCreationTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final String WORKFLOW_MODEL = "/var/workflow/models/bbby-fast-track-";
    
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
    protected void activate(FastTrackImagesetCreationTaskConfiguration config) throws LoginException {

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
        	
			createFastTrackImagesets();
			
		} catch (WCMException e) {
			e.printStackTrace();
			log.error("Error in creating fast track imagesets.", e.getMessage());
		} finally{
			if(resourceResolver != null)
				resourceResolver.close();
		}
    }
    
	private void createFastTrackImagesets() throws WCMException {
		log.debug("Inside createFastTrackImagesets() method.");

		String queryString = "SELECT * FROM [sling:Folder] AS N WHERE ISDESCENDANTNODE(N,\"/content/dam/bbby/asset_transitions_folder/e-comm/fasttrack/nonshared\") ";

		log.debug("Executing query {}", queryString);
		
		Iterator<Resource> batchDirectories = resourceResolver.findResources(queryString, Query.JCR_SQL2);
		
		List<String> batchDirectoriesList = new ArrayList<String>();
		
		while (batchDirectories.hasNext()) {
			Resource batchResource = (Resource) batchDirectories.next();
			Node directory = batchResource.adaptTo(Node.class);
			log.debug("Adding Directory to the list {}", directory);
			try {
				batchDirectoriesList.add(directory.getPath());
			} catch (Exception e) {
				log.error("Exception in adding directory for fast track assets {}", e.getLocalizedMessage());
			}
		}
		
		for (int i = 0; i < batchDirectoriesList.size(); i++){
			try{
				startWorkflow(batchDirectoriesList.get(i), WORKFLOW_MODEL);
			} catch (Exception e) {
				log.error("Exception processing fast track imagesets {}", e.getLocalizedMessage());
			}
		}
	}
	
	private void startWorkflow(String payload, String WORKFLOW_MODEL) throws RepositoryException, LoginException, WorkflowException {

        try(ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "workflow-service")) {

        	Session session = resourceResolver.adaptTo(Session.class);
            WorkflowSession wfSession = wfService.getWorkflowSession(session);
            WorkflowModel model = wfSession.getModel(WORKFLOW_MODEL);
            WorkflowData data = wfSession.newWorkflowData(CommonConstants.JCR_PATH, payload);
            Workflow workflow = wfSession.startWorkflow(model, data);
            if(workflow != null) {
                log.info("Successfully started workflow {}", workflow.getId());
            }
        } catch (Exception e) {
            log.error("Failed to start workflow", e);
            throw new WorkflowException(e.getMessage(), e);
        }
    }

}
