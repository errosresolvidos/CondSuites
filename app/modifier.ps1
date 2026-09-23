$ErrorActionPreference = 'Stop'

$mainPath = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\MainActivity.kt'
$artifactPath = 'C:\Users\samue\AppData\Local\Google\AndroidStudio2026.1.3\projects\condsuites.400619ec\.artifacts\dc9f52c4-7dfb-4b9f-9259-89c4676038f4\arquivamento_ocorrencias.artifact.md'

$mainContent = [System.IO.File]::ReadAllText($mainPath, [System.Text.Encoding]::UTF8)
$artifactContent = [System.IO.File]::ReadAllText($artifactPath, [System.Text.Encoding]::UTF8)

$match = [regex]::Match($artifactContent, '(?s)```kotlin\r?\n(.*?)```')
if ($match.Success) {
    $code = $match.Groups[1].Value.Trim()

    # We must escape $ for regex replace by using $$
    $codeEscaped = $code.Replace('$', '$$$$')

    $pattern = '(?s)fun OccurrencesScreen\(.*?(?=@Composable\s*fun ProfessionalBudgetBadgeIcon\()'
    $replacement = $codeEscaped + "`r`n`r`n"

    $newContent = [regex]::Replace($mainContent, $pattern, $replacement)

    [System.IO.File]::WriteAllText($mainPath, $newContent, [System.Text.Encoding]::UTF8)
    Write-Output "Script concluido com sucesso!"
} else {
    Write-Output "Falha ao encontrar o bloco kotlin"
}