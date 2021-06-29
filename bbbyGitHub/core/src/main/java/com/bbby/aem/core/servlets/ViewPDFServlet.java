package com.bbby.aem.core.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;

@Component(name = "View PDF Servlet", immediate = true, service = Servlet.class, property = {
		"sling.servlet.methods=" + HttpConstants.METHOD_GET, "sling.servlet.paths=/bin/bbby/bbby-view-pdf" })
public class ViewPDFServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Reference
	private ResourceResolverFactory resolverFactory;

	ResourceResolver resourceResolver;

	@Override
	protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {
		logger.info("View PDF Servlet get method");
		String payload = request.getParameter("assetPaths");

		try {
			resourceResolver = request.getResourceResolver();
			logger.info("payload path" + payload);

			// convert the payload path into a Resource
			Resource damResource = resourceResolver.resolve(payload);

			// further convert the resource into Dam asset
			Asset damAsset = damResource.adaptTo(Asset.class);
			if (damAsset != null) {
				response.setContentType("application/pdf");
				response.addHeader("Content-Disposition", "inline; filename=" + damAsset.getName());

				InputStream fileInputStream = damAsset.getOriginal().getStream();
				OutputStream responseOutputStream = response.getOutputStream();
				int bytes;
				while ((bytes = fileInputStream.read()) != -1) {
					responseOutputStream.write(bytes);
				}
			} else {
				logger.info("payload path does not exist : " + payload);
			}

		} catch (Exception e) {
			logger.error("Error in view PDF ::" + e.getMessage(), e);
			response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}