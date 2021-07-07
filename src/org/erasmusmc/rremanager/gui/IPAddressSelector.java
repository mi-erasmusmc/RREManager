package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.border.Border;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class IPAddressSelector {
	private static int IP_LABEL_SIZE          = 120;
	private static int USER_NAME_FIELD_HEIGHT =  28;
	
	private JFrame parentFrame;
	private JCheckBox allIPCheckBox;
	private boolean userAction;
	private Map<String, JCheckBox> ipCheckBoxMap = new HashMap<String, JCheckBox>();
	private Map<String, List<String>> newIPMap = null; 
	
	
	public IPAddressSelector(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}
	
	
	public void selectIPAddresses(Map<String, List<String>> ipMap, Set<String> selectSet) {
		Dimension ipSelectorDialogSize = new Dimension(500, 250);
		JDialog ipSelectorDialog = new JDialog(parentFrame, true);
		ipSelectorDialog.setTitle("Select IP-addresses");
		ipSelectorDialog.setLayout(new BorderLayout());
		ipSelectorDialog.setMinimumSize(ipSelectorDialogSize);
		ipSelectorDialog.setPreferredSize(ipSelectorDialogSize);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		List<String> usersHeader = new ArrayList<String>();
		usersHeader.add("Users");
		mainPanel.add(new IPSelectRow("IP-Address", usersHeader, selectSet == null), BorderLayout.NORTH);
		
		List<String> ipList = new ArrayList<String>();
		ipList.addAll(ipMap.keySet());
		JPanel ipPanel = new JPanel(new BorderLayout());
		JPanel currentPanel = ipPanel;
		for (int ipNr = 0; ipNr < ipList.size(); ipNr++) {
			currentPanel.add(new IPSelectRow(ipNr < ipList.size() ? ipList.get(ipNr) : "", ipNr < ipList.size() ? ipMap.get(ipList.get(ipNr)) : null, ((selectSet == null) || selectSet.contains(ipNr < ipList.size() ? ipList.get(ipNr) : ""))), BorderLayout.NORTH);
			if (ipNr < ipList.size() - 1) {
				JPanel nextPanel = new JPanel(new BorderLayout());
				currentPanel.add(nextPanel, BorderLayout.CENTER);
				currentPanel = nextPanel;
			}
		}

		JScrollPane ipScrollPane = new JScrollPane(ipPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mainPanel.add(ipScrollPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				newIPMap = new HashMap<String, List<String>>();
				for (String ip : ipMap.keySet()) {
					if (ipCheckBoxMap.get(ip).isSelected()) {
						newIPMap.put(ip, ipMap.get(ip));
					}
				}
				ipSelectorDialog.dispose();
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				newIPMap = null;
				ipSelectorDialog.dispose();
			}
		});
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

        ipSelectorDialog.add(mainPanel, BorderLayout.CENTER);
		ipSelectorDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		ipSelectorDialog.pack();
		ipSelectorDialog.setLocationRelativeTo(parentFrame);
		
		userAction = true;
		ipSelectorDialog.setVisible(true);
	}
	
	
	public Map<String, List<String>> getIPSelection() {
		return newIPMap;
	}
	
	
	private class IPSelectRow extends JPanel {
		private static final long serialVersionUID = 3765589314027285148L;

		public IPSelectRow(String ipAddress, List<String> userList, boolean selected) {
			String users = "";
			if (userList != null) {
				for (String user : userList) {
					users += (users.equals("") ? "" : ", ") + user;
				}
			}

			this.setMinimumSize(new Dimension(10, USER_NAME_FIELD_HEIGHT));
			this.setMaximumSize(new Dimension(10000, USER_NAME_FIELD_HEIGHT));
			this.setPreferredSize(new Dimension(10000, USER_NAME_FIELD_HEIGHT));
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			JPanel ipSelectLabelPanel = new JPanel(new BorderLayout());
			ipSelectLabelPanel.setMinimumSize(new Dimension(IP_LABEL_SIZE, USER_NAME_FIELD_HEIGHT));
			ipSelectLabelPanel.setMaximumSize(new Dimension(IP_LABEL_SIZE, USER_NAME_FIELD_HEIGHT));
			ipSelectLabelPanel.setPreferredSize(new Dimension(IP_LABEL_SIZE, USER_NAME_FIELD_HEIGHT));
			if (userList != null) {
				JCheckBox ipSelectCheckBox = new JCheckBox();
				ipSelectCheckBox.setSelected(selected);
				ipSelectCheckBox.setEnabled(true);
				if (users.equals("Users")) {
					ipSelectCheckBox.setToolTipText("Select/Deselect all IP-addresses");
					ipSelectCheckBox.addItemListener(new ItemListener() {
						
						@Override
						public void itemStateChanged(ItemEvent e) {
							if (userAction) {
								if (e.getStateChange() == ItemEvent.SELECTED) {
									for (String checkBoxIP : ipCheckBoxMap.keySet()) {
										ipCheckBoxMap.get(checkBoxIP).setSelected(true);
									}
								}
								else {
									for (String checkBoxIP : ipCheckBoxMap.keySet()) {
										ipCheckBoxMap.get(checkBoxIP).setSelected(false);
									}
								}
							}
							
						}
					});
					allIPCheckBox = ipSelectCheckBox;
				}
				else {
					ipSelectCheckBox.setToolTipText("Select/Deselect IP-address");
					ipSelectCheckBox.addItemListener(new ItemListener() {
						
						@Override
						public void itemStateChanged(ItemEvent e) {
							boolean allSelected = true;
							for (String checkBoxIP : ipCheckBoxMap.keySet()) {
								allSelected = allSelected && ipCheckBoxMap.get(checkBoxIP).isSelected();
							}
							userAction = false;
							allIPCheckBox.setSelected(allSelected);
							userAction = true;
						}
					});
					ipCheckBoxMap.put(ipAddress, ipSelectCheckBox);
				}
				ipSelectLabelPanel.add(ipSelectCheckBox, BorderLayout.WEST);
			}
			
			JPanel ipAddressLabelPanel = new JPanel(new BorderLayout());
			JLabel ipAddressLabel = new JLabel(ipAddress);
			if ((userList != null) && (!users.equals("Users"))) {
				ipAddressLabel.setFont(ipAddressLabel.getFont().deriveFont(java.awt.Font.PLAIN));
			}
			ipAddressLabelPanel.add(ipAddressLabel, BorderLayout.WEST);
			
			ipSelectLabelPanel.add(ipAddressLabelPanel, BorderLayout.CENTER);
			 
			JTextField userNamesField;
			if ((userList == null) || users.equals("Users")) {
				userNamesField = new JTextField() {
					private static final long serialVersionUID = 8575807196340335496L;

					@Override public void setBorder(Border border) {
				        // No!
				    }
				};
				userNamesField.setText(users);
				userNamesField.setFont(userNamesField.getFont().deriveFont(java.awt.Font.BOLD));
			}
			else {
				userNamesField = new JTextField();
				userNamesField.setText(users);
			}
			userNamesField.setMinimumSize(new Dimension(10, USER_NAME_FIELD_HEIGHT));
			userNamesField.setMaximumSize(new Dimension(10000, USER_NAME_FIELD_HEIGHT));
			userNamesField.setPreferredSize(new Dimension(10000, USER_NAME_FIELD_HEIGHT));
			userNamesField.setEditable(false);

			add(ipSelectLabelPanel);
			add(userNamesField);
		}
	}

}
