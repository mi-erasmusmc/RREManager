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
	
	public static String[] fieldName = new String[] {
			"First name",
			"Iniitials",
			"Last name",
			"User name",
			"Password",
			"Email address",
			"Email format",
			"Access type",
			"QR-Code available",
			"Projects",
			"Project groups",
			"IP Adresses",
			"MultiOTP PDF"
	};
	

	private List<String[]> users = new ArrayList<String[]>();
	
	
	public static String getUserDescription(String[] user, boolean withEmail) {
		String userDescription = user[UserData.FIRST_NAME] == null ? "" : user[UserData.FIRST_NAME];
		userDescription += (userDescription.equals("") ? "" : (((user[UserData.LAST_NAME] == null) || user[UserData.LAST_NAME].equals("")) ? "" : " ")) + (user[UserData.LAST_NAME] == null ? "" : user[UserData.LAST_NAME]);
		if (withEmail) {
			userDescription += (userDescription.equals("") ? user[UserData.EMAIL] : " (" + user[UserData.EMAIL] + ")");
		}
		return userDescription;
	}
	

	public UserData(String settingsGroup) {
		getData(settingsGroup);
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
	
	
	private void getData(String settingsGroup) {
		String usersFileName = RREManager.getIniFile().getValue(settingsGroup,"File");
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
				usersFile.close();
			}
		}
	}
}
