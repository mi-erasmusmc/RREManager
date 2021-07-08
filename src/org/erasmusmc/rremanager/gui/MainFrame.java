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
import java.util.List;

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
	private UsersTab usersTab;
	private ProjectsTab projectsTab;
	private JPanel logPanel;
	private Console console;
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
	
	
	public MainFrame(RREManager rreManager, List<String[]> users) {
		this.rreManager = rreManager;
		createInterface(users);
	}
	
	
	private void createInterface(List<String[]> users) {
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
		frame.setTitle("RRE User Manager v" + RREManager.version);
		MainFrame.setIcon(frame);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		RREManager.disableWhenRunning(tabbedPane);
		
		usersTab = new UsersTab(rreManager, this, users);
		projectsTab = new ProjectsTab(rreManager, this);
		
		tabbedPane.addTab("Users", usersTab);        		
		tabbedPane.addTab("Projects", projectsTab);

        logPanel = new JPanel(new BorderLayout());
        logPanel.setMinimumSize(new Dimension(700, 200));
        logPanel.setPreferredSize(new Dimension(700, 200));
        logPanel.add(createConsolePanel(), BorderLayout.CENTER);
        
        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.add(logPanel, BorderLayout.SOUTH);
        
        frame.setVisible(true);

		if (!RREManager.inEclipse) {
	        setLogFile();
		}
	}
	
	
	private JScrollPane createConsolePanel() {
		JTextArea consoleArea = new JTextArea();
		consoleArea.setToolTipText("General progress information");
		consoleArea.setEditable(false);
		console = new Console();
		console.setTextArea(consoleArea);
		if (!(System.getProperty("runInEclipse") == null ? false : System.getProperty("runInEclipse").equalsIgnoreCase("true"))) {
			System.setOut(new PrintStream(console));
			System.setErr(new PrintStream(console));
		}
		JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
		consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console"));
		consoleScrollPane.setAutoscrolls(true);
		return consoleScrollPane;
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
			allTimeLogFileName = RREManager.noLogging ? null : (RREManager.getIniFile().getValue("General", "Log Folder") + File.separator + "RREManagerLog.csv");
			String allTimeLogHeader = "Date";
			allTimeLogHeader += "," + "Time";
			allTimeLogHeader += "," + "Action";
			allTimeLogHeader += "," + "Recipient";
			allTimeLogHeader += "," + "User";
			allTimeLogHeader += "," + "First Name";
			allTimeLogHeader += "," + "Last Name";
			allTimeLogHeader += "," + "Password";
			allTimeLogHeader += "," + "IP-addresses";
			allTimeLogHeader += "," + "Approved";
			allTimeLogHeader += "," + "Result";
			allTimeLogHeader += "," + "Error";
			allTimeLogHeader += "," + "Log File";
			allTimeLogHeader += "," + "Attachments";
			allTimeLogFile = RREManager.noLogging ? null : new File(allTimeLogFileName);
			if (RREManager.noLogging || allTimeLogFile.exists() || allTimeLog(allTimeLogHeader)) {
				logWithTimeLn("RRE Manager v" + RREManager.version);
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
	
	
	public boolean allTimeLog(String record) {
		boolean success = true;
		if (!RREManager.noLogging) {
			try {
				FileWriter logWriter = new FileWriter(new File(allTimeLogFileName), true);
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

}
