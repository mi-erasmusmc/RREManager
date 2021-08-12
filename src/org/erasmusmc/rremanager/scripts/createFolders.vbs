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
  strExportDirectory = "e:\export\"
  strImportDirectory = "e:\import\"
  strDownLoadFtpDirectory = "e:\ftp\download\"
  strUpLoadFtpDirectory = "e:\ftp\upload\"
  strUserDirectory = "e:\users\"


  args = WScript.Arguments.Count

  If args < 1 then
    strUserName = inputbox("Enter the username")
  else
    strUserName = Wscript.Arguments.Item(0)
  end If

  If args < 2 then
    strProjectName = inputbox("Enter the projectname")
  else
    strProjectName = Wscript.Arguments.Item(1)
  end If
  
  If args < 3 then
    strGroupName = inputbox("Enter the groupname")
  else
    strGroupName = Wscript.Arguments.Item(2)
  end If


  If args < 4 then
    VERBOSE = 0
  else
    VERBOSE = Wscript.Arguments.Item(3)
  end If

  If args < 5 then
    strDriveName = inputbox("Enter the drive (e,d) of project " & strProjectName)
  else
    strDriveName = Wscript.Arguments.Item(4)
  end If

  strSharedDirectory = strDriveName & ":\projects\"
  ' WScript.Echo "project folder: " &  strSharedDirectory


  If Len(strUserName) > 0 Then
    ' Create the folders
    Call CreateFolder(strSharedDirectory & strProjectName & "\Share\" & strUserName, VERBOSE)
    Call CreateFolder(strExportDirectory & strUserName, VERBOSE)
    Call CreateFolder(strImportDirectory & strUserName, VERBOSE)
    Call CreateFolder(strDownloadFtpDirectory & strUserName, VERBOSE)
    Call CreateFolder(strUploadFtpDirectory & strUserName, VERBOSE)
    call CreateFolder(strUserDirectory & strUserName, VERBOSE)

    '  Set the permissions of all folders 
    ' Call RemoveAndDisableInheritance(strSharedDirectory & strProjectName & "\Share\" & strUserName, strGroupName)
    ' Call SetPermissions(strSharedDirectory & strProjectName & "\Share\" & strUserName, strGroupName, "(RX,S,RD,X,RA)")
    Call SetPermissions(strSharedDirectory & strProjectName & "\Share\" & strUserName, strUserName, "(OI)(CI)(WD,RD,AD,X,DC)", VERBOSE)

    Call RemoveAndDisableInheritance(strExportDirectory & strUserName, strGroupName, VERBOSE)
    Call SetPermissions(strExportDirectory & strUserName, strUserName, "(RX,WD,RD,AD,X,DC)", VERBOSE)

    Call RemoveAndDisableInheritance(strImportDirectory & strUserName, strGroupName, VERBOSE)
    Call SetPermissions(strImportDirectory & strUserName, strUserName, "(OI)(RX)", VERBOSE)
    Call SetPermissions(strImportDirectory & strUserName, strUserName, "(DC)", VERBOSE)
    Call SetPermissions(strImportDirectory & strUserName, strUserName, "(OI)(CI)(RD)", VERBOSE)
        
    Call RemoveAndDisableInheritance(strDownloadFtpDirectory & strUserName, strGroupName, VERBOSE)
    Call SetPermissions(strDownloadFtpDirectory & strUserName, strUserName, "(CI)(RX)", VERBOSE)
    Call SetPermissions(strDownloadFtpDirectory & strUserName, strUserName, "(OI)(CI)(R)", VERBOSE)
    
    Call RemoveAndDisableInheritance(strUploadFtpDirectory & strUserName, strGroupName, VERBOSE)
    Call SetPermissions(strUploadFtpDirectory & strUserName, strUserName, "(CI)(RX,DC)", VERBOSE)
    Call SetPermissions(strUploadFtpDirectory & strUserName, strUserName, "(OI)(CI)(W)", VERBOSE)


    Call SetPermissions(strUserDirectory & strUserName, strUserName, "(OI)(CI)(F)", VERBOSE)

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
     'WScript.Echo strDirectory & " was already created "
   End If
 Else
   Set objFolder = objFSO.CreateFolder(strDirectory)
   if (VERBOSE=1) Then
      'WScript.Echo "Just created " & strDirectory
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

