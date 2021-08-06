 <%--
  ADOBE CONFIDENTIAL

  Copyright 2015 Adobe Systems Incorporated
  All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and may be covered by U.S. and Foreign Patents,
  patents in process, and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.
--%>
<%
%><%@page import="org.apache.sling.api.resource.Resource,
                 com.day.cq.dam.commons.util.UIHelper,
                 java.util.List, 
                 com.bbby.aem.core.util.ServiceUtils"%><%
%><%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0"%><%
%><%@include file="/libs/dam/gui/coral/components/admin/contentrenderer/base/init/assetBase.jsp"%><%
%><%@include file="/libs/dam/gui/coral/components/admin/contentrenderer/base/insightBase.jsp"%><%

boolean showOriginalIfNoRenditionAvailable = (request!=null && request.getAttribute("showOriginalIfNoRenditionAvailable")!=null) ? (Boolean)request.getAttribute("showOriginalIfNoRenditionAvailable") : false;
boolean showOriginalForGifImages = (request!=null && request.getAttribute("showOriginalForGifImages")!=null) ? (Boolean)request.getAttribute("showOriginalForGifImages") : false;
boolean isOmniSearchRequest = request.getAttribute(IS_OMNISEARCH_REQUEST) != null ? (boolean) request.getAttribute(IS_OMNISEARCH_REQUEST) : false;
boolean isSnippetRequest = request.getAttribute(IS_SNIPPET_REQUEST) != null ? (boolean) request.getAttribute(IS_SNIPPET_REQUEST) : false;

String thumbnailUrl = request.getContextPath() + requestPrefix
+ getThumbnailUrl(asset, 48, showOriginalIfNoRenditionAvailable, showOriginalForGifImages) + "?ch_ck=" + ck + requestSuffix;

boolean lowResolution = false;
String assetLength = asset.getMetadataValue("tiff:ImageLength");
String assetWidth = asset.getMetadataValue("tiff:ImageWidth");

Resource upcMetadataResource = resourceResolver.getResource(asset.getPath() + "/jcr:content/upcmetadata/upc-0");
Node upcmetadata = upcMetadataResource != null ? upcMetadataResource.adaptTo(Node.class) : null;
//Node upcNodes = upcMetadataNode.getNode("upc-0");

Resource operationalMetadataResource = resourceResolver.getResource(asset.getPath() + "/jcr:content/operationalmetadata");
Node operationalMetadata = operationalMetadataResource != null ? operationalMetadataResource.adaptTo(Node.class) : null;

String primaryVendorName = "";
String priorityFlag = "";
String webProductRollupType = "";

String pdmCallSent = "";
String lastPdmCallStatus = "";

Resource jcrContentResource = resource.getChild(JCR_CONTENT);
ValueMap properties = jcrContentResource.getValueMap();
String relPath = properties.get("dam:relativePath", String.class) != null ? properties.get("dam:relativePath", String.class) : "";

if(upcmetadata != null){
	primaryVendorName = (upcmetadata.hasProperty("primaryVendorName")) ? upcmetadata.getProperty("primaryVendorName").getString() : "";
 	priorityFlag = (upcmetadata.hasProperty("priorityFlag")) ? upcmetadata.getProperty("priorityFlag").getString() : "";
 	webProductRollupType = (upcmetadata.hasProperty("webProductRollupType")) ? upcmetadata.getProperty("webProductRollupType").getString() : "";
}

if(operationalMetadata != null){
	pdmCallSent = (operationalMetadata.hasProperty("pdmCallSent")) ? operationalMetadata.getProperty("pdmCallSent").getString() : "";
 	lastPdmCallStatus = (operationalMetadata.hasProperty("lastPdmCallStatus")) ? operationalMetadata.getProperty("lastPdmCallStatus").getString() : "";
}

String primaryUPC = asset.getMetadataValue("bbby:primaryUpc") != null ? asset.getMetadataValue("bbby:primaryUpc") : "";
String rejectionReason = asset.getMetadataValue("bbby:rejectionReason") != null ? asset.getMetadataValue("bbby:rejectionReason") : "Not Set";
String assetUpdate = asset.getMetadataValue("bbby:assetUpdate") != null ? asset.getMetadataValue("bbby:assetUpdate").toUpperCase() : "No";
String primaryImage = asset.getMetadataValue("bbby:primaryImage") != null ? asset.getMetadataValue("bbby:primaryImage").toUpperCase() : "No";
String contentOpsReview = asset.getMetadataValue("bbby:contentOpsReview") != null ? asset.getMetadataValue("bbby:contentOpsReview").toUpperCase() : "No";
String instructions = asset.getMetadataValue("bbby:instructions") != null ? asset.getMetadataValue("bbby:instructions") : "";
String rejectionFollowup = asset.getMetadataValue("bbby:rejectionFollowup") != null ? asset.getMetadataValue("bbby:rejectionFollowup") : "";
String colorSpace = asset.getMetadataValue("bbby:colorSpace") != null ? asset.getMetadataValue("bbby:colorSpace") : "";
String scene7FileStatus = asset.getMetadataValue("dam:scene7FileStatus") != null ? asset.getMetadataValue("dam:scene7FileStatus") : "";
String count = "N/A";
String sku = asset.getMetadataValue("bbby:sku") != null ? asset.getMetadataValue("bbby:sku") : "";
String updatedBy = properties.get("jcr:lastModifiedBy", String.class) != null ? properties.get("jcr:lastModifiedBy", String.class) : "";
String dcCreator = asset.getMetadataValue("dc:creator") != null ? asset.getMetadataValue("dc:creator") : "";

//DAM-1234 : Expiration date is not shown in correct format in list view
String expirationDate = "";
ValueMap vm1 = metadataResc.adaptTo(ValueMap.class);
// asset created date
Calendar expiration = vm1.get("prism:expirationDate", Calendar.class);
if(expiration != null){
	expirationDate = ServiceUtils.getDateStr(expiration, "YYYY-MM-dd HH:mm");
}
if(expirationDate == null){
	expirationDate = "";
}

//DAM-1402
String photoshopDateCreated = "";
Calendar photoshopDateCreatedCal = vm1.get("photoshop:DateCreated", Calendar.class);
if(photoshopDateCreatedCal != null){
	photoshopDateCreated = ServiceUtils.getDateStr(photoshopDateCreatedCal, "YYYY-MM-dd HH:mm");
}
if(photoshopDateCreated == null){
	photoshopDateCreated = "";
}

if("yes".equalsIgnoreCase(assetUpdate)){
	assetUpdate = "Yes";
}else{
	assetUpdate = "No";
}

if("yes".equalsIgnoreCase(primaryImage)){
	primaryImage = "Yes";
}else{
	primaryImage = "No";
}

if("yes".equalsIgnoreCase(contentOpsReview)){
	contentOpsReview = "Yes";
}else{
	contentOpsReview = "No";
}

if(assetLength!=null && assetWidth!=null){
    try{
        int len = Integer.parseInt(assetLength);
        int wid =  Integer.parseInt(assetWidth);
        if(len < 48 || wid < 48) {
            lowResolution = true;
        }
    }catch (NumberFormatException nfe){
        log.error("Exception:" + nfe);
    }
}

//Override default thumbnail for set when there is manual thumbnail defined
if (dmSetManualThumbnailAsset != null) {
    thumbnailUrl = request.getContextPath() + requestPrefix
    		+ getThumbnailUrl(asset, 1280, showOriginalIfNoRenditionAvailable, showOriginalForGifImages) + "?ch_ck=" + ck + requestSuffix;
} else if (dmRemoteThumbnail != null && !dmRemoteThumbnail.isEmpty()) {
    thumbnailUrl = dmRemoteThumbnail + "?wid=48&ch_ck=" + ck + requestSuffix;
}

 List<String> assetActionRelsList = UIHelper.getAssetActionRels(hasJcrRead, hasJcrWrite, hasAddChild, canEdit, canAnnotate, isDAMAdmin, isAssetExpired, isSubAssetExpired, isContentFragment, isArchive, isSnippetTemplate, isDownloadable, isOmniSearchRequest, isStockAsset, isStockAssetLicensed, isStockAccessible, isLiveCopy);
 if (!DamUtil.isImage(asset)) {
     assetActionRelsList.removeIf(rel -> rel.equalsIgnoreCase("aem-assets-admin-actions-moderatetags-activator"));
 }
 String assetActionRels = StringUtils.join(assetActionRelsList, " ");

request.setAttribute("actionRels", actionRels.concat(" " + assetActionRels));
if (allowNavigation) {
attrs.addClass("foundation-collection-navigator");
}
%>
<cq:include script="link.jsp"/>
<%

    if (request.getAttribute("com.adobe.assets.card.nav")!=null){
        navigationHref =  (String) request.getAttribute("com.adobe.assets.card.nav");
    }
attrs.add("data-foundation-collection-navigator-href", xssAPI.getValidHref(navigationHref));

attrs.add("is", "coral-table-row");
attrs.add("data-item-title", resourceTitle);
attrs.add("data-item-type", type);

com.adobe.granite.workflow.status.WorkflowStatus workflowStatus = resource.adaptTo(com.adobe.granite.workflow.status.WorkflowStatus.class);
List<com.adobe.granite.workflow.exec.Workflow> workflows = workflowStatus.getWorkflows(false);

request.setAttribute("com.adobe.assets.meta.attributes", metaAttrs);

PublicationStatus publicationStatus = getPublicationStatus(request, i18n);

final String nameDisplayOrder = i18n.get("{0} {1}", "name display order: {0} is the given (first) name, {1} the family (last) name", "givenName middleName", "familyName");


%>
<tr <%= attrs %>>
    <td is="coral-table-cell" coral-table-rowselect><%
        if (isArchive) {
            %><coral-icon class="foundation-collection-item-thumbnail" icon="fileZip" size="S"></coral-icon><%
        } else {%>
            <img class="foundation-collection-item-thumbnail" src="<%= xssAPI.getValidHref(thumbnailUrl)%>" alt="" style="width: auto; height: auto; max-width: 3rem; max-height: 3rem;"><%
        }%>
    </td>
    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(resource.getName()) %>">
        <%= xssAPI.encodeForHTML(resource.getName()) %>
    </td>
    <% } %>
    <td class="foundation-collection-item-title" is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(resourceAbsTitle) %>"><%= xssAPI.encodeForHTML(resourceAbsTitle) %>
        <cq:include script = "status.jsp"/>
    </td>

    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= primaryUPC %>"><%= primaryUPC %></td>
    <% } %>
	
	<% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= sku %>"><%= sku %></td>
    <% } %>

    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= primaryVendorName %>"><%= primaryVendorName %></td>
    <% } %>

    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= instructions %>"><%= instructions %></td>
    <% } %>

    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= contentOpsReview %>"><%= contentOpsReview %></td>
    <% } %>

    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= assetUpdate %>"><%= assetUpdate %></td>
    <% } %>

    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= primaryImage %>"><%= primaryImage %></td>
    <% } %>

    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= rejectionReason %>"><%= rejectionReason %></td>
    <% } %>

    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= priorityFlag %>"><%= priorityFlag %></td>
    <% } %>

    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= webProductRollupType %>"><%= webProductRollupType %></td>
    <% } %>
    
    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= rejectionFollowup %>"><%= rejectionFollowup %></td>
    <% } %>
	
	<% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= colorSpace %>"><%= colorSpace %></td>
    <% } %>
    
    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= pdmCallSent %>"><%= pdmCallSent %></td>
    <% } %>
    
    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= lastPdmCallStatus %>"><%= lastPdmCallStatus %></td>
    <% } %>
    
    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= scene7FileStatus %>"><%= scene7FileStatus %></td>
    <% } %>
    
    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= count %>"><%= count %></td>
    <% } %>
	
	<% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= expirationDate %>"><%= expirationDate %></td>
    <% } %>
    
    <% if(!isSnippetRequest) { %>
	<td is="coral-table-cell" value="<%= relPath %>"><%= relPath %></td>
	<% } %>
	
	<% if(!isSnippetRequest) { %>
	<td is="coral-table-cell" value="<%= dcCreator %>"><%= dcCreator %></td>
	<% } %>
	
	<% if(!isSnippetRequest) { %>
	<td is="coral-table-cell" value="<%= photoshopDateCreated %>"><%= photoshopDateCreated %></td>
	<% } %>
	
    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="<%= displayLanguage %>"><%= displayLanguage %></td>
    <% } %>
    <cq:include script = "status-lister.jsp"/>
    <% if(!isSnippetRequest) { %>
    <td class="encodingstatus foundation-collection-item-title" is="coral-table-cell">
        <cq:include script = "encodingstatus.jsp"/>
    </td>
    <% } %>
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(displayMimeType) %>">
        <%= xssAPI.encodeForHTML(displayMimeType) %>
        <% if (isLiveCopy) { %><div class="foundation-layout-util-subtletext"><%= xssAPI.encodeForHTML(i18n.get("Live Copy")) %></div><% } %>
    </td>
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(resolution) %>"><%= xssAPI.encodeForHTML(resolution)%></td>
    <td is="coral-table-cell" value="<%= bytes %>"><%= size %></td>
    <% if(!isSnippetRequest) { %>
    <cq:include script = "rating.jsp"/>
    <td is="coral-table-cell" value="<%= assetUsageScore %>"><%= assetUsageScore %></td>
    <td is="coral-table-cell" value="<%= assetImpressionScore %>"><%= assetImpressionScore %></td>
    <td is="coral-table-cell" value="<%= assetClickScore %>"><%= assetClickScore %></td>
    <% } %>
    <td is="coral-table-cell" class="dam-test-collection-item-created" value="<%= xssAPI.encodeForHTMLAttr(Long.toString(createdLong)) %>"><%
        if (createdStr != null) {
            %><foundation-time type="datetime" value="<%= xssAPI.encodeForHTMLAttr(createdStr) %>"></foundation-time><%
        }
    %></td>
	<% if(!isSnippetRequest) { %>
	<td is="coral-table-cell" value="<%= updatedBy %>"><%= updatedBy %></td>
	<% } %>
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(Long.toString(assetLastModification)) %>"><%
        if (lastModified != null) {
            %><foundation-time type="datetime" value="<%= xssAPI.encodeForHTMLAttr(lastModified) %>"></foundation-time><%

            // Modified-after-publish indicator
                if (publishDateInMillis > 0 && publishDateInMillis < assetLastModification) {
                String modifiedAfterPublishStatus = i18n.get("Modified since last publication");
                %><coral-icon icon="alert" style = "margin-left: 5px;" size="XS" title="<%= xssAPI.encodeForHTMLAttr(modifiedAfterPublishStatus) %>"></coral-icon><%
            }

            %><div class="foundation-layout-util-subtletext"><%= xssAPI.encodeForHTML(lastModifiedBy) %></div><%
        }
    %></td>
    <td is="coral-table-cell"
        value="<%= (!isDeactivated && publishedDate != null) ? xssAPI.encodeForHTMLAttr(Long.toString(publishDateInMillis)) : "0" %>"><%
        // Published date and status
        String icon = null;
        String briefStatus = "";
        String title = "";
        if (publicationStatus.getAction() != null) {
            icon = publicationStatus.getIcon();
            briefStatus = publicationStatus.getBriefStatus();
            title = publicationStatus.getDetailedStatus();
        } else {
            if (publishedDate != null) {
                icon = isDeactivated ? "globeStrike" : "globe";
            }
        }
        if (icon != null) {
    %><coral-icon icon="<%= icon %>" style = "margin-left: 5px;" title = "<%= title %>" size="XS"%></coral-icon><%
        if (briefStatus != null && briefStatus.length() > 0) {%>
            <label> <%= i18n.getVar(briefStatus) %> </label>
        <%} else {%>
                <foundation-time type="datetime" value="<%= xssAPI.encodeForHTMLAttr(publishedDate) %>"></foundation-time><%
            }
        } else {%>
            <span><%= xssAPI.encodeForHTML(i18n.get("Not published")) %></span><%
        }
        // Published by
        if (publishedBy != null) {%>
            <div class="foundation-layout-util-subtletext"><%= xssAPI.encodeForHTML(publishedBy) %></div><%
        }

        %><cq:include script = "applicableRelationships.jsp"/>
    </td>
    <td is="coral-table-cell" value="<%= workflows.size() %>">
        <% if (workflows.size() > 0) { %>
        <a class="cq-timeline-control" data-cq-timeline-control-filter="workflows" href="#">
            <foundation-workflowstatus variant="<%= isWorkflowFailed(workflows) ? "error" : "default" %>">
                <%
                    for (com.adobe.granite.workflow.exec.Workflow w : workflows) { %>
                <foundation-workflowstatus-item
                        author="<%= xssAPI.encodeForHTMLAttr(AuthorizableUtil.getFormattedName(resourceResolver, w.getInitiator(), nameDisplayOrder)) %>"
                        timestamp="<%= xssAPI.encodeForHTMLAttr(w.getTimeStarted().toInstant().toString()) %>">
                    <%= xssAPI.encodeForHTML(i18n.getVar(w.getWorkflowModel().getTitle())) %></foundation-workflowstatus-item>

                <% } %>
            </foundation-workflowstatus>
        </a>
        <% } %>
    </td>
    <td is="coral-table-cell" value="<%= isCheckedOut ? xssAPI.encodeForHTMLAttr(checkedOutByFormatted) : "0" %>">
        <%
            // Checkout Status
            if (isCheckedOut) {
                String titleDisplay = i18n.get("Checked Out By {0}", "name inserted to variable", checkedOutByFormatted);
                %><coral-icon icon="lockOn" style = "margin-left: 5px;" size="XS" title="<%= xssAPI.encodeForHTMLAttr(titleDisplay) %>"><%
            }
        %>
    </td>
    <td is="coral-table-cell" value="<%= commentsCount %>"><%= commentsCount %></td>
    <td is="coral-table-cell" value="0"></td>   <!--Adding a placeholder column for metadata profile-->
    <% if(!isSnippetRequest) { %>
    <td is="coral-table-cell" value="0"></td>   <!--Adding a placeholder column for image profile-->
    <td is="coral-table-cell" value="0"></td>   <!--Adding a placeholder column for video profile-->
    <cq:include script = "reorder.jsp"/>
    <% } %>
    <cq:include script = "meta.jsp"/>
</tr><%!
     private boolean isWorkflowFailed(List<com.adobe.granite.workflow.exec.Workflow> workflows) {
         final String SUBTYPE_FAILURE_ITEM = "FailureItem";

         for (com.adobe.granite.workflow.exec.Workflow workflow : workflows) {
             List<com.adobe.granite.workflow.exec.WorkItem> workItems = workflow.getWorkItems();
             for (com.adobe.granite.workflow.exec.WorkItem workItem : workItems) {
                 if (SUBTYPE_FAILURE_ITEM.equals(workItem.getItemSubType())) {
                     return true;
                 }
             }
         }
         return false;
     }
 %>