package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ProjectDefiner {
	private static int LABELWIDTH    = 80;
	private static int FIELDWIDTH    = 255;
	private static int ROWHEIGHT     = 25;
	private static int INFOWIDTH     = 220;
	private static int INFOROWHEIGHT = 20;

	private JFrame parentFrame;
	private String name = null;
	private String groups = null;
	
	
	public ProjectDefiner(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}
	
	
	public Map<String, List<String>> getProjectGroups(String projectName) {
		Map<String, List<String>> project = null;
		askProject(projectName);
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
	
	
	public void askProject(String projectName) {
		name = null;
		groups = null;
		
		Dimension projectDialogSize = new Dimension(LABELWIDTH + FIELDWIDTH, (2 * ROWHEIGHT) + INFOROWHEIGHT + 90);
		JDialog projectDialog = new JDialog(parentFrame, true);
		projectDialog.setTitle("New Project/Groups");
		projectDialog.setLayout(new BorderLayout());
		projectDialog.setMinimumSize(projectDialogSize);
		projectDialog.setPreferredSize(projectDialogSize);

		JPanel projectPanel = new JPanel();
		projectPanel.setLayout(new BoxLayout(projectPanel, BoxLayout.PAGE_AXIS));

		JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel nameLabel = new JLabel("Project name:");
		nameLabel.setPreferredSize(new Dimension(LABELWIDTH, ROWHEIGHT));
		JTextField nameField = new JTextField(20);
		if (projectName != null) {
			nameField.setText(projectName);
			nameField.setEnabled(false);
		}
		namePanel.add(nameLabel);
		namePanel.add(nameField);
		
		JPanel groupsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel groupsLabel = new JLabel("Groups:");
		groupsLabel.setPreferredSize(new Dimension(LABELWIDTH, ROWHEIGHT));
		JTextField groupsField = new JTextField(20);
		groupsPanel.add(groupsLabel);
		groupsPanel.add(groupsField);
		
		JPanel groupsInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel groupsInfoLabel = new JLabel("");
		groupsInfoLabel.setPreferredSize(new Dimension(LABELWIDTH, INFOROWHEIGHT));
		groupsInfoLabel.setMaximumSize(new Dimension(LABELWIDTH, INFOROWHEIGHT));
		Font infoFont = groupsInfoLabel.getFont().deriveFont(java.awt.Font.PLAIN);
		infoFont = infoFont.deriveFont((float) 10.0);
		groupsInfoLabel.setFont(infoFont);
		JLabel groupsInfoTextLabel = new JLabel("Enter group names separated by a comma.");
		groupsInfoTextLabel.setPreferredSize(new Dimension(INFOWIDTH, INFOROWHEIGHT));
		groupsInfoTextLabel.setMaximumSize(new Dimension(INFOWIDTH, INFOROWHEIGHT));
		groupsInfoTextLabel.setFont(infoFont);
		groupsInfoPanel.add(groupsInfoLabel);
		groupsInfoPanel.add(groupsInfoTextLabel);

		projectPanel.add(namePanel);
		namePanel.setAlignmentX((float) 0.0);
		
		projectPanel.add(groupsPanel);
		groupsPanel.setAlignmentX((float) 0.0);
		
		projectPanel.add(groupsInfoPanel);
		groupsInfoPanel.setAlignmentX((float) 0.0);
		
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				name = nameField.getText().trim();
				groups = groupsField.getText().trim();
				if (!name.equals("")) {
					if (!groups.equals("")) {
						projectDialog.dispose();
					}
					else {
						JOptionPane.showMessageDialog(parentFrame, "No groups specified!", "Project Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					JOptionPane.showMessageDialog(parentFrame, "No project name specified!", "Project Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				name = null;
				groups = null;
				projectDialog.dispose();
			}
		});
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		projectDialog.add(projectPanel, BorderLayout.CENTER);
		projectDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		projectDialog.pack();
		projectDialog.setLocationRelativeTo(parentFrame);
		
		JRootPane rootPane = SwingUtilities.getRootPane(okButton); 
		rootPane.setDefaultButton(okButton);
		
		projectDialog.setVisible(true);
	}
}
