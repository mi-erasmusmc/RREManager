package org.erasmusmc.rremanager.smtp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.files.UserData;
import org.erasmusmc.rremanager.gui.EmailEditor;
import org.erasmusmc.rremanager.gui.MainFrame;

public class Mail {
	private RREManager rreManager = null;
	private MainFrame mainFrame = null;
	private SMTPMailClient mailClient = null;
	private EmailEditor emailEditor = null;
	private Map<String, String> additionalInfo = null;

	
	public Mail(RREManager rreManager, MainFrame mainFrame, String messageType, List<String[]> recipientsData) {
		this.rreManager = rreManager;
		this.mainFrame = mainFrame;

		if (!messageType.equals("")) {	
			if (getMailClient()) {
				emailEditor = rreManager.getEmailEditor();

				if (messageType.equals("Account Mail")) {
					sendAccountInformation(recipientsData);
				}
				else if (messageType.equals("Password Mail")) {
					sendPasswords(recipientsData);
				}
				else if (messageType.equals("Firewall Add Mail")) {
					sendFirewallAddRequest(recipientsData);
				}
				else if (messageType.equals("Firewall Remove Mail")) {
					sendFirewallRemoveRequest(recipientsData);
				}
				else {
					sendOtherMail(messageType, recipientsData);
				}
			}
		}
	}
	
	
	private boolean getMailClient() {
		boolean mailClientSet = false;
		mailClient = null;
		String password = rreManager.getPasswordManager().getPassword("EMail Password", rreManager.getAdministrator());
		if (password != null) {
			mailClient = new SMTPMailClient(
					RREManager.getIniFile().getValue("SMTP Mail Server","Server"), 
					RREManager.getIniFile().getValue("SMTP Mail Server","Port"), 
					RREManager.getIniFile().getValue(rreManager.getAdministrator(), "User Account"),  
					RREManager.getIniFile().getValue("SMTP Mail Server","SMTP Authentication"), 
					RREManager.getIniFile().getValue("SMTP Mail Server","Enable TLS"),
					password
					);
			mailClientSet = true;
		}
		return mailClientSet;
	}
	
	
	private void sendAccountInformation(List<String[]> recipientsData) {
		for (String[] recipientData : recipientsData) {
			mainFrame.logLn("");
			if (recipientData[UserData.ACCESS].equals("FTP-Only")) { // FTP-Only user
				String messageType = "FTP-Only Account Mail";
				if (RREManager.getIniFile().hasGroup(messageType)) {							
					List<String> attachments = new ArrayList<String>();
					attachments.addAll(getAttachments(messageType));

					String allTimeLogRecord = "Send " + messageType;
					allTimeLogRecord += "," + "\"" + recipientData[UserData.EMAIL] + "\"";
					allTimeLogRecord += "," + "\"" + recipientData[UserData.USER_NAME] + "\"";
					allTimeLogRecord += "," + "\"" + recipientData[UserData.FIRST_NAME] + "\"";
					allTimeLogRecord += "," + "\"" + recipientData[UserData.LAST_NAME] + "\"";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					
					//mail(messageType, recipientData, attachments, null, allTimeLogRecord, false, false);
				}
				else {
					mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
					JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				String messageType = "RDP Account Mail";
				if (RREManager.getIniFile().hasGroup(messageType)) {
					if (!recipientData[UserData.MULTIOTP_PDF].equals("")) {
						List<String> attachments = new ArrayList<String>();
						attachments.add(recipientData[UserData.MULTIOTP_PDF]);
						attachments.addAll(getAttachments(messageType));
						
						String allTimeLogRecord = "Send " + messageType;
						allTimeLogRecord += "," + "\"" + recipientData[UserData.EMAIL] + "\"";
						allTimeLogRecord += "," + "\"" + recipientData[UserData.USER_NAME] + "\"";
						allTimeLogRecord += "," + "\"" + recipientData[UserData.FIRST_NAME] + "\"";
						allTimeLogRecord += "," + "\"" + recipientData[UserData.LAST_NAME] + "\"";
						allTimeLogRecord += ",";
						allTimeLogRecord += ",";
						allTimeLogRecord += ",";
						
						mainFrame.logWithTimeLn("Send " + messageType + " to " + UserData.getUserDescription(recipientData, true) + " as " + recipientData[UserData.EMAIL_FORMAT] + " ...");
						
						//mail(messageType, recipientData, attachments, null, allTimeLogRecord, false, false);
					}
					else {
						mainFrame.logWithTimeLn("ERROR: No multiOTP pdf file found!");
						JOptionPane.showMessageDialog(mainFrame.getFrame(), "No multiOTP pdf file found for " + recipientData[UserData.FIRST_NAME] + " " + recipientData[UserData.LAST_NAME] + " (" + recipientData[UserData.EMAIL] + ")", "No pdf", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
					JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	
	private void sendPasswords(List<String[]> recipientsData) {
		
	}
	
	
	private void sendFirewallAddRequest(List<String[]> recipientsData) {
		
	}
	
	
	private void sendFirewallRemoveRequest(List<String[]> recipientsData) {
		
	}
	
	
	private boolean sendOtherMail(String messageType, List<String[]> recipientsData) {
		boolean success = false;
		
		if (RREManager.getIniFile().getValue(messageType, "Type").equals("MULTIPLE")) {
			String[] sender = new String[UserData.OBJECT_SIZE];
			sender[UserData.EMAIL] = RREManager.getIniFile().getValue("SMTP Mail Server", "From");
			Map<String, List<String>> bccRecipients = new HashMap<String, List<String>>();
			Map<String, List<String[]>> bccRecipientsData = new HashMap<String, List<String[]>>();
			for (String recipient[] : recipientsData) {
				List<String> formatBCCRecipients = bccRecipients.get(recipient[UserData.EMAIL_FORMAT]);
				List<String[]> formatBCCRecipientsData = bccRecipientsData.get(recipient[UserData.EMAIL_FORMAT]);
				if (formatBCCRecipients == null) {
					formatBCCRecipients = new ArrayList<String>();
					bccRecipients.put(recipient[UserData.EMAIL_FORMAT], formatBCCRecipients);
				}
				if (formatBCCRecipientsData == null) {
					formatBCCRecipientsData = new ArrayList<String[]>();
					bccRecipientsData.put(recipient[UserData.EMAIL_FORMAT], formatBCCRecipientsData);
				}
				formatBCCRecipients.add(recipient[UserData.EMAIL]);
				formatBCCRecipientsData.add(recipient);
			}
			additionalInfo = new HashMap<String, String>();
			for (String format : bccRecipients.keySet()) {
				mainFrame.logLn("");
				sender[UserData.EMAIL_FORMAT] = format;
				String emailText = getEmailText(messageType, sender); // Mail from sender to sender. Real recipients in BCC.
				emailEditor.setAdditionalInfo(additionalInfo);
				emailEditor.editEmail(emailText, format, sender, recipientsData, RREManager.getIniFile().getValue(messageType, "Subject"), true);
				additionalInfo = emailEditor.getAdditionalInfo();
				List<String> attachments = getAttachments(messageType);

				String bccString = "";
				mainFrame.logWithTimeLn("Send " + messageType + " to " + UserData.getUserDescription(sender, true) + " as " + format + " ...");
				for (int bccNr = 0; bccNr < bccRecipients.get(format).size(); bccNr++) {
					mainFrame.logWithTimeLn("  BCC " + Integer.toString(bccNr + 1) + ": " + UserData.getUserDescription(bccRecipientsData.get(format).get(bccNr), true));
					bccString += (bccNr == 0 ? "" : ";") + bccRecipientsData.get(format).get(bccNr)[UserData.EMAIL];
				}
				for (int attachmentNr = 0; attachmentNr < attachments.size(); attachmentNr++) {
					mainFrame.logWithTimeLn("  Attachment " + Integer.toString(attachmentNr + 1) + ": " + attachments.get(attachmentNr));
				}
				
				String allTimeLogRecord = "Send " + messageType;
				allTimeLogRecord += "," + "\"" + sender[UserData.EMAIL] + "\"";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += "," + "\"" + bccString + "\"";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				
				if (!emailEditor.isApproved()) {
					allTimeLogRecord += "," + "No";
					allTimeLogRecord += "," + "Failed";
					allTimeLogRecord += "," + "Rejected by user";
				}
				else {					
					allTimeLogRecord += "," + "Yes";

					String approvedSubject = emailEditor.getApprovedSubject();
					String approvedEmailText = emailEditor.getApprovedText();
					List<String> recipients = new ArrayList<String>();
					recipients.add(sender[UserData.EMAIL]);
										
					mainFrame.logWithTimeLn("Subject: " + approvedSubject);
					mainFrame.logWithTimeLn("Text: " + approvedEmailText);
					
					success = Send(format, approvedSubject, approvedEmailText, recipients, bccRecipients.get(format), attachments, allTimeLogRecord);
				}
			}
		}
		else {
			additionalInfo = new HashMap<String, String>();
			for (String[] recipient : recipientsData) {
				String emailText = getEmailText(messageType, recipient);
				emailEditor.setAdditionalInfo(additionalInfo);
				emailEditor.editEmail(emailText, recipient[UserData.EMAIL_FORMAT], recipient, null, RREManager.getIniFile().getValue(messageType, "Subject"), true);
				additionalInfo = emailEditor.getAdditionalInfo();
				List<String> attachments = getAttachments(messageType);

				String allTimeLogRecord = "Send " + messageType;
				allTimeLogRecord += "," + "\"" + recipient[UserData.EMAIL] + "\"";
				allTimeLogRecord += "," + "\"" + recipient[UserData.USER_NAME] + "\"";
				allTimeLogRecord += "," + "\"" + recipient[UserData.FIRST_NAME] + "\"";
				allTimeLogRecord += "," + "\"" + recipient[UserData.LAST_NAME] + "\"";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";

				mainFrame.logWithTimeLn("Send " + messageType + " to " + UserData.getUserDescription(recipient, true) + " as " + recipient[UserData.EMAIL_FORMAT] + " ...");
				for (int attachmentNr = 0; attachmentNr < attachments.size(); attachmentNr++) {
					mainFrame.logWithTimeLn("  Attachment " + Integer.toString(attachmentNr + 1) + ": " + attachments.get(attachmentNr));
				}
				
				if (!emailEditor.isApproved()) {
					allTimeLogRecord += "," + "No";
					allTimeLogRecord += "," + "Failed";
					allTimeLogRecord += "," + "Rejected by user";
					
					mainFrame.logWithTimeLn("REJECTED BY USER");
				}
				else {
					allTimeLogRecord += "," + "Yes";
					
					String approvedSubject = emailEditor.getApprovedSubject();
					String approvedEmailText = emailEditor.getApprovedText();
					List<String> recipients = new ArrayList<String>();
					recipients.add(recipient[UserData.EMAIL]);
					
					mainFrame.logWithTimeLn("Subject: " + approvedSubject);
					mainFrame.logWithTimeLn("Text: " + approvedEmailText);
					
					success = Send(recipient[UserData.EMAIL_FORMAT], approvedSubject, approvedEmailText, recipients, null, attachments, allTimeLogRecord);
				}
			}
		}
		
		return success;
	}
	
	
	private boolean Send(String messageFormat, String messageSubject, String messageText, List<String> recipients, List<String> bccRecipients, List<String> attachments, String allTimeLogRecord) {
		boolean success = false;

		String attachementsString = "";
		String unreadableAttachments = "";
		if (attachments != null) {
			for (int attachentNr = 0; attachentNr < attachments.size(); attachentNr++) {
				attachementsString += (attachementsString.equals("") ? "" : ",") + attachments.get(attachentNr);
				File attachementFile = new File(attachments.get(attachentNr));
				boolean unreadable = false;
				if (!attachementFile.canRead()) {
					unreadableAttachments += (unreadableAttachments.equals("") ? "Unreadable attachments: " : " ") + Integer.toString(attachentNr + 1);
					unreadable = true;
				}
				mainFrame.logWithTimeLn((attachentNr == 0 ? "    Attachments: " : "                 ") + attachments.get(attachentNr) + (unreadable ? " UNREADABLE" : ""));
			}
		}
		
		if (unreadableAttachments.equals("")) {
			String html = messageText;
			String text = null;
			if (!messageFormat.equals("HTML")) {
				html = null;
				text = messageText;
			}
			
			String sender = RREManager.getIniFile().getValue("SMTP Mail Server", "From");
			List<String> ccRecipients = new ArrayList<String>();
			ccRecipients.add(sender);
			
			allTimeLogRecord += ",";
			allTimeLogRecord += ",";
			
			success = mailClient.sendMail(messageSubject, html, text, sender, recipients, ccRecipients, bccRecipients, attachments);
			
			if (success) {
				allTimeLogRecord += "," + "Succeeded";
				allTimeLogRecord += ",";
				
				mainFrame.logWithTimeLn("SUCCEEDED");
			}
			else {
				allTimeLogRecord += "," + "Failed";
				allTimeLogRecord += "," + "\"" + mailClient.getError() + "\"";
				
				mainFrame.logWithTimeLn("FAILED: " + mailClient.getError());
			}
		}
		else {
			allTimeLogRecord += "," + "Failed";
			allTimeLogRecord += "," + "\"" + unreadableAttachments + "\"";
			
			mainFrame.logWithTimeLn("FAILED: " + unreadableAttachments);
		}
		
		mainFrame.allTimeLog(allTimeLogRecord, attachementsString);
		
		return success;
	}
	
	
	private List<String> getAttachments(String messageType) {
		List<String> attachments = new ArrayList<String>();
		if (RREManager.getIniFile().hasGroup(messageType)) {
			Integer attachmentNr = 1;
			String attachment = null;
			do { 
				attachment = RREManager.getIniFile().getValue(messageType, "Attachment_" + attachmentNr);
				if (attachment != null) {
					attachments.add(attachment);
				}
				attachmentNr++;
			} while (attachment != null);
		}
		return attachments;
	}
	
	
	private String getEmailText(String messageType, String[] recipient) {
		String email = "";
		if (recipient[UserData.EMAIL_FORMAT].equals("HTML")) {
			email += "<html>\r\n";
			email += "  <body>\r\n";
			email += "    <p style=\"font-family: arial; font-size: 11pt; font-weight: normal; color: #000000;\">\r\n";
			email += getEmailText(messageType, recipient, "      ");
			email += "    </p>\r\n";
			email += "  </body>\r\n";
			email += "</html>\r\n";
		}
		else {
			email = getEmailText(messageType, recipient, "") + "\r\n";
		}
		return email;
	}
	
	
	private String getEmailText(String messageType, String[] recipient, String indent) {
		String email = "";
		Integer lineNr = 1;
		String line = null;
		do { 
			line = RREManager.getIniFile().getValue(messageType, "Text_" + lineNr);
			if (line != null) {
				if (line.startsWith("[Picture_") && line.endsWith("]")) {
					if (recipient[UserData.EMAIL_FORMAT].equals("HTML")) {
						email += indent + "<br>\r\n";
						email += indent + RREManager.getIniFile().getValue(messageType, line.substring(1, line.length() - 1)) + "<br>\r\n";
					}
				}
				else if (line.equals("[IP ADDRESSES]")) {
					if (recipient[UserData.EMAIL_FORMAT].equals("HTML")) {
						email += indent + "<table>" + "\r\n";
						email += indent + "  <tr>\r\n";
						email += indent + "    <td>&nbsp;&nbsp;&nbsp;</td>\r\n";
						email += indent + "    <td><b>IP-Address</b></td>\r\n";
						email += indent + "    <td>&nbsp;&nbsp;&nbsp;</td>\r\n";
						email += indent + "    <td><b>User(s)</b></td>\r\n";
						email += indent + "  </tr>\r\n";
					}
					int ipNr = 0;
					while ((UserData.OBJECT_SIZE + (ipNr * 2) + 1) < recipient.length) {
						String ipAddress = recipient[UserData.OBJECT_SIZE + (ipNr *2)];
						String users = recipient[UserData.OBJECT_SIZE + (ipNr *2) + 1];
						if (recipient[UserData.EMAIL_FORMAT].equals("HTML")) {
							email += indent + "  <tr>\r\n";
							email += indent + "    <td>&nbsp;&nbsp;&nbsp;</td>\r\n";
							email += indent + "    <td>" + ipAddress + "</td>\r\n";
							email += indent + "    <td>&nbsp;&nbsp;&nbsp;</td>\r\n";
							email += indent + "    <td>" + users + "</td>\r\n";
							email += indent + "  </tr>\r\n";
						}
						else {
							email += indent + "  " + ipAddress + " (" + users + ")\r\n";
						}
						ipNr++;
					}
					if (recipient[UserData.EMAIL_FORMAT].equals("HTML")) {
						email += indent + "</table>" + "\r\n";
					}
				}
				else if (line.equals("[ADMINISTRATOR TELEPHONE]")) {
					String telephone = RREManager.getIniFile().getValue(rreManager.getAdministrator(), "Telephone");
					if ((telephone != null) && (!telephone.equals(""))) {
						email += indent + "Tel.: " + telephone + "<br>\r\n";
					} 
				}
				else if (line.startsWith("[") && line.endsWith("]") && RREManager.getIniFile().hasGroup(line.substring(1, line.length() - 1))) {
					email += getEmailText(line.substring(1, line.length() - 1), recipient, indent);
				}
				else {
					if (line.contains("[ADMINISTRATOR TITLE]")) {
						String title = RREManager.getIniFile().getValue(rreManager.getAdministrator(), "Title");
						if ((title != null) && (!title.equals(""))) {
							title = ", " + title;
						}
						else {
							title = "";
						}
						line = replaceAllTags(line, "[ADMINISTRATOR TITLE]", title);
					}
					line = replaceAllTags(line, "[BOLD START]", recipient[UserData.EMAIL_FORMAT].equals("HTML") ? "<b>" : "");
					line = replaceAllTags(line, "[BOLD END]", recipient[UserData.EMAIL_FORMAT].equals("HTML") ? "</b>" : "");
					line = replaceAllTags(line, "[ITALIC START]", recipient[UserData.EMAIL_FORMAT].equals("HTML") ? "<i>" : "");
					line = replaceAllTags(line, "[ITALIC END]", recipient[UserData.EMAIL_FORMAT].equals("HTML") ? "</i>" : "");
					line = replaceAllTags(line, "[FIRST NAME]", recipient[UserData.FIRST_NAME]);
					line = replaceAllTags(line, "[LAST NAME]", recipient[UserData.LAST_NAME]);
					line = replaceAllTags(line, "[USER NAME]", recipient[UserData.USER_NAME]);
					line = replaceAllTags(line, "[PASSWORD]", recipient[UserData.PASSWORD]);
					line = replaceAllTags(line, "[ADMINISTRATOR]", rreManager.getAdministrator());
					line = replaceSpecialCharacters(line);
					email += indent + line + (recipient[UserData.EMAIL_FORMAT].equals("HTML") ? "<br>" : "") + "\r\n";
				}
			}
			lineNr++;
		} while (line != null);
		return email;
	}
	
	
	private String replaceAllTags(String string, String tag, String replaceBy) {
		String newString = "";
		int tagIndex = string.indexOf(tag);
		while (tagIndex != -1) {
			newString += string.substring(0, tagIndex) + replaceBy;
			string = string.substring(tagIndex + tag.length());
			tagIndex = string.indexOf(tag);
		}
		newString += string;
		return newString;
	}
	
	
	private String replaceSpecialCharacters(String line) {
		line.replaceAll("\"", "&quot;");
		line.replaceAll("&", "&amp;");
		line.replaceAll("/", "&sol;");
		line.replaceAll("<", "&lt;");
		line.replaceAll(">", "&gt;");
		return line;
	}
}
