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

public class AddBBBYPDMMetaData {
	
	static final String REPOLOCATION = "http://40.117.135.191:4502";
	//static final String REPOLOCATION = "http://aem-bbby-auth.herodigital.com:4502";
	static final String REPOUSER = "admin";
	static final String REPOPASSWORD = "tUq4Ju%gN>UxB34SFhNeh85w";
	//static final String REPOPASSWORD = "admin";
	//static final String REPOPASSWORD = "M4C@rjdN*m58!S1e";
	static String DEFAULTFILE = "/Users/adamtrissel/projects/bbby/scripts/migration/data/pdmDataFile.csv";

	static String tagPrototype = "bbby:%s/%s";
	static String destinationPrototype = "http://%s:%s/content/dam/bbby/approved_dam/assets/%s";
	static String nodePrototype = "/content/dam/bbby";
	

	static String tagFieldPrototype = "-F \"%s\"";	
	
	static final HashMap<String, String> dateTagArray = DateArray.getDateArray();
	static final HashMap<String, String> shotTypeArray = ShotTypeArray.getShotTypeArray();
	
	static HashMap<String, String> tagHashMap = new HashMap<String, String>();
	
	private static Options options = new Options();
	
	private static final Logger log = Logger.getLogger(AddBBBYPDMMetaData.class.getName());	
	
	/* New file format
	 * 
	 * This is the primaryAssetBean, which is the distilled, unique, assets from the original list
	 * With this list we are going to add metadata to the assets that have been uploaded to the dam
	 * Item level PDM data will follow in a separate program
	 * 
	 * This data needs to be ordered by asset, because we want to batch the PDM metadata, for instance
	 * we want to delete the PDMMetadata node on the first asset to clear out old data.
	
	String include_in_migration;
	String fullpath;
	String fullname;
	String primaryUPC;
	String sku;
	String brand;
	String colorCode;
	String colorGroupCode;
	String product;
	String associatedWebProductID;
	String associatedCollectionID;
	String primaryVendorNumber;
	String primaryVendorName;
	String vendorDirectToCustomerItem;
	String priorityFlag;
	String bbbyWebDisabled;
	String babyWebDisabled;
	String caWebDisabled;
	String bbbyWebOfferedFlag;
	String babyWebOfferedFlag;
	String caWebOfferedFlag;
	String webProductRollupType;
	String masterProductDescription;
	String fastTrackFlag;
	String pullbackToMerchant;
	String pdmBatchID;
	
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
		String lastAssetFullname = "start";
		
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
        
        int upcSequence = 0;
        
        while ((line = reader.readLine()) != null) {
        	
        	recordCounter++;
        	
            String[] columns = line.split("\t");
            
            if (columns.length >= 12){
            	
            	PDMAssetBean ab = new PDMAssetBean(columns);
            	
            	String nodeName = "." + nodePrototype + ab.getFullpath().toLowerCase().replace(" ", "_") + "/" + ab.getFullname() ;
            	
            	if(!ab.getFullname().contentEquals(lastAssetFullname)) {
            		
//            		System.out.println("==================");
            		lastAssetFullname = ab.getFullname();

            		upcSequence = 0;
            		
//            		System.out.println(upcSequence + ":" + nodeName);
            		
            		
            		try {
						if(root.hasNode(nodeName + "/jcr:content/upcmetadata")) {
							
							Node upcNode = root.getNode(nodeName + "/jcr:content/upcmetadata");
							upcNode.remove();
							
						} else {
							
							root.addNode(nodeName + "/jcr:content/upcmetadata");
							
						}
					} catch (RepositoryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		
            	} else {
            		
            		upcSequence++;
//            		System.out.println(upcSequence + ":" + nodeName);
            		
            	}
            	
                Boolean success = setPropertiesOnNode(root, ab, upcSequence);
                
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
		
		PDMAssetBean ab = new PDMAssetBean(columns);
		
        ArrayList<String> tagList = new ArrayList<String>();
        
        ArrayList<String> partitions = PartitionUtil.hashFileName(ab.getFullpath());

        String node = String.format(nodePrototype, ab.getFullpath().toLowerCase().replace(" ", "_") + "/" + ab.getFullname()) ;

        AttributeArray attributes = new AttributeArray();
        
        attributes.setNodeName(node);

        attributes.setTagList(tagList);
        
		return attributes;
		
	}
	
	private static ArrayList<String> splitLists(String list){
		
		String[] listArray = list.split(";");
		
		ArrayList<String> myList = new ArrayList<String>();
		
		for (String item : listArray) {
			
			if(!item.equals("")) {
				
				myList.add(item);
				
			}
			
		}
		
		return myList;
		
	}
	
	/*
	 * take the attributes, and attach them to the node specified by the URL, which will be relative to "/content/dam/belkin"
	 */
	
	private static Boolean setPropertiesOnNode(Node root, PDMAssetBean ab, int upcSequence){
		
        Boolean success = false;
		
        String nodeName = "." + nodePrototype + ab.getFullpath().toLowerCase().replace(" ", "_") + "/" + ab.getFullname() ;
		
        String fullNodeName = nodeName + "/jcr:content/upcmetadata";
        
        ArrayList<String> associatedWebProductIdList = splitLists(ab.getAssociatedWebProductID());
        ArrayList<String> associatedCollectionIdList = splitLists(ab.getAssociatedCollectionID());
        
        try {
        
	        //check to see if the partent node exists, if not create it
	        if(!root.hasNode(fullNodeName)) {
	        	root.addNode(fullNodeName);
	        }
	        
	        Node upcMetadataNode = root.getNode(fullNodeName);
	        
	        Node upcNode = upcMetadataNode.addNode("./upc-" + upcSequence);
	        
	        /*
	         * 
				associatedWebProductID: "1243003",
				primaryVendorName: "Amazon",
				productType: "Bath Sheet",
				primaryUPC: "617495455897",
				masterProductDescription: "This is test description",
				associatedCollectionID: "98435843",
				priorityFlag: true,
				product: "Product1",
				sku: "43187881",
				brand: "BBBY",
				color: "CANVAS",
				webProductId: "3260932",
				vendorDirectToCustomerItem: "Towel",
				primaryVendorNumber: "6378393",
				collectionId: "213462;215746",
				colorCode: "CANVAS",
				colorGroupCode: "CANVAS_GROUP"
				String bbbyWebDisabled;
				String babyWebDisabled;
				String caWebDisabled;
				String bbbyWebOfferedFlag;
				String babyWebOfferedFlag;
				String caWebOfferedFlag;
	         */
	        
            String[] wpidList = new String[associatedWebProductIdList.size()];
            wpidList = associatedWebProductIdList.toArray(wpidList);
            
			upcNode.setProperty("associatedWebProductID", wpidList);

			
            String[] acidList = new String[associatedCollectionIdList.size()];
            acidList = associatedCollectionIdList.toArray(acidList);

            upcNode.setProperty("associatedCollectionID", acidList);
			
			upcNode.setProperty("primaryVendorName",ab.getPrimaryVendorName());
			upcNode.setProperty("primaryUPC", ab.getPrimaryUPC());
			upcNode.setProperty("masterProductDescription", ab.getMasterProductDescription());
			upcNode.setProperty("priorityFlag", ab.getPriorityFlag());
			upcNode.setProperty("product", ab.getProduct());
			upcNode.setProperty("sku", ab.getSku());
			upcNode.setProperty("brand", ab.getBrand());
			upcNode.setProperty("color", ab.getColorCode());
			upcNode.setProperty("vendorDirectToCustomerItem", ab.getVendorDirectToCustomerItem());
			upcNode.setProperty("primaryVendorNumber", ab.getPrimaryVendorNumber());
			upcNode.setProperty("collectionId", ab.getAssociatedCollectionID());
			upcNode.setProperty("colorCode", ab.getColorCode());
			upcNode.setProperty("colorGroupCode",ab.getColorGroupCode());
			upcNode.setProperty("bbbyWebDisabled", ab.getBbbyWebDisabled());
			upcNode.setProperty("babyWebDisabled", ab.getBabyWebDisabled());
			upcNode.setProperty("caWebDisabled", ab.getCaWebDisabled());
			upcNode.setProperty("bbbyWebOfferedFlag", ab.getBbbyWebOfferedFlag());
			upcNode.setProperty("babyWebOfferedFlag", ab.getBabyWebOfferedFlag());
			upcNode.setProperty("caWebOfferedFlag",ab.getCaWebOfferedFlag());
			upcNode.setProperty("webProductRollupType", ab.getWebProductRollupType());
			upcNode.setProperty("pullbackToMerchant", ab.pullbackToMerchant);
			upcNode.setProperty("fastTrackFlag", ab.getFastTrackFlag());
			upcNode.setProperty("pdmBatchID", ab.getPdmBatchID());
	        
			success = true;
        
		} catch (RepositoryException e) {
			e.printStackTrace();
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
