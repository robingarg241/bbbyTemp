package com.bbby.aem.core.schedulers;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.apache.sling.api.resource.LoginException;
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

import com.bbby.aem.core.util.CSVUtils;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.WCMException;
import com.google.gson.annotations.Expose;



/**
 * Create Report on NOT Published Assets in Approved DAM.
 *
 */
@Component(
    service = Runnable.class
)
@Designate(ocd = DailyReportVAHAppDamScheduledTaskConfiguration.class)
public class DailyPublishedAssetsTask implements Runnable {

	private final Logger log = LoggerFactory.getLogger(DailyPublishedAssetsTask.class);
    
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
        
        if(!enabled) {
            return;
        }
          
        try {
        	genrateADAMReport();
		} catch (WCMException e) {
			e.printStackTrace();
			log.error("Error", e.getMessage());
		}
    }
    
	private void genrateADAMReport() throws WCMException {
		log.debug("enter in genrateADAMReport()");
		String crdate1 = ServiceUtils.getCurrentDateStr("MM-dd-yyyy");
		int count = 0;
		File file = new File("csv_reports/Approved_DAM");
		file.mkdirs();
		String filename = "csv_reports/Approved_DAM/dam_not_publish_" + crdate1 + ".csv";
		try {
			FileWriter writer = new FileWriter(filename);
			CSVUtils.writeLine(writer, getHeaderList());

			String queryString = "SELECT * FROM [dam:Asset] AS N WHERE ISDESCENDANTNODE(N,\"/content/dam/bbby/approved_dam\") AND (N.[jcr:created] > CAST('2020-09-01T00:00:00.000-04:00' AS DATE)) AND (N.[jcr:content/metadata/dam:scene7FileStatus] <> \"PublishComplete\")";
			log.info("Executing query {}", queryString);
			Iterator<Resource> batchNodes = resourceResolver.findResources(queryString, Query.JCR_SQL2);
			List<String> propertyList = null;
			while (batchNodes.hasNext()) {
				propertyList = new ArrayList<String>();
				Resource batchResource = (Resource) batchNodes.next();
				Node node = batchResource.adaptTo(Node.class);
				try {
					String createddate = node.getProperty("jcr:created").getString();
					Node meta = node.getNode(CommonConstants.METADATA_NODE);
					log.info("pathm...." + meta.getPath());
					String damstat = meta.getProperty("dam:scene7FileStatus").getString();
					String exdate = "";
					String entdate = "";
					if (node.hasNode(CommonConstants.OPERATIONAL_METADATA_NODE)) {

						Node opera = node.getNode(CommonConstants.OPERATIONAL_METADATA_NODE);

						if (opera.hasProperty(CommonConstants.OPMETA_PC_Executed_Date)) {

							exdate = opera.getProperty(CommonConstants.OPMETA_PC_Executed_Date).getString();

						}

					}

					if (node.hasNode(CommonConstants.REPORTING_METADATA_NODE)) {
						Node repo = node.getNode(CommonConstants.REPORTING_METADATA_NODE);
						log.info("node path" + repo.getPath());
						if (repo.hasProperty(CommonConstants.ECOMM_ENTRY_DATE)) {
							entdate = repo.getProperty(CommonConstants.ECOMM_ENTRY_DATE).getString();

						}

					}
					propertyList.add(node.getPath());
					propertyList.add(damstat);
					propertyList.add(createddate);
					propertyList.add(exdate);
					propertyList.add(entdate);

					CSVUtils.writeLine(writer, propertyList);

				} catch (RepositoryException e) {
					log.error("Error" + e);
				}
				count++;
			}

			writer.close();

		} catch (Exception e) {
			log.error("Error" + e);
		}

		crateEmailBodyAndSendMail(filename, crdate1, count);
	}

	
	private void crateEmailBodyAndSendMail(String filename, String crdate1, int count) throws WCMException {
		StringBuilder builder = new StringBuilder("<br>");

		builder.append("<h2 style=\"text-align: center;\">Report: NOT Published Assets in Approved DAM</h2>");
		builder.append("<table border='1' style='border-collapse:collapse'>");
		builder.append("<tbody>");
		builder.append("<tr>");
		builder.append("<td><b>Created Date :</b></td>");
		builder.append("<td>" + crdate1 + "</td>");
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Not in Publish State Count :</b></td>");
		builder.append("<td>" + count + "</td>");

		builder.append("</tbody>");
		builder.append("</table>");

		builder.append("<br>");
		builder.append("<br>");

		builder.append("<p>Please contact your Bed Bath & Beyond Inc. representative for follow-up.</p>");
		builder.append("<p>Thank You,<br>Bed Bath & Beyond, Inc.</p>");
		builder.append("<br>");

		sendMailWithCsv(filename, crdate1, builder);

	}
	
	private void sendMailWithCsv(String filename, String crdate1, StringBuilder builder) throws WCMException {
		final Map<String, Object> props = new HashMap<String, Object>();
		String userName = "dam-it-support@bedbath.com";
		log.info("Queeing the job for sending mail to vendor " + userName);
		String MAIL_SUBJECT = ServiceUtils.getHostName() + ": Daily Report on NOT Published Assets in Approved DAM (" + crdate1 + ")";
		props.put(CommonConstants.TO, userName);
		// props.put(CommonConstants.CC, "");
		// props.put(CommonConstants.BCC, "");
		props.put(CommonConstants.SUBJECT, MAIL_SUBJECT);
		props.put(CommonConstants.MESSAGE, builder.toString());
		props.put(CommonConstants.FILENAME, filename);

		jobManager.addJob(CommonConstants.SEND_MAIL_TOPIC, props);
	}
	
	private static List<String> getHeaderList() {
		List<String> headerNameList = new ArrayList<String>();
		headerNameList.add("ASSET_PATH");
		headerNameList.add("Scene7 Status");
		headerNameList.add("CREATED DATE");
		headerNameList.add("PC EXECUTED DATE");
		headerNameList.add("ECOMM ENTRY DATE");
		return headerNameList;
	}

}
