$m = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\MainActivity.kt'
$c = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\OccurrenceComponents.kt'
$mc = [System.IO.File]::ReadAllText($m, [System.Text.Encoding]::UTF8)

# Just copy the whole thing into OccurrenceComponents.kt
[System.IO.File]::WriteAllText($c, $mc, [System.Text.Encoding]::UTF8)
Write-Output "Done copying"
