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
<%@page session="false"
       import="org.apache.sling.api.resource.Resource,com.bbby.aem.core.util.ServiceUtils"%><%
%><%@include file="/libs/dam/gui/coral/components/admin/collections/contentrenderer/collectionBase.jsp"%><%

attrs.addClass("foundation-collection-navigator");
attrs.add("data-foundation-collection-navigator-href", xssAPI.getValidHref(childCollectionUrl));

attrs.add("is", "coral-table-row");
attrs.add("data-item-title", title);

String created = "";
String createdBy = "";


%>

<%
	if (childCollectionNode.hasProperty("jcr:created")) {
       Calendar  crDate = childCollectionNode.getProperty("jcr:created").getDate();
    if(crDate != null){
	 created = ServiceUtils.getDateStr(crDate, "YYYY-MM-dd HH:mm");
	}

    }
	 if (childCollectionNode.hasProperty("jcr:createdBy")) {
        createdBy = childCollectionNode.getProperty("jcr:createdBy").getString();
    }

%>

<tr <%= attrs %>>
    <td is="coral-table-cell" coral-table-rowselect>
        <img class="foundation-collection-item-thumbnail" src="<%= xssAPI.getValidHref(thumbnailPath) %>" width="48px" height="48px" alt="<%=xssAPI.encodeForHTMLAttr(UIHelper.getAltText(resource))%>">
    </td>
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(collectionName) %>"><%= xssAPI.encodeForHTML(collectionName) %></td>
    <td class="foundation-collection-item-title" is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(title) %>"><%= xssAPI.encodeForHTML(title) %></td>
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(description) %>"><%= xssAPI.encodeForHTML(description) %></td>
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(type) %>"><%= xssAPI.encodeForHTML(type) %></td>
    <td is="coral-table-cell" value="<%= created %>"><%= created %></td>
 <td is="coral-table-cell" value="<%= createdBy %>"><%= createdBy %></td>
    <td is="coral-table-cell" value="<%= xssAPI.encodeForHTMLAttr(role) %>"><%= xssAPI.encodeForHTML(role) %>
        <meta class="foundation-collection-quickactions" data-foundation-collection-quickactions-rel="<%= xssAPI.encodeForHTMLAttr(actionRels) %>"></meta>
    </td>

</tr><%!

%>
