package com.bbby.aem.core.ondeploy.workflow;

import com.adobe.acs.commons.ondeploy.OnDeployScriptProvider;
import com.adobe.acs.commons.ondeploy.scripts.OnDeployScript;
import com.bbby.aem.core.ondeploy.workflow.scripts.DamUpdateAssetSync_v1;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides the list of workflow models that should be synced on an author instances. AEM 6.4 introduced the need to
 * "sync" scripts between {@code /conf} and {@code /var}. This is done with the ACS Commons On Deploy Script
 * functionality.
 *
 * <h2>Instructions</h2>
 * When updating the, for instance, the DAM Update Asset workflow, make updates to the {@code content.xml} file then
 * increment the version number of the Sync class (ex {@link DamUpdateAssetSync_v1}). This will cause the sync script
 * to run again on all environments when next deployed.
 *
 * <h2>Username / Password</h2>
 * In order for the scripts to run, you must provide credentials for a user with workflow edit permissions. This should
 * be manually set in /system/console/configMgr on each author server.
 * <p>
 * Upon saving changes in /system/console/configMgr, the scripts will automatically re-run (if they have yet to complete
 * successfully).
 *
 * @see <a href="https://adobe-consulting-services.github.io/acs-aem-commons/features/on-deploy-scripts/index.html">Docs</a>
 * @author joelepps
 * 7/13/18
 */
@Component(service = OnDeployScriptProvider.class)
@Designate(ocd = WorkflowSyncScriptProviderConfig.class)
public class WorkflowSyncScriptProvider implements OnDeployScriptProvider {

    private static final Logger log = LoggerFactory.getLogger(WorkflowSyncScriptProvider.class);

    private String username;
    private String password;

    @Reference
    private SlingSettingsService slingSettingsService;

    @Activate
    protected void activate(WorkflowSyncScriptProviderConfig config) {
        this.username = config.username();
        this.password = config.password();
    }

    @Override
    public List<OnDeployScript> getScripts() {
        if (!slingSettingsService.getRunModes().contains("author")) return Collections.emptyList();

        return Arrays.asList(
            new DamUpdateAssetSync_v1(username, password));
    }

}
