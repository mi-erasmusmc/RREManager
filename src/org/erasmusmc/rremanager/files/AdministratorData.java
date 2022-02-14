package org.erasmusmc.rremanager.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.gui.MainFrame;

public class AdministratorData {
	public final static int NAME        = 0;
	public final static int TITLE       = 1;
	public final static int PHONE       = 2;
	public final static int ERASMUS     = 3;
	public final static int PASSWORD    = 4;
	public final static int OBJECT_SIZE = 5;
	
	public static String[] fieldName = new String[] {
			"Name",
			"Title",
			"Telephone",
			"User Account",
			"Password"
	};
	

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
				administrator[TITLE]    = adminstratorDefinition.get("Title");
				administrator[PHONE]    = adminstratorDefinition.get("Telephone");
				administrator[ERASMUS]  = adminstratorDefinition.get("User Account");
								
				administrators.add(administrator);
			}
		}
	}
	
	
	public boolean addAdministrator(String[] administrator) {
		boolean success = true;
		
		success = success && RREManager.getIniFile().setValue("Administrators", administrator[AdministratorData.NAME], administrator[AdministratorData.PASSWORD], null);
		success = success && RREManager.getIniFile().addGroup(administrator[AdministratorData.NAME], null);
		success = success && RREManager.getIniFile().setValue(administrator[AdministratorData.NAME], fieldName[AdministratorData.ERASMUS], administrator[AdministratorData.ERASMUS], null);
		success = success && RREManager.getIniFile().setValue(administrator[AdministratorData.NAME], fieldName[AdministratorData.TITLE], administrator[AdministratorData.TITLE], null);
		success = success && RREManager.getIniFile().setValue(administrator[AdministratorData.NAME], fieldName[AdministratorData.PHONE], administrator[AdministratorData.PHONE], null);
		
		if (success) {
			success = RREManager.getIniFile().writeFile();
			if (success) {
				getData();
			}
		}
		
		return success;
	}
	
	
	public boolean modifyAdministrator(String[] administrator, String[] modifiedAdministrator) {
		boolean success = true;
		
		if (!administrator[AdministratorData.PASSWORD].equals("")) {
			success = success && RREManager.getIniFile().setValue("Administrators", administrator[AdministratorData.NAME], administrator[AdministratorData.PASSWORD], null);
		}
		success = success && RREManager.getIniFile().setValue(administrator[AdministratorData.NAME], fieldName[AdministratorData.ERASMUS], administrator[AdministratorData.ERASMUS], null);
		success = success && RREManager.getIniFile().setValue(administrator[AdministratorData.NAME], fieldName[AdministratorData.TITLE], administrator[AdministratorData.TITLE], null);
		success = success && RREManager.getIniFile().setValue(administrator[AdministratorData.NAME], fieldName[AdministratorData.PHONE], administrator[AdministratorData.PHONE], null);
		
		if (success) {
			success = RREManager.getIniFile().writeFile();
			if (success) {
				getData();
			}
		}
		
		return success;
	}

}
