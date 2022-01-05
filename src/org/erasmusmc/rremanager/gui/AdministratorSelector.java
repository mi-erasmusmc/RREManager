package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.erasmusmc.rremanager.RREManager;

public class AdministratorSelector {
	private JFrame parentFrame;
	private String administrator = null;
	
	
	public static String encryptPassword(String password) {
		String encryptedPassword = null;
		try {
			MessageDigest digest;
			digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
			encryptedPassword = bytesToHex(encodedHash);
		} catch (NoSuchAlgorithmException e) {
			encryptedPassword = null;
		}
		return encryptedPassword;
	}
	
	
	private static String bytesToHex(byte[] hash) {
	    StringBuilder hexString = new StringBuilder(2 * hash.length);
	    for (int i = 0; i < hash.length; i++) {
	        String hex = Integer.toHexString(0xff & hash[i]);
	        if(hex.length() == 1) {
	            hexString.append('0');
	        }
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}

	
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
				Dimension dimension = new Dimension(350, 150);
				JDialog administratorSelectorDialog = new JDialog(parentFrame, true);
				administratorSelectorDialog.setTitle("Select yourself");
				administratorSelectorDialog.setLayout(new BorderLayout());
				administratorSelectorDialog.setMinimumSize(dimension);
				administratorSelectorDialog.setMaximumSize(dimension);
				administratorSelectorDialog.setPreferredSize(dimension);
				
				JPanel selectionPanel = new JPanel();
				GroupLayout layout = new GroupLayout(selectionPanel);
				layout.setAutoCreateGaps(true);
				layout.setAutoCreateContainerGaps(true);
				selectionPanel.setLayout(layout);
				
				JLabel administratorLabel = new JLabel("Administrator:");
				JComboBox<String> administratorComboBox = new JComboBox<String>();
				administratorComboBox.addItem("");
				for (String administratorName : administratorsList) {
					administratorComboBox.addItem(administratorName);
				}
				JLabel passwordLabel = new JLabel("Password:");
				JPasswordField passwordField = new JPasswordField(20);
				
				layout.setHorizontalGroup(
						layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addGroup(
									layout.createSequentialGroup()
									.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
											.addComponent(administratorLabel)
											.addComponent(passwordLabel)
											)
									.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
											.addComponent(administratorComboBox)
											.addComponent(passwordField)
											)
									)
						);
				
				layout.setVerticalGroup(
						layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(administratorLabel)
									.addComponent(administratorComboBox)
									)
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(passwordLabel)
									.addComponent(passwordField)
									)
						);
				
				layout.linkSize(administratorComboBox, passwordField);
				
				JPanel buttonPanel = new JPanel();
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						administrator = (String) administratorComboBox.getSelectedItem();
						String password = String.valueOf(passwordField.getPassword());
						if (!administrator.equals("")) {
							if (!password.equals("")) {
								String encryptedPassword = encryptPassword(password);
								if (!administratorsMap.get(administrator).equals(encryptedPassword)) {
									JOptionPane.showMessageDialog(administratorSelectorDialog, "The pasword for administrator '" + administrator + "' is not correct!", "Incorrect password", JOptionPane.ERROR_MESSAGE);
									administrator = null;
								}
								administratorSelectorDialog.dispose();
							}
							else {
								JOptionPane.showMessageDialog(administratorSelectorDialog, "The pasword may not be empty!", "Empty password", JOptionPane.ERROR_MESSAGE);
							}
						}
						else {
							JOptionPane.showMessageDialog(administratorSelectorDialog, "You must select an administrator!", "No administrator selected", JOptionPane.ERROR_MESSAGE);
						}
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
	
	
	private static void getEncryptedPassword() {
		JFrame passwordEncryptor = new JFrame("Encrypt Password");
		passwordEncryptor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		passwordEncryptor.setLayout(new BorderLayout());
		
		JPanel mainPanel = new JPanel();
		GroupLayout layout = new GroupLayout(mainPanel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		mainPanel.setLayout(layout);
		
		JLabel passwordLabel = new JLabel("Password:");
		JPasswordField passwordField = new JPasswordField(50);
		JLabel encryptedPasswordLabel = new JLabel("Encrypted Password:");
		JTextField encryptedPasswordField = new JTextField(50);
		JButton encryptButton = new JButton("Encrypt");
		encryptButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String password = String.valueOf(passwordField.getPassword());
				if (!password.equals("")) {
					encryptedPasswordField.setText(encryptPassword(password));
				}
			}
		});
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(
							layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(passwordLabel)
									.addComponent(encryptedPasswordLabel)
									)
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(passwordField)
									.addComponent(encryptedPasswordField)
									)
							)
					.addGroup(layout.createSequentialGroup()
							.addComponent(encryptButton)
							.addComponent(closeButton)
							)
				);
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(passwordLabel)
							.addComponent(passwordField)
							)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(encryptedPasswordLabel)
							.addComponent(encryptedPasswordField)
							)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(encryptButton)
							.addComponent(closeButton)
							)
				);

		passwordEncryptor.add(mainPanel, BorderLayout.CENTER);
		passwordEncryptor.pack();
		passwordEncryptor.setVisible(true);
	}
	

	public static void main(String[] args) {
		AdministratorSelector.getEncryptedPassword();
	}
}
