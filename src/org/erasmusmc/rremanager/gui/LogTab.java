package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
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
import org.erasmusmc.rremanager.files.LogData;


public class LogTab extends MainFrameTab {
	private static final long serialVersionUID = -7204677944893089481L;
	

	private LogData logData = null;

	private JPanel logPanel;
	private JPanel logListPanel;
	private JPanel logListTablePanel = null;
	private JScrollPane logTableScrollPane = null;
	private List<String[]> log = new ArrayList<String[]>();
	private LogTableModel logTableModel;
	private JTable logTable;
	private TableRowSorter<? extends TableModel> rowSorter;
	private JPanel logFilePanel;

	
	public LogTab(RREManager rreManager, MainFrame mainFrame) {
		super(rreManager, mainFrame);
		
		setLayout(new BorderLayout());
		
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
                	// TODO
                }

                if (logTable.getRowCount() > 0) {
            		ListSelectionModel selectionModel = logTable.getSelectionModel();
            		selectionModel.setSelectionInterval(0, 0);
                }
             }
		});
		searchPanel.add(searchLabel);
		searchPanel.add(searchField);

		logPanel = new JPanel(new BorderLayout());
        
        logListPanel = new JPanel(new BorderLayout()); 
		refresh();
       
        logFilePanel = new JPanel(new BorderLayout());
        logFilePanel.setBorder(BorderFactory.createTitledBorder("Logfile"));
		logFilePanel.setMinimumSize(new Dimension(100, 200));
		logFilePanel.setMinimumSize(new Dimension(100, 200));
		logFilePanel.setMaximumSize(new Dimension(10000, 200));
        logListPanel.add(logFilePanel, BorderLayout.SOUTH);
        
        add(searchPanel, BorderLayout.NORTH);
		add(logPanel, BorderLayout.CENTER);
		logPanel.add(logListPanel, BorderLayout.NORTH);
		logPanel.add(logFilePanel, BorderLayout.CENTER);
	}
	
	
	public void refresh() {
		logData = new LogData(rreManager.getAllTimeLogFileName());
		log = logData.getLog();
		if (logListTablePanel != null) {
			logListPanel.remove(logListTablePanel);
		}

		logListTablePanel = new JPanel(new BorderLayout());
		logListTablePanel.setMinimumSize(new Dimension(100, 400));
		logListTablePanel.setMaximumSize(new Dimension(10000, 400));
		logListTablePanel.setPreferredSize(new Dimension(100, 400));
		logTable = getLogTable();
		logTableScrollPane = new JScrollPane(logTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		logListTablePanel.add(logTableScrollPane, BorderLayout.CENTER);
		logListPanel.add(logListTablePanel, BorderLayout.NORTH);
		logListPanel.validate();
	}
	
	
	private void showLogFile(int logNr) {
		String logFileName = log.get(logNr)[LogData.LOG_FILE];
		logFilePanel.removeAll();
		if (logFileName != null) {
			logFilePanel.setBorder(BorderFactory.createTitledBorder("Logfile " + logFileName));
			JPanel logTextPanel = new JPanel(new BorderLayout());
			
			JTextArea logTextArea = new JTextArea(getLogText(logFileName));
			logTextPanel.add(logTextArea, BorderLayout.CENTER);
			
	        JScrollPane logFileScrollPane = new JScrollPane(logTextPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	        logFilePanel.add(logFileScrollPane, BorderLayout.CENTER);
		}
        logFilePanel.validate();
	}
	
	
	private JTable getLogTable() {
		logTableModel = new LogTableModel();
		JTable logTable = new JTable(logTableModel) {
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
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		DefaultTableCellRenderer rightAlignmentRenderer = new DefaultTableCellRenderer();
		rightAlignmentRenderer.setHorizontalAlignment(JLabel.RIGHT);
		
		logTable.setAutoCreateRowSorter(true);
		rowSorter = (TableRowSorter<? extends TableModel>) logTable.getRowSorter();

		// Set selection to first row
		ListSelectionModel selectionModel = logTable.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		selectionModel.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int selection = logTable.getSelectedRow();
					if (selection != -1) {
						showLogFile(logTable.convertRowIndexToModel(selection));
					}
				}
			}
		});
		
		// Date
		logTable.getColumnModel().getColumn(LogData.DATE).setMinWidth(75);
		logTable.getColumnModel().getColumn(LogData.DATE).setMaxWidth(75);
		logTable.getColumnModel().getColumn(LogData.DATE).setPreferredWidth(75);
		
		// Time
		logTable.getColumnModel().getColumn(LogData.TIME).setMinWidth(60);
		logTable.getColumnModel().getColumn(LogData.TIME).setMaxWidth(60);
		logTable.getColumnModel().getColumn(LogData.TIME).setPreferredWidth(60);
		
		// Administrator
		logTable.getColumnModel().getColumn(LogData.ADMINISTRATOR).setMinWidth(120);
		logTable.getColumnModel().getColumn(LogData.ADMINISTRATOR).setMaxWidth(200);
		logTable.getColumnModel().getColumn(LogData.ADMINISTRATOR).setPreferredWidth(120);
		
		// Action
		logTable.getColumnModel().getColumn(LogData.ACTION).setMinWidth(140);
		logTable.getColumnModel().getColumn(LogData.ACTION).setMaxWidth(300);
		logTable.getColumnModel().getColumn(LogData.ACTION).setPreferredWidth(140);
		
		// Recipient
		logTable.getColumnModel().getColumn(LogData.RECIPIENT).setMinWidth(80);
		logTable.getColumnModel().getColumn(LogData.RECIPIENT).setMaxWidth(300);
		logTable.getColumnModel().getColumn(LogData.RECIPIENT).setPreferredWidth(80);
		
		// User Name
		logTable.getColumnModel().getColumn(LogData.USER).setMinWidth(80);
		logTable.getColumnModel().getColumn(LogData.USER).setMaxWidth(150);
		logTable.getColumnModel().getColumn(LogData.USER).setPreferredWidth(80);
		
		// First Name
		logTable.getColumnModel().getColumn(LogData.FIRST_NAME).setMinWidth(75);
		logTable.getColumnModel().getColumn(LogData.FIRST_NAME).setMaxWidth(150);
		logTable.getColumnModel().getColumn(LogData.FIRST_NAME).setPreferredWidth(75);
		
		// Last Name
		logTable.getColumnModel().getColumn(LogData.LAST_NAME).setMinWidth(80);
		logTable.getColumnModel().getColumn(LogData.LAST_NAME).setMaxWidth(150);
		logTable.getColumnModel().getColumn(LogData.LAST_NAME).setPreferredWidth(80);
		
		// Password
		logTable.getColumnModel().getColumn(LogData.PASSWORD).setMinWidth(80);
		logTable.getColumnModel().getColumn(LogData.PASSWORD).setMaxWidth(100);
		logTable.getColumnModel().getColumn(LogData.PASSWORD).setPreferredWidth(80);
		
		// IP-addresses
		logTable.getColumnModel().getColumn(LogData.IP_ADDRESSES).setMinWidth(80);
		logTable.getColumnModel().getColumn(LogData.IP_ADDRESSES).setMaxWidth(300);
		logTable.getColumnModel().getColumn(LogData.IP_ADDRESSES).setPreferredWidth(80);
		
		// Approved
		logTable.getColumnModel().getColumn(LogData.APPROVED).setMinWidth(60);
		logTable.getColumnModel().getColumn(LogData.APPROVED).setMaxWidth(60);
		logTable.getColumnModel().getColumn(LogData.APPROVED).setPreferredWidth(60);
		
		// Result
		logTable.getColumnModel().getColumn(LogData.RESULT).setMinWidth(70);
		logTable.getColumnModel().getColumn(LogData.RESULT).setMaxWidth(70);
		logTable.getColumnModel().getColumn(LogData.RESULT).setPreferredWidth(70);
/*
		// Error
		logTable.getColumnModel().getColumn(LogData.ERROR).setMinWidth(80);
		logTable.getColumnModel().getColumn(LogData.ERROR).setMaxWidth(300);
		logTable.getColumnModel().getColumn(LogData.ERROR).setPreferredWidth(80);
		
		// Log File
		logTable.getColumnModel().getColumn(LogData.LOG_FILE).setMinWidth(80);
		logTable.getColumnModel().getColumn(LogData.LOG_FILE).setMaxWidth(300);
		logTable.getColumnModel().getColumn(LogData.LOG_FILE).setPreferredWidth(80);
		
		// Attachments
		logTable.getColumnModel().getColumn(LogData.ATTACHMENTS).setMinWidth(80);
		logTable.getColumnModel().getColumn(LogData.ATTACHMENTS).setMaxWidth(300);
		logTable.getColumnModel().getColumn(LogData.ATTACHMENTS).setPreferredWidth(80);
*/
		RREManager.disableWhenRunning(logTable);
		
		return logTable;
	}
	
	
	private String getLogText(String logFileName) {
		String logText = "No logfile available.";
		
		if (logFileName != null) {
			logText = "Cannot read logfile.";
			
			File logFile = new File(logFileName);
			if (logFile.canRead()) {
				try {
					BufferedReader logFileReader = new BufferedReader(new FileReader(logFile));
					logText = "";
					String line = logFileReader.readLine();
					while (line != null) {
						logText += (logText.equals("") ? "" : "\r\n") + line;
						line = logFileReader.readLine();
					}
					logFileReader.close();
				} catch (FileNotFoundException e) {
					logText = "Cannot open logfile.";
				} catch (IOException e) {
					logText = "Error while reading logfile.";
				}
			}
		}
		
		return logText;
	}
	
	
	private class LogTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -2026827800711156459L;
		
		private String[] columnNames = new String[] {
				"Date",
				"Time",
				"Administrator",
				"Action",
				"Recipient",
				"User",
				"First Name",
				"Last Name",
				"Password",
				"IP-addresses",
				"Approved",
				"Result"/*,
				"Error",
				"Log File",
				"Attachments"*/
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
			return log.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			return log.get(row)[column];
		}
	     
	    @Override
	    public Class<?> getColumnClass(int columnIndex) {
	        if (log.isEmpty()) {
	            return Object.class;
	        }
	        return getValueAt(0, columnIndex).getClass();
	    }
		
	}

}
