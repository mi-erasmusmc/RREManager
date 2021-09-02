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
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.utilities.DateUtilities;


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
	
	
	private void createInterface() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	String busy = isBusy();
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
		logTab      = new LogTab(rreManager, this);
		
		if (usersTab    != null) tabbedPane.addTab("Users"   , usersTab);
		if (specialsTab != null) tabbedPane.addTab("Specials", specialsTab);        		
		if (projectsTab != null) tabbedPane.addTab("Projects", projectsTab);
		if (logTab      != null) tabbedPane.addTab("Log"     , logTab);
		
        frame.add(tabbedPane, BorderLayout.CENTER);
        
        frame.setVisible(true);

		if (!RREManager.inEclipse) {
	        setLogFile();
		}
	}
	
	
	public void refreshLog() {
		logTab.refresh();
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
				if (!RREManager.inEclipse) {
					logLn("");
					logLn("");
					logLn(RREManager.getIniFile().getFileName() + ":");
					logLn("--------------------------------------------------------------------------------------");
					RREManager.getIniFile().writeFile(System.out);
					logLn("--------------------------------------------------------------------------------------");
					logLn("");
				}
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
				record += "," + info;
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

}
