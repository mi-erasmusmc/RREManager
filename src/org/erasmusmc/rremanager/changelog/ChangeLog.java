package org.erasmusmc.rremanager.changelog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChangeLog {
	
	
	private List<LogEntry> log = new ArrayList<LogEntry>();
	
	
	public void addLogEntry(LogEntry logEntry) {
		log.add(logEntry);
	}
	
	
	public boolean hasChanges() {
		return (log.size() > 0);
	}
	
	
	public boolean hasScriptedChanges() {
		boolean hasScriptedChanges = false;
		for (LogEntry logEntry : log) {
			if (!logEntry.isNonSciptedChangeLogEntry()) {
				hasScriptedChanges = true;
				break;
			}
		}
		return hasScriptedChanges;
	}
	
	
	public List<LogEntry> getLogEntries() {
		Collections.sort(log, new Comparator<LogEntry>() {

			@Override
			public int compare(LogEntry logEntry1, LogEntry logEntry2) {
				return logEntry1.compareTo(logEntry2);
			}
			
		});
		return log;
	}
}
