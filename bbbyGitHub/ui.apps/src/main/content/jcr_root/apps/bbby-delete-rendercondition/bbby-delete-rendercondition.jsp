<%@page session="false"
        import="com.adobe.granite.ui.components.ComponentHelper,
                com.adobe.granite.ui.components.Config,
                org.apache.sling.api.resource.Resource,
                org.apache.sling.api.resource.ValueMap,
                com.adobe.granite.ui.components.rendercondition.RenderCondition,
                com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition,
                javax.jcr.Node,
                org.apache.jackrabbit.api.security.user.Group,
                org.apache.jackrabbit.api.security.user.User,
                com.day.cq.dam.commons.util.UIHelper,
                com.day.cq.dam.api.DamConstants" %>
<%@ page import="javax.jcr.Session" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="javax.jcr.NodeIterator" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0" %>

<sling:defineObjects/>
<cq:defineObjects/>

<%

    ComponentHelper cmp = new ComponentHelper(pageContext);
    Config cfg = cmp.getConfig();
    String path = cmp.getExpressionHelper().getString(cfg.get("path", String.class));
    Resource contentRes = null;

    if (path != null) {
        contentRes = slingRequest.getResourceResolver().getResource(path);
    } else {
        contentRes = UIHelper.getCurrentSuffixResource(slingRequest);
    }

    if (contentRes == null) {
        return;
    }
    
    boolean showDelete = true;
    try{
	    Resource marketingRes = slingRequest.getResourceResolver().getResource("/content/dam/marketing");
	    Node assetNode = marketingRes.adaptTo(Node.class);
	    String groupName = null;
	    User currentUser = slingRequest.getResourceResolver().adaptTo(User.class);
	    Iterator<Group> currentUserGroups = currentUser.memberOf();
	    while (currentUserGroups.hasNext()) {
	        Group grp = (Group) currentUserGroups.next();
	        groupName = grp.getID();
	        if(assetNode.hasNode("./jcr:content/metadata")){
            	Node metadataNode = assetNode.getNode("./jcr:content/metadata");
    	        if(metadataNode.hasProperty("userGroupDeleteHidden")){
    	        	String userGroups = metadataNode.getProperty("userGroupDeleteHidden").getString();
    	        	if (userGroups != null) {
    					String[] userGroupsList = userGroups.split(",");
    					for (int i = 0; i < userGroupsList.length; i++) {
    						if(groupName.equals(userGroupsList[i].trim())){
        	        			showDelete = false;
        	                } 
    					}
    				}
    	        }
            }
	    }
    }catch(Exception e){
    	showDelete = true;
    }

    if(showDelete){
%>
        <sling:include path="/libs/dam/gui/coral/components/commons/renderconditions/mainasset"/>
<%
    }else{
        request.setAttribute(RenderCondition.class.getName(), new SimpleRenderCondition(false));
    }
%>