package com.bbby.aem.core.api.commands;

import com.bbby.aem.core.util.CommonConstants;
import org.apache.http.entity.StringEntity;
import org.apache.jackrabbit.commons.JcrUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.UnsupportedEncodingException;

public class PublishAssetCommand extends AbstractPDMAPICommand{
    private static final String PATH_FULL = "/bbby_pdm/assets/submitAssetInfo";

    private static final Logger log = LoggerFactory.getLogger(PublishAssetCommand.class);

    public PublishAssetCommand(String method, Node assetNode) {
        super(method);
        setURL(PATH_FULL);
        try {
            String entity = createAssetEntity(assetNode).toString();
            log.debug("Asset Entity for asset " + assetNode.getPath() + " is " + entity);
            StringEntity assetEntity = new StringEntity(entity);
            setEntity(assetEntity);
        } catch (UnsupportedEncodingException ex) {
            log.error("Error while creating entity" + ex);
        } catch (RepositoryException ex) {
            log.error("Error while accessing the repository" + ex);
        } catch (JSONException ex) {
            log.error("Error while converting the json" + ex);
        }

    }

    @Override
    public JSONObject createAssetEntity(Node assetNode) throws RepositoryException, JSONException {
        JSONObject requestEntityJSON = super.createAssetEntity(assetNode);

        requestEntityJSON.put("action", "publish");
        addMetadata(assetNode, CommonConstants.DAM_SCENE_7_FILENAME, requestEntityJSON);

        return requestEntityJSON;
    }

    private void addMetadata(Node assetNode, String metadataProp, JSONObject requestJSON) throws RepositoryException, JSONException {

        JSONArray assetsJSON = requestJSON.getJSONArray("assets");
        JSONObject metadata = ((JSONObject) assetsJSON.get(0)).getJSONObject(CommonConstants.METADATA);

        Node jcrContentNode = assetNode.getNode(CommonConstants.JCR_CONTENT);
        Node metadataNode = jcrContentNode.getNode("metadata");

        metadata.put(metadataProp, JcrUtils.getStringProperty(metadataNode, metadataProp, ""));
    }
}
