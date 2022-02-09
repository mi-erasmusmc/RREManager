' CreateUserProjects.vbs
' VBScript program to create users according to the information in a
' Microsoft Excel spreadsheet.
'
' ----------------------------------------------------------------------
' Copyright (c) 2003-2010 Richard L. Mueller
' Modified 2011 Peter Rijnbeek, Mees Mosseveld
' Hilltop Lab web site - http://www.rlmueller.net
' Version 1.0 - September 8, 2003
' Version 1.1 - January 25, 2004 - Modify error trapping.
' Version 1.2 - March 18, 2004 - Modify NameTranslate constants.
' Version 2.0 - October 7, 2007 - Specify container for each user object
'                             in spreadsheet. Accept NT names of groups.
' Version 2.1 - November 6, 2010 - No need to set objects to Nothing.
' Version 3.0 - August 12, 2011 - Added creation of folders, warnings etc.
' Version 3.1 - August 14, 2011 - Added project possibilities
'
' You have a royalty-free right to use, modify, reproduce, and
' distribute this script file in any way you find useful, provided that
' you agree that the copyright owner above has no warranty, obligations,
' or liability for such use.

' Arguments:
'   first name
'   initial
'   last name
'   user name
'   password
'   email address
'   projects
'   groups
'   update flag (1 = update)
'   log file path
'   log file indent


Option Explicit

Dim exitCode, wshShell, strUserDNSDomain, userDNSDomainArray, strDC, counter
Dim args, argNr, VERBOSE, logFileName, logFileIndent, objLogFile
Dim Update, FTPOnly, strFirst, strLast, strInitials, strUserName, strPW, strProjects, strGroups, strEmail
Dim strGroupDN, objUser, objGroup, objContainer
Dim strCN, strNTName, strContainerDN
Dim strHomeFolder, strHomeDrive, objFSO, objShell
Dim intRunError, strNetBIOSDomain, strDNSDomain, strProjectName
Dim objRootDSE, objTrans, strLogonScript, strUPN
Dim strPreviousDN, blnBound, multiOTPGroup, projectsDrive
Dim FolderScript, FTPOnlyFolderScript, objExplorer
Dim strUserOU, strGroupOU, strGroup, strUser, strUpdate
Dim objRootLDAP

Dim strExportDirectory, strImportDirectory, strDownLoadFtpDirectory, strUpLoadFtpDirectory, strUserDirectory

exitCode = 0
multiOTPGroup = "MultiOTP Users"
FolderScript = "createFolders.vbs"
FTPOnlyFolderScript = "createFTPOnlyFolders.vbs"
projectsDrive = "D"
strExportDirectory = "E:\Export\"
strImportDirectory = "E:\Import\"
strDownLoadFtpDirectory = "E:\FTP\Download\"
strUpLoadFtpDirectory = "E:\FTP\Upload\"
strUserDirectory = "E:\Users\"


' Constants for the NameTranslate object.
Const ADS_NAME_INITTYPE_GC = 3
Const ADS_NAME_TYPE_NT4 = 3
Const ADS_NAME_TYPE_1779 = 1
Const ADS_UF_SMARTCARD_REQUIRED = &h40000
Const ADS_UF_DONT_EXPIRE_PASSWD = &h10000

Set objFSO = CreateObject("Scripting.FileSystemObject")
Set objShell = CreateObject("Wscript.Shell")

' Create log in internet explorer
Set objExplorer = createobject("internetexplorer.application")
If IsObject(objExplorer) Then
  objExplorer.navigate2 "about:blank" : objExplorer.width = 700 : objExplorer.height = 600 : objExplorer.toolbar = false : objExplorer.menubar = false : objExplorer.statusbar = false : objExplorer.visible = True
  objExplorer.document.title = "Please be patient.... "
End If

' Get the current domain
Set wshShell = CreateObject("WScript.Shell")
strUserDNSDomain = wshShell.ExpandEnvironmentStrings("%USERDNSDOMAIN%")
userDNSDomainArray = Split(strUserDNSDomain,".")
strDC = ""
For counter = 0 to UBound(userDNSDomainArray)
  strDC = strDC & ",dc=" & Trim(userDNSDomainArray(counter))
Next
strContainerDN = "ou=Researchers" & strDC

' Determine DNS domain name from RootDSE object.
Set objRootDSE = GetObject("LDAP://RootDSE")
strDNSDomain = objRootDSE.Get("DefaultNamingContext")

' Use the NameTranslate object to find the NetBIOS domain name
' from the DNS domain name.
Set objTrans = CreateObject("NameTranslate")
objTrans.Init ADS_NAME_INITTYPE_GC, ""
objTrans.Set ADS_NAME_TYPE_1779, strDNSDomain
strNetBIOSDomain = objTrans.Get(ADS_NAME_TYPE_NT4)
' Remove trailing backslash.
strNetBIOSdomain = Left(strNetBIOSDomain, Len(strNetBIOSDomain) - 1)

args = WScript.Arguments.Count

If args = 11 then
  strFirst = Wscript.Arguments.Item(0)
  strInitials = Wscript.Arguments.Item(1)
  strLast = Wscript.Arguments.Item(2)
  strUserName = Wscript.Arguments.Item(3)
  strPW = Wscript.Arguments.Item(4)
  strEmail = Wscript.Arguments.Item(5)
  strProjects = Wscript.Arguments.Item(6)
  strGroups = Wscript.Arguments.Item(7)
  strUpdate = Wscript.Arguments.Item(8)
  
  ' Open log file
  Call OpenLogFile(Wscript.Arguments.Item(args - 2), Wscript.Arguments.Item(args - 1))
  Call Log("<font color=green>Creating user account.<font color=black><br><br>")
  Call Log("<font color=blue><br><br>IMPORTANT: FOLLOW THE INSTRUCTIONS AT THE END OF THIS PAGE<font color=black><br><br>")

  ' Log script parameters
  Call Log("createUserProjects.vbs" & "<br>")
  Call Log("  First Name   : " & strFirst & "<br>")
  Call Log("  Initial      : " & strInitials & "<br>")
  Call Log("  Last Name    : " & strLast & "<br>")
  Call Log("  User Name    : " & strUserName & "<br>")
  Call Log("  Password     : " & strPW & "<br>")
  Call Log("  Email Address: " & strEmail & "<br>")
  Call Log("  Projects     : " & strProjects & "<br>")
  Call Log("  Groups       : " & strGroups & "<br>")
  Call Log("  Update       : " & strUpdate & "<br>")
  Call Log("<br>")
  
  DIM ProjectsArray
  ProjectsArray = Split(strProjects,",")
        
  DIM groupArray
  groupArray = Split(strGroups,",")
  
  
  If strUpdate = "1" Then
    Update = True
  Else
    Update = False
  End If
  
  

  FTPOnly = 0
  If strGroups = "" Then
  	' FTP Only user (no group(s) specified)
    FTPOnly = 1
    strCN = strFirst & " " & strLast
    strNTName = LCase(Left(strFirst,1) & strLast)
    strNTName = Replace(strNTName," ","")
    
    ' Show some info to the user
    Call Log("<p><font color=black>-----------------------------------------------------------------<br>")
    Call Log("Adding FTP only user: " & strCN & "</p>") 
    Call Log("<font color=green>Creating folders for FTP<br>")
  	Call Log("<br>")

'REPLACED    Call CloseLogFile()
'REPLACED    objShell.run chr(34) & FTPOnlyFolderScript & chr(34) & " " & chr(34) & strNTName & chr(34) & " " & VERBOSE,1,true
'REPLACED    Call OpenLogFile(logFileName, logFileIndent)
    
    Call CreateFTPOnlyFolders(strNTName, VERBOSE)

    Call Log("<br>")
    Call Log("<p><font color=black>-----------------------------------------------------------------</p><br>")
  Else
  	' Researcher
      strCN = strFirst & " " & strLast
      'strNTName = LCase(Left(strFirst,1) & strLast)
      'strNTName = Replace(strNTName," ","")
      strNTName = LCase(strUserName)
  
      strUPN = LCase(strNTName & "@rre.erasmusmc.nl")
        
      ' Not used can be added if necessary as extra columns in Excel sheet
      strHomeFolder = ""
      strHomeDrive = ""
      strLogonScript = ""
    
      ' Show some info to the user
      Call Log("<p><font color=black>-----------------------------------------------------------------<br>")
    
      ' Is this a new user?
         
      If Not DoesExist(strDNSDomain, strNTName) OR Update Then
          If Update Then
  	        Call Log("<font color=orange>Only Adding missing projects and groups for user: " & strCN & " (" & strNTName & ")<br>")
  	      Else
            Call Log("Adding user: " & strCN & " (" & strNTName & ")</p>")
          End If
          ' If this container is different from the previous, bind to
          ' the container the user object will be created in.
    
          If (strContainerDN <> strPreviousDN) Then
              Call Log("Bind to container: " & strContainerDN & "<br>")
              On Error Resume Next
              Set objContainer = GetObject("LDAP://" & strContainerDN)
              If (Err.Number <> 0) Then
                  On Error GoTo 0
                  Call Log("<font color=red>Unable to bind to container: " & strContainerDN & "<br>")
                  Call Log("<font color=red>Unable to create user with NT name: " & strNTName & "<br>")
                  ' Flag that container not bound.
                  strPreviousDN = ""
                  exitCode = 1
              Else
                  On Error GoTo 0
                  strPreviousDN = strContainerDN
              End If
          End If
          ' Proceed if parent container bound.
          If (strPreviousDN <> "") Then
              Call Log("Bound to container: " & strPreviousDN & "<br>")
              On Error Resume Next
              If NOT Update Then
                ' Create user object.
                Call Log("Create user " & strCN & "<br>")
                Set objUser = objContainer.Create("user", "cn=" & strCN)
              Else
                ' Get user object.
                Set objUser = GetObject("LDAP://cn=" & strCN & "," & strContainerDN)
              End If
    
              If (Err.Number <> 0) Then
                  On Error GoTo 0
                  Wscript.Echo "Unable to create user with cn: " & strCN
  	              Call Log("<font color=red>Unable to create user with cn: " & strCN & "<br>")
                  exitCode = 2
              Else
  	              Call Log("<font color=green>Created user with cn: " & strCN & "<br>")
                  ' Assign mandatory attributes and save user object.
                  If (strNTName = "") Then
                      strNTName = strCN
                  End If
                  objUser.sAMAccountName = strNTName
                  On Error GoTo 0
                  objUser.SetInfo
                  If (Err.Number <> 0) Then
                      On Error GoTo 0
                      Call Log("<font color=red>Unable to create user with NT name: " & strNTName & "<br>")
                      exitCode = 3
                  Else
                      ' Set password for user.
                      Call Log("<font color=green>password for user " & strNTName & ": " & strPW & "<br>")
                      objUser.SetPassword strPW
                      If (Err.Number <> 0) Then
                         On Error GoTo 0
                         Call Log("<font color=red>Unable to set password for user " & strNTName & "<br>")
                         exitCode = 4
                      End If
                      On Error GoTo 0
                      ' Enable the user account.
                      objUser.AccountDisabled = False
                      If (strFirst <> "") Then
                          objUser.givenName = strFirst
                      End If
                      ' Assign values to remaining attributes.
                      If (strInitials <> "") Then
                          objUser.initials = strInitials
                      End If
                      If (strLast <> "") Then
                          objUser.sn = strLast
                      End If
                      If (strEmail <> "") Then
                          objUser.mail = strEmail
                      End If

                      If (strUPN <> "") Then
                          objUser.userPrincipalName = strUPN
                      End If
                      If (strHomeDrive <> "") Then
                          objUser.homeDrive = strHomeDrive
                      End If
                      If (strHomeFolder <> "") Then
                          objUser.homeDirectory = strHomeFolder
                      End If
                      If (strLogonScript <> "") Then
                          objUser.scriptPath = strLogonScript
                      End If
                      ' Set password expired. Must be changed on next logon -> NO!.
                      ' objUser.pwdLastSet = 1
                      ' Save changes.
               
                      On Error GoTo 0
                      objUser.SetInfo
                      If (Err.Number <> 0) Then
                        On Error GoTo 0
                        Call Log("<font color=red>Unable to set attributes for user with NT name: " & strNTName & "<br>")
                        exitCode = 5
                      End If
                      On Error GoTo 0
                      ' Create home folder.
                      If (strHomeFolder <> "") Then
                          If (objFSO.FolderExists(strHomeFolder) = False) Then
                              On Error Resume Next
                              objFSO.CreateFolder strHomeFolder
                              If (Err.Number <> 0) Then
                                  On Error GoTo 0
                                  Call Log("<font color=red>Unable to create home folder: " & strHomeFolder & "<br>")
                                  exitCode = 6
                              End If
                              On Error GoTo 0
                          End If
                          If (objFSO.FolderExists(strHomeFolder) = True) Then
                              ' Assign user permission to home folder.
                              intRunError = objShell.Run("%COMSPEC% /c Echo Y| cacls " & strHomeFolder & " /T /E /C /G " & strNetBIOSDomain & "\" & strNTName & ":F", 2, True)
                              If (intRunError <> 0) Then
                                  Call Log("<font color=red>Error assigning permissions for user " & strNTName & " to home folder " & strHomeFolder & "<br>")
                                  exitCode = 7
                              End If
                          End If
                      End If
       
                      ' Group DN's (Comma Seperated).
                      Call Log("<font color=green>Start Group assignment<br>")
              
                      For counter = 0 to UBound(groupArray)
                          strGroupDN = Trim(groupArray(counter))
                          ' Attempt to bind to group object DN.
                          Call Log("<font color=black>Bind to group " & strGroupDN & "<br>")
                          blnBound = False
                          On Error Resume Next
                          Set objGroup = GetObject("LDAP://CN=" & strGroupDN & ",OU=Researchers" & strDNSDomain)
                          If (Err.Number <> 0) Then
                              On Error GoTo 0
                              ' Try  again converting NT Name to DN.
                              On Error Resume Next
                              objTrans.Set ADS_NAME_TYPE_NT4, strNetBIOSDomain & "\" & strGroupDN
                              If (Err.Number <> 0) Then
                                  On Error GoTo 0
                                  Call Log("<font color=red>Unable to bind to group " & strGroupDN & "<br>")
                                  exitCode = 8
                              Else
                                  On Error GoTo 0
                                  strGroupDN = objTrans.Get(ADS_NAME_TYPE_1779)
                                  Set objGroup = GetObject("LDAP://" & strGroupDN)
                                  blnBound = True
                              End If
                          Else
                              On Error GoTo 0
                              blnBound = True
                          End If
                          If (blnBound = True) Then
                              On Error Resume Next
                              objGroup.Add(objUser.AdsPath)
                              If (Err.Number <> 0) Then
                                  On Error GoTo 0
                                  Call Log("<font color=orange>user " & strNTName & " already added to group " & strGroupDN  & "? (skipped)<br>")
                              End If
                          End If
                          On Error GoTo 0
                      Next
                   
                      ' Create folders
                      If Update Then
                         VERBOSE = -1 ' No messages about existing directories
                         Call Log("<font color=orange>User folders will only be created for new projects<br>")
                      Else
                         VERBOSE = 0 ' Only messages about existing directories
                      End If
    		
                      For counter = 0 to UBound(ProjectsArray)
                        If NOT Update Then
                          Call Log("<font color=green>Creating folders for project: " & ProjectsArray(counter) & "<br>")
                        End If
                        strProjectName = Replace(ProjectsArray(counter)," ","")
  	                    Call Log("<br>")
                        Call CreateFolders(strNTName, strProjectName, strProjectName & " Researchers", projectsDrive, VERBOSE)
                        Call Log("<br>")
                        Call Log("<font color=green>User folders have been created. <br>")
                      Next
                  End If
              End If
          End If
      Else
          Call Log("<font color=orange>User " & strCN & " (" & strNTName & ") already exists! (skipped)<br>")
      End If ' User exists?
        
      ' Add the user to the multiOTPGroup in AD Users
      '  Check these objects referenced by strOU, strGroup exist in strOU
      strUserOU = "OU=Researchers,"
      strUser = "CN=" & strCN & ","
      strGroupOU = "CN=Users,"
      strGroup = "CN=" & multiOTPGroup & ","

      '  Bind to Active Directory and get LDAP name
      Set objRootLDAP = GetObject("LDAP://RootDSE")

      ' Add (str)User to (str)Group
      ON Error GoTo 0
      Set objUser = GetObject("LDAP://"& strUser & strUserOU & strDNSDomain)
      Set objGroup = GetObject("LDAP://"& strGroup & strGroupOU & strDNSDomain)
      objGroup.add(objUser.ADsPath) 
      If (Err.Number <> 0) Then
        on Error GoTo 0
  	    Call Log("<font color=orange>" & strCN & " already member of group " & multiOTPGroup & "? (Skipped)<br>")
      Else
   	    Call Log("<font color=green>" & strCN & " added to group " & multiOTPGroup & "<br>")
      End If
        
      Call Log("<p><font color=black>-----------------------------------------------------------------</p><br>")
  End If
    
  Call Log("<font color=blue>IMPORTANT:<br>")
  Call Log("<br>")
  Call Log("You have to add the user to the Cerberus FTP server:<br>")
  Call Log("<br>")
  Call Log("In Cerberus click on 'User Manager' in the left panel.<br>")
  Call Log("Right click on a similar user in the users list and select the option 'Clone User'<br>")
  Call Log("The information of the cloned user is now shown in the middle panel.<br>")
  Call Log("Set the following fields as follows:<br>")
  Call Log("<br>")
  Call Log("User Name = " & strUserName & "<br>")
  Call Log("First Name = " & strFirst & "<br>")
  Call Log("Last Name = " & strLast & "<br>")
  Call Log("<br>")
  Call Log("Click on the 'Update User' button.<br>"
  Call Log("Click on the 'Change Password' button and set the password to: " & strPW & "<br>")
  Call Log("<br>")
  Call Log("Click on the 'Directories' link above the User Name field.<br>")
  Call Log("<br>")
  Call Log("Double click on the download folder definition and change the Path to:<br>")
  Call Log("<br>")
  Call Log(strDownLoadFtpDirectory & strUserName & "<br>")
  Call Log("<br>")
  Call Log("Make sure that the options 'List Files', 'List Directories', 'Delete', and 'Download'<br>)
  Call Log("are checked and the others not.<br>")
  Call Log("<br>")
  Call Log("Click on the 'Update' button<br>")
  Call Log("<br>")
  Call Log("Double click on the upload folder definition and change the Path to:<br>")
  Call Log("<br>")
  Call Log(strUpLoadFtpDirectory & strUserName & "<br>")
  Call Log("<br>")
  Call Log("Make sure that the options 'List Files', 'List Directories', 'Delete' 'Rename', 'Upload',<br>")
  Call Log("and 'Create Directories' are checked and the others not.<br>")
  Call Log("<br>")
  Call Log("Click on the 'Update' button<br>")
  Call Log("<br>")
  Call Log("<br>")
  Call Log("IMPORTANT: In case you also have defined or modified projects first perform the actions<br>")
  Call Log("described in the web page.<br>")
  Call Log("<br>")
  Call Log("Now double click on the desktop shortcut 'Force GP Update' and wait till it is finished.<br>")
  If (FTPOnly = 0) Then
    Call Log("<br>")
    Call Log("Now double click on the desktop shortcut 'SyncOTP-AD' and wait till it is finished.<br>")
    Call Log("Open Google Chrome and click on the 'MultiOTP Admin' bookmark and login.<br>")
    Call Log("In the list of users click on the 'Print' button in front of the user '" & strUserName & "'.<br>")
    Call Log("Now press the keys <Ctrl>+<p> and save it as '" & strUserName & ".pdf' to the folder:<br>")
    Call Log("<br>")
    Call Log("F:\Administration\OTP Accounts<br>")
  End If
  Call Log("<br>")
  Call Log("<br>")
  Call Log("Now you can close the browser window(s) and start the RREManager again to send the mails<br>")
  Call Log("for opening the firewall and later the mails with the login credentials of the user.<br>")
  

  Call CloseLogFile()

  If IsObject(objExplorer) Then
    objExplorer.document.title = "Ready" 
  End If
Else
  Call OpenLogFile("", "")
  Call Log("<font color=red>Creating user account.<font color=black><br><br>")
  Call Log("<font color=red>Incorrect number of arguments (" & args & " instead of 11):<br>")
  For argNr = 0 To (args - 1)
    Call Log("<font color=red>  " & argNr & "=" & Wscript.Arguments.Item(argNr) & "<br>")
  Next
  Call CloseLogFile()
  exitCode = 9
End If


function DoesExist(strDomain, strUser)
DIM cnn, cmd, rs


Const ADS_SCOPE_SUBTREE = 2
Set cnn = CreateObject("ADODB.Connection")
Set cmd = CreateObject("ADODB.Command")
cnn.Provider = "ADsDSOObject"
cnn.Open "Active Directory Provider"
Set cmd.ActiveConnection = cnn
cmd.Properties("Page Size") = 1000
cmd.Properties("Timeout") = 30
cmd.Properties("Searchscope") = ADS_SCOPE_SUBTREE
cmd.Properties("Cache Results") = False

cmd.CommandText = "SELECT name from 'LDAP://" & strDomain & "' WHERE objectCategory = 'user' AND SAMAccountName = '" & strUser & "'"

Set rs = cmd.Execute
If rs.EOF Then
    DoesExist = false
  Else
    DoesExist = true
End If

End function


Private Sub AddGrpMem(strGroupI,strMemberI,VERBOSE)
' Adds a member to a group

   Dim strUserOU, strGroupOU, strGroup, strUser, strDNSDomain
   Dim objRootLDAP, objGroup, objUser

   '  Check these objects referenced by strOU, strGroup exist in strOU
   strUserOU = "OU=Researchers,"
   strUser = "CN=" & strMemberI & ","
   strGroupOU = "OU=Users,"
   strGroup = "CN=" & strGroupI & ","

   '  Bind to Active Directory and get LDAP name
   Set objRootLDAP = GetObject("LDAP://RootDSE")
   strDNSDomain = objRootLDAP.Get("DefaultNamingContext")

   ' Add (str)User to (str)Group
   ON Error Resume Next
   Set objUser = GetObject("LDAP://"& strUser & strUserOU & strDNSDomain)
   Set objGroup = GetObject("LDAP://"& strGroup & strGroupOU & strDNSDomain)
   objGroup.add(objUser.ADsPath) 
   IF (Err.Number <> 0) Then
      on Error Goto 0
      Call Log("<font color=orange>Member " & strMemberI & " already exists? (Skipped)<br>")
   ELSE
      if VERBOSE Then
        Call Log("<font color=green>Member " & strmemberI & " added to " & strGroupI & "<br>")
      End If
   End If
End Sub


Private Sub CreateFolders(strUserName, strProjectName, strGroupName, strDriveName, VERBOSE)
  Dim orgLogFileIndent, strSharedDirectory

  orgLogFileIndent = logFileIndent
  logFileIndent = logFileIndent & "    "
  strSharedDirectory = strDriveName & ":\Projects\"
  
  Call Log("Create Folders" & "<br>")
  Call Log("  User Name     : " & strUserName & "<br>")
  Call Log("  Project Name  : " & strProjectName & "<br>")
  Call Log("  Group Name    : " & strGroupName & "<br>")
  Call Log("  Drive Name    : " & strDriveName & "<br>")
  Call Log("  Project Folder: " & strSharedDirectory & "<br>")
  Call Log("<br>")

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
  End If
  
  logFileindent = orgLogFileIndent
End Sub


private Sub CreateFTPOnlyFolders(strUserName, VERBOSE)
  Dim orgLogFileIndent, strSharedDirectory

  orgLogFileIndent = logFileIndent
  logFileIndent = logFileIndent & "    "
  strSharedDirectory = strDriveName & ":\Projects\"
  
  Call Log("Create FTP Folders" & "<br>")
  Call Log("  User Name     : " & strUserName & "<br>")
  Call Log("  Group Name    : " & strGroupName & "<br>")
  Call Log("<br>")

  If Len(strUserName) > 0 Then
    Call CreateFolder(strDownloadFtpDirectory & strUserName, VERBOSE)
    Call CreateFolder(strUploadFtpDirectory & strUserName, VERBOSE)
  End If
  
  logFileindent = orgLogFileIndent
End Sub


Private Sub CreateFolder(strDirectory, VERBOSE)
 ' Create FileSystemObject. So we can apply .createFolder method
 Dim objFSO, objFolder, DEBUG

 If VERBOSE=1 Then
      Call Log("Creating " & strDirectory & "<br>")
 End If
 Set objFSO = CreateObject("Scripting.FileSystemObject")
 If objFSO.FolderExists(strDirectory) Then
   If (CInt(VERBOSE)>-1) Then
     Set objFolder = objFSO.GetFolder(strDirectory) 
     Call Log(strDirectory & " was already created<br>")
   End If
 Else
   Set objFolder = objFSO.CreateFolder(strDirectory)
   if (VERBOSE=1) Then
      'WScript.Echo "Just created " & strDirectory
   End If
 End If

End Sub


Private Sub SetPermissions(strDirectory, strName, strPermissions, VERBOSE)
' sets the permissions for a user or group

 Dim intRunError, objShell, objFSO, DEBUG

 Set objShell = CreateObject("Wscript.Shell")
 Set objFSO = CreateObject("Scripting.FileSystemObject")
 If objFSO.FolderExists(strDirectory) Then
   ' Assign user permission to home folder.
   intRunError = objShell.Run("%COMSPEC% /c Echo Y| icacls " & strDirectory & " /c /grant " & Chr(34) & strName & Chr(34) & ":" & strPermissions , 2, True) 

   If intRunError <> 0 Then
     Call Log("<font color=red>Error assigning permissions for user " & strUserName & " to folder " & strDirectory & "<font color=black><br>")
     exitCode = 10
   Else
     if (VERBOSE="1") Then
       Call Log("<font color=green>" & strPermissions & " permissions set for " & strDirectory & " for " & strName & "<font color=black><br>")
     End If
   End If
 End If

End Sub


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
     Call Log("<font color=red>Error assigning permissions for user " & strUserName & " to folder " & strDirectory & "<font color=black><br>")
     exitCode = 11
   Else
     if (VERBOSE="1") Then
       Call Log("<font color=green>" & strName & " permissions removed for " & strDirectory & "<font color=black><br>")
     End If
   End If
 End If

End Sub


Private Sub OpenLogFile(fileName, indent)
  logFileName = fileName
  logFileIndent = indent
  If StrComp(logFileName, "") <> 0 Then
    Set objLogFile = CreateObject("Scripting.FileSystemObject").OpenTextFile(logFileName,8,false)
  End If
End Sub


Private Sub Log(text)
  objExplorer.document.title = "Please be patient.... "
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
