# RREManager

The RREManager is a tool to support the management of projects and users on the Erasmus MC Remote Research Environment Octopus.

java -jar RREManager-vx.x.jar [settings=<path of ini file]

When no parameter is specified it uses the RREManager-vx.x.ini file that is in the same folder as the RREManager-vx.x.jar file.


## The user definitions

The RREManager works with the user definitions as specified in an Excel file. This Excel file should have the following columns in the specified order:

| Column name          | Contents                                                     |
| -------------------- | ------------------------------------------------------------ |
| Update Column        | Flag (empty or 1) to signal that a user should be updated (added projects/groups) in the active directory. |
| Projects Column      | A comma separated list of projects the user participates in. |
| Groups Column        | A comma separated list of groups the user participates in. A group name always consists of the project name followed by a space followed by the group name. |
| First Name Column    | The first name of the user.                                  |
| Initials Column      | The initials of the user.                                    |
| Last Name Column     | The last name of the user.                                   |
| User Name Column     | The user name of the user. Usually the first initial followed by the last name. |
| Password Column      | The password of the user.                                    |
| Email Column         | The email addres of the user.                                |
| Email Format Column  | The prefered email format, HTML or TEXT, of the user.        |
| IP-Addresses Column  | The IP-addresses the user wants to access the RRE from.      |



## The ini file

The structure of the ini file is:

```
[General]
Log Folder = <path to where the log files will be written>


[Administrators]
<admnistrator name>
  :
<admnistrator name>


# This section should be present for each administrator in the [Administrators] section.
# When it is not present that administrator cannot be selected.
[<admnistrator name>]
User Account = <the email username of the administrator>
Title        = <the title of the administrator>
Telephone    = <the telephone number of the administrator>


# Excel file and sheet specifications for regular RDP and FTP-Only researchers
[User Projects]
File                = <path to the users Excel file>
Sheet               = <the name of the sheet in the Excel file with the users>
Update Column       = <the name of the column in the Excel file with the update flag>
Projects Column     = <the name of the column in the Excel file with the projects of the user> 
Groups Column       = <the name of the column in the Excel file with the groups of the user>
First Name Column   = <the name of the column in the Excel file with the first name of the user>
Initials Column     = <the name of the column in the Excel file with the initials of the user>
Last Name Column    = <the name of the column in the Excel file with the last name of the user>
User Name Column    = <the name of the column in the Excel file with the user name of the user>
Password Column     = <the name of the column in the Excel file with the password of the user>
Email Column        = <the name of the column in the Excel file with the email address of the user>
Email Format Column = <the name of the column in the Excel file with the perferred email format (HTML or TEXT) of the user>
IP-Addresses Column = <the name of the column in the Excel file with the IP-addresses of the user>


# Excel file and sheet specifications for special FTP-Only users (NOT researchers; e.g. GP Information Systems)
[Specials]
File                = <path to the users Excel file>
Sheet               = <the name of the sheet in the Excel file with the users>
Update Column       = <the name of the column in the Excel file with the update flag>
Projects Column     = <the name of the column in the Excel file with the projects of the user> 
Groups Column       = <the name of the column in the Excel file with the groups of the user>
First Name Column   = <the name of the column in the Excel file with the first name of the user>
Initials Column     = <the name of the column in the Excel file with the initials of the user>
Last Name Column    = <the name of the column in the Excel file with the last name of the user>
User Name Column    = <the name of the column in the Excel file with the user name of the user>
Password Column     = <the name of the column in the Excel file with the password of the user>
Email Column        = <the name of the column in the Excel file with the email address of the user>
Email Format Column = <the name of the column in the Excel file with the perferred email format (HTML or TEXT) of the user>
IP-Addresses Column = <the name of the column in the Excel file with the IP-addresses of the user>


# Excel file and sheet specifications for the definitions of the projects (NOT YET USED)
[Projects]
File                = <path to the users Excel file>
Sheet               = <the name of the sheet in the Excel file with the project definitions>
Project Column      = <the name of the column in the Excel file with the project name> 
Group Column        = <the name of the column in the Excel file with the project groups>


[MultiOTP]
PDFFolder = <the path to the folder with the MultiOTP .pdf files with the account information>


[SMTP Mail Server]
Server = <the smtp mailserver>
Port   = <the smtp port, usually 25>
from   = <the from email address>
cc     = <optional, the cc email address>


[<mail message type>]
Subject = <subject of the email>
Email   = Only when <email message type> is Firewall Add Mail or Firewall Remove Mail: <recipient email address>
Format  = Only when <email message type> is Firewall Add Mail or Firewall Remove Mail: HTML or TEXT
Text_1  = <line 1 of the email>
    :
Text_N  = <line N of the email>
Attachment_1 = <optional path of extra attachment 1 (e.g. an .rdp file)>
    :
Attachment_N = <optional path of extra attachment N>
Picture_1 = <img alt="Embedded Image" height="<picture height>" width="<picture wdth>" src="data:image/png;base64,<base 64 string of png image>" />
    :
Picture_N = <img alt="Embedded Image" height="<picture height>" width="<picture wdth>" src="data:image/png;base64,<base 64 string of png image>" />


[<message sup part>]
Text_1  = <line 1 of the email>
    : 
Text_N  = <line N of the email>
Picture_1 = <img alt="Embedded Image" height="<picture height>" width="<picture wdth>" src="data:image/png;base64,<base 64 string of png image>" />
    :
Picture_N = <img alt="Embedded Image" height="<picture height>" width="<picture wdth>" src="data:image/png;base64,<base 64 string of png image>" />
```

Comments can be added by starting a line with a '#' character.

There are six predefined email messages that should be defined:

RDP Account Mail<br>
FTP-Only Account Mail<br>
Password Mail<br>
Firewall FTP/RDP Add Mail<br>
Firewall FTP Add Mail<br>
Firewall Remove Mail

In an email you can use the following in-line tags:

[BOLD START] = Start text in bold.<br>
[BOLD END] = End text in bold.<br>
[ITALIC START] = Start of text in italics.<br>
[ITALIC END] = End of text in italics.<br>
[FIRST NAME] = The first name of the recipient.<br>
[LAST NAME] = The last name of the recipient.<br>
[USER NAME] = The user name of the recipient.<br>
[PASSWORD] = The password of the recipient.<br>
[ADMINISTRATOR] = The name of the administrator.<br>
[ADMINISTRATOR TITLE] = ', ' followed he title of the administrator when specified, otherwise nothing.

The following tags can be used only as the only contents of a text line:

[IP ADDRESSES] = A table containing selected IP-addresses (only applicable to "Firewall Add Mail" and "Firewall Remove Mail").<br>
[Picture_...] = A reference to a picture specified in the same definition. Pictures are only included in HTML type emails.<br>
[ADMINISTRATOR TELEPHONE] = 'Tel. " followed by the telephone number of the administrator.<br>
[xxxxxx] = When not equal to any of the predefined tags a reference to an email sub part that will be inserted here.