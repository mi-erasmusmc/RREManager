package org.erasmusmc.rremanager.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.JOptionPane;

import org.erasmusmc.rremanager.RREManager;

public class ScriptUtilities {
	
	
	public static boolean callVBS(String vbScript, List<String> arguments) {
		boolean result = true;
		
		String workPath = RREManager.getCurentPath() + File.separator;
		String scriptPath = copyScript(workPath, vbScript);
		
		if (scriptPath != null) {
			File semaphoreFile = new File(workPath + getSemaphoreName());
			semaphoreFile.delete();
			
			String argumentsString = "";
			if (arguments != null) {
				for (String argument : arguments) {
					argumentsString += " & chr(34) & \"" + argument + "\" & chr(34)";
				}
			} 
			
			String scriptWrapper = getScriptWrapper(workPath, scriptPath, argumentsString);
			if (scriptWrapper != null) {
				String command = "wscript \"" + scriptWrapper + "\"";
				try {
					Runtime.getRuntime().exec(command);
					while (!semaphoreFile.exists());
				} catch( IOException e ) {
					result = false;
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "Could not create script wrapper.", "RREManager Error", JOptionPane.ERROR_MESSAGE);
			}

			semaphoreFile.delete();
			if (scriptWrapper != null) {
				(new File(scriptWrapper)).delete();
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "Cannot find VBScript '" + scriptPath + "'.", "RREManager Error", JOptionPane.ERROR_MESSAGE);
		}
		
		if (scriptPath != null) {
			(new File(scriptPath)).delete();
		}
		
		return result;
	}
	
	
	private static String copyScript(String workPath, String scriptName) {
		String scriptFileName = workPath + getScriptName();
		
		InputStream resourceStream = null;
		resourceStream = RREManager.class.getResourceAsStream("scripts/" + scriptName);
		
		try {
			BufferedReader scriptReader = new BufferedReader(new InputStreamReader(resourceStream));
			PrintWriter scriptWriter = new PrintWriter(scriptFileName);
			
			String line = scriptReader.readLine();
			while (line != null) {
				scriptWriter.println(line);
				line = scriptReader.readLine();
			}
			scriptReader.close();
			scriptWriter.close();
		} catch (FileNotFoundException e) {
			scriptFileName = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return scriptFileName;
	}
	
	
	private static String getSemaphoreName() {
		return "ScriptWrapperSemaphore.txt";
	}
	
	
	private static String getScriptName() {
		return "Script.vbs";
	}
	
	
	private static String getScriptWrapper(String workPath, String scriptPath, String argumentsString) {
		String scriptWrapperScript = workPath + "ScriptWrapper.vbs";
		
		try {
			PrintWriter scriptWriter = new PrintWriter(scriptWrapperScript);
			scriptWriter.println("dim objShell, objFSO");
			scriptWriter.println();
			scriptWriter.println("Set objShell = CreateObject(\"Wscript.Shell\")");
			scriptWriter.println("objShell.run chr(34) & \"" + scriptPath + "\" & chr(34)" + argumentsString + ",1,true");
			scriptWriter.println();
			scriptWriter.println("Set objFSO=CreateObject(\"Scripting.FileSystemObject\")");
			scriptWriter.println();
			scriptWriter.println("outFile=\"" + workPath + getSemaphoreName() + "\"");
			scriptWriter.println("Set objFile = objFSO.CreateTextFile(outFile,True)");
			scriptWriter.println("objFile.Write \"Ready\"");
			scriptWriter.println("objFile.Close");
			scriptWriter.close();
		} catch (FileNotFoundException e) {
			scriptWrapperScript = null;
		}
		
		return scriptWrapperScript;
	}

}
