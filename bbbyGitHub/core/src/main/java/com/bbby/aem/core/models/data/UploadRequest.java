package com.bbby.aem.core.models.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.fileupload.util.Streams;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.sling.api.request.RequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author vpokotylo
 *
 */
public class UploadRequest {
    
	private final Logger log = LoggerFactory.getLogger(UploadRequest.class);
	
    /**
     * The Contact info
     */
    String requestedBy;
    
	String batchId;
    
    String email;

    UploadItem uploadItem;
    
    Map<String, RequestParameter[]> reqParams;
    
	Map<String, MetadataRow> metadataMap;
	
	UploadItem uploadRejectedItem;

    String batchUuid = "";

    String fileOrder = "0";
    
    int totalFilesCount;
    
    String invalidFiles;
    
    String acceptedFiles;

    public UploadRequest(final Map<String, RequestParameter[]> reqParams) throws IOException {
		super();
		this.requestedBy = Streams.asString(reqParams.get("requestedBy")[0].getInputStream());
		this.batchId = Streams.asString(reqParams.get("batchId")[0].getInputStream());
		this.email = Streams.asString(reqParams.get("email")[0].getInputStream());
		
		this.reqParams = reqParams;
	}
    

    /**
     * @return
     * @throws IOException
     */
    public UploadRequest buildMetadata() throws IOException {
    	
    	if(!reqParams.containsKey("batchUpload")) {
    		return this;
    	}
    	
    	RequestParameter batchUpload = reqParams.get("batchUpload")[0];
    	
    	boolean isBatchUpload = Boolean.parseBoolean(batchUpload.getString());
    	
    	return isBatchUpload ? this.buildMetadataFromExcel() : this.buildMetadataFromRequest();
    }
    
    /**
     * @return
     * @throws IOException
     */
    private UploadRequest buildMetadataFromExcel() throws IOException {
    	
    	XSSFWorkbook wb  = null;
    	
    	try
	    {
	          for (final Map.Entry<String, RequestParameter[]> pairs : reqParams.entrySet()) {
	            final String k = pairs.getKey();
	            final RequestParameter[] pArr = pairs.getValue();
	            final RequestParameter param = pArr[0];
	            final InputStream stream = param.getInputStream();
	            
	            if (param.isFormField() ) {
	              log.debug("Form field " + k + " with value " + Streams.asString(stream) + " detected.");
	            } else {
	              if(param.getName().equals("excelFile")) {
	            	  
	            	  wb = new XSSFWorkbook(stream);
	            	  // Use second sheet according to template
	            	  final XSSFSheet sheet = wb.getSheetAt(1);
	                  //rowCount = sheet.getLastRowNum();
	                  final Iterator<Row> rows = sheet.rowIterator();

	                  // skip the header
	                  rows.next();
	                  
	                  Iterable<Row> remainingRows = () -> rows;
	                  
	                  Map<String, MetadataRow> metadataMap = StreamSupport.stream(remainingRows.spliterator(), false)
	                          .map(this::createMetadata)
	                          .filter(Objects::nonNull)
	                          .collect(Collectors.toMap(MetadataRow::getFilename, x -> x, (x1, x2) -> {
	                              log.warn("Duplicate key found for {} in metadata file. Skipping duplicate entry.", x1.getFilename());
	                              return x1;
	                          }));
	                  
	                  this.setMetadataMap(metadataMap);
	              }
	              
	            }
	          }
	    
	      } catch (Exception e) {
	    	  log.error("Error while processing multipart.", e);
  	      throw new IOException(e.toString());
  	    } 	
    	
    	return this;
    }

    private MetadataRow createMetadata(Row row) {
    	MetadataRow metadataRow = new MetadataRow(row);
    	
    	
    	return metadataRow.buildMetadataFromExcel();
    }
    
    /**
     * Builds Metadata from request parameters
     * 
     * @return
     * @throws IOException
     */
    private UploadRequest buildMetadataFromRequest() throws IOException {
    	Map<String, MetadataRow> metadataMap = new HashMap<String, MetadataRow>();
    	
    	MetadataRow metadataRow = new MetadataRow(reqParams);
    	
    	metadataMap.put(this.getUploadItem().getFileName(), metadataRow.buildMetadata());
    	this.setMetadataMap(metadataMap);
    	return this;
    }
    /**
     * gets the agreement check status
     *
     * @return
     */

    public String getBatchUuid() {
        return batchUuid;
    }

    public void setBatchUuid(String batchUuid) {
        this.batchUuid = batchUuid;
    }

    public String getFileOrder() {
        return fileOrder;
    }

    public void setFileOrder(String fileOrder) {
        this.fileOrder = fileOrder;
    }
    
    public Map<String, MetadataRow> getMetadataMap() {
		return metadataMap;
	}


	public void setMetadataMap(Map<String, MetadataRow> metadataMap) {
		this.metadataMap = metadataMap;
	}
	
	public MetadataRow getMetadata(String fileName) {
	
		return metadataMap.get(fileName);
	}
	
    public String getRequestedBy() {
		return requestedBy;
	}
	public String getBatchId() {
		return batchId;
	}
	public String getEmail() {
		return email;
	}
	
	public boolean hasUploadItem() {
		return uploadItem != null;
	}

	public void setUploadItem(UploadItem uploadItem) {
		this.uploadItem = uploadItem;
	}
	
	public UploadItem getUploadItem() {
		return uploadItem;
	}

	public UploadItem getUploadRejectedItem() {
		return uploadRejectedItem;
	}

	public void setUploadRejectedItem(UploadItem uploadRejectedItem) {
		this.uploadRejectedItem = uploadRejectedItem;
	}
	
	public boolean hasUploadRejectedItem() {
		return uploadRejectedItem != null;
	}


	public int getTotalFilesCount() {
		return totalFilesCount;
	}


	public void setTotalFilesCount(int totalFilesCount) {
		this.totalFilesCount = totalFilesCount;
	}


	public String getInvalidFiles() {
		return invalidFiles;
	}


	public void setInvalidFiles(String invalidFiles) {
		this.invalidFiles = invalidFiles;
	}


	public String getAcceptedFiles() {
		return acceptedFiles;
	}


	public void setAcceptedFiles(String acceptedFiles) {
		this.acceptedFiles = acceptedFiles;
	}
	
}
