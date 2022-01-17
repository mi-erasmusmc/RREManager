argCount = WScript.Arguments.Count
If argCount > 0 then
	WScript.Echo "Action: " & Wscript.Arguments.Item(0)
	If StrComp(Wscript.Arguments.Item(0),"Create project") = 0 Then
		WScript.Echo "Project   : " & Wscript.Arguments.Item(1)
		WScript.Echo "Subfolders: " & Wscript.Arguments.Item(2)
	ElseIf StrComp(Wscript.Arguments.Item(0),"Modify user") = 0 Then
		WScript.Echo "First Name   : " & Wscript.Arguments.Item(1)
		WScript.Echo "Initial      : " & Wscript.Arguments.Item(2)
		WScript.Echo "Last Name    : " & Wscript.Arguments.Item(3)
		WScript.Echo "User Name    : " & Wscript.Arguments.Item(4)
		WScript.Echo "Password     : " & Wscript.Arguments.Item(5)
		WScript.Echo "Email Address: " & Wscript.Arguments.Item(6)
		WScript.Echo "Projects     : " & Wscript.Arguments.Item(7)
		WScript.Echo "Groups       : " & Wscript.Arguments.Item(8)
		WScript.Echo "Update       : " & Wscript.Arguments.Item(9)
	ElseIf StrComp(Wscript.Arguments.Item(0),"Add user") = 0 Then
		WScript.Echo "First Name   : " & Wscript.Arguments.Item(1)
		WScript.Echo "Initial      : " & Wscript.Arguments.Item(2)
		WScript.Echo "Last Name    : " & Wscript.Arguments.Item(3)
		WScript.Echo "User Name    : " & Wscript.Arguments.Item(4)
		WScript.Echo "Password     : " & Wscript.Arguments.Item(5)
		WScript.Echo "Email Address: " & Wscript.Arguments.Item(6)
		WScript.Echo "Projects     : " & Wscript.Arguments.Item(7)
		WScript.Echo "Groups       : " & Wscript.Arguments.Item(8)
		WScript.Echo "Update       : " & Wscript.Arguments.Item(9)
	End If
End If