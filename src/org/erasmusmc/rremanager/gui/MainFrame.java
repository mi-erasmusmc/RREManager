package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.files.UserData;
import org.erasmusmc.rremanager.utilities.DateUtilities;


public class MainFrame {
	
	private static final String ICON = "/org/erasmusmc/rremanager/gui/Octopus 48x48.png";
	
	private RREManager rreManager;
	private String logFolder;
	private String fullLogFileName;
	private String allTimeLogFileName = null;
	private File allTimeLogFile = null;
	
	private JFrame frame;
	private JPanel usersPanel;
	private JPanel usersListPanel;
	private JPanel detailsPanel;
	private Console console;
	private JButton sendAccountsButton;
	private JButton sendPasswordsButton;
	private JButton sendFirewallAddRequestButton;
	private JButton sendFirewallRemoveRequestButton;
	private String busy = null; 
	private List<String[]> users = new ArrayList<String[]>();
	private UsersTableModel usersTableModel;
	private JTable usersTable;
	private TableRowSorter<? extends TableModel> rowSorter;
	private int[] selectedUsers;

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
		this.users = users;
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
		frame.setTitle("RRE User Manager v" + RREManager.version);
		MainFrame.setIcon(frame);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		
		usersPanel = new JPanel(new BorderLayout());
		usersPanel.setBorder(BorderFactory.createTitledBorder("Users"));
		
		usersListPanel = new JPanel(new BorderLayout());
		
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel searchLabel = new JLabel("Search: ");
		JTextField searchField = new JTextField(20);
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				filter();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				filter();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				filter();
			}
			
			private void filter() {
                String text = searchField.getText();
                if (text.length() == 0) {
                    rowSorter.setRowFilter(null);
                }
                else {
                    rowSorter.setRowFilter(RowFilter.regexFilter(text));
                }
                
                if (rowSorter.getViewRowCount() == 0) {
                	/* TODO
                	drugMappingLogPanel.removeAll();
                	drugMappingResultPanel.removeAll();
            		mainFrame.getFrame().repaint();
            		lastSelectedSourceDrug = null;
            		lastSelectedLogRecord = null;
            		*/
                }

                if (usersTable.getRowCount() > 0) {
            		ListSelectionModel selectionModel = usersTable.getSelectionModel();
            		selectionModel.setSelectionInterval(0, 0);
                }
             }
		});
		searchPanel.add(searchLabel);
		searchPanel.add(searchField);
		
		usersTableModel = new UsersTableModel();
		usersTable = new JTable(usersTableModel) {
			private static final long serialVersionUID = 1L;

			//Implement table cell tool tips.           
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    tip = getValueAt(rowIndex, colIndex).toString();
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }
        };
        usersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		DefaultTableCellRenderer rightAlignmentRenderer = new DefaultTableCellRenderer();
		rightAlignmentRenderer.setHorizontalAlignment(JLabel.RIGHT);
		
		usersTable.setAutoCreateRowSorter(true);
		rowSorter = (TableRowSorter<? extends TableModel>) usersTable.getRowSorter();

		// Set selection to first row
		ListSelectionModel selectionModel = usersTable.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		selectionModel.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int[] selection = usersTable.getSelectedRows();
					if (selection.length > 0) {
						sendAccountsButton.setEnabled(true);
						sendPasswordsButton.setEnabled(true);
						sendFirewallAddRequestButton.setEnabled(true);
						sendFirewallRemoveRequestButton.setEnabled(true);
						List<Integer> realSelection = new ArrayList<Integer>();
						for (int selectionIndex : selection) {
							int realIndex = usersTable.convertRowIndexToModel(selectionIndex);
							realSelection.add(realIndex);
						}
						showInfo(realSelection);
					}
					else {
						sendAccountsButton.setEnabled(false);
						sendPasswordsButton.setEnabled(false);
						sendFirewallAddRequestButton.setEnabled(false);
						sendFirewallRemoveRequestButton.setEnabled(false);
					}
					selectedUsers = new int[selection.length];
					for (int nr = 0; nr < selection.length; nr++) {
						selectedUsers[nr] = usersTable.convertRowIndexToModel(selection[nr]);
					}
				}
			}
		});
		
		// First Name
		usersTable.getColumnModel().getColumn(UserData.FIRST_NAME).setMinWidth(80);
		usersTable.getColumnModel().getColumn(UserData.FIRST_NAME).setMaxWidth(150);
		usersTable.getColumnModel().getColumn(UserData.FIRST_NAME).setPreferredWidth(80);
		
		// Initials
		usersTable.getColumnModel().getColumn(UserData.INITIALS).setMinWidth(50);
		usersTable.getColumnModel().getColumn(UserData.INITIALS).setMaxWidth(80);
		usersTable.getColumnModel().getColumn(UserData.INITIALS).setPreferredWidth(50);
		
		// Last Name
		usersTable.getColumnModel().getColumn(UserData.LAST_NAME).setMinWidth(80);
		usersTable.getColumnModel().getColumn(UserData.LAST_NAME).setMaxWidth(150);
		usersTable.getColumnModel().getColumn(UserData.LAST_NAME).setPreferredWidth(80);
		
		// User Name
		usersTable.getColumnModel().getColumn(UserData.USER_NAME).setMinWidth(80);
		usersTable.getColumnModel().getColumn(UserData.USER_NAME).setMaxWidth(150);
		usersTable.getColumnModel().getColumn(UserData.USER_NAME).setPreferredWidth(80);
/*		
		// Password
		usersTable.getColumnModel().getColumn(UserData.PASSWORD).setMinWidth(80);
		usersTable.getColumnModel().getColumn(UserData.PASSWORD).setMaxWidth(100);
		usersTable.getColumnModel().getColumn(UserData.PASSWORD).setPreferredWidth(80);
		
		// Email
		usersTable.getColumnModel().getColumn(UserData.EMAIL).setMinWidth(80);
		usersTable.getColumnModel().getColumn(UserData.EMAIL).setMaxWidth(300);
		usersTable.getColumnModel().getColumn(UserData.EMAIL).setPreferredWidth(80);
		
		// Email Type
		usersTable.getColumnModel().getColumn(UserData.EMAIL_FORMAT).setMinWidth(80);
		usersTable.getColumnModel().getColumn(UserData.EMAIL_FORMAT).setMaxWidth(80);
		usersTable.getColumnModel().getColumn(UserData.EMAIL_FORMAT).setPreferredWidth(80);
		
		// Access
		usersTable.getColumnModel().getColumn(UserData.ACCESS).setMinWidth(60);
		usersTable.getColumnModel().getColumn(UserData.ACCESS).setMaxWidth(60);
		usersTable.getColumnModel().getColumn(UserData.ACCESS).setPreferredWidth(60);
		
		// MultiOTP
		usersTable.getColumnModel().getColumn(UserData.MULTIOTP).setMinWidth(60);
		usersTable.getColumnModel().getColumn(UserData.MULTIOTP).setMaxWidth(60);
		usersTable.getColumnModel().getColumn(UserData.MULTIOTP).setPreferredWidth(60);
*/		
		RREManager.disableWhenRunning(usersTable);
		
		usersPanel.add(searchPanel, BorderLayout.NORTH);

		JScrollPane usersListScrollPane = new JScrollPane(usersTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        usersListPanel.add(usersListScrollPane, BorderLayout.CENTER);
        
        detailsPanel = new JPanel(new BorderLayout());
        
        usersPanel.add(usersListPanel, BorderLayout.WEST);
        usersPanel.add(detailsPanel, BorderLayout.CENTER);
        
        
        
        JPanel buttonLogPanel = new JPanel(new BorderLayout());
        buttonLogPanel.setMinimumSize(new Dimension(700, 200));
        buttonLogPanel.setPreferredSize(new Dimension(700, 200));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        sendAccountsButton = new JButton("Send Accounts");
		sendAccountsButton.setEnabled(false);
		sendAccountsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RREManager.disableComponents();
				rreManager.sendAccountInformation(selectedUsers);
				RREManager.enableComponents();
			}
		});
		RREManager.disableWhenRunning(sendAccountsButton);
		
        sendPasswordsButton = new JButton("Send Passwords");
		sendPasswordsButton.setEnabled(false);
		sendPasswordsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RREManager.disableComponents();
				rreManager.sendPasswords(selectedUsers);
				RREManager.enableComponents();
			}
		});
		RREManager.disableWhenRunning(sendPasswordsButton);
		
        sendFirewallAddRequestButton = new JButton("Firewall Add Request");
		sendFirewallAddRequestButton.setEnabled(false);
		sendFirewallAddRequestButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RREManager.disableComponents();
				rreManager.sendFirewallAddRequest(selectedUsers);
				RREManager.enableComponents();
			}
		});
		RREManager.disableWhenRunning(sendFirewallAddRequestButton);
		
        sendFirewallRemoveRequestButton = new JButton("Firewall Remove Request");
		sendFirewallRemoveRequestButton.setEnabled(false);
		sendFirewallRemoveRequestButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RREManager.disableComponents();
				rreManager.sendFirewallRemoveRequest(selectedUsers);
				RREManager.enableComponents();
			}
		});
		RREManager.disableWhenRunning(sendFirewallRemoveRequestButton);
		
        buttonPanel.add(sendAccountsButton);
        buttonPanel.add(sendPasswordsButton);
        buttonPanel.add(sendFirewallAddRequestButton);
        buttonPanel.add(sendFirewallRemoveRequestButton);
        
        buttonLogPanel.add(buttonPanel, BorderLayout.NORTH);
        buttonLogPanel.add(createConsolePanel(), BorderLayout.CENTER);
        		
        frame.add(usersPanel, BorderLayout.CENTER);
        frame.add(buttonLogPanel, BorderLayout.SOUTH);
        
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
	
	
	private class UsersTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -2026827800711156459L;
		
		private String[] columnNames = new String[] {
				"First Name",
				"Initials",
				"Last Name",
				"User Name"/*,
				"Password",
				"Email address",
				"Email Format",
				"Access",
				"MultiOTP"*/
		};

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public int getRowCount() {
			return users.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			return users.get(row)[column];
		}
	     
	    @Override
	    public Class<?> getColumnClass(int columnIndex) {
	        if (users.isEmpty()) {
	            return Object.class;
	        }
	        return getValueAt(0, columnIndex).getClass();
	    }
		
	}
	
	
	private void showInfo(List<Integer> selection) {
		final int rowLabelWidth = 150;
		final int rowHeight = 20;
		
		final int[] itemsToShow = new int[] {
				UserData.USER_NAME,
				UserData.PASSWORD,
				UserData.EMAIL,
				UserData.EMAIL_FORMAT,
				UserData.IP_ADDRESSES,
				UserData.ACCESS,
				UserData.PROJECTS,
				UserData.GROUPS,
				UserData.MULTIOTP
		};
		
		detailsPanel.removeAll();
		JPanel usersDetailPanel = new JPanel(new BorderLayout());
		JPanel currentPanel = usersDetailPanel;
		for (int userNr : selection) {
			String[] user = users.get(userNr);
			JPanel userPanel = new JPanel(new BorderLayout());
			JLabel nameLabel = new JLabel(UserData.getUserDescription(user, false));
			nameLabel.setFont(new Font(nameLabel.getFont().getName(), Font.BOLD, 18));
			userPanel.add(nameLabel, BorderLayout.NORTH);
			
			JPanel parentPanel = userPanel;
			JPanel levelPanel;
			JPanel rowPanel;
			JPanel rowLabelPanel;
			JLabel rowLabel;
			JLabel rowValueLabel;
			JPanel nextPanel;

			for (int item : itemsToShow) {
				levelPanel = new JPanel(new BorderLayout());
				rowPanel = new JPanel();
				rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
				
				rowLabelPanel = new JPanel(new BorderLayout());
				rowLabel = new JLabel("    " + UserData.fieldName[item] + ":");
				rowLabel.setMinimumSize(new Dimension(rowLabelWidth, rowHeight));
				rowLabel.setMaximumSize(new Dimension(rowLabelWidth, rowHeight));
				rowLabel.setPreferredSize(new Dimension(rowLabelWidth, rowHeight));
				rowLabelPanel.add(rowLabel, BorderLayout.CENTER);
				
				rowValueLabel = new JLabel(user[item]);
				rowValueLabel.setFont(rowValueLabel.getFont().deriveFont(Font.PLAIN));
				rowValueLabel.setMinimumSize(new Dimension(10, rowHeight));
				rowValueLabel.setPreferredSize(new Dimension(10000, rowHeight));

				rowPanel.add(rowLabelPanel);
				rowPanel.add(rowValueLabel);
				
				levelPanel.add(rowPanel, BorderLayout.NORTH);
				parentPanel.add(levelPanel, BorderLayout.CENTER);
				parentPanel = levelPanel;
			}
			
			currentPanel.add(userPanel, BorderLayout.NORTH);
			nextPanel = new JPanel(new BorderLayout());
			currentPanel.add(nextPanel, BorderLayout.CENTER);
			currentPanel = nextPanel;
			
			// Create blank space between users
			JPanel blankSpacePanel = new JPanel();
			blankSpacePanel.setMinimumSize(new Dimension(10, rowHeight));
			blankSpacePanel.setPreferredSize(new Dimension(10000, rowHeight));
			
			currentPanel.add(blankSpacePanel, BorderLayout.NORTH);
			nextPanel = new JPanel(new BorderLayout());
			currentPanel.add(nextPanel, BorderLayout.CENTER);
			currentPanel = nextPanel;
		}
        JScrollPane usersDetailScrollPane = new JScrollPane(usersDetailPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailsPanel.add(usersDetailScrollPane, BorderLayout.CENTER);
        detailsPanel.validate();
	}
	
	
	public int[] getSelectedUsers() {
		return selectedUsers;
	}
	
	
	public String getLogFileName() {
		return fullLogFileName;
	}

}
