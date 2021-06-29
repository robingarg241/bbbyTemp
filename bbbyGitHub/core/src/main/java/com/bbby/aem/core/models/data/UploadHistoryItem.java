package com.bbby.aem.core.models.data;

import java.util.Date;

/**
 * The Class UploadHistoryItem.
 *
 * @author karthik.koduru
 */
public class UploadHistoryItem {

    /**
     * The batch name.
     */
    private String batchID;

    /**
     * The Requested By.
     */

    private String requestedBy;

    /**
     * The upload date.
     */
    private Date uploadDate;

    /**
     * The upload display date.
     */
    private String uploadDisplayDate;

    /**
     * The no of files.
     */
    private int noOfFiles;

    /**
     * Adobe unique ID of the batch
     */
    private String adobeUUID;
    
    /**
     * The total no of files.
     */
    private int totalNoOfFiles;
    
    /**
     * The no of Invalid files.
     */
    private int noOfInvalidFiles;
    
    /**
     * The no of failed to upload files.
     */
    private int noOfFailedToUploadFiles;

    /**
     * Gets the batch ID.
     *
     * @return the batch ID
     */
    public String getBatchID() {
        return batchID;
    }

    /**
     * Sets the batch ID.
     *
     * @param batchID the new batch ID
     */
    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    /**
     * Gets the requested by.
     *
     * @return the requested by
     */
    public String getRequestedBy() {
        return requestedBy;
    }

    /**
     * Sets the requested by.
     *
     * @param requestedBy the requested by
     */
    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }


    /**
     * Gets the upload date.
     *
     * @return the upload  date
     */
    public Date getUploadDate() {
        return uploadDate;
    }

    /**
     * Sets the upload display date.
     *
     * @param uploadDate the new upload date
     */
    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    /**
     * Gets the upload display date.
     *
     * @return the upload display date
     */
    public String getUploadDisplayDate() {
        return uploadDisplayDate;
    }

    /**
     * Sets the upload display date.
     *
     * @param uploadDisplayDate the new upload display date
     */
    public void setUploadDisplayDate(String uploadDisplayDate) {
        this.uploadDisplayDate = uploadDisplayDate;
    }

    /**
     * Gets the no of files.
     *
     * @return the no of files
     */
    public int getNoOfFiles() {
        return noOfFiles;
    }

    /**
     * Sets the no of files.
     *
     * @param noOfFiles the new no of files
     */
    public void setNoOfFiles(int noOfFiles) {
        this.noOfFiles = noOfFiles;
    }


    public String getAdobeUUID() {
        return adobeUUID;
    }

    public void setAdobeUUID(String adobeUUID) {
        this.adobeUUID = adobeUUID;
    }

	/**
	 * @return the totalNoOfFiles
	 */
	public int getTotalNoOfFiles() {
		return totalNoOfFiles;
	}

	/**
	 * @param totalNoOfFiles the totalNoOfFiles to set
	 */
	public void setTotalNoOfFiles(int totalNoOfFiles) {
		this.totalNoOfFiles = totalNoOfFiles;
	}

	/**
	 * @return the noOfInvalidFiles
	 */
	public int getNoOfInvalidFiles() {
		return noOfInvalidFiles;
	}

	/**
	 * @param noOfInvalidFiles the noOfInvalidFiles to set
	 */
	public void setNoOfInvalidFiles(int noOfInvalidFiles) {
		this.noOfInvalidFiles = noOfInvalidFiles;
	}

	/**
	 * @return the noOfFailedToUploadFiles
	 */
	public int getNoOfFailedToUploadFiles() {
		return noOfFailedToUploadFiles;
	}

	/**
	 * @param noOfFailedToUploadFiles the noOfFailedToUploadFiles to set
	 */
	public void setNoOfFailedToUploadFiles(int noOfFailedToUploadFiles) {
		this.noOfFailedToUploadFiles = noOfFailedToUploadFiles;
	}
}
