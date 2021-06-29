/**
 * 
 */
package com.bbby.aem.core.util;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;

/**
 * @author Karan
 *
 */
public class JcrPropertiesUtil {

	private static final Logger log = LoggerFactory.getLogger(JcrPropertiesUtil.class);

	public static Node getReportingNode(Node node,Session session) {
        Node repometa = null;
        try {
			if(node.hasNode(CommonConstants.REPORTING_METADATA_NODE)){
				repometa = node.getNode(CommonConstants.REPORTING_METADATA_NODE);
			}else{
				repometa = JcrUtil.createPath(node.getPath() + "/" + CommonConstants.REL_ASSET_REPORTING_METADATA, JcrConstants.NT_UNSTRUCTURED, session);
			}
		} catch (PathNotFoundException e) {
			log.debug("Path not found : ", e);
		} catch (RepositoryException e) {
			log.debug("RepositoryException : ", e);
		}
		return repometa;
	}
	
	public static Node getOperationalNode(Node node,Session session) {
        Node operationalmeta = null;
        try {
			if(node.hasNode(CommonConstants.OPERATIONAL_METADATA_NODE)){
				operationalmeta = node.getNode(CommonConstants.OPERATIONAL_METADATA_NODE);
			}else{
				operationalmeta = JcrUtil.createPath(node.getPath() + "/" + CommonConstants.REL_ASSET_OPERATIONAL_METADATA, JcrConstants.NT_UNSTRUCTURED, session);
			}
		} catch (PathNotFoundException e) {
			log.debug("Path not found : ", e);
		} catch (RepositoryException e) {
			log.debug("RepositoryException : ", e);
		}
		return operationalmeta;
	}

}
