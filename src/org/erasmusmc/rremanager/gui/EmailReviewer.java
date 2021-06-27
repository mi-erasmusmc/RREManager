package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.erasmusmc.rremanager.files.UserData;

public class EmailReviewer {
	private static int LABEL_SIZE   = 50;
	private static int FIELD_HEIGHT = 28;
	
	private JFrame parentFrame;
	private boolean approved = false;
	
	
	public EmailReviewer(JFrame paremtFrame) {
		this.parentFrame = paremtFrame;
	}
	
	
	public void reviewEmail(String emailText, String emailFormat, String[] user, String subject) {
		approved = false;
		Dimension ipSelectorDialogSize = new Dimension(800, 800);
		JDialog emailReviewerDialog = new JDialog(parentFrame, true);
		emailReviewerDialog.setTitle("Email Reviewer");
		emailReviewerDialog.setLayout(new BorderLayout());
		emailReviewerDialog.setMinimumSize(ipSelectorDialogSize);
		emailReviewerDialog.setPreferredSize(ipSelectorDialogSize);
		
		String to = UserData.getUserDescription(user);
		
		JPanel toPanel = new JPanel(new BorderLayout());
		toPanel.add(new InfoRow("To:", to), BorderLayout.NORTH);
		
		JPanel subjectPanel = new JPanel(new BorderLayout());
		subjectPanel.add(new InfoRow("Subject:", subject), BorderLayout.NORTH);
		toPanel.add(subjectPanel, BorderLayout.CENTER);
		
		JPanel emailPanel = new JPanel(new BorderLayout());
		JScrollPane emailReviewScrollPane;
		if (emailFormat.equals("TEXT")) {
			JPanel textPanel = new JPanel(new BorderLayout());
			JTextArea emailTextField = new JTextArea(emailText);
			emailTextField.setEditable(false);
			textPanel.add(emailTextField, BorderLayout.CENTER);
			emailReviewScrollPane = new JScrollPane(textPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			emailPanel.add(emailReviewScrollPane, BorderLayout.CENTER);
		}
		else {
	        JEditorPane emailHTMLField = new JEditorPane();
	        emailHTMLField.setEditable(false);
	        emailHTMLField.setContentType("text/html");
	        emailHTMLField.setText(emailText);
	        emailHTMLField.setEditable(false);
			emailReviewScrollPane = new JScrollPane(emailHTMLField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			emailPanel.add(emailReviewScrollPane, BorderLayout.CENTER);
		}
		subjectPanel.add(emailPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton approveButton = new JButton("Approved");
		approveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				approved = true;
				emailReviewerDialog.dispose();
			}
		});
		JButton rejectButton = new JButton("Reject");
		rejectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				approved = false;
				emailReviewerDialog.dispose();
			}
		});
		buttonPanel.add(approveButton);
		buttonPanel.add(rejectButton);

        emailReviewerDialog.add(toPanel, BorderLayout.CENTER);
		emailReviewerDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		emailReviewerDialog.pack();
		emailReviewerDialog.setLocationRelativeTo(parentFrame);
		
		emailReviewScrollPane.getViewport().setViewPosition( new Point(0, 0) );
		
		emailReviewerDialog.setVisible(true);
	}
	
	
	public boolean isApproved() {
		return approved;
	}
	
	
	private class InfoRow extends JPanel {
		private static final long serialVersionUID = 1462355636131464143L;

		public InfoRow(String label, String info) {
			this.setMinimumSize(new Dimension(10, FIELD_HEIGHT));
			this.setMaximumSize(new Dimension(10000, FIELD_HEIGHT));
			this.setPreferredSize(new Dimension(10000, FIELD_HEIGHT));
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			JPanel fieldLabelPanel = new JPanel(new BorderLayout());
			fieldLabelPanel.setMinimumSize(new Dimension(LABEL_SIZE, FIELD_HEIGHT));
			fieldLabelPanel.setMaximumSize(new Dimension(LABEL_SIZE, FIELD_HEIGHT));
			fieldLabelPanel.setPreferredSize(new Dimension(LABEL_SIZE, FIELD_HEIGHT));
			
			JPanel fileLabelPanel = new JPanel(new BorderLayout());
			JLabel fileLabel = new JLabel(label);
			fileLabelPanel.add(fileLabel, BorderLayout.WEST);
			
			fieldLabelPanel.add(fileLabelPanel, BorderLayout.CENTER);
			 
			JTextField userNamesField = new JTextField();
			userNamesField.setText(info);
			userNamesField.setMinimumSize(new Dimension(10, FIELD_HEIGHT));
			userNamesField.setMaximumSize(new Dimension(10000, FIELD_HEIGHT));
			userNamesField.setPreferredSize(new Dimension(10000, FIELD_HEIGHT));
			userNamesField.setEditable(false);

			add(fieldLabelPanel);
			add(userNamesField);
		}
	}

}
