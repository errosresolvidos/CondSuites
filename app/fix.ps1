$ErrorActionPreference = 'Stop'
$mainPath = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\MainActivity.kt'
$content = [System.IO.File]::ReadAllText($mainPath, [System.Text.Encoding]::UTF8)
$content = $content.Replace('$$', '$')
[System.IO.File]::WriteAllText($mainPath, $content, [System.Text.Encoding]::UTF8)
Write-Output "Fixed successfully"