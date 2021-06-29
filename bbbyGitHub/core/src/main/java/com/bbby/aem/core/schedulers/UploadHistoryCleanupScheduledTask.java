package com.bbby.aem.core.schedulers;

import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.google.gson.annotations.Expose;
import org.apache.sling.api.resource.LoginException;
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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;

/**
 * Deletes old upload history nodes
 * 
 * @author vpokotylo
 *
 */
@Component(
    service = Runnable.class
)
@Designate(ocd = UploadHistoryCleanupScheduledTaskConfiguration.class)
public class UploadHistoryCleanupScheduledTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Expose
    private boolean enabled;
    
    @Expose
    private int daysTooKeep;

    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Reference
    private SlingSettingsService slingSettingsService;
    
    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    Externalizer externalizer;
    
    private ResourceResolver resourceResolver;
    
    @Activate
    protected void activate(UploadHistoryCleanupScheduledTaskConfiguration config) throws LoginException {

        this.enabled = config.enabled();
        this.daysTooKeep = config.daysTooKeep();
        
        resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice");
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
          
        try {
        	
			cleanupHistory();
			
		} catch (WCMException e) {
			e.printStackTrace();
			log.error("Error removing upload history", e.getMessage());
		} finally{
			if(resourceResolver != null)
				resourceResolver.close();
		}
    }
    
	private void cleanupHistory() throws WCMException {
		log.debug("Cleaning upload history");

		PageManager pageManager = (PageManager) resourceResolver.adaptTo(PageManager.class);

		LocalDateTime today = LocalDateTime.now();
		today = today.minusDays(daysTooKeep);

		ZonedDateTime todayZoned = today.atZone(ZoneId.systemDefault());
		String formattedDate = todayZoned.format(DateTimeFormatter.ofPattern(CommonConstants.DATE_FORMATTER_FULL).withLocale(Locale.getDefault()));

		String queryString = MessageFormat.format(
            "SELECT * FROM [cq:Page] AS N WHERE ISDESCENDANTNODE(N,\"/var/vendor\") AND N.[jcr:content/sling:resourceType]=\"bbby/components/structure/pageUploadBatch\" AND N.[jcr:created] < CAST(\"{0}\" AS DATE)",
				new Object[] { formattedDate });

		log.debug("Executing query {}", queryString);

		int count = 0;
		Iterator<Resource> batchNodes = resourceResolver.findResources(queryString, Query.JCR_SQL2);
		while (batchNodes.hasNext()) {
			Resource batchResource = (Resource) batchNodes.next();
			log.debug("Removing batch upload page at {}", batchResource.getPath());
			
			pageManager.delete((Page) batchResource.adaptTo(Page.class), false, false);
			count++;
		}

		Session session = resourceResolver.adaptTo(Session.class);
		
		try {
			session.save();
			
		} catch (RepositoryException e) {
			log.error("Repository save error ", e.getLocalizedMessage());
		}
		log.debug("Deleted {} batch upload pages", count);
	}
}
