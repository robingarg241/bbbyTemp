package com.hero.jcr.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import tagUtil.AttributeArray;
import tagUtil.DateArray;
import tagUtil.ShotTypeArray;

import com.hero.jcr.util.CqHelper;
import com.hero.jcr.util.PartitionUtil;

/*
 * This program takes a file of formated asset data and applies tags to the matching
 * Asset in the DAM.
 * 
 * Adam Trissel
 * Hero Digital
 * 02.27.17
 */

public class AddBBBYMetaData {
	
	static final String REPOLOCATION = "http://40.117.135.191:4502";
	//static final String REPOLOCATION = "http://aem-bbby-auth.herodigital.com:4502";
	static final String REPOUSER = "admin";
	static final String REPOPASSWORD = "tUq4Ju%gN>UxB34SFhNeh85w";
	//static final String REPOPASSWORD = "admin";
	//static final String REPOPASSWORD = "M4C@rjdN*m58!S1e";
	static String DEFAULTFILE = "/Users/adamtrissel/projects/bbby/scripts/migration/data/uniqueAssetFile.csv";

	static String tagPrototype = "bbby:%s/%s";
	static String destinationPrototype = "http://%s:%s/content/dam/bbby/approved_dam/assets/%s";
	static String nodePrototype = "/content/dam/bbby%s";
	

	static String tagFieldPrototype = "-F \"%s\"";	
	
	static final HashMap<String, String> dateTagArray = DateArray.getDateArray();
	static final HashMap<String, String> shotTypeArray = ShotTypeArray.getShotTypeArray();
	
	static HashMap<String, String> tagHashMap = new HashMap<String, String>();
	
	private static Options options = new Options();
	
	private static final Logger log = Logger.getLogger(AddBBBYMetaData.class.getName());	
	
	/* New file format
	 * 
	 * This is the primaryAssetBean, which is the distilled, unique, assets from the original list
	 * With this list we are going to add metadata to the assets that have been uploaded to the dam
	 * Item level PDM data will follow in a separate program
	
	String include_in_migration;
	String fullpath;
	String fullname;
	String uPC;
	String collection;
	String concept;
	String asset_Use;
	String channel;
	String asset_Source;
	String asset_Type_Level_4;
	String asset_Type_Product;
	String shot_Type;
	String sequencing;
	String holiday;
	String environment;
	String departmentName;
	String departmentNumber;
	String swatchOverrideName;
	String primaryImage;
	String sku;
	
	 */
	
	public static void main(String[] args) throws Exception {
		
		HashMap<String, String> cliHash = getCommandLineArgs(args);
		
		processFile(cliHash.get("repo"),
					cliHash.get("username"),
					cliHash.get("password"),
					cliHash.get("filename"));
		
	}
	
	private static String createFlatName(String name){
		
		String retVal = null;
		
		name = name.toLowerCase();
		
		//retVal = name.replace(" ", "_").replace("(", "").replace(")", "").replace("/", "_").replace("&", "\\&").replace("'", "");
		retVal = name.replace(" ", "_").replace("(", "").replace(")", "").replace("/", "_").replace("'", "");
		
		return retVal;
	}
	
	private static String processAssetType(String asset_type){
		
		return makeFormalName("wd", "asset_type", asset_type);
		
	}
	
	private static String processCampaign(String campaign){
		
		return makeFormalName("wd", "campaign", campaign);
		
	}
	
	private static String processProduct(String product){
		
		return makeFormalName("wd", "product", product);
		
	}
	
	private static void processFile(String repo, String username, String password, String filename) throws IOException {
		
		/*
		 * open up the repo establish a session and find the root node
		 */
		
		CqHelper helper = new CqHelper();
		
		Session session = null;
		try {
			session = helper.getSession(repo, username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Node root = null;
		
		try {
			root = session.getRootNode();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		
		if (session == null || root == null){
			return;
		}
		
		Path file = Paths.get(filename);
        BufferedReader reader = Files.newBufferedReader(file,Charset.forName("UTF-8"));
        StringBuilder content = new StringBuilder();
        String line = null;

        long recordCounter = 0;
        
        while ((line = reader.readLine()) != null) {
        	
        	recordCounter++;
        	
            String[] columns = line.split("\t");
            
            if (columns.length >= 18){
            	
            	AttributeArray attributes = createAttributeArray(columns);

            	System.out.println(attributes.toString());
            	System.out.println();
            	
                Boolean success = setPropertiesOnNode(root, attributes);
                
                //save work every 100 records
                if (recordCounter % 100 == 0) {
                	
                	try {
                		log.info("****saving session****");
						session.save();
					} catch (RepositoryException e) {
						e.printStackTrace();					}
                	
                }

            }
            
        }
        
        /*
         * close the session
         */
        
        try {
			session.save();
		} catch (RepositoryException e) {
			System.out.println("rolled back");
			e.printStackTrace();
		}
        
        session.logout();
        
        reader.close();
		
	}
	
	private static String decodeTags(String tag){
		
		String returnTag = "";
		
		if (tagHashMap.keySet().contains(tag)){
			
			returnTag = tagHashMap.get(tag);
			
		}
		
		return returnTag;
	}
	
	private static ArrayList<String> addStandardTags(ArrayList<String> tagList, String fieldName, String tag){
		
		if (!tag.equals("")){
			tagList.add("bbby:" + fieldName.toLowerCase().replace(" ", "_") + "/" + tag.toLowerCase().replace(" ", "_"));
		}
		
		return tagList;
		
	}
		/*
	 * marshal the attribute array bean
	 */
	
	private static AttributeArray createAttributeArray(String[] columns){
		
		PrimaryAssetBean ab = new PrimaryAssetBean(columns);
		
        ArrayList<String> tagList = new ArrayList<String>();
        
//        ArrayList<String> partitions = PartitionUtil.hashFileName(ab.asset_Type_Level_4);
//        
//        System.out.println(partitions.toString());
//        
//        String asset_type_tag = partitions.get(0).toLowerCase().replace(" ", "_");
//        asset_type_tag = "approved_dam/" + asset_type_tag.substring(1);
        
        String asset_type_tag = ab.getAsset_Type_Product().toLowerCase().replace(" ", "_");

		addStandardTags(tagList, "asset_type", asset_type_tag);
		addStandardTags(tagList, "concept", ab.getConcept());
		addStandardTags(tagList, "asset_use", ab.getAsset_Use());
		addStandardTags(tagList, "channel", ab.getChannel());
		addStandardTags(tagList, "asset_source", ab.getAsset_Source());
		addStandardTags(tagList, "shot_type", ab.getShot_Type());

		//leaving out sequencing, holiday, environment
		
        AttributeArray attributes = new AttributeArray();
        
        attributes.setUpc(ab.getuPC());
        attributes.setSku(ab.getSku());
        attributes.setCollection(ab.getCollection());
        attributes.setDepartmentName(ab.getDepartmentName());
        attributes.setDepartmentNumber(ab.getDepartmentNumber());
        attributes.setSwatchOverrideName(ab.getSwatchOverrideName());
        attributes.setPrimaryImage(ab.getPrimaryImage());

        String node = String.format(nodePrototype, ab.getAsset_Type_Level_4().toLowerCase().replace(" ", "_") + "/" + ab.getFullname()) ;
        
        attributes.setNodeName(node);

        attributes.setTagList(tagList);
        
		return attributes;
		
	}
	
	/*
	 * take the attributes, and attach them to the node specified by the URL, which will be relative to "/content/dam/belkin"
	 */
	
	private static Boolean setPropertiesOnNode(Node root, AttributeArray attributes){
		
        String fullNodeName = attributes.getNodeName() + "/jcr:content/metadata";
        
        Boolean success = false;
        
        try {
			
        	Node n = root.getNode("." + fullNodeName);
			
            String[] tagsArray = new String[attributes.getTagList().size()];
            tagsArray = attributes.getTagList().toArray(tagsArray);
            
            n.setProperty("cq:tags", tagsArray);
            n.setProperty("bbby:primaryUpc", attributes.getUpc());
            n.setProperty("bbby:assetUpdate", "no");
            n.setProperty("bbby:sku", attributes.getCollection());
            n.setProperty("bbby:departmentName", attributes.getDepartmentName());
            n.setProperty("bbby:departmentNumber", attributes.getDepartmentNumber());
            n.setProperty("bbby:swatchOverride", attributes.getSwatchOverrideName());
            n.setProperty("bbby:primaryImage", attributes.getPrimaryImage());
            
            success = true;
            
		} catch (RepositoryException e) {
			e.printStackTrace();
			//log.severe("failed to find node: " + fullNodeName + "/jcr:content/metadata");
		}
        
        return success;
		
	}
	
	private static HashMap<String, String> getCommandLineArgs(String[] args){
		
		HashMap<String, String> cliHash = new HashMap<String, String>();
		String repo = REPOLOCATION;
		String username = REPOUSER;
		String password = REPOPASSWORD;
		String filename = DEFAULTFILE;
		
		options.addOption("r", "repo", true, "Repo Location.");
		options.addOption("u", "username", true, "Repo Username.");
		options.addOption("p", "password", true, "Repo User Password.");
		options.addOption("f", "filename", true, "Input file name");
		
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
			
			if(cmd.hasOption('f')){
				filename = cmd.getOptionValue("f");
			}

		} catch (ParseException e){
			
		   log.log(Level.SEVERE, "Failed to parse comand line properties", e);
			
		}
		
		cliHash.put("repo", repo);
		cliHash.put("username", username);
		cliHash.put("password", password);
		cliHash.put("filename", filename);
		
		return cliHash;
		
	}
	
	private static String flattenName(String name) {
		
		name = name.toLowerCase();
		
		name = name.replace(" - ","/");
		name = name.replace(" ", "_");
		
		return name;
		
	}
	
	private static String makeFormalName(String namespace, String type, String value) {
		
		String retVal = flattenName(namespace) + ":" +
	                    flattenName(type) + "/" + 
	                    flattenName(value);
		
		return retVal;
		
	}	

}
