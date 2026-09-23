$ErrorActionPreference = 'Stop'
$main = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\MainActivity.kt'
$comp = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\OccurrenceComponents.kt'

$content = [System.IO.File]::ReadAllText($main, [System.Text.Encoding]::UTF8)

# Extract imports
$importsMatch = [regex]::Match($content, '(?s)^package.*?import.*?(?=@Entity|@Composable|class|fun)')
$imports = $importsMatch.Value

# We want to extract OccurrencesScreen, OccurrenceCard, and RegisterOccurrenceDialog
$regex1 = '(?s)(@OptIn\(ExperimentalMaterial3Api::class\)\s*@Composable\s*fun OccurrencesScreen\(.*?\})(?=\s*@OptIn|\s*@Composable|\s*fun)'
$match1 = [regex]::Match($content, $regex1)
$occScreen = $match1.Value

$regex2 = '(?s)(@OptIn\(ExperimentalFoundationApi::class\)\s*@Composable\s*fun OccurrenceCard\(.*?\})(?=\s*@OptIn|\s*@Composable|\s*fun)'
$match2 = [regex]::Match($content, $regex2)
$occCard = $match2.Value

$regex3 = '(?s)(@OptIn\(ExperimentalMaterial3Api::class\)\s*@Composable\s*fun RegisterOccurrenceDialog\(.*?\})(?=\s*@OptIn|\s*@Composable|\s*fun)'
$match3 = [regex]::Match($content, $regex3)
$regDialog = $match3.Value

if ($match1.Success -and $match2.Success -and $match3.Success) {
    # Remove them from main
    $newMain = $content.Replace($occScreen, '').Replace($occCard, '').Replace($regDialog, '')
    [System.IO.File]::WriteAllText($main, $newMain, [System.Text.Encoding]::UTF8)

    # Write to components
    $newComp = $imports + "`r`n" + $occScreen + "`r`n`r`n" + $occCard + "`r`n`r`n" + $regDialog + "`r`n"
    [System.IO.File]::WriteAllText($comp, $newComp, [System.Text.Encoding]::UTF8)

    Write-Output "Extracted successfully"
} else {
    Write-Output "Could not find all functions"
}
