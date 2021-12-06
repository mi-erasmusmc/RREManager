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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.erasmusmc.rremanager.files.UserData;

public class EmailEditor {
	private static int LABEL_SIZE   = 50;
	private static int FIELD_HEIGHT = 28;
	
	private JFrame parentFrame;
	private InfoRow subjectRow = null;
	private JEditorPane emailHTMLField = null;
	private boolean approved = false;
	private String approvedText = null;
	private String approvedSubject = null;
	
	
	public EmailEditor(JFrame paremtFrame) {
		this.parentFrame = paremtFrame;
	}
	
	
	public void editEmail(String emailText, String emailFormat, String[] user, String subject, boolean editable) {
		approved = false;
		approvedText = null;
		Dimension emailEditorDialogSize = new Dimension(800, 800);
		JDialog emailEditorDialog = new JDialog(parentFrame, true);
		emailEditorDialog.setTitle("Email Reviewer");
		emailEditorDialog.setLayout(new BorderLayout());
		emailEditorDialog.setMinimumSize(emailEditorDialogSize);
		emailEditorDialog.setPreferredSize(emailEditorDialogSize);
		
		String to = UserData.getUserDescription(user, true);
		
		JPanel toPanel = new JPanel(new BorderLayout());
		toPanel.add(new InfoRow("To:", to, false), BorderLayout.NORTH);
		
		JPanel subjectPanel = new JPanel(new BorderLayout());
		subjectRow = new InfoRow("Subject:", subject, editable);
		subjectPanel.add(subjectRow, BorderLayout.NORTH);
		toPanel.add(subjectPanel, BorderLayout.CENTER);
		
		JTabbedPane tabbedPane = null;
		JPanel rawPanel = null;
		JPanel previewPanel = null;

		rawPanel = new JPanel(new BorderLayout());
		JPanel textPanel = new JPanel(new BorderLayout());
		JTextArea emailTextField = new JTextArea(emailText);
		emailTextField.setEditable(editable);
		textPanel.add(emailTextField, BorderLayout.CENTER);
		JScrollPane emailReviewScrollPane = new JScrollPane(textPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		rawPanel.add(emailReviewScrollPane, BorderLayout.CENTER);
		

		if (!emailFormat.equals("TEXT")) {
			previewPanel = new JPanel(new BorderLayout());emailHTMLField = new JEditorPane();
	        emailHTMLField.setEditable(false);
	        emailHTMLField.setContentType("text/html");
	        emailHTMLField.setText(emailText);
	        emailHTMLField.setEditable(false);
			emailReviewScrollPane = new JScrollPane(emailHTMLField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			previewPanel.add(emailReviewScrollPane, BorderLayout.CENTER);
		}
		
		if (editable) {
			if (emailFormat.equals("TEXT")) {
				subjectPanel.add(rawPanel, BorderLayout.CENTER);
			}
			else {
				tabbedPane = new JTabbedPane();
				tabbedPane.addTab("HTML", rawPanel);
				tabbedPane.addTab("Preview", previewPanel);
				subjectPanel.add(tabbedPane, BorderLayout.CENTER);
				tabbedPane.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent changeEvent) {
						JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
						int index = sourceTabbedPane.getSelectedIndex();
						if (sourceTabbedPane.getTitleAt(index).equals("Preview")) {
					        emailHTMLField.setText(emailTextField.getText());
						}
					}
				});
			}
		}
		else {
			subjectPanel.add(previewPanel == null ? rawPanel : previewPanel, BorderLayout.CENTER);
		}
		
		JPanel buttonPanel = new JPanel();
		JButton approveButton = new JButton("Approved");
		approveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!subjectRow.getInfo().equals("")) {
					approved = true;
					approvedText = emailTextField.getText();
					approvedSubject = subjectRow.getInfo();
					emailEditorDialog.dispose();
				}
				else {
					JOptionPane.showMessageDialog(parentFrame, "The subject may not be empty", "Empty subject", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		JButton rejectButton = new JButton("Reject");
		rejectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				approved = false;
				approvedText = null;
				approvedSubject = null;
				emailEditorDialog.dispose();
			}
		});
		buttonPanel.add(approveButton);
		buttonPanel.add(rejectButton);

        emailEditorDialog.add(toPanel, BorderLayout.CENTER);
		emailEditorDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		emailEditorDialog.pack();
		emailEditorDialog.setLocationRelativeTo(parentFrame);
		
		emailReviewScrollPane.getViewport().setViewPosition( new Point(0, 0) );
		
		emailEditorDialog.setVisible(true);
	}
	
	
	public boolean isApproved() {
		return approved;
	}
	
	
	public String getApprovedText() {
		return approvedText;
	}
	
	
	public String getApprovedSubject() {
		return approvedSubject;
	}
	
	
	private class InfoRow extends JPanel {
		private static final long serialVersionUID = 1462355636131464143L;
		
		private JTextField infoField = null;

		public InfoRow(String label, String info, boolean editable) {
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
			 
			infoField = new JTextField();
			infoField.setText(info);
			infoField.setMinimumSize(new Dimension(10, FIELD_HEIGHT));
			infoField.setMaximumSize(new Dimension(10000, FIELD_HEIGHT));
			infoField.setPreferredSize(new Dimension(10000, FIELD_HEIGHT));
			infoField.setEditable(editable);

			add(fieldLabelPanel);
			add(infoField);
		}
		
		
		public String getInfo() {
			return infoField.getText().trim();
		}
	}

}
