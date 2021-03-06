package org.erasmusmc.rremanager.files;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LogData {
	public static int DATE          =  0;
	public static int TIME          =  1;
	public static int VERSION       =  2;
	public static int ADMINISTRATOR =  3;
	public static int ACTION        =  4;
	public static int RECIPIENT     =  5;
	public static int USER          =  6;
	public static int FIRST_NAME    =  7;
	public static int LAST_NAME     =  8;
	public static int BCC           =  9;
	public static int PASSWORD      = 10;
	public static int IP_ADDRESSES  = 11;
	public static int APPROVED      = 12;
	public static int PROJECT       = 13;
	public static int GROUP         = 14;
	public static int RESULT        = 15;
	public static int ERROR         = 16;
	public static int LOG_FILE      = 17;
	public static int INFO          = 18;
	public static int OBJECT_SIZE   = 19;
	

	private String allTimeLogFileName = null;
	private Iterator<DelimitedFileRow> logFileIterator = null;
	private List<String[]> log = new ArrayList<String[]>();
	
	
	public LogData(String allTimeLogFileName) {
		this.allTimeLogFileName = allTimeLogFileName;
		getData();
	}
	
	
	public List<String[]> getLog() {
		return log;
	}
	
	
	public void getData() {
		log.clear();
		File file = new File(allTimeLogFileName);
		if (file.exists() && file.canRead()) {
			DelimitedFileWithHeader allTimeLogFile = new DelimitedFileWithHeader(allTimeLogFileName);
			if (allTimeLogFile.openForReading()) {
				logFileIterator = allTimeLogFile.iterator();
				while (logFileIterator.hasNext()) {
					DelimitedFileRow row = logFileIterator.next();
					
					String[] record = new String[OBJECT_SIZE];
					record[DATE]          = row.get("Date"         , true);
					record[TIME]          = row.get("Time"         , true);
					record[VERSION]       = row.get("Version"      , true);
					record[ADMINISTRATOR] = row.get("Administrator", true);
					record[ACTION]        = row.get("Action"       , true);
					record[RECIPIENT]     = row.get("Recipient"    , true);
					record[USER]          = row.get("User"         , true);
					record[FIRST_NAME]    = row.get("First Name"   , true);
					record[LAST_NAME]     = row.get("Last Name"    , true);
					record[BCC]           = row.get("BCC"          , true);
					record[PASSWORD]      = row.get("Password"     , true);
					record[IP_ADDRESSES]  = row.get("IP-addresses" , true);
					record[APPROVED]      = row.get("Approved"     , true);
					record[PROJECT]       = row.get("Project"      , true);
					record[GROUP]         = row.get("Group"        , true);
					record[RESULT]        = row.get("Result"       , true);
					record[ERROR]         = row.get("Error"        , true);
					record[LOG_FILE]      = row.get("Log File"     , true);
					record[INFO]          = row.get("Info"         , true);
					
					log.add(record);
				}
			}
		}
	}

}
