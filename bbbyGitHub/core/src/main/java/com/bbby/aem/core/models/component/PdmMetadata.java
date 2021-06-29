package com.bbby.aem.core.models.component;

import com.bbby.aem.core.models.data.PDMMetadataModel;
import com.bbby.aem.core.util.CommonConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Pdm Metadata Component to display the nodes under upcmetadata node for an asset
 */
@Model(adaptables = {Resource.class, SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/pdm-metadata")
@Exporter(name = "jackson", extensions = "json")
public class PdmMetadata extends ComponentSlingModel {

    @Inject
    @SlingObject
    @Required
    private SlingHttpServletRequest request;

    private List<PDMMetadataModel> pdmMetaData;


    @Override
    public void postConstruct() throws Exception {

        if (request.getRequestParameter("item") != null) {
            String assetUrl = request.getRequestParameter("item").toString();

            pdmMetaData = getPDMMetaData(assetUrl);
        }

    }

    private List<PDMMetadataModel> getPDMMetaData(String assetUrl) throws Exception {

        ResourceResolver resourceResolver = request.getResourceResolver();

        Session session= resourceResolver.adaptTo(Session.class);

        List<PDMMetadataModel> pdmMetadataList = new ArrayList<>();

        Node assetNode = session.getNode(assetUrl);

        if (assetNode.hasNode(CommonConstants.PDM_METADATA_NODE)) {

            Node upcMetaDataNode = assetNode.getNode(CommonConstants.PDM_METADATA_NODE);

            if (upcMetaDataNode != null) {

                Iterator<Node> pdmNodes = upcMetaDataNode.getNodes();

                while (pdmNodes.hasNext()) {
                    Node pdmMetadataNode = pdmNodes.next();

                    PDMMetadataModel pdmMetadata = new PDMMetadataModel();

                    setPDMMetadata(pdmMetadata, pdmMetadataNode);
                    pdmMetadataList.add(pdmMetadata);
                }
            }
        }

        return pdmMetadataList;
    }

    private void setPDMMetadata(PDMMetadataModel pdmMetadata, Node pdmMetadataNode) throws RepositoryException {
        if (pdmMetadataNode.hasProperty(CommonConstants.PRIMARY_UPC))
            pdmMetadata.setPrimaryUPC(pdmMetadataNode.getProperty(CommonConstants.PRIMARY_UPC).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.SKU))
            pdmMetadata.setSku(pdmMetadataNode.getProperty(CommonConstants.SKU).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.PDM_BRAND))
            pdmMetadata.setBrand(pdmMetadataNode.getProperty(CommonConstants.PDM_BRAND).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.COLOR_CODE))
            pdmMetadata.setColorCode(pdmMetadataNode.getProperty(CommonConstants.COLOR_CODE).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.COLOR_GROUP_CODE))
            pdmMetadata.setColorGroupCode(pdmMetadataNode.getProperty(CommonConstants.COLOR_GROUP_CODE).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.PRODUCT))
            pdmMetadata.setProduct(pdmMetadataNode.getProperty(CommonConstants.PRODUCT).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.ASSOCIATED_WEB_PRODUCT_ID)) {
            String associatedWebProductID = Arrays.toString(pdmMetadataNode.getProperty(CommonConstants.ASSOCIATED_WEB_PRODUCT_ID).getValues()).replace("[", "").replace("]", "").trim();
            pdmMetadata.setAssociatedWebProductID(associatedWebProductID);
        }
        if (pdmMetadataNode.hasProperty(CommonConstants.ASSOCIATED_COLLECTION_ID)) {
            String associatedCollectionID = Arrays.toString(pdmMetadataNode.getProperty(CommonConstants.ASSOCIATED_COLLECTION_ID).getValues()).replace("[", "").replace("]", "").trim();
            pdmMetadata.setAssociatedCollectionID(associatedCollectionID);
        }
        if (pdmMetadataNode.hasProperty(CommonConstants.PDM_BATCH_ID)) {
        	pdmMetadata.setPdmBatchID(pdmMetadataNode.getProperty(CommonConstants.PDM_BATCH_ID).getValue().toString());
        }
        if (pdmMetadataNode.hasProperty(CommonConstants.PRIMARY_VENDOR_NUMBER))
            pdmMetadata.setPrimaryVendorNumber(pdmMetadataNode.getProperty(CommonConstants.PRIMARY_VENDOR_NUMBER).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.PRIMARY_VENDOR_NAME))
            pdmMetadata.setPrimaryVendorName(pdmMetadataNode.getProperty(CommonConstants.PRIMARY_VENDOR_NAME).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.VENDOR_DIRECT_TO_CUSTOMER_ITEM))
            pdmMetadata.setVendorDirectToCustomerItem(pdmMetadataNode.getProperty(CommonConstants.VENDOR_DIRECT_TO_CUSTOMER_ITEM).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.PRIORITY_FLAG))
            pdmMetadata.setPriorityFlag(pdmMetadataNode.getProperty(CommonConstants.PRIORITY_FLAG).getValue().toString().substring(2));
        if (pdmMetadataNode.hasProperty(CommonConstants.BBBY_WEB_DISABLED))
            pdmMetadata.setBbbyWebDisabled(pdmMetadataNode.getProperty(CommonConstants.BBBY_WEB_DISABLED).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.BABY_WEB_DISABLED))
            pdmMetadata.setBabyWebDisabled(pdmMetadataNode.getProperty(CommonConstants.BABY_WEB_DISABLED).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.CA_WEB_DISABLED))
            pdmMetadata.setCaWebDisabled(pdmMetadataNode.getProperty(CommonConstants.CA_WEB_DISABLED).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.BBBY_WEB_OFEERED_FLAG))
            pdmMetadata.setBbbyWebOfferedFlag(pdmMetadataNode.getProperty(CommonConstants.BBBY_WEB_OFEERED_FLAG).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.BABY_WEB_OFEERED_FLAG))
            pdmMetadata.setBabyWebOfferedFlag(pdmMetadataNode.getProperty(CommonConstants.BABY_WEB_OFEERED_FLAG).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.CA_WEB_OFEERED_FLAG))
            pdmMetadata.setCaWebOfferedFlag(pdmMetadataNode.getProperty(CommonConstants.CA_WEB_OFEERED_FLAG).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.WEB_PRODUCT_ROLL_UP_TYPE))
            pdmMetadata.setWebProductRollUpType(pdmMetadataNode.getProperty(CommonConstants.WEB_PRODUCT_ROLL_UP_TYPE).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.MASTER_PRODUCT_DESCRIPTION))
            pdmMetadata.setMasterProductDescription(pdmMetadataNode.getProperty(CommonConstants.MASTER_PRODUCT_DESCRIPTION).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.FAST_TRACK_FLAG))
            pdmMetadata.setFastTrackFlag(pdmMetadataNode.getProperty(CommonConstants.FAST_TRACK_FLAG).getValue().toString());
        if (pdmMetadataNode.hasProperty(CommonConstants.PULLBACK_TO_MERCHANT))
            pdmMetadata.setPullbackToMerchant(pdmMetadataNode.getProperty(CommonConstants.PULLBACK_TO_MERCHANT).getValue().toString());
    }

    public List<PDMMetadataModel> getPdmMetaData() {
        return pdmMetaData;
    }

}
