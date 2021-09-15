package com.bbby.aem.core.util;

// TODO: Auto-generated Javadoc


/**
 * Constants.
 */
public final class CommonConstants
{

	public static final String ASSET_UPDATE_TOPIC = "com/bbby/jobs/pdm-asset-update";
	
	public static final String SINGLE_ASSET_UPDATE_TOPIC = "com/bbby/jobs/single-pdm-asset-update";
	
	public static final String ASSET_MOVE_TOPIC = "com/bbby/jobs/asset-move";
	
	public static final String SEND_MAIL_TOPIC = "com/bbby/jobs/send-mail";
	
	public static final String FAST_TRACK_ASSET_PROCESS_TOPIC = "com/bbby/jobs/fast-track";
	
    /** The Constant DATA. */
    public static final String DATA = "data";

    /** The Constant FORWARD_SLASH. */
    public static final String FORWARD_SLASH = "/";

    /** The Constant VENDOR_ROOT_PATH. */
    public static final String VENDOR_ROOT_PATH = "/var/vendor/";

    /** The Constant EMPTY_STRING. */
    public static final String EMPTY_STRING = "";

    /** The Constant CAMPAIGN_NAME. */
    public static final String CAMPAIGN_NAME = "campaignName";

    /** The Constant USER_CONTACT. */
    public static final String USER_CONTACT = "userContact";

    /** The Constant BRAND. */
    public static final String BRAND = "brand";

    /** The Constant LIVE_DATE. */
    public static final String LIVE_DATE = "liveDate";

    /** The Constant NOTE. */
    public static final String NOTE = "note";

    /** The Constant START_DATE. */
    public static final String START_DATE = "startDate";

    /** The Constant FINAL_DATE. */
    public static final String FINAL_DATE = "finalDate";

    /** The Constant AGREEMENT. */
    public static final String AGREEMENT = "agreement";

    /** The Constant BATCH_NAME. */
    public static final String BATCH_NAME = "BatchName";

    /** The Constant NOTES. */
    public static final String NOTES = "notes";

    /** The Constant DISPLAY_START_DATE. */
    public static final String DISPLAY_START_DATE = "StartDate";

    /** The Constant DISPLAY_END_DATE. */
    public static final String DISPLAY_END_DATE = "EndDate";

    public static final String BATCH_UUID = "batchUuid";

    public static final String FILE_ORDER = "fileOrder";
    
    public static final String JCR_PATH = "JCR_PATH";
    
    public static final String ROOTPATH = "rootPath";


    /** The Constant DISPLAY_LIVE_DATE. */
    public static final String DISPLAY_LIVE_DATE = "LiveDate";

    /** The Constant AGREEMENT_CHECK. */
    public static final String AGREEMENT_CHECK = "AgreementCheck";

    /** The Constant AGREEMENT_STATUS. */
    public static final String AGREEMENT_STATUS = "AgreementStatus";

    /** The Constant DISPLAY_CAMPAIGN_NAME. */
    public static final String DISPLAY_CAMPAIGN_NAME = "CampaignName";

    /** The Constant CONTACT_NUMBER. */
    public static final String CONTACT_NUMBER = "ContactNumber";

    /** The Constant BRAND_NAME. */
    public static final String BRAND_NAME = "BrandName";

    /** The Constant FILE_TYPE. */
    public static final String FILE_TYPE = "image/jpeg";

    /** The Constant DATE_FORMAT. */
    public static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm";

    /** The Constant EYERYONE. */
    public static final String EYERYONE = "everyone";

    /** The Constant USER_COOKIE. */
    public static final String USER_COOKIE= "usr_c";

    /** The Constant JCR_CONTENT. */
    public static final String JCR_CONTENT = "jcr:content";
    
    /** The Constant JCR_CONTENT Node. */
    public static final String JCR_CONTENT_NODE = "./jcr:content";
    
    /** The Constant METADATA Node. */
    public static final String METADATA_NODE = "./jcr:content/metadata";
    
    /** The Constant METADATA IPTC Node. */
    public static final String METADATA_IPTC_NODE = "./jcr:content/metadata/Iptc4xmpCore:CreatorContactInfo";
    
    /** The Constant PDMMETADATA Node. */
    public static final String PDM_METADATA_NODE = "./jcr:content/upcmetadata";

    /** The Constant REPORTING_METADATA Node. */
    public static final String REPORTING_METADATA_NODE = "./jcr:content/reportingmetadata";
    
    public static final String REL_ASSET_REPORTING_METADATA = "jcr:content/reportingmetadata";
    
    /** The Constant OPERATIONAL_METADATA Node. */
    public static final String OPERATIONAL_METADATA_NODE = "./jcr:content/operationalmetadata";
    
    public static final String REL_ASSET_OPERATIONAL_METADATA = "jcr:content/operationalmetadata";

    /**
     * The Constant CQ_LAST_REPLICATION_ACTION.
     */
    public static final String CQ_LAST_REPLICATION_ACTION = "cq:lastReplicationAction";

    /** The Constant DATE_FORMATTER. */
    public static final String DATE_FORMATTER = "yyyy-MM-dd-HH-mm";
    
    /** The Constant DATE_FORMATTER_FULL. */
    public static final String DATE_FORMATTER_FULL = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /** The Constant DISPLAY_DATE_FORMATTER. */
    public static final String DISPLAY_DATE_FORMATTER = "MMM dd,yyyy";

    /** The Constant REVIEW_ASSET_DIRECTORY. */
    public static final String REVIEW_ASSET_DIRECTORY = "/content/revewAssets/";

    /** The Constant BATCH_ID */
    public static final String BATCH_ID = "batchId";
    
    /** The Constant EMAIL. */
    public static final String EMAIL = "email";
    /** The Constant REQUESTED BY. */
    public static final String REQUESTED_BY = "requestedBy";
    /** The Constant EXCEL FILE. */
    public static final String EXCEL_FILE = "excelFile";
    /** The Constant UPLOAD LIST. */
    public static final String UPLOAD_LIST = "uploadList";

    public static final String UNDERSCORE = "_";
    
    public static final String TOTAL_FILES_COUNT = "totalFilesCount";
    
    public static final String FILE_COUNT = "fileCount";
    
    public static final String FAIL_FILES_COUNT = "failFilesCount";
    
    public static final String INVALID_FILES_COUNT = "invalidFilesCount";
    
    public static final String INVALID_FILES = "invalidFiles";
    
    public static final String ACCEPTED_FILES = "acceptedFiles";
    
    public static final String FAILED_FILES = "failedFiles";
    /**
     *
     * Default Constructor.
     *
     * Private constructor to restrict instantiation.
     */
    private CommonConstants()
    {

    }

    /** project properties **/
    public  static final String PROJECT_EXPIRE_DATE = "project.expireDate";
    public  static final String PROJECT_MONTH = "bbby.row.cycleMonth";
    public  static final String PROJECT_YEAR = "bbby.row.cycleYear";


    /** asset properties **/
    public static final String LICENSE_START_DATE = "bbby.lice.start";
    public static final String ASSET_EXPIRE_DATE = "prism:expirationDate";
    public  static final String ASSET_MONTH = "bbby.row.cycleMonth";
    public  static final String ASSET_YEAR = "bbby.row.cycleYear";
    public static final String LAST_PDM_METADATA_UPDATE = "lastPDMMetadataUpdate";

    /** vendor portal properties **/
    public static final String NO_END_DATE = "No End Date";
    public static final String DAM_ASSET_STATE = "dam:assetState";
    public static final String PROCESSED = "processed";

    /**
     * PDM Metadata
     **/
    public static final String PRIMARY_UPC = "primaryUPC";
    public static final String SKU = "sku";
    public static final String PDM_BRAND = "brand";
    public static final String COLOR_CODE = "colorCode";
    public static final String COLOR_GROUP_CODE = "colorGroupCode";
    public static final String PRODUCT = "product";
    public static final String ASSOCIATED_WEB_PRODUCT_ID = "associatedWebProductID";
    public static final String ASSOCIATED_COLLECTION_ID = "associatedCollectionID";
    public static final String PDM_BATCH_ID = "pdmBatchID";
    public static final String DEPARTMENT_NAME = "departmentName";
    public static final String DEPARTMENT_NUMBER = "departmentNumber";
    public static final String PRIMARY_VENDOR_NUMBER = "primaryVendorNumber";
    public static final String PRIMARY_VENDOR_NAME = "primaryVendorName";
    public static final String VENDOR_DIRECT_TO_CUSTOMER_ITEM = "vendorDirectToCustomerItem";
    public static final String PRIORITY_FLAG = "priorityFlag";
    public static final String BBBY_WEB_DISABLED = "bbbyWebDisabled";
    public static final String BABY_WEB_DISABLED = "babyWebDisabled";
    public static final String CA_WEB_DISABLED = "caWebDisabled";
    public static final String BBBY_WEB_OFEERED_FLAG = "bbbyWebOfferedFlag";
    public static final String BABY_WEB_OFEERED_FLAG = "babyWebOfferedFlag";
    public static final String CA_WEB_OFEERED_FLAG = "caWebOfferedFlag";
    public static final String WEB_PRODUCT_ROLL_UP_TYPE = "webProductRollupType";
    public static final String MASTER_PRODUCT_DESCRIPTION = "masterProductDescription";
    public static final String FAST_TRACK_FLAG = "fastTrackFlag";
    public static final String PULLBACK_TO_MERCHANT = "pullbackToMerchant";

    /**
     * Metadata for PDM Asset Update
     **/
    public static final String JCR_CREATED_BY = "jcr:createdBy";
    public static final String JCR_CREATED = "jcr:created";
    public static final String JCR_UUID = "jcr:uuid";
    public static final String LAST_PDM_SYNC_ACTION = "lastPdmSyncAction";
    public static final String BBBY_ASSET_STATE = "bbby:assetState";
    public static final String BBBY_SWATCH = "swatch";
    public static final String BBBY_SWATCH_OVERRIDE = "bbby:swatchOverride";
    public static final String BBBY_SWATCH_OVERRIDE_NAME = "bbby:swatchOverrideName";
    public static final String BBBY_IMAGE_SET_NAME = "bbby:imageSetName";
    public static final String BBBY_ASSIGN_TO_WEB_PRODUCT_ID_LABEL = "bbby:assignToWebProductID";
    public static final String BBBY_ASSIGN_TO_WEB_PRODUCT_ID = "bbby:assetAssignmentMSWPID";
    public static final String BBBY_ASSIGN_TO_WEB_COLLECTION_ID_LABEL = "bbby:assignToCollectionID";
    public static final String BBBY_ASSIGN_TO_WEB_COLLECTION_ID = "bbby:assetAssignmentCollectionID";
    public static final String BBBY_PRIMARY_IMAGE = "bbby:primaryImage";
    public static final String BBBY_ASSET_UPDATE = "bbby:assetUpdate";
    public static final String BBBY_ASSET_TYPE_LABEL = "bbby:assetType";
    public static final String BBBY_ASSET_TYPE = "bbby:asset_type";
    public static final String BBBY_SHOT_TYPE_LABEL = "bbby:shotType";
    public static final String BBBY_SHOT_TYPE = "bbby:shot_type";
    public static final String BBBY_REJECTION_REASON = "bbby:rejectionReason";
    public static final String BBBY_SEQUENCE = "bbby:sequence";
    public static final String DAM_RELATIVE_PATH = "dam:relativePath";
    public static final String DAM_SCENE_7_FILENAME = "dam:scene7File";
    public static final String DAM_SCENE_7_FILE_STATUS = "dam:scene7FileStatus";
    public static final String DAM_MIME_TYPE = "dam:MIMEtype";
    public static final String RENDITIONS = "renditions";
    public static final String METADATA = "metadata";
    public static final String BBBY_UPC = "bbby:primaryUpc";
    public static final String BBBY_SKU = "bbby:sku";
    public static final String BBBY_DEPARTMENT_NAME = "bbby:departmentName";
    public static final String BBBY_DEPARTMENT_NUMBER = "bbby:departmentNumber";
    public static final String BBBY_CONTENT_OPS_REVIEW = "bbby:contentOpsReview";
    public static final String BBBY_IMAGESET_NIGHTLY_PUBLISH = "bbby:imagesetNightlyPublish";
    public static final String BBBY_ASSET_HASH = "bbby:assetHash";
    public static final String BBBY_ASSET_HASH_SEED = "bbby:assetHashSeed";
    public static final String BBBY_UPLOADED_ASSET_NAME = "bbby:uploadedAssetName";
    public static final String BBBY_ADDITIONAL_UPC = "bbby:additionalUPC";
    public static final String BBBY_WIDTH = "bbby:width";
    public static final String BBBY_HEIGHT = "bbby:height";
    public static final String BBBY_SIZE = "bbby:size";
    public static final String BBBY_COLOR_SPACE = "bbby:colorSpace";
    public static final String CQ_TAGS = "cq:tags";
    public static final String BBBY_DUMMY_SKU = "bbby:dummySku";
    
    public static final String BBBY_FAST_TRACK_ASSET = "assetSubmittedByFastTrack";
    public static final String BBBY_SHARED_ASSET = "isSharedAsset";
    
    //Asset Type and Shot Type for Marketing Assets
    public static final String MARKETING_ASSET_TYPE_LABEL = "marketing:assetType";
    public static final String MARKETING_ASSET_TYPE = "marketing:asset_type";
    public static final String MARKETING_SHOT_TYPE_LABEL = "marketing:shotType";
    public static final String MARKETING_SHOT_TYPE = "marketing:shot_type";
    
    /**
     * Operational Metadata for Asset Update
     **/
    public static final String OPMETA_PDM_CALL_SENT = "pdmCallSent";
    public static final String OPMETA_PDM_CALL_RECEIVED = "pdmCallReceived";
    public static final String OPMETA_PDM_CALL_COUNT = "pdmCallCount";
    public static final String OPMETA_PDM_WEB_OFFERED_FLAGS = "pdmWebOfferedFlags";
    public static final String OPMETA_HAS_PDM_ASSOCIATED_ID = "hasPdmAssociatedId";
    public static final String OPMETA_PC_Executed_Date = "pcExecutedDate";
    public static final String OPMETA_PC_WF_State = "pcWFState";
    public static final String OPMETA_NIGHTLY_PROCESSED_DATE = "nightlyProcessedDate";
    public static final String OPMETA_NIGHTLY_WF_STATE = "nightlyWFState";
    public static final String OPMETA_LAST_PDM_CALL_STATUS = "lastPdmCallStatus";
    public static final String OPMETA_MEMBERS_SEQUENCE = "membersSequence";
    
    /**
     * Reporting Metadata for Asset Update
     **/
    public static final String PUBLISHED_TO_S7_BY_USER = "publishedToS7ByUser";
    public static final String REJECTED_BY = "rejectedBy";
    public static final String ECOMM_ENTRY_DATE = "ecommEntryDate";
    public static final String CONTENT_OPS_REVIEWED_DATE = "contentOpsReviewedDate";
    public static final String LAST_MODIFIED_BY_USER = "lastModifiedByUser";
    public static final String REJECTED_DATE = "rejectedDate";
    public static final String VAH_ENTRY_DATE = "vahEntryDate";
    public static final String IMAGESET_CREATED_BY = "imageSetCreatedBy";
    public static final String IMAGESET_NIGHTLY_PUBLISH_DATE = "imagesetNightlyPublishDate";
    
    /** Send Mail properties **/
    public static final String TO = "to";
    public static final String CC = "cc";
    public static final String BCC = "bcc";
    public static final String TO_MULTIPLE = "tos";
    public static final String CC_MULTIPLE = "ccs";
    public static final String BCC_MULTIPLE = "bccs";
    public static final String REPLY_TO = "replyTo";
    public static final String SUBJECT = "subject";
    public static final String MESSAGE = "message";
    public static final String FILENAME = "filename";
    public static final String FILENAME1 = "filename1";
}
