package org.erasmusmc.rremanager.files;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.changelog.AddProjectLogEntry;
import org.erasmusmc.rremanager.gui.MainFrame;

public class ProjectData {
	
	private String settingsGroup = null;
	private MainFrame mainFrame = null;
	private Map<String, List<String>> projects = new HashMap<String, List<String>>();
	private String error = null;

	
	public ProjectData(MainFrame mainFrame, String settingsGroup) {
		this.mainFrame = mainFrame;
		getData(settingsGroup);
	}
	
	
	private void getData(String settingsGroup) {
		this.settingsGroup = settingsGroup;
		String projectsFileName = RREManager.getIniFile().getValue("General","DataFile");
		String sheetName = RREManager.getIniFile().getValue(settingsGroup, "Sheet");
		File file = new File(projectsFileName);
		if (file.exists() && file.canRead()) {
			ExcelFile projectsFile = new ExcelFile(projectsFileName);
			if (projectsFile.open()) {
				if (projectsFile.getSheet(sheetName, true)) {
					while (projectsFile.hasNext(sheetName)) {
						Row row = projectsFile.getNext(sheetName);

						String project = projectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup, "Project Column")).trim();
						String group   = projectsFile.getStringValue(sheetName, row, RREManager.getIniFile().getValue(settingsGroup, "Group Column")).trim();
						
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
			String groupsParameter = "";
			for (String newGroup : newProjects.get(project)) {
				groupsParameter += (groupsParameter.equals("") ? "" : ",") + newGroup;
			}
			List<String> parameters = new ArrayList<String>();
			parameters.add(project);
			parameters.add(groupsParameter);
			
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

		
		String projectsFileName = RREManager.getIniFile().getValue("General","DataFile");
		String sheetName = RREManager.getIniFile().getValue(settingsGroup, "Sheet");
		File file = new File(projectsFileName);
		if (file.exists() && file.canWrite()) {
			ExcelFile projectsFile = new ExcelFile(projectsFileName);
			if (projectsFile.open()) {
				if (projectsFile.getSheet(sheetName, true)) {
					if (projectsFile.clearSheet(sheetName, false)) {
						List<String> projectNames = new ArrayList<String>();
						projectNames.addAll(projects.keySet());
						for (String projectName : projectNames) {
							for (String group : projects.get(projectName)) {
								String allTimeLogRecord = null;
								String logLine = null;
								if ((newProjects.get(projectName) != null) && (newProjects.get(projectName).contains(group))) {
									logLine = "Add project group: " + projectName + "," + group + " ";
									allTimeLogRecord = "Add project group";
									allTimeLogRecord += ",";
									allTimeLogRecord += ",";
									allTimeLogRecord += ",";
									allTimeLogRecord += ",";
									allTimeLogRecord += ",";
									allTimeLogRecord += ",";
									allTimeLogRecord += ",";
									allTimeLogRecord += "," + projectName;
									allTimeLogRecord += "," + group;
								}
								
								Map<String, Object> row = new HashMap<String, Object>();
								row.put("Project", projectName);
								row.put("Group", group);
								
								if (!projectsFile.addRow(sheetName, row)) {
									error = "ERROR while adding project group " + projectName + "," + group + "";
									
									if (allTimeLogRecord != null) {
										allTimeLogRecord += "," + "Failed";
										allTimeLogRecord += "," + "\"" + error + "\"";
										mainFrame.logLn("");
										mainFrame.logWithTimeLn(logLine + "FAILED");
										mainFrame.allTimeLog(allTimeLogRecord, "");
									}
									
									success = false; 
								}
								else {
									if (allTimeLogRecord != null) {
										
										allTimeLogRecord += "," + "Succeeded";
										allTimeLogRecord += ",";
										mainFrame.logLn("");
										mainFrame.logWithTimeLn(logLine + "SUCCEEDED");
										mainFrame.allTimeLog(allTimeLogRecord, "");
									}
									
									success = true;
								}
							}
							if (!success) {
								break;
							}
						}
					}
					else {
						success = false;
					}
				}
				projectsFile.close();
				if (success) {
					success = projectsFile.write();
					if (!success) {
						error = "ERROR writing file " + projectsFileName;
						
						String logLine = "Adding project group FAILED";
						String allTimeLogRecord = "Adding project group";
						allTimeLogRecord += ",";
						allTimeLogRecord += ",";
						allTimeLogRecord += ",";
						allTimeLogRecord += ",";
						allTimeLogRecord += ",";
						allTimeLogRecord += ",";
						allTimeLogRecord += ",";
						allTimeLogRecord += ",";
						allTimeLogRecord += ",";
						allTimeLogRecord += "," + "Failed";
						allTimeLogRecord += "," + "\"" + error + "\"";
						mainFrame.logWithTimeLn(logLine);
						mainFrame.allTimeLog(allTimeLogRecord, "");
					}
				}
			}
		}
		
		return success;
	}
	
	
	public String getError() {
		return error;
	}
}
