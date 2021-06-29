package com.bbby.aem.core.jobs.impl;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.models.data.PdmAsset;
import com.bbby.aem.core.services.AssetService;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Job consumer that downloads images to DAM and links them to product asset node.
 * More info https://sling.apache.org/documentation/bundles/apache-sling-eventing-and-job-handling.html
 *
 * @author vpokotylo
 * 
 */
@Component(immediate = true, service = JobConsumer.class,
    property = {"job.topics=" + CommonConstants.ASSET_UPDATE_TOPIC})
public class PDMMetadataJobConsumerImpl implements JobConsumer {

    private static final Logger log = LoggerFactory.getLogger(PDMMetadataJobConsumerImpl.class);

//    private static final Pattern RENDITION_REPLACE = Pattern.compile("\\.\\d{1,3}w\\.");

    public static final Gson GSON = new GsonBuilder().create();
	
    
    @Reference
    private ResourceResolverFactory resourceFactory;

    @Reference
    private AssetService assetService;
    
    @Override
    public JobResult process(final Job job) {

    	log.debug("Starting a job " + job.getTopic());
        try (ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resourceFactory, "pdm-content-service")) {

            String assetsJson = (String) job.getProperty("assetsMetadata");

            if (StringUtils.isEmpty(assetsJson)) {
        		log.warn("Empty asset JSON");
        		return JobConsumer.JobResult.CANCEL;
        	}
            Instant start = Instant.now();
            try {
                Type listType = new TypeToken<ArrayList<PdmAsset>>() {
                }.getType();

                List<PdmAsset> assetsList = GSON.fromJson(assetsJson, listType);
                log.debug("PDM data for asset is " + assetsList.toString());

                for (PdmAsset pdmAsset : assetsList) {
                    try {
                        assetService.updateAssetMetadata(pdmAsset, resourceResolver);
                    } catch (Exception ex) {
                        log.error("Update error for asset with uuid : " + pdmAsset.getUuid() + ex);
                        return JobConsumer.JobResult.FAILED;
                    }
                }
                // Lastly commit changes...
                resourceResolver.commit();
                Instant finish = Instant.now();
    			long timeElapsed = Duration.between(start, finish).toMillis(); // in_millis
                log.debug("Completed a job " + job.getTopic() + " + in + " + timeElapsed + " MilliSeconds.");
            } catch (PersistenceException e) {
                log.error("Persistence error : " + e);
                return JobConsumer.JobResult.CANCEL;
            }
        } catch (LoginException e) {
            log.error("Error obtaining resource resolver job consumer");
            return JobConsumer.JobResult.CANCEL;
        }
        

        return JobConsumer.JobResult.OK;
    }

    
}
