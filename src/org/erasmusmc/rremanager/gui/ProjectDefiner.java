package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ProjectDefiner {
	private static int LABELWIDTH = 80;
	private static int FIELDWIDTH = 255;
	private static int ROWHEIGHT  = 20;

	private JFrame parentFrame;
	private String name = null;
	private String groups = null;
	
	
	public ProjectDefiner(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}
	
	
	public Map<String, List<String>> getProject() {
		Map<String, List<String>> project = null;
		askProject();
		if (name != null) {
			name = name.toUpperCase();
			List<String> groupList = null;
			if (groups != null) {
				String[] groupsSplit = groups.split(",");
				for (String group : groupsSplit) {
					group = group.trim();
					if (!group.equals("")) {
						group = group.toUpperCase();
						if (groupList == null) {
							groupList = new ArrayList<String>();
						}
						groupList.add(group);
					}
				}
			}
			project = new HashMap<String, List<String>>();
			project.put(name, groupList);
		}
		return project;
	}
	
	
	public void askProject() {
		name = null;
		groups = null;
		
		Dimension projectDialogSize = new Dimension(LABELWIDTH + FIELDWIDTH, (2 * ROWHEIGHT) + 90);
		JDialog projectDialog = new JDialog(parentFrame, true);
		projectDialog.setTitle("New Project");
		projectDialog.setLayout(new BorderLayout());
		projectDialog.setMinimumSize(projectDialogSize);
		projectDialog.setPreferredSize(projectDialogSize);

		JPanel projectPanel = new JPanel();
		projectPanel.setLayout(new BoxLayout(projectPanel, BoxLayout.PAGE_AXIS));
		
		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel nameLabel = new JLabel("Project name:");
		nameLabel.setPreferredSize(new Dimension(LABELWIDTH, ROWHEIGHT));
		JTextField nameField = new JTextField(20);
		namePanel.add(nameLabel);
		namePanel.add(nameField);
		
		JPanel groupsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel groupsLabel = new JLabel("Groups:");
		groupsLabel.setPreferredSize(new Dimension(LABELWIDTH, ROWHEIGHT));
		JTextField groupsField = new JTextField(20);
		groupsPanel.add(groupsLabel);
		groupsPanel.add(groupsField);
		
		projectPanel.add(namePanel);
		namePanel.setAlignmentX((float) 0.0);
		
		projectPanel.add(groupsPanel);
		groupsPanel.setAlignmentX((float) 0.0);
		
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				name = nameField.getText().trim();
				groups = groupsField.getText().trim();
				if (name.equals("")) {
					name = null;
					groups = null;
				}
				projectDialog.dispose();
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				name = null;
				projectDialog.dispose();
			}
		});
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		projectDialog.add(projectPanel, BorderLayout.CENTER);
		projectDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		projectDialog.pack();
		projectDialog.setLocationRelativeTo(parentFrame);
		
		projectDialog.setVisible(true);
	}
	
	
	public String getName() {
		return name;
	}
}
