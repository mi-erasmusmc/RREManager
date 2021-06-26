package org.erasmusmc.rremanager.files;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.erasmusmc.rremanager.RREManager;

public class UserData {
	public static int FIRST_NAME   =  0;
	public static int INITIALS     =  1;
	public static int LAST_NAME    =  2;
	public static int USER_NAME    =  3;
	public static int PASSWORD     =  4;
	public static int EMAIL        =  5;
	public static int EMAIL_FORMAT =  6;
	public static int ACCESS       =  7;
	public static int MULTIOTP     =  8;
	public static int PROJECTS     =  9;
	public static int GROUPS       = 10;
	public static int IP_ADDRESSES = 11;
	public static int MULTIOTP_PDF = 12;
	public static int OBJECT_SIZE  = 13;
	

	private List<String[]> users = new ArrayList<String[]>();
	
	
	public static String getUserDescription(String[] user) {
		String userDescription = user[UserData.FIRST_NAME] == null ? "" : user[UserData.FIRST_NAME];
		userDescription += (userDescription.equals("") ? "" : (((user[UserData.LAST_NAME] == null) || user[UserData.LAST_NAME].equals("")) ? "" : " ")) + (user[UserData.LAST_NAME] == null ? "" : user[UserData.LAST_NAME]);
		userDescription += (userDescription.equals("") ? user[UserData.EMAIL] : " (" + user[UserData.EMAIL] + ")");
		return userDescription;
	}
	

	public UserData(String userProjectsFileName) {
		getData(userProjectsFileName);
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
	
	
	private void getData(String userProjectsFileName) {
		File file = new File(userProjectsFileName);
		if (file.exists() && file.canRead()) {
			ExcelFile userProjectsFile = new ExcelFile(userProjectsFileName);
			if (userProjectsFile.open()) {
				String sheetName = RREManager.getIniFile().getValue("User Projects File","Sheet");
				if (userProjectsFile.getSheet(sheetName, true)) {
					while (userProjectsFile.hasNext(sheetName)) {
						Row row = userProjectsFile.getNext(sheetName);

						String projects     = userProjectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue("User Projects File","Projects Column"));
						String groups       = userProjectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue("User Projects File","Groups Column"));
						String firstName    = userProjectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue("User Projects File","First Name Column"));
						String initials     = userProjectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue("User Projects File","Initials Column"));
						String lastName     = userProjectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue("User Projects File","Last Name Column"));
						String userName     = userProjectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue("User Projects File","User Name Column"));
						String password     = userProjectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue("User Projects File","Password Column"));
						String email        = userProjectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue("User Projects File","Email Column"));
						String format       = userProjectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue("User Projects File","Email Format Column"));
						String ipAddresses  = userProjectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue("User Projects File","IP-Addresses Column"));
						String multiOTP     = "";
						
						String access = "FTP-Only";
						String multiOTPPDFName = "";
						if ((groups != null) && (!groups.trim().equals(""))) {
							access = "RDP";
							multiOTP = "No";
							if (!userName.equals("")) {
								multiOTPPDFName = RREManager.getIniFile().getValue("MultiOTP","PDFFolder") + File.separator + userName + ".pdf";
								File multiOTPPDFFile = new File(multiOTPPDFName);
								if (multiOTPPDFFile.exists() && multiOTPPDFFile.canRead()) {
									multiOTP = "Yes";
								}
							}
						}
						
						String[] record = new String[OBJECT_SIZE];
						record[FIRST_NAME]   = firstName == null ? "" : firstName.trim();
						record[INITIALS]     = initials  == null ? "" : initials.trim();
						record[LAST_NAME]    = lastName  == null ? "" : lastName.trim();
						record[USER_NAME]    = userName;
						record[PASSWORD]     = password  == null ? "" : password.trim();
						record[EMAIL]        = email     == null ? "" : email.trim();
						record[EMAIL_FORMAT] = format    == null ? "" : format.trim().toUpperCase();
						record[ACCESS]       = access;
						record[MULTIOTP]     = multiOTP;
						record[PROJECTS]     = projects  == null ? "" : projects.trim();
						record[GROUPS]       = groups    == null ? "" : groups.trim();
						record[IP_ADDRESSES] = ipAddresses;
						record[MULTIOTP_PDF] = multiOTPPDFName;
						
						users.add(record);
						
					}
				}
				userProjectsFile.close();
			}
		}
	}
}
