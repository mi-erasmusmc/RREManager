package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.erasmusmc.rremanager.RREManager;
import org.erasmusmc.rremanager.files.AdministratorData;

public class AdministratorDefiner {
	private static int LABELWIDTH    = 90;
	private static int FIELDWIDTH    = 200;
	private static int FIELDHEIGHT   = 25;
	
	private String[] administrator = null;
	
	private JFrame parentFrame;
	private JDialog administratorDialog = null;
	private JTextField nameField = null;
	private JPasswordField passwordField = null;
	private JPasswordField confirmField = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	
	
	public AdministratorDefiner(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}
	
	
	String[] getAdministrator(String[] administrator) {
		this.administrator = administrator;
		askAdministrator();
		return this.administrator;
	}
	
	
	private void askAdministrator() {
		String name       = administrator == null ? "" : administrator[AdministratorData.NAME]    == null ? "" : administrator[AdministratorData.NAME].trim();
		String title      = administrator == null ? "" : administrator[AdministratorData.TITLE]   == null ? "" : administrator[AdministratorData.TITLE].trim();
		String phone      = administrator == null ? "" : administrator[AdministratorData.PHONE]   == null ? "" : administrator[AdministratorData.PHONE].trim();
		String userNumber = administrator == null ? "" : administrator[AdministratorData.ERASMUS] == null ? "" : administrator[AdministratorData.ERASMUS].trim();

		Dimension administratorDialogSize = new Dimension(LABELWIDTH + FIELDWIDTH + 250, (6 * FIELDHEIGHT) + 90);
		administratorDialog = new JDialog(parentFrame, true);
		administratorDialog.setTitle("Add Administrator");
		administratorDialog.setLayout(new BorderLayout());
		administratorDialog.setMinimumSize(administratorDialogSize);
		administratorDialog.setPreferredSize(administratorDialogSize);
		
		JPanel mainPanel = new JPanel();
		GroupLayout layout = new GroupLayout(mainPanel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		mainPanel.setLayout(layout);
		
		JLabel nameLabel = new JLabel("Name:");
		nameField = new JTextField(30);
		if (administrator != null) {
			nameField.setEditable(false);
		}
		else {
			nameField.getDocument().addDocumentListener(new NamePasswordDocumentListener());
		}
		nameField.setText(name);
		JLabel titleLabel = new JLabel("Title:");
		JTextField titleField = new JTextField(30);
		titleField.setText(title);
		JLabel phoneLabel = new JLabel("Phone number:");
		JTextField phoneField = new JTextField(30);
		phoneField.setText(phone);
		JLabel userNumberLabel = new JLabel("Erasmus MC Nr:");
		JTextField userNumberField = new JTextField(30);
		userNumberField.setText(userNumber);
		JLabel passwordLabel = new JLabel("Password:");
		passwordField = new JPasswordField(30);
		passwordField.getDocument().addDocumentListener(new NamePasswordDocumentListener());
		JLabel confirmLabel = new JLabel("Confirm:");
		confirmField = new JPasswordField(30);
		confirmField.getDocument().addDocumentListener(new NamePasswordDocumentListener());
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (nameField.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(parentFrame, "No name specified!", "Name Error", JOptionPane.ERROR_MESSAGE);
				}
				else if (String.valueOf(passwordField.getPassword()).length() < 6) {
					JOptionPane.showMessageDialog(parentFrame, "Password should be 6 characters or more!", "Password Error", JOptionPane.ERROR_MESSAGE);
				}
				else {
					if (administrator == null) {
						administrator = new String[AdministratorData.OBJECT_SIZE];
					}
					administrator[AdministratorData.NAME]     = nameField.getText().trim();
					administrator[AdministratorData.TITLE]    = titleField.getText().trim();
					administrator[AdministratorData.PHONE]    = phoneField.getText().trim();
					administrator[AdministratorData.ERASMUS]  = userNumberField.getText().trim();
					administrator[AdministratorData.PASSWORD] = String.valueOf(passwordField.getPassword()).equals("") ? "" : AdministratorManager.encryptPassword(String.valueOf(passwordField.getPassword()));
					administratorDialog.dispose();
				}
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				administratorDialog.dispose();
			}
		});
	
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(
							layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(nameLabel)
									.addComponent(titleLabel)
									.addComponent(phoneLabel)
									.addComponent(userNumberLabel)
									.addComponent(passwordLabel)
									.addComponent(confirmLabel)
									)
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(nameField)
									.addComponent(titleField)
									.addComponent(phoneField)
									.addComponent(userNumberField)
									.addComponent(passwordField)
									.addComponent(confirmField)
									)
							)
					.addGroup(layout.createSequentialGroup()
							.addComponent(okButton)
							.addComponent(cancelButton)
							)
				);
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(nameLabel)
							.addComponent(nameField)
							)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(titleLabel)
							.addComponent(titleField)
							)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(phoneLabel)
							.addComponent(phoneField)
							)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(userNumberLabel)
							.addComponent(userNumberField)
							)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(passwordLabel)
							.addComponent(passwordField)
							)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(confirmLabel)
							.addComponent(confirmField)
							)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(okButton)
							.addComponent(cancelButton)
							)
				);
		layout.linkSize(nameLabel, titleLabel, phoneLabel, userNumberLabel, passwordLabel, confirmLabel);

		administratorDialog.add(mainPanel, BorderLayout.CENTER);
		administratorDialog.pack();
		administratorDialog.setLocationRelativeTo(parentFrame);
		
		JRootPane rootPane = SwingUtilities.getRootPane(okButton); 
		rootPane.setDefaultButton(okButton);
		
		administratorDialog.setVisible(true);
	}
	
	
	private class NamePasswordDocumentListener implements DocumentListener {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			check();
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			check();
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			check();
		}
		
		private void check() {
			okButton.setEnabled(nameOK() && passwordOK());
		}
		
	}
	
	
	private boolean nameOK() {
		boolean ok = true;

		if (administrator == null) {
			if (RREManager.getIniFile().getValue("Administrators", nameField.getText()) != null) {
				JOptionPane.showMessageDialog(administratorDialog, "Administrator '" + nameField.getText() + "' already exists!", "Name error", JOptionPane.ERROR_MESSAGE);	
				ok = false;
			}
			else {
				ok = true;
			}
		}
		
		return ok;
	}
	
	
	private boolean passwordOK() {
		boolean ok = true;

		if (String.valueOf(passwordField.getPassword()).equals(String.valueOf(confirmField.getPassword()))) {
			if (administrator == null) {
				if (!String.valueOf(passwordField.getPassword()).equals("")) {
					ok = true;
				}
				else {
					ok = false;
				}
			}
			else {
				ok = true;
			}
		}
		else {
			ok = false;
		}

		return ok;
	}

}
