Param([string]$Text1, [string]$Text2)

Add-Type -AssemblyName PresentationCore,PresentationFramework
$Result = [System.Windows.MessageBox]::Show($Text1 + " " + $Text2)

exit 1
