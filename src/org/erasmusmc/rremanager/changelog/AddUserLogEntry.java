package org.erasmusmc.rremanager.changelog;

public class AddUserLogEntry extends LogEntry {
	
	private String userName = null;

	
	public AddUserLogEntry(String userName) {
		action = LogEntry.ACTION_ADD_USER;
		this.userName = userName;
	}
	
	
	public String getUserName() {
		return userName;
	}
}
