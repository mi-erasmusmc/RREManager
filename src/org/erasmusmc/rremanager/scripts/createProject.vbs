' CreateProject.vbs
' Author Peter Rijnbeek

' ------------------------------------------------'
' This script will create a new project in the RRE
' All directories will be created and including permissions
' Notifications will be given if directories already existed or an error occured
' If VERBOSE=1 every action is presented in a popup

' Version 1.0 - Jan 2012

' Note that the project main folder needs to be present with the correct permissions (D:\Projects\)

' Arguments:
'   project name
'   project sub folders (comma-separated)
'   log file path
'   log file indent

Dim VERBOSE, logFileName, logFileIndent, objLogFile, objExplorer
Dim strSubFolders,strMainGroup, strOU, strProjectsFolder, strDataMainSub, strGroupName, strProjectName, strShareMainSub
Dim strDataSUbFolder, strSubFolder, strSubGroupName, strDataFolder, strShareFolder

strProjectsFolder = "D:\Projects\" 
strDataMainSub = "Data"
strShareMainSub = "Share"
strOU = "Researchers"
strMainGroup = "All Researchers"

' Create log in internet explorer
Set objExplorer = createobject("internetexplorer.application")
If IsObject(objExplorer) Then
  objExplorer.navigate2 "about:blank" : objExplorer.width = 700 : objExplorer.height = 600 : objExplorer.toolbar = false : objExplorer.menubar = false : objExplorer.statusbar = false : objExplorer.visible = True
  objExplorer.document.title = "Please be patient.... "
End If

Call Log("<font color=green>Creating project!<font color=black><br>")

VERBOSE = 0
args = WScript.Arguments.Count

If args == 4 then
  strProjectName = Wscript.Arguments.Item(0)
  strSubFolders = Wscript.Arguments.Item(1)
  
  ' Open log file
  Call OpenLogFile(Wscript.Arguments.Item(args - 2), Wscript.Arguments.Item(args - 1))

  ' Log script parameters
  Call Log("createUserProject.vbs" & "<br>)
  Call Log("  Project Name: " & strProjectName & "<br>)
  Call Log("  Sub folders : " & strSubFolders "<br>)
  Call Log("<br>")


  strGroupName = strProjectName & " Researchers"  'e.g. 'ARITMO Researchers
  Call CreateUserGroup(strOU,strGroupName, VERBOSE)
  Call AddGrpMem(strMainGroup,strGroupName, VERBOSE)

  If Len(strProjectName) > 0 Then
    ' Create the folders
    strDataFolder = strProjectsFolder & strProjectName & "\" & strDataMainSub
    strShareFolder = strProjectsFolder & strProjectName & "\" & strShareMainSub

    Call CreateFolder(strProjectsFolder & strProjectName, VERBOSE)
    Call RemoveAndDisableInheritance(strProjectsFolder & strProjectName, "All Researchers", VERBOSE)
    
    Call CreateFolder(strDataFolder, VERBOSE)
    Call CreateFolder(strShareFolder, VERBOSE)

    ' Set the permissions of these folders 
    Call SetPermissions(strDataFolder, StrGroupName, "(OI)(CI)(RX)", VERBOSE) 
    Call SetPermissions(strShareFolder, StrGroupName, "(OI)(CI)(RX)", VERBOSE)

   ' Create Share
    Call createShare(strProjectName & "-Share",strProjectsFolder & strProjectName & "\" & strShareMainSub, VERBOSE)

    if Len(strSubFolders)> 0 Then

      Call RemoveAndDisableInheritance(strProjectsFolder & strProjectName & "\" & strDataMainSub, strGroupName, VERBOSE)

      DIM SubFolderArray, counter
      SubFolderArray = Split(strSubFolders,",")
      For counter = 0 to UBound(subFolderArray)
         strSubFolder = Trim(subFolderArray(counter))
         strDataSubFolder = strProjectsFolder & strProjectName & "\" & strDataMainSub & "\" & strSubFolder
         Call CreateFolder(strDataSubFolder, VERBOSE)
 	 
         strSubGroupName = strProjectName & " " & strSubFolder  'e.g. 'ARITMO WP5
         Call CreateUserGroup(strOU,strSubGroupName, VERBOSE)
         ' Add WP group to project group
         Call AddGrpMem(strGroupName, strSubGroupName, VERBOSE)

         Call SetPermissions(strDataSubFolder, StrSubGroupName, "(OI)(CI)(RX)", VERBOSE)
        
         ' Create Date Share on this level
         Call createShare(strProjectName & "-" & strSubFolder & "-Data",strDataSubFolder, VERBOSE)
      Next
    Else
      ' Create Date Share on this level
      Call createShare(strProjectName & "-Data",strProjectsFolder & strProjectName & "\" & strDataMainSub, VERBOSE)
    End If
  End If
End If

If IsObject(objExplorer) Then
  objExplorer.document.title = "Ready" 
End If

Call CloseLogFile()


Private Sub CreateFolder (strDirectory, VERBOSE)
 ' Create FileSystemObject. So we can apply .createFolder method
 Dim objFSO, objFolder, DEBUG

 Call Log("Creating " & strDirectory + "<br>")
 If VERBOSE Then
      WScript.Echo "Creating " & strDirectory
 End If
 Set objFSO = CreateObject("Scripting.FileSystemObject")
 If objFSO.FolderExists(strDirectory) Then
   Set objFolder = objFSO.GetFolder(strDirectory)
   Call Log(strDirectory & " was already created<br>")
   If VERBOSE Then
     Script.Echo strDirectory & " was already created"
   End If
 Else
   Set objFolder = objFSO.CreateFolder(strDirectory)
   Call Log("Just created " & strDirectory & "<br>")
   If VERBOSE Then
      WScript.Echo "Just created " & strDirectory
   End If
 End If

end Sub


Private Sub SetPermissions(strDirectory, strName, strPermissions, VERBOSE)
' sets the permissions for a user or group

 Dim intRunError, objShell, objFSO, DEBUG

 Set objShell = CreateObject("Wscript.Shell")
 Set objFSO = CreateObject("Scripting.FileSystemObject")
 If objFSO.FolderExists(strDirectory) Then
   ' Assign user permission to home folder.
   intRunError = objShell.Run("%COMSPEC% /c Echo Y| icacls " & strDirectory & " /c /grant " & Chr(34) & strName & Chr(34) & ":" & strPermissions , 2, True) 


   If intRunError <> 0 Then
   Call Log("<font color=red>Error assigning permissions for user " & strName & " to folder " & strDirectory & "<font color=black><br>")
   Wscript.Echo "Error assigning permissions for user " & strName & " to folder " & strDirectory
   Else
     Call Log("<font color=green>" & strPermissions & " permissions set for " & strDirectory & " for " & strName & "<font color=black><br>")
     if VERBOSE Then
       WScript.Echo strPermissions & " permissions set for " & strDirectory & " for " & strName
     End If
   End If
 End If

end Sub


Private Sub RemoveAndDisableInheritance(strDirectory, strName, VERBOSE)
' sets the permissions for a user or group

 Dim intRunError, objShell, objFSO

 Set objShell = CreateObject("Wscript.Shell")
 Set objFSO = CreateObject("Scripting.FileSystemObject")
 If objFSO.FolderExists(strDirectory) Then
   ' Assign user permission to home folder.
  intRunError = objShell.Run("%COMSPEC% /c Echo Y| icacls " & strDirectory & " /inheritance:d /T", 2, True) 
  intRunError = objShell.Run("%COMSPEC% /c Echo Y| icacls " & strDirectory & " /remove:g " & Chr(34) & strName & Chr(34), 2, True) 

   
   If intRunError <> 0 Then
     Call Log("<font color=red>Error assigning permissions for user " & strUserName & " to folder " & strDirectory & "<font color=black><br>")
     Wscript.Echo "Error assigning permissions for user " & strUserName & " to folder " & strDirectory
   Else
     Call Log("<font color=green>" & strName & " permissions removed for " & strDirectory & "<font color=black><br>")
     if VERBOSE Then
       WScript.Echo strName & " permissions removed for " & strDirectory
     End If
   End If
 End If
end Sub


Private Sub CreateUserGroup(strOU,strNewGp, VERBOSE)
' Adds a usergroup to the OU

  Dim StrNewGpLong, strDNSDomain, objArguments

  StrNewGpLong = "CN=" & StrNewGp

  Set objRootDSE = GetObject("LDAP://RootDSE")
  strDNSDomain = objRootDSE.Get("DefaultNamingContext")

  'TODO Check arguments

    'Create NewGroup
    set objOU = GetObject("LDAP://ou=" & strOU & "," & strDNSDomain )
    ON Error Resume Next
    set objGroup = objOU.Create("Group",strNewGpLong)
    objGroup.Put "sAMAccountName", strNewGp
    objGroup.setInfo  
    IF (Err.Number <> 0) Then
      on Error Goto 0
      Call Log("<font color=orange>Group " & strNewGp & " already existed (Skipped)<font color=black><br>")
      wscript.Echo "Group " & strNewGp & " already existed (Skipped)"
    ELSE
      Call Log("<font color=green>Group " & strMewGp & " created<font color=black><br>")
      if VERBOSE Then
        WScript.Echo "Group " & strMewGp & " created"
      End If
   End If
End Sub


Private Sub AddGrpMem(strGroupI,strMemberI,VERBOSE)
' Adds a member to a group

   Dim strOU, strGroup, strUser, strDNSDomain
   Dim objRootLDAP, objGroup, objUser

   '  Check these objects referenced by strOU, strGroup exist in strOU
   strOU = "OU=Researchers,"
   strUser = "CN=" & strMemberI & ","
   strGroup = "CN=" & strGroupI & ","

   '  Bind to Active Directory and get LDAP name
   Set objRootLDAP = GetObject("LDAP://RootDSE")
   strDNSDomain = objRootLDAP.Get("DefaultNamingContext")

   ' Add (str)User to (str)Group
   ON Error Resume Next
   Set objUser = GetObject("LDAP://"& strUser & strOU & strDNSDomain)
   Set objGroup = GetObject("LDAP://"& strGroup & strOU & strDNSDomain)
   objGroup.add(objUser.ADsPath) 
   IF (Err.Number <> 0) Then
      on Error Goto 0
      Call Log("<font color=orange>Member " & strMemberI & " already exists? (Skipped)<font color=black><br>")
      wscript.Echo "Member " & strMemberI & " already exists? (Skipped)"
   ELSE
      Call Log("<font color=green>Member " & strmemberI & " added to " & strGroupI" & "<font color=black><br>")
      if VERBOSE Then
        WScript.Echo "Member " & strmemberI & " added to " & strGroupI
      End If
   End If
End Sub


Private Sub createShareAlternative(strShareName, strShareFolder, VERBOSE)
'Obsolete

   Const FILE_SHARE = 0
   Const MAXIMUM_CONNECTIONS = 100
   Const ShareDescription = " "

   strComputer = "."
   Set objWMIService = GetObject("winmgmts:" & "{impersonationLevel=impersonate}!\\" & strComputer & "\root\cimv2")

   Set objNewShare = objWMIService.Get("Win32_Share")

   ON Error Resume Next
   errReturn = objNewShare.Create (strSharefolder, strSharename, FILE_SHARE, MAXIMUM_CONNECTIONS, ShareDescription)
   IF (Err.Number <> 0) Then
      on Error Goto 0
      Call Log("<font color=red>Share " & strShareName & " not created (Skipped)<font color=black><br>")
      wscript.Echo "Share " & strShareName & " not created (Skipped)"
   ELSE
      Call Log("<font color=green>Share " & strShareName & " created.<font color=black><br>")
      If VERBOSE Then
        WScript.Echo "Share " & strShareName & " created." 
      End If
   End If

End Sub


Private Sub createShare(strShareName, strShareFolder, VERBOSE)
  Dim intRunError, objShell, objFSO

  Set objShell = CreateObject("Wscript.Shell")
  Set objExecObject = objShell.Exec("cmd /c NET SHARE " & strShareName & "=" & Chr(34) & strShareFolder & Chr(34) & " /GRANT:EVERYONE,FULL") 
 
  Call Log("<font color=green>Permissions set for " & strShareName & " created.<font color=black><br>")
  if VERBOSE Then
    WScript.Echo "Permissions set for " & strShareName
  End If
End Sub


Private Sub OpenLogFile(fileName, indent)
  If StrComp(fileName, "") <> 0 Then
    logFileName = fileName
    logFileIndent = indent
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

  
WScript.Quit 

