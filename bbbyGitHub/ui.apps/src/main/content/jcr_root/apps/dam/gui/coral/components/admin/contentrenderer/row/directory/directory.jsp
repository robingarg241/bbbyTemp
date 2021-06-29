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
%><%@page import="org.apache.sling.api.resource.Resource, com.bbby.aem.core.util.ServiceUtils"%><%
%><%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0"%><%
%><%@include file="/libs/dam/gui/coral/components/admin/contentrenderer/base/init/directoryBase.jsp"%><%

String directoryActionRels = StringUtils.join(UIHelper.getDirectoryActionRels(hasJcrRead, hasModifyAccessControl, hasJcrWrite, hasReplicate, isMACShared, isCCShared, isRootMACShared, isMPShared, isRootMPShared, isLiveCopy), " ");

request.setAttribute("actionRels", actionRels.concat(" " + directoryActionRels));

attrs.addClass("foundation-collection-navigator");
if (request.getAttribute("com.adobe.directory.card.nav") != null){
    navigationHref =  (String) request.getAttribute("com.adobe.directory.card.nav");
    attrs.add("data-foundation-collection-navigator-href", xssAPI.getValidHref(navigationHref));
}

attrs.add("is", "coral-table-row");
attrs.add("data-item-title", resourceTitle);
attrs.add("data-item-type", type);


request.setAttribute("com.adobe.assets.meta.attributes", metaAttrs);
PublicationStatus publicationStatus = getPublicationStatus(request, i18n);

String primaryUPC = "";
String rejectionReason = "";
String primaryVendorName = "";
String priorityFlag = "";
String webProductRollupType = "";
String assetUpdate = "";
String primaryImage = "";
String contentOpsReview = "";
String instructions = "";
String rejectionFollowup = "";
String colorSpace = "";
String scene7FileStatus = "";
String count = "";
String sku = "";
String expirationDate = "";
String relPath = "";
String updatedBy = "";
String photoshopDateCreated = "";
String dcCreator = "";

if(resource.getPath().contains("e-comm")){
	count = ServiceUtils.getChildCount(resourceResolver, resource.getPath());
}

%><tr <%= attrs %>>
    <td is="coral-table-cell" coral-table-rowselect>
        <coral-icon class="foundation-collection-item-thumbnail" icon="folder"></coral-icon>
    </td>
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(resource.getName()) %>">
        <%= xssAPI.encodeForHTML(resource.getName()) %>
    </td>
    <td class="foundation-collection-item-title" is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(resourceAbsTitle) %>"><%= xssAPI.encodeForHTML(resourceAbsTitle) %></td>
	<td is="coral-table-cell" value="<%= primaryUPC %>"><%= primaryUPC %></td>
	<td is="coral-table-cell" value="<%= sku %>"><%= sku %></td>
	<td is="coral-table-cell" value="<%= primaryVendorName %>"><%= primaryVendorName %></td>
	<td is="coral-table-cell" value="<%= instructions %>"><%= instructions %></td>
	<td is="coral-table-cell" value="<%= contentOpsReview %>"><%= contentOpsReview %></td>
	<td is="coral-table-cell" value="<%= assetUpdate %>"><%= assetUpdate %></td>
	<td is="coral-table-cell" value="<%= primaryImage %>"><%= primaryImage %></td>
    <td is="coral-table-cell" value="<%= rejectionReason %>"><%= rejectionReason %></td>
    <td is="coral-table-cell" value="<%= priorityFlag %>"><%= priorityFlag %></td>
    <td is="coral-table-cell" value="<%= webProductRollupType %>"><%= webProductRollupType %></td>
	<td is="coral-table-cell" value="<%= rejectionFollowup %>"><%= rejectionFollowup %></td>
	<td is="coral-table-cell" value="<%= colorSpace %>"><%= colorSpace %></td>
	<td is="coral-table-cell" value="<%= scene7FileStatus %>"><%= scene7FileStatus %></td>
    <td is="coral-table-cell" value="<%= count %>"><%= count %></td>
	<td is="coral-table-cell" value="<%= expirationDate %>"><%= expirationDate %></td>
	<td is="coral-table-cell" value="<%= relPath %>"><%= relPath %></td>
	<td is="coral-table-cell" value="<%= dcCreator %>"><%= dcCreator %></td>
	<td is="coral-table-cell" value="<%= photoshopDateCreated %>"><%= photoshopDateCreated %></td>
	<td is="coral-table-cell" value="<%= displayLanguage %>"><%= displayLanguage %></td>
    <td is="coral-table-cell" value="0"></td> <!--Adding a placeholder column for expiryStatus -->
    <td is="coral-table-cell" value="0"></td> <!--Adding a placeholder column for encodingStatus -->
    <td is="coral-table-cell" value="type">
        <%= i18n.get("FOLDER") %>
        <% if (isLiveCopy) { %><div class="foundation-layout-util-subtletext"><%= xssAPI.encodeForHTML(i18n.get("Live Copy")) %></div><% } %>
    </td>
    <td is="coral-table-cell" value="0"></td>   <!--Adding a placeholder column for dimensions -->
    <td is="coral-table-cell" value="0"></td>   <!--Adding a placeholder column for size -->
    <td is="coral-table-cell" value="0"></td>   <!--Adding a placeholder column for rating -->
    <td is="coral-table-cell" value="0"></td>   <!--Adding a placeholder column for usagescore-->
    <td is="coral-table-cell" value="0"></td>   <!--Adding a placeholder column for impression score-->
    <td is="coral-table-cell" value="0"></td>   <!--Adding a placeholder column for click score-->
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(Long.toString(createdLong)) %>"><%
        if (createdStr != null) {
            %><foundation-time type="datetime" value="<%= xssAPI.encodeForHTMLAttr(createdStr) %>"></foundation-time><%
        }
    %>
    </td>
	<td is="coral-table-cell" value="<%= updatedBy %>"><%= updatedBy %></td>
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(Long.toString(directoryLastModification)) %>"><%
        if (lastModified != null) {
            %><foundation-time type="datetime" value="<%= xssAPI.encodeForHTMLAttr(lastModified) %>"></foundation-time><%

            // Modified-after-publish indicator
                if (publishDateInMillis > 0 && publishDateInMillis < directoryLastModification) {
                String modifiedAfterPublishStatus = i18n.get("Modified since last publication");
                %><coral-icon icon="alert" style = "margin-left: 5px;" size="XS" title="<%= xssAPI.encodeForHTMLAttr(modifiedAfterPublishStatus) %>"></coral-icon><%
            }

            %><div class="foundation-layout-util-subtletext"><%= xssAPI.encodeForHTML(lastModifiedBy) %></div><%
        }
    %>
    </td>
    <td is="coral-table-cell" value="<%= (!isDeactivated && publishedDate != null) ? xssAPI.encodeForHTMLAttr(Long.toString(publishDateInMillis)) : "0" %>"><%
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
        } else {
        %><span><%= xssAPI.encodeForHTML(i18n.get("Not published")) %></span><%
            }

            // Published by
            if (publishedBy != null) {
        %><div class="foundation-layout-util-subtletext"><%= xssAPI.encodeForHTML(publishedBy) %></div><%
            }

        %><cq:include script = "applicableRelationships.jsp"/>
    </td>
    <td is="coral-table-cell"></td>  <!--Adding a placeholder column for workflow status-->
    <td is="coral-table-cell"></td> <!--Adding a placeholder column for checkout status-->
    <td is="coral-table-cell" value="0"></td>   <!--Adding a placeholder column for comments-->

    <% if (isProcessingProfileEntitled && !profileTitleList[0].trim().isEmpty()) { %>
        <td is="coral-table-cell" value="0"><%= xssAPI.encodeForHTML(profileTitleList[0].trim()) %></td>
    <% } else{ %>
        <td is="coral-table-cell" value="0"></td>
    <% } %>

    <% if (isProcessingProfileEntitled && !profileTitleList[1].trim().isEmpty()) { %>
        <td is="coral-table-cell" value="0"><%= xssAPI.encodeForHTML(profileTitleList[1].trim()) %></td>
    <% } else{ %>
        <td is="coral-table-cell" value="0"></td>
    <% } %>

    <% if (isProcessingProfileEntitled && !profileTitleList[2].trim().isEmpty()) { %>
        <td is="coral-table-cell" value="0"><%= xssAPI.encodeForHTML(profileTitleList[2].trim()) %></td>
    <% } else{ %>
        <td is="coral-table-cell" value="0"></td>
    <% } %>

    <cq:include script = "reorder.jsp"/>
    <cq:include script = "meta.jsp"/>
</tr><%!

%>
