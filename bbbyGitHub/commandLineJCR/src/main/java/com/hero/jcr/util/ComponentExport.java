package com.hero.jcr.util;

import com.hero.jcr.util.CommandLineBean;
import com.hero.jcr.util.CqHelper;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class ComponentExport {

	public static void main(String[] args) throws Exception {

		CommandLineBean cli = new CommandLineBean(args);
	    
	    String repoLocation = cli.getRepoLocation();
	    String userName = cli.getUsername();
	    String password = cli.getPassword();
	    String targetURL = cli.getTargeturl();
		
		if (repoLocation == null || targetURL == null){
			return;
		}
		
		int index = targetURL.indexOf("/content");
		
		if (index == -1){
			
			//ok, they path put in does not conform to the true page in the JCR.
			System.out.println("URL does not contain '/content'. This program needs the true JCR node name, not a vainty URL");
			return;
			
		}
		
		String targetJCRNode = "." + targetURL.substring(index);
		
		CqHelper helper = new CqHelper();
		
		Session session = helper.getSession(repoLocation, userName, password);
		
		Node root = session.getRootNode();
		
		Node slide = root.getNode(targetJCRNode);
		
		NodeIterator it = slide.getNodes();
		
		Node parsys = it.nextNode();
		
		NodeIterator it2 = parsys.getNodes();
		
		Node component = it2.nextNode();
		
		String componentPath = component.getPath();
		
		String url = repoLocation + componentPath + ".html";
		
        Client c = Client.create();
        
        c.addFilter(new HTTPBasicAuthFilter(userName, password));
        
        WebResource resource = c.resource(url);
		
		String output = resource.get(String.class);
		
		System.out.println(output);
		
	}

}
