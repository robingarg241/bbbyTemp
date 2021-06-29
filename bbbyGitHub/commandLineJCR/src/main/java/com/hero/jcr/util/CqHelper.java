package com.hero.jcr.util;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.JcrUtils;

public class CqHelper {
	
	Session session;

	public Session getSession(String repoLocation, 
				String repoUser, 
				String repoPassword) throws Exception {
  
		Repository repository = JcrUtils.getRepository(
		        repoLocation + "/crx/server");
	
		SimpleCredentials creds = new SimpleCredentials(repoUser, repoPassword.toCharArray());
	
		session = repository.login(creds);
		
		return session;
	
	}
		
	public String makeRelative(String url) {
		 
		  String newURL = url;
		  if (url.startsWith("/") == true){
		    newURL = url.substring(1);
		  }
		  
		  return newURL;
		  
		}

}