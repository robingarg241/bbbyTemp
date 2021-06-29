package com.bbby.aem.core.processes.impl;


import com.adobe.acs.commons.mcp.AuthorizedGroupProcessDefinitionFactory;
import com.adobe.acs.commons.mcp.ProcessDefinition;
import com.adobe.acs.commons.mcp.ProcessDefinitionFactory;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = ProcessDefinitionFactory.class)
public class AzureAssetIngestorFactory extends AuthorizedGroupProcessDefinitionFactory<ProcessDefinition> {

    @Reference
    MimeTypeService mimetypeService;

    @Override
    public String getName() {
        return "Azure Asset Ingestor";
    }

    @Override
    public ProcessDefinition createProcessDefinitionInstance() {
        return new AzureAssetIngestor(mimetypeService);
    }

	@Override
	protected String[] getAuthorizedGroups() {
		return new String[] {
	            "administrators",
	            "asset-ingest",
	            "dam-administrators"
	    };
	}

    
}
