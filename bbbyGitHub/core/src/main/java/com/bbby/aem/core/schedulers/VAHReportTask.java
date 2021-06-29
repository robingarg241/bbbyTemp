package com.bbby.aem.core.schedulers;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
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
 * VAH Assets Report for No UPC or No Shot_Type Or No Asset_Type Tag
 *
 */
@Component(service = Runnable.class)
@Designate(ocd = DailyReportVAHAppDamScheduledTaskConfiguration.class)
public class VAHReportTask implements Runnable {

	private final Logger log = LoggerFactory.getLogger(VAHReportTask.class);

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
			generateVAHReport();

		} catch (WCMException e) {
			e.printStackTrace();
			log.error("Error", e.getMessage());
		}
	}

	private void generateVAHReport() throws WCMException {
		log.info("enter in generateVAHReport()");
		String crdate1 = ServiceUtils.getCurrentDateStr("MM-dd-yyyy");

		List<String> datearray = new ArrayList<String>();
		List<String> upcaaray = new ArrayList<String>();
		List<String> noassetarray = new ArrayList<String>();
		List<String> noshotarray = new ArrayList<String>();
		List<String> noassetshotaaray = new ArrayList<String>();
		List<String> novendordata = new ArrayList<String>();

		File file = new File("csv_reports/VAH");
		file.mkdirs();
		try {
			FileWriter writer = new FileWriter("csv_reports/VAH/vah_report_" + crdate1 + ".csv");

			CSVUtils.writeLine(writer, getHeaderList());

			LocalDateTime currentDate = LocalDateTime.now();
			
			// DAM:1561 - Added the milliseconds to date if its 000.
			if(!currentDate.toString().contains(".")) {
				String correctDate = currentDate.toString()+".001";
				currentDate = LocalDateTime.parse(correctDate);
				log.info("Date after adding ms.."+currentDate);
			}
			for (int i = 1; i < 4; i++) {
				datearray.add(currentDate.minusDays(i).toString().substring(5, 10)+"-"+currentDate.minusDays(i).toString().substring(0, 4));
				int blankUpcCount = 0;
				int noAssetTypeCount = 0;
				int noShotTypeCount = 0;
				int noAssetAndShotTypeCount = 0;
				int noMetadataCount = 0;
				Iterator<Resource> batchNodes = executeQuery(currentDate.minusDays(i), currentDate.minusDays(i - 1));
				List<String> propertyList = null;
				StringBuilder tagval = null;
				while (batchNodes.hasNext()) {
					propertyList = new ArrayList<String>();
					tagval = new StringBuilder();
					Resource batchResource = (Resource) batchNodes.next();
					Node node = batchResource.adaptTo(Node.class);
					try {
						String createdDate = node.getProperty("jcr:created").getString();
						Node metadataNode = node.getNode(CommonConstants.METADATA_NODE);
						log.info("Metadata Node Path...." + metadataNode.getPath());
						String assetupc = ServiceUtils.getMetadataValue(batchResource, CommonConstants.BBBY_UPC, null);
						if (assetupc != null && assetupc.contains(",")) {
							assetupc = assetupc.replaceAll(",", "|");
						}
						log.info("assetupc..." + assetupc);

						boolean hasAssetType = ServiceUtils.startsWithTag(node, CommonConstants.BBBY_ASSET_TYPE);
						boolean hasShotType = ServiceUtils.startsWithTag(node, CommonConstants.BBBY_SHOT_TYPE);
						String vendorEmail = ServiceUtils.getMetadataValue(batchResource,"bbby:vendorEmail", null);
						String vendorPorId = ServiceUtils.getMetadataValue(batchResource,"bbby:vendorPortalID", null);
						

						if (metadataNode.hasProperty(CommonConstants.CQ_TAGS)) {
							Property Prop = metadataNode.getProperty("cq:tags");
							Value[] values = Prop.getValues();
							if (values != null) {
								for (Value val : values) {
									String tag = val.getString();
									tagval.append(tag + "|");
								}
							}
						}
						if (tagval.length() > 0) {
							tagval.setLength(tagval.length() - 1);
						}
						propertyList.add(node != null ? node.getPath() : "");
						propertyList.add(createdDate != null ? createdDate : "");
						propertyList.add(assetupc != null ? "Yes" : "No");
						propertyList.add(hasAssetType ? "Yes" : "No");
						propertyList.add(hasShotType ? "Yes" : "No");
						propertyList.add(assetupc != null && hasAssetType && hasShotType ? "Yes" : "No");
						propertyList.add(assetupc != null ? assetupc : "");
						propertyList.add(vendorEmail != null ? vendorEmail : "");
						propertyList.add(vendorPorId != null ? vendorPorId : "");
						propertyList.add(tagval != null ? tagval.toString() : "");

						CSVUtils.writeLine(writer, propertyList);

						if (assetupc == null) {
							blankUpcCount++;
						}

						if (!hasAssetType) {
							noAssetTypeCount++;
						}
						if (!hasShotType) {
							noShotTypeCount++;
						}

						if (!hasShotType & !hasAssetType) {
							noAssetAndShotTypeCount++;
						}
						if (!hasShotType & !hasAssetType & assetupc == null) {
							noMetadataCount++;
						}

					} catch (RepositoryException e) {
						log.error("Error" + e);
					}

				}
		
				upcaaray.add(Integer.toString(blankUpcCount));
				noassetarray.add(Integer.toString(noAssetTypeCount));
				noshotarray.add(Integer.toString(noShotTypeCount));
				noassetshotaaray.add(Integer.toString(noAssetAndShotTypeCount));
				novendordata.add(Integer.toString(noMetadataCount));

			}

			writer.close();

		} catch (Exception e) {
			log.error("Error" + e);
		}

		// mail body start
		StringBuilder builder = new StringBuilder("<br>");

		builder.append(
				"<h2 style=\"text-align: center;\">VAH Assets Report: To identify Asset Type and Shot Type Issues</h2>");
		builder.append("<table border='1' style='border-collapse:collapse'>");
		builder.append("<tbody>");
		builder.append("<tr>");
		builder.append("<td><b>Created Date: </b></td>");
		for (int counter = 0; counter < datearray.size(); counter++) {
			builder.append("<td>" + datearray.get(counter) + "</td>");
		}

		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>No UPC Count: </b></td>");
		for (int counter = 0; counter < upcaaray.size(); counter++) {
			builder.append("<td>" + upcaaray.get(counter) + "</td>");
		}

		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>No Asset Type count: </b></td>");
		for (int counter = 0; counter < noassetarray.size(); counter++) {
			builder.append("<td>" + noassetarray.get(counter) + "</td>");
		}
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>No Shot Type Count : </b></td>");
		for (int counter = 0; counter < noshotarray.size(); counter++) {
			builder.append("<td>" + noshotarray.get(counter) + "</td>");
		}
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>No Asset and Shot Type Count: </b></td>");
		for (int counter = 0; counter < noassetshotaaray.size(); counter++) {
			builder.append("<td>" + noassetshotaaray.get(counter) + "</td>");
		}
		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>No Vendor Metadata : </b></td>");
		for (int counter = 0; counter < novendordata.size(); counter++) {
			builder.append("<td>" + novendordata.get(counter) + "</td>");
		}
		builder.append("</tr>");

		builder.append("</tbody>");
		builder.append("</table>");

		builder.append("<br>");
		builder.append("<br>");

		builder.append("<p>Please contact your Bed Bath & Beyond Inc. representative for follow-up.</p>");
		builder.append("<p>Thank You,<br>Bed Bath & Beyond, Inc.</p>");
		builder.append("<br>");

		sendMailWithCSV("csv_reports/VAH/vah_report_" + crdate1 + ".csv", builder, crdate1);

	}

	private Iterator<Resource> executeQuery(LocalDateTime fromDate, LocalDateTime toDate) throws Exception {
		Iterator<Resource> batchNodes = null;
		String queryString = "SELECT * FROM [dam:Asset] AS N WHERE ISDESCENDANTNODE(N,\"/content/dam/bbby/asset_transitions_folder/vendor/vendor_assets_holding\") AND N.[jcr:created] >= CAST('"
				+ fromDate + "' AS DATE) AND N.[jcr:created] <= CAST('" + toDate + "' AS DATE)";
		log.info("Executing query..." + queryString);
		batchNodes = resourceResolver.findResources(queryString, Query.JCR_SQL2);
		if(batchNodes==null) {
		log.info("Query results are Null");
		}
		return batchNodes;
	}

	private static List<String> getHeaderList() {
		List<String> headerNameList = new ArrayList<String>();
		headerNameList.add("ASSET_PATH");
		headerNameList.add("CREATED DATE");
		headerNameList.add("HAS PRIMARY UPC");
		headerNameList.add("HAS ASSET TYPE");
		headerNameList.add("HAS SHOT TYPE");
		headerNameList.add("HAS METADATA");
		headerNameList.add("PRIMARY UPC");
		headerNameList.add("VENDOR E-MAIL");
		headerNameList.add("VENDOR PORTAL ID");
		headerNameList.add("CQ TAGS");
		
		return headerNameList;
	}

	private void sendMailWithCSV(String filename, StringBuilder builder, String crdate1) throws WCMException {
		String userName = "dam-it-support@bedbath.com";
		String MAIL_SUBJECT = ServiceUtils.getHostName() + ": Daily Report On VAH Assets (" + crdate1 + ")";

		final Map<String, Object> props = new HashMap<String, Object>();
		log.info("Queeing the job for sending mail to vendor " + userName);

		props.put(CommonConstants.TO, userName);
		props.put(CommonConstants.SUBJECT, MAIL_SUBJECT);
		props.put(CommonConstants.MESSAGE, builder.toString());
		props.put("filename", filename);

		jobManager.addJob(CommonConstants.SEND_MAIL_TOPIC, props);
	}
}
