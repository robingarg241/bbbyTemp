package com.bbby.aem.core.servlets;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.handler.AssetHandler;
import com.day.cq.dam.api.renditions.RenditionMaker;

@Component(name = "Download Custom Rendition Servlet", immediate = true, service = Servlet.class, property = {
		"sling.servlet.methods=" + HttpConstants.METHOD_GET,
		"sling.servlet.paths=/bin/bbby/download-custom-rendition.zip" })
public class DownloadCustomRenditionServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private RenditionMaker renditionMaker;

	@Reference
	AssetHandler assetHandler;

	private static String JPEG_FORMAT = "jpeg";
	private static String PNG_FORMAT = "png";
	private static int DEFAULT_WIDTH = 2000;
	private static int DEFAULT_HEIGHT = 2000;
	private static int DEFAULT_DPI_72 = 72;
	private static int DEFAULT_DPI_300 = 300;

	ResourceResolver resourceResolver;

	@Override
	protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {
		logger.info("Download Custom Rendition Servlet get method");
		String payloadString = request.getParameter("assetspaths");

		try {
			resourceResolver = request.getResourceResolver();
			logger.info("payload path" + payloadString);

			String[] payloadStrings = null;
			if (payloadString != null && !payloadString.trim().isEmpty()) {
				// split by new line
	            payloadStrings = payloadString.split("\\r?\\n");
			}
			List<Asset> damAssets = getAssetList(payloadStrings);

			if (damAssets != null && damAssets.size() > 0) {

				// Create a MAP to store results
				Map<String, InputStream> dataMap = getRenditions(request, damAssets);

				// ZIP up the AEM DAM Assets
				byte[] zip = zipFiles(dataMap);

				// String imageName = damAsset.getName();
				String filename = "DAM_Reditions.zip";
				// response.setContentType("text/html");
				OutputStream out = response.getOutputStream();
				response.setContentType("application/octet-stream");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
				out.write(zip);
				out.close();

				logger.info("The ZIP is sent");

			}

		} catch (Exception e) {
			logger.error("Error in creating Asset ::" + e.getMessage(), e);
			response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}


	private Map<String, InputStream> getRenditions(SlingHttpServletRequest request, List<Asset> damAssets) throws IOException {
		logger.info("Download Custom Rendition Servlet getRendition method");
		// create a Rendition Template using Rendition Maker Api and
		// give the width, height, quality, mimietype for your template

		String jpg72 = request.getParameter("jpg72");
		logger.info("jpg72 : " + jpg72);
		String png72 = request.getParameter("png72");
		logger.info("png72 : " + png72);
		String jpg300 = request.getParameter("jpg300");
		logger.info("jpg300 : " + jpg300);
		String png300 = request.getParameter("png300");
		logger.info("png300 : " + png300);
		String other = request.getParameter("other");
		logger.info("other : " + other);

		Map<String, InputStream> renditionList = new HashMap<String, InputStream>();

		for (Asset damAsset : damAssets) {

			if ("checked".equalsIgnoreCase(jpg72)) {
				renditionList.putAll(convert(damAsset, DEFAULT_WIDTH, DEFAULT_HEIGHT,
						DEFAULT_DPI_72, JPEG_FORMAT));
			}

			if ("checked".equalsIgnoreCase(png72)) {
				renditionList.putAll(convert(damAsset, DEFAULT_WIDTH, DEFAULT_HEIGHT,
						DEFAULT_DPI_72, PNG_FORMAT));
			}

			if ("checked".equalsIgnoreCase(jpg300)) {
				renditionList.putAll(convert(damAsset, DEFAULT_WIDTH, DEFAULT_HEIGHT,
						DEFAULT_DPI_300, JPEG_FORMAT));
			}

			if ("checked".equalsIgnoreCase(png300)) {
				renditionList.putAll(convert(damAsset, DEFAULT_WIDTH, DEFAULT_HEIGHT,
						DEFAULT_DPI_300, PNG_FORMAT));
			}

			if ("checked".equalsIgnoreCase(other)) {
				Map<String, String> datamap = getTemplateMap(request, damAsset);
				if (!datamap.isEmpty() && !ServiceUtils.isNullOrEmpty(datamap.get("width"))
						&& !ServiceUtils.isNullOrEmpty(datamap.get("height"))
						&& !ServiceUtils.isNullOrEmpty(datamap.get("resolution"))
						&& !ServiceUtils.isNullOrEmpty(datamap.get("format")))
					renditionList.putAll(convert(damAsset,
							Integer.parseInt(datamap.get("width")), Integer.parseInt(datamap.get("height")),
							Integer.parseInt(datamap.get("resolution")), datamap.get("format")));
			}
		}

		return renditionList;
	}

	private byte[] zipFiles(Map<String, InputStream> data) throws IOException {
		logger.info("Download Custom Rendition Servlet zipFiles method");
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
		byte bytes[] = new byte[2048];
		Iterator<Map.Entry<String, InputStream>> entries = data.entrySet().iterator();

		while (entries.hasNext()) {
			Map.Entry<String, InputStream> entry = entries.next();

			String fileName = (String) entry.getKey();
			InputStream inputStream = (InputStream) entry.getValue();

			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

			// populate the next entry of the ZIP with the AEM DAM asset
			zipArchiveOutputStream.putArchiveEntry(new ZipArchiveEntry(fileName));

			int bytesRead;
			while ((bytesRead = bufferedInputStream.read(bytes)) != -1) {
				zipArchiveOutputStream.write(bytes, 0, bytesRead);

			}
			zipArchiveOutputStream.closeArchiveEntry();
			bufferedInputStream.close();
			inputStream.close();

		}

		zipArchiveOutputStream.finish();
		byteArrayOutputStream.flush();
		zipArchiveOutputStream.close();
		byteArrayOutputStream.close();
		return byteArrayOutputStream.toByteArray();
	}

	private Map<String, String> getTemplateMap(SlingHttpServletRequest request, Asset damAsset) {
		logger.info("Download Custom Rendition Servlet getTemplateMap method");
		// Create a MAP to store results
		Map<String, String> dataMap = new HashMap<String, String>();
		String width = request.getParameter("width");
		String height = request.getParameter("height");
		String resolution = request.getParameter("resolution");
		String format = request.getParameter("format");// JPEG_TEMPLATE;
		String calculateHeight = request.getParameter("calculateHeight");
		String calculateWidth = request.getParameter("calculateWidth");

		if (!ServiceUtils.isNullOrEmpty(resolution)) {
			dataMap.put("resolution", resolution);
		} else {
			dataMap.put("resolution", "72");
		}

		if (!ServiceUtils.isNullOrEmpty(width)) {
			dataMap.put("width", width);
		} else {
			dataMap.put("width", "500");
		}

		if (!ServiceUtils.isNullOrEmpty(height)) {
			dataMap.put("height", height);
		} else {
			dataMap.put("height", "500");
		}

		if (!ServiceUtils.isNullOrEmpty(format)) {
			dataMap.put("format", format);
		} else {
			dataMap.put("format", JPEG_FORMAT);
		}

		// calculate the width/height automatically
		if ("checked".equalsIgnoreCase(calculateHeight) && !ServiceUtils.isNullOrEmpty(width)) {
			String imageLength = damAsset.getMetadataValue("tiff:ImageLength");
			String imageWidth = damAsset.getMetadataValue("tiff:ImageWidth");

			double ratio = ServiceUtils.calculatePercentage(Double.parseDouble(width), Double.parseDouble(imageWidth));
			height = Integer.toString((int) ServiceUtils.calculateValueByPercentage(Double.parseDouble(imageLength), ratio));
			dataMap.put("height", height);

		} else if ("checked".equalsIgnoreCase(calculateWidth) && !ServiceUtils.isNullOrEmpty(height)) {
			String imageLength = damAsset.getMetadataValue("tiff:ImageLength");
			String imageWidth = damAsset.getMetadataValue("tiff:ImageWidth");

			double ratio = ServiceUtils.calculatePercentage(Double.parseDouble(height),
					Double.parseDouble(imageLength));
			width = Integer
					.toString((int) ServiceUtils.calculateValueByPercentage(Double.parseDouble(imageWidth), ratio));
			dataMap.put("width", width);

		}

		return dataMap;
	}

	private Map<String, InputStream> convert(Asset damAsset, int width, int height, int dpi, String format) {
		logger.info("Download Custom Rendition Servlet convert method");
		try {
			InputStream is = damAsset.getOriginal().getStream();
			Pipe pipeIn = new Pipe(is, null);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			Pipe pipeOut = new Pipe(null, os);
			ConvertCmd convert = new ConvertCmd();

			// convert.setSearchPath("C:\\Program Files\\ImageMagick-7.0.10-Q16-HDRI");

			convert.setInputProvider(pipeIn);
			convert.setOutputConsumer(pipeOut);

			String imageName = damAsset.getName();
			imageName = imageName.substring(0, imageName.lastIndexOf("."));

			String newName = imageName + "_" + width + "_" + height + "_" + dpi + "." + format;

			IMOperation op = getIMOperation(width, height, dpi, format);

			// execute the operation
			convert.run(op);

			Map<String, InputStream> map = new HashMap<String, InputStream>();
			map.put(newName, new ByteArrayInputStream(os.toByteArray()));
			return map;
		} catch (IOException ex) {
			logger.error("ImageMagic - IOException %s", ex);
		} catch (InterruptedException ex) {
			logger.error("ImageMagic - InterruptedException %s", ex);
		} catch (IM4JavaException ex) {
			logger.error("ImageMagic - IM4JavaException %s", ex);
		}
		return null;
	}

	private List<Asset> getAssetList(String[] payloads) {
		List<Asset> assets = new ArrayList<Asset>();

		if(payloads == null){
			return null;
		}
		
		for (String payload : payloads) {
			// convert the payload path into a Resource
			Resource damResource = resourceResolver.resolve(payload);

			// further convert the resource into Dam asset
			Asset damAsset = damResource.adaptTo(Asset.class);

			if (damAsset != null) {
				logger.info("dam asset exists .. " + payload);
				assets.add(damAsset);
			} else {
				logger.info("dam asset does not exist .. " + payload);
			}
		}
		return assets;
	}
	
	private IMOperation getIMOperation(int width, int height, int dpi, String format) {
		logger.info("Download Custom Rendition Servlet getIMOperation method : {}, {}, {} and {}", width, height, dpi, format);
		IMOperation op = new IMOperation();
		op.addImage("-");
		op.units("PixelsPerInch");
		op.density(dpi);
		op.resize(width, height, '^');
		op.addImage(format + ":-");
		return op;
	}
}