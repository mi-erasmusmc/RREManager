package org.erasmusmc.rremanager.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
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
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.erasmusmc.rremanager.files.UserData;

public class EmailEditor {
	private final static int LABEL_SIZE               = 50;
	private final static int FIELD_HEIGHT             = 25;
	private final static int VARIABLES_PANEL_MI_WIDTH = 500;
	private final static int VARIABLE_LABEL_WIDTH     = 100;
	private final static int MULTILINE_VARIABLE_LINES = 20;
	
	private JFrame parentFrame;
	private InfoRow subjectRow = null;
	private JEditorPane emailHTMLField = null;
	private boolean approved = false;
	private String approvedText = null;
	private String approvedSubject = null;
	private String orgSubject = null;
	private String orgEmailText = null;
	private Map<String, String> additionalInfo = new HashMap<String, String>();
	
	
	public EmailEditor(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}
	
	
	public void setAdditionalInfo(Map<String, String> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}
	
	
	public Map<String, String> getAdditionalInfo() {
		return additionalInfo;
	}
	
	
	public void editEmail(String emailText, String emailFormat, String[] recipient, List<String[]> bccRecipients, String subject, boolean editable) {
		approved = false;
		approvedText = null;
		orgSubject = subject;
		orgEmailText = emailText;
		
		Dimension emailEditorDialogSize = new Dimension(600 + VARIABLES_PANEL_MI_WIDTH + 30, 800);
		JDialog emailEditorDialog = new JDialog(parentFrame, true);
		emailEditorDialog.setTitle("Email Reviewer");
		emailEditorDialog.setLayout(new BorderLayout());
		emailEditorDialog.setMinimumSize(emailEditorDialogSize);
		emailEditorDialog.setPreferredSize(emailEditorDialogSize);
		
		String to = UserData.getUserDescription(recipient, true);
		String bcc = "";
		if (bccRecipients != null) {
			for (String[] bccRecipient : bccRecipients) {
				bcc += (bcc.equals("") ? "" : ", ") + UserData.getUserDescription(bccRecipient, true);
			}
		}
		
		JPanel toPanel = new JPanel(new BorderLayout());
		toPanel.add(new InfoRow("To:", to, false), BorderLayout.NORTH);
		
		JPanel bccPanel = new JPanel(new BorderLayout());
		bccPanel.add(new InfoRow("BCC:", bcc, false), BorderLayout.NORTH);
		toPanel.add(bccPanel, BorderLayout.CENTER);
		
		JPanel subjectPanel = new JPanel(new BorderLayout());
		subjectRow = new InfoRow("Subject:", subject, editable);
		subjectPanel.add(subjectRow, BorderLayout.NORTH);
		bccPanel.add(subjectPanel, BorderLayout.CENTER);
		
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
			previewPanel = new JPanel(new BorderLayout());
			emailHTMLField = new JEditorPane();
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

		JPanel variablesPanel = new JPanel(new BorderLayout());
		variablesPanel.setBorder(BorderFactory.createTitledBorder("Variables"));
		
		JPanel variablesScrollPanel = new JPanel(new BorderLayout());
		JPanel currentParentPanel = variablesScrollPanel;
		JPanel nextParentPanel = null;
		List<String> variables = findNotSetVariables(subject + emailText);
		Map<JTextComponent, String> fieldVariableMap = new HashMap<JTextComponent, String>();
		Map<String, JTextComponent> variableFieldMap = new HashMap<String, JTextComponent>();
		for (String variable : variables) {
			if (!additionalInfo.containsKey(variable)) {
				additionalInfo.put(variable, null);
			}
			JPanel variablePanel = new JPanel();
			variablePanel.setLayout(new BoxLayout(variablePanel, BoxLayout.X_AXIS));
			
			JLabel variableLabel = new JLabel(variable);
			variableLabel.setMinimumSize(new Dimension(VARIABLE_LABEL_WIDTH, FIELD_HEIGHT));
			variableLabel.setMaximumSize(new Dimension(VARIABLE_LABEL_WIDTH, FIELD_HEIGHT));
			variableLabel.setPreferredSize(new Dimension(VARIABLE_LABEL_WIDTH, FIELD_HEIGHT));
			JTextComponent variableValueField = null;
			if (variable.startsWith("M:")) {
				variableValueField = new JTextArea(10, 50);
				variableValueField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
				int textAreaHeight = Math.floorDiv(MULTILINE_VARIABLE_LINES * 33, 2);
				variableValueField.setMinimumSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableValueField.setPreferredSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));

				JPanel variableLabelPanel = new JPanel(new BorderLayout());
				variableLabelPanel.add(variableLabel);
				variableLabelPanel.setMinimumSize(new Dimension(VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableLabelPanel.setMaximumSize(new Dimension(VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableLabelPanel.setPreferredSize(new Dimension(VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableLabelPanel.add(variableLabel, BorderLayout.NORTH);
				variablePanel.add(variableLabelPanel);
				
				JPanel variableValueFieldPanel = new JPanel(new BorderLayout()); 
				variableValueFieldPanel.add(variableValueField);
				variableValueFieldPanel.setMinimumSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableValueFieldPanel.setPreferredSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableValueFieldPanel.add(variableValueField, BorderLayout.NORTH);
				variablePanel.add(variableValueFieldPanel);
				
				variablePanel.setMinimumSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));
				variablePanel.setPreferredSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));
			}
			else {
				variableValueField = new JTextField(50);
				String value = additionalInfo.get(variable); 
				if (value != null) {
					variableValueField.setText(value);
				}
				variableValueField.setMinimumSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, FIELD_HEIGHT));
				variableValueField.setPreferredSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, FIELD_HEIGHT));
				
				variablePanel.add(variableLabel);
				variablePanel.add(variableValueField);
				
				variablePanel.setMinimumSize(new Dimension(VARIABLES_PANEL_MI_WIDTH, FIELD_HEIGHT));
				variablePanel.setPreferredSize(new Dimension(VARIABLES_PANEL_MI_WIDTH, FIELD_HEIGHT));
			}
			
			fieldVariableMap.put(variableValueField, variable);
			variableFieldMap.put(variable, variableValueField);
			variableValueField.getDocument().addDocumentListener(new DocumentListener() {
				
				@Override
				public void removeUpdate(DocumentEvent e) {
					filter();
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					filter();
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
					filter();
				}
				
				private void filter() {
	                String text = variableFieldMap.get(variable).getText();
	                if (text.length() == 0) {
	                	text = null;
	                }
                    if ((text != null) && variable.startsWith("M:") && emailFormat.equals("HTML")) {
                    	String[] textSplit = text.split("\n");
                    	text = String.join("<br>", textSplit);
                    }
                    additionalInfo.put(variable, text);
                    subjectRow.setText(replaceVariables(orgSubject, additionalInfo));
                    emailTextField.setText(replaceVariables(orgEmailText, additionalInfo));
            		if (!emailFormat.equals("TEXT")) {
				        emailHTMLField.setText(emailTextField.getText());
            		}
	             }
			});
			
			nextParentPanel = new JPanel(new BorderLayout());
			currentParentPanel.add(variablePanel, BorderLayout.NORTH);
			currentParentPanel.add(nextParentPanel, BorderLayout.CENTER);
			currentParentPanel = nextParentPanel;			
		}
		
        JScrollPane variablesScrollPane = new JScrollPane(variablesScrollPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        variablesPanel.add(variablesScrollPane, BorderLayout.CENTER);
		
        emailEditorDialog.add(toPanel, BorderLayout.CENTER);
        emailEditorDialog.add(variablesPanel, BorderLayout.EAST);
		emailEditorDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		emailEditorDialog.pack();
		emailEditorDialog.setLocationRelativeTo(parentFrame);
		
		emailReviewScrollPane.getViewport().setViewPosition( new Point(0, 0) );

        subjectRow.setText(replaceVariables(orgSubject, additionalInfo));
        emailTextField.setText(replaceVariables(orgEmailText, additionalInfo));
		if (!emailFormat.equals("TEXT")) {
	        emailHTMLField.setText(emailTextField.getText());
		}
		
		emailEditorDialog.setVisible(true);
	}
	
	
	public void editEmailOld(String emailText, String emailFormat, String[] user, String subject, boolean editable) {
		approved = false;
		approvedText = null;
		orgSubject = subject;
		orgEmailText = emailText;
		
		Dimension emailEditorDialogSize = new Dimension(600 + VARIABLES_PANEL_MI_WIDTH + 30, 800);
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
			previewPanel = new JPanel(new BorderLayout());
			emailHTMLField = new JEditorPane();
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

		JPanel variablesPanel = new JPanel(new BorderLayout());
		variablesPanel.setBorder(BorderFactory.createTitledBorder("Variables"));
		
		JPanel variablesScrollPanel = new JPanel(new BorderLayout());
		JPanel currentParentPanel = variablesScrollPanel;
		JPanel nextParentPanel = null;
		List<String> variables = findNotSetVariables(subject + emailText);
		Map<String, String> variableValueMap = new HashMap<String, String>();
		Map<JTextComponent, String> fieldVariableMap = new HashMap<JTextComponent, String>();
		Map<String, JTextComponent> variableFieldMap = new HashMap<String, JTextComponent>();
		for (String variable : variables) {
			variableValueMap.put(variable, null);
			JPanel variablePanel = new JPanel();
			variablePanel.setLayout(new BoxLayout(variablePanel, BoxLayout.X_AXIS));
			
			JLabel variableLabel = new JLabel(variable);
			variableLabel.setMinimumSize(new Dimension(VARIABLE_LABEL_WIDTH, FIELD_HEIGHT));
			variableLabel.setMaximumSize(new Dimension(VARIABLE_LABEL_WIDTH, FIELD_HEIGHT));
			variableLabel.setPreferredSize(new Dimension(VARIABLE_LABEL_WIDTH, FIELD_HEIGHT));
			JTextComponent variableValueField = null;
			if (variable.startsWith("M:")) {
				variableValueField = new JTextArea(10, 50);
				variableValueField.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
				int textAreaHeight = Math.floorDiv(MULTILINE_VARIABLE_LINES * 33, 2);
				variableValueField.setMinimumSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableValueField.setPreferredSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));

				JPanel variableLabelPanel = new JPanel(new BorderLayout());
				variableLabelPanel.add(variableLabel);
				variableLabelPanel.setMinimumSize(new Dimension(VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableLabelPanel.setMaximumSize(new Dimension(VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableLabelPanel.setPreferredSize(new Dimension(VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableLabelPanel.add(variableLabel, BorderLayout.NORTH);
				variablePanel.add(variableLabelPanel);
				
				JPanel variableValueFieldPanel = new JPanel(new BorderLayout()); 
				variableValueFieldPanel.add(variableValueField);
				variableValueFieldPanel.setMinimumSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableValueFieldPanel.setPreferredSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));
				variableValueFieldPanel.add(variableValueField, BorderLayout.NORTH);
				variablePanel.add(variableValueFieldPanel);
				
				variablePanel.setMinimumSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));
				variablePanel.setPreferredSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, textAreaHeight));
			}
			else {
				variableValueField = new JTextField(50);
				variableValueField.setMinimumSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, FIELD_HEIGHT));
				variableValueField.setPreferredSize(new Dimension(VARIABLES_PANEL_MI_WIDTH - VARIABLE_LABEL_WIDTH, FIELD_HEIGHT));
				
				variablePanel.add(variableLabel);
				variablePanel.add(variableValueField);
				
				variablePanel.setMinimumSize(new Dimension(VARIABLES_PANEL_MI_WIDTH, FIELD_HEIGHT));
				variablePanel.setPreferredSize(new Dimension(VARIABLES_PANEL_MI_WIDTH, FIELD_HEIGHT));
			}
			
			fieldVariableMap.put(variableValueField, variable);
			variableFieldMap.put(variable, variableValueField);
			variableValueField.getDocument().addDocumentListener(new DocumentListener() {
				
				@Override
				public void removeUpdate(DocumentEvent e) {
					filter();
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					filter();
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
					filter();
				}
				
				private void filter() {
	                String text = variableFieldMap.get(variable).getText();
	                if (text.length() == 0) {
	                	text = null;
	                }
                    if ((text != null) && variable.startsWith("M:") && emailFormat.equals("HTML")) {
                    	String[] textSplit = text.split("\n");
                    	text = String.join("<br>", textSplit);
                    }
                    variableValueMap.put(variable, text);
                    subjectRow.setText(replaceVariables(orgSubject, variableValueMap));
                    emailTextField.setText(replaceVariables(orgEmailText, variableValueMap));
            		if (!emailFormat.equals("TEXT")) {
				        emailHTMLField.setText(emailTextField.getText());
            		}
	             }
			});
			
			nextParentPanel = new JPanel(new BorderLayout());
			currentParentPanel.add(variablePanel, BorderLayout.NORTH);
			currentParentPanel.add(nextParentPanel, BorderLayout.CENTER);
			currentParentPanel = nextParentPanel;			
		}
		
        JScrollPane variablesScrollPane = new JScrollPane(variablesScrollPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        variablesPanel.add(variablesScrollPane, BorderLayout.CENTER);
		
        emailEditorDialog.add(toPanel, BorderLayout.CENTER);
        emailEditorDialog.add(variablesPanel, BorderLayout.EAST);
		emailEditorDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		emailEditorDialog.pack();
		emailEditorDialog.setLocationRelativeTo(parentFrame);
		
		emailReviewScrollPane.getViewport().setViewPosition( new Point(0, 0) );
		
		emailEditorDialog.setVisible(true);
	}
	
	
	private List<String> findNotSetVariables(String text) {
		List<String> notSetVariables = new ArrayList<String>();
		
		String currentVariable = null;
		for (int charNr = 0; charNr < text.length(); charNr++) {
			String currentCharacter = text.substring(charNr, charNr + 1); 
			if (currentVariable == null) {
				if (currentCharacter.equals("[")) {
					currentVariable = "";
				}
			}
			else {
				if (currentCharacter.equals("]")) {
					if (!notSetVariables.contains(currentVariable)) {
						notSetVariables.add(currentVariable);
					}
					currentVariable = null;
				}
				else {
					currentVariable += currentCharacter; 
				}
			}
		}
		return notSetVariables;
	}
	
	
	private String replaceVariables(String text, Map<String, String> variableValues) {
		for (String variable : variableValues.keySet()) {
			String value = variableValues.get(variable);
			if (value != null) {
				text = text.replaceAll("\\[" + variable + "\\]", value);
			}
		}
		return text;
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
		
		
		public void setText(String text) {
			infoField.setText(text);
		}
	}

}
