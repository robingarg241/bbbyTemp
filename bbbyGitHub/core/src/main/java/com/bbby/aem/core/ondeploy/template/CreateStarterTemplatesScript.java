package com.bbby.aem.core.ondeploy.template;

import com.adobe.acs.commons.ondeploy.scripts.OnDeployScriptBase;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.TemplateManager;
import com.day.cq.wcm.api.WCMException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * This script creates a base set of unconfigured editable page templates. It's meant to provide a small jump start
 * to the developer/qa who is setting up a project's AEM instance for the first time.
 * <p>
 * If this project already exists and is being installed on a new system, you should download a package of
 * {@code /conf/bbby} folder from the authoritative AEM instance and install it into the new environment.
 *
 * @author joelepps
 * 7/31/18
 */
public class CreateStarterTemplatesScript extends OnDeployScriptBase {

    private static final Logger log = LoggerFactory.getLogger(CreateStarterTemplatesScript.class);

    @Override
    protected void execute() throws Exception {
        ResourceResolver resourceResolver = getResourceResolver();

        TemplateManager templateManager = resourceResolver.adaptTo(TemplateManager.class);
        if (templateManager == null) {
            throw new IllegalStateException("Could not obtain reference to a Template Manager.");
        }

        final String templateConfigDir = "/conf/bbby/settings/wcm";
        final String templatesDir = templateConfigDir + "/templates";
        final String templateTypesDir = templateConfigDir + "/template-types";

        createTemplate(templateManager,
            templatesDir,
            templateTypesDir + "/content-page",
            "Content Page",
            "Generic content page.");

        createTemplate(templateManager,
            templatesDir,
            templateTypesDir + "/home-page",
            "Home Page",
            "Website home page.");

        createTemplate(templateManager,
            templatesDir,
            templateTypesDir + "/fragment-page",
            "Fragment Page",
            "Page which excludes navigation and footer elements.");
    }

    private void createTemplate(TemplateManager templateManager,
                                String parentPath,
                                String templateType,
                                String templateName,
                                String description) throws WCMException {

        if (templateExists(parentPath, templateName)) {
            log.info("Already exists: {} in {}", templateName, parentPath);
            return;
        }

        ValueMap properties = new ValueMapDecorator(new HashMap<>());
        properties.put("jcr:description", description);
        properties.put("jcr:title", templateName);
        properties.put("hidden", "false");
        properties.put("status", "enabled");

        templateManager.createTemplate(parentPath, templateType, templateName, properties);

        log.info("Created: {} in {}", templateName, parentPath);
    }

    private boolean templateExists(String parentPath, String name) {
        String nodeName = JcrUtil.createValidName(name, JcrUtil.HYPHEN_LABEL_CHAR_MAPPING, "_");
        String path = parentPath + "/" + nodeName;

        return getResourceResolver().getResource(path) != null;
    }

}
