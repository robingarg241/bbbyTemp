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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;

import com.microsoft.azure.storage.CloudStorageAccount; 
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import com.hero.jcr.util.PartitionUtil;


public class AzureFileMigrationManager {
	
	static final String REPOLOCATION = "http://40.117.135.191:4502";
	//static final String REPOLOCATION = "http://aem-bbby-auth.herodigital.com:4502";
	static final String REPOUSER = "admin";
	static final String REPOPASSWORD = "tUq4Ju%gN>UxB34SFhNeh85w";
	//static final String REPOPASSWORD = "admin";
	//static final String REPOPASSWORD = "M4C@rjdN*m58!S1e";
	static final String DEFAULTFILE = "/Users/adamtrissel/projects/bbby/scripts/migration/data/uniqueAssetFile.csv";
	static final String DEFAULTJCRROOT = "/content/dam/bbby";
	static final String BUCKETNAME = "adobe-aem";
    // Retrieve the credentials and initialize SharedKeyCredentials
    private static final String DEFAULTACCESSKEY = "atrisselbbb";
    private static final String DEFAULTSECRETKEY = "7sp2TxqRBfJeJnpNQMroYdju9sMDvza4cvUojUPuhg5GElT0nOXDkl8+McHnj2SelCgKxLYG7xn0BZ1akEfHmA==";
    
    private static final String DEFAULTENDPOINT = "https://adobeaempoc.blob.core.windows.net";
    private static final String DEFAULTSIGNATURE = "sp=rl&st=2019-05-28T14:55:23Z&se=2019-12-31T14:55:00Z&sv=2018-03-28&sig=k8rmZ4St%2FZXsBKkHLd2SrOjTu%2FJA5fOu4ldJka9Fob8%3D&sr=c";
	
    //private static final String DEFAULTAUTH = "ACCESSKEY";
	private static final String DEFAULTAUTH = "URL";
	private static final String DEFAULTMODE = "RUN";
	//private static final String DEFAULTMODE = "TEST";
    
    
    private static final Logger log = Logger.getLogger(AzureFileMigrationManager.class.getName());	
	
	private static Options options = new Options();

	public static void main(String[] args) throws java.lang.Exception {
		
		HashMap<String, String> cliOptions = getCommandLineArgs(args);
		
		String host = cliOptions.get("repo");
		String path = cliOptions.get("jcrroot");
		String user = cliOptions.get("username");
		String password = cliOptions.get("password");
		String assetFileName = cliOptions.get("filename");
		String signature = cliOptions.get("signature");
		String endpoint = cliOptions.get("endpoint");
		String auth = cliOptions.get("auth");
		
		String accountName=cliOptions.get("accesskey");
		String accountKey=cliOptions.get("secretkey");
		String bucketName = cliOptions.get("bucketname");
		
		String mode = cliOptions.get("mode");
		
		Sardine sardine = SardineFactory.begin(user, password);
		
        // Create a ServiceURL to call the Blob service. We will also use this to construct the ContainerURL
        //SharedKeyCredentials creds = new SharedKeyCredentials(accountName, accountKey);
		String storageConnectionString = "";
		
		if (auth.contentEquals("ACCESSKEY")) {
			
			storageConnectionString =
			          "DefaultEndpointsProtocol=https;" +
			          "AccountName=" + accountName + ";" +
			          "AccountKey="+ accountKey;
			
		} else {
			
			storageConnectionString =
			          "BlobEndpoint=" + endpoint + ";" +
		    		  "SharedAccessSignature=" + signature;
			
			
		}
		
	     CloudStorageAccount storageAccount;
	     CloudBlobClient blobClient = null;
	     CloudBlobContainer container=null;
	     
	      try {    
	    	  
	            // Parse the connection string and create a blob client to interact with Blob storage
	            storageAccount = CloudStorageAccount.parse(storageConnectionString);
	            blobClient = storageAccount.createCloudBlobClient();
	            container = blobClient.getContainerReference(bucketName);
	      
	      } catch (Exception e) {
	      
	    	  e.printStackTrace();
	      
	      }
        
//        // We are using a default pipeline here, you can learn more about it at https://github.com/Azure/azure-storage-java/wiki/Azure-Storage-Java-V10-Overview
//        final ServiceURL serviceURL = new ServiceURL(new URL("https://" + accountName + ".blob.core.windows.net"), StorageURL.createPipeline(creds, new PipelineOptions()));
//
//        // Let's create a container using a blocking call to Azure Storage
//        // If container exists, we'll catch and continue
//        ContainerURL containerURL = serviceURL.createContainerURL("bbby");		
		
		try{

			Path file = Paths.get(assetFileName);
	        BufferedReader reader = Files.newBufferedReader(file,Charset.forName("UTF-8"));
	        //StringBuilder content = new StringBuilder();
	        String line = null;
	        
	        long recordCounter = 0;
	        ExecutorService executor = Executors.newFixedThreadPool(5);
	        
	        while ((line = reader.readLine()) != null) {
	        	
	        	recordCounter++;
	        	
	        	PrimaryAssetBean ab = new PrimaryAssetBean(line);
	        	
	        	String AzureFilePath = ab.getFullpath().replace("./", "");
	        	
	        	ArrayList<String> partitions = PartitionUtil.hashFileName(ab.getAsset_Type_Level_4().toLowerCase().replace(" ", "_"));
	        	
				if(mode.contentEquals("RUN")) {

		        	createPartition(host, sardine, path + partitions.get(0) + "/" + partitions.get(1), user, password);
		        	createPartition(host, sardine, path + partitions.get(0) + "/" + partitions.get(1) + "/" + partitions.get(2), user, password);
		        	
		        	
		        	String JCRFilename = ab.getAsset_Type_Level_4().toLowerCase().replace(" ", "_") + "/" + ab.getFullname();
		        	
					if (JCRFilename.startsWith("/")) {
						JCRFilename = JCRFilename.substring(1);
					}
		        	
					try{
						
						System.out.println("Processing: " + recordCounter + " :" + AzureFilePath + " -> " + path + "/" + JCRFilename) ;
						DownloadThread dt = new DownloadThread(container, AzureFilePath, JCRFilename, host, sardine, path, user, password);
					       
						executor.execute(dt);

						//byte[] data = getFileFromAzure(container, AzureFilePath);
						
						//webDavPostAEM(data, JCRFilename, host, sardine, path, user, password);			
						
					}catch(Exception e){
						
						System.out.println("failed to move file: " + JCRFilename + " : " + e.getMessage());
						e.printStackTrace();
					
					}
					
				} else {
					
					long length = checkFileFromAzure(container, AzureFilePath);
					
					System.out.println(recordCounter + " : " + ab.getFullpath() + " byteLength: " + length + " numberOfColumns: " + line.split("\t").length);
					
				}
				
	        }
			
			reader.close();
			executor.shutdown();
        	
		} catch (Exception e){
			
			System.out.println("Failed to open asset definition file: " + assetFileName + " : " + e.getMessage());
			e.printStackTrace();
			
		}
		
	}
	
	private static void createPartition(
			  String host, 
			  Sardine sardine2,
			  String path, 
			  String user, 
			  String password
			) {
		
		try {

			Sardine sardine = SardineFactory.begin(user, password);
			
			if (!sardine.exists(host + path)) {
				
				sardine.createDirectory(host + path);
				
			}
			
		} catch (Exception e) {
			
			//System.out.println("failed to create partition in JCR (Most likely already exists): " + path + " : " + e.getMessage());
			
		}
		
	}

	private static String getFileRoot(String fullPath){
		
		String[] elements = fullPath.split("/");
		
		String root = elements[elements.length - 1];
		
		return root;
	}
	
	private static long checkFileFromAzure(CloudBlobContainer containerURL, String filename) throws Exception{
	    
	    CloudBlockBlob blockBlob = containerURL.getBlockBlobReference(filename);
	    blockBlob.downloadAttributes();
	    long fileByteLength = blockBlob.getProperties().getLength();
	    
	    return fileByteLength;
		
	}
	
	private static String flattenFileName(String filename){
		
		filename = filename.replace("(", "").replace(")", "");
		
		return filename;
	}
	
	private static HashMap<String, String> getCommandLineArgs(String[] args){
		
		HashMap<String, String> cliHash = new HashMap<String, String>();
		String repo = REPOLOCATION;
		String username = REPOUSER;
		String password = REPOPASSWORD;
		String filename = DEFAULTFILE;
		
		String endpoint = DEFAULTENDPOINT;
	    String signature = DEFAULTSIGNATURE;
		String auth = DEFAULTAUTH;		
		
		String accessKeyID = DEFAULTACCESSKEY;
		String secretKey = DEFAULTSECRETKEY;
		
		String jcrRoot = DEFAULTJCRROOT;
		String bucketName = BUCKETNAME;
		
		String mode = DEFAULTMODE;
		
		options.addOption("r", "repo", true, "Repo Location.");
		options.addOption("u", "username", true, "Repo Username.");
		options.addOption("p", "password", true, "Repo User Password.");
		options.addOption("f", "filename", true, "Input file name");

		options.addOption("e", "endpoint", true, "Blob Endpoint");
		options.addOption("S", "signature", true, "SA Signature");
		options.addOption("m", "auth", true, "Auth Method");
		
		
		options.addOption("a", "accesskey", true, "Azure Account Name");
		options.addOption("s", "secretkey", true, "Azure Account Key");
		options.addOption("j", "jcrroot", true, "JCR Root Directory - where are the files to be put");
		options.addOption("b", "bucket", true, "AWS Bucket Name");
		
		options.addOption("M", "mode", true, "run mode");
		
		
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
			
			if(cmd.hasOption('a')){
				accessKeyID = cmd.getOptionValue("a");
			}
			
			if(cmd.hasOption('s')){
				secretKey = cmd.getOptionValue("s");
			}
			
			if(cmd.hasOption('j')){
				jcrRoot = cmd.getOptionValue("j");
			}

			if(cmd.hasOption('b')){
				bucketName = cmd.getOptionValue("b");
			}
			
			if(cmd.hasOption('e')){
				endpoint = cmd.getOptionValue("e");
			}
			
			if(cmd.hasOption('S')){
				signature = cmd.getOptionValue("S");
			}
			
			if(cmd.hasOption('m')){
				auth = cmd.getOptionValue("m");
			}

			if(cmd.hasOption('M')){
				mode = cmd.getOptionValue("M");
			}


		} catch (ParseException e){
			
		   log.log(Level.SEVERE, "Failed to parse comand line properties", e);
			
		}
		
		cliHash.put("repo", repo);
		cliHash.put("username", username);
		cliHash.put("password", password);
		cliHash.put("filename", filename);
		cliHash.put("accesskey", accessKeyID);
		cliHash.put("secretkey", secretKey);
		cliHash.put("jcrroot", jcrRoot);
		cliHash.put("bucketname", bucketName);
		cliHash.put("signature", signature);
		cliHash.put("endpoint", endpoint);
		cliHash.put("auth", auth);
		cliHash.put("mode", mode);
		
		return cliHash;
		
	}	

}
