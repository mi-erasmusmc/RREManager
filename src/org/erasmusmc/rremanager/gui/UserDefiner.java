package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class UserDefiner {
	private static int LABELWIDTH    = 80;
	private static int FIELDWIDTH    = 255;
	private static int ROWHEIGHT     = 25;
	
	private JFrame parentFrame;
	private String firstName = null;
	private String Initials = null;
	private String lastName = null;
	private String userName = null;
	private String password = null;
	private String emailAddress = null;
	private String projects = null;
	private String groups = null;
	private String ipAddresses = null;
	
		
	public UserDefiner(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}
	
	
	Map<String, String> getUser() {
		Map<String, String> user = null;
		askUser();
		return user;
	}
	
	
	private void askUser() {
		firstName = null;
		Initials = null;
		lastName = null;
		userName = null;
		password = null;
		emailAddress = null;
		projects = null;
		groups = null;
		ipAddresses = null;
		
		Dimension userDialogSize = new Dimension(350, (6 * ROWHEIGHT) + 90);
		JDialog userDialog = new JDialog(parentFrame, true);
		userDialog.setTitle("New User");
		userDialog.setLayout(new BorderLayout());
		userDialog.setMinimumSize(userDialogSize);
		userDialog.setPreferredSize(userDialogSize);
		
		JLabel firstNameLabel = new JLabel("First name:");
		JTextField firstNameField = new JTextField();
		JLabel initialsLabel = new JLabel("Initials:");
		JTextField initialsField = new JTextField();
		JLabel lastNameLabel = new JLabel("Last name:");
		JTextField lastNameField = new JTextField();
		JLabel userNameLabel = new JLabel("User name:");
		JTextField userNameField = new JTextField();
		JLabel passwordLabel = new JLabel("Password:");
		JTextField passwordField = new JTextField();
		JLabel emailAddressLabel = new JLabel("Email address:");
		JTextField emailAddressField = new JTextField();

		JPanel userPanel = new JPanel();
		GroupLayout layout = new GroupLayout(userPanel);
		userPanel.setLayout(layout);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(firstNameLabel)
						.addComponent(lastNameLabel)
						.addComponent(userNameLabel)
						.addComponent(emailAddressLabel)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(firstNameField)
								.addComponent(initialsLabel)
								.addComponent(initialsField)
								)
						.addComponent(lastNameField)
						.addComponent(userNameField)
						.addComponent(emailAddressField)
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
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(emailAddressLabel)
						.addComponent(emailAddressField)
						)
				);
		
		layout.linkSize(firstNameLabel, lastNameLabel, userNameLabel, emailAddressLabel);
		layout.linkSize(lastNameField, userNameField, emailAddressField);
		
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				userName = userNameField.getText().trim();
				if (!userName.equals("")) {
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
				userName = null;
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
		
		userDialog.setVisible(true);
	}

}
