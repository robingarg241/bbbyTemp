package com.bbby.aem.core.jmx.schedulers;

public class JMXConstants {

	public static String OBJECT_NAME_WORKFLOW = "com.adobe.granite.workflow:type=Maintenance";
	public static String OBJECT_NAME_S7 = "com.adobe.granite.replication:type=agent,id=\"s7delivery\"";
	public static String OBJECT_NAME_SLING_QUEUE = "org.apache.sling:type=queues,name=AllQueues";
	public static String OBJECT_NAME_JACKRABBIT_OAK_SESSION = "org.apache.jackrabbit.oak:type=Metrics,name=SESSION_COUNT";
	public static String OBJECT_NAME_JACKRABBIT_OAK_OBSERVATION = "org.apache.jackrabbit.oak:type=Metrics,name=OBSERVATION_QUEUE_MAX_LENGTH";
	public static String OBJECT_NAME_JACKRABBIT_OAK_QUERYSTAT = "org.apache.jackrabbit.oak:type=QueryStat,name=Oak Query Statistics";
	public static String OBJECT_NAME_JACKRABBIT_OAK_CONSOLIDATEDLISTENERSTATS = "org.apache.jackrabbit.oak:type=ConsolidatedListenerStats,name=Consolidated Event Listener statistics";

}
