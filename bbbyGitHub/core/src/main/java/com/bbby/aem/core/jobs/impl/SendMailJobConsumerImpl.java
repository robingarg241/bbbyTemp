package com.bbby.aem.core.jobs.impl;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbby.aem.core.util.CommonConstants;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;

/**
 * Job consumer that downloads images to DAM and links them to product asset node.
 * More info https://sling.apache.org/documentation/bundles/apache-sling-eventing-and-job-handling.html
 *
 * @author vpokotylo
 * 
 */
@Component(immediate = true, service = JobConsumer.class,
    property = {"job.topics=" + CommonConstants.SEND_MAIL_TOPIC})
public class SendMailJobConsumerImpl implements JobConsumer {

    private static final Logger log = LoggerFactory.getLogger(SendMailJobConsumerImpl.class);
    
    @Reference
    private MessageGatewayService messageGatewayService;
    
    private File initialFile = null;
    
    private File initialFile1 = null;
	
    private MessageGateway<Email> messageGateway;
    
    @Override
	public JobResult process(final Job job) {

		log.debug("Starting a job " + job.getTopic());
		try {

			String to = (String) job.getProperty(CommonConstants.TO);
			String cc = (String) job.getProperty(CommonConstants.CC);
			String bcc = (String) job.getProperty(CommonConstants.BCC);
			String[] tos = (String[]) job.getProperty(CommonConstants.TO_MULTIPLE);
			String[] ccs = (String[]) job.getProperty(CommonConstants.CC_MULTIPLE);
			String[] bccs = (String[]) job.getProperty(CommonConstants.BCC_MULTIPLE);
			String replyTo = (String) job.getProperty(CommonConstants.REPLY_TO);
			String subject = (String) job.getProperty(CommonConstants.SUBJECT);
			String message = (String) job.getProperty(CommonConstants.MESSAGE);
			String filename = (String) job.getProperty(CommonConstants.FILENAME);
			String filename1 = (String) job.getProperty(CommonConstants.FILENAME1);

			if (StringUtils.isEmpty(to) && (tos == null || tos.length == 0)) {
				log.warn("Empty recipient");
				return JobConsumer.JobResult.CANCEL;
			}

			MultiPartEmail email = new HtmlEmail();
			
			if (to != null && !to.trim().isEmpty())
				email.addTo(to);
			if (cc != null && !cc.trim().isEmpty())
				email.addCc(cc);
			if (bcc != null && !bcc.trim().isEmpty())
				email.addBcc(bcc);
			if (tos != null && tos.length > 0)
				email.addTo(tos);
			if (ccs != null && ccs.length > 0)
				email.addCc(ccs);
			if (bccs != null && bccs.length > 0)
				email.addBcc(bccs);
			if (replyTo != null && !replyTo.trim().isEmpty())
				email.addReplyTo(replyTo);
			
			email.setSubject(subject);
			
			email.setMsg(message);
			
			// Check for filename if not empty, attach the file
            if (filename != null && !filename.trim().isEmpty()){
	            initialFile = new File(filename);  
	            email.attach(initialFile);
            }
            
         // Check for filename1 if not empty, attach the file
            if (filename1 != null && !filename1.trim().isEmpty()){
	            initialFile1 = new File(filename1);  
	            email.attach(initialFile1);
            }

			// Inject a MessageGateway Service and send the message
			messageGateway = messageGatewayService.getGateway(Email.class);

			// Check the logs to see that messageGateway is not null
			messageGateway.send((Email) email);
		
		} catch (Exception e) {
			log.error("Exception processing asset move request", e);
			return JobConsumer.JobResult.FAILED;
		}

		return JobConsumer.JobResult.OK;
	}

}
