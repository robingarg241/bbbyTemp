package com.bbby.aem.core.servlets;

import java.io.IOException;
import java.text.ParseException;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.services.UploadHistoryDetailsService;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.commons.jcr.JcrUtil;


/**
 * The Class  UploadHistoryDetailsServlet.
 *
 * @author karthik.koduru, hero-digital.
 */
@Component(
    service = Servlet.class,
    immediate = true,
    name = "Upload History Details Servlet",
    property = {
        ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/bedbath/vendor-portal/history/batch-details",
        ServletResolverConstants.SLING_SERVLET_METHODS + "=GET"
    })
public class UploadHistoryDetailsServlet extends SlingAllMethodsServlet {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UploadHistoryDetailsServlet.class);

    /**
     * The batches service.
     */
    @Reference
    public UploadHistoryDetailsService uploadHistoryDetailsService;

    /**
     * The resolver factory.
     */
    @Reference
    private ResourceResolverFactory resolverFactory;


    /**
     *
     * @param request
     * @param response
     */
    @SuppressWarnings("deprecation")
    @Override
    protected final void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)

    {
        JSONObject obj = new JSONObject();
        try (ResourceResolver resolver = ServiceUtils.getResourceResolver(resolverFactory, "writeservice")) {
            String userName = ServiceUtils.getUserName(request);
            //String userNamePage = JcrUtil.createValidName(userName);
            String groupName = ServiceUtils.getUserGroup(resolverFactory, userName);
            String groupNamePage = JcrUtil.createValidName(groupName);
            String queryString = request.getQueryString();
            String[] queryValue = queryString.split("=");
            String batchID = queryValue[1];

//            ResourceResolver resolver = request.getResourceResolver();
            String userPagePath = ServiceUtils.getUserPagePath(userName, groupNamePage, resolver);
            
            obj = uploadHistoryDetailsService.getBatchDetailsJSON(request, userPagePath, batchID , resolver);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(obj.toString());
            resolver.close();
        } catch (RepositoryException | JSONException | IOException | ParseException | LoginException e) {
            LOG.error("Exception in fetching batch details::" + e.getMessage());
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

}
