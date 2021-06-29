package com.bbby.aem.core.services;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import javax.jcr.*;
import java.text.ParseException;

/**
 * The Interface UploadHistoryDetailsService.
 * @author karthik.koduru
 */
public interface UploadHistoryDetailsService {


    JSONObject getBatchDetailsJSON(SlingHttpServletRequest request, String userPath, String batchName, ResourceResolver resolver)
        throws AccessDeniedException, ValueFormatException, PathNotFoundException, ItemNotFoundException,
        RepositoryException, ParseException, JSONException;

}
