/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.bbby.aem.core.listeners;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.DamEvent;
import com.day.cq.dam.commons.util.S7SetHelper;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.Workflow;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.model.WorkflowModel;

/**
 * A service to demonstrate how changes in the resource tree can be listened for. It registers an event handler service.
 * The component is activated immediately after the bundle is started through the immediate flag. Please note, that
 * apart from EventHandler services, the immediate flag should not be set on a service.
 */
@Component(
    immediate = true,
    service = EventHandler.class,
    property = {
        EventConstants.EVENT_TOPIC + "=" + DamEvent.EVENT_TOPIC
    })
public class EcommAssetEventListener implements EventHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String WORKFLOW_MODEL = "/var/workflow/models/bbby-s7-batch-upload";
    private final String DEACTIVATED_WORKFLOW_MODEL = "/var/workflow/models/bbby-deactivated-page-asset";
    
    private final String ECOMM_FOLDER = "/content/dam/bbby/asset_transitions_folder/e-comm";
    private final String RETOUCHER_FOLDER = "retoucher";
    private final String PUBLISH_COMPLETE = "PublishComplete";
    private long timeout = 3000;
    
    @Reference
    private WorkflowService wfService;
    
    @Reference
    private ResourceResolverFactory resolverFactory;
    
	@Reference
	private Replicator replicator;
    
	public void handleEvent(final Event event) {
		log.trace("Resource event: {} at: {}", event.getTopic(), event.getProperty(SlingConstants.PROPERTY_PATH));
		Object type = event.getProperty("type");
		log.info("Asset type is {}", type);

		if (type == DamEvent.Type.ASSET_MOVED) {

			String path = DamEvent.fromEvent(event).getAssetPath();

			log.info("Asset Moved to {}", path);

			String userId = DamEvent.fromEvent(event).getUserId();

			log.info("Asset Moved by {}", userId);

			if (path.startsWith(ECOMM_FOLDER)) {
				
				if (path.contains(RETOUCHER_FOLDER)) {
					try {
						updateReportingMetadata(path, userId);
					} catch (Exception e) {
						log.error("Exception updating the reporting metadata event {}", e.getLocalizedMessage());
					}
				}

				try {
					
					boolean isPublished = isPublished(path);
					if (!isPublished) {
						startWorkflow(path);
					}

				} catch (Exception e) {
					e.printStackTrace();
					log.error("Exception processing Update Asset complete event {}", e.getLocalizedMessage());
				}
			} 

		}
	}
    
    private void startWorkflow(String payload) throws RepositoryException, LoginException, WorkflowException {

        
        try(ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "workflow-service")) {
        	
        	Resource image1r = resourceResolver.resolve(payload);
			if (S7SetHelper.isS7Set(image1r)) {
				log.info("Scene7 workflow is not needed, it is Imageset with path {}", payload);
				return;
			}

			Node node = image1r.adaptTo(Node.class);
			if(!node.hasNode(CommonConstants.METADATA_NODE)){
				return;
			}
			Node meta = node.getNode(CommonConstants.METADATA_NODE);
			if (meta.hasProperty(CommonConstants.DAM_SCENE_7_FILENAME)) {
				log.info("Already sync with Scene7 : path {}", payload);
				return;
			}

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
    
    private void updateReportingMetadata(String payload, String user) throws RepositoryException, LoginException {

        try(ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "workflow-service")) {
        	Session session = resourceResolver.adaptTo(Session.class);
        	
        	Resource image1r = resourceResolver.resolve(payload);
        	Node node1 = image1r.adaptTo(Node.class);
        	NodeType nodeType = node1.getPrimaryNodeType();
        	if(nodeType.getName().equalsIgnoreCase("sling:Folder")){
        		Thread.sleep(this.timeout);
        		String q = "SELECT * FROM [dam:Asset] AS s WHERE ISDESCENDANTNODE([" + payload + "])";
        		int index = 0;
    			Iterator<Resource> result = resourceResolver.findResources(q, Query.JCR_SQL2);
    			while(result.hasNext()){
    				Resource resource = result.next();
    				Node n = resource.adaptTo(Node.class);
                    //DAM-320 : populated reporting metadata attribute "Last Modified By User" for folders.
                    Node repometa = JcrPropertiesUtil.getReportingNode(n, session);
      				if (repometa != null) {
      					JcrUtil.setProperty(repometa, CommonConstants.LAST_MODIFIED_BY_USER, user);
      					log.info("Updating the LAST_MODIFIED_BY_USER of path {}", n.getPath());
      				}
      				index++;
    			}
    			log.info("Folder size of {} is {}",node1.getPath(), index);
               /* Query myQuery;
                myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
                QueryResult result = myQuery.execute();
                NodeIterator ni = result.getNodes();
                log.info("Folder size is {}", ni.getSize());
                while (ni.hasNext()) {
                    Node n = (Node) ni.next();
                  //DAM-320 : populated reporting metadata attribute "Last Modified By User" for folders.
                    Node repometa = JcrPropertiesUtil.getReportingNode(n, session);
    				if (repometa != null) {
    					JcrUtil.setProperty(repometa, CommonConstants.LAST_MODIFIED_BY_USER, user);
    				}
                }*/
        		
        	} else{
        	
	        	Node node = session.getNode(payload);
	        	
	        	//DAM-320 : populated reporting metadata attribute "Last Modified By User" for assets.
	            Node repometa = JcrPropertiesUtil.getReportingNode(node, session);
				if (repometa != null) {
					JcrUtil.setProperty(repometa, CommonConstants.LAST_MODIFIED_BY_USER, user);
				}
        	}
        	session.save();
        	resourceResolver.commit();
        } catch (Exception e) {
            log.error("Failed to start workflow", e);
        }
    }
    
	private boolean unPublishAsset(Session session, String destination) throws WorkflowException {
		log.info("entering unPublishAsset() method");
		boolean successful = true;
		try {
			log.info("Unpublish Asset : " + destination);
			replicator.replicate(session, ReplicationActionType.DEACTIVATE, destination);
			log.info("Successfully Unpublish asset : " + destination);
		} catch (ReplicationException e) {
			log.error("Unable to Unpublish the asset: " + e);
			successful = false;
		}
		return successful;
	}
	
	private boolean isPublished(String payload) {
		boolean isPublished = false;
		try (ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "workflow-service")) {
			Session session = resourceResolver.adaptTo(Session.class);
			Resource image1r = resourceResolver.resolve(payload);
			Node node = image1r.adaptTo(Node.class);
			if (!node.hasNode(CommonConstants.METADATA_NODE)) {
				return isPublished;
			}

			Node meta = node.getNode(CommonConstants.METADATA_NODE);
			if (meta.hasProperty(CommonConstants.DAM_SCENE_7_FILE_STATUS) && PUBLISH_COMPLETE.equalsIgnoreCase(
					meta.getProperty(CommonConstants.DAM_SCENE_7_FILE_STATUS).getValue().getString())) {
				log.info("Already published : path {}", payload);
				isPublished = true;
			}
			
			if (isPublished) {
				//unPublishAsset(session, payload);
	            WorkflowSession wfSession = wfService.getWorkflowSession(session);

	            WorkflowModel model = wfSession.getModel(DEACTIVATED_WORKFLOW_MODEL);
	            WorkflowData data = wfSession.newWorkflowData(CommonConstants.JCR_PATH, payload);
	            Workflow workflow = wfSession.startWorkflow(model, data);
	            
	            if(workflow != null) {
	                log.info("Successfully started unpublish workflow {}", workflow.getId());
	            }
			}
		} catch (Exception e) {
			log.error("Failed to check isPublished", e);
		}
		return isPublished;
	}
}