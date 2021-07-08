package org.erasmusmc.rremanager;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.erasmusmc.rremanager.files.IniFile;
import org.erasmusmc.rremanager.files.UserData;
import org.erasmusmc.rremanager.gui.EmailReviewer;
import org.erasmusmc.rremanager.gui.IPAddressSelector;
import org.erasmusmc.rremanager.gui.MainFrame;
import org.erasmusmc.rremanager.gui.PasswordManager;
import org.erasmusmc.rremanager.smtp.SMTPMailClient;
import org.erasmusmc.rremanager.utilities.DateUtilities;

public class RREManager {
	public static boolean inEclipse = false;
	public static String version = "1.3";
	public static boolean noLogging = true;
	public static boolean loggingStarted = false;
	
	private static Set<JComponent> componentsToDisableWhenRunning = new HashSet<JComponent>();
	
	private static String currentPath = null;
	private static IniFile iniFile;
	
	private PasswordManager passwordManager;
	private IPAddressSelector ipAddressSelector;
	private EmailReviewer emailReviewer;
	private SMTPMailClient mailClient = null;
	private MainFrame mainFrame = null;
	private UserData userData = null;
	
	
	public static IniFile getIniFile() {
		return iniFile;
	}
	
	
	public static void disableWhenRunning(JComponent component) {
		componentsToDisableWhenRunning.add(component);
	}
	
	
	public static void disableComponents() {
		for (JComponent component : componentsToDisableWhenRunning) {
			component.setEnabled(false);
		}
	}
	
	
	public static void enableComponents() {
		for (JComponent component : componentsToDisableWhenRunning) {
			component.setEnabled(true);
		}
	}
	
	
	public RREManager(Map<String, String> parameters) {
		noLogging = parameters.keySet().contains("nologging");
		inEclipse = ((System.getProperty("runInEclipse") != null) && System.getProperty("runInEclipse").equalsIgnoreCase("true"));
		if (setCurrentPath()) {
			iniFile = new IniFile(parameters.keySet().contains("settings") ? parameters.get("settings") : (currentPath + File.separator + "RREManager-v" + version + ".ini"));
			if (iniFile.readFile()) {
				userData = new UserData(getIniFile().getValue("User Projects File","File"));
				mainFrame = new MainFrame(this, userData.getUsersList());
				passwordManager = new PasswordManager(mainFrame.getFrame());
				ipAddressSelector = new IPAddressSelector(mainFrame.getFrame());
				emailReviewer = new EmailReviewer(mainFrame.getFrame());
			}
			else {
				JOptionPane.showMessageDialog(null, iniFile.getError(), "RREManager Error", JOptionPane.ERROR_MESSAGE);
			}			
		}
	}
	
	
	public boolean setCurrentPath() {
		boolean result = false;
		try {
			currentPath = (new File(RREManager.class.getProtectionDomain().getCodeSource().getLocation().toURI())).getParent();
			result = true;
		} catch (URISyntaxException e) {
			JOptionPane.showMessageDialog(null, "Cannot determine location of RREManager.ini file.", "RREManager Error", JOptionPane.ERROR_MESSAGE);
		}
		return result;
	}
	
	
	public static String getCurentPath() {
		return currentPath;
	}
	
	
	public void sendAccountInformation(int[] selectedUsers) {
		if (getMailClient()) {
			for (int userNr : selectedUsers) {
				String[] user = userData.getUser(userNr);
				if (user != null) {
					mainFrame.logLn("");
					String userAccountName = user[UserData.INITIALS].length() > 0 ? user[UserData.INITIALS].substring(0, 1) : "";
					userAccountName += user[UserData.LAST_NAME];
					userAccountName = userAccountName.toLowerCase();
					if (user[UserData.ACCESS].equals("FTP-Only")) { // FTP-Only user
						String messageType = "FTP-Only Account Mail";
						if (getIniFile().hasGroup(messageType)) {							
							List<String> attachments = new ArrayList<String>();
							attachments.addAll(getAdditionalAttachments(messageType));

							String allTimeLogRecord = "Send FTP-Only Account";
							allTimeLogRecord += "," + user[UserData.EMAIL];
							allTimeLogRecord += "," + user[UserData.USER_NAME];
							allTimeLogRecord += "," + user[UserData.FIRST_NAME];
							allTimeLogRecord += "," + user[UserData.LAST_NAME];
							allTimeLogRecord += ",";
							allTimeLogRecord += ",";
							
							mail(messageType, user, attachments, allTimeLogRecord);
						}
						else {
							mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
							JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
						}
					}
					else {
						String messageType = "RDP Account Mail";
						if (getIniFile().hasGroup(messageType)) {
							if (!user[UserData.MULTIOTP_PDF].equals("")) {
								List<String> attachments = new ArrayList<String>();
								attachments.add(user[UserData.MULTIOTP_PDF]);
								attachments.addAll(getAdditionalAttachments(messageType));
								
								String allTimeLogRecord = "Send RDP Account";
								allTimeLogRecord += "," + user[UserData.EMAIL];
								allTimeLogRecord += "," + user[UserData.USER_NAME];
								allTimeLogRecord += "," + user[UserData.FIRST_NAME];
								allTimeLogRecord += "," + user[UserData.LAST_NAME];
								allTimeLogRecord += ",";
								allTimeLogRecord += ",";
								
								mail(messageType, user, attachments, allTimeLogRecord);
							}
							else {
								mainFrame.logWithTimeLn("ERROR: No multiOTP pdf file found!");
								JOptionPane.showMessageDialog(mainFrame.getFrame(), "No multiOTP pdf file found for " + user[UserData.FIRST_NAME] + " " + user[UserData.LAST_NAME] + " (" + user[UserData.EMAIL] + ")", "No pdf", JOptionPane.ERROR_MESSAGE);
							}
						}
						else {
							mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
							JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		}
	}
	
	
	public void sendPasswords(int[] selectedUsers) {
		String messageType = "Password Mail";
		if (getIniFile().hasGroup(messageType)) {
			if (!getIniFile().hasVariable(messageType, "Text_1")) {
				mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
				JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
			}
			else {
				if (getMailClient()) {
					for (int userNr : selectedUsers) {
						String[] user = userData.getUser(userNr);
						if (user != null) {
							String userAccountName = user[UserData.INITIALS].length() > 0 ? user[UserData.INITIALS].substring(0, 1) : "";
							userAccountName += user[UserData.LAST_NAME];
							userAccountName = userAccountName.toLowerCase();
							if (!user[UserData.PASSWORD].equals("")) {
								List<String> attachments = new ArrayList<String>();
								attachments.addAll(getAdditionalAttachments(messageType));
								
								String allTimeLogRecord = "Send Password";
								allTimeLogRecord += "," + user[UserData.EMAIL];
								allTimeLogRecord += "," + user[UserData.USER_NAME];
								allTimeLogRecord += "," + user[UserData.FIRST_NAME];
								allTimeLogRecord += "," + user[UserData.LAST_NAME];
								allTimeLogRecord += "," + user[UserData.PASSWORD];
								allTimeLogRecord += ",";
								
								mail(messageType, user, attachments, allTimeLogRecord);
							}
							else {
								mainFrame.logWithTimeLn("ERROR: No password found!");
								JOptionPane.showMessageDialog(mainFrame.getFrame(), "No password found!", "No password", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		}
		else {
			mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
			JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	public void sendFirewallAddRequest(int[] selectedUsers) {
		String messageType = "Firewall Add Mail";
		if (getIniFile().hasGroup(messageType)) {
			if (!getIniFile().hasVariable(messageType, "Text_1")) {
				mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
				JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
			}
			else {
				String format = getIniFile().getValue(messageType, "Format");
				if (format != null) {
					Map<String, List<String>> ipMap = new HashMap<String,List<String>>();
					for (int userNr : selectedUsers) {
						String[] user = userData.getUser(userNr);
						String userName = user[UserData.USER_NAME];
						if (user != null) {
							String ipAddresses = user[UserData.IP_ADDRESSES];
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
										ipUserList.add(userName);
									}
								}
							}
						}
					}
					
					ipAddressSelector.selectIPAddresses(ipMap, null);
					ipMap = ipAddressSelector.getIPSelection();
					
					if ((ipMap != null) && (ipMap.keySet().size() > 0)) {
						if (getMailClient()) {
							String info[] = new String[UserData.OBJECT_SIZE + (ipMap.keySet().size() * 2)];
							info[UserData.EMAIL] = getIniFile().getValue(messageType, "Email");
							info[UserData.EMAIL_FORMAT] = format;
							int ipNr = 0;
							String ipAddressString = "";
							for (String ipAddress : ipMap.keySet()) {
								String users = "";
								for (String user : ipMap.get(ipAddress)) {
									users += (users.equals("") ? "" : ", ") + user;
								}
								info[UserData.OBJECT_SIZE + (ipNr * 2)] = ipAddress;
								info[UserData.OBJECT_SIZE + (ipNr * 2) + 1] = users;
								ipAddressString += (ipAddressString.equals("") ? "" : ";") + ipAddress + " (" + users + ")";
								ipNr++;
							}
							info[UserData.IP_ADDRESSES] = ipAddressString;
							
							List<String> attachments = new ArrayList<String>();
							attachments.addAll(getAdditionalAttachments(messageType));
							
							String allTimeLogRecord = "Send Firewall Add Request";
							allTimeLogRecord += "," + getIniFile().getValue(messageType, "Email");
							allTimeLogRecord += ",";
							allTimeLogRecord += ",";
							allTimeLogRecord += ",";
							allTimeLogRecord += ",";
							allTimeLogRecord += "," + "\"" + ipAddressString + "\"";
							
							mail(messageType, info, attachments, allTimeLogRecord);
						}
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
	}
	
	
	public void sendFirewallRemoveRequest(int[] selectedUsers) {
		String messageType = "Firewall Remove Mail";
		if (getIniFile().hasGroup(messageType)) {
			if (!getIniFile().hasVariable(messageType, "Text_1")) {
				mainFrame.logWithTimeLn("ERROR: No message definition for " + messageType);
				JOptionPane.showMessageDialog(mainFrame.getFrame(), "No message definition for " + messageType, "No message", JOptionPane.ERROR_MESSAGE);
			}
			else {
				String format = getIniFile().getValue(messageType, "Format");
				if (format != null) {
					Map<String, Set<String>> ipUsersMap = userData.getIPAddressUsersMap();
					Set<String> selectSet = new HashSet<String>();
					Map<String, List<String>> ipMap = new HashMap<String, List<String>>();
					for (int userNr : selectedUsers) {
						String[] user = userData.getUser(userNr);
						
						if (user != null) {
							String userDescription = user[UserData.USER_NAME];
							
							for (String ipAddress : ipUsersMap.keySet()) {
								if (ipUsersMap.containsKey(ipAddress)) {
									ipUsersMap.get(ipAddress).remove(userDescription);
								}
							}

							String ipAddresses = user[UserData.IP_ADDRESSES];
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
					}
					
					ipAddressSelector.selectIPAddresses(ipMap, selectSet);
					ipMap = ipAddressSelector.getIPSelection();
					
					if ((ipMap != null) && (ipMap.keySet().size() > 0)) {
						if (getMailClient()) {
							String info[] = new String[UserData.OBJECT_SIZE + (ipMap.keySet().size() * 2)];
							info[UserData.EMAIL] = getIniFile().getValue(messageType, "Email");
							info[UserData.EMAIL_FORMAT] = format;
							
							int ipNr = 0;
							String ipAddressString = "";
							for (String ipAddress : ipMap.keySet()) {
								String users = "";
								for (String user : ipMap.get(ipAddress)) {
									users += (users.equals("") ? "" : ", ") + user;
								}
								info[UserData.OBJECT_SIZE + (ipNr * 2)] = ipAddress;
								info[UserData.OBJECT_SIZE + (ipNr * 2) + 1] = users;
								ipAddressString += (ipAddressString.equals("") ? "" : ";") + ipAddress + " (" + users + ")";
								ipNr++;
							}
							info[UserData.IP_ADDRESSES] = ipAddressString;
							
							List<String> attachments = new ArrayList<String>();
							attachments.addAll(getAdditionalAttachments(messageType));
							
							String allTimeLogRecord = "Send Firewall Remove Request";
							allTimeLogRecord += "," + getIniFile().getValue(messageType, "Email");
							allTimeLogRecord += ",";
							allTimeLogRecord += ",";
							allTimeLogRecord += ",";
							allTimeLogRecord += ",";
							allTimeLogRecord += "," + "\"" + ipAddressString + "\"";
							
							mail(messageType, info, attachments, allTimeLogRecord);
						}
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
	}
	
	
	private boolean getMailClient() {
		boolean mailClientSet = false;
		mailClient = null;
		String password = passwordManager.getPassword(getIniFile().getValue("SMTP Mail Server","User"));
		if (password != null) {
			mailClient = new SMTPMailClient(
					getIniFile().getValue("SMTP Mail Server","Server"), 
					getIniFile().getValue("SMTP Mail Server","Port"), 
					getIniFile().getValue("SMTP Mail Server","User"), 
					password
					);
			mailClientSet = true;
		}
		return mailClientSet;
	}
	
	
	private void mail(String messageType, String[] info, List<String> attachments, String allTimeLogRecord) {
		String recipientDescription = info[UserData.FIRST_NAME] == null ? "" : info[UserData.FIRST_NAME];
		recipientDescription += (recipientDescription.equals("") ? "" : (((info[UserData.LAST_NAME] == null) || info[UserData.LAST_NAME].equals("")) ? "" : " ")) + (info[UserData.LAST_NAME] == null ? "" : info[UserData.LAST_NAME]);
		recipientDescription += (recipientDescription.equals("") ? info[UserData.EMAIL] : " (" + info[UserData.EMAIL] + ")");

		allTimeLogRecord = DateUtilities.getCurrentTime().replaceAll(" ", ",") + "," + allTimeLogRecord;
		mainFrame.logLn("");
		mainFrame.logWithTimeLn("Send " + messageType + " to " + recipientDescription + " as " + info[UserData.EMAIL_FORMAT] + " ...");

		if (messageType.startsWith("Firewall") && (!info[UserData.IP_ADDRESSES].equals(""))) {
			int ipNr = 0;
			while ((ipNr * 2) < (info.length - UserData.OBJECT_SIZE)) {
				String ipAddress = info[UserData.OBJECT_SIZE + (ipNr * 2)];
				String users = info[UserData.OBJECT_SIZE + (ipNr * 2) + 1];
				mainFrame.logWithTimeLn((ipNr == 0 ? "    IP-Addresses: " : "                  ") + ipAddress + " (" + users + ")");
				ipNr++;
			}
		}

		String attachementsString = "";
		String unreadableAttachments = "";
		for (int attachentNr = 0; attachentNr < attachments.size(); attachentNr++) {
			attachementsString += (attachementsString.equals("") ? "" : ",") + attachments.get(attachentNr);
			File attachementFile = new File(attachments.get(attachentNr));
			boolean unreadable = false;
			if (!attachementFile.canRead()) {
				unreadableAttachments += (unreadableAttachments.equals("") ? "" : " ") + Integer.toString(attachentNr + 1);
				unreadable = true;
			}
			mainFrame.logWithTimeLn((attachentNr == 0 ? "    Attachments: " : "                 ") + attachments.get(attachentNr) + (unreadable ? " UNREADABLE" : ""));
		}

		String mailError = "";
		if (unreadableAttachments.equals("")) {
			String text = approveEmail(getEmailText(messageType, info), info, getIniFile().getValue(messageType, "Subject"));
			
			if (text != null) {
				List<String> recipients = new ArrayList<String>();
				recipients.add(info[UserData.EMAIL]);
				List<String> ccRecipients = new ArrayList<String>();
				ccRecipients.add(getIniFile().getValue("SMTP Mail Server","cc"));
			
				if (mailClient.sendMail(
						getIniFile().getValue(messageType, "Subject"), 
						info[UserData.EMAIL_FORMAT].equals("HTML") ? text : null,
						info[UserData.EMAIL_FORMAT].equals("TEXT") ? text : null, 
						getIniFile().getValue("SMTP Mail Server","from"), 
						recipients, 
						ccRecipients, 
						null, 
						attachments
						)) {
					allTimeLogRecord += "," + "Yes";
					allTimeLogRecord += "," + "Succeeded";
					mainFrame.logWithTimeLn("  SUCCEEDED");
				}
				else {
					allTimeLogRecord += "," + "Yes";
					allTimeLogRecord += "," + "Failed";
					mailError = mailClient.getError().replaceAll("\r\n", " ").replaceAll("\r", " ").replaceAll("\n", " ");
					mainFrame.logWithTimeLn("  FAILED: " + mailError);
				}
			}
			else {
				allTimeLogRecord += "," + "No";
				allTimeLogRecord += "," + "Failed";
				mailError = "Rejected by user.";
				mainFrame.logWithTimeLn("  REJECTED by user.");
			}
		}
		else {
			allTimeLogRecord += "," + "";
			allTimeLogRecord += "," + "Failed";
			mailError = "Cannot read attachments " + unreadableAttachments;
			mainFrame.logWithTimeLn("  REJECTED: Cannot read attachments " + unreadableAttachments);
			JOptionPane.showMessageDialog(mainFrame.getFrame(), "  REJECTED: Cannot read attachments " + unreadableAttachments, "Unreadble attachments", JOptionPane.ERROR_MESSAGE);
		}

		allTimeLogRecord += "," + "\"" + mailError + "\"";
		allTimeLogRecord += "," + mainFrame.getLogFileName();
		allTimeLogRecord += "," + "\"" + attachementsString + "\"";
		
		mainFrame.allTimeLog(allTimeLogRecord);
	}
	
	
	private String getEmailText(String textID, String[] info) {
		String email = "";
		if (info[UserData.EMAIL_FORMAT].equals("HTML")) {
			email += "<html>\r\n";
			email += "  <body>\r\n";
			email += "    <p style=\"font-family: arial; font-size: 11pt; font-weight: normal; color: #000000;\">\r\n";
			email += getEmailText(textID, info, "      ");
			email += "    </p>\r\n";
			email += "  </body>\r\n";
			email += "</html>\r\n";
		}
		else {
			email = getEmailText(textID, info, "") + "\r\n";
		}
		return email;
	}
	
	
	private String getEmailText(String textID, String[] info, String indent) {
		String email = "";
		Integer lineNr = 1;
		String line = null;
		do { 
			line = getIniFile().getValue(textID, "Text_" + lineNr);
			if (line != null) {
				if (line.startsWith("[Picture_") && line.endsWith("]")) {
					if (info[UserData.EMAIL_FORMAT].equals("HTML")) {
						email += indent + "<br>\r\n";
						email += indent + getIniFile().getValue(textID, line.substring(1, line.length() - 1)) + "<br>\r\n";
					}
				}
				else if (line.equals("[IP ADDRESSES]")) {
					if (info[UserData.EMAIL_FORMAT].equals("HTML")) {
						email += indent + "<table>" + "\r\n";
						email += indent + "  <tr>\r\n";
						email += indent + "    <td>&nbsp;&nbsp;&nbsp;</td>\r\n";
						email += indent + "    <td><b>IP-Address</b></td>\r\n";
						email += indent + "    <td>&nbsp;&nbsp;&nbsp;</td>\r\n";
						email += indent + "    <td><b>User(s)</b></td>\r\n";
						email += indent + "  </tr>\r\n";
					}
					int ipNr = 0;
					while ((UserData.OBJECT_SIZE + (ipNr * 2) + 1) < info.length) {
						String ipAddress = info[UserData.OBJECT_SIZE + (ipNr *2)];
						String users = info[UserData.OBJECT_SIZE + (ipNr *2) + 1];
						if (info[UserData.EMAIL_FORMAT].equals("HTML")) {
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
					if (info[UserData.EMAIL_FORMAT].equals("HTML")) {
						email += indent + "</table>" + "\r\n";
					}
				}
				else if (line.startsWith("[") && line.endsWith("]") && getIniFile().hasGroup(line.substring(1, line.length() - 1))) {
					email += getEmailText(line.substring(1, line.length() - 1), info, indent);
				}
				else {
					line = replaceAllTags(line, "[BOLD START]", info[UserData.EMAIL_FORMAT].equals("HTML") ? "<b>" : "");
					line = replaceAllTags(line, "[BOLD END]", info[UserData.EMAIL_FORMAT].equals("HTML") ? "</b>" : "");
					line = replaceAllTags(line, "[ITALIC START]", info[UserData.EMAIL_FORMAT].equals("HTML") ? "<i>" : "");
					line = replaceAllTags(line, "[ITALIC END]", info[UserData.EMAIL_FORMAT].equals("HTML") ? "</i>" : "");
					line = replaceAllTags(line, "[FIRST NAME]", info[UserData.FIRST_NAME]);
					line = replaceAllTags(line, "[LAST NAME]", info[UserData.LAST_NAME]);
					line = replaceAllTags(line, "[USER NAME]", info[UserData.USER_NAME]);
					line = replaceAllTags(line, "[PASSWORD]", info[UserData.PASSWORD]);
					line = replaceSpecialCharacters(line);
					email += indent + line + (info[UserData.EMAIL_FORMAT].equals("HTML") ? "<br>" : "") + "\r\n";
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
	
	
	private String approveEmail(String emailText, String[] user, String subject) {
		emailReviewer.reviewEmail(emailText, user[UserData.EMAIL_FORMAT], user, subject);
		if (!emailReviewer.isApproved()) {
			emailText = null;
			mailClient.setError("Rejected by user.");
		}
		return emailText;
	}
	
	
	private List<String> getAdditionalAttachments(String messageType) {
		List<String> attachments = new ArrayList<String>();
		if (getIniFile().hasGroup(messageType)) {
			Integer attachmentNr = 1;
			String attachment = null;
			do { 
				attachment = getIniFile().getValue(messageType, "Attachment_" + attachmentNr);
				if (attachment != null) {
					attachments.add(attachment);
				}
				attachmentNr++;
			} while (attachment != null);
		}
		return attachments;
	}
	

	public static void main(String[] args) {
		Map<String, String> parameters = new HashMap<String, String>();

		for (int i = 0; i < args.length; i++) {
			int equalSignIndex = args[i].indexOf("=");
			String argVariable = args[i].toLowerCase();
			String value = "";
			if (equalSignIndex != -1) {
				argVariable = args[i].substring(0, equalSignIndex).toLowerCase();
				value = args[i].substring(equalSignIndex + 1);
			}
			parameters.put(argVariable, value);
		}
		
		new RREManager(parameters);
	}

}
