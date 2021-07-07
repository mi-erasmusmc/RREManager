' CreateFolders.vbs
' Author Peter Rijnbeek

' ------------------------------------------------'
' This script will create all directories for a new user and will set the permissions
' Notifications will be given if directories already existed or an error occured
' If VERBOSE=1 in both procedures every action is presented in a popup

' Version 1.0 - May 2011'
' version 1.1 - August 2011 added project parameter to allow for multiple projects and user directory
' version 1.2 - March 2014 added project drive parameter

' Note that the project folder, share, and data directories need to be present with the correct permissions

  Dim strSharedDirectory, strDataOutDirectory, strUserName, strGroupName, strProjectName, strUserDirectory

  Dim objNetwork 
  Set objNetwork = WScript.CreateObject("WScript.Network")
  strDownLoadFtpDirectory = "e:\ftp\download\"
  strUpLoadFtpDirectory = "e:\ftp\upload\"


  args = WScript.Arguments.Count

  If args < 1 then
    strUserName = inputbox("Enter the username")
  else
    strUserName = Wscript.Arguments.Item(0)
  end If

  If args < 2 then
    VERBOSE = 0
  else
    VERBOSE = Wscript.Arguments.Item(1)
  end If


  If Len(strUserName) > 0 Then
    ' Create the folders
    Call CreateFolder(strDownloadFtpDirectory & strUserName, VERBOSE)
    Call CreateFolder(strUploadFtpDirectory & strUserName, VERBOSE)
  End if 



Private Sub CreateFolder (strDirectory, VERBOSE)
 ' Create FileSystemObject. So we can apply .createFolder method
 Dim objFSO, objFolder, DEBUG

 If VERBOSE=1 Then
      WScript.Echo "Creating " & strDirectory
 End If
 Set objFSO = CreateObject("Scripting.FileSystemObject")
 If objFSO.FolderExists(strDirectory) Then
   If (CInt(VERBOSE)>-1) Then
     Set objFolder = objFSO.GetFolder(strDirectory) 
     WScript.Echo strDirectory & " was already created "
   End If
 Else
   Set objFolder = objFSO.CreateFolder(strDirectory)
   if (VERBOSE=1) Then
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
   Wscript.Echo "Error assigning permissions for user " _
     & strUserName & " to folder " & strDirectory
   Else
     if (VERBOSE="1") Then
       WScript.Echo strPermissions & " permissions set for " & strDirectory & " for " & strName
     End If
   End If
 End If

end Sub

Private Sub RemoveAndDisableInheritance(strDirectory, strName, VERBOSE)
' sets the permissions for a user or group

 Dim intRunError, objShell, objFSO, DEBUG

 Set objShell = CreateObject("Wscript.Shell")
 Set objFSO = CreateObject("Scripting.FileSystemObject")
 If objFSO.FolderExists(strDirectory) Then
   ' Assign user permission to home folder.
   intRunError = objShell.Run("%COMSPEC% /c Echo Y| icacls " & strDirectory & " /inheritance:d /T", 2, True) 
  intRunError = objShell.Run("%COMSPEC% /c Echo Y| icacls " & strDirectory & " /remove:g " & Chr(34) & strName & Chr(34), 2, True) 

   
   If intRunError <> 0 Then
     Wscript.Echo "Error assigning permissions for user " _
     & strUserName & " to folder " & strDirectory
   Else
     if (VERBOSE="1") Then
       WScript.Echo strName & " permissions removed for " & strDirectory
     End If
   End If
 End If

end Sub

If args<1 then
  WScript.Echo "Finished!"
End If
  
WScript.Quit 

