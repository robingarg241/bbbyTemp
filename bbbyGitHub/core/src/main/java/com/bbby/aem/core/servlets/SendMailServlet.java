package com.bbby.aem.core.servlets;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.commons.email.EmailService;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import com.day.cq.mailer.MessageGatewayService;
 
@Component(
        name = "Marketing Send Mail Notification Servlet",
        immediate = true,
        service = Servlet.class,
        property = {
                "sling.servlet.methods=POST",
                "sling.servlet.paths=/bin/bbby/notify"
        }
)
public class SendMailServlet extends SlingAllMethodsServlet {
    private final Logger logger = LoggerFactory.getLogger(getClass());
 
   // private static String EMAIL_TEMPLATE_PATH = "/apps/eaem-assets-send-mail/mail-templates/send-assets.html";
 
    @Reference
    private MessageGatewayService messageGatewayService;
    
    @Reference
    private JobManager jobManager;
    
    @Reference
    private EmailService emailService;
 
    @Override
    protected void doPost(final SlingHttpServletRequest req, final SlingHttpServletResponse resp) throws ServletException, IOException {
 
        try{
            String to = req.getParameter("./to-notify");
            String cc = req.getParameter("./cc-notify");
            String subject = req.getParameter("./subject");
            String body = req.getParameter("./body");
            String assets = req.getParameter("./assets");
            
          //  String replyTo = "karan.kumar@idc.bedbath.com";
            
            String[] tos = null;
            String[] ccs = null;
            if(to != null && !to.trim().isEmpty()){
            	tos = to.split(",");
            }
            if(cc != null && !cc.trim().isEmpty()){
            	ccs = cc.split(",");
            }
            
            StringBuffer url = req.getRequestURL();
            String uri = req.getRequestURI();
            String host = url.substring(0, url.indexOf(uri)); //result
            
            String folderPath = "";
            // split by new line
            String[] assetsArray = assets.split("\\r?\\n");
            
			for (String asset : assetsArray) {
				if ("".equalsIgnoreCase(folderPath)) {
					folderPath = asset.substring(0, asset.lastIndexOf("/"));
					String anchorTag = "<a href='" + host + "/assets.html" + folderPath + "'>" + folderPath + "</a>";
					body = body.replaceAll(folderPath.trim(), anchorTag);
				}
			}
            
            for (String asset : assetsArray) {

                // create object of Path 
                Path path = Paths.get(asset); 
          
                // call getFileName() and get FileName path object 
                Path fileName = path.getFileName(); 
                String anchorTag = "";
                if(fileName.toString().contains(".")){
                	anchorTag = "<a href='"+host+"/assetdetails.html"+asset+"'>"+fileName+"</a>";
                }else{
                	anchorTag = "<a href='"+host+"/assets.html"+asset+"'>"+fileName+"</a>";
                }
                body = body.replaceAll("<"+fileName.toString()+">", anchorTag);
            }
            
            String userName = ServiceUtils.getUserName(req);
            body = body.replaceAll("<USER_EMAIL>", userName);
 
            Map<String, String> emailParams = new HashMap<String,String>();
            body = "<span style='font-family:Effra'>"+body+"</span>";
            emailParams.put("subject", subject);
            emailParams.put("body", body.replaceAll("\r\n", ""));
 
    		final Map<String, Object> props = new HashMap<String, Object>();    	
			logger.info("Queeing the job for sndind mail to vendor " + tos.toString());

    		props.put(CommonConstants.TO_MULTIPLE, tos);
    		if (ccs != null && ccs.length > 0)
    			props.put(CommonConstants.CC_MULTIPLE, ccs);
    		props.put(CommonConstants.SUBJECT, subject );
    		props.put(CommonConstants.MESSAGE, body);
    		//props.put(CommonConstants.REPLY_TO, replyTo);
    		
    		jobManager.addJob(CommonConstants.SEND_MAIL_TOPIC, props);
        }catch(Exception e){
            logger.error("Error sending email", e);
        }
    }
 
}