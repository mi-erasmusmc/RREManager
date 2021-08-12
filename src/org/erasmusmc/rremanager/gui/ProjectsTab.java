package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.files.ProjectData;

public class ProjectsTab extends MainFrameTab {
	private static final long serialVersionUID = 5033121660144362646L;
	
	private ProjectData projectData;
	private Map<String, List<String>> newProjects;
	@SuppressWarnings("rawtypes")
	private JList projectsList;
	@SuppressWarnings("rawtypes")
	private JList projectGroupsList;
	private JPanel groupsPanel;
	private JButton addProjectButton;
	private JButton addGroupButton;
	private JButton createGroupButton;

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ProjectsTab(RREManager rreManager, MainFrame mainFrame, String settingsGroup) {
		super(rreManager, mainFrame);
		
		projectData = new ProjectData(settingsGroup);
		newProjects = new HashMap<String, List<String>>();
		
		setLayout(new BorderLayout());
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JPanel projectsPanel = new JPanel(new BorderLayout());
		projectsPanel.setBorder(BorderFactory.createTitledBorder("Projects"));
		projectsPanel.setPreferredSize(new Dimension(300, 50));
		
		DefaultListModel<String> projectsListModel = new DefaultListModel<String>();
		for (String projectName : projectData.getProjectNames()) {
			projectsListModel.addElement(projectName);
		}
		projectsList = new JList(projectsListModel);
		projectsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		projectsList.setLayoutOrientation(JList.VERTICAL);
		projectsList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					int selection = projectsList.getSelectedIndex();
					showGroups(selection == -1 ? null : (String) projectsList.getSelectedValue());
				}
			}
		});
		JScrollPane projectsScrollPane = new JScrollPane(projectsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		projectsPanel.add(projectsScrollPane, BorderLayout.CENTER);

		groupsPanel = new JPanel(new BorderLayout());
		groupsPanel.setBorder(BorderFactory.createTitledBorder("Groups"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addProjectButton = new JButton("Add Project");
        addProjectButton.setEnabled(true);
        addProjectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RREManager.disableComponents();
				addProject();
				RREManager.enableComponents();
				mainFrame.refreshLog();
			}
		});
		RREManager.disableWhenRunning(addProjectButton);
        
        addGroupButton = new JButton("Add Group");
		addGroupButton.setEnabled(false);
		addGroupButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RREManager.disableComponents();
				//rreManager.sendAccountInformation(selectedUsers, userData);
				RREManager.enableComponents();
				mainFrame.refreshLog();
			}
		});
		RREManager.disableWhenRunning(addGroupButton);
        
        createGroupButton = new JButton("Add Group");
        createGroupButton.setEnabled(false);
        createGroupButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RREManager.disableComponents();
				//rreManager.sendAccountInformation(selectedUsers, userData);
				RREManager.enableComponents();
				mainFrame.refreshLog();
			}
		});
		RREManager.disableWhenRunning(createGroupButton);
		
		buttonPanel.add(addProjectButton);
        buttonPanel.add(addGroupButton);
        buttonPanel.add(createGroupButton);
		
		mainPanel.add(projectsPanel, BorderLayout.WEST);
		mainPanel.add(groupsPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		add(mainPanel, BorderLayout.CENTER);
		add(mainFrame.createLogPanel(), BorderLayout.SOUTH);
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void showGroups(String projectName) {
		groupsPanel.removeAll();
		if (projectName != null) {
			addGroupButton.setEnabled(true);
			DefaultListModel<String> projectGroupsListModel = new DefaultListModel<String>();
			for (String group : projectData.getProjectGroups(projectName)) {
				projectGroupsListModel.addElement(group);
			}
			projectGroupsList = new JList(projectGroupsListModel);
			projectGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			projectGroupsList.setLayoutOrientation(JList.VERTICAL);
			projectGroupsList.addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting() == false) {
						//int selection = projectGroupsList.getSelectedIndex();
						//showGroups(selection == -1 ? null : (String) projectsList.getSelectedValue());
					}
				}
			});
			JScrollPane projectGroupsScrollPane = new JScrollPane(projectGroupsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			groupsPanel.add(projectGroupsScrollPane, BorderLayout.CENTER);
		}
		groupsPanel.validate();
	}
	
	
	private void addProject() {
		Map<String, List<String>> project = rreManager.getProjectDefiner().getProject();
		if (project != null) {
			System.out.println("TEST");
		}
	}

}
