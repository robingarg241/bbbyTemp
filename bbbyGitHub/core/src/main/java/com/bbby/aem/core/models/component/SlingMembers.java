package com.bbby.aem.core.models.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

/**
 * Pdm Metadata Component to display the nodes under upcmetadata node for an
 * asset
 */
@Model(adaptables = { Resource.class,
		SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, resourceType = "bbby/components/content/sling-members")
@Exporter(name = "jackson", extensions = "json")
public class SlingMembers extends ComponentSlingModel {

	@Inject
	@SlingObject
	@Required
	private SlingHttpServletRequest request;

	private List<String> slingMembers;
	private List<String> membersSequence;

	@Override
	public void postConstruct() throws Exception {

		if (request.getRequestParameter("item") != null) {
			String assetUrl = request.getRequestParameter("item").toString();

			slingMembers = getSlingMembers(assetUrl);
			
			membersSequence = getMembersSequence(assetUrl);
		}

	}

	private List<String> getSlingMembers(String assetUrl) throws Exception {

		ResourceResolver resourceResolver = request.getResourceResolver();

		Session session = resourceResolver.adaptTo(Session.class);

		List<String> pdmMetadataList = new ArrayList<String>();

		Node assetNode = session.getNode(assetUrl);

		if (assetNode.hasNode("./jcr:content/related/s7Set/sling:members")) {

			Node memberNode = assetNode.getNode("./jcr:content/related/s7Set/sling:members");

			if (memberNode != null && memberNode.hasProperty("sling:resources")) {

				String[] membersArray = convertValueArrayToStringArray(
						memberNode.getProperty("sling:resources").getValues());
				pdmMetadataList = Arrays.asList(membersArray);
			}
		}

		return pdmMetadataList;
	}
	
	private List<String> getMembersSequence(String assetUrl) throws Exception {

		ResourceResolver resourceResolver = request.getResourceResolver();

		Session session = resourceResolver.adaptTo(Session.class);

		List<String> pdmMetadataList = new ArrayList<String>();

		Node assetNode = session.getNode(assetUrl);

		if (assetNode.hasNode("./jcr:content/operationalmetadata")) {

			Node operationalNode = assetNode.getNode("./jcr:content/operationalmetadata");

			if (operationalNode != null && operationalNode.hasProperty("membersSequence")) {

				String[] membersArray = convertValueArrayToStringArray(
						operationalNode.getProperty("membersSequence").getValues());
				pdmMetadataList = Arrays.asList(membersArray);
			}
		}

		return pdmMetadataList;
	}

	private static String[] convertValueArrayToStringArray(Value[] strArr)
			throws ValueFormatException, IllegalStateException, RepositoryException {
		String[] sb = new String[strArr.length];
		int i = 0;
		for (Value str : strArr) {
			sb[i] = str.getString();
			i++;
		}
		return sb;
	}

	public List<String> getSlingMembers() {
		return slingMembers;
	}

	public List<String> getMembersSequence() {
		return membersSequence;
	}
	
	

}
