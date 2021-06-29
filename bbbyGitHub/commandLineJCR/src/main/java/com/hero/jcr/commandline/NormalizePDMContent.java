package com.hero.jcr.commandline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/*
 * This program seperates the classification spreadsheet into two logical files, one with the data for the primary asset
 * and one with the repeated elements (pdm metadata)
 */

public class NormalizePDMContent {
	
	private static Options options = new Options();
	static String DEFAULTFILE = "/Users/ba38752/Desktop/migration1.csv";
	static String DEFAULT_UNIQUE_ASSET_FILE = "/Users/ba38752/Desktop/uniqueAssets.csv";
	static String DEFAULT_PDM_DATA_FILE = "/Users/ba38752/Desktop/pdmAssets.csv";
	
	private static final Logger log = Logger.getLogger(NormalizePDMContent.class.getName());
	
	private static void processFile(String fileName, String uniqueName, String pdmFileName) throws Exception {
		
		Path file = Paths.get(fileName);
        BufferedReader reader = Files.newBufferedReader(file,Charset.forName("UTF-8"));
        
        BufferedWriter assetFilewriter = new BufferedWriter(new FileWriter(uniqueName));
        BufferedWriter pdmFilewriter = new BufferedWriter(new FileWriter(pdmFileName));
        
        StringBuilder content = new StringBuilder();
        String line = null;

        long recordCounter = 0;
        String priorFileName = "StartOfRun";
        
        while ((line = reader.readLine()) != null) {
        	
            String[] columns = line.split("\t");
            
            if (columns.length >= 42){

            	AssetBean ab = new AssetBean(columns);

            	if (!columns[1].equals(priorFileName)) {
            	
	            	recordCounter++;
	
	            	System.out.println("new asset!" + columns[1]);
	            	priorFileName = columns[1];
	            	
	            	PrimaryAssetBean pab = new PrimaryAssetBean(ab);
	            	PDMAssetBean pdab = new PDMAssetBean(ab);
	            	writeToUniqueFile(assetFilewriter, pab);
	            	writeToPDMFile(pdmFilewriter, pdab);
	            	
	            	
	        	} else {
	        		
	        		System.out.println("Additional PDM Metadata for asset: " + columns[1]);
	        		PDMAssetBean pab = new PDMAssetBean(ab);
	        		writeToPDMFile(pdmFilewriter, pab);
	        		
	        	}
	
            } else {
            	
            	System.out.println("column length is incorrect needs to be 39: " + columns.length);
            	
            }
            
        }	
        
        reader.close();
        assetFilewriter.close();
        pdmFilewriter.close();
		
	}
	
	private static void writeToUniqueFile(BufferedWriter assetFilewriter, PrimaryAssetBean ab) throws Exception {
		
		assetFilewriter.write(ab.getFormattedOutput());
		
	}
	
	private static void writeToPDMFile(BufferedWriter pdmFilewriter, PDMAssetBean ab) throws Exception {
		
		pdmFilewriter.write(ab.getFormattedOutput());
		
	}

	private static HashMap<String, String> getCommandLineArgs(String[] args){
		
		HashMap<String, String> cliHash = new HashMap<String, String>();
		String filename = DEFAULTFILE;
		String uniqueAssetFile = DEFAULT_UNIQUE_ASSET_FILE;
		String pdmDataFile = DEFAULT_PDM_DATA_FILE;
		
		
		options.addOption("f", "filename", true, "Input file name");
		options.addOption("u", "unique", true, "Input unique asset file name");
		options.addOption("p", "pdm", true, "Input PDM file name");
		
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		
		try{
			
			cmd = parser.parse(options, args);
			
			if(cmd.hasOption('u')){
				uniqueAssetFile = cmd.getOptionValue("u");
			}
			
			if(cmd.hasOption('p')){
				pdmDataFile = cmd.getOptionValue("p");
			}
			
			if(cmd.hasOption('f')){
				filename = cmd.getOptionValue("f");
			}

		} catch (ParseException e){
			
		   log.log(Level.SEVERE, "Failed to parse comand line properties", e);
			
		}
		
		cliHash.put("unique", uniqueAssetFile);
		cliHash.put("pdm", pdmDataFile);
		cliHash.put("filename", filename);
		
		return cliHash;
		
	}	
	
	public static void main(String[] args) throws Exception {
		
		
		
		HashMap<String, String> cliHash = getCommandLineArgs(args);
		
		processFile(cliHash.get("filename"),
				    cliHash.get("unique"),
					cliHash.get("pdm")
					);
		
	}
	
	

}
