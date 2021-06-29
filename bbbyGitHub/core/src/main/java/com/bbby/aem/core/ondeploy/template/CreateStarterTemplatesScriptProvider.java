package com.bbby.aem.core.ondeploy.template;

import com.adobe.acs.commons.ondeploy.OnDeployScriptProvider;
import com.adobe.acs.commons.ondeploy.scripts.OnDeployScript;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.List;

@Component(service = OnDeployScriptProvider.class)
public class CreateStarterTemplatesScriptProvider implements OnDeployScriptProvider {

    @Override
    public List<OnDeployScript> getScripts() {
        return Collections.singletonList(new CreateStarterTemplatesScript());
    }

}
