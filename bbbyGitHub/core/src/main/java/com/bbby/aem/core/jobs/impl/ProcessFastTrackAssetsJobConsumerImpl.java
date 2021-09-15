package com.bbby.aem.core.jobs.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.s7dam.set.ImageSet;
import com.day.cq.dam.commons.util.S7SetHelper;

/**
 *
 * @author BA37483
 * 
 */
@Component(immediate = true, service = JobConsumer.class, property = {
		"job.topics=" + CommonConstants.FAST_TRACK_ASSET_PROCESS_TOPIC })
public class ProcessFastTrackAssetsJobConsumerImpl implements JobConsumer {

	private static final Logger log = LoggerFactory.getLogger(ProcessFastTrackAssetsJobConsumerImpl.class);

	@Reference
	private ResourceResolverFactory resourceFactory;
	
	private Session session;

	@Override
	public JobResult process(final Job job) {

		log.debug("Starting a job " + job.getTopic());
		try (ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resourceFactory, "writeservice")) {

			session = resourceResolver.adaptTo(Session.class);
			String rootPath = (String) job.getProperty(CommonConstants.ROOTPATH);

			if (StringUtils.isEmpty(rootPath)) {
				log.warn("Empty rootPath");
				return JobConsumer.JobResult.CANCEL;
			}
			
			Map<String,ArrayList<Resource>> upcMap = new HashMap<String,ArrayList<Resource>>();
			
			Map<String,TreeMap<Integer, Resource>> upcMapwithSequenceMap = new HashMap<String,TreeMap<Integer, Resource>>();
			
			List<Resource> resources = ServiceUtils.getResourcesInDirectory(resourceResolver, rootPath);
			
			for (Resource resource : resources) {
				String upc = ServiceUtils.getMetadataValue(resource, CommonConstants.BBBY_UPC, null);
				if (upc != null) {
					String sequence = ServiceUtils.getMetadataValue(resource, CommonConstants.BBBY_SEQUENCE, null);
					if(sequence != null){
						if (upcMapwithSequenceMap.containsKey(upc)) {
							upcMapwithSequenceMap.get(upc).put(Integer.valueOf(sequence),resource);
						} else {
							upcMapwithSequenceMap.put(upc, new TreeMap<Integer, Resource>());
							upcMapwithSequenceMap.get(upc).put(Integer.valueOf(sequence),resource);
						}	
					}
					
					if (upcMap.containsKey(upc)) {
						upcMap.get(upc).add(resource);
					} else {
						upcMap.put(upc, new ArrayList<Resource>());
						upcMap.get(upc).add(resource);
					}
				}
			}
			
			log.debug("Total number of set present in batch : " + upcMap.size());
			
			createImageset(upcMap, upcMapwithSequenceMap);

		} catch (Exception e) {
			log.error("Exception processing fast track asset", e);
			return JobConsumer.JobResult.FAILED;
		}

		return JobConsumer.JobResult.OK;
	}
	
	private void createImageset(Map<String, ArrayList<Resource>> upcMap,
			Map<String, TreeMap<Integer, Resource>> upcMapwithSequenceMap) {
		Set<String> keys = upcMap.keySet();
		for (String key : keys) {
			if (upcMap.get(key).size() != upcMapwithSequenceMap.get(key).size()) {
				log.debug("Can not process set with UPC{} due to Incorrect Sequence number  : " + upcMap.size());
			} else {
				// TODO: check for proper sequence of BBBY:Sequence Number like 1,2,3,4,5.....so on.
				createPrimaryImageSet(upcMapwithSequenceMap.get(key));
			}
		}

	}
	
	private void createPrimaryImageSet(TreeMap<Integer, Resource> map){
    	log.debug("entering createPrimaryImageSet() method");
        
        try {
        	Resource image1r = map.get(1);
            String fileName = image1r.getName();
            
            Resource damResource = image1r.getParent();
            Asset asset = image1r.adaptTo(Asset.class);
            
            String imageSetName = fileName.substring(0, fileName.lastIndexOf(".")) + "_imageset";
            String imagesetPath = damResource.getPath()+"/"+imageSetName;
            try {
                @SuppressWarnings("deprecation")
                ImageSet imageset = S7SetHelper.createS7ImageSet(damResource, imageSetName, null);
                log.debug(imageset.getPath());
                imageset.add(asset);
                imagesetPath = imageset.getPath();
                
                for (Map.Entry<Integer, Resource> entry : map.entrySet()) {
					if(!new Integer(1).equals(entry.getKey())){
						Asset altAsset = entry.getValue().adaptTo(Asset.class);
						imageset.add(altAsset);
					}
				}
                
            } catch (Exception e) {
                log.error("failed to create ImageSet as it already Exist", e);
            }
            
            Node imageMeta = session.getNode(imagesetPath).getNode(CommonConstants.METADATA_NODE);
            
            session.getNode(image1r.getPath()).getNode(CommonConstants.METADATA_NODE).setProperty(CommonConstants.BBBY_IMAGE_SET_NAME, imageSetName);
            
            //brand the imageset with the asset tags and decorators
            
            ArrayList<String> tagList = getImagesetTags(session.getNode(image1r.getPath()));
            
            imageMeta.setProperty(CommonConstants.CQ_TAGS, (String[]) tagList.toArray(new String[tagList.size()]));
            
            String primaryUPC = ServiceUtils.getMetadataValue(image1r, CommonConstants.BBBY_UPC, "");
            String sku = ServiceUtils.getMetadataValue(image1r, CommonConstants.BBBY_SKU, "");

            imageMeta.setProperty(CommonConstants.BBBY_UPC, primaryUPC);
            imageMeta.setProperty(CommonConstants.BBBY_SKU, sku);
            imageMeta.setProperty(CommonConstants.BBBY_ASSET_UPDATE, "no");
            imageMeta.setProperty("dc:title", imageSetName);
            
            session.save();
            
        } catch (Exception e) {
            log.error("failed to create ImageSet", e);
        }
        
    }
	
	private ArrayList<String> getImagesetTags(Node node) throws WorkflowException {
		log.debug("entering getImagesetTags() method");
		ArrayList<String> tagList = new ArrayList<String>();

		// We always want to move the image set to this directory. It overwrites the images asset_type
		tagList.add("bbby:asset_type/approved_dam/assets/product/images/image_sets");

		try {
			Node meta = node.getNode(CommonConstants.METADATA_NODE);
			Property tagP = meta.getProperty(CommonConstants.CQ_TAGS);
			Value[] tagV = tagP.getValues();

			// Add other tags to the image set
			for (Value v : tagV) {
				if (!v.getString().contains("asset_type")) {
					tagList.add(v.getString());
				}
			}

		} catch (Exception e) {
			log.error("Unable to determine asset tags", e);
		}

		return tagList;
	}

}
