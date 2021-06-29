package com.bbby.aem.core.jmx.schedulers;

import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.Date;

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
import com.day.cq.mailer.MessageGatewayService;
import com.google.gson.annotations.Expose;
/**
 * Manage Workflow
 * 
 * @author BA37483
 *
 */
@Component(service = Runnable.class)
@Designate(ocd = PerformanceStatsScheduledTaskConfiguration.class)
public class ManagePerformanceStats implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(ManagePerformanceStats.class);

	@Expose
	private boolean enabled;

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
	protected void activate(PerformanceStatsScheduledTaskConfiguration config) throws LoginException {
		this.enabled = config.enabled();
	}

	/* Execute all the methods */
	String model = "";
	private static MBeanServer mbs = null;

	@Override
	public void run() {

		if (!enabled) {
			return;
		}

		if (!slingSettingsService.getRunModes().contains("author")) {
			log.warn("Attempt to run from non-author environment");
			return;
		}

		log.info("**Manage Workflows Process Start at {}", new Timestamp(new Date().getTime()));
		try(ResourceResolver resourceResolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice")) {
			if (enabled) {
				// Get the MBean server
				mbs = ManagementFactory.getPlatformMBeanServer();
				WorkflowMBean workflowMBean = JMX.newMBeanProxy(mbs, new ObjectName(JMXConstants.OBJECT_NAME_WORKFLOW), WorkflowMBean.class);
				StringBuilder builder = new StringBuilder("\n");
				
				builder.append("--------------S7delivery--------------\n");
				logCount(new ObjectName(JMXConstants.OBJECT_NAME_S7), "QueueNumEntries", "Queue Number Entries", builder);
				
				builder.append("--------------Sling : All Queues--------------\n");
				ObjectName objectName = new ObjectName(JMXConstants.OBJECT_NAME_SLING_QUEUE);
				logCount(objectName, "NumberOfActiveJobs", "Number Of Active Jobs", builder);
				logCount(objectName, "NumberOfProcessedJobs", "Number Of Processed Jobs", builder);
				logCount(objectName, "NumberOfCancelledJobs", "Number Of Cancelled Jobs", builder);
				logCount(objectName, "NumberOfFailedJobs", "Number Of Failed Jobs", builder);
				logCount(objectName, "NumberOfJobs", "Number Of Jobs", builder);

				builder.append("--------------Session Count--------------\n");
				logCount(new ObjectName(JMXConstants.OBJECT_NAME_JACKRABBIT_OAK_SESSION), "Count", "Session Count", builder);
				
				builder.append("--------------Consolidated Event Listener statistics--------------\n");
				objectName = new ObjectName(JMXConstants.OBJECT_NAME_JACKRABBIT_OAK_CONSOLIDATEDLISTENERSTATS);
				logCount(objectName, "ObserversCount", "Observers Count", builder);
				logCount(objectName, "ListenersCount", "Listeners Count", builder);
				
				builder.append("--------------Observation Queue Max Length--------------\n");
				logCount(new ObjectName(JMXConstants.OBJECT_NAME_JACKRABBIT_OAK_OBSERVATION), "Count", "Observation Queue Max Length", builder);

				builder.append("--------------Workflow : Maintenance--------------\n");
				logCount("Stale Workflows Count", workflowMBean.countStaleWorkflows(model), builder);
				logCount("Running Workflows Count", workflowMBean.countRunningWorkflows(model), builder);
				logCount("Completed Workflows Count", workflowMBean.countCompletedWorkflows(model), builder);
				logCount("Failed Workflow Count", workflowMBean.returnFailedWorkflowCount(model), builder);
				logTabularData("List Running Workflows Per Model", workflowMBean.listRunningWorkflowsPerModel(),
						builder);
				logTabularData("List Completed Workflows Per Model", workflowMBean.listCompletedWorkflowsPerModel(),
						builder);
				logTabularData("Failed Workflow Count Per Model", workflowMBean.returnFailedWorkflowCountPerModel(),
						builder);

				builder.append("--------------Oak Query Statistics--------------\n");
				TabularData queryStatDtos = (TabularData) mbs.getAttribute(new ObjectName(JMXConstants.OBJECT_NAME_JACKRABBIT_OAK_QUERYSTAT), "SlowQueries");
				logTabularData("Oak Query Statistics", queryStatDtos, builder);
				log.info(builder.toString());

			}
			log.info("**Manage Workflows Process End at {}", new Timestamp(new Date().getTime()));
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
		builder.append("**");
		builder.append(title);
		builder.append(" : ");
		builder.append(countstr);
		builder.append("\n");
	}
	
	public static void logTabularData(String title, TabularData data, StringBuilder builder) {
		builder.append("**");
		builder.append(title);
		builder.append(" : \n");
		int count = 0;
		for (Object o : data.values()) {
			CompositeData row = (CompositeData) o;
			CompositeType type = row.getCompositeType();
			for (Object k : type.keySet()) {
				String key = k.toString();
				if (count > 0)
					builder.append(" ----- ");
				builder.append(key.toUpperCase());
				count++;
			}
			builder.append("\n");
			break;
		}

		count = 0;
		for (Object o : data.values()) {
			CompositeData row = (CompositeData) o;
			CompositeType type = row.getCompositeType();
			for (Object k : type.keySet()) {
				String key = k.toString();
				if (count > 0)
					builder.append(" ----- ");
				builder.append(row.get(key));
				count++;
			}
			builder.append("\n");
			count = 0;
		}
		builder.append("\n");
	}

	public static void logCount(String title, int counter, StringBuilder builder) {
		builder.append("**");
		builder.append(title);
		builder.append(" : ");
		builder.append(counter);
		builder.append("\n");
	}
	
}
