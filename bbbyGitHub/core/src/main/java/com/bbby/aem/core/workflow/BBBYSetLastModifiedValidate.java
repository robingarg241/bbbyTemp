package com.bbby.aem.core.workflow;

import java.util.Calendar;
import java.util.Iterator;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import com.google.common.collect.Iterators;

import org.apache.sling.api.resource.Resource;

/**
 * @author Sandeep
 *
 */
@Component(service = WorkflowProcess.class, property = { "process.label=BBBY Last Mod Validate",
		"Constants.SERVICE_VENDOR=BBBY", "Constants.SERVICE_DESCRIPTION=ADD Set Last Modified Date" })
public class BBBYSetLastModifiedValidate implements WorkflowProcess {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
			throws WorkflowException {
		WorkflowData workflowData = workItem.getWorkflowData();
		String path = workflowData.getPayload().toString();
		log.info("path is: " + path);
		String userId = (String)workItem.getWorkflowData().getMetaDataMap().get("userId", String.class);
		log.info("user id.. "+userId);
		Session session = workflowSession.adaptTo(Session.class);
		 ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
		 if(resolver==null) {
				log.info("resourceResolver is Null");
				}
		  Asset asset = getAssetFromPayload(workItem, resolver);
		  if (null == asset) {
			  log.info("asset is null...");
				String newPath = getNewAssetPath(path, session,resolver);
				String newPathDup = getNewAssetPathFromDup(path, session,resolver);
				log.info("new path is:" + newPath);
				log.info("new path Dup is:" + newPathDup);
				if(newPath!=null || newPathDup!=null) {
					String nodePath;
					if(newPath!=null) {
						nodePath = newPath;
					}
					else {
						nodePath = newPathDup;	
					}
					try {
					Node meta = session.getNode(nodePath);
					Node content = session.getNode(nodePath).getParent();
					content.setProperty("jcr:lastModifiedBy", userId);
		            content.setProperty("jcr:lastModified", Calendar.getInstance());
		            if (!meta.hasProperty("dc:modified")) {
			        	meta.setProperty("dc:modified", Calendar.getInstance()); 
				}
		            log.info("property added manually");
					}
					catch (Exception e) {
						log.info("Unable to complete processing of BBBY Set Last Mod Date, for path: " + path, e);
					}
			
				}
				else {
					log.info("need to retry as new path is null");
					throw new WorkflowException("retry");
				}
				
				log.info("terminating workflow");
				workflowSession.terminateWorkflow(workItem.getWorkflow());
				log.info("terminated workflow");
		  }
		  else {
			  log.info("asset is not null...");
		  }

	}
	

	private String getNewAssetPathFromDup(String path, Session session, ResourceResolver resolver) {
		String newPath = null;
		path = path.replaceAll("/jcr:content.*", "");
		String newName = path.substring(path.lastIndexOf('/') + 1, path.length());
		String uploadName = newName.substring(newName.indexOf("_") + 1, newName.length());
		log.info("new Name is.." + newName);
		String q = "SELECT * FROM [dam:Asset] AS s WHERE ISDESCENDANTNODE([/content/dam/bbby/asset_transitions_folder/vendor/duplicate_vendor_assets]) " + "and (s.[jcr:content/metadata/bbby:uploadedAssetName] = \""
				+ uploadName + "\")";
		log.info("Query for new Name is.." + q);
		Query myQuery;
		try {
		//	Thread.sleep(5000);
			myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
			QueryResult result = myQuery.execute();
			NodeIterator ni = result.getNodes();
			Iterator<Resource> batchNodes1 = resolver.findResources(q, Query.JCR_SQL2);
			log.info("number of results = "+Iterators.size(batchNodes1));
			while (ni.hasNext()) {
				Node n = ni.nextNode();
				Node metanode = session.getNode(n.getPath()+"/jcr:content/metadata");
				newPath = metanode.getPath();

			}
		} catch (Exception e) {
			log.error("can't get new path for payload..."+e);
		} 
		
		return newPath;
	}
	
	private String getNewAssetPath(String path, Session session, ResourceResolver resolver) {
		String newPath = null;
		path = path.replaceAll("/jcr:content.*", "");
		String newName = path.substring(path.lastIndexOf('/') + 1, path.length());
		log.info("new Name is.." + newName);
		String q = "SELECT * FROM [dam:Asset] AS s WHERE ISDESCENDANTNODE([/content/dam/bbby]) " + "and NAME() = \""
				+ newName + "\"";
		log.info("Query for new Name is.." + q);
		Query myQuery;
		try {
		//	Thread.sleep(5000);
			myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
			QueryResult result = myQuery.execute();
			NodeIterator ni = result.getNodes();
			Iterator<Resource> batchNodes1 = resolver.findResources(q, Query.JCR_SQL2);
			log.info("number of results = "+Iterators.size(batchNodes1));
			while (ni.hasNext()) {
				Node n = ni.nextNode();
				Node metanode = session.getNode(n.getPath()+"/jcr:content/metadata");
				newPath = metanode.getPath();

			}
		} catch (Exception e) {
			log.error("can't get new path for payload..."+e);
		} 
		
		return newPath;
	}
	
	
	private Asset getAssetFromPayload(WorkItem item, ResourceResolver resolver) {
		Asset asset = null;
		if (item.getWorkflowData().getPayloadType().equals("JCR_PATH")) {
		String path = item.getWorkflowData().getPayload().toString();
		Resource resource = resolver.getResource(path);
		if (null != resource) {
		asset = DamUtil.resolveToAsset(resource);
		} else {
		log.error("getAssetFromPaylod: asset [{}] in payload of workflow [{}] does not exist.", path,
		item.getWorkflow().getId());
		}
		}

		return asset;
		}

}