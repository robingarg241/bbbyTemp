package com.bbby.aem.core.processes.impl;


import com.adobe.acs.commons.fam.ActionManager;
import com.adobe.acs.commons.fam.Failure;
import com.adobe.acs.commons.fam.actions.Actions;
import com.adobe.acs.commons.mcp.ProcessInstance;
import com.adobe.acs.commons.mcp.form.FormField;
import com.adobe.acs.commons.mcp.form.PasswordComponent;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobInputStream;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.CloudBlob; 
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.day.cq.commons.jcr.JcrUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.mime.MimeTypeService;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class AzureAssetIngestor extends AssetIngestor {

	private static final Logger log = Logger.getLogger(AzureAssetIngestor.class.getName());	
	
    public AzureAssetIngestor(MimeTypeService mimeTypeService) {
        super(mimeTypeService);
    }

    @FormField(
            name = "Blob Container",
            description = "Azure Blob Container",
            options = {"default=adobe-aem"}
    )
    String bucket;

    @FormField(
            name = "Account Name",
            description = "Azure Account Name. Leave blank if using SAS",
            required = false
    )
    String accountName;

    @FormField(
            name = "Account Key",
            description = "Azure Account Key. Leave blank if using SAS",
            //component = PasswordComponent.class,
            options = {"default=7sp2TxqRBfJeJnpNQMroYdju9sMDvza4cvUojUPuhg5GElT0nOXDkl8+McHnj2SelCgKxLYG7xn0BZ1akEfHmA=="}
    )
    String accountKey;

    @FormField(
            name = "Blob Endpoint",
            description = "Blob Endpoint (Use with SAS)",
            required = false,
            options = {"default=https://adobeaempoc.blob.core.windows.net"}
    )
    String blobEndpoint;
    
    @FormField(
            name = "Shared Access Signature",
            description = "Shared Access Signature",
            required = false
            //sp=rl&st=2019-05-28T14:55:23Z&se=2019-12-31T14:55:00Z&sv=2018-03-28&sig=k8rmZ4St%2FZXsBKkHLd2SrOjTu%2FJA5fOu4ldJka9Fob8%3D&sr=c
    )
    String saSignature;
    
    @FormField(
            name = "Base Path",
            description = "Optional Base Path (Folder)",
            required = false
    )
    String basePath;

    transient CloudBlobClient blobClient = null;
    
    transient CloudBlobContainer container = null;
    
    transient String baseItemName;

    @Override
    public void init() throws RepositoryException {
        super.init();
        if (StringUtils.isNotBlank(basePath)) {
            baseItemName = bucket + ":" + basePath;
        } else {
            baseItemName = bucket;
        }
        
    }

    @Override
    public void buildProcess(ProcessInstance instance, ResourceResolver rr) throws LoginException, RepositoryException {
        if (StringUtils.isNotBlank(basePath) && !basePath.endsWith("/")) {
        	basePath = basePath + "/";
        }
        instance.getInfo().setDescription(baseItemName + "->" + jcrBasePath);
        instance.defineCriticalAction("Create Folders", rr, this::createFolders);
        instance.defineCriticalAction("Import Assets", rr, this::importAssets);
        
        String storageConnectionString = "";
        
        // If accountName is present, use account+key to connect
        if(StringUtils.isNotBlank(accountName)) {
        	storageConnectionString =
  		          "DefaultEndpointsProtocol=https;" +
  		          "AccountName=" + accountName + ";" +
  		          "AccountKey="+ accountKey;
        	
        // if account is not present, use blob endpoint URL and SAS to connect
        	
        //BlobEndpoint=https://storagesample.blob.core.windows.net;
        //SharedAccessSignature=sv=2015-04-05&sr=b&si=tutorial-policy-635959936145100803&sig=9aCzs76n0E7y5BpEi2GvsSv433BZa22leDOZXX%2BXXIU%3D
        } else if(StringUtils.isNotBlank(blobEndpoint)) {
        	
        	storageConnectionString =
    		          "BlobEndpoint=" + blobEndpoint + ";" +
    		          "SharedAccessSignature=" + saSignature;
        }
        
	     CloudStorageAccount storageAccount;
	     
	      try {    
	            // Parse the connection string and create a blob client to interact with Blob storage
	            storageAccount = CloudStorageAccount.parse(storageConnectionString);
	            blobClient = storageAccount.createCloudBlobClient();
	            
	            container = blobClient.getContainerReference(bucket);
	            
	      } catch (Exception e) {
	          e.printStackTrace();
	      }
    }

    void createFolders(ActionManager manager) {
        manager.deferredWithResolver(r -> {
            JcrUtil.createPath(jcrBasePath, DEFAULT_FOLDER_TYPE, DEFAULT_FOLDER_TYPE, r.adaptTo(Session.class), true);
            manager.setCurrentItem(baseItemName);

            Collection<ListBlobItem> listing = new ArrayList<ListBlobItem>();
            //ObjectListing listing = s3Client.listObjects(bucket, s3BasePath);
            
            Iterable<ListBlobItem> blobs = null;
            
            if (StringUtils.isNotBlank(basePath)) { 
            	CloudBlobDirectory folder = container.getDirectoryReference(basePath);
            	blobs = folder.listBlobs();
            } else {
            	//OperationContext op = new OperationContext();
            	//blobs = container.listBlobs(null, false, EnumSet.noneOf(BlobListingDetails.class), null, op);
            	//null, false, EnumSet.noneOf(BlobListingDetails.class), null, op
            	
            	blobs = container.listBlobs();
            	
            }
            
            for (ListBlobItem blob : blobs) {
            	log.info("blob item " + blob.getUri().toString());
            	
                if (blob instanceof CloudBlobDirectory) {
                	listing.add(blob);
                    log.info(String.format("\t\t%s\t: %s", ((CloudBlobDirectory) blob).getPrefix(), blob.getUri().toString()));
                }
            }
            
            createFolders(manager, listing);
        });
    }

    private void createFolders(ActionManager manager, Collection<ListBlobItem> listing) {
        listing.stream().map(S3HierarchicalElement::new)
                .filter(S3HierarchicalElement::isFolder).filter(this::canImportFolder).forEach(el -> {
            manager.deferredWithResolver(Actions.retry(retries, retryPause, rr -> {
                manager.setCurrentItem(el.getItemName());
                createFolderNode(el, rr);
            }));
        });
        
    }

    void importAssets(ActionManager manager) {
        manager.deferredWithResolver(rr -> {
        	
        	Collection<ListBlobItem> listing = new ArrayList<ListBlobItem>();
            JcrUtil.createPath(jcrBasePath, DEFAULT_FOLDER_TYPE, DEFAULT_FOLDER_TYPE, rr.adaptTo(Session.class), true);
            
            manager.setCurrentItem(baseItemName);
            
            log.info(String.format("\tContainer: %s", container.getName()));
            
            Iterable<ListBlobItem> blobs = null;
            
            if (StringUtils.isNotBlank(basePath)) { 
            	CloudBlobDirectory folder = container.getDirectoryReference(basePath);
            	blobs = folder.listBlobs();
            	//blobs = container.listBlobs(s3BasePath, true);
            } else {
            	blobs = container.listBlobs();
            }
            
            for (ListBlobItem blob : blobs) {
            	this.listObjects(blob, listing);
            }
            
            //ObjectListing listing = s3Client.listObjects(bucket, s3BasePath);
            importAssets(manager, listing);
        });
    }

	private void listObjects(ListBlobItem item, Collection<ListBlobItem> listing)
			throws StorageException, URISyntaxException {

		if (item instanceof CloudBlob) {
			listing.add(item);
			log.info(String.format("\t\t%s\t: %s", ((CloudBlob) item).getProperties().getBlobType(), ((CloudBlob) item).getUri().toString()));

		} else if (item instanceof CloudBlobDirectory) {
			for (ListBlobItem blob : ((CloudBlobDirectory) item).listBlobs()) {
				listObjects(blob, listing);
			}

		}
	}
    
    private void importAssets(ActionManager manager, Collection<ListBlobItem> listing) {
        listing.stream().map(S3HierarchicalElement::new)
                .map(S3HierarchicalElement::getSource).forEach(ss -> {
            try {
                if (canImportFile(ss)) {
                    manager.deferredWithResolver(Actions.retry(retries, retryPause, importAsset(ss, manager)));
                } else {
                    incrementCount(skippedFiles, 1);
                    trackDetailedActivity(ss.getName(), "Skip", "Skipping file", 0L);
                }
            } catch (IOException ex) {
                Failure failure = new Failure();
                failure.setException(ex);
                failure.setNodePath(ss.getElement().getNodePath(preserveFileName));
                manager.getFailureList().add(failure);
            } finally {
                try {
                    ss.close();
                } catch (IOException ex) {
                    Failure failure = new Failure();
                    failure.setException(ex);
                    failure.setNodePath(ss.getElement().getNodePath(preserveFileName));
                    manager.getFailureList().add(failure);
                }
            }
        });
        //if (listing.isTruncated()) {
       //     importAssets(manager, s3Client.listNextBatchOfObjects(listing));
       // }
    }

    private class AzureBlobSource implements Source {

        private BlobInputStream lastOpenStream;
        ListBlobItem blobItem;
        final HierarchicalElement element;

        private AzureBlobSource(ListBlobItem s3ObjectSummary, S3HierarchicalElement element) {
            this.blobItem = s3ObjectSummary;
            this.element = element;
        }

        @Override
        public long getLength() {
        	if((blobItem instanceof CloudBlockBlob)) {
            	return ((CloudBlockBlob)blobItem).getProperties().getLength();
            } else {
            	return 0;
            }
        }

        @Override
        public InputStream getStream() throws IOException {
            close();
            
            if(!(blobItem instanceof CloudBlockBlob)) {
            	return null;
            }
            //lastOpenStream = s3Client.getObject(bucket, s3ObjectSummary.getKey()).getObjectContent();
            //CloudBlockBlob blockBlob2 = container.getBlockBlobReference("blockblob2.tmp");
            try {
				lastOpenStream = ((CloudBlockBlob)blobItem).openInputStream();
			} catch (StorageException e) {
				throw new IOException(e);
			}
            
            return lastOpenStream;
        }

        @Override
        public String getName() {
            return element.getName();
        }

        @Override
        public HierarchicalElement getElement() {
            return element;
        }

        @Override
        public void close() throws IOException {
            if (lastOpenStream != null) {
                lastOpenStream.close();
            }
            lastOpenStream = null;  
        }
    }

    class S3HierarchicalElement implements HierarchicalElement {

    	  final ListBlobItem original;
            final String negativePath;
            //final String effectiveKey;

            S3HierarchicalElement(ListBlobItem original) {
                this(original, null);
            }

            private S3HierarchicalElement(ListBlobItem original, String negativePath) {
                this.original = original;
                this.negativePath = negativePath != null ? negativePath : "";
                //this.effectiveKey = original.getKey().substring(0, original.getKey().length() - this.negativePath.length());
            }

            @Override
            public Stream<HierarchicalElement> getChildren() {
                throw new UnsupportedOperationException("S3 Elements do not support navigation children directly");
            }        
            
            @Override
            public boolean isFile() {
                return !isFolder();
            }

            @Override
            public boolean isFolder() {
                return original.getUri().toString().endsWith("/");
            }

            @Override
            public HierarchicalElement getParent() {
            	//CloudBlobDirectory parent = original.getParent();
            	//return new S3HierarchicalElement(parent, null);
            	return null;
                
            }

            @Override
            public String getName() {
            	if(original instanceof CloudBlockBlob) {
                	return ((CloudBlockBlob)original).getName();
                } else if (original instanceof CloudBlobDirectory) {
                	String prefix = ((CloudBlobDirectory)original).getPrefix();
                	return (prefix.endsWith("/")) ? prefix.substring(0, prefix.length()-1): prefix;
                }
                return "";
            }

            @Override
            public String getItemName() {
                return getName();
            }

            @Override
            public Source getSource() {
                if (StringUtils.isNotBlank(negativePath)) {
                    return null;
                } else {
                    return new AzureBlobSource(original, this);
                }
            }

            @Override
            public String getJcrBasePath() {
                return jcrBasePath;
            }

            @Override
            public String getSourcePath() {
                return getItemName();
            }
    }
}