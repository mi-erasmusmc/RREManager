package org.erasmusmc.rremanager.changelog;


public abstract class LogEntry {
	public static final Integer ACTION_ADD_PROJECT     = 0;
	public static final Integer ACTION_ADD_PROJECT_GPO = 1;
	public static final Integer ACTION_MODIFY_USER     = 2;
	public static final Integer ACTION_ADD_USER        = 3;
	
	
	protected Integer action = -1;
	
	
	public boolean isAddProjectLogEntry() {
		return action == ACTION_ADD_PROJECT;
	}
	
	
	public boolean isAddProjectGPOLogEntry() {
		return action == ACTION_ADD_PROJECT_GPO;
	}
	
	
	public boolean isModifyUserLogEntry() {
		return action == ACTION_MODIFY_USER;
	}
	
	
	public boolean isAddUserLogEntry() {
		return action == ACTION_ADD_USER;
	}
	
	
	public int compareTo(LogEntry otherLogEntry) {
		return action.compareTo(otherLogEntry.action);
	}
}
