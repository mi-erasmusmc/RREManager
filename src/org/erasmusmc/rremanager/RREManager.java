package org.erasmusmc.rremanager;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.erasmusmc.rremanager.changelog.ChangeLog;
import org.erasmusmc.rremanager.files.AdministratorData;
import org.erasmusmc.rremanager.files.IniFile;
import org.erasmusmc.rremanager.gui.AdministratorDefiner;
import org.erasmusmc.rremanager.gui.AdministratorManager;
import org.erasmusmc.rremanager.gui.EmailEditor;
import org.erasmusmc.rremanager.gui.IPAddressSelector;
import org.erasmusmc.rremanager.gui.MainFrame;
import org.erasmusmc.rremanager.gui.ProjectDefiner;
import org.erasmusmc.rremanager.gui.UserDefiner;
import org.erasmusmc.rremanager.gui.PasswordManager;

public class RREManager {
	public static String version = "2.2.3";
	public static boolean test = true;
	public static boolean noLogging = false;
	public static boolean loggingStarted = false;
	public static ChangeLog changeLog = new ChangeLog();

	private static boolean error = false;
	private static Set<JComponent> componentsToDisableWhenRunning = new HashSet<JComponent>();
	private static Map<JComponent, Boolean> componentsStatusBeforeRun = new HashMap<JComponent, Boolean>();
	
	private static String currentPath = null;
	private static IniFile iniFile = null;
	
	private AdministratorManager administratorSelector;
	private PasswordManager passwordManager;
	private IPAddressSelector ipAddressSelector;
	private EmailEditor emailEditor;
	private ProjectDefiner projectDefiner = null;
	private UserDefiner userDefiner = null;
	private AdministratorDefiner administratorDefiner = null;
	private MainFrame mainFrame = null;
	
	private String administrator = null;
	
	
	public static IniFile getIniFile() {
		return iniFile;
	}
	
	
	public static void disableWhenRunning(JComponent component) {
		componentsToDisableWhenRunning.add(component);
	}
	
	
	public static void disableComponents() {
		componentsStatusBeforeRun.clear();
		for (JComponent component : componentsToDisableWhenRunning) {
			componentsStatusBeforeRun.put(component, component.isEnabled());
			component.setEnabled(false);
		}
	}
	
	
	public static void enableComponents() {
		for (JComponent component : componentsToDisableWhenRunning) {
			component.setEnabled(componentsStatusBeforeRun.get(component));
		}
	}
	
	
	public RREManager(Map<String, String> parameters) {
		if (setCurrentPath()) {
			iniFile = new IniFile(parameters.keySet().contains("settings") ? parameters.get("settings") : (currentPath + File.separator + "RREManager-v" + version + ".ini"));
			if (iniFile.readFile()) {
				test = (!iniFile.getValue("General", "DataFile").equals("F:\\Administration\\Users\\UsersProjects.xlsx"));
				if ((!iniFile.hasGroup("Administrators")) || (iniFile.getGroup("Administrators").keySet().size() == 0)) {
					administratorDefiner = new AdministratorDefiner(null);
					String[] firstAdministrator = administratorDefiner.getAdministrator(null);
					if (firstAdministrator != null) {
						AdministratorData.addAdministratorToIniFile(firstAdministrator, iniFile, null);
					}
				}
				administratorSelector = new AdministratorManager(null);
				administrator = administratorSelector.login();
				if (administrator != null) {
					mainFrame = new MainFrame(this);
					mainFrame.setTitle(administrator);
					passwordManager = new PasswordManager(mainFrame.getFrame());
					ipAddressSelector = new IPAddressSelector(mainFrame.getFrame());
					emailEditor = new EmailEditor(mainFrame.getFrame());
					projectDefiner = new ProjectDefiner(mainFrame.getFrame());
					userDefiner = new UserDefiner(mainFrame.getFrame());
					administratorDefiner = new AdministratorDefiner(mainFrame.getFrame());
				}
				else {
		            System.exit(0);
				}
			}
			else {
				JOptionPane.showMessageDialog(null, iniFile.getError(), "RREManager Error", JOptionPane.ERROR_MESSAGE);
			}			
		}
	}
	
	
	public void raiseErrorFlag() {
		error = true;
	}
	
	
	public boolean errorOccurred() {
		return error;
	}
	
	
	public boolean setCurrentPath() {
		boolean result = false;
		try {
			currentPath = (new File(RREManager.class.getProtectionDomain().getCodeSource().getLocation().toURI())).getParent();
			result = true;
		} catch (URISyntaxException e) {
			JOptionPane.showMessageDialog(null, "Cannot determine location of RREManager.ini file.", "RREManager Error", JOptionPane.ERROR_MESSAGE);
		}
		return result;
	}
	
	
	public static String getCurentPath() {
		return ((currentPath == null) || test) ? "D:\\Temp\\RRE\\RREManagerTestLog" : currentPath;
	}
	
	
	public String getAdministrator() {
		return administrator;
	}
	
	
	public String getAllTimeLogFileName() {
		String allTimeLogFileName = null;
		if (getIniFile() != null) {
			allTimeLogFileName = noLogging ? null : (getIniFile().getValue("General", "Log Folder") + File.separator + "RREManagerLog.csv");
		}
		return allTimeLogFileName;
	}
	
	
	public ProjectDefiner getProjectDefiner() {
		return projectDefiner;
	}
	
	
	public UserDefiner getUserDefiner() {
		return userDefiner;
	}
	
	
	public AdministratorManager getAdministratorSelector() {
		return administratorSelector;
	}
	
	
	public AdministratorDefiner getAdministratorDefiner() {
		return administratorDefiner;
	}
	
	
	public PasswordManager getPasswordManager() {
		return passwordManager;
	}
	
	
	public EmailEditor getEmailEditor() {
		return emailEditor;
	}
	
	
	public IPAddressSelector getIPAddressSelector() {
		return ipAddressSelector;
	}
	

	public static void main(String[] args) {
		Map<String, String> parameters = new HashMap<String, String>();

		for (int i = 0; i < args.length; i++) {
			int equalSignIndex = args[i].indexOf("=");
			String argVariable = args[i].toLowerCase();
			String value = "";
			if (equalSignIndex != -1) {
				argVariable = args[i].substring(0, equalSignIndex).toLowerCase();
				value = args[i].substring(equalSignIndex + 1);
			}
			parameters.put(argVariable, value);
		}
		
		new RREManager(parameters);
	}

}
