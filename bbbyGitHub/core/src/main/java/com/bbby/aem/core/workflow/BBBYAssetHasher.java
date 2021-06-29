package com.bbby.aem.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.bbby.aem.core.models.data.AssetHash;
import com.bbby.aem.core.services.AssetHasherService;
import com.bbby.aem.core.util.AssetHasherUtils;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.workflow.impl.BBBYAssetHasherConfiguration;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;

/**
 * @author ANT
 * 07/05/19
 * <p>
 * This code is used to sort assets to specific sub-directories (node) based on information received from PDM
 */

@Component(service = WorkflowProcess.class, property = {
    "process.label=BBBY Asset Hasher",
    "Constants.SERVICE_VENDOR=Hero Digital",
    "Constants.SERVICE_DESCRIPTION=BBBY creates a unique hash of an asset based on specific metadata"})
@Designate(ocd = BBBYAssetHasherConfiguration.class)

public class BBBYAssetHasher implements WorkflowProcess {

    @Reference
    private AssetHasherService assetHasherService;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Activate
    public synchronized void activate(BBBYAssetHasherConfiguration config) {
    }

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {

        WorkflowData workflowData = workItem.getWorkflowData();
        String type = workflowData.getPayloadType();
        ResourceResolver resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
        Session session = workflowSession.adaptTo(Session.class);


        if (CommonConstants.JCR_PATH.equalsIgnoreCase(type)) {

            String path = workflowData.getPayload().toString().replaceAll("/jcr:content.*", "");

            AssetHash assetHash = null;
            try {
                assetHash = assetHasherService.createAssetHash(resourceResolver, path);

                if (assetHash != null) {
                    Resource assetResource = resourceResolver.getResource(path);
                    Node assetNode = assetResource.adaptTo(Node.class);
                    boolean duplicate = AssetHasherUtils.checkForDups(session, assetNode, assetHash.getAssetHashCode());

                    workflowData.getMetaDataMap().put("duplicate.asset", duplicate);
                }
            } catch (Exception ex) {
                log.error("Unable to create asset hash value: ", ex);
            }

        }

    }
}
