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

import com.bbby.aem.core.api.PDMAPICommand;
import com.bbby.aem.core.api.commands.UpdateAssetCommand;
import com.bbby.aem.core.models.data.AssetHash;
import com.bbby.aem.core.services.AssetHasherService;
import com.bbby.aem.core.services.CallPDMService;
import com.bbby.aem.core.util.AssetHasherUtils;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.dam.api.DamEvent;
import com.day.cq.workflow.WorkflowService;
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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * A service to demonstrate how changes in the resource tree can be listened for. It registers an event handler service.
 * The component is activated immediately after the bundle is started through the immediate flag. Please note, that
 * apart from EventHandler services, the immediate flag should not be set on a service.
 */
/*@Component(
    immediate = true,
    service = EventHandler.class,
    property = {
        EventConstants.EVENT_TOPIC + "=" + DamEvent.EVENT_TOPIC
    })
public class UpdateAssetEventListener implements EventHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String WORKFLOW_MODEL = "/var/workflow/models/bbby-update-asset";

    @Reference
    private WorkflowService wfService;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private AssetHasherService assetHasherService;

    @Reference
    private CallPDMService callPDMService;

    public void handleEvent(final Event event) {
        log.trace("Resource event: {} at: {}", event.getTopic(), event.getProperty(SlingConstants.PROPERTY_PATH));
        Object type = event.getProperty("type");

        if (type == DamEvent.Type.DAM_UPDATE_ASSET_WORKFLOW_COMPLETED) {

            String assetPath = DamEvent.fromEvent(event).getAssetPath();
            log.info("Update Asset workflow completed for {}", assetPath);


            try {
                createHash(assetPath);

            } catch (Exception e) {
                e.printStackTrace();
                log.error("Exception processing Update Asset complete event {}", e.getLocalizedMessage());
            }
        }
    }

    private void createHash(String assetPath) throws RepositoryException, LoginException {


        try (ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "workflow-service")) {

            Session session = resourceResolver.adaptTo(Session.class);


            AssetHash assetHash = null;
            boolean duplicate = true;
            assetHash = assetHasherService.createAssetHash(resourceResolver, assetPath);

            session.save();

            if (assetHash != null) {
                Resource assetResource = resourceResolver.getResource(assetPath);
                Node assetNode = assetResource.adaptTo(Node.class);
                duplicate = AssetHasherUtils.checkForDups(session, assetNode, assetHash.getAssetHashCode());
            }
            
            if (duplicate && !assetPath.contains("bbby/asset_transitions_folder/internal")) {
                log.warn("Asset marked as duplicate. Skipping PDM Call");
                return;
            }

            callPDM(session, assetPath);

        } catch (Exception e) {
            log.error("Failed to start workflow", e);
        }
    }

    private void callPDM(Session session, String assetPath) throws Exception {
        int responceCode;

        Node assetNode = (Node) session.getItem(assetPath.replaceAll("/jcr:content.*", ""));
        PDMAPICommand updateAssetCmd = new UpdateAssetCommand(PDMAPICommand.METHOD_POST, assetNode);
        responceCode = callPDMService.makePDMCall(session, assetPath, updateAssetCmd);

        log.debug(String.format("PDM Call response for asset %1$s is %2$s", assetPath, responceCode));
    }
}*/
