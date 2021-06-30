package org.erasmusmc.rremanager.smtp;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SMTPMailClient {
	
	private String mailServer;
	private String port;
	private String userName;
	private String password;
	private String error;

	
	public SMTPMailClient(String mailServer, String port, String userName, String password) {
		this.mailServer = mailServer;
		this.port = port;
		this.userName = userName;
		this.password = password;
		error = "";
	}
	
	
	public boolean sendMail(String subject, String html, String text, String from, List<String> recipients, List<String> ccRecipients, List<String> bccRecipients, List<String> attachments) {
		boolean success = false;
		error = "";
		
		Properties properties = new Properties();
	    properties.put("mail.smtp.auth", "true");
	    properties.put("mail.smtp.starttls.enable", "true");
	    properties.put("mail.smtp.host", mailServer);
	    properties.put("mail.smtp.port", port);

	    // Get the Session object.
	    Session session = Session.getInstance(
	    								properties,
	    								new javax.mail.Authenticator() {
	    									protected PasswordAuthentication getPasswordAuthentication() {
	    										return new PasswordAuthentication(userName, password);
	    									}
	    								}
	    							);

	    try {
	    	// Create a default MimeMessage object.
	        Message message = new MimeMessage(session);

	        // Set From: header field of the header.
	        message.setFrom(new InternetAddress(from));

	        // Set To: header field of the header.
	        if (recipients != null) {
	        	// Add to recipients
	        	String to = "";
	        	for (String recipient : recipients) {
	        		recipient = recipient.trim();
	        		if (!recipient.equals("")) {
	        			to = to + (to.equals("") ? "" : ",") + recipient;
	        		}
	        	}
	        	if (!to.equals("")) {
	    	        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
	    	        
	    	        // Add cc recipients
	    	        if (ccRecipients != null) {
	    	        	String cc = "";
	    	        	for (String ccRecipient : ccRecipients) {
	    	        		ccRecipient = ccRecipient.trim();
	    	        		if (!ccRecipient.equals("")) {
	    	        			cc = cc + (cc.equals("") ? "" : ",") + ccRecipient;
	    	        		}
	    	        	}
	    	        	if (!cc.equals("")) {
	    	    	        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
	    	        	}
	    	        }
	    	        
	    	        // Add bcc recipients
	    	        if (bccRecipients != null) {
	    	        	String bcc = "";
	    	        	for (String bccRecipient : bccRecipients) {
	    	        		bccRecipient = bccRecipient.trim();
	    	        		if (!bccRecipient.equals("")) {
	    	        			bcc = bcc + (bcc.equals("") ? "" : ",") + bccRecipient;
	    	        		}
	    	        	}
	    	        	if (!bcc.equals("")) {
	    	    	        message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
	    	        	}
	    	        }

	    	        // Set Subject: header field
	    	        message.setSubject(subject);

	    	        // Create the message HTML part
	    	        MimeBodyPart messageHTMLPart = null;
	    	        if (html != null) {
	    	        	messageHTMLPart = new MimeBodyPart();
	    	        	messageHTMLPart.setContent(html, "text/html");
	    	        }

	    	        // Create the message text part
	    	        MimeBodyPart messageTextPart = null;
	    	        if (text != null) {
		    	        messageTextPart = new MimeBodyPart();
		    	        messageTextPart.setText(text);
	    	        }

	    	        // Create a multipart message
	    	        Multipart multipart = new MimeMultipart();

	    	        // Set text message part
	    	        if (messageTextPart != null) {
	    	        	multipart.addBodyPart(messageTextPart);
	    	        }

	    	        // Set HTML message part
	    	        if (messageHTMLPart != null) {
	    	        	multipart.addBodyPart(messageHTMLPart);
	    	        }

	    	        // Add attachments
	    	        if (attachments != null) {
	    		        for (String attachment : attachments) {
	    		        	String fileName = attachment;
	    		        	if (attachment.contains(File.separator)) {
	    		        		fileName = attachment.substring(attachment.lastIndexOf(File.separator) + 1);
	    		        	}
	    		        	MimeBodyPart messageFilePart = new MimeBodyPart();
	    			        DataSource source = new FileDataSource(attachment);
	    			        messageFilePart.setDataHandler(new DataHandler(source));
	    			        messageFilePart.setFileName(fileName);
	    			        multipart.addBodyPart(messageFilePart);
	    		        }
	    	        }

	    	        // Send the complete message parts
	    	        message.setContent(multipart);

	    	        // Send message
	    	        Transport.send(message);
	    	  
	    	        success = true;
	        	}
	        	else {
	        		error = "No recipients specified";
	        	}
	        }
        	else {
        		error = "No recipients specified";
        	}
	    } catch (MessagingException e) {
	    	error = e.getMessage();
	    }
		
		return success;
	}
	
	
	public String getError() {
		return error;
	}
	
	
	public void setError(String error) {
		this.error = error;
	}
}
