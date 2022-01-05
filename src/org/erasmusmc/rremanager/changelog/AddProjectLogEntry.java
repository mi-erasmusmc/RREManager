package org.erasmusmc.rremanager.changelog;


public class AddProjectLogEntry extends LogEntry {
	String project = null;
	String subfolders = null;
	
	
	public AddProjectLogEntry(String project, String subFolders) {
		action = LogEntry.ACTION_ADD_PROJECT;
		this.project = project;
		this.subfolders = subFolders;
	}
	
	
	public String getProject() {
		return project;
	}
	
	
	public String getSubFolders() {
		return subfolders;
	}
}
