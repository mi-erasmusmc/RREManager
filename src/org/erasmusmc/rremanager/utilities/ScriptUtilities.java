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
	
	
	public static void deleteOutputFiles() {
		String workPath = RREManager.getCurentPath() + File.separator;
		File folder = new File(workPath);
		String[] fileList = folder.list();
		for (String outputFileName : fileList) {
			File outputFile = new File(outputFileName);
			if (outputFile.isFile() && outputFileName.endsWith("_Output.txt")) {
				outputFile.delete();
			}
		}
	}
	
	
	public static void saveFileZillaXMLFile(String userName) {
		String scriptFileName = RREManager.getIniFile().getValue("FileZilla", "XMLFolder") + File.separator + "FileZilla " + userName + ".xml";

		InputStream resourceStream = null;
		resourceStream = RREManager.class.getResourceAsStream("scripts/FileZilla.xml");

		try {
			BufferedReader scriptReader = new BufferedReader(new InputStreamReader(resourceStream));
			PrintWriter scriptWriter = new PrintWriter(scriptFileName);

			String line = scriptReader.readLine();
			while (line != null) {
				line = StringUtilities.replaceAllTags(line, "[USER NAME]", userName);
				scriptWriter.println(line);
				line = scriptReader.readLine();
			}
			scriptReader.close();
			scriptWriter.close();
		} catch (FileNotFoundException e) {
			scriptFileName = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				String command = "wscript \"" + scriptWrapper+ "\"";
				try {
					Runtime.getRuntime().exec(command);
					while (!semaphoreFile.exists()) {
						TimeUnit.SECONDS.sleep(2);
					};
					String resultMessage = null;
					do {
						BufferedReader semaphoreFileReader = new BufferedReader(new FileReader(semaphoreFile));
						resultMessage = semaphoreFileReader.readLine();
						semaphoreFileReader.close();
						
					} while (resultMessage == null);
					if (!resultMessage.equals("Ready")) {
						result = false;
						JOptionPane.showMessageDialog(null, script + ": " + resultMessage, "RREManager Script Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch( IOException e ) {
					result = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
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
		String scriptName = scriptPath;
		if (scriptName.contains(File.separator)) {
			scriptName = scriptName.substring(scriptName.lastIndexOf(File.separator) + 1);
		}

		try {
			String argumentsString = "";
			if (arguments != null) {
				for (String argument : arguments) {
					argumentsString += " & \" \" & chr(34) & \"" + argument + "\" & chr(34)";
				}
			}

			PrintWriter scriptWriter = new PrintWriter(scriptWrapperScript);
			scriptWriter.println("dim args, argNr, command, exitCode, objShell, objFSO");
			scriptWriter.println();
			scriptWriter.println("args = WScript.Arguments.Count");
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

		String workPath = RREManager.getCurentPath() + File.separator;
		String scriptPath = copyScript(workPath, script);
		String outputFile = RREManager.getIniFile().getValue("General", "Log Folder") + File.separator + script.replaceAll("\\.", "_") + "_Output.txt";
		if (RREManager.test) {
			outputFile = "D:\\Temp\\RRE\\RREManagerTestLog\\" + script.replaceAll("\\.", "_") + "_Output.txt";
		}

		if (scriptPath != null) {
			File semaphoreFile = new File(workPath + getSemaphoreName());
			semaphoreFile.delete();

			String scriptWrapper = getPowerShellScriptWrapper(workPath, scriptPath, arguments);
			if (scriptWrapper != null) {
				String command = "cmd /c powershell -file \"" + scriptWrapper + "\" 2>&1 >> \"" + outputFile + "\"";
				try {
					Runtime.getRuntime().exec(command);
					while (!semaphoreFile.exists()) {
						TimeUnit.SECONDS.sleep(2);
					};
					String resultMessage = null;
					do {
						BufferedReader semaphoreFileReader = new BufferedReader(new FileReader(semaphoreFile));
						resultMessage = semaphoreFileReader.readLine();
						semaphoreFileReader.close();
						
					} while (resultMessage == null);
					if (!resultMessage.equals("Ready")) {
						result = false;
						JOptionPane.showMessageDialog(null, script + ": " + resultMessage, "RREManager Script Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch( IOException e ) {
					result = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
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
			JOptionPane.showMessageDialog(null, "Cannot find PowerShell script '" + scriptPath + "'.", "RREManager Error", JOptionPane.ERROR_MESSAGE);
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
		String scriptName = scriptPath;

		try {
			String argumentsString = "";
			if (arguments != null) {
				for (String argument : arguments) {
					argumentsString += " \"\"" + argument + "\"\"";
				}
			}

			PrintWriter scriptWriter = new PrintWriter(scriptWrapperScript);
			scriptWriter.println("Write-Host \"ScriptWrapper.ps1 " + scriptName + "\"");
			scriptWriter.println("for (($argNr = 0); $argNr -lt $args.Count; $argNr++) {");
			scriptWriter.println("  Write-Host \"  \" $args[$argNr]");
			scriptWriter.println("}");
			scriptWriter.println("Write-Host \"\"");
			scriptWriter.println();
			scriptWriter.println("$command = \"& \"\"" + scriptPath + "\"\"" + argumentsString + "\"");
			scriptWriter.println("Invoke-Expression $command");
			scriptWriter.println("$exitCode = $LASTEXITCODE");
			scriptWriter.println("if ($exitCode -gt 0) {");
			scriptWriter.println("  $errorString = \"ExitCode=\" + $exitCode");
			scriptWriter.println("  $errorString | Out-File -FilePath \"" + workPath + getSemaphoreName() + "\" -Encoding ASCII");
			scriptWriter.println("} else {");
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
		arguments.add("CreateProjectGPOs");
		arguments.add("Hello");
		arguments.add("World!");
		arguments.add("D:\\Temp\\RRE\\Log.txt");
		arguments.add("  ");
		callScript("testScript.ps1", arguments);
		System.out.println("Ready");
	}
	*/
}
