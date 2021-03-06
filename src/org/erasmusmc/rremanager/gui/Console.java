/*******************************************************************************
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.erasmusmc.rremanager.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.erasmusmc.rremanager.files.WriteTextFile;

public class Console extends OutputStream {
	
	private StringBuffer	buffer	= new StringBuffer();
	private WriteTextFile	debug	= null;
	private List<JTextArea>	textAreas = new ArrayList<JTextArea>();;
	
	public void println(String string) {
		for (JTextArea textArea : textAreas) {
			textArea.append(string + "\n");
			textArea.repaint();
		}
		System.out.println(string);
	}
	
	public void print(String string) {
		for (JTextArea textArea : textAreas) {
			textArea.append(string);
			textArea.repaint();
		}
		System.out.print(string);
	}
	
	public void addTextArea(JTextArea textArea) {
		if (!textAreas.contains(textArea)) {
			textAreas.add(textArea);
		}
	}
	
	public void removeTextArea(JTextArea textArea) {
		textAreas.remove(textArea);
	}
	
	public void setDebugFile(String filename) {
		closeDebugFile();
		for (JTextArea textArea : textAreas) {
			textArea.setText("");
		}
		if (filename != null) {
			debug = new WriteTextFile(filename);
		}
		else {
			debug = null;
		}
	}
	
	public void closeDebugFile() {
		debug = null;
	}
	
	public void clear() {
		closeDebugFile();
		for (JTextArea textArea : textAreas) {
			textArea.setText("");
		}
	}
	
	public String getText(JTextArea textArea) {
		try {
			return textArea.getDocument().getText(0, textArea.getDocument().getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void write(int b) throws IOException {
		buffer.append((char) b);
		if ((char) b == '\n') {
			for (JTextArea textArea : textAreas) {
				textArea.append(buffer.toString());
				textArea.setCaretPosition(textArea.getDocument().getLength());
			}
			if (debug != null) {
				String line = buffer.toString();
				while ((line.length() > 0) && ((line.substring(line.length() - 1).equals("\r")) || (line.substring(line.length() - 1).equals("\n")))) {
					line = line.substring(0, line.length() - 2);
				}
				debug.writeln(line);
			}
			buffer = new StringBuffer();
		}
	}
	
}
