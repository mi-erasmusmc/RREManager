package org.erasmusmc.rremanager.files;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.changelog.AddUserLogEntry;
import org.erasmusmc.rremanager.changelog.DeleteUserLogEntry;
import org.erasmusmc.rremanager.changelog.ModifyUserLogEntry;
import org.erasmusmc.rremanager.changelog.NonScriptedChangeLogEntry;
import org.erasmusmc.rremanager.gui.MainFrame;

public class UserData {
	public static int FIRST_NAME    =  0;
	public static int INITIALS      =  1;
	public static int LAST_NAME     =  2;
	public static int USER_NAME     =  3;
	public static int ACCESS        =  4;
	public static int PASSWORD      =  5;
	public static int EMAIL         =  6;
	public static int EMAIL_FORMAT  =  7;
	public static int MULTIOTP      =  8;
	public static int PROJECTS      =  9;
	public static int GROUPS        = 10;
	public static int IP_ADDRESSES  = 11;
	public static int MULTIOTP_PDF  = 12;
	public static int FILEZILLA_XML = 13;
	public static int OBJECT_SIZE   = 14;
	
	public static String[] fieldName = new String[] {
			"First name",
			"Iniitials",
			"Last name",
			"User name",
			"Access type",
			"Password",
			"Email address",
			"Email format",
			"QR-Code available",
			"Projects",
			"Project groups",
			"IP Adresses",
			"MultiOTP PDF",
			"FileZilla XML"
	};
	

	private MainFrame mainFrame = null;
	private String settingsGroup = null;
	private List<String[]> users = new ArrayList<String[]>();
	
	
	public static String getUserDescription(String[] user, boolean withEmail) {
		String userDescription = user[UserData.FIRST_NAME] == null ? "" : user[UserData.FIRST_NAME];
		userDescription += (userDescription.equals("") ? "" : (((user[UserData.LAST_NAME] == null) || user[UserData.LAST_NAME].equals("")) ? "" : " ")) + (user[UserData.LAST_NAME] == null ? "" : user[UserData.LAST_NAME]);
		if (withEmail) {
			userDescription += (userDescription.equals("") ? user[UserData.EMAIL] : " (" + user[UserData.EMAIL] + ")");
		}
		return userDescription;
	}
	

	public UserData(MainFrame mainFrame, String settingsGroup) {
		this.mainFrame = mainFrame;
		this.settingsGroup = settingsGroup;
		getData();
	}
	
	
	public List<String[]> getUsersList() {
		return users;
	}
	
	
	public String[] getUser(int userNr) {
		String[] user = null;
		if ((userNr >= 0) && (userNr < users.size())) {
			user = users.get(userNr);
		}
		return user;
	}
	
	
	public String[] getUser(String userName) {
		String[] user = null;
		for (int userNr = 0; userNr < users.size(); userNr++) {
			if (users.get(userNr)[USER_NAME].equals(userName)) {
				user = users.get(userNr);
			}
		}
		return user;
	}
	
	
	public Map<String, Set<String>> getIPAddressUsersMap() {
		Map<String, Set<String>> ipAddressUsersMap = new HashMap<String, Set<String>>();
		
		for (String[] user : users) {
			String userDescription = user[USER_NAME];

			String ipAddresses = user[IP_ADDRESSES];
			if ((ipAddresses != null) && (!ipAddresses.trim().equals(""))) {
				String[] ipAddressesSplit = ipAddresses.split(";");
				for (String ipAddress : ipAddressesSplit) {
					ipAddress = ipAddress.trim();
					if (!ipAddress.equals("")) {
						Set<String> usersSet = ipAddressUsersMap.get(ipAddress);
						if (usersSet == null) {
							usersSet = new HashSet<String>();
							ipAddressUsersMap.put(ipAddress, usersSet);
						}
						usersSet.add(userDescription);
					}
				}
			}
		}
		
		return ipAddressUsersMap;
	}
	
	
	private void getData() {
		users.clear();
		String usersFileName = RREManager.getIniFile().getValue("General","DataFile");
		String sheetName = RREManager.getIniFile().getValue(settingsGroup,"Sheet");
		File file = new File(usersFileName);
		if (file.exists() && file.canRead()) {
			ExcelFile usersFile = new ExcelFile(usersFileName);
			if (usersFile.open()) {
				if (usersFile.getSheet(sheetName, true)) {
					while (usersFile.hasNext(sheetName)) {
						Row row = usersFile.getNext(sheetName);

						String projects     = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"Projects Column"));
						String groups       = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"Groups Column"));
						String firstName    = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"First Name Column"));
						String initials     = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"Initials Column"));
						String lastName     = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"Last Name Column"));
						String userName     = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"User Name Column"));
						String password     = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"Password Column"));
						String email        = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"Email Column"));
						String format       = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"Email Format Column"));
						String ipAddresses  = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"IP-Addresses Column"));
						String multiOTP     = "";
						
						String access = "FTP-Only";
						String multiOTPPDFName = "";
						String fileZillaXMLName = RREManager.getIniFile().getValue("FileZilla", "XMLFolder") + File.separator + "FileZilla " + userName + ".xml";
						if ((groups != null) && (!groups.trim().equals(""))) {
							access = "RDP";
							multiOTP = "No";
							if (!userName.equals("")) {
								multiOTPPDFName = RREManager.getIniFile().getValue("MultiOTP", "PDFFolder") + File.separator + userName + ".pdf";
								File multiOTPPDFFile = new File(multiOTPPDFName);
								if (multiOTPPDFFile.exists() && multiOTPPDFFile.canRead()) {
									multiOTP = "Yes";
								}
							}
						}
						
						String[] record = new String[OBJECT_SIZE];
						record[FIRST_NAME]    = firstName == null ? "" : firstName.trim();
						record[INITIALS]      = initials  == null ? "" : initials.trim();
						record[LAST_NAME]     = lastName  == null ? "" : lastName.trim();
						record[USER_NAME]     = userName;
						record[PASSWORD]      = password  == null ? "" : password.trim();
						record[EMAIL]         = email     == null ? "" : email.trim();
						record[EMAIL_FORMAT]  = format    == null ? "" : format.trim().toUpperCase();
						record[ACCESS]        = access;
						record[MULTIOTP]      = multiOTP;
						record[PROJECTS]      = projects  == null ? "" : projects.trim();
						record[GROUPS]        = groups    == null ? "" : groups.trim();
						record[IP_ADDRESSES]  = ipAddresses;
						record[MULTIOTP_PDF]  = multiOTPPDFName;
						record[FILEZILLA_XML] = fileZillaXMLName;
						
						users.add(record);
						
					}
				}
				usersFile.close();
			}
		}
	}
	
	
	public boolean addUser(String[] user) {
		boolean success = false;
		String error = null;
		
		String usersFileName = RREManager.getIniFile().getValue("General","DataFile");
		String sheetName = RREManager.getIniFile().getValue(settingsGroup,"Sheet");
		File file = new File(usersFileName);
		if (file.exists()) {
			if (file.canWrite()) {
				ExcelFile usersFile = new ExcelFile(usersFileName);
				if (usersFile.open()) {
					if (usersFile.getSheet(sheetName, true)) {
						
						Map<String, Object> row = new HashMap<String, Object>();
						
						row.put("Update"        , "");
						row.put(RREManager.getIniFile().getValue(settingsGroup,"Projects Column")    , user[PROJECTS]);
						row.put(RREManager.getIniFile().getValue(settingsGroup,"Groups Column")      , user[GROUPS]);
						row.put(RREManager.getIniFile().getValue(settingsGroup,"First Name Column")  , user[FIRST_NAME]);
						row.put(RREManager.getIniFile().getValue(settingsGroup,"Initials Column")    , user[INITIALS]);
						row.put(RREManager.getIniFile().getValue(settingsGroup,"Last Name Column")   , user[LAST_NAME]);
						row.put(RREManager.getIniFile().getValue(settingsGroup,"User Name Column")   , user[USER_NAME]);
						row.put(RREManager.getIniFile().getValue(settingsGroup,"Password Column")    , user[PASSWORD]);
						row.put(RREManager.getIniFile().getValue(settingsGroup,"Email Column")       , user[EMAIL]);
						row.put(RREManager.getIniFile().getValue(settingsGroup,"Email Format Column"), user[EMAIL_FORMAT]);
						row.put(RREManager.getIniFile().getValue(settingsGroup,"IP-Addresses Column"), user[IP_ADDRESSES]);

						if (usersFile.addRow(sheetName, row) && usersFile.write()) {
							if (!settingsGroup.equals("Specials")) {
								RREManager.changeLog.addLogEntry(new AddUserLogEntry(user[USER_NAME]));
							}
							else {
								RREManager.changeLog.addLogEntry(new NonScriptedChangeLogEntry());
							}
							getData();
							success = true;
						}
						else {
							error = "ERROR while adding user " + user[FIRST_NAME] + " " + user[LAST_NAME] + " (" + user[USER_NAME] + ")";
							success = false;
						}
					}
					else {
						error = "ERROR sheet '" + sheetName + "' not found in users file '" + usersFileName + "'";
						success = false;
					}
				}
				else {
					error = "ERROR cannot open users file '" + usersFileName + "'";
					success = false;
				}
			}
			else {
				error = "ERROR cannot write users file '" + usersFileName + "'";
				success = false;
			}
		}
		else {
			error = "ERROR cannot find users file '" + usersFileName + "'";
			success = false;
		}

		String allTimeLogRecord = "Add " + (settingsGroup.equals("Specials") ? "Special" : "User");
		allTimeLogRecord += "," + "\"" + user[EMAIL] + "\"";
		allTimeLogRecord += "," + "\"" + user[USER_NAME] + "\"";
		allTimeLogRecord += "," + "\"" + user[FIRST_NAME] + "\"";
		allTimeLogRecord += "," + "\"" + user[LAST_NAME] + "\"";
		allTimeLogRecord += ",";
		allTimeLogRecord += "," + "\"" + user[PASSWORD] + "\"";
		allTimeLogRecord += "," + "\"" + user[IP_ADDRESSES] + "\"";
		allTimeLogRecord += "," + "Yes";
		allTimeLogRecord += "," + "\"" + user[PROJECTS] + "\"";
		allTimeLogRecord += "," + "\"" + user[GROUPS] + "\"";
		
		String logLn = "Adding " + (settingsGroup.equals("Specials") ? "Special" : "User") + " " + user[FIRST_NAME] + " " + user[LAST_NAME] + " (" + user[USER_NAME] + ") ";
		
		if (success) {
			allTimeLogRecord += "," + "Succeeded";
			allTimeLogRecord += ",";
			mainFrame.allTimeLog(allTimeLogRecord, "");
			
			mainFrame.logWithTimeLn(logLn + "SUCCEEDED");
		}
		else {
			allTimeLogRecord += "," + "Failed";
			allTimeLogRecord += "," + "\"" + error + "\"";
			
			mainFrame.logWithTimeLn(logLn + "FAILED");
			mainFrame.logWithTimeLn(logLn + "  " + error);
		}
		
		return success;
	}
	
	
	public boolean modifyUser(String[] user, String[] modifiedUser) {
		boolean success = true;
		String error = null;
		
		if (
				(!user[FIRST_NAME].equals(modifiedUser[FIRST_NAME])) ||
				(!user[INITIALS].equals(modifiedUser[INITIALS])) ||
				(!user[LAST_NAME].equals(modifiedUser[LAST_NAME])) ||
				(!user[EMAIL].equals(modifiedUser[EMAIL])) ||
				(!user[EMAIL_FORMAT].equals(modifiedUser[EMAIL_FORMAT])) ||
				(!user[IP_ADDRESSES].equals(modifiedUser[IP_ADDRESSES])) ||
				(!user[PROJECTS].equals(modifiedUser[PROJECTS])) ||
				(!user[GROUPS].equals(modifiedUser[GROUPS]))
			) {
			
			String usersFileName = RREManager.getIniFile().getValue("General","DataFile");
			String sheetName = RREManager.getIniFile().getValue(settingsGroup,"Sheet");
			File file = new File(usersFileName);
			if (file.exists()) {
				if (file.canWrite()) {
					ExcelFile usersFile = new ExcelFile(usersFileName);
					if (usersFile.open()) {
						if (usersFile.getSheet(sheetName, true)) {
							while (usersFile.hasNext(sheetName)) {
								Row row = usersFile.getNext(sheetName);

								String userName = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"User Name Column"));
								
								if (userName.equals(user[USER_NAME])) {
									Cell projectsCell    = row.getCell(usersFile.getColumnNr(sheetName, RREManager.getIniFile().getValue(settingsGroup,"Projects Column")));
									Cell groupsCell      = row.getCell(usersFile.getColumnNr(sheetName, RREManager.getIniFile().getValue(settingsGroup,"Groups Column")));
									Cell firstNameCell   = row.getCell(usersFile.getColumnNr(sheetName, RREManager.getIniFile().getValue(settingsGroup,"First Name Column")));
									Cell initialsCell    = row.getCell(usersFile.getColumnNr(sheetName, RREManager.getIniFile().getValue(settingsGroup,"Initials Column")));
									Cell lastNameCell    = row.getCell(usersFile.getColumnNr(sheetName, RREManager.getIniFile().getValue(settingsGroup,"Last Name Column")));
									Cell emailCell       = row.getCell(usersFile.getColumnNr(sheetName, RREManager.getIniFile().getValue(settingsGroup,"Email Column")));
									Cell emailFormatCell = row.getCell(usersFile.getColumnNr(sheetName, RREManager.getIniFile().getValue(settingsGroup,"Email Format Column")));
									Cell ipAdressesCell  = row.getCell(usersFile.getColumnNr(sheetName, RREManager.getIniFile().getValue(settingsGroup,"IP-Addresses Column")));
									
									if (projectsCell    != null) projectsCell.setCellValue(modifiedUser[PROJECTS] == null ? "" : modifiedUser[PROJECTS]);
									if (groupsCell      != null) groupsCell.setCellValue(modifiedUser[GROUPS] == null ? "" : modifiedUser[GROUPS]);
									if (firstNameCell   != null) firstNameCell.setCellValue(modifiedUser[FIRST_NAME] == null ? "" : modifiedUser[FIRST_NAME]);
									if (initialsCell    != null) initialsCell.setCellValue(modifiedUser[INITIALS] == null ? "" : modifiedUser[INITIALS]);
									if (lastNameCell    != null) lastNameCell.setCellValue(modifiedUser[LAST_NAME] == null ? "" : modifiedUser[LAST_NAME]);
									if (emailCell       != null) emailCell.setCellValue(modifiedUser[EMAIL] == null ? "" : modifiedUser[EMAIL]);
									if (emailFormatCell != null) emailFormatCell.setCellValue(modifiedUser[EMAIL_FORMAT] == null ? "" : modifiedUser[EMAIL_FORMAT]);
									if (ipAdressesCell  != null) ipAdressesCell.setCellValue(modifiedUser[IP_ADDRESSES] == null ? "" : modifiedUser[IP_ADDRESSES]);

									if (usersFile.write()) {
										if (
												(
														(!user[PROJECTS].equals(modifiedUser[PROJECTS])) ||
														(!user[GROUPS].equals(modifiedUser[GROUPS]))
												)
											) {
											RREManager.changeLog.addLogEntry(new ModifyUserLogEntry(user[USER_NAME]));
										}
										else {
											RREManager.changeLog.addLogEntry(new NonScriptedChangeLogEntry());
										}
										success = true;
									}
									else {
										error = "ERROR while updating user " + user[FIRST_NAME] + " " + user[LAST_NAME] + " (" + user[USER_NAME] + ")";
										success = false;
									}
									break;
								}
							}
							usersFile.close();
							getData();
						}
						else {
							error = "ERROR sheet '" + sheetName + "' not found in users file '" + usersFileName + "'";
							success = false;
						}
					}
					else {
						error = "ERROR cannot open users file '" + usersFileName + "'";
						success = false;
					}
				}
				else {
					error = "ERROR cannot write users file '" + usersFileName + "'";
					success = false;
				}
			}
			else {
				error = "ERROR cannot find users file '" + usersFileName + "'";
				success = false;
			}

			String allTimeLogRecord = "Modify " + (settingsGroup.equals("Specials") ? "Special" : "User");
			allTimeLogRecord += "," + "\"" + modifiedUser[EMAIL] + "\"";
			allTimeLogRecord += "," + "\"" + modifiedUser[USER_NAME] + "\"";
			allTimeLogRecord += "," + "\"" + modifiedUser[FIRST_NAME] + "\"";
			allTimeLogRecord += "," + "\"" + modifiedUser[LAST_NAME] + "\"";
			allTimeLogRecord += ",";
			allTimeLogRecord += "," + "\"" + modifiedUser[PASSWORD] + "\"";
			allTimeLogRecord += "," + "\"" + modifiedUser[IP_ADDRESSES] + "\"";
			allTimeLogRecord += "," + "Yes";
			allTimeLogRecord += "," + "\"" + modifiedUser[PROJECTS] + "\"";
			allTimeLogRecord += "," + "\"" + modifiedUser[GROUPS] + "\"";
			
			String logLn = "Modifying " + (settingsGroup.equals("Specials") ? "Special" : "User") + " " + user[FIRST_NAME] + " " + user[LAST_NAME] + " (" + user[USER_NAME] + ") ";
			
			if (success) {
				allTimeLogRecord += "," + "Succeeded";
				allTimeLogRecord += ",";
				mainFrame.allTimeLog(allTimeLogRecord, "");
				
				mainFrame.logWithTimeLn(logLn + "SUCCEEDED");
			}
			else {
				allTimeLogRecord += "," + "Failed";
				allTimeLogRecord += "," + "\"" + error + "\"";
				
				mainFrame.logWithTimeLn(logLn + "FAILED");
				mainFrame.logWithTimeLn(logLn + "  " + error);
			}
		}
		
		return success;
	}
	
	
	public boolean deleteUser(String[] user) {
		boolean success = true;
		String error = null;
		
		String usersFileName = RREManager.getIniFile().getValue("General","DataFile");
		String sheetName = RREManager.getIniFile().getValue(settingsGroup,"Sheet");
		File file = new File(usersFileName);
		if (file.exists()) {
			if (file.canWrite()) {
				ExcelFile usersFile = new ExcelFile(usersFileName);
				if (usersFile.open()) {
					if (usersFile.getSheet(sheetName, true)) {
						while (usersFile.hasNext(sheetName)) {
							Row row = usersFile.getNext(sheetName);

							String userName = usersFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"User Name Column"));
							
							if (userName.equals(user[USER_NAME])) {
								usersFile.deleteRow(sheetName, row);
								if (usersFile.write()) {
									RREManager.changeLog.addLogEntry(new DeleteUserLogEntry(user[USER_NAME]));
									success = true;
								}
								else {
									error = "ERROR while deleting user " + user[FIRST_NAME] + " " + user[LAST_NAME] + " (" + user[USER_NAME] + ")";
									success = false;
								}
								break;
							}
						}
						usersFile.close();
						getData();
					}
					else {
						error = "ERROR sheet '" + sheetName + "' not found in users file '" + usersFileName + "'";
						success = false;
					}
				}
				else {
					error = "ERROR cannot open users file '" + usersFileName + "'";
					success = false;
				}
			}
			else {
				error = "ERROR cannot write users file '" + usersFileName + "'";
				success = false;
			}
		}
		else {
			error = "ERROR cannot find users file '" + usersFileName + "'";
			success = false;
		}

		String allTimeLogRecord = "Delete " + (settingsGroup.equals("Specials") ? "Special" : "User");
		allTimeLogRecord += ",";
		allTimeLogRecord += "," + "\"" + user[USER_NAME] + "\"";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		
		String logLn = "Deleting " + (settingsGroup.equals("Specials") ? "Special" : "User") + " " + user[USER_NAME] + " ";
		
		if (success) {
			allTimeLogRecord += "," + "Succeeded";
			allTimeLogRecord += ",";
			mainFrame.allTimeLog(allTimeLogRecord, "");
			
			mainFrame.logWithTimeLn(logLn + "SUCCEEDED");
			
			JOptionPane.showMessageDialog(null, "Don't forget to remove the user from " + ((!user[PROJECTS].equals("")) ? "Active Directory\nand " : "") + "Cerberus.", "Delete user", JOptionPane.WARNING_MESSAGE);
		}
		else {
			allTimeLogRecord += "," + "Failed";
			allTimeLogRecord += "," + "\"" + error + "\"";
			
			mainFrame.logWithTimeLn(logLn + "FAILED");
			mainFrame.logWithTimeLn(logLn + "  " + error);
		}
		
		return success;
	}

}
