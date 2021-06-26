package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

public class PasswordManager {
	private String password;
	private JFrame parentFrame;
	
	
	public PasswordManager(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}
	
	
	public String getPassword(String userName) {
		Dimension dimension = new Dimension(250, 100);
		JDialog passwordDialog = new JDialog(parentFrame, true);
		passwordDialog.setTitle("Password for user " + userName);
		passwordDialog.setLayout(new BorderLayout());
		passwordDialog.setMinimumSize(dimension);
		passwordDialog.setMaximumSize(dimension);
		passwordDialog.setPreferredSize(dimension);
		
		JPanel passwordPanel = new JPanel();
		JLabel passwordLabel = new JLabel("Password:");
		JPasswordField passwordField = new JPasswordField(10);
		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordField);
		
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				password = new String(passwordField.getPassword());
				passwordDialog.dispose();
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				password = null;
				passwordDialog.dispose();
			}
		});
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		passwordDialog.add(passwordPanel, BorderLayout.CENTER);
		passwordDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		passwordDialog.pack();
		passwordDialog.setLocationRelativeTo(parentFrame);
		
		JRootPane rootPane = SwingUtilities.getRootPane(okButton); 
		rootPane.setDefaultButton(okButton);
		
		passwordDialog.setVisible(true);
		
		return password;
	}

}
