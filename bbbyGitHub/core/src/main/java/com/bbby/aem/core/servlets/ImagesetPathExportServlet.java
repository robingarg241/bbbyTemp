package com.bbby.aem.core.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.util.CSVUtils;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.PartitionUtil;
import com.bbby.aem.core.util.ServiceUtils;

@Component(immediate = true, service = Servlet.class, property = { "sling.servlet.paths=/bin/imagesetpathexport",
		"sling.servlet.methods=post" })
public class ImagesetPathExportServlet extends SlingAllMethodsServlet {

	/** The settings service. */
	@Reference
	private SlingSettingsService settingsService;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant LOGGER. */
	private static final Logger log = LoggerFactory.getLogger(ImagesetPathExportServlet.class);

	/** The message. */
	StringBuilder message = new StringBuilder();

	@Override
	protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
	}

	@Override
	protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		log.info("In doPost Method for ImagesetPathExport Servlet");
		InputStream inputStream = null;
		String filename = null;
		PrintWriter out = response.getWriter();
		Session session = request.getResourceResolver().adaptTo(Session.class);
		try {
			String userName = request.getRemoteUser();
			if (userName == null) {
				userName = "unknown";
			}
			log.info("User Name.." + userName);
			// check file is multipart or not
			final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				// get submitted csv file from request
				RequestParameter filePart = request.getRequestParameter("csv");
				// check that submitted file is csv
				String uploadFileName = filePart.getFileName();
				log.info("Input file Name is.." + uploadFileName);
				if (uploadFileName.contains("csv")) {
					boolean isUpcValid = false;
					inputStream = filePart.getInputStream();
					isUpcValid = checkUpcValidation(inputStream, out);
					if (!isUpcValid) {
						log.info("Either CSV is blank or UPCs Value are not Numeric");
					} else {
						// make file directory if not exist
						File file = new File("csv_reports/Imageset_Details");
						file.mkdirs();
						filename = "csv_reports/Imageset_Details/file_" + userName + ".csv";
						FileWriter writer = new FileWriter(filename);
						CSVUtils.writeLine(writer, getHeaderList());
						BufferedReader reader = new BufferedReader(new InputStreamReader(filePart.getInputStream()));
						String newLine = null;
						reader.readLine();
						String imagesetPath = null;
						HashSet<String> imageSet = new HashSet<String>();
						while ((newLine = reader.readLine()) != null) {
							String[] valCol = newLine.split(",");
							imagesetPath = valCol[0];
							log.info("UPC Value is..." + valCol[0]);
							String queryString = "SELECT * FROM [dam:Asset] AS s WHERE (ISDESCENDANTNODE([/content/dam/bbby/approved_dam/assets/product/images/single_product]) OR ISDESCENDANTNODE([/content/dam/bbby/asset_transitions_folder/e-comm])) "
									+ "and (s.[jcr:content/upcmetadata/*/primaryUPC] = \"" + valCol[0]
									+ "\" AND s.[jcr:content/metadata/bbby:primaryImage] = \"yes\")";
							log.info("query is..." + queryString);
							Iterator<Resource> batchNodes = request.getResourceResolver().findResources(queryString,
									Query.JCR_SQL2);
							List<String> propertyList = new ArrayList<String>();
							int count = 0;
							StringBuilder memList = new StringBuilder();
							StringBuilder primList = new StringBuilder();
							while (batchNodes.hasNext()) {
								Resource batchResource = (Resource) batchNodes.next();
								Node imgNode = batchResource.adaptTo(Node.class);
								try {
									String Path = imgNode.getPath();
									if (Path.contains("_imageset")) {
										count++;
										memList.append(Path).append(";");

									} else {
										primList.append(Path).append(";");
										String ImgSetName = (imgNode != null
												&& imgNode.hasProperty("jcr:content/metadata/bbby:imageSetName"))
														? imgNode.getProperty("jcr:content/metadata/bbby:imageSetName")
																.getString()
														: null;
										if (ImgSetName != null) {
											String imgPath = null;
											if (session.nodeExists(imgNode.getParent().getPath() + "/" + ImgSetName)) {
												imgPath = imgNode.getParent().getPath() + "/" + ImgSetName;
											} else {
												imgPath = getImagesetPath(ImgSetName, session,
														request.getResourceResolver());
											}

											if (imgPath != null && session.nodeExists(imgPath)) {
												memList.append(imgPath).append(";");
												count++;
											} else {
												log.info("Imageset Path not exist");
											}
										}
									}

								} catch (Exception e) {
									log.error("error:", e);
								}

							}
							propertyList.add(valCol[0]);
							if (count > 0) {
								String finalVal = memList.substring(0, memList.length() - 1);
								String primVal = primList.substring(0, primList.length() - 1);
								propertyList.add(primVal);
								propertyList.add(finalVal);
								if (finalVal.contains("/e-comm") && finalVal.contains("/approved_dam")) {
									propertyList.add("Exist_E-comm_AD");
								} else if (finalVal.contains("/approved_dam")) {
									propertyList.add("Exist_AD");
								} else {
									propertyList.add("Exist_E-comm");
								}
								// add sling resources to csv
								addSlingResToCsv(propertyList,finalVal,session);
			
							} else {
								propertyList.add(primList.length() > 0 ? primList.substring(0, primList.length() - 1)
										: "Not Exist");
								propertyList.add("");
								propertyList.add("Not Exist");
								propertyList.add("");

							}
							CSVUtils.writeLine(writer, propertyList);
							imageSet.add(imagesetPath);
						}
						reader.close();
						writer.close();

						log.info("CSV file written successfully");

						exportDeleteGeneratedCsvFile(response, out, filename);

					}
				} else {
					log.info("Invalid file");
					out.println("File with name : " + uploadFileName + " received. This is not valid csv file");
				}
			} else {
				log.info("It is not Multipart File");
				out.println("Uploaded File is not Multipart");
			}
		} catch (Exception e) {
			log.info("Unable to export Imageset Path File", e.getMessage(), e);
		}

	}

	private void addSlingResToCsv(List<String> propertyList, String finalVal, Session session) throws Exception {
		StringTokenizer st = new StringTokenizer(finalVal, ";");
		while (st.hasMoreTokens()) {
			String val= null;
			StringBuilder sb = new StringBuilder();
			String stoken = st.nextToken();
			if (session.nodeExists(stoken + "/jcr:content/related/s7Set/sling:members")) {
				Node memNode = session.getNode(stoken + "/jcr:content/related/s7Set/sling:members");
				if(memNode != null && memNode.hasProperty("sling:resources")){
					 Value[] resValue = memNode.getProperty("sling:resources").getValues();
					if(resValue!=null){
						for (Value resVal : resValue) {
							sb.append(resVal.getString()).append(";");	
					}
						val = sb.substring(0, sb.length() - 1);	
					}	   
				}
				}
			propertyList.add(val!=null ? val : "");		
		}	
	}
	
	private void exportDeleteGeneratedCsvFile(SlingHttpServletResponse response, PrintWriter out, String filename)
			throws Exception {

		String outfilename = "UPC_ImagesetPath_Export.csv";
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + outfilename + "\"");

		FileInputStream fileInputStream = new FileInputStream(filename);

		int j;
		while ((j = fileInputStream.read()) != -1) {
			out.write(j);
		}
		fileInputStream.close();
		out.close();
		log.info("downloading csv file");
		File fileToDel = new File(filename);
		fileToDel.delete();
		log.info("Deleted file..." + filename);
	}

	private String getImagesetPathByQuery(String fileName, ResourceResolver resourceResolver) throws Exception {
		String imgsetPath = null;

		// We need to query the e-comm if imageset does not exist into Approved
		// Dam & primary Image path
		String q = "SELECT * FROM [dam:Asset] AS s WHERE ISDESCENDANTNODE([/content/dam/bbby/asset_transitions_folder/e-comm]) "
				+ "and NAME() = \"" + fileName + "\"";
		Iterator<Resource> batchNodes = resourceResolver.findResources(q, Query.JCR_SQL2);
		while (batchNodes.hasNext()) {

			Resource batchResource = (Resource) batchNodes.next();
			Node node = batchResource.adaptTo(Node.class);
			imgsetPath = node.getPath();
			log.info("Imageset Path By Query.." + imgsetPath);
		}
		return imgsetPath;
	}

	private String getImagesetPath(String fileName, Session session, ResourceResolver resourceResolver)
			throws Exception {
		String imgsetPath = null;
		ArrayList<String> partitions = PartitionUtil.hashFileName(fileName);
		// create the holding partitions if they don't exist
		String dest = "/content/dam/bbby/approved_dam/assets/product/images/image_sets";
		dest = dest + "/" + partitions.get(0);
		dest = dest + "/" + partitions.get(1);
		imgsetPath = dest + "/" + fileName;
		log.info("Imageset Path.." + imgsetPath);
		if (imgsetPath != null && session.nodeExists(imgsetPath)) {
			return imgsetPath;
		} else {
			imgsetPath = getImagesetPathByQuery(fileName, resourceResolver);
			return imgsetPath;
		}

	}

	private boolean checkUpcValidation(InputStream inputStream, PrintWriter out) throws Exception {
		boolean isUpcValid = false;
		ArrayList<String> upcList = new ArrayList<String>();
		ArrayList<String> wrongUpcList = new ArrayList<String>();
		// Iterate CSV to check the UPC value is numeric.
		BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		bf.readLine();
		while ((line = bf.readLine()) != null) {
			String[] valCol = line.split(",");
			boolean checkUpcFormat = ServiceUtils.isNumeric(valCol[0]);
			upcList.add(valCol[0]);
			if (!checkUpcFormat) {
				wrongUpcList.add(valCol[0]);
				log.info("UPC Value is not Numeric.." + valCol[0]);
				out.println("UPC Value is not Numeric.." + valCol[0]);
			}
		}
		bf.close();
		if (upcList.size() > 0 && wrongUpcList.size() == 0) {
			isUpcValid = true;
		}
		return isUpcValid;

	}

	private static List<String> getHeaderList() {
		List<String> headerNameList = new ArrayList<String>();
		headerNameList.add("UPC Value");
		headerNameList.add("Primary Image");
		headerNameList.add("Imageset Path");
		headerNameList.add("Imageset Exist");
		headerNameList.add("Sling Resources");
		return headerNameList;
	}

}
