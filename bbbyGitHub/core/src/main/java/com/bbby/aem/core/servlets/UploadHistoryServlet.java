package com.bbby.aem.core.servlets;

import com.bbby.aem.core.services.UploadHistoryService;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;
import java.text.ParseException;

/**
 * The Class UploadHistoryServlet.
 *
 * @author karthik.koduru, hero-digital.
 */
@Component(
	    service = Servlet.class,
	    immediate = true,
	    name = "Upload History Servlet",
	    property = {
	        ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/bedbath/vendor-portal/history",
	        ServletResolverConstants.SLING_SERVLET_METHODS + "=GET"
	    })
public class UploadHistoryServlet extends SlingAllMethodsServlet {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UploadHistoryServlet.class);

    /**
     * The UploadHistory service.
     */
    @Reference
    public UploadHistoryService uploadHistory;

    /**
     * The resolver factory.
     */
    @Reference
    private ResourceResolverFactory resolverFactory;


    /**
     * @param request
     * @param response
     */
    @Override
    protected final void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)

    {
        try (ResourceResolver resolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice")) {
            JSONObject obj = new JSONObject();
            String userName = ServiceUtils.getUserName(request);
            //String userNamePage = JcrUtil.createValidName(userName);
            
            String groupName = ServiceUtils.getUserGroup(resolverFactory, userName);
            String groupNamePage = JcrUtil.createValidName(groupName);

//            ResourceResolver resolver = request.getResourceResolver();
            String userPagePath = ServiceUtils.getUserPagePath(userName, groupNamePage, resolver);
            
            
            obj = uploadHistory.getBatchesJSON(request, userPagePath, resolver);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(obj.toString());

        } catch (RepositoryException | JSONException | IOException | ParseException | LoginException e) {
            LOG.info(e.getMessage(), e);
            LOG.info("Exception in fetching reviews::" + e.getMessage());
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

}
