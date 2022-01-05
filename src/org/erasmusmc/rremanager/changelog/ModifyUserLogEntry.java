package org.erasmusmc.rremanager.changelog;

public class ModifyUserLogEntry extends LogEntry {
	
	private String userName = null;

	
	public ModifyUserLogEntry(String userName) {
		action = LogEntry.ACTION_MODIFY_USER;
		this.userName = userName;
	}
	
	
	public String getUserName() {
		return userName;
	}
}
