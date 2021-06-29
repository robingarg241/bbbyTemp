package com.bbby.aem.core.schedulers;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.Scheduler;
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
 * Deletes empty folders in DAM
 * 
 * @author BA37483
 *
 */
@Component(service = Runnable.class)
@Designate(ocd = EmptyDirectoryCleanupScheduledTaskConfiguration.class)
public class EmptyDirectoryCleanupScheduledTask implements Runnable {

	private final Logger log = LoggerFactory.getLogger(EmptyDirectoryCleanupScheduledTask.class);

	@Expose
	private boolean enabledHolding;

	@Expose
	private boolean enabledEComm;

	@Expose
	private int daysTooKeepHolding;

	@Expose
	private int daysTooKeepEComm;
	
	@Expose
	private int maxNoOfFolders;

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private SlingSettingsService slingSettingsService;
	
    @Reference
    private Scheduler scheduler ; 

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	Externalizer externalizer;

	@Activate
	protected void activate(EmptyDirectoryCleanupScheduledTaskConfiguration config) throws LoginException {

		this.enabledHolding = config.enabledHolding();
		this.daysTooKeepHolding = config.daysTooKeepHolding();
		this.enabledEComm = config.enabledEComm();
		this.daysTooKeepEComm = config.daysTooKeepEComm();
		this.maxNoOfFolders = config.maxNoOfFolders();
	}

	@Override
	public void run() {

		if (!slingSettingsService.getRunModes().contains("author")) {
			log.warn("Attempt to run from non-author environment");
			return;
		}

		if (!enabledHolding && !enabledEComm) {
			return;
		}
		
		log.info("**Empty Folder Deletion Process Start at {}", new Timestamp(new Date().getTime()));
		try (ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice")) {
			if (enabledHolding)
				cleanupHoldingFolders(resourceResolver);

			if (enabledEComm)
				cleanupECommFolders(resourceResolver);

			Session session = resourceResolver.adaptTo(Session.class);
			ServiceUtils.saveSession(session);
			log.info("**Empty Folder Deletion Process End at {}", new Timestamp(new Date().getTime()));
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error removing empty folders", e.getMessage());

		}
	}

	private void cleanupHoldingFolders(ResourceResolver resourceResolver) throws WCMException, PersistenceException {

		LocalDateTime today = LocalDateTime.now();
		today = today.minusDays(daysTooKeepHolding);

		ZonedDateTime todayZoned = today.atZone(ZoneId.systemDefault());
		String formattedDate = todayZoned
				.format(DateTimeFormatter.ofPattern(CommonConstants.DATE_FORMATTER_FULL).withLocale(Locale.getDefault()));

		String queryString = MessageFormat.format(
				"SELECT * FROM [sling:Folder] AS N WHERE ISDESCENDANTNODE(N,\"/content/dam/bbby/asset_transitions_folder/vendor\") AND N.[jcr:created] < CAST(\"{0}\" AS DATE)",
				new Object[] { formattedDate });

		log.debug("Executing query {}", queryString);

		int count = 0;
		Iterator<Resource> folderResources = resourceResolver.findResources(queryString, Query.JCR_SQL2);
		while (folderResources.hasNext()) {
			
			//check it should not delete more than max no. of folders.
			if(count >= maxNoOfFolders){
				break;
			}
			
			Resource folderResource = (Resource) folderResources.next();
			String path = folderResource.getPath();
			log.debug("Checking Folder at {}", path);
			boolean checkChild = checkChildInRejects(path);
			if (checkChild) {
				final Iterator<Resource> itr = folderResource.listChildren();
				boolean empty = true;

			while (itr.hasNext()) {
				Resource resource = itr.next();
				if (!resource.getName().equals(CommonConstants.JCR_CONTENT)) {
					empty = false;
					break;
				}
			}

			if (empty) {
				log.info("Removing Asset Holding empty Folder at {}", folderResource.getPath());
				resourceResolver.delete(folderResource);

				count++;
			}
			}
		}

		log.info("Deleted {} Holding empty folders", count);
	}

	private void cleanupECommFolders(ResourceResolver resourceResolver) throws WCMException, PersistenceException {

		LocalDateTime today = LocalDateTime.now();
		today = today.minusDays(daysTooKeepEComm);

		ZonedDateTime todayZoned = today.atZone(ZoneId.systemDefault());
		String formattedDate = todayZoned
				.format(DateTimeFormatter.ofPattern(CommonConstants.DATE_FORMATTER_FULL).withLocale(Locale.getDefault()));

		String queryString = MessageFormat.format(
				"SELECT * FROM [sling:Folder] AS N WHERE ISDESCENDANTNODE(N,\"/content/dam/bbby/asset_transitions_folder/e-comm\") AND N.[jcr:created] < CAST(\"{0}\" AS DATE)",
				new Object[] { formattedDate });

		log.debug("Executing query {}", queryString);

		int count = 0;
		Iterator<Resource> folderResources = resourceResolver.findResources(queryString, Query.JCR_SQL2);
		while (folderResources.hasNext()) {
			
			//check it should not delete more than max no. of folders.
			if(count >= maxNoOfFolders){
				break;
			}
			
			Resource folderResource = (Resource) folderResources.next();
			
			String path = folderResource.getPath();
			log.debug("Checking Folder at {}", path);
			boolean checkChild = false;
			if(path.contains("/rejects_folder")){
				checkChild = checkChildInRejects(path);
			}
			else{
				checkChild = checkChild(path);
			}
			
			if (checkChild) {
				final Iterator<Resource> itr = folderResource.listChildren();
				boolean empty = true;

				while (itr.hasNext()) {
					Resource resource = itr.next();
					if (!resource.getName().equals(CommonConstants.JCR_CONTENT)) {
						empty = false;
						break;
					}
				}

				if (empty) {
					log.info("Removing E-Comm empty Folder at {}", folderResource.getPath());
					resourceResolver.delete(folderResource);

					count++;
				}
			}
		}

		log.info("Deleted {} E-Comm empty folders", count);
	}
	
	private boolean checkChild(String path){
		boolean checkChild = false;
		if (path != null && path.contains("/")) {
			String[] arr = path.split("/");
			if (path.startsWith("/") && arr.length == 9) {
				checkChild = true;
			} else if(!path.startsWith("/") && arr.length == 8) {
				checkChild = true;
			} else{
				checkChild = false;
			}
		}
		return checkChild;
	}
	
	private boolean checkChildInRejects(String path){
		boolean checkChild = false;
		if (path != null && path.contains("/")) {
			String[] arr = path.split("/");
			if (path.startsWith("/") && arr.length > 7) {
				checkChild = true;
			} else if(!path.startsWith("/") && arr.length > 6) {
				checkChild = true;
			} else{
				checkChild = false;
			}
		}
		return checkChild;
	}
}
