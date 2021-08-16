package org.erasmusmc.rremanager.files;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.erasmusmc.rremanager.RREManager;

public class ProjectData {
	
	private String settingsGroup = null; 
	private Map<String, List<String>> projects = new HashMap<String, List<String>>();

	
	public ProjectData(String settingsGroup) {
		getData(settingsGroup);
	}
	
	
	private void getData(String settingsGroup) {
		this.settingsGroup = settingsGroup;
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
	
	
	public List<String> getProjectNames() {
		List<String> sortedProjectNames = new ArrayList<String>();
		sortedProjectNames.addAll(projects.keySet());
		Collections.sort(sortedProjectNames);
		return sortedProjectNames;
	}
	
	
	public List<String> getProjectGroups(String projectName) {
		List<String> sortedProjectGroups = new ArrayList<String>();
		if (projects.containsKey(projectName)) {
			sortedProjectGroups.addAll(projects.get(projectName));
			Collections.sort(sortedProjectGroups);
		}
		return sortedProjectGroups;
	}
	
	
	public boolean addProjects(Map<String, List<String>> newProjects) {
		boolean success = false;
		
		for (String project : newProjects.keySet()) {
			List<String> currentGroups = projects.get(project);
			if (currentGroups == null) {
				projects.put(project, newProjects.get(project));
			}
			else {
				for (String newGroup : newProjects.get(project)) {
					if (!projects.get(project).contains(newGroup)) {
						projects.get(project).add(newGroup);
					}
				}
				Collections.sort(projects.get(project));
			}
		}

		/*
		String projectsFileName = RREManager.getIniFile().getValue(settingsGroup,"File");
		String sheetName = RREManager.getIniFile().getValue(settingsGroup,"Sheet");
		File file = new File(projectsFileName);
		if (file.exists() && file.canRead()) {
			ExcelFile projectsFile = new ExcelFile(projectsFileName);
			if (projectsFile.open()) {
				if (projectsFile.getSheet(sheetName, true)) {
					//TODO
				}
				projectsFile.close();
			}
		}
		*/
		
		return success;
	}
	
	
}
