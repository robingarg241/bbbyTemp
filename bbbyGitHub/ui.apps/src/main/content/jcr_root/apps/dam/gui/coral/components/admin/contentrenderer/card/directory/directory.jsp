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
--%><%
%><%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0"%>
<%@page import="org.apache.jackrabbit.util.Text, com.bbby.aem.core.util.ServiceUtils"%><%
%><%@include file="/libs/dam/gui/coral/components/admin/contentrenderer/base/init/directoryBase.jsp"%><%

String thumbnailUrl = "";

String directoryActionRels = StringUtils.join(UIHelper.getDirectoryActionRels(hasJcrRead, hasModifyAccessControl, hasJcrWrite, hasReplicate, isMACShared, isCCShared, isRootMACShared, isMPShared, isRootMPShared, isLiveCopy), " ");

request.setAttribute("actionRels", actionRels.concat(" " + directoryActionRels));

     boolean manualThumnailExists = false;
    // Path at which the manual thumbnail(if present) exists
    String manualThumbnailPath = resource.getPath() + "/jcr:content/manualThumbnail.jpg";
    Resource manualThumbnail = resourceResolver.getResource(manualThumbnailPath);
    if (null != manualThumbnail) {
        manualThumnailExists = true;
    }
    
    String escapedResourcePath = Text.escapePath(resourcePath);
    
    if (manualThumnailExists) {
        //UIHelper.getCacheKiller() generates the cache killer based on default thumbnail's last modified time.
        int cck = 600000 + (int)(Math.random() * (600001));
        	thumbnailUrl = requestPrefix + escapedResourcePath + "/jcr:content/manualThumbnail.jpg?ch_ck=" + cck + requestSuffix;
    } else {
        thumbnailUrl = requestPrefix + escapedResourcePath + ".folderthumbnail.jpg?width=280&height=240&ch_ck=" + ck + requestSuffix;
    }

attrs.add("variant", "inverted");
attrs.addClass("foundation-collection-navigator");

String count = "";
if(resource.getPath().contains("e-comm")){
	count = ServiceUtils.getChildCount(resourceResolver, resource.getPath());
}

%>
<cq:include script="link.jsp"/>
<%
    if (request.getAttribute("com.adobe.directory.card.nav")!=null){
        navigationHref =  (String) request.getAttribute("com.adobe.directory.card.nav");
    //navigationHref = Text.escapePath(navigationHref);
    navigationHref = navigationHref;
    attrs.add("data-foundation-collection-navigator-href", xssAPI.getValidHref(navigationHref));
    }


request.setAttribute("com.adobe.assets.meta.attributes", metaAttrs);
request.setAttribute("com.adobe.cq.assets.contentrenderer.directory.profileTitleList", profileTitleList);

%>
<cq:include script = "meta.jsp"/>
<coral-card <%= attrs %>>
    <coral-card-asset class="coral-Card-asset">
       <img src="<%= xssAPI.getValidHref(thumbnailUrl) %>" alt="<%=xssAPI.encodeForHTMLAttr(resourceTitle)%>">
    </coral-card-asset>
	<coral-card-content>
        <coral-card-context class="coral-Card-context">
            <%= xssAPI.encodeForHTML(context) %>
            <% if (isLiveCopy) { %><%= xssAPI.encodeForHTML(i18n.get("Live Copy")) %><% } %>
        </coral-card-context>
         <coral-card-title class="foundation-collection-item-title"><%= xssAPI.encodeForHTML(resourceTitle) %></coral-card-title>
         <% if (!resource.getName().equalsIgnoreCase(resourceTitle)) { %>
         <coral-card-subtitle class="foundation-collection-item-subtitle"><%= xssAPI.encodeForHTML(resource.getName()) %></coral-card-subtitle>
         <% } %>
         <coral-card-subtitle class="foundation-collection-item-subtitle"><%= xssAPI.encodeForHTML(count) %></coral-card-subtitle>
        <cq:include script = "propertyList.jsp"/>
    </coral-card-content>
    <cq:include script = "applicableRelationships.jsp"/>
</coral-card>
<cq:include script = "quickActions.jsp"/>
<%!

%>