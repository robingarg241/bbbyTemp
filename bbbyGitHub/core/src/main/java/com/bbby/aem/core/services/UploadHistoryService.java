package com.bbby.aem.core.services;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.json.JSONException;
import org.json.simple.JSONObject;

import javax.jcr.*;
import java.text.ParseException;

/**
 * The Interface BatchesService.
 * @author karthik.koduru
 */
public interface UploadHistoryService {

    /**
     * Gets the batches JSON.
     *
     * @param request the request
     * @return the batches JSON
     * @throws AccessDeniedException the access denied exception
     * @throws ValueFormatException  the value format exception
     * @throws PathNotFoundException the path not found exception
     * @throws ItemNotFoundException the item not found exception
     * @throws RepositoryException   the repository exception
     * @throws ParseException        the parse exception
     * @throws JSONException         the JSON exception
     */
    JSONObject getBatchesJSON(SlingHttpServletRequest request, String pagePath, ResourceResolver resolver)
        throws AccessDeniedException, ValueFormatException, PathNotFoundException, ItemNotFoundException,
        RepositoryException, ParseException, JSONException;

}
