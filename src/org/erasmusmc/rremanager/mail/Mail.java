package org.erasmusmc.rremanager.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.files.UserData;
import org.erasmusmc.rremanager.gui.EmailEditor;
import org.erasmusmc.rremanager.gui.MainFrame;
import org.erasmusmc.rremanager.utilities.StringUtilities;

public class Mail {
	private RREManager rreManager = null;
	private MainFrame mainFrame = null;
	private UserData userData = null;
	private SMTPMailClient mailClient = null;
	private EmailEditor emailEditor = null;
	private Map<String, String> additionalInfo = null;

	
	public Mail(RREManager rreManager, MainFrame mainFrame, UserData userData) {
		this.rreManager = rreManager;
		this.mainFrame = mainFrame;
		this.userData = userData;
	}
	
	
	public boolean send(String messageType, List<String[]> recipientsData) {
		boolean success = false;

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
					sendFirewallRemoveRequest(recipientsData, userData);
				}
				else {
					sendOtherMail(messageType, recipientsData);
				}
			}
		}
				
		return success;
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
	
	
	private boolean sendAccountInformation(List<String[]> recipientsData) {
		boolean success = false;
		
		for (String[] recipient : recipientsData) {
			if (recipient[UserData.ACCESS].equals("FTP-Only")) { // FTP-Only user
				String messageType = "FTP-Only Account Mail";
				if (RREManager.getIniFile().hasGroup(messageType)) {
					if (!RREManager.getIniFile().hasVariable(messageType, "Text_1")) {
						mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
						JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
					}
					else {
						List<String> attachments = new ArrayList<String>();
						attachments.add(recipient[UserData.FILEZILLA_XML]);
						attachments.addAll(getAttachments(messageType));
						
						String emailText = getEmailText(messageType, recipient);
						emailEditor.editEmail(emailText, recipient[UserData.EMAIL_FORMAT], recipient, null, RREManager.getIniFile().getValue(messageType, "Subject"), false);

						String approvedSubject = "";
						String approvedEmailText = "";
						if (emailEditor.isApproved()) {
							approvedSubject = emailEditor.getApprovedSubject();
							approvedEmailText = emailEditor.getApprovedText();
						}
						
						success = sendMail(emailEditor.isApproved(), messageType, approvedSubject, approvedEmailText, recipient, null, attachments);
					}
				}
				else {
					mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
					JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				String messageType = "RDP Account Mail";
				if (RREManager.getIniFile().hasGroup(messageType)) {
					if (!RREManager.getIniFile().hasVariable(messageType, "Text_1")) {
						mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
						JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
					}
					else {
						if (!recipient[UserData.MULTIOTP_PDF].equals("")) {
							List<String> attachments = new ArrayList<String>();
							attachments.add(recipient[UserData.MULTIOTP_PDF]);
							attachments.add(recipient[UserData.FILEZILLA_XML]);
							attachments.addAll(getAttachments(messageType));
							
							String emailText = getEmailText(messageType, recipient);
							emailEditor.editEmail(emailText, recipient[UserData.EMAIL_FORMAT], recipient, null, RREManager.getIniFile().getValue(messageType, "Subject"), false);

							String approvedSubject = "";
							String approvedEmailText = "";
							if (emailEditor.isApproved()) {
								approvedSubject = emailEditor.getApprovedSubject();
								approvedEmailText = emailEditor.getApprovedText();
							}
							
							success = sendMail(emailEditor.isApproved(), messageType, approvedSubject, approvedEmailText, recipient, null, attachments);
						}
						else {
							mainFrame.logWithTimeLn("ERROR: No multiOTP pdf file found!");
							JOptionPane.showMessageDialog(mainFrame.getFrame(), "No multiOTP pdf file found for " + recipient[UserData.FIRST_NAME] + " " + recipient[UserData.LAST_NAME] + " (" + recipient[UserData.EMAIL] + ")", "No pdf", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				else {
					mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
					JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		return success;
	}
	
	
	private boolean sendPasswords(List<String[]> recipientsData) {
		boolean success = false;

		String messageType = "Password Mail";
		if (RREManager.getIniFile().hasGroup(messageType)) {
			if (!RREManager.getIniFile().hasVariable(messageType, "Text_1")) {
				mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
				JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
			}
			else {
				for (String[] recipient : recipientsData) {
					if (!recipient[UserData.PASSWORD].equals("")) {
						List<String> attachments = new ArrayList<String>();
						attachments.addAll(getAttachments(messageType));
						
						String emailText = getEmailText(messageType, recipient);
						emailEditor.editEmail(emailText, recipient[UserData.EMAIL_FORMAT], recipient, null, RREManager.getIniFile().getValue(messageType, "Subject"), false);

						String approvedSubject = "";
						String approvedEmailText = "";
						if (emailEditor.isApproved()) {
							approvedSubject = emailEditor.getApprovedSubject();
							approvedEmailText = emailEditor.getApprovedText();
						}
						
						success = sendMail(emailEditor.isApproved(), messageType, approvedSubject, approvedEmailText, recipient, null, attachments);
					}
					else {
						mainFrame.logWithTimeLn("ERROR: No password found!");
						JOptionPane.showMessageDialog(mainFrame.getFrame(), "No password found!", "No password", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		else {
			mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
			JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
		}
		
		return success;
	}
	
	
	private boolean sendFirewallAddRequest(List<String[]> recipientsData) {
		boolean success = false;

		String messageType = "Firewall Add Mail";
		if (RREManager.getIniFile().hasGroup(messageType)) {
			if (!RREManager.getIniFile().hasVariable(messageType, "Text_1")) {
				mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
				JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
			}
			else {
				String format = RREManager.getIniFile().getValue(messageType, "Format");
				if (format != null) {
					Map<String, List<String>> ipMap = new HashMap<String,List<String>>();
					for (String[] recipient : recipientsData) {
						String ipAddresses = recipient[UserData.IP_ADDRESSES];
						if ((ipAddresses != null) && (!ipAddresses.trim().equals(""))) {
							String[] ipAddressesSplit = ipAddresses.split(";");
							for (String ipAddress : ipAddressesSplit) {
								ipAddress = ipAddress.trim();
								if (!ipAddress.equals("")) {
									List<String> ipUserList = ipMap.get(ipAddress);
									if (ipUserList == null) {
										ipUserList = new ArrayList<String>();
										ipMap.put(ipAddress, ipUserList);
									}
									ipUserList.add(recipient[UserData.USER_NAME]);
								}
							}
						}
					}
					
					rreManager.getIPAddressSelector().selectIPAddresses(ipMap, null);
					ipMap = rreManager.getIPAddressSelector().getIPSelection();
					
					if ((ipMap != null) && (ipMap.keySet().size() > 0)) {
						String recipient[] = new String[UserData.OBJECT_SIZE + (ipMap.keySet().size() * 2)];
						recipient[UserData.EMAIL] = RREManager.getIniFile().getValue(messageType, "Email");
						recipient[UserData.EMAIL_FORMAT] = format;
						int ipNr = 0;
						String ipAddressString = "";
						for (String ipAddress : ipMap.keySet()) {
							String users = "";
							for (String user : ipMap.get(ipAddress)) {
								users += (users.equals("") ? "" : ", ") + user;
							}
							recipient[UserData.OBJECT_SIZE + (ipNr * 2)] = ipAddress;
							recipient[UserData.OBJECT_SIZE + (ipNr * 2) + 1] = users;
							ipAddressString += (ipAddressString.equals("") ? "" : ";") + ipAddress + " (" + users + ")";
							ipNr++;
						}
						recipient[UserData.IP_ADDRESSES] = ipAddressString;
						
						List<String> attachments = new ArrayList<String>();
						attachments.addAll(getAttachments(messageType));
						
						String emailText = getEmailText(messageType, recipient); 
						emailEditor.setAdditionalInfo(additionalInfo);
						emailEditor.editEmail(emailText, format, recipient, null, RREManager.getIniFile().getValue(messageType, "Subject"), true);
						additionalInfo = emailEditor.getAdditionalInfo();

						String approvedSubject = "";
						String approvedEmailText = "";
						if (emailEditor.isApproved()) {
							approvedSubject = emailEditor.getApprovedSubject();
							approvedEmailText = emailEditor.getApprovedText();
						}
						
						success = sendMail(emailEditor.isApproved(), messageType, approvedSubject, approvedEmailText, recipient, null, attachments);
					}
				}
				else {
					mainFrame.logWithTimeLn("ERROR: No message format specified for " + messageType);
					JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message format specified for " + messageType, "No message format", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else {
			mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
			JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
		}
		
		return success;
	}
	
	
	private boolean sendFirewallRemoveRequest(List<String[]> recipientsData, UserData userData) {
		boolean success = false;

		String messageType = "Firewall Remove Mail";
		if (RREManager.getIniFile().hasGroup(messageType)) {
			if (!RREManager.getIniFile().hasVariable(messageType, "Text_1")) {
				mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
				JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
			}
			else {
				String format = RREManager.getIniFile().getValue(messageType, "Format");
				if (format != null) {
					Map<String, Set<String>> ipUsersMap = userData.getIPAddressUsersMap();
					Set<String> selectSet = new HashSet<String>();
					Map<String, List<String>> ipMap = new HashMap<String, List<String>>();
					for (String[] recipient : recipientsData) {
						String userDescription = recipient[UserData.USER_NAME];
						
						for (String ipAddress : ipUsersMap.keySet()) {
							if (ipUsersMap.containsKey(ipAddress)) {
								ipUsersMap.get(ipAddress).remove(userDescription);
							}
						}

						String ipAddresses = recipient[UserData.IP_ADDRESSES];
						if ((ipAddresses != null) && (!ipAddresses.trim().equals(""))) {
							String[] ipAddressesSplit = ipAddresses.split(";");
							for (String ipAddress : ipAddressesSplit) {
								ipAddress = ipAddress.trim();
								if (!ipAddress.equals("")) {
									List<String> ipUsers = ipMap.get(ipAddress);
									if (ipUsers == null) {
										ipUsers = new ArrayList<String>();
										ipMap.put(ipAddress, ipUsers);
									}
									if (!ipUsers.contains(userDescription)) {
										ipUsers.add(userDescription);
									}
									
									for (String otherUser : ipUsersMap.get(ipAddress)) {
										if (!ipUsers.contains(otherUser)) {
											ipUsers.add(otherUser);
										}
									}
									
									if (ipUsersMap.get(ipAddress).size() == 0) {
										selectSet.add(ipAddress);
									}
								}
							}
						}
					}
					
					rreManager.getIPAddressSelector().selectIPAddresses(ipMap, selectSet);
					ipMap = rreManager.getIPAddressSelector().getIPSelection();
					
					if ((ipMap != null) && (ipMap.keySet().size() > 0)) {
						String recipient[] = new String[UserData.OBJECT_SIZE + (ipMap.keySet().size() * 2)];
						recipient[UserData.EMAIL] = RREManager.getIniFile().getValue(messageType, "Email");
						recipient[UserData.EMAIL_FORMAT] = format;
						
						int ipNr = 0;
						String ipAddressString = "";
						for (String ipAddress : ipMap.keySet()) {
							String users = "";
							for (String user : ipMap.get(ipAddress)) {
								users += (users.equals("") ? "" : ", ") + user;
							}
							recipient[UserData.OBJECT_SIZE + (ipNr * 2)] = ipAddress;
							recipient[UserData.OBJECT_SIZE + (ipNr * 2) + 1] = users;
							ipAddressString += (ipAddressString.equals("") ? "" : ";") + ipAddress + " (" + users + ")";
							ipNr++;
						}
						recipient[UserData.IP_ADDRESSES] = ipAddressString;
						
						List<String> attachments = new ArrayList<String>();
						attachments.addAll(getAttachments(messageType));
						
						String emailText = getEmailText(messageType, recipient); 
						emailEditor.setAdditionalInfo(additionalInfo);
						emailEditor.editEmail(emailText, format, recipient, null, RREManager.getIniFile().getValue(messageType, "Subject"), true);
						additionalInfo = emailEditor.getAdditionalInfo();

						String approvedSubject = "";
						String approvedEmailText = "";
						if (emailEditor.isApproved()) {
							approvedSubject = emailEditor.getApprovedSubject();
							approvedEmailText = emailEditor.getApprovedText();
						}
						
						success = sendMail(emailEditor.isApproved(), messageType, approvedSubject, approvedEmailText, recipient, null, attachments);
					}
					
				}
				else {
					mainFrame.logWithTimeLn("ERROR: No message format specified for " + messageType);
					JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message format specified for " + messageType, "No message format", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else {
			mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
			JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
		}
		
		return success;
	}
	
	
	private boolean sendOtherMail(String messageType, List<String[]> recipientsData) {
		boolean success = false;
		
		if (RREManager.getIniFile().getValue(messageType, "Type").equals("MULTIPLE")) {
			Map<String, List<String[]>> bccRecipientsData = new HashMap<String, List<String[]>>();
			for (String recipient[] : recipientsData) {
				List<String[]> formatBCCRecipientsData = bccRecipientsData.get(recipient[UserData.EMAIL_FORMAT]);
				if (formatBCCRecipientsData == null) {
					formatBCCRecipientsData = new ArrayList<String[]>();
					bccRecipientsData.put(recipient[UserData.EMAIL_FORMAT], formatBCCRecipientsData);
				}
				formatBCCRecipientsData.add(recipient);
			}
			
			// Mail from sender to sender. Real recipients in BCC.
			String[] recipient = new String[UserData.OBJECT_SIZE];
			recipient[UserData.EMAIL] = RREManager.getIniFile().getValue("SMTP Mail Server", "From");
			
			additionalInfo = new HashMap<String, String>();
			for (String format : bccRecipientsData.keySet()) {
				recipient[UserData.EMAIL_FORMAT] = format;
				List<String> attachments = getAttachments(messageType);
				
				String emailText = getEmailText(messageType, recipient); 
				emailEditor.setAdditionalInfo(additionalInfo);
				emailEditor.editEmail(emailText, format, recipient, bccRecipientsData.get(format), RREManager.getIniFile().getValue(messageType, "Subject"), true);
				additionalInfo = emailEditor.getAdditionalInfo();

				String approvedSubject = "";
				String approvedEmailText = "";
				if (emailEditor.isApproved()) {
					approvedSubject = emailEditor.getApprovedSubject();
					approvedEmailText = emailEditor.getApprovedText();
				}
				
				success = sendMail(emailEditor.isApproved(), messageType, approvedSubject, approvedEmailText, recipient, bccRecipientsData.get(format), attachments);
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

				String approvedSubject = "";
				String approvedEmailText = "";
				if (emailEditor.isApproved()) {
					approvedSubject = emailEditor.getApprovedSubject();
					approvedEmailText = emailEditor.getApprovedText();
				}

				success = sendMail(emailEditor.isApproved(), messageType, approvedSubject, approvedEmailText, recipient, null, attachments);
			}
		}
		
		return success;
	}
	
	
	private boolean sendMail(boolean approved, String messageType, String messageSubject, String messageText, String[] recipient, List<String[]> bccRecipientsData, List<String> attachments) {
		boolean success = false;

		mainFrame.logLn("");
		mainFrame.logWithTimeLn("Send " + messageType + " to " + UserData.getUserDescription(recipient, true) + " as " + recipient[UserData.EMAIL_FORMAT] + " ...");
		
		// Format BCC recipients
		List<String> bccRecipients = new ArrayList<String>();
		String bccRecipientsString = "";
		if (bccRecipientsData != null) {
			for (int bccNr = 0; bccNr < bccRecipientsData.size(); bccNr++) {
				String[] bccRecipient = bccRecipientsData.get(bccNr);
				bccRecipients.add(bccRecipient[UserData.EMAIL]);
				bccRecipientsString += (bccRecipientsString.equals("") ? "" : "; ") + bccRecipient[UserData.EMAIL];
				mainFrame.logWithTimeLn("  BCC " + Integer.toString(bccNr + 1) + ": " + UserData.getUserDescription(bccRecipient, true));
			}
		}

		// Format attachments
		String attachementsString = "";
		String unreadableAttachments = "";
		if (attachments != null) {
			for (int attachmentNr = 0; attachmentNr < attachments.size(); attachmentNr++) {
				attachementsString += (attachementsString.equals("") ? "" : ",") + attachments.get(attachmentNr);
				File attachementFile = new File(attachments.get(attachmentNr));
				boolean unreadable = false;
				if (!attachementFile.canRead()) {
					unreadableAttachments += (unreadableAttachments.equals("") ? "Unreadable attachments: " : " ") + Integer.toString(attachmentNr + 1);
					unreadable = true;
				}
				mainFrame.logWithTimeLn("  Attachment " + Integer.toString(attachmentNr + 1) + ": " + attachments.get(attachmentNr) + (unreadable ? " UNREADABLE" : ""));
			}
		}
		mainFrame.logWithTimeLn("  Subject: " + messageSubject);
		mainFrame.logWithTimeLn("  Text:");
		mainFrame.logLn(messageText);

		String allTimeLogRecord = "Send " + messageType;
		allTimeLogRecord += "," + "\"" + recipient[UserData.EMAIL] + "\"";
		allTimeLogRecord += "," + (((recipient[UserData.USER_NAME]    != null) && (!recipient[UserData.USER_NAME].equals("")))    ? ("\"" + recipient[UserData.USER_NAME]    + "\"") : "");
		allTimeLogRecord += "," + (((recipient[UserData.FIRST_NAME]   != null) && (!recipient[UserData.FIRST_NAME].equals("")))   ? ("\"" + recipient[UserData.FIRST_NAME]   + "\"") : "");
		allTimeLogRecord += "," + (((recipient[UserData.LAST_NAME]    != null) && (!recipient[UserData.LAST_NAME].equals("")))    ? ("\"" + recipient[UserData.LAST_NAME]    + "\"") : "");
		allTimeLogRecord += "," + "\"" + bccRecipientsString + "\"";
		allTimeLogRecord += "," + (((recipient[UserData.PASSWORD]     != null) && (!recipient[UserData.PASSWORD].equals("")))     ? ("\"" + recipient[UserData.PASSWORD]     + "\"") : "");
		allTimeLogRecord += "," + (((recipient[UserData.IP_ADDRESSES] != null) && (!recipient[UserData.IP_ADDRESSES].equals(""))) ? ("\"" + recipient[UserData.IP_ADDRESSES] + "\"") : "");
		allTimeLogRecord += "," + (approved ? "Yes" : "No");
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		String info = attachementsString;

		if (approved) {
			if (unreadableAttachments.equals("")) {
				String html = messageText;
				String text = null;
				if (!recipient[UserData.EMAIL_FORMAT].equals("HTML")) {
					html = null;
					text = messageText;
				}

				List<String> recipients = new ArrayList<String>();
				recipients.add(recipient[UserData.EMAIL]);
				
				String sender = RREManager.getIniFile().getValue("SMTP Mail Server", "From");
				List<String> ccRecipients = new ArrayList<String>();
				ccRecipients.add(sender);
				
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
		}
		else {
			allTimeLogRecord += "," + "Failed";
			allTimeLogRecord += "," + "Rejected by user";
		}
		
		mainFrame.allTimeLog(allTimeLogRecord, info);
		
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
						String ipAddress = recipient[UserData.OBJECT_SIZE + (ipNr * 2)];
						String users = recipient[UserData.OBJECT_SIZE + (ipNr * 2) + 1];
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
						line = StringUtilities.replaceAllTags(line, "[ADMINISTRATOR TITLE]", title);
					}
					line = StringUtilities.replaceAllTags(line, "[BOLD START]", recipient[UserData.EMAIL_FORMAT].equals("HTML") ? "<b>" : "");
					line = StringUtilities.replaceAllTags(line, "[BOLD END]", recipient[UserData.EMAIL_FORMAT].equals("HTML") ? "</b>" : "");
					line = StringUtilities.replaceAllTags(line, "[ITALIC START]", recipient[UserData.EMAIL_FORMAT].equals("HTML") ? "<i>" : "");
					line = StringUtilities.replaceAllTags(line, "[ITALIC END]", recipient[UserData.EMAIL_FORMAT].equals("HTML") ? "</i>" : "");
					line = StringUtilities.replaceAllTags(line, "[FIRST NAME]", recipient[UserData.FIRST_NAME]);
					line = StringUtilities.replaceAllTags(line, "[LAST NAME]", recipient[UserData.LAST_NAME]);
					line = StringUtilities.replaceAllTags(line, "[USER NAME]", recipient[UserData.USER_NAME]);
					line = StringUtilities.replaceAllTags(line, "[PASSWORD]", recipient[UserData.PASSWORD]);
					line = StringUtilities.replaceAllTags(line, "[ADMINISTRATOR]", rreManager.getAdministrator());
					line = StringUtilities.replaceSpecialCharacters(line);
					email += indent + line + (recipient[UserData.EMAIL_FORMAT].equals("HTML") ? "<br>" : "") + "\r\n";
				}
			}
			lineNr++;
		} while (line != null);
		return email;
	}
}
