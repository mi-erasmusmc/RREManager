 CreateUsers.vbs
' VBScript program to create users according to the information in a
' Microsoft Excel spreadsheet.
'
' ----------------------------------------------------------------------
' Copyright (c) 2003-2010 Richard L. Mueller
' Modified 2011 Peter Rijnbeek
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

Option Explicit

Dim VERBOSE
Dim Update, FTPOnly, strFirst, strLast, strInitials, strUserName, strPW, strProjects, strGroups, strEmail
Dim strGroupDN, objUser, objGroup, objContainer
Dim strCN, strNTName, strContainerDN
Dim strHomeFolder, strHomeDrive, objFSO, objShell
Dim intRunError, strNetBIOSDomain, strDNSDomain, strProjectName
Dim objRootDSE, objTrans, strLogonScript, strUPN
Dim strPreviousDN, blnBound, multiOTPGroup
Dim FolderScript, FTPOnlyFolderScript, objExplorer
Dim strUserOU, strGroupOU, strGroup, strUser
Dim objRootLDAP

multiOTPGroup = "MultiOTP Users"
FolderScript = "F:\Administration\Scripts\createFolders.vbs"
FTPOnlyFolderScript = "F:\Administration\Scripts\createFTPOnlyFolders.vbs"


' Constants for the NameTranslate object.
Const ADS_NAME_INITTYPE_GC = 3
Const ADS_NAME_TYPE_NT4 = 3
Const ADS_NAME_TYPE_1779 = 1
Const ADS_UF_SMARTCARD_REQUIRED = &h40000
Const ADS_UF_DONT_EXPIRE_PASSWD = &h10000

Set objFSO = CreateObject("Scripting.FileSystemObject")
Set objShell = CreateObject("Wscript.Shell")

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

' Arguments:
'   first name
'   initial
'   last name
'   username
'   password
'   email address
'   projects
'   groups
'   update flag (1 = update)
args = WScript.Arguments.Count

If args > 5 then
  strFirst = Wscript.Arguments.Item(0)
  strInitials = Wscript.Arguments.Item(1)
  strLast = Wscript.Arguments.Item(2)
  strUserName = Wscript.Arguments.Item(3)
  strPW = Wscript.Arguments.Item(4)
  strEmail = Wscript.Arguments.Item(5)
  strProjects = Wscript.Arguments.Item(6)
  strGroups = Wscript.Arguments.Item(7)
  strUpdate = Wscript.Arguments.Item(8)
  
  DIM ProjectsArray
  ProjectsArray = Split(strProjects,",")
        
  DIM groupArray
  groupArray = Split(strGroups,",")
  
  
  If strUpdate = "1" Then
    Update = True
  Else
    Update = False
  End If
  
  

  If strGroups = "" Then
  	' FTP Only user (no group(s) specified)
    FTPOnly = 1
    strCN = strFirst & " " & strLast
    strNTName = LCase(Left(strFirst,1) & strLast)
    strNTName = Replace(strNTName," ","")
    
    ' Show some info to the user
    objExplorer.document.write "<p><font color=black>-----------------------------------------------------------------<br>"
    objExplorer.document.write "Adding FTP only user: " & strCN & "</p>" 
        
    objExplorer.document.write "<font color=green>Creating folders for FTP<br>"
    objShell.run chr(34) & FTPOnlyFolderScript & chr(34) & " " & chr(34) & strNTName & chr(34) & " " & VERBOSE,1,true
        
    objExplorer.document.write "<p><font color=black>-----------------------------------------------------------------</p><br>"
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
      objExplorer.document.write "<p><font color=black>-----------------------------------------------------------------<br>"
      objExplorer.document.write "Adding user: " & strCN & "</p>" 
    
      ' Is this a new user?
         
      If Not DoesExist( strDNSDomain, strNTName ) OR Update Then
          If Update Then
  	        objExplorer.document.write "<font color=red>Only Adding missing projects and groups for user: " & strNTName & "<br>"
          End If
          ' If this container is different from the previous, bind to
          ' the container the user object will be created in.
    
          If (strContainerDN <> strPreviousDN) Then
              On Error Resume Next
              Set objContainer = GetObject("LDAP://" & strContainerDN)
              If (Err.Number <> 0) Then
                  On Error GoTo 0
                  Wscript.Echo "Unable to bind to container: " & strContainerDN
                  Wscript.Echo "Unable to create user with NT name: " & strNTName
                  ' Flag that container not bound.
                  strPreviousDN = ""
  	              noError = 0	
              Else
                  On Error GoTo 0
                  strPreviousDN = strContainerDN
              End If
          End If
          ' Proceed if parent container bound.
          If (strPreviousDN <> "") Then
              On Error Resume Next
              If NOT Update Then
                ' Create user object.
                Set objUser = objContainer.Create("user", "cn=" & strCN)
              Else
                ' Get user object.
                Set objUser = GetObject("LDAP://cn=" & strCN & "," & strContainerDN)
              End If
    
              If (Err.Number <> 0) Then
                  On Error GoTo 0
                  Wscript.Echo "Unable to create user with cn: " & strCN
    
              Else
                  ' Assign mandatory attributes and save user object.
                  If (strNTName = "") Then
                      strNTName = strCN
                  End If
                  objUser.sAMAccountName = strNTName
                  On Error Resume Next
                  objUser.SetInfo
                  If (Err.Number <> 0) Then
                      On Error GoTo 0
                      objExplorer.document.write "<font color=red>Unable to create user with NT name: " & strNTName & "<br>"
                  Else
                      ' Set password for user.
                      objUser.SetPassword strPW
                      objExplorer.document.write "<font color=green>password for user " & strNTName & " " & strPW & "<br>"
                      If (Err.Number <> 0) Then
                          On Error GoTo 0
                          objExplorer.document.write "<font color=red>Unable to set password for user " & strNTName & "<br>"
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
                        objExplorer.document.write "<font color=red>Unable to set attributes for user with NT name: " & strNTName & "<br>"
                      End If
                      On Error GoTo 0
                      ' Create home folder.
                      If (strHomeFolder <> "") Then
                          If (objFSO.FolderExists(strHomeFolder) = False) Then
                              On Error Resume Next
                              objFSO.CreateFolder strHomeFolder
                              If (Err.Number <> 0) Then
                                  On Error GoTo 0
                                  objExplorer.document.write "<font color=red>Unable to create home folder: " & strHomeFolder & "<br>"
                              End If
                              On Error GoTo 0
                          End If
                          If (objFSO.FolderExists(strHomeFolder) = True) Then
                              ' Assign user permission to home folder.
                              intRunError = objShell.Run("%COMSPEC% /c Echo Y| cacls " & strHomeFolder & " /T /E /C /G " & strNetBIOSDomain & "\" & strNTName & ":F", 2, True)
                              If (intRunError <> 0) Then
                                  objExplorer.document.write "<font color=red>Error assigning permissions for user " & strNTName & " to home folder " & strHomeFolder & "<br>"
                              End If
                          End If
                      End If
       
                      ' Group DN's (Comma Seperated).
                      objExplorer.document.write "<font color=green>Start Group assignment<br>"
              
                      DIM counter
                      For counter = 0 to UBound(groupArray)
                          strGroupDN = Trim(groupArray(counter))
                          ' Attempt to bind to group object DN.
                          blnBound = False
                          On Error Resume Next
                          Set objGroup = GetObject("LDAP://" & strGroupDN)
                          If (Err.Number <> 0) Then
                              On Error GoTo 0
                              ' Try  again converting NT Name to DN.
                              On Error Resume Next
                              objTrans.Set ADS_NAME_TYPE_NT4, strNetBIOSDomain & "\" & strGroupDN
                              If (Err.Number <> 0) Then
                                  On Error GoTo 0
                                  objExplorer.document.write "<font color=red>Unable to bind to group " & strGroupDN & "<br>"
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
                                  objExplorer.document.write "<font color=red>user " & strNTName & " already added to group " & strGroupDN  & "? (skipped)<br>"
                              End If
                          End If
                          On Error GoTo 0
                      Next
                   
                      ' Create folders
                      If Update Then
                         VERBOSE = -1 ' No messages about existing directories
                         objExplorer.document.write "<font color=Red>User folders will only be created for new projects <br>"
                      Else
                         VERBOSE = 0 ' Only messages about existing directories
                      End If
    		
                      For counter = 0 to UBound(ProjectsArray)
                        If NOT Update Then
                          objExplorer.document.write "<font color=green>Creating folders for project: " & ProjectsArray(counter) & "<br>"
                        End If
                        strProjectName = Replace(ProjectsArray(counter)," ","")
                        objShell.run chr(34) & FolderScript & chr(34) & " " & chr(34) & strNTName & chr(34) & " " & chr(34) & strProjectName & chr(34) & " " & chr(34) & strProjectName & " Researchers" & chr(34) & " " & VERBOSE & " D",1,true
                        objExplorer.document.write "<font color=green>User folders have been created. <br>"
                      Next
                  End If
              End If
          End If
      Else
          objExplorer.document.write "<font color=orange>Skipped, already exists! <br>"
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
      ON Error Resume Next
      Set objUser = GetObject("LDAP://"& strUser & strUserOU & strDNSDomain)
      Set objGroup = GetObject("LDAP://"& strGroup & strGroupOU & strDNSDomain)
      objGroup.add(objUser.ADsPath) 
      If (Err.Number <> 0) Then
        on Error Goto 0
  	    objExplorer.document.write "<font color=orange>" & strCN & " already member of group " & multiOTPGroup & "? (Skipped)<br>"
      Else
   	    objExplorer.document.write "<font color=green>" & strCN & " added to group " & multiOTPGroup & "<br>"
      End If
        
      objExplorer.document.write "<p><font color=black>-----------------------------------------------------------------</p><br>"
  End If
End If