Option Explicit

Dim exitCode, args, argNr, objExplorer, logFileName, logFileIndent, objLogFile

exitCode = 0
args = WScript.Arguments.Count

' Create log in internet explorer
Set objExplorer = createobject("internetexplorer.application")
If IsObject(objExplorer) Then
  objExplorer.navigate2 "about:blank" : objExplorer.width = 700 : objExplorer.height = 600 : objExplorer.toolbar = false : objExplorer.menubar = false : objExplorer.statusbar = false : objExplorer.visible = True
  objExplorer.document.title = "Please be patient.... "
End If

Call OpenLogFile("", "")
Call Log("<font color=green>Testing.<font color=black><br><br>")

If args > 0 then
	logFileName = Wscript.Arguments.Item(args - 2)
	logFileIndent = Wscript.Arguments.Item(args - 1)
	Set objLogFile = CreateObject("Scripting.FileSystemObject").OpenTextFile(logFileName,8,false)
	objLogFile.WriteLine(logFileIndent & "testScript.vbs")
	
	objLogFile.WriteLine(logFileIndent & "  Action: " & Wscript.Arguments.Item(0))
	If StrComp(Wscript.Arguments.Item(0),"Create Project") = 0 Then
		Call Log(logFileIndent & "  Project   : " & Wscript.Arguments.Item(1) & "<br>")
		Call Log(logFileIndent & "  Subfolders: " & Wscript.Arguments.Item(2) & "<br>")
	ElseIf (StrComp(Wscript.Arguments.Item(0),"Modify User") = 0) OR (StrComp(Wscript.Arguments.Item(0),"Add User") = 0) Then
		Call Log(logFileIndent & "  First Name   : " & Wscript.Arguments.Item(1) & "<br>")
		Call Log(logFileIndent & "  Initial      : " & Wscript.Arguments.Item(2) & "<br>")
		Call Log(logFileIndent & "  Last Name    : " & Wscript.Arguments.Item(3) & "<br>")
		Call Log(logFileIndent & "  User Name    : " & Wscript.Arguments.Item(4) & "<br>")
		Call Log(logFileIndent & "  Password     : " & Wscript.Arguments.Item(5) & "<br>")
		Call Log(logFileIndent & "  Email Address: " & Wscript.Arguments.Item(6) & "<br>")
		Call Log(logFileIndent & "  Projects     : " & Wscript.Arguments.Item(7) & "<br>")
		Call Log(logFileIndent & "  Groups       : " & Wscript.Arguments.Item(8) & "<br>")
		Call Log(logFileIndent & "  Update       : " & Wscript.Arguments.Item(9) & "<br>")
	End If
	
    Call CloseLogFile()
Else
  Call Log("<font color=red>Incorrect number of arguments (" & args & "):<br>")
  For argNr = 0 To (args - 1)
    Call Log("<font color=red>  " & argNr & "=" & Wscript.Arguments.Item(argNr) & "<br>")
  Next
  Call CloseLogFile()
  exitCode = 1 
End If


Private Sub OpenLogFile(fileName, indent)
  logFileName = fileName
  logFileIndent = indent
  If StrComp(logFileName, "") <> 0 Then
    Set objLogFile = CreateObject("Scripting.FileSystemObject").OpenTextFile(logFileName,8,false)
  End If
End Sub


Private Sub Log(text)
  If IsObject(objExplorer) Then
    objExplorer.document.write text
  End If
  If StrComp(logFileName, "") <> 0 Then
    objLogFile.WriteLine(logFileIndent & StripHTMLTags(text))
  End If
End Sub


Private Sub CloseLogFile()
  If StrComp(logFileName, "") <> 0 Then
    objLogFile.Close
    Set objLogFile = Nothing
  End If
End Sub


Private Function StripHTMLTags(text)
  Dim strippedText, gtPos
  strippedText = text
  ' Strip HTML tags at the start
  Do While StrComp(Left(strippedText, 1), "<") = 0
    gtPos = InStr(strippedText,">")
    If gtPos > 0 Then
      strippedText = Mid(strippedText, gtPos + 1)
    Else
      Exit Do
    End If
  Loop
  ' Strip HTML tags at the end
  Do While StrComp(Right(strippedText, 1), ">") = 0
    gtPos = InStrRev(strippedText,"<")
    If gtPos > 0 Then
      strippedText = Mid(strippedText, 1, gtPos - 1)
    Else
      Exit Do
    End If
  Loop
  StripHTMLTags = strippedText
End Function

  
WScript.Quit(exitCode)
