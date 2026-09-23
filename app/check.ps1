$m = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\MainActivity.kt'
$content = [System.IO.File]::ReadAllText($m, [System.Text.Encoding]::UTF8)
if ($content -match 'val firstMsg = OccurrenceMessageEntity') {
    Write-Output 'YES IT IS THERE'
} else {
    Write-Output 'NO IT IS NOT THERE'
}