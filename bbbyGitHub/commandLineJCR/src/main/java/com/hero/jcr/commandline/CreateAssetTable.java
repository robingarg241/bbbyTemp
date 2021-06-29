package com.hero.jcr.commandline;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hero.jcr.model.ResourceBean;
import com.hero.jcr.util.CqHelper;

import org.apache.felix.scr.annotations.Reference;

/**
 * @author adamtrissel
 * 
 * This program scans a jcr instance and reports what assets have been loaded by MT ID Number
 *
 */
public class CreateAssetTable {
	
	//these should be passed in on the command line
	
	private static HashMap<String, String> assetIDMap = new HashMap<String, String>();
	
	private static Options options = new Options();
	
	private static final Logger log = Logger.getLogger(CreateAssetTable.class.getName());
	
	static String REPOLOCATION = "http://localhost:4502";
	static String REPOUSER = "admin";
	static String REPOPASSWORD = "admin";
	static String FILLER = "Unknown";
	
	static ArrayList<String> allowedTypes = new ArrayList<String>();
	static HashMap<String, String> languageMap = new HashMap<String, String>();
	static HashMap<String, AssetBean> damAssets = new HashMap<String, AssetBean>();
	
	public static void main(String[] args) throws Exception {
		
		//get command line attributes
		
		HashMap<String, String> cliHash = getCommandLineArgs(args);
		
		String repo = cliHash.get("repo");
		String username = cliHash.get("username");
		String password = cliHash.get("password");
		
		if (repo == null){
			log.log(Level.WARNING, "Starting to create asset map");
			return;
		}
		
		log.log(Level.INFO, "Starting to create asset map");
		damAssets = getDamAssets(repo, username, password);
		log.log(Level.INFO, "Finished creating asset map");
		
	}
	
	private static HashMap<String, String> getCommandLineArgs(String[] args){
		
		HashMap<String, String> cliHash = new HashMap<String, String>();
		String repo = REPOLOCATION;
		String username = REPOUSER;
		String password = REPOPASSWORD;
		
		options.addOption("r", "repo", true, "Repo Location.");
		options.addOption("u", "username", true, "Repo Username.");
		options.addOption("p", "password", true, "Repo User Password.");
		options.addOption("f", "filename", true, "id to file filename.");
		
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		
		try{
			
			cmd = parser.parse(options, args);
			
			if(cmd.hasOption('r')){
				repo = cmd.getOptionValue("r");
			}

			if(cmd.hasOption('u')){
				username = cmd.getOptionValue("u");
			}
			
			if(cmd.hasOption('p')){
				password = cmd.getOptionValue("p");
			}


		} catch (ParseException e){
			
		   log.log(Level.SEVERE, "Failed to parse comand line properties", e);
			
		}
		
		cliHash.put("repo", repo);
		cliHash.put("username", username);
		cliHash.put("password", password);
		
		return cliHash;
		
	}
	
	private static String getTagNamespace(String tag){
		
		String retVal = null;
		
		String[] elements = tag.split("/");
		
		retVal = elements[0].replace("tcolv:", "");
		
		return retVal;
		
	}
	
	private static String getPropertyValue(Node metaData, String propertyName){
		
		String retVal = null;		

		try{

			Property p = metaData.getProperty(propertyName);
			retVal = p.getString();
		
		} catch (Exception e){
			
			retVal = "";
			
		}
		
		return retVal;
		
	}
	
	/*
	 * This is kinda tricky
	 * The CRM Tag taxonomy has the following structure: tcolv:crm/[var name]/[value]
	 * 
	 * We need to extract the tags, convert them into key value pairs and send them back
	 * NOTE: My guess is that the [value] part is not going to match the values in the recipient/targetData set, so there might 
	 * be a translation between values necessary, and that would have to be added to this routine
	 * 
	 */
	
	private static Map<String, String> getCRMTags(Node metaData){
		
		HashMap<String, String> crmTagMap = new HashMap<String, String>();
		
		try{

			Property p = metaData.getProperty("cq:tags");
			Value[] v = p.getValues();
			
			for (Value tagV : v){
				
				String tag = tagV.getString();
				if (getTagNamespace(tag).equals("crm")){
					
					String[] pieces = tag.split("/");
					
					String key = pieces[pieces.length - 2];
					String value = pieces[pieces.length - 1];
					
					crmTagMap.put(key, value);
					
				}
				
			}
			
			
			
		} catch (Exception e) {
			
			//pass
			
		}
		
		return crmTagMap;
		
	}
	
	private static HashMap<String, AssetBean> getDamAssets(String repo, String username, String password) throws Exception {
		
		HashMap<String,AssetBean> damAssets = new HashMap<String,AssetBean>();

		CqHelper helper = new CqHelper();
		
		Session session = helper.getSession(repo, username, password);
		
		Node root = session.getRootNode();
		
		ArrayList<String> headerArray = new ArrayList<String>();
		
	    headerArray.add("AssetPath");
	    headerArray.add("productDivision");
	    headerArray.add("longBodyType");
	    headerArray.add("shortBodyType");
	    headerArray.add("url");
	    headerArray.add("weight");
	    headerArray.add("iconURL");
		
	    System.out.println(String.join("\t", headerArray));
	    
		String q = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([/content/dam/tcolv]) and [jcr:primaryType] = 'dam:Asset'";
				
		Query myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
		
		QueryResult result = myQuery.execute();
		
		NodeIterator ni = result.getNodes();
		
		long nodeCounter = 0;
		
		while (ni.hasNext()){
			
			nodeCounter++;

			String longBodyCopy = "";

			String shortBodyCopy = "";
			
			String url = "";
			
			String weight = "";

			String iconURL = "";

			String assetStartDate = "";
			
			String assetEndDate = "";
			
			Map<String, String> crmTagMap = new HashMap<String, String>();
			
			Node n = ni.nextNode();
			
			ArrayList<String> assetArray = new ArrayList<String>();
			
			HashMap<String, String> tagMap = new HashMap<String, String>();
			
			if (!n.getPath().contains("subasset")){
				
				try{

					Node metaData = root.getNode("." + n.getPath() + "/jcr:content/metadata");
					
					longBodyCopy = getPropertyValue(metaData, "longBodyCopy");

					shortBodyCopy = getPropertyValue(metaData, "shortBodyCopy");
					
					url = getPropertyValue(metaData, "url");
					
					weight = getPropertyValue(metaData, "weight");

					iconURL = getPropertyValue(metaData, "iconURL");
					
					assetStartDate = getPropertyValue(metaData, "assetStartDate");

					assetEndDate = getPropertyValue(metaData, "assetEndDate");

					//					p = metaData.getProperty("cq:tags");
//					Value[] tags = p.getValues();
//					
//					for (Value t : tags){
//
//						tagMap.put(getTagNamespace(t.getString()), t.getString());
//					}
					
					crmTagMap = getCRMTags(metaData);
					
					

				} catch (Exception e){
					//pass
				}
				
				assetArray.add(n.getPath());
				assetArray.add(longBodyCopy);
				assetArray.add(shortBodyCopy);
				assetArray.add(url);
				assetArray.add(weight);
				assetArray.add(iconURL);
				assetArray.add(assetStartDate);
				assetArray.add(assetEndDate);
				
				assetArray.add(crmTagMap.getOrDefault("tag_category", ""));
				assetArray.add(crmTagMap.getOrDefault("player_tier", ""));
				assetArray.add(crmTagMap.getOrDefault("gender", ""));
				assetArray.add(crmTagMap.getOrDefault("player_credit", ""));
				assetArray.add(crmTagMap.getOrDefault("entertainment_pref", ""));
				assetArray.add(crmTagMap.getOrDefault("campaign_market", ""));
				assetArray.add(crmTagMap.getOrDefault("host_name", ""));
				assetArray.add(crmTagMap.getOrDefault("age_category", ""));
				assetArray.add(crmTagMap.getOrDefault("birth_month", ""));
				assetArray.add(crmTagMap.getOrDefault("point_flag", ""));
				assetArray.add(crmTagMap.getOrDefault("lodging_worth", ""));
				assetArray.add(crmTagMap.getOrDefault("gaming_worth", ""));
				assetArray.add(crmTagMap.getOrDefault("gaming_predom", ""));
				assetArray.add(crmTagMap.getOrDefault("gaming_type", ""));
				assetArray.add(crmTagMap.getOrDefault("nongaming_type", ""));
				assetArray.add(crmTagMap.getOrDefault("gaming_subtype", ""));
				assetArray.add(crmTagMap.getOrDefault("preferred_gaming_location", ""));
				assetArray.add(crmTagMap.getOrDefault("fb_pref", ""));
				assetArray.add(crmTagMap.getOrDefault("restaurant_type", ""));
				assetArray.add(crmTagMap.getOrDefault("restaurant_spend_level", ""));
				assetArray.add(crmTagMap.getOrDefault("retail_pref", ""));
				assetArray.add(crmTagMap.getOrDefault("bar_pref", ""));
				assetArray.add(crmTagMap.getOrDefault("room_type", ""));
				assetArray.add(crmTagMap.getOrDefault("recent_visit", ""));

				assetArray.add("END");
				
				System.out.println(String.join("\t", assetArray));
				
			}
			
		}
		
		session.logout();
		
		return damAssets;
		
	}

}
