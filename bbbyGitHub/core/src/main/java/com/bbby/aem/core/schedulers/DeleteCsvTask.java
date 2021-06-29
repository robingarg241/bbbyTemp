package com.bbby.aem.core.schedulers;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.sling.api.resource.LoginException;
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

import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.WCMException;
import com.google.gson.annotations.Expose;

/**
 * 
 *
 */
@Component(service = Runnable.class)
@Designate(ocd = DailyReportVAHAppDamScheduledTaskConfiguration.class)
public class DeleteCsvTask implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(DeleteCsvTask.class);

	@Expose
	private boolean enabled;

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private SlingSettingsService slingSettingsService;
	@Reference
	private JobManager jobManager;
	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	Externalizer externalizer;

	private ResourceResolver resourceResolver;

	@Activate
	protected void activate(DailyReportVAHAppDamScheduledTaskConfiguration config) throws LoginException {

		this.enabled = config.enabled();

		resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice");
	}

	@Override
	public void run() {

		if (!slingSettingsService.getRunModes().contains("author")) {
			log.warn("Attempt to run from non-author environment");
			return;
		}

		if (!enabled) {
			return;
		}

		try {

			deleteCSV();

		} catch (WCMException e) {
			e.printStackTrace();
			log.error("Error", e.getMessage());
		}
	}

	private void deleteCSV() throws WCMException {
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ss'Z'");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		Date todate1 = cal.getTime();
		String fromdate = dateFormat.format(todate1);
		String deldate = fromdate.substring(0, 10);
		File[] files = new File("csv_reports").listFiles();
		showFiles(files, deldate);
	}

	private static void showFiles(File[] files, String deldate) {
		for (File file : files) {
			if (file.isDirectory()) {
				showFiles(file.listFiles(), deldate); // Calls same method
														// again.
			} else {
				String fileName = file.getName();
				if (fileName.contains(deldate)) {
					file.delete();
					log.info("Deletion successful.");
				} else {
					log.info("file with specific date not found.");
				}
			}
		}
	}
}
