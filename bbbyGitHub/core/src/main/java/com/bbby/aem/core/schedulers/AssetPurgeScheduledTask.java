package com.bbby.aem.core.schedulers;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.JobManager;
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
    
    @Expose
	private int maxCount;
    
    @Reference
	private JobManager jobManager;
    
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
        this.maxCount = config.maxCount();
    }
    
    private boolean assetUnsuccessful = false;
    private boolean assetUnsuccessfulDeletion = false;
    private String msg = "";
    private String msgUnsuccessful = "Asset Purging task unsuccessful due to some error while saving session.";

    
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
        	if (assetUnsuccessful && assetUnsuccessfulDeletion){
        		generateUnsuccessfulReport(msgUnsuccessful+'\n'+msg);
        	}else if (assetUnsuccessful && !assetUnsuccessfulDeletion){
        		generateUnsuccessfulReport(msgUnsuccessful);
        	}else if(!assetUnsuccessful && assetUnsuccessfulDeletion){
        		generateUnsuccessfulReport(msg);
        	}
        	msg = "";
        	
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error removing rejected assets", e.getMessage());
			
		} 
    }
    
	private void cleanupAssets(ResourceResolver resourceResolver, String path, int daysTooKeep) throws WCMException, PersistenceException {
		log.info("Cleaning rejected and duplicate assets");
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
		int sizeAssets = 0;
		Iterator<Resource> assetResourcesInitial = resourceResolver.findResources(queryString, Query.JCR_SQL2);
		while (assetResourcesInitial.hasNext()) {
			assetResourcesInitial.next();
			sizeAssets++;
		}
		log.info("Total {} assets to be deleted at path: {}", sizeAssets, path);
		if(sizeAssets<=maxCount){
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
						assetUnsuccessful = true;
						log.error("Repository save error while saving 50 assets at a time.", e.getLocalizedMessage());
					}
				}
			}
			try {
				session.save();
				log.info("Deleted {} assets at path: {}", count, path);
			} catch (RepositoryException e) {
				assetUnsuccessful = true;
				log.error("Repository save error at path: {}", path);
			}
		}else{
			log.info("Assets cannot be deleted at path: " + path + " .Total assets size at this path is greater than max assets to be deleted.");
			assetUnsuccessfulDeletion = true;
			msg = msg + '\n' + "Assets size  at path: " + path + " is : " + sizeAssets + ". This size is greater than " + maxCount + ". That's why these assets cannot be deleted.";
		}
	}
	
	private void generateUnsuccessfulReport(String message) throws WCMException {
		log.debug("enter in generateUnsuccessfulReport()");
		String crdate1 = ServiceUtils.getCurrentDateStr("MM-dd-yyyy");
		
		// mail body start
		StringBuilder builder = new StringBuilder("<br>");

		builder.append(
				"<p>"+ message + "</p>");

		builder.append("<br>");
		builder.append("<br>");

		builder.append("<p>Please contact your Bed Bath & Beyond Inc. representative for follow-up.</p>");
		builder.append("<p>Thank You,<br>Bed Bath & Beyond, Inc.</p>");
		builder.append("<br>");

		sendMail(builder, crdate1);

	}
	
	private void sendMail(StringBuilder builder, String crdate1) throws WCMException {
		String MAIL_SUBJECT = ServiceUtils.getHostName() + ": Report On Unsuccessful purging of assets (" + crdate1 + ")";

		final Map<String, Object> props = new HashMap<String, Object>();
		log.info("Queeing the job for sending mail to vendor:dam-it-support@bedbath.com");

		props.put(CommonConstants.TO, "dam-it-support@bedbath.com");
		//props.put(CommonConstants.TO, "robin.garg@idc.bedbath.com");
		props.put(CommonConstants.SUBJECT, MAIL_SUBJECT);
		props.put(CommonConstants.MESSAGE, builder.toString());

		jobManager.addJob(CommonConstants.SEND_MAIL_TOPIC, props);
	}
}
