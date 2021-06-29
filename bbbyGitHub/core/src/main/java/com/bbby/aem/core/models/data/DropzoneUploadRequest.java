package com.bbby.aem.core.models.data;

import java.util.List;

public class DropzoneUploadRequest {
    private String dzuuid;
    private String dzchunkindex;
    private String dztotalfilesize;
    private String dzchunksize;
    private String dztotalchunkcount;
    private String dzchunkbyteoffset;
    private String campaignName;
    private String userContact;
    private String brand;
    private String liveDate;
    private String note;
    private List<UploadItem> uploadList;
    private String startDate;
    private String finalDate;
    private String agreement;
    private String batchUuid;
    private String file;
    private String fileOrder;

    public String getDzuuid() {
        return dzuuid;
    }

    public void setDzuuid(String dzuuid) {
        this.dzuuid = dzuuid;
    }

    public String getDzchunkindex() {
        return dzchunkindex;
    }

    public void setDzchunkindex(String dzchunkindex) {
        this.dzchunkindex = dzchunkindex;
    }

    public String getDztotalfilesize() {
        return dztotalfilesize;
    }

    public void setDztotalfilesize(String dztotalfilesize) {
        this.dztotalfilesize = dztotalfilesize;
    }

    public String getDzchunksize() {
        return dzchunksize;
    }

    public void setDzchunksize(String dzchunksize) {
        this.dzchunksize = dzchunksize;
    }

    public String getDztotalchunkcount() {
        return dztotalchunkcount;
    }

    public void setDztotalchunkcount(String dztotalchunkcount) {
        this.dztotalchunkcount = dztotalchunkcount;
    }

    public String getDzchunkbyteoffset() {
        return dzchunkbyteoffset;
    }

    public void setDzchunkbyteoffset(String dzchunkbyteoffset) {
        this.dzchunkbyteoffset = dzchunkbyteoffset;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getUserContact() {
        return userContact;
    }

    public void setUserContact(String userContact) {
        this.userContact = userContact;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getLiveDate() {
        return liveDate;
    }

    public void setLiveDate(String liveDate) {
        this.liveDate = liveDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<UploadItem> getUploadList() {
        return uploadList;
    }

    public void setUploadList(List<UploadItem> uploadList) {
        this.uploadList = uploadList;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(String finalDate) {
        this.finalDate = finalDate;
    }

    public String getAgreement() {
        return agreement;
    }

    public void setAgreement(String agreement) {
        this.agreement = agreement;
    }

    public String getBatchUuid() {
        return batchUuid;
    }

    public void setBatchUuid(String batchUuid) {
        this.batchUuid = batchUuid;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFileOrder() {
        return fileOrder;
    }

    public void setFileOrder(String fileOrder) {
        this.fileOrder = fileOrder;
    }
}
