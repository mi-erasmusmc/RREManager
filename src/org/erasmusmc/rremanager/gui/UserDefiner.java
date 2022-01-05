package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.files.ProjectData;
import org.erasmusmc.rremanager.files.UserData;

public class UserDefiner {
	private static int LABELWIDTH    = 90;
	private static int FIELDWIDTH    = 200;
	private static int FIELDHEIGHT   = 25;

	private ProjectData projectData = null;
	private String[] user = null;
	
	private JFrame parentFrame;
	private Boolean originalFTPOnly = false;

	private JCheckBox ftpOnlyField = null;
	private JPanel availablePanel = null;
	private JButton addButton = null;
	private JButton removeButton = null;
	private JPanel selectedPanel = null;
	
	private List<String> availableGroups;
	private List<String> groupsSelectedToAdd;
	private List<String> originalSelectedGroups;
	private List<String> selectedGroups;
	private List<String> groupsSelectedToRemove;
	
		
	public UserDefiner(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}
	
	
	String[] getUser(String[] user, ProjectData projectData) {
		this.projectData = projectData;
		this.user = user;
		askUser();
		return this.user;
	}
	
	
	private void askUser() {
		String firstName    = user == null ? "" : user[UserData.FIRST_NAME]   == null ? ""     : user[UserData.FIRST_NAME].trim();
		String initials     = user == null ? "" : user[UserData.INITIALS]     == null ? ""     : user[UserData.INITIALS].trim();
		String lastName     = user == null ? "" : user[UserData.LAST_NAME]    == null ? ""     : user[UserData.LAST_NAME].trim();
		String userName     = user == null ? "" : user[UserData.USER_NAME]    == null ? ""     : user[UserData.USER_NAME].trim();
		String password     = user == null ? "" : user[UserData.PASSWORD]     == null ? ""     : user[UserData.PASSWORD].trim();
		String emailAddress = user == null ? "" : user[UserData.EMAIL]        == null ? ""     : user[UserData.EMAIL].trim();
		String emailFormat  = user == null ? "" : user[UserData.EMAIL_FORMAT] == null ? "HTML" : user[UserData.EMAIL_FORMAT].trim();
		String projects     = user == null ? "" : user[UserData.PROJECTS]     == null ? ""     : user[UserData.PROJECTS].trim();
		String groups       = user == null ? "" : user[UserData.GROUPS]       == null ? ""     : user[UserData.GROUPS].trim();
		String ipAddresses  = user == null ? "" : user[UserData.IP_ADDRESSES] == null ? ""     : user[UserData.IP_ADDRESSES].trim();
		Boolean ftpOnly     = user == null ? false : groups.equals("");
		originalFTPOnly = ftpOnly;

		availableGroups = new ArrayList<String>();
		if (projectData != null) {
			for (String project : projectData.getProjectNames()) {
				for (String group : projectData.getProjectGroups(project)) {
					availableGroups.add(project + " " + group);
				}
			}
		}
		groupsSelectedToAdd = new ArrayList<String>();

		originalSelectedGroups = new ArrayList<String>();
		selectedGroups = new ArrayList<String>();
		if ((projects != null) && (!projects.equals(""))) {
			String[] projectsSplit = projects.split(",");
			if ((groups != null) && (!groups.equals(""))) {
				String[] groupsSplit = groups.split(",");
				for (String project : projectsSplit) {
					projects = project.trim();
					for (String group : groupsSplit) {
						group = group.trim();
						if (group.startsWith(project + " ")) {
							originalSelectedGroups.add(group);
							selectedGroups.add(group);
							availableGroups.remove(group);
						}
					}
				} 
			}
		}
		groupsSelectedToRemove = new ArrayList<String>();
		
		Dimension userDialogSize = new Dimension(LABELWIDTH + FIELDWIDTH + 250, (6 * FIELDHEIGHT) + 250);
		JDialog userDialog = new JDialog(parentFrame, true);
		userDialog.setTitle(user == null ? "New User" : "Edit User");
		userDialog.setLayout(new BorderLayout());
		userDialog.setMinimumSize(userDialogSize);
		userDialog.setPreferredSize(userDialogSize);
		
		JLabel firstNameLabel = new JLabel("First name:");
		firstNameLabel.setPreferredSize(new Dimension(LABELWIDTH, FIELDHEIGHT));
		JTextField firstNameField = new JTextField();
		firstNameField.setMinimumSize(new Dimension(FIELDWIDTH, FIELDHEIGHT));
		firstNameField.setText(firstName);
		JLabel initialsLabel = new JLabel("Initials:");
		JTextField initialsField = new JTextField();
		initialsField.setMinimumSize(new Dimension(60, FIELDHEIGHT));
		initialsField.setMaximumSize(new Dimension(60, FIELDHEIGHT));
		initialsField.setPreferredSize(new Dimension(60, FIELDHEIGHT));
		initialsField.setText(initials);
		JLabel lastNameLabel = new JLabel("Last name:");
		JTextField lastNameField = new JTextField();
		lastNameField.setMinimumSize(new Dimension(FIELDWIDTH, FIELDHEIGHT));
		lastNameField.setText(lastName);
		JLabel userNameLabel = new JLabel("User name:");
		JTextField userNameField = new JTextField();
		userNameField.setMinimumSize(new Dimension(FIELDWIDTH, FIELDHEIGHT));
		userNameField.setText(userName);
		JLabel passwordLabel = new JLabel("Password:");
		JTextField passwordField = new JTextField();
		passwordField.setMinimumSize(new Dimension(110, FIELDHEIGHT));
		passwordField.setMaximumSize(new Dimension(110, FIELDHEIGHT));
		passwordField.setPreferredSize(new Dimension(110, FIELDHEIGHT));
		passwordField.setText(password);
		JLabel emailAddressLabel = new JLabel("Email address:");
		JTextField emailAddressField = new JTextField();
		emailAddressField.setMinimumSize(new Dimension(FIELDWIDTH, FIELDHEIGHT));
		emailAddressField.setText(emailAddress);
		JLabel emailFormatLabel = new JLabel("Format:");
		JComboBox<String> emailFormatField = new JComboBox<String> (new String[] { "HTML", "TEXT" });
		emailFormatField.setMinimumSize(new Dimension(60, FIELDHEIGHT));
		emailFormatField.setMaximumSize(new Dimension(60, FIELDHEIGHT));
		emailFormatField.setPreferredSize(new Dimension(60, FIELDHEIGHT));
		emailFormatField.setSelectedItem(emailFormat);
		JLabel ipAddressesLabel = new JLabel("IP-addresses:");
		JTextField ipAddressesField = new JTextField();
		ipAddressesField.setMinimumSize(new Dimension(FIELDWIDTH, FIELDHEIGHT));
		ipAddressesField.setText(ipAddresses);
		JLabel ftpOnlyLabel = new JLabel("FTP-Only:");
		ftpOnlyField = new JCheckBox();
		ftpOnlyField.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				showGroups(availableGroups);
				showGroups(selectedGroups);
			}
		});
		JLabel projectGroupsLabel = new JLabel("Project groups:");
		
		availablePanel = new JPanel(new BorderLayout());
		availablePanel.setPreferredSize(new Dimension(80, 100));
		
		JPanel spacerPanel = new JPanel();
		spacerPanel.setMinimumSize(new Dimension(15, 40));
		spacerPanel.setMaximumSize(new Dimension(15, 40));
		spacerPanel.setPreferredSize(new Dimension(15, 40));
		
		addButton = new JButton(new ImageIcon(RREManager.class.getResource("/org/erasmusmc/rremanager/gui/RightArrow 20x20.gif")));
		addButton.setMinimumSize(new Dimension(20, 20));
		addButton.setMaximumSize(new Dimension(20, 20));
		addButton.setPreferredSize(new Dimension(20, 20));
		addButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				for (String group : groupsSelectedToAdd) {
					if (!selectedGroups.contains(group)) {
						selectedGroups.add(group);
						availableGroups.remove(group);
					}
				}
				showGroups(availableGroups);
				showGroups(selectedGroups);
			}
		});
		
		removeButton = new JButton(new ImageIcon(RREManager.class.getResource("/org/erasmusmc/rremanager/gui/LeftArrow 20x20.gif")));
		removeButton.setMinimumSize(new Dimension(20, 20));
		removeButton.setMaximumSize(new Dimension(20, 20));
		removeButton.setPreferredSize(new Dimension(20, 20));
		removeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				for (String group : groupsSelectedToRemove) {
					if (!originalSelectedGroups.contains(group)) {
						availableGroups.add(group);
						selectedGroups.remove(group);
					}
				}
				showGroups(availableGroups);
				showGroups(selectedGroups);
			}
		});

		selectedPanel = new JPanel(new BorderLayout());
		selectedPanel.setPreferredSize(new Dimension(80, 100));

		JPanel userPanel = new JPanel();
		GroupLayout layout = new GroupLayout(userPanel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
		userPanel.setLayout(layout);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(firstNameLabel)
						.addComponent(lastNameLabel)
						.addComponent(userNameLabel)
						.addComponent(emailAddressLabel)
						.addComponent(ipAddressesLabel)
						.addComponent(ftpOnlyLabel)
						.addComponent(projectGroupsLabel)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(firstNameField)
								.addComponent(initialsLabel)
								.addComponent(initialsField)
								)
						.addComponent(lastNameField)
						.addGroup(layout.createSequentialGroup()
								.addComponent(userNameField)
								.addComponent(passwordLabel)
								.addComponent(passwordField)
								)
						.addGroup(layout.createSequentialGroup()
								.addComponent(emailAddressField)
								.addComponent(emailFormatLabel)
								.addComponent(emailFormatField)
								)
						.addComponent(ipAddressesField)
						.addComponent(ftpOnlyField)
						.addGroup(layout.createSequentialGroup()
								.addComponent(availablePanel)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(spacerPanel)
										.addComponent(addButton)
										.addComponent(removeButton)
										)
								.addComponent(selectedPanel)
								)
						)
				);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(firstNameLabel)
						.addComponent(firstNameField)
						.addComponent(initialsLabel)
						.addComponent(initialsField)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(lastNameLabel)
						.addComponent(lastNameField)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(userNameLabel)
						.addComponent(userNameField)
						.addComponent(passwordLabel)
						.addComponent(passwordField)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(emailAddressLabel)
						.addComponent(emailAddressField)
						.addComponent(emailFormatLabel)
						.addComponent(emailFormatField)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(ipAddressesLabel)
						.addComponent(ipAddressesField)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(ftpOnlyLabel)
						.addComponent(ftpOnlyField)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(projectGroupsLabel)
						.addComponent(availablePanel)
						.addGroup(layout.createSequentialGroup()
								.addComponent(spacerPanel)
								.addComponent(addButton)
								.addComponent(removeButton)
								)
						.addComponent(selectedPanel)
						)
				);
		
		layout.linkSize(firstNameLabel, lastNameLabel, userNameLabel, emailAddressLabel, projectGroupsLabel);
		
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!userNameField.getText().trim().equals("")) {
					if (user == null) {
						user = new String[UserData.OBJECT_SIZE];
					}
					user[UserData.FIRST_NAME]   = firstNameField.getText().trim();
					user[UserData.INITIALS]     = initialsField.getText().trim();
					user[UserData.LAST_NAME]    = lastNameField.getText().trim();
					user[UserData.USER_NAME]    = userNameField.getText().trim();
					user[UserData.PASSWORD]     = passwordField.getText().trim();
					user[UserData.EMAIL]        = emailAddressField.getText().trim();
					user[UserData.EMAIL_FORMAT] = ((String) emailFormatField.getSelectedItem()).trim();
					user[UserData.IP_ADDRESSES] = ipAddressesField.getText().trim();
					user[UserData.PROJECTS]     = "";
					user[UserData.GROUPS]       = "";
					if (!ftpOnlyField.isSelected()) {
						if (selectedGroups.size() > 0) {
							Set<String> selectedProjects = new HashSet<String>();
							for (String projectGroup : selectedGroups) {
								String project = projectGroup.split(" ")[0];
								if (selectedProjects.add(project)) {
									user[UserData.PROJECTS] += (user[UserData.PROJECTS].equals("") ? "" : ",") + project;
								}
								user[UserData.GROUPS] += (user[UserData.GROUPS].equals("") ? "" : ",") + projectGroup;
							}
						}
					}
					userDialog.dispose();
				}
				else {
					JOptionPane.showMessageDialog(parentFrame, "No user name specified!", "user Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				userDialog.dispose();
			}
		});
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		userDialog.add(userPanel, BorderLayout.CENTER);
		userDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		userDialog.pack();
		userDialog.setLocationRelativeTo(parentFrame);
		
		JRootPane rootPane = SwingUtilities.getRootPane(okButton); 
		rootPane.setDefaultButton(okButton);
		
		ftpOnlyField.setSelected(ftpOnly);
		showGroups(availableGroups);
		showGroups(selectedGroups);
		
		if (user != null) {
			firstNameField.setEditable(false);
			initialsField.setEditable(false);
			lastNameField.setEditable(false);
			userNameField.setEditable(false);
			passwordField.setEditable(false);
			emailAddressField.setEditable(false);
			if (!originalFTPOnly) {
				ftpOnlyField.setEnabled(false);
			}
		}
		
		userDialog.setVisible(true);
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void showGroups(List<String> groupNamesList) {
		JPanel groupsPanel = null;
		JLabel groupsPanelLabel = null;
		if (groupNamesList == selectedGroups) {
			groupsPanel = selectedPanel;
			groupsPanelLabel = new JLabel("Selected");
		}
		else {
			groupsPanel = availablePanel;
			groupsPanelLabel = new JLabel("Available");
		}
		groupsPanel.removeAll();
		DefaultListModel<String> projectsListModel = new DefaultListModel<String>();
		Collections.sort(groupNamesList);
		for (String group : groupNamesList) {
			projectsListModel.addElement(group);
		}
		JList groupsList = new JList(projectsListModel);
		groupsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		groupsList.setLayoutOrientation(JList.VERTICAL);
		groupsList.setCellRenderer(new MyGroupListCellRenderer());
		groupsList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (groupNamesList == selectedGroups) {
						groupsSelectedToRemove.clear();
						for (int index : groupsList.getSelectedIndices()) {
							groupsSelectedToRemove.add(groupNamesList.get(index));
						}
					}
					else {
						groupsSelectedToAdd.clear();
						for (int index : groupsList.getSelectedIndices()) {
							groupsSelectedToAdd.add(groupNamesList.get(index));
						}
					}
				}
			}
		});
		JScrollPane groupsScrollPane = new JScrollPane(groupsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		groupsPanel.add(groupsPanelLabel, BorderLayout.NORTH);
		groupsPanel.add(groupsScrollPane, BorderLayout.CENTER);

		groupsScrollPane.setEnabled(!ftpOnlyField.isSelected());
		groupsList.setEnabled(!ftpOnlyField.isSelected());
		addButton.setEnabled(!ftpOnlyField.isSelected());
		removeButton.setEnabled(!ftpOnlyField.isSelected());
		
		groupsPanel.validate();
	}
	
	
	@SuppressWarnings("rawtypes")
	class MyGroupListCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 759949058974963733L;
		
		private Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			String s = value != null ? value.toString() : "";
			setText(s);
			setOpaque(true);
			setHorizontalAlignment(LEFT);

			if (isSelected)
			  {
			    setBackground(list.getSelectionBackground());
			    setForeground(list.getSelectionForeground());
			  }
			else
			  {
			    setBackground(list.getBackground());
			    setForeground(list.getForeground());
			  }

			setEnabled(list.isEnabled());
			if (originalSelectedGroups.contains(getText())) {
				setFont(list.getFont().deriveFont(java.awt.Font.BOLD));
			}
			else {
				setFont(list.getFont().deriveFont(java.awt.Font.PLAIN));
			}

			// Use focusCellHighlightBorder when renderer has focus and
			// noFocusBorder otherwise
			if (cellHasFocus)
			  setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
			else
			  setBorder(noFocusBorder);
			return this;
		}
		
	}

}
