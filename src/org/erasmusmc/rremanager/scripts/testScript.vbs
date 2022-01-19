Dim logFileName, logFileIndent, objLogFile

argCount = WScript.Arguments.Count

If argCount > 0 then
	logFileName = Wscript.Arguments.Item(argCount - 2)
	logFileIndent = Wscript.Arguments.Item(argCount - 1)
	Set objLogFile = CreateObject("Scripting.FileSystemObject").OpenTextFile(logFileName,8,false)
	objLogFile.WriteLine(logFileIndent & "testScript.vbs")
	
	objLogFile.WriteLine(logFileIndent & "  Action: " & Wscript.Arguments.Item(0))
	If StrComp(Wscript.Arguments.Item(0),"Create Project") = 0 Then
		objLogFile.WriteLine(logFileIndent & "  Project   : " & Wscript.Arguments.Item(1))
		objLogFile.WriteLine(logFileIndent & "  Subfolders: " & Wscript.Arguments.Item(2))
	ElseIf (StrComp(Wscript.Arguments.Item(0),"Modify User") = 0) OR (StrComp(Wscript.Arguments.Item(0),"Add User") = 0) Then
		objLogFile.WriteLine(logFileIndent & "  First Name   : " & Wscript.Arguments.Item(1))
		objLogFile.WriteLine(logFileIndent & "  Initial      : " & Wscript.Arguments.Item(2))
		objLogFile.WriteLine(logFileIndent & "  Last Name    : " & Wscript.Arguments.Item(3))
		objLogFile.WriteLine(logFileIndent & "  User Name    : " & Wscript.Arguments.Item(4))
		objLogFile.WriteLine(logFileIndent & "  Password     : " & Wscript.Arguments.Item(5))
		objLogFile.WriteLine(logFileIndent & "  Email Address: " & Wscript.Arguments.Item(6))
		objLogFile.WriteLine(logFileIndent & "  Projects     : " & Wscript.Arguments.Item(7))
		objLogFile.WriteLine(logFileIndent & "  Groups       : " & Wscript.Arguments.Item(8))
		objLogFile.WriteLine(logFileIndent & "  Update       : " & Wscript.Arguments.Item(9))
	End If
	
	objLogFile.Close
	set objLogFile = Nothing
End If