package com.bbby.aem.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.bbby.aem.core.util.CommonConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds the {@link #ANALYTICS_PAGE_ID} property to all cq:PageContent nodes on page creation.
 *
 * @author joelepps 7/01/16
 */
@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=BBBY Analytics Page ID Creator"
    })
public class AnalyticsPageIdCreator implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsPageIdCreator.class);

    public static final String ANALYTICS_PAGE_ID = "analytics.pageId";

    /**
     * Group 1: locale Group 2: node-name
     */
    private static final Pattern KEYS_EXTRACT = Pattern.compile("/content/[^/]+/([^/]+)(?:.*/(.*))?");

    /**
     * Group 1: locale Group 2: node name Group 3: random string
     */
    private static final Pattern ANALYTICS_ID_VALID = Pattern.compile("^([^:]+):([^:]+):([^:]+)$");

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
        throws WorkflowException {
        if (workItem == null) {
            log.error("WorkItem is null");
            return;
        }

        WorkflowData workflowData = workItem.getWorkflowData();
        if (workflowData == null) {
            log.error("WorkflowData is null");
            return;
        }
        if (!workflowData.getPayloadType().equals(CommonConstants.JCR_PATH)) {
            log.error("Unsupported payload type " + workflowData.getPayloadType());
            return;
        }

        String workItemTargetPath = workflowData.getPayload() + "";

        log.debug("Working on {}", workItemTargetPath);

        Session currentSession = workflowSession.adaptTo(Session.class);

        try {
            ResourceResolver resolver = resourceResolverFactory
                .getResourceResolver(Collections.singletonMap("user.jcr.session", currentSession));

            PageManager pageManager = resolver.adaptTo(PageManager.class);
            if (pageManager == null) {
                log.error("Failed to adapt resolver to page manager: " + workItemTargetPath + ", " + workItem.getId());
                return;
            }

            Page page = pageManager.getContainingPage(workItemTargetPath);
            if (page == null) {
                log.info("Skipping {}, cannot find page", workItemTargetPath);
                return;
            }

            Matcher m = KEYS_EXTRACT.matcher(page.getPath());
            String locale;
            String nodeName;
            if (!m.find()) {
                log.info("{}, does not match pattern, using fallback values", page.getPath());
                locale = "";
                nodeName = page.getName();
            } else {
                locale = m.group(1);
                nodeName = m.group(2);
            }

            Resource jcrContentResource = page.getContentResource();

            Node jcrContentNode = jcrContentResource.adaptTo(Node.class);
            if (jcrContentNode == null) {
                log.info("Skipping {}, could not adapt to node", jcrContentResource);
                return;
            }

            if (nodeName == null) nodeName = locale;
            String random = RandomStringUtils.randomAlphanumeric(8);
            String analyticsId = locale + ":" + nodeName + ":" + random;

            if (hasValidValue(jcrContentResource, locale)) {
                log.info("Skipping {}, already valid value", jcrContentResource.getPath());
                return;
            }

            jcrContentNode.setProperty(ANALYTICS_PAGE_ID, analyticsId);
            currentSession.save();

            log.info("Added {} to {}: {}", ANALYTICS_PAGE_ID, jcrContentResource.getPath(), analyticsId);

        } catch (LoginException | RepositoryException e) {
            log.error("Failed during " + workItemTargetPath, e);
        }
    }

    private boolean hasValidValue(Resource jcrContentResource, String expectedLocale) {
        ValueMap valueMap = jcrContentResource.getValueMap();

        String analyticsId = valueMap.get(ANALYTICS_PAGE_ID, String.class);
        if (analyticsId == null) return false;

        Matcher m = ANALYTICS_ID_VALID.matcher(analyticsId);
        if (!m.find()) return false;

        String locale = m.group(1);

        if (!locale.equals(expectedLocale)) return false;

        return true;
    }

}
