package com.bbby.aem.core.schedulers;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.Workflow;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.model.WorkflowModel;
import com.google.gson.annotations.Expose;

/**
 * Move Image Sets along with assets in Image Sets from ecomm to approved dam and publish them.
 * DAM-1361
 * @author rgarg
 *
 */
@Component(
    service = Runnable.class
)
@Designate(ocd = ImagesetNightlyPublishScheduledTaskConfiguration.class)
public class ImagesetNightlyPublishScheduledTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final String WORKFLOW_MODEL = "/var/workflow/models/bbby-approve-and-publish-nightly-";
    
    @Expose
    private boolean enabled;
    
    @Expose
    private int daysToRun;
    
    @Expose
    private int numberOfAssets;
    
    @Expose
    private String userEmail;

    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Reference
	private JobManager jobManager;
    
    @Reference
    private SlingSettingsService slingSettingsService;
    
    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    Externalizer externalizer;
    
    private ResourceResolver resourceResolver;
    
    @Reference
    private WorkflowService wfService;
 
    @Activate
    protected void activate(ImagesetNightlyPublishScheduledTaskConfiguration config) throws LoginException {

        this.enabled = config.enabled();
        this.daysToRun = config.daysToRun();
        this.numberOfAssets = config.numberOfAssets();
        this.userEmail = config.userEmail();
        
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
        
        // This condition is added to avoid division being not defined or negative.
        if(numberOfAssets <= 0) {
        	numberOfAssets =1;
        }
          
        try {
        	
			movingImagesetsApprovedDam();
			
		} catch (WCMException e) {
			e.printStackTrace();
			log.error("Error in moving Imagesets from ecomm to approved dam", e.getMessage());
		} finally{
			if(resourceResolver != null)
				resourceResolver.close();
		}
    }
    
	private void movingImagesetsApprovedDam() throws WCMException {
		log.debug("Inside movingImagesetsApprovedDam() method.");

		LocalDateTime today = LocalDateTime.now();
		ZonedDateTime todayZoned = today.atZone(ZoneId.systemDefault());
		String formattedDateToday = todayZoned.format(DateTimeFormatter.ofPattern(CommonConstants.DATE_FORMATTER_FULL).withLocale(Locale.getDefault()));
		
		LocalDateTime before = today.minusDays(daysToRun);
		ZonedDateTime beforeZoned = before.atZone(ZoneId.systemDefault());
		String formattedDateBefore = beforeZoned.format(DateTimeFormatter.ofPattern(CommonConstants.DATE_FORMATTER_FULL).withLocale(Locale.getDefault()));
		
		String queryString = "SELECT * FROM [dam:Asset] AS N WHERE ISDESCENDANTNODE(N,\"/content/dam/bbby/asset_transitions_folder/e-comm\") AND NAME() LIKE '%_imageset' AND N.[jcr:content/reportingmetadata/imagesetNightlyPublishDate] >= CAST('"
				+ formattedDateBefore + "' AS DATE) AND N.[jcr:content/reportingmetadata/imagesetNightlyPublishDate] <= CAST('" + formattedDateToday + "' AS DATE)";

		log.debug("Executing query {}", queryString);
		
		Iterator<Resource> batchNodes = resourceResolver.findResources(queryString, Query.JCR_SQL2);
		
		List<String> batchNodesList = new ArrayList<String>();
		
		Session session = resourceResolver.adaptTo(Session.class);
		int sizeMembers = 0;
		
		while (batchNodes.hasNext()) {
			Resource batchResource = (Resource) batchNodes.next();
			Node node = batchResource.adaptTo(Node.class);
			log.debug("Adding Imageset to the list {}", node);
			try {
				batchNodesList.add(node.getPath());
				ArrayList<String> membersImageset = ServiceUtils.getMembersOfImageset(session,node.getPath());
				sizeMembers = sizeMembers + membersImageset.size();
				log.debug("Member size: {}", sizeMembers);
			} catch (Exception e) {
				log.error("Exception in fetching imageset {}", e.getLocalizedMessage());
			}
		}
		
		for (int i = 0; i < batchNodesList.size(); i++){
			try{
				startWorkflow(batchNodesList.get(i), WORKFLOW_MODEL, "images");
			} catch (Exception e) {
				log.error("Exception processing nightly workflow for images in imageset {}", e.getLocalizedMessage());
			}
		}
		
		int timeCounter = ((sizeMembers/numberOfAssets)+10); //as we have analyzed movement of images at the rate of 70 images per minute.
		log.debug("Sleep time for imagesets to move: {}", timeCounter);
		
		try{
			Thread.sleep(timeCounter * 60 * 1000); // timeCounter min pause
		}catch (Exception e){
			log.error("Exception in thread", e.getLocalizedMessage());
		}
		
		for (int i = 0; i < batchNodesList.size(); i++){
			try{
				startWorkflow(batchNodesList.get(i), WORKFLOW_MODEL, "imagesets");
			} catch (Exception e) {
				log.error("Exception processing nightly workflow for imageset {}", e.getLocalizedMessage());
			}
		}
		
		int timeCounter1 = ((batchNodesList.size()/numberOfAssets)+5); //as we have analyzed movement of imagesets at the rate of 70 imagesets per minute.
		log.debug("Sleep time for email to trigger after images and imagesets movement: {}", timeCounter1);
		
		try{
			Thread.sleep(timeCounter1 * 60 * 1000); // timeCounter1 min pause
		}catch (Exception e){
			log.error("Exception in thread", e.getLocalizedMessage());
		}
		
		try {
			generateImagesetReport(batchNodesList, queryString);
		} catch (WCMException e) {
			e.printStackTrace();
			log.error("Error", e.getMessage());
		}
		
	}
	
	private void startWorkflow(String payload, String WORKFLOW_MODEL, String value) throws RepositoryException, LoginException, WorkflowException {

        try(ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "workflow-service")) {

        	Session session = resourceResolver.adaptTo(Session.class);
            WorkflowSession wfSession = wfService.getWorkflowSession(session);

            WorkflowModel model = wfSession.getModel(WORKFLOW_MODEL);
            WorkflowData data = wfSession.newWorkflowData(CommonConstants.JCR_PATH, payload);
            String moveImages = "";
            if(value.equals("images")){
            	moveImages = "moveImages";
            	data.getMetaDataMap().put("moveImages", moveImages);
            } else {
            	moveImages = "moveImageset";
            	data.getMetaDataMap().put("moveImages", moveImages);
            }
            Workflow workflow = wfSession.startWorkflow(model, data);
            
            if(workflow != null) {
                log.info("Successfully started workflow {}", workflow.getId());
            }
        } catch (Exception e) {
            log.error("Failed to start workflow", e);
            throw new WorkflowException(e.getMessage(), e);
            
        }
    }
	
	private void generateImagesetReport(List<String> batchNodesList, String queryString) throws WCMException {
		log.debug("enter in generateImagesetReport()");
		String crdate1 = ServiceUtils.getCurrentDateStr("MM-dd-yyyy");
		List<String> batchNodesListUnsuccessful = new ArrayList<String>();
		File file = new File("csv_reports/Imagesets");
		file.mkdirs();
		try {
			FileWriter writer = new FileWriter("csv_reports/Imagesets/imageset_report_" + crdate1 + ".csv");
			FileWriter writerUnsuccessful = new FileWriter("csv_reports/Imagesets/imageset_unsuccessful_report_" + crdate1 + ".csv");

			CSVUtils.writeLine(writer, getHeaderList());
			CSVUtils.writeLine(writerUnsuccessful, getHeaderList());

			List<String> propertyList = null;
			for (int i = 0; i < batchNodesList.size(); i++){
				propertyList = new ArrayList<String>();
				try {
					propertyList.add(batchNodesList.get(i));
					CSVUtils.writeLine(writer, propertyList);

				} catch (Exception e) {
					log.error("Error in creating imageset report" + e);
				}

			}
			
			Iterator<Resource> batchNodesUnsuccessful = resourceResolver.findResources(queryString, Query.JCR_SQL2);
			List<String> propertyListUnsucceessful = null;
			
			while (batchNodesUnsuccessful.hasNext()) {
				Resource batchResource = (Resource) batchNodesUnsuccessful.next();
				Node node = batchResource.adaptTo(Node.class);
				propertyListUnsucceessful = new ArrayList<String>();
				try {
					propertyListUnsucceessful.add(node.getPath());
					batchNodesListUnsuccessful.add(node.getPath());
					CSVUtils.writeLine(writerUnsuccessful, propertyListUnsucceessful);
				} catch (Exception e) {
					log.error("Exception in creating report for unmoved imagesets" + e.getLocalizedMessage());
				}
			}

			writer.close();
			writerUnsuccessful.close();

		} catch (Exception e) {
			log.error("Error" + e);
		}

		// mail body start
		StringBuilder builder = new StringBuilder("<br>");

		builder.append(
				"<h2 style=\"text-align: center;\">Imagesets movement Report: To identify whether imagesets has been moved to the destination path.</h2>");
		builder.append("<table border='1' style='border-collapse:collapse'>");
		builder.append("<tbody>");
		builder.append("<tr>");
		builder.append("<td><b>Movement Date: </b></td>");
		builder.append("<td>" + crdate1 + "</td>");

		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Total Imagesets: </b></td>");
		builder.append("<td>" + batchNodesList.size() + "</td>");

		builder.append("</tr>");
		builder.append("<tr>");
		builder.append("<td><b>Unsuccessful Imagesets: </b></td>");
		builder.append("<td>" + batchNodesListUnsuccessful.size() + "</td>");
		builder.append("</tr>");
		
		builder.append("</tbody>");
		builder.append("</table>");

		builder.append("<br>");
		builder.append("<br>");

		builder.append("<p>Please contact your Bed Bath & Beyond Inc. representative for follow-up.</p>");
		builder.append("<p>Thank You,<br>Bed Bath & Beyond, Inc.</p>");
		builder.append("<br>");

		sendMailWithCSV("csv_reports/Imagesets/imageset_report_" + crdate1 + ".csv", "csv_reports/Imagesets/imageset_unsuccessful_report_" + crdate1 + ".csv", builder, crdate1);

	}
	
	private static List<String> getHeaderList() {
		List<String> headerNameList = new ArrayList<String>();
		headerNameList.add("ASSET_PATH");	
		return headerNameList;
	}
	
	private void sendMailWithCSV(String filename, String filename1, StringBuilder builder, String crdate1) throws WCMException {
		String MAIL_SUBJECT = ServiceUtils.getHostName() + ": Daily Report On Imagesets (" + crdate1 + ")";

		final Map<String, Object> props = new HashMap<String, Object>();
		log.info("Queeing the job for sending mail to vendor " + userEmail);

		props.put(CommonConstants.TO, userEmail);
		props.put(CommonConstants.SUBJECT, MAIL_SUBJECT);
		props.put(CommonConstants.MESSAGE, builder.toString());
		props.put(CommonConstants.FILENAME, filename);
		props.put(CommonConstants.FILENAME1, filename1);

		jobManager.addJob(CommonConstants.SEND_MAIL_TOPIC, props);
	}
	
}
