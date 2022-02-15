package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import org.erasmusmc.rremanager.files.AdministratorData;

public class AdministratorsTab extends MainFrameTab {
	private static final long serialVersionUID = -7615290258243259587L;
	

	private AdministratorData administratorData = null; 
	
	private JPanel administratorsListPanel;
	private JPanel detailsPanel;
	private JButton editAdministratorButton;
	private JButton addAdministratorButton;
	
	private List<String[]> administrators = new ArrayList<String[]>();
	private AdministratorsTableModel administratorsTableModel;
	private JTable administratorsTable;
	private TableRowSorter<? extends TableModel> rowSorter;
	private int[] selectedAdministrators;

	public AdministratorsTab(RREManager rreManager, MainFrame mainFrame) {
		super(rreManager, mainFrame);
		
		administratorData = new AdministratorData(mainFrame);
		administrators = administratorData.getAdministratorsList();
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder("Administrators"));
		
		administratorsListPanel = new JPanel(new BorderLayout());
		
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
                	// Do nothing
                }

                if (administratorsTable.getRowCount() > 0) {
            		ListSelectionModel selectionModel = administratorsTable.getSelectionModel();
            		selectionModel.setSelectionInterval(0, 0);
                }
             }
		});
		searchPanel.add(searchLabel);
		searchPanel.add(searchField);
		
		administratorsTableModel = new AdministratorsTableModel();
		administratorsTable = new JTable(administratorsTableModel) {
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
        administratorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		DefaultTableCellRenderer rightAlignmentRenderer = new DefaultTableCellRenderer();
		rightAlignmentRenderer.setHorizontalAlignment(JLabel.RIGHT);
		
		administratorsTable.setAutoCreateRowSorter(true);
		rowSorter = (TableRowSorter<? extends TableModel>) administratorsTable.getRowSorter();

		// Set selection to first row
		ListSelectionModel selectionModel = administratorsTable.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		selectionModel.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int[] selection = administratorsTable.getSelectedRows();
					if (selection.length > 0) {
						showSelection();
					}
					else {
						editAdministratorButton.setEnabled(false);
						addAdministratorButton.setEnabled(true);
					}
					selectedAdministrators = new int[selection.length];
					for (int nr = 0; nr < selection.length; nr++) {
						selectedAdministrators[nr] = administratorsTable.convertRowIndexToModel(selection[nr]);
					}
				}
			}
		});
		
		// Name
		administratorsTable.getColumnModel().getColumn(AdministratorData.NAME).setMinWidth(80);
		administratorsTable.getColumnModel().getColumn(AdministratorData.NAME).setMaxWidth(150);
		administratorsTable.getColumnModel().getColumn(AdministratorData.NAME).setPreferredWidth(80);
		
		// Title
		administratorsTable.getColumnModel().getColumn(AdministratorData.TITLE).setMinWidth(50);
		administratorsTable.getColumnModel().getColumn(AdministratorData.TITLE).setMaxWidth(80);
		administratorsTable.getColumnModel().getColumn(AdministratorData.TITLE).setPreferredWidth(50);
		
		// Telephone Number
		administratorsTable.getColumnModel().getColumn(AdministratorData.PHONE).setMinWidth(80);
		administratorsTable.getColumnModel().getColumn(AdministratorData.PHONE).setMaxWidth(150);
		administratorsTable.getColumnModel().getColumn(AdministratorData.PHONE).setPreferredWidth(80);
		
		// Erasmus MC Number
		administratorsTable.getColumnModel().getColumn(AdministratorData.ERASMUS).setMinWidth(80);
		administratorsTable.getColumnModel().getColumn(AdministratorData.ERASMUS).setMaxWidth(150);
		administratorsTable.getColumnModel().getColumn(AdministratorData.ERASMUS).setPreferredWidth(80);
		
		RREManager.disableWhenRunning(administratorsTable);		

		JScrollPane administratorsListScrollPane = new JScrollPane(administratorsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        administratorsListPanel.add(administratorsListScrollPane, BorderLayout.CENTER);
        
        detailsPanel = new JPanel(new BorderLayout());

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JPanel administratorManagementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		administratorManagementPanel.setBorder(BorderFactory.createTitledBorder("Administrator Management"));
		
        editAdministratorButton = new JButton("Edit Administrator");
        editAdministratorButton.setEnabled(false);
        editAdministratorButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				editAdministrator(selectedAdministrators, administratorData);
				update();
			}
		});
		RREManager.disableWhenRunning(editAdministratorButton);
		
        addAdministratorButton = new JButton("Add Administrator");
		addAdministratorButton.setEnabled(true);
		addAdministratorButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				addAdministrator();
				update();
			}
		});
		RREManager.disableWhenRunning(addAdministratorButton);
		
		administratorManagementPanel.add(editAdministratorButton);
		administratorManagementPanel.add(addAdministratorButton);
		
		actionsPanel.add(administratorManagementPanel);

        JPanel administratorsLogPanel = new JPanel(new BorderLayout());
		administratorsLogPanel.add(administratorsListPanel, BorderLayout.WEST);
		administratorsLogPanel.add(detailsPanel, BorderLayout.CENTER);
		administratorsLogPanel.add(actionsPanel, BorderLayout.SOUTH);
        
		add(searchPanel, BorderLayout.NORTH);
		add(administratorsLogPanel, BorderLayout.CENTER);
		add(mainFrame.createLogPanel(), BorderLayout.SOUTH);
	}
	
	
	public void update() {
		RREManager.getIniFile().readFile();
		administratorData = new AdministratorData(mainFrame);
		administrators = administratorData.getAdministratorsList();
		mainFrame.refreshLog();
		administratorsTableModel.fireTableDataChanged();
		showSelection();
	}
	
	
	private class AdministratorsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -2026827800711156459L;
		
		private String[] columnNames = new String[] {
				"Name",
				"Title",
				"Phone",
				"Erasmus MC Nr"
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
			return administrators.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			return administrators.get(row)[column];
		}
	     
	    @Override
	    public Class<?> getColumnClass(int columnIndex) {
	        if (administrators.isEmpty()) {
	            return Object.class;
	        }
	        return getValueAt(0, columnIndex).getClass();
	    }
		
	}
	
	
	private void editAdministrator(int[] selectedAdministrators, AdministratorData administratorData) {
		if (rreManager.getAdministratorSelector().login() != null) {
			String[] administrator = null;
			if ((selectedAdministrators != null) && (selectedAdministrators.length == 1)) {
				administrator = administratorData.getAdministrator(selectedAdministrators[0]);
				String[] modifiedAdministrator = new String[administrator.length];
				for (int i = 0; i < administrator.length; i++) {
					modifiedAdministrator[i] = administrator[i];
				}
				modifiedAdministrator = rreManager.getAdministratorDefiner().getAdministrator(modifiedAdministrator);
				if (modifiedAdministrator != null) {
					administratorData.modifyAdministrator(modifiedAdministrator);
					update();
				}
			}
		}
	}
	
	
	private void addAdministrator() {
		if (rreManager.getAdministratorSelector().login() != null) {
			String[] administrator = rreManager.getAdministratorDefiner().getAdministrator(null);
			if (administrator != null) {
				administratorData.addAdministrator(administrator);
				update();
			}
		}
	}
	
	
	private void showSelection() {
		int[] selection = administratorsTable.getSelectedRows();
		List<Integer> realSelection = new ArrayList<Integer>();
		for (int selectionIndex : selection) {
			int realIndex = administratorsTable.convertRowIndexToModel(selectionIndex);
			realSelection.add(realIndex);
		}
		if (selection.length == 1) {
			editAdministratorButton.setEnabled(true);
		}
		else {
			editAdministratorButton.setEnabled(false);
		}
		addAdministratorButton.setEnabled(true);
		showInfo(realSelection);
	}
	
	
	private void showInfo(List<Integer> selection) {
		final int rowLabelWidth = 150;
		final int rowHeight = 20;
		
		final int[] itemsToShow = new int[] {
				AdministratorData.NAME,
				AdministratorData.TITLE,
				AdministratorData.PHONE,
				AdministratorData.ERASMUS
		};
		
		detailsPanel.removeAll();
		JPanel administratorsDetailPanel = new JPanel(new BorderLayout());
		JPanel currentPanel = administratorsDetailPanel;
		for (int administratorNr : selection) {
			String[] user = administrators.get(administratorNr);
			JPanel administratorPanel = new JPanel(new BorderLayout());
			JLabel nameLabel = new JLabel(AdministratorData.getAdministratorDescription(user, false));
			nameLabel.setFont(new Font(nameLabel.getFont().getName(), Font.BOLD, 18));
			administratorPanel.add(nameLabel, BorderLayout.NORTH);
			
			JPanel parentPanel = administratorPanel;
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
				rowLabel = new JLabel("    " + AdministratorData.fieldName[item] + ":");
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
			
			currentPanel.add(administratorPanel, BorderLayout.NORTH);
			nextPanel = new JPanel(new BorderLayout());
			currentPanel.add(nextPanel, BorderLayout.CENTER);
			currentPanel = nextPanel;
			
			// Create blank space between administrators
			JPanel blankSpacePanel = new JPanel();
			blankSpacePanel.setMinimumSize(new Dimension(10, rowHeight));
			blankSpacePanel.setPreferredSize(new Dimension(10000, rowHeight));
			
			currentPanel.add(blankSpacePanel, BorderLayout.NORTH);
			nextPanel = new JPanel(new BorderLayout());
			currentPanel.add(nextPanel, BorderLayout.CENTER);
			currentPanel = nextPanel;
		}
        JScrollPane usersDetailScrollPane = new JScrollPane(administratorsDetailPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailsPanel.add(usersDetailScrollPane, BorderLayout.CENTER);
        detailsPanel.validate();
	}
	
	
	public int[] getSelectedAdministrators() {
		return selectedAdministrators;
	}

}
