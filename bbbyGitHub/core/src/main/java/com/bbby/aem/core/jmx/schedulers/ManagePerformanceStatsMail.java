package com.bbby.aem.core.jmx.schedulers;

import java.lang.management.ManagementFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.sling.api.resource.LoginException;
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

import com.bbby.aem.core.jmx.api.WorkflowMBean;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.Externalizer;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import com.google.gson.annotations.Expose;
/**
 * Manage Workflow
 * 
 * @author BA37483
 *
 */
@Component(service = Runnable.class)
@Designate(ocd = PerformanceStatsMailScheduledTaskConfiguration.class)
public class ManagePerformanceStatsMail implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(ManagePerformanceStatsMail.class);

	@Expose
	private boolean enabled;
	
	@Expose
	private String to;
	
	@Expose
	private String cc;
	
	@Expose
	private String bcc;
	
	@Expose
	private String subject;
	
	@Expose
	private String note;
	
	@Expose
	private String warning;
	
	@Expose
	private String signature;
	
	@Reference
	private ResourceResolverFactory resolverFactory;
	
	@Reference
    private MessageGatewayService messageGatewayService;

	@Reference
	private SlingSettingsService slingSettingsService;

	@Reference
	private Scheduler scheduler;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	Externalizer externalizer;

	@Activate
	protected void activate(PerformanceStatsMailScheduledTaskConfiguration config) throws LoginException {
		this.enabled = config.enabled();
		this.to = config.to();
		this.cc = config.cc();
		this.bcc = config.bcc();
		this.subject = config.subject();
		this.note = config.note();
		this.warning = config.warning();
		this.signature = config.signature();
	}

	/* Execute all the methods */
	String model = "";
	private static MBeanServer mbs = null;

	@Override
	public void run() {

		if (!enabled) {
			return;
		}
		
		if (to != null && !to.trim().isEmpty()) {
			return;
		}

		if (!slingSettingsService.getRunModes().contains("author")) {
			log.warn("Attempt to run from non-author environment");
			return;
		}

		//log.info("**Manage Workflows Process Start at {}", new Timestamp(new Date().getTime()));
		try(ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice")) {
			if (enabled) {
				// Get the MBean server
				mbs = ManagementFactory.getPlatformMBeanServer();
				WorkflowMBean workflowMBean = JMX.newMBeanProxy(mbs, new ObjectName(JMXConstants.OBJECT_NAME_WORKFLOW), WorkflowMBean.class);
				StringBuilder builder = new StringBuilder("<br>");
				
				builder.append("<table border =\"1\"><caption style=\"background-color: #d4d4d4;font-weight: bold;\">S7delivery</caption><tbody> ");
				logCount(new ObjectName(JMXConstants.OBJECT_NAME_S7), "QueueNumEntries", "Queue Number Entries", builder);
				builder.append("</tbody> </table>");
				builder.append("<br>");
				
				builder.append("<table border =\"1\"><caption style=\"background-color: #d4d4d4;font-weight: bold;\">Sling : All Queues</caption><tbody> ");
				ObjectName objectName = new ObjectName(JMXConstants.OBJECT_NAME_SLING_QUEUE);
				logCount(objectName, "NumberOfActiveJobs", "Number Of Active Jobs", builder);
				logCount(objectName, "NumberOfProcessedJobs", "Number Of Processed Jobs", builder);
				logCount(objectName, "NumberOfCancelledJobs", "Number Of Cancelled Jobs", builder);
				logCount(objectName, "NumberOfFailedJobs", "Number Of Failed Jobs", builder);
				logCount(objectName, "NumberOfJobs", "Number Of Jobs", builder);
				builder.append("</tbody></table>");
				builder.append("<br>");
				
				builder.append("<table border =\"1\"><caption style=\"background-color: #d4d4d4;font-weight: bold;\">Session Count</caption><tbody>");
				logCount(new ObjectName(JMXConstants.OBJECT_NAME_JACKRABBIT_OAK_SESSION), "Count", "Session Count", builder);
				builder.append("</tbody></table>");
				builder.append("<br>");
				
				builder.append("<table border =\"1\"><caption style=\"background-color: #d4d4d4;font-weight: bold;\">Consolidated Event Listener statistics</caption><tbody>");
				objectName = new ObjectName(JMXConstants.OBJECT_NAME_JACKRABBIT_OAK_CONSOLIDATEDLISTENERSTATS);
				logCount(objectName, "ObserversCount", "Observers Count", builder);
				logCount(objectName, "ListenersCount", "Listeners Count", builder);
				builder.append("</tbody></table>");
				builder.append("<br>");
				
				builder.append("<table border =\"1\"><caption style=\"background-color: #d4d4d4;font-weight: bold;\">Observation Queue Max Length</caption><tbody>");
				logCount(new ObjectName(JMXConstants.OBJECT_NAME_JACKRABBIT_OAK_OBSERVATION), "Count", "Observation Queue Max Length", builder);
				builder.append("</tbody></table>");
				builder.append("<br>");
				
				builder.append("<table border =\"1\"><caption style=\"background-color: #d4d4d4;font-weight: bold;\">Workflow : Maintenance</caption><tbody>");
				logCount("Stale Workflows Count", workflowMBean.countStaleWorkflows(model), builder);
				logCount("Running Workflows Count", workflowMBean.countRunningWorkflows(model), builder);
				logCount("Completed Workflows Count", workflowMBean.countCompletedWorkflows(model), builder);
				logCount("Failed Workflow Count", workflowMBean.returnFailedWorkflowCount(model), builder);
				builder.append("</tbody></table>");
				builder.append("<br>");
				
				builder.append("<table border =\"1\"><caption style=\"background-color: #d4d4d4;font-weight: bold;\">List Running Workflows Per Model</caption>");
				logTabularData("List Running Workflows Per Model", workflowMBean.listRunningWorkflowsPerModel(),
						builder);
				builder.append("</table>");
				builder.append("<br>");
				
				builder.append("<table border =\"1\"><caption style=\"background-color: #d4d4d4;font-weight: bold;\">List Completed Workflows Per Model</caption>");
				logTabularData("List Completed Workflows Per Model", workflowMBean.listCompletedWorkflowsPerModel(),
						builder);
				builder.append("</table>");
				builder.append("<br>");
				
				builder.append("<table border =\"1\"><caption style=\"background-color: #d4d4d4;font-weight: bold;\">Failed Workflow Count Per Model</caption>");
				logTabularData("Failed Workflow Count Per Model", workflowMBean.returnFailedWorkflowCountPerModel(),
						builder);
				builder.append("</table>");
				builder.append("<br>");
				
				builder.append("<table border =\"1\"><caption style=\"background-color: #d4d4d4;font-weight: bold;\">Oak Query Statistics</caption>");
				TabularData queryStatDtos = (TabularData) mbs.getAttribute(new ObjectName(JMXConstants.OBJECT_NAME_JACKRABBIT_OAK_QUERYSTAT), "SlowQueries");
				logTabularData("Oak Query Statistics", queryStatDtos, builder);
				builder.append("</table>");
				builder.append("<br>");
				//log.info(builder.toString());
				
				sendMail(builder.toString());

			}
			//log.info("**Manage Workflows Process End at {}", new Timestamp(new Date().getTime()));
		} catch (Exception e) {
			log.error("Error Managing Workflows", e.getMessage());

		}
		
	}
	
	public static void logCount(ObjectName objectName, String operation, String title, StringBuilder builder)
			throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
		Object o = mbs.getAttribute(objectName, operation);
		String countstr = "0";
		if (o instanceof Long) {
			Long count = (Long) o;
			countstr = count.toString();
		} else if (o instanceof Integer) {
			Integer count = (Integer) o;
			countstr = count.toString();
		}
		builder.append("<tr> <td>");
		builder.append(title);
		builder.append("</td> <td>");
		builder.append(countstr);
		builder.append("</td> </tr>");
	}
	
	public static void logTabularData(String title, TabularData data, StringBuilder builder) {
		for (Object o : data.values()) {
			CompositeData row = (CompositeData) o;
			CompositeType type = row.getCompositeType();
			builder.append("<thead> <tr>");
			for (Object k : type.keySet()) {
				String key = k.toString();
				builder.append("<th style=\"padding: 5px;\">");
				builder.append(key.toUpperCase());
				builder.append("</th>");
			}
			builder.append("</tr></thead>");
			break;
		}

		for (Object o : data.values()) {
			CompositeData row = (CompositeData) o;
			CompositeType type = row.getCompositeType();
			builder.append(" <tbody> <tr>");
			for (Object k : type.keySet()) {
				String key = k.toString();
				builder.append("<td style=\"padding: 5px;\">");
				builder.append(row.get(key));
				builder.append("</td>");
			}
			builder.append("</tr> </tbody> ");
		}
	}

	public static void logCount(String title, int counter, StringBuilder builder) {
		builder.append("<tr> <td style=\"padding: 5px;\">");
		builder.append(title);
		builder.append("</td> <td style=\"padding: 5px;\">");
		builder.append(counter);
		builder.append("</td> </tr>");
	}
	
	private void sendMail(String message) {

		try {
			MessageGateway<Email> messageGateway;

			Email email = new HtmlEmail();

			email.addTo(to);
			if (cc != null && !cc.trim().isEmpty())
				email.addCc(cc);
			if (bcc != null && !bcc.trim().isEmpty())
				email.addBcc(bcc);
			email.setSubject(subject);

			String msg = "Dear,<p>Below are the Performance stats :</p>";
			if (note != null && !note.trim().isEmpty()) {
				msg = msg + "<p><b>Note :</b>" + note + "</p>";
			}

			msg = msg + message + "<br><b>Best Regards,</b> <br>" + signature;
			if (warning != null && !warning.trim().isEmpty()) {
				msg = msg + "<p><b>Warning :</b>" + warning + "</p>";
			}
			
			email.setMsg(msg);

			// Inject a MessageGateway Service and send the message
			messageGateway = messageGatewayService.getGateway(Email.class);

			// Check the logs to see that messageGateway is not null
			messageGateway.send((Email) email);
		} catch (EmailException e) {
			log.error("Error Managing Workflows", e.getMessage());
		}

	}
	
}
