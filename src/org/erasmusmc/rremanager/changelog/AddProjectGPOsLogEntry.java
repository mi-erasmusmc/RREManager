package org.erasmusmc.rremanager.changelog;

public class AddProjectGPOsLogEntry extends LogEntry {
	String project = null;
	String groups = null;
	
	public AddProjectGPOsLogEntry(String project, String groups) {
		action = LogEntry.ACTION_ADD_PROJECT_GPO;
		this.project = project;
		this.groups = groups;
	}
	
	
	public String getProject() {
		return project;
	}
	
	
	public String getGroups() {
		return groups;
	}
}
