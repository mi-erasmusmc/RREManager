param([string]$Project, [string]$Group, [String]$OrganizationalUnit)

# Create Data drive
$gpoName = "Map " + $Project + " " + $Group + " Data"
$securityFilter = $Project + " " + $Group
New-GPO -Name $gpoName
New-GPLink -Name $gpoName -Target $OrganizationalUnit
Set-GPPermission -Name $gpoName -TargetName $securityFilter -TargetType Group -PermissionLevel GpoApply -Replace

# Create Share drive
$gpoName = "Map User Share " + $Project
$securityFilter = $Project + " Researchers"
New-GPO -Name $gpoName
New-GPLink -Name $gpoName -Target $OrganizationalUnit
Set-GPPermission -Name $gpoName -TargetName $securityFilter -TargetType Group -PermissionLevel GpoApply -Replace

exit 0