package org.erasmusmc.rremanager.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.erasmusmc.rremanager.RREManager;

public class ScriptUtilities {


	public static boolean callScript(String script, List<String> arguments) {
		boolean result = true;
		if (script.substring(script.lastIndexOf(".")).toLowerCase().equals(".vbs")) {
			callVisualBasicScript(script, arguments);
		}
		else if (script.substring(script.lastIndexOf(".")).toLowerCase().equals(".ps1")) {
			callPowerShellScript(script, arguments);
		}
		else {
			result = false;
		}
		return result;
	}


	private static boolean callVisualBasicScript(String script, List<String> arguments) {
		boolean result = true;

		String workPath = RREManager.getCurentPath() + File.separator;
		String scriptPath = copyScript(workPath, script);

		if (scriptPath != null) {
			File semaphoreFile = new File(workPath + getSemaphoreName());
			semaphoreFile.delete();

			String scriptWrapper = getVisualBasicScriptWrapper(workPath, scriptPath, arguments);
			if (scriptWrapper != null) {
				String command = "wscript \"" + scriptWrapper + "\"";
				try {
					Runtime.getRuntime().exec(command);
					while (!semaphoreFile.exists());
					BufferedReader semaphoreFileReader = new BufferedReader(new FileReader(semaphoreFile));
					String resultMessage = semaphoreFileReader.readLine();
					semaphoreFileReader.close();
					if (!resultMessage.equals("Ready")) {
						result = false;
						JOptionPane.showMessageDialog(null, script + ": " + resultMessage, "RREManager Script Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch( IOException e ) {
					result = false;
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "Could not create script wrapper.", "RREManager Error", JOptionPane.ERROR_MESSAGE);
			}

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
		if (workPath != null) {
			(new File(workPath + getSemaphoreName())).delete();
		}

		return result;
	}


	private static String getVisualBasicScriptWrapper(String workPath, String scriptPath, List<String> arguments) {
		String scriptWrapperScript = workPath + "ScriptWrapper.vbs";

		try {
			String argumentsString = "";
			if (arguments != null) {
				for (String argument : arguments) {
					argumentsString += " & \" \" & chr(34) & \"" + argument + "\" & chr(34)";
				}
			}

			PrintWriter scriptWriter = new PrintWriter(scriptWrapperScript);
			scriptWriter.println("dim command, exitCode, objShell, objFSO");
			scriptWriter.println();
			scriptWriter.println("Set objShell = CreateObject(\"Wscript.Shell\")");
			scriptWriter.println("command = \"wscript.exe \" & chr(34) & \"" + scriptPath + "\" & chr(34)" + argumentsString);
			scriptWriter.println("exitCode = objShell.Run(command,1,true)");
			scriptWriter.println();
			scriptWriter.println("Set objFSO=CreateObject(\"Scripting.FileSystemObject\")");
			scriptWriter.println("outFile=\"" + workPath + getSemaphoreName() + "\"");
			scriptWriter.println("Set objFile = objFSO.CreateTextFile(outFile,True)");
			scriptWriter.println();
			scriptWriter.println("If exitCode > 0 Then");
			scriptWriter.println("  objFile.WriteLine \"ExitCode=\" & exitCode");
			scriptWriter.println("Else");
			scriptWriter.println("  objFile.WriteLine \"Ready\"");
			scriptWriter.println("End If");
			scriptWriter.println("objFile.WriteLine command");
			scriptWriter.println();
			scriptWriter.println("objFile.Close");
			scriptWriter.close();
		} catch (FileNotFoundException e) {
			scriptWrapperScript = null;
		}

		return scriptWrapperScript;
	}


	private static boolean callPowerShellScript(String script, List<String> arguments) {
		boolean result = true;

		String workPath = "D:\\Temp\\RRE\\"; //RREManager.getCurentPath() + File.separator;
		String scriptPath = copyScript(workPath, script);

		if (scriptPath != null) {
			File semaphoreFile = new File(workPath + getSemaphoreName());
			semaphoreFile.delete();

			String scriptWrapper = getPowerShellScriptWrapper(workPath, scriptPath, arguments);
			try {
				Runtime.getRuntime().exec("powershell -file \"" + scriptWrapper + "\"");
				while (!semaphoreFile.exists());
				TimeUnit.SECONDS.sleep(2);
				BufferedReader semaphoreFileReader = new BufferedReader(new FileReader(semaphoreFile));
				String resultMessage = semaphoreFileReader.readLine();
				semaphoreFileReader.close();
				if (!resultMessage.equals("Ready")) {
					result = false;
					JOptionPane.showMessageDialog(null, script + ": " + resultMessage, "RREManager Script Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch( IOException e ) {
				result = false;
			} catch (InterruptedException e) {
				//Do nothing
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "Cannot find VBScript '" + scriptPath + "'.", "RREManager Error", JOptionPane.ERROR_MESSAGE);
		}

		if (scriptPath != null) {
			(new File(scriptPath)).delete();
		}
		if (workPath != null) {
			(new File(workPath + getSemaphoreName())).delete();
		}

		return result;
	}


	private static String getPowerShellScriptWrapper(String workPath, String scriptPath, List<String> arguments) {
		String scriptWrapperScript = workPath + "ScriptWrapper.ps1";

		try {
			String argumentsString = "";
			if (arguments != null) {
				for (String argument : arguments) {
					argumentsString += " " + argument.replaceAll("\"", "\"\"");
				}
			}

			PrintWriter scriptWriter = new PrintWriter(scriptWrapperScript);
			scriptWriter.println("$command = \"& \"\"" + scriptPath + "\"\"" + argumentsString + "\"");
			scriptWriter.println("Invoke-Expression $command");
			scriptWriter.println("$exitCode = $LASTEXITCODE");
			scriptWriter.println("if ($exitCode -gt 0) {");
			scriptWriter.println("  $errorString = \"ExitCode=\" + $exitCode");
			scriptWriter.println("  $errorString | Out-File -FilePath \"" + workPath + getSemaphoreName() + "\" -Encoding ASCII");
			scriptWriter.println("}");
			scriptWriter.println("else {");
			scriptWriter.println("  \"Ready\" | Out-File -FilePath \"" + workPath + getSemaphoreName() + "\" -Encoding ASCII");
			scriptWriter.println("}");
			scriptWriter.println("$command | Out-File -FilePath \"" + workPath + getSemaphoreName() + "\" -Encoding ASCII -Append");
			scriptWriter.close();
		} catch (FileNotFoundException e) {
			scriptWrapperScript = null;
		}

		return scriptWrapperScript;
	}


	private static String copyScript(String workPath, String scriptName) {
		String scriptFileName = workPath + getScriptName(scriptName);

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


	private static String getScriptName(String orgScriptName) {
		return "Script" + orgScriptName.substring(orgScriptName.lastIndexOf("."));
	}

/*
	public static void main(String[] args) {
		List<String> arguments = new ArrayList<String>();
		arguments.add("-Text1 \"Hello\"");
		arguments.add("-Text2 \"World!\"");
		callScript("testScript.ps1", arguments);
		System.out.println("Ready");
	}
*/
}
