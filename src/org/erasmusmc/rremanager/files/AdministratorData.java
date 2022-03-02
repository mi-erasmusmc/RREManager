package org.erasmusmc.rremanager.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.changelog.NonScriptedChangeLogEntry;
import org.erasmusmc.rremanager.gui.MainFrame;

public class AdministratorData {
	public final static int NAME        = 0;
	public final static int TITLE       = 1;
	public final static int PHONE       = 2;
	public final static int ERASMUS     = 3;
	public final static int EMAIL       = 4;
	public final static int PASSWORD    = 5;
	public final static int OBJECT_SIZE = 6;
	
	public static String[] fieldName = new String[] {
			"Name",
			"Title",
			"Telephone",
			"User Account",
			"Email Address",
			"Password"
	};
	
	
	public static boolean addAdministratorToIniFile(String[] administrator, IniFile iniFile, MainFrame mainFrame) {
		String error = "";

		String allTimeLogRecord = "Add Administrator";
		allTimeLogRecord += "," + "\"" + administrator[AdministratorData.EMAIL] + "\"";
		allTimeLogRecord += "," + "\"" + administrator[AdministratorData.NAME] + "\"";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		allTimeLogRecord += "," + "\"" + administrator[AdministratorData.PASSWORD] + "\"";
		allTimeLogRecord += ",";
		allTimeLogRecord += "," + "Yes";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		
		String logLn = "Adding Administrator " + administrator[AdministratorData.NAME] + " ";
		
		error = (error.equals("") ? (iniFile.setValue("Administrators", administrator[AdministratorData.NAME], administrator[AdministratorData.PASSWORD], null) ? "" : "Error adding admininistrator to group Administrators.") : error);
		error = (error.equals("") ? (iniFile.addGroup(administrator[AdministratorData.NAME], null) ? "" : "Error adding group for administrator in ini file.") : error);
		error = (error.equals("") ? (iniFile.setValue(administrator[AdministratorData.NAME], fieldName[AdministratorData.ERASMUS], administrator[AdministratorData.ERASMUS], null) ? "" : "Error setting user number.") : error);
		error = (error.equals("") ? (iniFile.setValue(administrator[AdministratorData.NAME], fieldName[AdministratorData.EMAIL], administrator[AdministratorData.EMAIL], null) ? "" : "Error setting email address.") : error);
		error = (error.equals("") ? (iniFile.setValue(administrator[AdministratorData.NAME], fieldName[AdministratorData.TITLE], administrator[AdministratorData.TITLE], null) ? "" : "Error setting title.") : error);
		error = (error.equals("") ? (iniFile.setValue(administrator[AdministratorData.NAME], fieldName[AdministratorData.PHONE], administrator[AdministratorData.PHONE], null) ? "" : "Error setting telephone number.") : error);
		error = (error.equals("") ? (RREManager.getIniFile().writeFile() ? "" : "Error writing ini file.") : error);

		if (mainFrame != null) {
			if (error.equals("")) {
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
		
		return error.equals("");
	}
	

	private MainFrame mainFrame = null;
	private List<String[]> administrators = new ArrayList<String[]>();
	
	
	public static String getAdministratorDescription(String[] administrator, boolean withEmail) {
		String administratorDescription = administrator[AdministratorData.NAME];
		return administratorDescription;
	}
	

	public AdministratorData(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		getData();
	}
	
	
	public List<String[]> getAdministratorsList() {
		return administrators;
	}
	
	
	public String[] getAdministrator(int administratorNr) {
		String[] administrator = null;
		if ((administratorNr >= 0) && (administratorNr < administrators.size())) {
			administrator = administrators.get(administratorNr);
		}
		return administrator;
	}
	
	
	public String[] getAdministrator(String administratorName) {
		String[] administrator = null;
		for (int administratorNr = 0; administratorNr < administrators.size(); administratorNr++) {
			if (administrators.get(administratorNr)[NAME].equals(administratorName)) {
				administrator = administrators.get(administratorNr);
			}
		}
		return administrator;
	}
	
	
	private void getData() {
		administrators.clear();
		Map<String, String> administratorsGroup = RREManager.getIniFile().getGroup("Administrators");
		if (administratorsGroup != null) {
			for (String administratorName : administratorsGroup.keySet()) {
				Map<String, String> adminstratorDefinition = RREManager.getIniFile().getGroup(administratorName);
				
				String[] administrator = new String[OBJECT_SIZE];
				administrator[NAME]     = administratorName;
				administrator[PASSWORD] = administratorsGroup.get(administratorName);
				administrator[TITLE]    = adminstratorDefinition.get(fieldName[TITLE]);
				administrator[PHONE]    = adminstratorDefinition.get(fieldName[PHONE]);
				administrator[ERASMUS]  = adminstratorDefinition.get(fieldName[ERASMUS]);
				administrator[EMAIL]    = adminstratorDefinition.get(fieldName[EMAIL]);
								
				administrators.add(administrator);
			}
		}
	}
	
	
	public boolean addAdministrator(String[] administrator) {
		boolean success = false;
		if (addAdministratorToIniFile(administrator, RREManager.getIniFile(), mainFrame)) {
			RREManager.changeLog.addLogEntry(new NonScriptedChangeLogEntry());
			success = true;
			getData();
		}
		
		return success;
	}
	
	
	public boolean modifyAdministrator(String[] modifiedAdministrator) {
		String error = "";

		String allTimeLogRecord = "Modify Administrator";
		allTimeLogRecord += "," + "\"" + modifiedAdministrator[AdministratorData.EMAIL] + "\"";
		allTimeLogRecord += "," + "\"" + modifiedAdministrator[AdministratorData.NAME] + "\"";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		allTimeLogRecord += "," + "\"" + modifiedAdministrator[AdministratorData.PASSWORD] + "\"";
		allTimeLogRecord += ",";
		allTimeLogRecord += "," + "Yes";
		allTimeLogRecord += ",";
		allTimeLogRecord += ",";
		
		String logLn = "Modifying Administrator " + modifiedAdministrator[AdministratorData.NAME] + " ";
		
		if (!modifiedAdministrator[AdministratorData.PASSWORD].equals("")) {
			error = (error.equals("") ? (RREManager.getIniFile().setValue("Administrators", modifiedAdministrator[AdministratorData.NAME], modifiedAdministrator[AdministratorData.PASSWORD], null) ? "" : "Error setting password.") : error); 
		}
		error = (error.equals("") ? (RREManager.getIniFile().setValue(modifiedAdministrator[AdministratorData.NAME], fieldName[AdministratorData.ERASMUS], modifiedAdministrator[AdministratorData.ERASMUS], null) ? "" : "Error setting user number.") : error);
		error = (error.equals("") ? (RREManager.getIniFile().setValue(modifiedAdministrator[AdministratorData.NAME], fieldName[AdministratorData.EMAIL], modifiedAdministrator[AdministratorData.EMAIL], null) ? "" : "Error setting email address.") : error);
		error = (error.equals("") ? (RREManager.getIniFile().setValue(modifiedAdministrator[AdministratorData.NAME], fieldName[AdministratorData.TITLE], modifiedAdministrator[AdministratorData.TITLE], null) ? "" : "Error setting title.") : error);
		error = (error.equals("") ? (RREManager.getIniFile().setValue(modifiedAdministrator[AdministratorData.NAME], fieldName[AdministratorData.PHONE], modifiedAdministrator[AdministratorData.PHONE], null) ? "" : "Error setting telephone number.") : error);
		error = (error.equals("") ? (RREManager.getIniFile().writeFile() ? "" : "Error writing ini file.") : error);
		
		if (error.equals("")) {
			allTimeLogRecord += "," + "Succeeded";
			allTimeLogRecord += ",";
			mainFrame.allTimeLog(allTimeLogRecord, "");
			
			mainFrame.logWithTimeLn(logLn + "SUCCEEDED");
			
			RREManager.changeLog.addLogEntry(new NonScriptedChangeLogEntry());
			
			getData();
		}
		else {
			allTimeLogRecord += "," + "Failed";
			allTimeLogRecord += "," + "\"" + error + "\"";
			
			mainFrame.logWithTimeLn(logLn + "FAILED");
			mainFrame.logWithTimeLn(logLn + "  " + error);
		}
		
		return error.equals("");
	}

}
