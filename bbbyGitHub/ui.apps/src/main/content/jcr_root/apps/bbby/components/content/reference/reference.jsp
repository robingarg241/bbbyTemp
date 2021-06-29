<%@page session="false"%><%--
  Copyright 1997-2008 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.


  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.


  ==============================================================================


  Default reference component.


  Includes the referenced component addressed by the "path" property. It
  temporarily disables the WCM so that the included components cannot be
  edited.


  ==============================================================================


--%><%@page import="com.day.cq.wcm.api.WCMMode,
    com.day.cq.wcm.api.components.DropTarget,
    com.day.cq.wcm.foundation.Placeholder,
    java.net.URLEncoder" %><%
  %><%@include file="/libs/foundation/global.jsp" %><%


WCMMode mode = WCMMode.DISABLED.toRequest(request);

// Remember the mode on the original page before any reference started
String originalModeKey = "com.day.cq.wcm.components.reference.mode";
WCMMode originalMode = (WCMMode)request.getAttribute(originalModeKey);
if (originalMode == null) {
    originalMode = mode;
    request.setAttribute(originalModeKey, originalMode);
}


boolean needToCloseDiv = false;
try {
    /* START: HERO UPDATE TO ADD MARKER POINT */
    if (mode == WCMMode.EDIT) {
        %><span class="edit-start container">&nbsp;<%= resource.getName() %> <i class="fa fa-arrow-circle-down" aria-hidden="true"></i></span><%
    }
    /* END: HERO UPDATE TO ADD MARKER POINT */

    // Use request attributes to guard against reference loops
    String path = resource.getPath();
    String key = "com.day.cq.wcm.components.reference:" + path;
    if (request.getAttribute(key) == null) {
        request.setAttribute(key, Boolean.TRUE);
    } else {
        throw new IllegalStateException("Reference loop: " + path);
    }

    //drop target css class = dd prefix + name of the drop target in the edit config
    String ddClassName = DropTarget.CSS_CLASS_PREFIX + "paragraph";

    // Include the target paragraph by path
    String target = properties.get("path", String.class);
    if (target != null) {

        /* START: HERO CUSTOMIZATION TO LINK TO REFERENCED PAGE */
        if (mode == WCMMode.EDIT || mode == WCMMode.PREVIEW) {
            String[] parts = target.split("/.?jcr.content");
            if (parts.length == 2) {
                String targetPage = parts[0];
                String targetPageOpen = "/bin/wcmcommand?cmd=open&_charset_=utf-8&path=" + URLEncoder.encode(targetPage, "UTF-8");
                %>
                    <!-- This is used by reference-toolbar-action.js to allow authors to open the page containing the referenced component. -->
                    <span class="reference-cmp-source" data-link="<%= targetPageOpen %>"></span>
                <%
            }
        }
        /* END: HERO CUSTOMIZATION TO LINK TO REFERENCED PAGE */

        /*
         * START: HERO CUSTOMIZATION TO REMOVE EXTRA DIVS
         */
        Object prevBypass = request.getAttribute(ComponentContext.BYPASS_COMPONENT_HANDLING_ON_INCLUDE_ATTRIBUTE);
        slingRequest.setAttribute(ComponentContext.BYPASS_COMPONENT_HANDLING_ON_INCLUDE_ATTRIBUTE, true);
        %><% needToCloseDiv = true; %><sling:include path="<%= target %>"/><% needToCloseDiv = false; %><%
        slingRequest.setAttribute(ComponentContext.BYPASS_COMPONENT_HANDLING_ON_INCLUDE_ATTRIBUTE, prevBypass);
        /*
         * END: HERO CUSTOMIZATION TO REMOVE EXTRA DIVS
         */
    } else if (mode == WCMMode.EDIT) {
        String classicPlaceholder =
                "<p><img src=\"/libs/cq/ui/resources/0.gif\" class=\"cq-reference-placeholder " + ddClassName + "\" alt=\"\"></p>";
        String placeholder = Placeholder.getDefaultPlaceholder(slingRequest, component, classicPlaceholder, ddClassName);
        %><%= placeholder %><%
    }

    /* START: HERO UPDATE TO ADD MARKER POINT */
    if (mode == WCMMode.EDIT) {
        %><span class="edit-start container">&nbsp;<%= resource.getName() %> <i class="fa fa-arrow-circle-up" aria-hidden="true"></i></span><%
    }
    /* END: HERO UPDATE TO ADD MARKER POINT */
} catch (Exception e) {
    // Log errors always
    log.error("Reference component error", e);
    // Display errors only in edit mode
    if (originalMode == WCMMode.EDIT) {
        %><p>Reference error: <%= xssAPI.encodeForHTML(e.toString()) %></p><%
    }
} finally {
    if (needToCloseDiv) {
        %></div><%
    }
    mode.toRequest(request);
}
%>
