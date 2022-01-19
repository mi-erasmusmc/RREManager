package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FileUtils;
import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.changelog.AddProjectLogEntry;
import org.erasmusmc.rremanager.changelog.AddUserLogEntry;
import org.erasmusmc.rremanager.changelog.LogEntry;
import org.erasmusmc.rremanager.changelog.ModifyUserLogEntry;
import org.erasmusmc.rremanager.files.UserData;
import org.erasmusmc.rremanager.utilities.DateUtilities;
import org.erasmusmc.rremanager.utilities.ScriptUtilities;


public class MainFrame {
	
	private static final String ICON = "/org/erasmusmc/rremanager/gui/Octopus 48x48.png";
	
	private RREManager rreManager;
	private String logFolder;
	private String fullLogFileName;
	private String allTimeLogFileName = null;
	private File allTimeLogFile = null;
	
	private JFrame frame;
	private JTabbedPane tabbedPane;
	private UsersTab usersTab = null;
	private UsersTab specialsTab = null;
	private ProjectsTab projectsTab = null;
	private LogTab logTab = null;
	private Console console = null;
	private String busy = null; 

	/**
	 * Sets an icon on a JFrame or a JDialog.
	 * @param container - the GUI component on which the icon is to be put
	 */
	public static void setIcon(Object container){
		URL url = RREManager.class.getResource(ICON);
		Image img = Toolkit.getDefaultToolkit().getImage(url);
		if (container.getClass() == JFrame.class ||
				JFrame.class.isAssignableFrom(container.getClass()))
			((JFrame)container).setIconImage(img);
		else if (container.getClass() == JDialog.class  ||
				JDialog.class.isAssignableFrom(container.getClass()))
			((JDialog)container).setIconImage(img);
		else
			((JFrame)container).setIconImage(img);
	}
	
	
	public MainFrame(RREManager rreManager) {
		this.rreManager = rreManager;
		allTimeLogFileName = RREManager.noLogging ? null : (RREManager.getIniFile().getValue("General", "Log Folder") + File.separator + "RREManagerLog.csv");
		createInterface();
	}
	
	
	public RREManager getRREManager() {
		return rreManager;
	}
	
	
	private void createInterface() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	String busy = isBusy();
		    	if (busy == null) {
		    		if (rreManager.errorOccurred()) {
	    				if (restoreDataFile(true)) {
	    					JOptionPane.showMessageDialog(null, "Errors Occurred!\nRestored original data file!\nCHECK ACTIVE DIRECTORY STATUS!", "RREManager Script Error", JOptionPane.ERROR_MESSAGE);
	    				}
	    				else {
	    					JOptionPane.showMessageDialog(null, "Errors Occurred!\nCould not restore original data file!\nCHECK ACTIVE DIRECTORY STATUS!", "RREManager Script Error", JOptionPane.ERROR_MESSAGE);
	    				}
		    		}
		    		else {
			    		if (RREManager.changeLog.hasChanges()) {
			    			if (JOptionPane.showConfirmDialog(frame, "Do you want apply the changes to the server?", "Apply changes?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
				    			if (runScripts()) {
					    			backupDataFile();
				    			}
				    			else {
				    				if (restoreDataFile(true)) {
				    					JOptionPane.showMessageDialog(null, "Script Error!\nRestored original data file!\nCHECK ACTIVE DIRECTORY STATUS!", "RREManager Script Error", JOptionPane.ERROR_MESSAGE);
				    				}
				    				else {
				    					JOptionPane.showMessageDialog(null, "Script Error!\nCould not restore original data file!\nCHECK ACTIVE DIRECTORY STATUS!", "RREManager Script Error", JOptionPane.ERROR_MESSAGE);
				    				}
				    			}
			    			}
			    			else {
			    				if (restoreDataFile(false)) {
			    					JOptionPane.showMessageDialog(null, "Restored original data file!", "RREManager Restore", JOptionPane.INFORMATION_MESSAGE);
			    				}
			    				else {
			    					JOptionPane.showMessageDialog(null, "Script Error!\nCould not restore original data file!", "RREManager Restore Error", JOptionPane.ERROR_MESSAGE);
			    				}
			    			}
			    		}
		    		}
		            System.exit(0);
		    	}
		    	/*
		        if (
		        		(busy == null) ||
		        		(JOptionPane.showConfirmDialog(
		        						frame, 
		        						busy + "\r\n" + "Are you sure you want to exit?", "Exit?", 
		        						JOptionPane.YES_NO_OPTION,
		        						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		        ) {
		            System.exit(0);
		        }
		        */
		    }
		});
		
		frame.setSize(1000, 800);
		frame.setMinimumSize(new Dimension(800, 600));
		setTitle(null);
		MainFrame.setIcon(frame);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		RREManager.disableWhenRunning(tabbedPane);
		
		usersTab    = new UsersTab(rreManager, this, "User Projects");
		specialsTab = new UsersTab(rreManager, this, "Specials");
		projectsTab = new ProjectsTab(rreManager, this, "Projects");
		logTab      = RREManager.noLogging ? null : new LogTab(rreManager, this);
		
		if (usersTab    != null) tabbedPane.addTab("Users"   , usersTab);
		if (specialsTab != null) tabbedPane.addTab("Specials", specialsTab);        		
		if (projectsTab != null) tabbedPane.addTab("Projects", projectsTab);
		if (logTab      != null) tabbedPane.addTab("Log"     , logTab);
		
        frame.add(tabbedPane, BorderLayout.CENTER);
        
        frame.setVisible(true);

        setLogFile();
	}
	
	
	public void refreshLog() {
		if (logTab != null) {
			logTab.refresh();
		}
	}
	
	
	public JPanel createLogPanel() {
		JTextArea consoleArea = new JTextArea();
		consoleArea.setToolTipText("General progress information");
		consoleArea.setEditable(false);
		if (console == null) {
			console = new Console();
		}
		console.addTextArea(consoleArea);
		if (!(System.getProperty("runInEclipse") == null ? false : System.getProperty("runInEclipse").equalsIgnoreCase("true"))) {
			System.setOut(new PrintStream(console));
			System.setErr(new PrintStream(console));
		}
		JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
		consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console"));
		consoleScrollPane.setAutoscrolls(true);
		
		JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setMinimumSize(new Dimension(700, 200));
        logPanel.setPreferredSize(new Dimension(700, 200));
        logPanel.add(consoleScrollPane, BorderLayout.CENTER);
        
		return logPanel;
	}
	
	
	private void backupDataFile() {
		String dataFileName = RREManager.getIniFile().getValue("General","DataFile");
		String backupFileName = getNextFileName(dataFileName);
		if (backupFileName != null) {
			File dataFile = new File(dataFileName);
			File backupFile = new File(backupFileName);
			try {
				FileUtils.copyFile(dataFile, backupFile);
				String allTimeLogRecord = "Create backup";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += "," + "Succeeded";
				allTimeLogRecord += ",";
				logWithTimeLn("Created backup of " + dataFileName + " in " + backupFileName);
				allTimeLog(allTimeLogRecord, backupFileName);
			} catch (IOException copyException) {
				String allTimeLogRecord = "Create backup";
				allTimeLogRecord += ",";
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
				allTimeLogRecord += "," + "Copy failed";
				logWithTimeLn("FAILED to create backup of '" + dataFileName + "' in '" + backupFileName + "'");
				allTimeLog(allTimeLogRecord, backupFileName);

				allTimeLogRecord = "Delete failed backup";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				String logLn = "Deleted backup file '" + dataFileName + "'";
				try {
					backupFile.delete();
					allTimeLogRecord += "," + "Succeeded";
					allTimeLogRecord += ",";
				} catch (SecurityException securityException) {
					allTimeLogRecord += "," + "Failed";
					allTimeLogRecord += "," + "Delete failed";
					logLn = "FAILED to delete backup file '" + dataFileName + "'";
				}
				logWithTimeLn(logLn);
				allTimeLog(allTimeLogRecord, backupFileName);
				JOptionPane.showMessageDialog(null, "Cannot create backup file!\nCreate it manually.", "RREManager Backup Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	
	private boolean restoreDataFile(boolean createErrorFile) {
		boolean success = false;
		
		String dataFileName = RREManager.getIniFile().getValue("General","DataFile");
		
		if (createErrorFile) {
			String errorFileName = getNextErrorFileName(getNextFileName(dataFileName));
			
			if (errorFileName != null) {
				try {
					File dataFile = new File(dataFileName);
					File errorFile = new File(errorFileName);
					FileUtils.copyFile(dataFile, errorFile);
					String allTimeLogRecord = "Create error file";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					allTimeLogRecord += ",";
					allTimeLogRecord += "," + "Succeeded";
					allTimeLogRecord += ",";
					logWithTimeLn("Created error file " + errorFileName + " from " + dataFileName);
					allTimeLog(allTimeLogRecord, errorFileName);
				} catch (IOException e) {
					String allTimeLogRecord = "Create error file";
					allTimeLogRecord += ",";
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
					allTimeLogRecord += "," + "Copy failed";
					logWithTimeLn("FAILED to create error file " + errorFileName + " from " + dataFileName);
					allTimeLog(allTimeLogRecord, errorFileName);
					JOptionPane.showMessageDialog(null, "Cannot create error file!", "RREManager File Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		String restoreFileName = getLastFileName(dataFileName);
		if (restoreFileName != null) {
			try {
				File dataFile = new File(dataFileName);
				File restoreFile = new File(restoreFileName);
				FileUtils.copyFile(restoreFile, dataFile);
				String allTimeLogRecord = "Restore backup";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += "," + "Succeeded";
				allTimeLogRecord += ",";
				logWithTimeLn("Restored backup of " + dataFileName + " from " + restoreFileName);
				allTimeLog(allTimeLogRecord, restoreFileName);
				success = true;
			} catch (IOException e) {
				String allTimeLogRecord = "Restore backup";
				allTimeLogRecord += ",";
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
				allTimeLogRecord += "," + "Copy failed";
				logWithTimeLn("FAILED to restorer backup of " + dataFileName + " from " + restoreFileName);
				allTimeLog(allTimeLogRecord, restoreFileName);
				JOptionPane.showMessageDialog(null, "Cannot restore original file!", "RREManager Restore Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		return success;
	}
	
	
	private String getNextFileName(String fileName) {
		String nextFileName = null;

		String date = DateUtilities.getCurrentDate();
		String baseFileName = fileName.substring(0, fileName.lastIndexOf("."));
		String extension = fileName.substring(fileName.lastIndexOf("."));

		boolean found = false;
		for (Integer versionNr = 1; versionNr < 100; versionNr++) {
			String versionNrString = ("00" + versionNr).substring(versionNr.toString().length());
			nextFileName = baseFileName + " " + date + " " + versionNrString + extension;
			File nextFile = new File(nextFileName);
			if (!nextFile.exists()) {
				found = true;
				break;
			}
		}
		if (!found) {
			JOptionPane.showMessageDialog(null, "Cannot create new version of file '" + fileName + "'.", "RREManager File Error", JOptionPane.ERROR_MESSAGE);
			nextFileName = null;
		}
		
		return nextFileName;
	}
	
	
	private String getNextErrorFileName(String dataFileName) {
		String nextErrorFileName = null;

		String baseFileName = dataFileName.substring(0, dataFileName.lastIndexOf("."));
		String extension = dataFileName.substring(dataFileName.lastIndexOf("."));

		boolean found = false;
		for (Integer versionNr = 1; versionNr < 100; versionNr++) {
			String versionNrString = ("00" + versionNr).substring(versionNr.toString().length());
			nextErrorFileName = baseFileName + " ERROR " + versionNrString + extension;
			File nextFile = new File(nextErrorFileName);
			if (!nextFile.exists()) {
				found = true;
				break;
			}
		}
		if (!found) {
			JOptionPane.showMessageDialog(null, "Cannot create new version of file '" + dataFileName + "'.", "RREManager File Error", JOptionPane.ERROR_MESSAGE);
			nextErrorFileName = null;
		}
		
		return nextErrorFileName;
	}
	
	
	private String getLastFileName(String dataFileName) {
		String lastFileName = null;

		String baseFileName = dataFileName.substring(dataFileName.lastIndexOf(File.separator) + 1);
		baseFileName = baseFileName.substring(0, baseFileName.lastIndexOf(".")) + " ";
		String extension = dataFileName.substring(dataFileName.lastIndexOf("."));
		
		String folderName = dataFileName.substring(0, dataFileName.lastIndexOf(File.separator));
		File folder = new File(folderName);
		String[] fileFolderList = folder.list();
		List<String> dataFileNamesList = new ArrayList<String>();
		for (String fileFolderName : fileFolderList) {
			File fileFolder = new File(fileFolderName);
			if (fileFolder.isFile() && fileFolderName.endsWith(extension) && fileFolderName.startsWith(baseFileName) && (!fileFolderName.contains("ERROR"))) {
				dataFileNamesList.add(fileFolderName);
			}
		}
		
		if (dataFileNamesList.size() > 0) {
			Collections.sort(dataFileNamesList);
			lastFileName = dataFileNamesList.get(dataFileNamesList.size() - 1);
		}
		
		return lastFileName;
	}
	
	
	private void setLogFile() {
		logFolder = RREManager.getIniFile().getValue("General", "Log Folder");
		if ((logFolder == null) || logFolder.equals("")) {
			logFolder = new File(".").getAbsolutePath();
		}
		String logFileName = "RREManager.log";
		String outputVersion = getOutputVersion(logFileName);;
		String baseName = logFolder + File.separator + outputVersion;
		fullLogFileName = baseName + logFileName;
	}
	
	
	private String getOutputVersion(String logFileName) {
		String version = "";
		
		String date = DateUtilities.getCurrentDate();
		
		for (Integer versionNr = 1; versionNr < 100; versionNr++) {
			String versionNrString = ("00" + versionNr).substring(versionNr.toString().length());
			File logFile = new File(logFolder + "/" + date + " " + versionNrString + " " + logFileName);
			if (!logFile.exists()) {
				version = date + " " + versionNrString + " ";
				break;
			}
		}
		
		return version;
	}
	
	
	public void setTitle(String user) {
		frame.setTitle("RRE User Manager v" + RREManager.version + (user == null ? "" : " - " + user));
	}
	
	
	public void logWithTime(String logText) {
		log(DateUtilities.getCurrentTime() + " " + logText);
	}
	
	
	public void logWithTimeLn(String logText) {
		logLn(DateUtilities.getCurrentTime() + " " + logText);
	}
	
	
	public void log(String logText) {
		System.out.print(logText);
	}
	
	
	public void logLn(String logText) {
		if (!RREManager.loggingStarted) {
			RREManager.loggingStarted = true;
			console.setDebugFile(RREManager.noLogging ? null : fullLogFileName);
			String allTimeLogHeader = "Action";
			allTimeLogHeader += "," + "Recipient";
			allTimeLogHeader += "," + "User";
			allTimeLogHeader += "," + "First Name";
			allTimeLogHeader += "," + "Last Name";
			allTimeLogHeader += "," + "BCC";
			allTimeLogHeader += "," + "Password";
			allTimeLogHeader += "," + "IP-addresses";
			allTimeLogHeader += "," + "Approved";
			allTimeLogHeader += "," + "Project";
			allTimeLogHeader += "," + "Group";
			allTimeLogHeader += "," + "Result";
			allTimeLogHeader += "," + "Error";
			allTimeLogHeader += "," + "Log File";
			allTimeLogFile = RREManager.noLogging ? null : new File(allTimeLogFileName);
			if (RREManager.noLogging || allTimeLogFile.exists() || allTimeLog(allTimeLogHeader, "Info")) {
				logWithTimeLn("RRE Manager v" + RREManager.version + " - " + rreManager.getAdministrator());
				logLn("");
				logLn("");
				logLn(RREManager.getIniFile().getFileName() + ":");
				logLn("--------------------------------------------------------------------------------------");
				RREManager.getIniFile().writeFile(System.out);
				logLn("--------------------------------------------------------------------------------------");
				logLn("");
			}
		}
		System.out.println(logText);
	}
	
	
	public boolean allTimeLog(String record, String info) {
		boolean success = true;
		if (!RREManager.noLogging) {
			if (allTimeLogFile.exists()) {
				record = DateUtilities.getCurrentTime().replaceAll(" ", ",") + "," + rreManager.getAdministrator() + "," + record;
				record += "," + getLogFileName();
				record += "," + "\"" + info + "\"";
			}
			else { // File does no exist -> write header
				record = "Date,Time,Administrator," + record;
				record += "," + info;
			}
			try {
				FileWriter logWriter = new FileWriter(allTimeLogFile, true);
				logWriter.write(record + "\r\n");
				logWriter.close();
				success = true;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Cannot write all time log file '" + allTimeLogFileName + "'.", "RREManager Log Error", JOptionPane.ERROR_MESSAGE);
				success = false;
			}
		}
		return success;
	}
	
	
	public void setBusy(String busy) {
		this.busy = busy;
	}
	
	
	public JFrame getFrame() {
		return frame;
	}
	
	
	private String isBusy() {
		return busy;
	}
	
	
	public String getLogFileName() {
		return fullLogFileName;
	}
	
	
	public String getAllTimeLogFileName() {
		return allTimeLogFileName;
	}
	
	
	public ProjectsTab getProjectsTab() {
		return projectsTab;
	}
	
	
	private boolean runScripts() {
		boolean success = true;

		logWithTimeLn("Running scripts");
		UserData userData = new UserData(this, "User Projects");
		for (LogEntry logEntry : RREManager.changeLog.getLogEntries()) {
			String allTimeLogRecord = "";
			
			String action = "<UNKNOWN>";
			List<String> parameters = new ArrayList<String>();
			String script = null;
			if (logEntry.isAddProjectLogEntry()) {
				action = "Create Project";
				parameters.add(((AddProjectLogEntry) logEntry).getProject());
				parameters.add(((AddProjectLogEntry) logEntry).getSubFolders());
				script = "createProject.vbs";

				allTimeLogRecord += "Run " + action + " Script";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += ",";
				allTimeLogRecord += "," + "Yes";
				allTimeLogRecord += "," + "\"" + ((AddProjectLogEntry) logEntry).getProject() + "\"";
				allTimeLogRecord += "," + "\"" + ((AddProjectLogEntry) logEntry).getSubFolders() + "\"";
			}
			else if (logEntry.isModifyUserLogEntry()) {
				String userName = ((ModifyUserLogEntry) logEntry).getUserName();
				String[] user = userData.getUser(userName);
				if (user != null) {
					action = "Modify User";
					parameters.add(user[UserData.FIRST_NAME]);
					parameters.add(user[UserData.INITIALS]);
					parameters.add(user[UserData.LAST_NAME]);
					parameters.add(user[UserData.USER_NAME]);
					parameters.add(user[UserData.PASSWORD]);
					parameters.add(user[UserData.EMAIL]);
					parameters.add(user[UserData.PROJECTS]);
					parameters.add(user[UserData.GROUPS]);
					parameters.add("1"); // Update Flag
					script = "createUserProjects.vbs";

					allTimeLogRecord += "Run " + action + " Script";
					allTimeLogRecord += ",";
					allTimeLogRecord += "," + "\"" + user[UserData.USER_NAME] + "\"";
					allTimeLogRecord += "," + "\"" + user[UserData.FIRST_NAME] + "\"";
					allTimeLogRecord += "," + "\"" + user[UserData.LAST_NAME] + "\"";
					allTimeLogRecord += ",";
					allTimeLogRecord += "," + "\"" + user[UserData.PASSWORD] + "\"";
					allTimeLogRecord += "," + "\"" + user[UserData.IP_ADDRESSES] + "\"";
					allTimeLogRecord += "," + "Yes";
					allTimeLogRecord += "," + "\"" + user[UserData.PROJECTS] + "\"";
					allTimeLogRecord += "," + "\"" + user[UserData.GROUPS] + "\"";
				}
				else {
					logWithTimeLn("  " + action + " " + ((ModifyUserLogEntry) logEntry).getUserName() + "FAILED: User does not exist.");
				}
			}
			else if (logEntry.isAddUserLogEntry()) {
				String userName = ((AddUserLogEntry) logEntry).getUserName();
				String[] user = userData.getUser(userName);
				if (user != null) {
					action = "Add User";
					parameters.add(user[UserData.FIRST_NAME]);
					parameters.add(user[UserData.INITIALS]);
					parameters.add(user[UserData.LAST_NAME]);
					parameters.add(user[UserData.USER_NAME]);
					parameters.add(user[UserData.PASSWORD]);
					parameters.add(user[UserData.EMAIL]);
					parameters.add(user[UserData.PROJECTS]);
					parameters.add(user[UserData.GROUPS]);
					parameters.add("0"); // Update Flag
					script = "createUserProjects.vbs";

					allTimeLogRecord += "Run " + action + " Script";
					allTimeLogRecord += ",";
					allTimeLogRecord += "," + "\"" + user[UserData.USER_NAME] + "\"";
					allTimeLogRecord += "," + "\"" + user[UserData.FIRST_NAME] + "\"";
					allTimeLogRecord += "," + "\"" + user[UserData.LAST_NAME] + "\"";
					allTimeLogRecord += ",";
					allTimeLogRecord += "," + "\"" + user[UserData.PASSWORD] + "\"";
					allTimeLogRecord += "," + "\"" + user[UserData.IP_ADDRESSES] + "\"";
					allTimeLogRecord += "," + "Yes";
					allTimeLogRecord += "," + "\"" + user[UserData.PROJECTS] + "\"";
					allTimeLogRecord += "," + "\"" + user[UserData.GROUPS] + "\"";
				}
				else {
					logWithTimeLn("  " + action + " " + ((AddUserLogEntry) logEntry).getUserName() + "FAILED: User does not exist.");
				}
			}

			parameters.add(fullLogFileName);
			String logLine = "  " + action;
			String paramatersLogLn = "";
			for (String parameter : parameters) {
				paramatersLogLn += " \"" + parameter + "\"";
			}
			logWithTimeLn(logLine + paramatersLogLn);
			
			if (RREManager.test) {
				parameters.add(0, action);
				script = "testScript.vbs";
			}
			
			if (script != null) {
				logWithTimeLn("    " + script + paramatersLogLn);
				if (ScriptUtilities.callVBS(script, parameters)) {
					allTimeLogRecord += "," + "Succeeded";
					allTimeLogRecord += ",";
					allTimeLog(allTimeLogRecord, script);
					logWithTimeLn("  SUCCEEDED");
				}
				else {
					allTimeLogRecord += "," + "Failed";
					allTimeLogRecord += ",";
					allTimeLog(allTimeLogRecord, script);
					logWithTimeLn("  FAILED");
					success = false;
					break;
				}
			}
			else {
				logWithTimeLn("    No script specified.");
			}
		}
		if (success) {
			logWithTimeLn("Running scripts finished successfully");
		}
		else {
			logWithTimeLn("Running scripts finished with errors");
			logWithTimeLn("CHECK ACTIVE DIRECTORY STATUS!");
		}
		
		return success;
	}

}
