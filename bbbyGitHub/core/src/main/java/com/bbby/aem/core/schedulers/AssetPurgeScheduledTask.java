package com.bbby.aem.core.schedulers;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.WCMException;
import com.google.gson.annotations.Expose;

/**
 * Deletes rejected assets in DAM that are past their expiration date.
 * 
 * @author vpokotylo
 *
 */
@Component(
    service = Runnable.class
)
@Designate(ocd = AssetPurgeScheduledTaskConfiguration.class)
public class AssetPurgeScheduledTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Expose
    private boolean enabled;
    
    @Expose
    private int daysTooKeepRejected;

    @Expose
    private String[] pathsRejected;
    
    @Expose
    private int daysTooKeepDuplicate;

    @Expose
    private String[] pathsDuplicate;
    
    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Reference
    private SlingSettingsService slingSettingsService;
    
    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    Externalizer externalizer;
    
    @Activate
    protected void activate(AssetPurgeScheduledTaskConfiguration config) throws LoginException {

        this.enabled = config.enabled();
        this.daysTooKeepRejected = config.daysTooKeepRejected();
        this.pathsRejected = config.pathsRejected();
        this.daysTooKeepDuplicate = config.daysTooKeepDuplicate();
        this.pathsDuplicate = config.pathsDuplicate();
    }

    
    @Override
    public void run() {
        
        if (!slingSettingsService.getRunModes().contains("author")) {
            log.warn("Attempt to run from non-author environment");
            return;
        }
        
        if(!enabled) {
            return;
        }
          
        try( ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice")) {        	
       
        	for(String path : pathsRejected) {
        		cleanupAssets(resourceResolver, path, daysTooKeepRejected);
        	}
        	
        	for(String path : pathsDuplicate) {
        		cleanupAssets(resourceResolver, path, daysTooKeepDuplicate);
        	}
        	
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error removing rejected assets", e.getMessage());
			
		} 
    }
    
	private void cleanupAssets(ResourceResolver resourceResolver, String path, int daysTooKeep) throws WCMException, PersistenceException {
		log.info("Cleaning rejected assets");

		LocalDateTime today = LocalDateTime.now();
		today = today.minusDays(daysTooKeep);

		ZonedDateTime todayZoned = today.atZone(ZoneId.systemDefault());
		String formattedDate = todayZoned.format(DateTimeFormatter.ofPattern(CommonConstants.DATE_FORMATTER_FULL).withLocale(Locale.getDefault()));

		String queryString = MessageFormat.format(
				"SELECT * FROM [dam:Asset] AS N WHERE ISDESCENDANTNODE(N,\"{0}\") AND N.[jcr:created] < CAST(\"{1}\" AS DATE)",
				new Object[] { path, formattedDate });

		log.debug("Executing query {}", queryString);
		Session session = resourceResolver.adaptTo(Session.class);
		int count = 0;
		Iterator<Resource> assetResources = resourceResolver.findResources(queryString, Query.JCR_SQL2);
		while (assetResources.hasNext()) {
			Resource assetResource = (Resource) assetResources.next();

			log.debug("Removing asset at {}", assetResource.getPath());
			resourceResolver.delete(assetResource);

			count++;
			if(count%50 == 0){
				try{
					session.save();
				}catch (RepositoryException e) {
					log.error("Repository save error while saving 50 assets at a time.", e.getLocalizedMessage());
				}
			}

		}
		
		try {
			session.save();
			
		} catch (RepositoryException e) {
			log.error("Repository save error ", e.getLocalizedMessage());
		}
		log.info("Deleted {} assets", count);
	}
}
