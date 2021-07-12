package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import org.erasmusmc.rremanager.RREManager;

public class AdministratorSelector {
	private JFrame parentFrame;
	private String administrator = null;

	
	public AdministratorSelector(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}
	
	
	public String getAdministrator() {
		administrator = null;
		
		Map<String, String> administratorsMap = RREManager.getIniFile().getGroup("Administrators");
		if (administratorsMap != null) {
			List<String> administratorsList = new ArrayList<String>();
			for (String administratorName : administratorsMap.keySet()) {
				if (RREManager.getIniFile().hasGroup(administratorName)) {
					administratorsList.add(administratorName);
				}
			}
			Collections.sort(administratorsList);
			
			if (administratorsList.size() > 1) {
				Dimension dimension = new Dimension(250, 110);
				JDialog administratorSelectorDialog = new JDialog(parentFrame, true);
				administratorSelectorDialog.setTitle("Select yourself");
				administratorSelectorDialog.setLayout(new BorderLayout());
				administratorSelectorDialog.setMinimumSize(dimension);
				administratorSelectorDialog.setMaximumSize(dimension);
				administratorSelectorDialog.setPreferredSize(dimension);
				
				JPanel selectionPanel = new JPanel();
				JComboBox<String> administratorsComboBox = new JComboBox<String>();
				administratorsComboBox.addItem("");
				for (String administratorName : administratorsList) {
					administratorsComboBox.addItem(administratorName);
				}
				selectionPanel.add(administratorsComboBox);
				
				JPanel buttonPanel = new JPanel();
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						administrator = (String) administratorsComboBox.getSelectedItem();
						administratorSelectorDialog.dispose();
					}
				});
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						administrator = null;
						administratorSelectorDialog.dispose();
					}
				});
				buttonPanel.add(okButton);
				buttonPanel.add(cancelButton);
				
				administratorSelectorDialog.add(selectionPanel, BorderLayout.CENTER);
				administratorSelectorDialog.add(buttonPanel, BorderLayout.SOUTH);
				
				administratorSelectorDialog.pack();
				administratorSelectorDialog.setLocationRelativeTo(parentFrame);
				
				JRootPane rootPane = SwingUtilities.getRootPane(okButton); 
				rootPane.setDefaultButton(okButton);
				
				administratorSelectorDialog.setVisible(true);
			}
			else {
				administrator = (String) administratorsMap.keySet().toArray()[0];
			}
		}
		
		return administrator;
	}
}
