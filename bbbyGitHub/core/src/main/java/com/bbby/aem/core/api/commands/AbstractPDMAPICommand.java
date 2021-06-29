package com.bbby.aem.core.api.commands;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.jackrabbit.commons.JcrUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bbby.aem.core.api.PDMAPICommand;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.JcrPropertiesUtil;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.date.DateUtil;

public class AbstractPDMAPICommand implements PDMAPICommand {
    private final String method;
    private final ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    private final ArrayList<Header> headers = new ArrayList<Header>();
    private String url;
    private HttpEntity entity;


    /**
     * Public constructor with default HTTP method (GET).
     */
    public AbstractPDMAPICommand() {
        this(PDMAPICommand.METHOD_GET);
    }

    /**
     * Public constructor with HTTP method specified.
     *
     * @param method the HTTP method
     */
    public AbstractPDMAPICommand(final String method) {
        this.method = method;
        addHeader("Use-HTTP-Errors", "true"); // always add this header to get correct HTTP response status
    }


    /**
     * {@inheritDoc}
     *
     * @see PDMAPICommand#getMethod()
     */
    public String getMethod() {
        return method;
    }

    /**
     * {@inheritDoc}
     *
     * @see PDMAPICommand#getURL()
     */
    public String getURL() {
        return url;
    }

    /**
     * Set the path.
     *
     * @param url the path to be used
     */
    public void setURL(final String url) {
        this.url = url;
    }

    /**
     * {@inheritDoc}
     *
     * @see PDMAPICommand#getQueryParameters()
     */
    public ArrayList<NameValuePair> getQueryParameters() {
        return parameters;
    }

    /**
     * Add query string parameter
     *
     * @param key   the parameter name
     * @param value the parameter value
     */
    public void addParameter(final String key, final String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key may not be blank");
        }
        parameters.add(new BasicNameValuePair(key, value));
    }

    /**
     * {@inheritDoc}
     *
     * @see PDMAPICommand#getHeaders()
     */
    public ArrayList<Header> getHeaders() {
        return headers;
    }

    /**
     * Add header attribute.
     *
     * @param key   the header attribute key
     * @param value the header attribute value
     */
    public void addHeader(final String key, final String value) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key may not be blank");
        }

        if (StringUtils.isNotBlank(value)) {
            headers.add(new BasicHeader(key, value));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see PDMAPICommand#getEntity()
     */
    public HttpEntity getEntity() {
        return entity;
    }

    /**
     * set the entity
     *
     * @param entity the entity to be used
     */
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    /**
     * {@inheritDoc}
     *
     * @see PDMAPICommand#isAuthenticationRequired()
     */
    public boolean isAuthenticationRequired() {
        return false;
    }


    @Override
    public String toString() {
        return "Command: " + getMethod() + " " + getURL() + "?" + getQueryParameters().toString();
    }

    /**
     * create the asset entity
     *
     * @param assetNode the asset Node to be used
     */
    public JSONObject createAssetEntity(Node assetNode) throws RepositoryException, JSONException {
        JSONObject requestJSON = new JSONObject();
        Node jcrContentNode = assetNode.getNode(CommonConstants.JCR_CONTENT);
        JSONArray assets = new JSONArray();
        JSONObject asset = new JSONObject();
        asset.put(CommonConstants.JCR_CREATED_BY, JcrUtils.getStringProperty(assetNode, CommonConstants.JCR_CREATED_BY, ""));
        asset.put(CommonConstants.JCR_CREATED, DateUtil.getISO8601DateAndTimeNoMillis(JcrUtils.getDateProperty(assetNode, CommonConstants.JCR_CREATED, null)));
        asset.put(CommonConstants.JCR_UUID, JcrUtils.getStringProperty(assetNode, CommonConstants.JCR_UUID, ""));
        asset.put(CommonConstants.LAST_PDM_SYNC_ACTION, JcrUtils.getStringProperty(assetNode, CommonConstants.LAST_PDM_SYNC_ACTION, ""));
        asset.put(CommonConstants.DAM_RELATIVE_PATH, assetNode.getPath());
        asset.put(CommonConstants.RENDITIONS, getRenditions(jcrContentNode));
        asset.put(CommonConstants.METADATA, getMetadata(jcrContentNode));
        assets.put(asset);

        requestJSON.put("assets", assets);

        return requestJSON;
    }

    private JSONObject getRenditions(Node jcrContentNode) throws RepositoryException, JSONException {
        JSONObject renditions = new JSONObject();
        Node renditionsNode = jcrContentNode.getNode("renditions");

        if (renditionsNode.hasNode("cq5dam.web.75.75.jpeg"))
            renditions.put("thumbnail-75x75", jcrContentNode.getParent().getPath() + ".imgt.75.75.jpeg");
        if (renditionsNode.hasNode("cq5dam.web.300.300.jpeg"))
            renditions.put("thumbnail-300x300", jcrContentNode.getParent().getPath() + ".imgt.300.300.jpeg");
        return renditions;
    }

    private JSONObject getMetadata(Node jcrContentNode) throws RepositoryException, JSONException {
        JSONObject metadata = new JSONObject();
        Node metadataNode = jcrContentNode.getNode("metadata");
        Node repometa = null;
        if(jcrContentNode.hasNode("reportingmetadata")){
			repometa = jcrContentNode.getNode("reportingmetadata");
		if (repometa != null && repometa.hasProperty("imageApprovedDate")) {
			 metadata.put("imageApprovedDate", JcrUtils.getStringProperty(repometa, "imageApprovedDate", ""));
		}
		else {
			metadata.put("imageApprovedDate", "");
		}
		}
        else {
        	metadata.put("imageApprovedDate", "");	
        }
        
        metadata.put("upc", getNodeValues(metadataNode, CommonConstants.BBBY_UPC));
        metadata.put("sku", getNodeValues(metadataNode, CommonConstants.BBBY_SKU));

        metadata.put(CommonConstants.BBBY_PRIMARY_IMAGE, ServiceUtils.getBooleanValue(JcrUtils.getStringProperty(metadataNode, CommonConstants.BBBY_PRIMARY_IMAGE, "")));
        metadata.put(CommonConstants.BBBY_IMAGE_SET_NAME, JcrUtils.getStringProperty(metadataNode, CommonConstants.BBBY_IMAGE_SET_NAME, ""));

        if (metadataNode.hasProperty(CommonConstants.BBBY_SWATCH_OVERRIDE))
            metadata.put(CommonConstants.BBBY_SWATCH_OVERRIDE_NAME, JcrUtils.getStringProperty(metadataNode, CommonConstants.BBBY_SWATCH_OVERRIDE, ""));

        metadata.put(CommonConstants.BBBY_ASSIGN_TO_WEB_PRODUCT_ID_LABEL, getNodeValues(metadataNode, CommonConstants.BBBY_ASSIGN_TO_WEB_PRODUCT_ID));
        metadata.put(CommonConstants.BBBY_ASSIGN_TO_WEB_COLLECTION_ID_LABEL, getNodeValues(metadataNode, CommonConstants.BBBY_ASSIGN_TO_WEB_COLLECTION_ID));

        metadata.put(CommonConstants.BBBY_ASSET_UPDATE, ServiceUtils.getBooleanValue(JcrUtils.getStringProperty(metadataNode, CommonConstants.BBBY_ASSET_UPDATE, "")));
        
        //Setting asset type and shot type for marketing assets
        if(jcrContentNode.getPath().contains("/content/dam/marketing")){
	        metadata.put(CommonConstants.MARKETING_ASSET_TYPE_LABEL, ServiceUtils.getTagValue(CommonConstants.MARKETING_ASSET_TYPE, metadataNode));
	        metadata.put(CommonConstants.MARKETING_SHOT_TYPE_LABEL, ServiceUtils.getTagValue(CommonConstants.MARKETING_SHOT_TYPE, metadataNode));
        } else{
        	metadata.put(CommonConstants.BBBY_ASSET_TYPE_LABEL, ServiceUtils.getTagValue(CommonConstants.BBBY_ASSET_TYPE, metadataNode));
            metadata.put(CommonConstants.BBBY_SHOT_TYPE_LABEL, ServiceUtils.getTagValue(CommonConstants.BBBY_SHOT_TYPE, metadataNode));
        }
        
        //DAM-1085 : Videos, Collateral asset status is set to Rejected in Asset Tracking tab for Internal assets uploaded into AEM
        if(ServiceUtils.isRejectedAsset(metadataNode)){
        	metadata.put(CommonConstants.BBBY_REJECTION_REASON, JcrUtils.getStringProperty(metadataNode, CommonConstants.BBBY_REJECTION_REASON, null));
        }
        
        metadata.put(CommonConstants.BBBY_SEQUENCE, JcrUtils.getStringProperty(metadataNode, CommonConstants.BBBY_SEQUENCE, ""));
        metadata.put(CommonConstants.DAM_MIME_TYPE, JcrUtils.getStringProperty(metadataNode, CommonConstants.DAM_MIME_TYPE, ""));

        String scene7FileName = JcrUtils.getStringProperty(metadataNode, CommonConstants.DAM_SCENE_7_FILENAME, "");

        if (StringUtils.isNotEmpty(scene7FileName))
            metadata.put(CommonConstants.BBBY_SWATCH, scene7FileName + ":Swatch");

        return metadata;
    }

	private JSONArray getNodeValues(Node metadataNode, String nodeName) throws RepositoryException {
		JSONArray jsonArray = new JSONArray();
		String[] nodeValues = StringUtils.split(JcrUtils.getStringProperty(metadataNode, nodeName, ""), ";");
		for (String value : nodeValues) {
			jsonArray.put(value);
		}
		return jsonArray;
	}

}
