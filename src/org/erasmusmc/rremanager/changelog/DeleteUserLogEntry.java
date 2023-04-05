package org.erasmusmc.rremanager.changelog;

public class DeleteUserLogEntry extends LogEntry {
	
	private String userName = null;

	
	public DeleteUserLogEntry(String userName) {
		action = LogEntry.ACTION_DELETE_USER;
		this.userName = userName;
	}
	
	
	public String getUserName() {
		return userName;
	}
}
