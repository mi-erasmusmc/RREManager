Add-Type -AssemblyName PresentationCore,PresentationFramework

Write-Host "testScript.ps1"
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
  
  $logFileIndent + $text | Out-File -FilePath $logFileName -Encoding ascii -Append
}

if ($args.count -eq 5) {
  Log("Action: " + $args[0])
  Log("  Project: " + $args[1])
  Log("  Groups : " + $args[2])
  
  $Result = [System.Windows.MessageBox]::Show($args[0] + " " + $args[1] + " " + $args[2])
 
  exit 0
}
else {
  exit 1
}

