Write-Host "createProjectDriveMapGPOs.ps1"
for (($argNr = 0); $argNr -lt $args.Count; $argNr++) {
  Write-Host "  " $args[$argNr]
}
Write-Host ""


if ($args.count -gt 2) {
  $logFileName = $args[$args.count - 2]
  $logFileIndent = $args[$args.count - 1]
}


function Log {
  param([string]$text)
  
  $logFileIndent + $text | Out-File -FilePath $logFileName -Encoding ASCII -Append
}


if ($args.count -eq 4) {
  $project = $args[0]
  $groups = $args[1]
  $researchers = Get-ADOrganizationalUnit -Filter 'Name -eq "Researchers"' | Format-Table DistinguishedName | Out-String -Stream | Select-String -Pattern 'OU=' | Out-String -Stream
  $researchers = $researchers[1]
  $computername = $env:computername
  
  Log("createProjectDriveMapGPOs.ps1")
  Log("  Project Name: " + $project)
  Log("  Groups      : " + $groups)
  Log("")
  
  $groupList = $groups.Split(",")
  for (($groupNr = 0); $groupNr -lt $groupList.Count; $groupNr++) {
    $group = $groupList[$groupNr]

    # Create Data drive
    $gpoName = "Map " + $project + " " + $group + " Data"
    Log("  Create GPO: " + $gpoName)
    $securityFilter = $project + " " + $group
    New-GPO -Name $gpoName
    New-GPLink -Name $gpoName -Target $researchers
    # Since PowerShell cannot remove the "Authenticated Users" from the Security Filtering without
    # an interactive confirm it is set to GPORead so it is not applied.
    Set-GPPermission -Name $gpoName -TargetName "Authenticated Users" -TargetType Group -PermissionLevel GPORead  
    Set-GPPermission -Name $gpoName -TargetName $securityFilter -TargetType Group -PermissionLevel GpoApply -Replace
    Log("  done")
  }

  # Create Share drive
  $gpoName = "Map User Share " + $project
  Log("  Create GPO: " + $gpoName)
  $securityFilter = $Project + " Researchers"
  New-GPO -Name $gpoName
  New-GPLink -Name $gpoName -Target $researchers
  Set-GPPermission -Name $gpoName -TargetName $securityFilter -TargetType Group -PermissionLevel GpoApply -Replace
  Log("  done")

  exit 0
}
else {
  exit 1
}