$ErrorActionPreference = 'Stop'
$mainPath = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\MainActivity.kt'
$mainContent = [System.IO.File]::ReadAllText($mainPath, [System.Text.Encoding]::UTF8)

$pattern = '(?s)uploadImageToCloudinary\(context, uri\) \{ path -> if \(path != null\) \{ val att = (.*?); val attId = dao\.insertAttachmentReplace\(att\); FirestoreSyncManager\.syncAttachment\(att\.copy\(id = attId\)\) \} \}'
$replacement = 'uploadImageToCloudinary(context, uri) { path -> if (path != null) { scope.launch { val att = $1; val attId = dao.insertAttachmentReplace(att); FirestoreSyncManager.syncAttachment(att.copy(id = attId)) } } }'

$mainContent = [regex]::Replace($mainContent, $pattern, $replacement)
[System.IO.File]::WriteAllText($mainPath, $mainContent, [System.Text.Encoding]::UTF8)
Write-Output 'Patched successfully'
