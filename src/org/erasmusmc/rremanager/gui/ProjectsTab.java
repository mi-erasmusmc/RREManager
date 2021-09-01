package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.UIManager;

import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.files.ProjectData;

public class ProjectsTab extends MainFrameTab {
	private static final long serialVersionUID = 5033121660144362646L;
	
	private ProjectData projectData;
	private Map<String, List<String>> newProjects = null;
	private JPanel projectsPanel;
	@SuppressWarnings("rawtypes")
	private JList projectsList;
	@SuppressWarnings("rawtypes")
	private JList projectGroupsList;
	private JPanel groupsPanel;
	private JButton addProjectButton;
	private JButton addGroupButton;
	private JButton createNewButton;

	
	public ProjectsTab(RREManager rreManager, MainFrame mainFrame, String settingsGroup) {
		super(rreManager, mainFrame);
		
		projectData = new ProjectData(mainFrame, settingsGroup);
		newProjects = new HashMap<String, List<String>>();
		
		setLayout(new BorderLayout());
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		projectsPanel = new JPanel(new BorderLayout());
		projectsPanel.setBorder(BorderFactory.createTitledBorder("Projects"));
		projectsPanel.setPreferredSize(new Dimension(300, 50));

		groupsPanel = new JPanel(new BorderLayout());
		groupsPanel.setBorder(BorderFactory.createTitledBorder("Groups"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addProjectButton = new JButton("Add Project");
        addProjectButton.setEnabled(true);
        addProjectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				addProjectGroups(null);
				mainFrame.refreshLog();
			}
		});
		RREManager.disableWhenRunning(addProjectButton);
        
        addGroupButton = new JButton("Add Groups");
		addGroupButton.setEnabled(false);
		addGroupButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				addProjectGroups((String) projectsList.getSelectedValue());
				mainFrame.refreshLog();
			}
		});
		RREManager.disableWhenRunning(addGroupButton);
        
        createNewButton = new JButton("Create New");
        createNewButton.setEnabled(false);
        createNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RREManager.disableComponents();
				if (projectData.addProjects(newProjects)) {
					newProjects = new HashMap<String, List<String>>();
				}
				else {
					mainFrame.logWithTimeLn(projectData.getError());
				}
				RREManager.enableComponents();
				createNewButton.setEnabled(false);
				showProjects();
				mainFrame.refreshLog();
			}
		});
		RREManager.disableWhenRunning(createNewButton);
		
		buttonPanel.add(addProjectButton);
        buttonPanel.add(addGroupButton);
        buttonPanel.add(createNewButton);
		
		mainPanel.add(projectsPanel, BorderLayout.WEST);
		mainPanel.add(groupsPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		add(mainPanel, BorderLayout.CENTER);
		add(mainFrame.createLogPanel(), BorderLayout.SOUTH);
		
		showProjects();
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void showProjects() {
		projectsPanel.removeAll();
		DefaultListModel<String> projectsListModel = new DefaultListModel<String>();
		
		List<String> projectNames = new ArrayList<String>();
		for (String projectName : projectData.getProjectNames()) {
			projectNames.add(projectName);
		}
		if (newProjects != null) {
			for (String projectName : newProjects.keySet()) {
				if (!projectNames.contains(projectName)) {
					projectNames.add(projectName);
				}
			}
		}
		Collections.sort(projectNames);
		for (String projectName : projectNames) {
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
		projectsList.setCellRenderer(new MyProjectListCellRenderer());
		JScrollPane projectsScrollPane = new JScrollPane(projectsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		projectsPanel.add(projectsScrollPane, BorderLayout.CENTER);
		projectsPanel.validate();
		showGroups(null);
	}
	
	
	@SuppressWarnings("rawtypes")
	class MyProjectListCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 759949058974963733L;
		
		private List<String> projects;
		private Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
		
		public MyProjectListCellRenderer() {
			projects = projectData.getProjectNames();
		}

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
			if ((!projects.contains(getText()) || newProjects.containsKey(getText()))) {
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
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void showGroups(String projectName) {
		groupsPanel.removeAll();
		addGroupButton.setEnabled(false);
		if (projectName != null) {
			addGroupButton.setEnabled(true);
			DefaultListModel<String> projectGroupsListModel = new DefaultListModel<String>();
			List<String> projectGroups = new ArrayList<String>();
			for (String group : projectData.getProjectGroups(projectName)) {
				projectGroups.add(group);
			}
			if (newProjects.get(projectName) != null) {
				for (String group : newProjects.get(projectName)) {
					if (!projectGroups.contains(group)) {
						projectGroups.add(group);
					}
				}
			}
			Collections.sort(projectGroups);
			if (projectGroups.size() > 0) {
				for (String group : projectGroups) {
					projectGroupsListModel.addElement(group);
				}
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
			projectGroupsList.setCellRenderer(new MyGroupListCellRenderer(projectName));
			JScrollPane projectGroupsScrollPane = new JScrollPane(projectGroupsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			groupsPanel.add(projectGroupsScrollPane, BorderLayout.CENTER);
			addGroupButton.setEnabled(true);
		}
		groupsPanel.validate();
	}
	
	
	@SuppressWarnings("rawtypes")
	class MyGroupListCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 4831163291117596619L;
		
		private List<String> projectGroups;
		private Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
		
		public MyGroupListCellRenderer(String project) {
			projectGroups = projectData.getProjectGroups(project);
		}

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
			if (!projectGroups.contains(getText())) {
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
	
	
	private void addProjectGroups(String projectName) {
		Map<String, List<String>> newProject = rreManager.getProjectDefiner().getProjectGroups(projectName);
		if (newProject != null) {
			for (String project : newProject.keySet()) {
				List<String> currentGroups = projectData.getProjectGroups(project);
				List<String> newGroups = new ArrayList<String>();
				if (newProjects.get(project) != null) {
					currentGroups.addAll(newProjects.get(project));
					newGroups.addAll(newProjects.get(project));
				}
				for (String newGroup : newProject.get(project)) {
					if ((!currentGroups.contains(newGroup)) && (!newGroups.contains(newGroup))) {
						newGroups.add(newGroup);
					}
				} 
				if (newGroups.size() > 0) {
					Collections.sort(newGroups);
					newProjects.put(project, newGroups);
				}
			}
		}
		if (newProjects.size() > 0) {
			createNewButton.setEnabled(true);
		}
		if (projectName == null) {
			showProjects();
		}
		else {
			showGroups(projectName);
		}
	}
	
	
	public List<String> getProjectNames() {
		List<String> projectNames = new ArrayList<String>();
		
		if (projectData.getProjectNames() != null) {
			projectNames.addAll(projectData.getProjectNames());
		}

		Collections.sort(projectNames);
		
		return projectNames;
	}
	
	
	public List<String> getGroups(String projectName) {
		List<String> groups = new ArrayList<String>();
		
		groups.addAll(projectData.getProjectGroups(projectName));
		
		Collections.sort(groups);
		return groups;
	}

}
