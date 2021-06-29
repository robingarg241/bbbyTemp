package com.bbby.aem.core.models.data;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.commerce.api.CommerceException;
import com.day.cq.dam.api.Asset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;


public class AssetMetadata {

    private static final Logger log = LoggerFactory.getLogger(AssetMetadata.class);

    public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Expose
    protected String uuid;
    @Expose
    protected Date created;
    @Expose
    protected String createdBy;
    @Expose
    protected String title;
    @Expose
    protected String name;
    @Expose
    protected String relativePath;
    @Expose
    protected Map<String, Object> metadata;

    

    public AssetMetadata(Asset asset, ResourceResolver resolver) throws CommerceException {
        if (asset == null) throw new IllegalArgumentException("Asset cannot be null.");
        if (resolver == null) throw new IllegalArgumentException("Resolver cannot be null for " + asset.getPath());

		try {
			this.uuid = asset.getID();
			Node assetNode = (Node) asset.adaptTo(Node.class);

			this.relativePath = assetNode.getPath();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		this.metadata = asset.getMetadata().entrySet()
	            .stream()
	            .filter(e -> e.getKey().startsWith("dam:"))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
        //skus = new ArrayList<>();
        //product.getVariants().forEachRemaining(v -> skus.add(v.getSKU()));
    }

    

    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {
        return uuid;
    }



	public static Logger getLog() {
		return log;
	}



	public static Gson getGson() {
		return GSON;
	}



	public String getUuid() {
		return uuid;
	}



	public Date getCreated() {
		return created;
	}



	public String getCreatedBy() {
		return createdBy;
	}



	public String getTitle() {
		return title;
	}



	public String getName() {
		return name;
	}



	public String getRelativePath() {
		return relativePath;
	}



	public Map<String, Object> getMetadata() {
		return metadata;
	}
}
