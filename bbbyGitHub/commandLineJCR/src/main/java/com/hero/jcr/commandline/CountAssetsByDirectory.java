package com.hero.jcr.commandline;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * @author adamtrissel
 * 
 * This program scans a jcr instance and reports what assets have been loaded by MT ID Number
 *
 */
public class CountAssetsByDirectory {
	
	static ArrayList<String> directoryArray = new ArrayList<String>() {{
		add("/content/dam/belkin/belkin/digital/corporate_overview");
		add("/content/dam/belkin/belkin/digital/email_blasts");
		add("/content/dam/belkin/belkin/digital/external_banners");
		add("/content/dam/belkin/belkin/digital/internal_site_banners");
		add("/content/dam/belkin/belkin/digital/page_layouts");
		add("/content/dam/belkin/belkin/digital/product_page_images");
		add("/content/dam/belkin/belkin/digital/social");
		add("/content/dam/belkin/belkin/digital/stock_image");
		add("/content/dam/belkin/belkin/events/pop-up_banners");
		add("/content/dam/belkin/belkin/events/product_place_cards");
		add("/content/dam/belkin/belkin/events/tabletops");
		add("/content/dam/belkin/belkin/guides/product_highlights");
		add("/content/dam/belkin/belkin/guides/user_manuals");
		add("/content/dam/belkin/belkin/icons/");
		add("/content/dam/belkin/belkin/logos/corporate_logos");
		add("/content/dam/belkin/belkin/logos/logo_guidelines");
		add("/content/dam/belkin/belkin/logos/product");
		add("/content/dam/belkin/belkin/logos/product_family");
		add("/content/dam/belkin/belkin/logos/retail_logos");
		add("/content/dam/belkin/belkin/packaging/concepts");
		add("/content/dam/belkin/belkin/packaging/corporate_logos");
		add("/content/dam/belkin/belkin/packaging/design_layouts");
		add("/content/dam/belkin/belkin/packaging/internal_site_banners");
		add("/content/dam/belkin/belkin/packaging/stock_image");
		add("/content/dam/belkin/belkin/packaging/templates");
		add("/content/dam/belkin/belkin/photography/events");
		add("/content/dam/belkin/belkin/photography/in_store");
		add("/content/dam/belkin/belkin/photography/in_use");
		add("/content/dam/belkin/belkin/photography/lifestyle");
		add("/content/dam/belkin/belkin/photography/packaging");
		add("/content/dam/belkin/belkin/photography/product/2008");
		add("/content/dam/belkin/belkin/photography/product/2009");
		add("/content/dam/belkin/belkin/photography/product/2010");
		add("/content/dam/belkin/belkin/photography/product/2011");
		add("/content/dam/belkin/belkin/photography/product/2012");
		add("/content/dam/belkin/belkin/photography/product/2013");
		add("/content/dam/belkin/belkin/photography/product/2014");
		add("/content/dam/belkin/belkin/photography/product/2015");
		add("/content/dam/belkin/belkin/photography/product/2016");
		add("/content/dam/belkin/belkin/photography/stock_image");
		add("/content/dam/belkin/belkin/print/ads");
		add("/content/dam/belkin/belkin/print/brochure");
		add("/content/dam/belkin/belkin/print/card");
		add("/content/dam/belkin/belkin/print/catalog");
		add("/content/dam/belkin/belkin/print/flyer");
		add("/content/dam/belkin/belkin/print/lookbook");
		add("/content/dam/belkin/belkin/print/poster");
		add("/content/dam/belkin/belkin/video/brand");
		add("/content/dam/belkin/belkin/video/commercial");
		add("/content/dam/belkin/belkin/video/demos");
		add("/content/dam/belkin/belkin/video/how_to");
		add("/content/dam/belkin/belkin/video/product");
		add("/content/dam/belkin/belkin/video/scripts");
		add("/content/dam/belkin/belkin/corporate_creative_assets/brand_guidelines");
		add("/content/dam/belkin/belkin/corporate_creative_assets/corporate_overview");
		add("/content/dam/belkin/belkin/corporate_creative_assets/employee_badges");
		add("/content/dam/belkin/belkin/corporate_creative_assets/greeting_cards");
		add("/content/dam/belkin/belkin/corporate_creative_assets/ppt_templates");
		add("/content/dam/belkin/belkin/illustrations_&_diagrams/corporate");
		add("/content/dam/belkin/belkin/illustrations_&_diagrams/illustrations");
		add("/content/dam/belkin/belkin/pos_in-store/");
		add("/content/dam/belkin/belkin/pos_in-store/ctu");
		add("/content/dam/belkin/belkin/pos_in-store/end_cap");
		add("/content/dam/belkin/belkin/pos_in-store/fsdu");
		add("/content/dam/belkin/belkin/pos_in-store/header");
		add("/content/dam/belkin/belkin/pos_in-store/in-line");
		add("/content/dam/belkin/belkin/pos_in-store/other_signage");
		add("/content/dam/belkin/belkin/pos_in-store/pdq");
		add("/content/dam/belkin/belkin/pos_in-store/pog");
		add("/content/dam/belkin/belkin/pos_in-store/pallet");
		add("/content/dam/belkin/belkin/pos_in-store/peg_talker");
		add("/content/dam/belkin/belkin/pos_in-store/posters");
		add("/content/dam/belkin/belkin/pos_in-store/print_banner");
		add("/content/dam/belkin/belkin/pos_in-store/pull-up_banner");
		add("/content/dam/belkin/belkin/pos_in-store/shelf_talker");
		add("/content/dam/belkin/belkin/pos_in-store/shipper");
		add("/content/dam/belkin/belkin/pos_in-store/tabletop_display");
		add("/content/dam/belkin/belkin/pos_in-store/toolkits");
		add("/content/dam/belkin/belkin/public_relations/info_brief");
		add("/content/dam/belkin/belkin/sales_support/line_review_deck");
		add("/content/dam/belkin/belkin/sales_support/product_bulletins");
		add("/content/dam/belkin/belkin/sales_support/sales_playbook");
		add("/content/dam/belkin/belkin/sales_support/sales_sheets");
		add("/content/dam/belkin/belkin/sales_support/solution_briefs");
		add("/content/dam/belkin/belkin/sales_support/toolkits");
		add("/content/dam/belkin/belkin/sales_support/training");
		add("/content/dam/belkin/belkin/sales_support/sales_sheets");
		add("/content/dam/belkin/belkin/video/brand");
		add("/content/dam/belkin/belkin_networking//");
		add("/content/dam/belkin/corporate//");
		add("/content/dam/belkin/corporate/guides/user_manuals");
		add("/content/dam/belkin/corporate/video/");
		add("/content/dam/belkin/corporate/pos_in-store/pog");
		add("/content/dam/belkin/corporate/pos_in-store/pallet");
		add("/content/dam/belkin/linksys//");
		add("/content/dam/belkin/linksys/digital/");
		add("/content/dam/belkin/linksys/digital/email_blasts");
		add("/content/dam/belkin/linksys/digital/external_banners");
		add("/content/dam/belkin/linksys/digital/internal_site_banners");
		add("/content/dam/belkin/linksys/digital/landing_page");
		add("/content/dam/belkin/linksys/events/pop-up_banners");
		add("/content/dam/belkin/linksys/guides/qsg");
		add("/content/dam/belkin/linksys/icons/compatibility");
		add("/content/dam/belkin/linksys/icons/feature");
		add("/content/dam/belkin/linksys/logos/corporate_logos");
		add("/content/dam/belkin/linksys/logos/product_family");
		add("/content/dam/belkin/linksys/logos/retail_logos");
		add("/content/dam/belkin/linksys/packaging/bundle_packaging");
		add("/content/dam/belkin/linksys/packaging/concepts");
		add("/content/dam/belkin/linksys/packaging/design_layouts");
		add("/content/dam/belkin/linksys/packaging/production_mechanicals");
		add("/content/dam/belkin/linksys/packaging/stickers");
		add("/content/dam/belkin/linksys/photography/device_screens");
		add("/content/dam/belkin/linksys/photography/lifestyle");
		add("/content/dam/belkin/linksys/photography/packaging");
		add("/content/dam/belkin/linksys/photography/product/2008");
		add("/content/dam/belkin/linksys/photography/product/2009");
		add("/content/dam/belkin/linksys/photography/product/2010");
		add("/content/dam/belkin/linksys/photography/product/2011");
		add("/content/dam/belkin/linksys/photography/product/2012");
		add("/content/dam/belkin/linksys/photography/product/2013");
		add("/content/dam/belkin/linksys/photography/product/2014");
		add("/content/dam/belkin/linksys/photography/product/2015");
		add("/content/dam/belkin/linksys/photography/product/2016");
		add("/content/dam/belkin/linksys/photography/stock_image");
		add("/content/dam/belkin/linksys/print/ads");
		add("/content/dam/belkin/linksys/print/brochure");
		add("/content/dam/belkin/linksys/print/circular");
		add("/content/dam/belkin/linksys/print/poster");
		add("/content/dam/belkin/linksys/video/commercial");
		add("/content/dam/belkin/linksys/video/media_clip");
		add("/content/dam/belkin/linksys/video/product");
		add("/content/dam/belkin/linksys/video/scripts");
		add("/content/dam/belkin/linksys/video/social");
		add("/content/dam/belkin/linksys/video/training");
		add("/content/dam/belkin/linksys/video/voiceover");
		add("/content/dam/belkin/linksys/corporate_creative_assets/brand_guidelines");
		add("/content/dam/belkin/linksys/illustrations_&_diagrams/diagrams");
		add("/content/dam/belkin/linksys/pos_in-store/");
		add("/content/dam/belkin/linksys/pos_in-store/end_cap");
		add("/content/dam/belkin/linksys/pos_in-store/fsdu");
		add("/content/dam/belkin/linksys/pos_in-store/header");
		add("/content/dam/belkin/linksys/pos_in-store/in-line");
		add("/content/dam/belkin/linksys/pos_in-store/other_signage");
		add("/content/dam/belkin/linksys/pos_in-store/pdq");
		add("/content/dam/belkin/linksys/pos_in-store/pog");
		add("/content/dam/belkin/linksys/pos_in-store/pallet");
		add("/content/dam/belkin/linksys/pos_in-store/posters");
		add("/content/dam/belkin/linksys/pos_in-store/print_banner");
		add("/content/dam/belkin/linksys/pos_in-store/pull-up_banner");
		add("/content/dam/belkin/linksys/pos_in-store/shipper");
		add("/content/dam/belkin/linksys/pos_in-store/shop_in_shop");
		add("/content/dam/belkin/linksys/pos_in-store/tabletop_display");
		add("/content/dam/belkin/linksys/public_relations/info_brief");
		add("/content/dam/belkin/linksys/sales_support/competitive_reference_tools");
		add("/content/dam/belkin/linksys/sales_support/product_bulletins");
		add("/content/dam/belkin/linksys/sales_support/sales_playbook");
		add("/content/dam/belkin/linksys/sales_support/sales_sheets");
		add("/content/dam/belkin/linksys/sales_support/solution_briefs");
		add("/content/dam/belkin/linksys/sales_support/toolkits");
		add("/content/dam/belkin/linksys/video/voiceover");
		add("/content/dam/belkin/wemo/digital/external_banners");
		add("/content/dam/belkin/wemo/digital/internal_site_banners");
		add("/content/dam/belkin/wemo/digital/landing_page");
		add("/content/dam/belkin/wemo/digital/page_layouts");
		add("/content/dam/belkin/wemo/digital/product_page_images");
		add("/content/dam/belkin/wemo/digital/promo_pod");
		add("/content/dam/belkin/wemo/digital/social");
		add("/content/dam/belkin/wemo/events/pop-up_banners");
		add("/content/dam/belkin/wemo/events/product_place_cards");
		add("/content/dam/belkin/wemo/guides/product_highlights");
		add("/content/dam/belkin/wemo/logos/corporate_logos");
		add("/content/dam/belkin/wemo/logos/logo_guidelines");
		add("/content/dam/belkin/wemo/packaging/concepts");
		add("/content/dam/belkin/wemo/packaging/design_layouts");
		add("/content/dam/belkin/wemo/packaging/mocks");
		add("/content/dam/belkin/wemo/packaging/stickers");
		add("/content/dam/belkin/wemo/photography/corporate");
		add("/content/dam/belkin/wemo/photography/device_screens");
		add("/content/dam/belkin/wemo/photography/in_use");
		add("/content/dam/belkin/wemo/photography/lifestyle");
		add("/content/dam/belkin/wemo/photography/packaging");
		add("/content/dam/belkin/wemo/photography/product/2008");
		add("/content/dam/belkin/wemo/photography/product/2009");
		add("/content/dam/belkin/wemo/photography/product/2010");
		add("/content/dam/belkin/wemo/photography/product/2011");
		add("/content/dam/belkin/wemo/photography/product/2012");
		add("/content/dam/belkin/wemo/photography/product/2013");
		add("/content/dam/belkin/wemo/photography/product/2014");
		add("/content/dam/belkin/wemo/photography/product/2015");
		add("/content/dam/belkin/wemo/photography/product/2016");
		add("/content/dam/belkin/wemo/photography/stock_image");
		add("/content/dam/belkin/wemo/print/ads");
		add("/content/dam/belkin/wemo/print/brochure");
		add("/content/dam/belkin/wemo/print/card");
		add("/content/dam/belkin/wemo/print/catalog");
		add("/content/dam/belkin/wemo/print/display_card");
		add("/content/dam/belkin/wemo/print/flyer");
		add("/content/dam/belkin/wemo/video/b-roll");
		add("/content/dam/belkin/wemo/video/brand");
		add("/content/dam/belkin/wemo/video/demos");
		add("/content/dam/belkin/wemo/video/hi_im_jennifer");
		add("/content/dam/belkin/wemo/video/how_to");
		add("/content/dam/belkin/wemo/video/media_clip");
		add("/content/dam/belkin/wemo/video/product");
		add("/content/dam/belkin/wemo/illustrations_&_diagrams/diagrams");
		add("/content/dam/belkin/wemo/illustrations_&_diagrams/illustrations");
		add("/content/dam/belkin/wemo/packaging_inserts/promotional_insert");
		add("/content/dam/belkin/wemo/pos_in-store/ctu");
		add("/content/dam/belkin/wemo/pos_in-store/end_cap");
		add("/content/dam/belkin/wemo/pos_in-store/fsdu");
		add("/content/dam/belkin/wemo/pos_in-store/in-line");
		add("/content/dam/belkin/wemo/pos_in-store/other_signage");
		add("/content/dam/belkin/wemo/pos_in-store/pdq");
		add("/content/dam/belkin/wemo/pos_in-store/pog");
		add("/content/dam/belkin/wemo/pos_in-store/posters");
		add("/content/dam/belkin/wemo/pos_in-store/print_banner");
		add("/content/dam/belkin/wemo/pos_in-store/pull-up_banner");
		add("/content/dam/belkin/wemo/pos_in-store/shelf_talker");
		add("/content/dam/belkin/wemo/pos_in-store/shipper");
		add("/content/dam/belkin/wemo/pos_in-store/tabletop_display");
		add("/content/dam/belkin/wemo/sales_support/product_bulletins");
		add("/content/dam/belkin/wemo/sales_support/sales_playbook");
		add("/content/dam/belkin/wemo/sales_support/sales_sheets");
		add("/content/dam/belkin/wemo/sales_support/toolkits");
		add("/content/dam/belkin/wemo/video/brand");
	}};
	
	//these should be passed in on the command line
	
	private static HashMap<String, String> assetIDMap = new HashMap<String, String>();
	
	private static Options options = new Options();
	
	private static final Logger log = Logger.getLogger(CountAssetsByDirectory.class.getName());
	
//	static String REPOLOCATION = "http://192.168.233.51:4502";
	static String REPOLOCATION = "http://52.86.77.252:4502";
	static String REPOUSER = "admin";
	//static String REPOPASSWORD = "admin";
	static String REPOPASSWORD = "O9Y{]w8CHurN";
	static String IDFILENAME = "/Users/adamtrissel/projects/belkin/assets/data/assetIDs.txt";
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
		String filename = cliHash.get("filename");
		
		log.log(Level.INFO, "Starting to create asset count map");
		getDamAssets(repo, username, password);
		log.log(Level.INFO, "Finished creating asset count map");
		
	}
	
	private static HashMap<String, String> getCommandLineArgs(String[] args){
		
		HashMap<String, String> cliHash = new HashMap<String, String>();
		String repo = REPOLOCATION;
		String username = REPOUSER;
		String password = REPOPASSWORD;
		String filename = IDFILENAME;
		
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
	
	private static int countAssets(Session session){
		
		int count = 0;
		
		for (String directory : directoryArray){
			
			try{
			
				String q = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE([" + directory + "]) and [jcr:primaryType] = 'dam:Asset'";
				
				Query myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
				
				QueryResult result = myQuery.execute();
				
				NodeIterator ni = result.getNodes();
				
				long nodeCounter = ni.getSize();
				
				System.out.println(directory + " : " + nodeCounter);

			} catch (Exception e){
				
				System.out.println("Error, could not find: " + directory + " : " + e.getMessage());
				
			}
			
		}
		
		return count;
	}
	
	private static void getDamAssets(String repo, String username, String password) throws Exception {
		
		HashMap<String,AssetBean> damAssets = new HashMap<String,AssetBean>();

		CqHelper helper = new CqHelper();
		
		Session session = helper.getSession(repo, username, password);
		
		countAssets(session);
		
		session.logout();
		
	}

}
