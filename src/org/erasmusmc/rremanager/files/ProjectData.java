package org.erasmusmc.rremanager.files;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.erasmusmc.rremanager.RREManager;

public class ProjectData {
	
	private Map<String, List<String>> projects = new HashMap<String, List<String>>();

	
	public ProjectData(String settingsGroup) {
		getData(settingsGroup);
	}
	
	
	private void getData(String settingsGroup) {
		String projectsFileName = RREManager.getIniFile().getValue(settingsGroup,"File");
		String sheetName = RREManager.getIniFile().getValue(settingsGroup,"Sheet");
		File file = new File(projectsFileName);
		if (file.exists() && file.canRead()) {
			ExcelFile projectsFile = new ExcelFile(projectsFileName);
			if (projectsFile.open()) {
				if (projectsFile.getSheet(sheetName, true)) {
					while (projectsFile.hasNext(sheetName)) {
						Row row = projectsFile.getNext(sheetName);

						String project = projectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"Project Column")).trim();
						String group   = projectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup,"Group Column")).trim();
						
						if ((!project.equals("")) && (!group.equals(""))) {
							List<String> projectGroups = projects.get(project);
							if (projectGroups == null) {
								projectGroups = new ArrayList<String>();
								projects.put(project, projectGroups);
							}
							if (!projectGroups.contains(group)) {
								projectGroups.add(group);
							}
						}
					}
				}
				projectsFile.close();
			}
		}
	}
	
	
}
