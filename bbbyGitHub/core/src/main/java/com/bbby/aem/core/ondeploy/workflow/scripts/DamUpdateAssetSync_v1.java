package com.bbby.aem.core.ondeploy.workflow.scripts;

import com.bbby.aem.core.ondeploy.workflow.WorkflowMetadata;
import com.bbby.aem.core.ondeploy.workflow.WorkflowSyncScript;

public class DamUpdateAssetSync_v1 extends WorkflowSyncScript {

    public DamUpdateAssetSync_v1(String username, String password) {
        super(username, password, new WorkflowMetadata("/conf/global/settings/workflow/models/dam/update_asset"));
    }

}
