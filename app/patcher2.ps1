$ErrorActionPreference = 'Stop'
$mainPath = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\MainActivity.kt'
$mainContent = [System.IO.File]::ReadAllText($mainPath, [System.Text.Encoding]::UTF8)

# Fix saveFileToAppStorage if it's there
$pattern1 = '(?s)if \(Build\.VERSION\.SDK_INT >= Build\.VERSION_CODES\.Q\) \{\s*saveFileToAppStorage.*?\}\s*\}'
$replacement1 = 'uploadImageToCloudinary(context, uri) { path -> if (path != null) { val att = OccurrenceAttachmentEntity(messageId = messageId, occurrenceId = occWithMsgs.occurrence.id, fileName = getFileName(context, uri), filePath = path); val attId = dao.insertAttachmentReplace(att); FirestoreSyncManager.syncAttachment(att.copy(id = attId)) } }'
$mainContent = [regex]::Replace($mainContent, $pattern1, $replacement1)

# Fix the RegisterOccurrenceDialog signature
$pattern3 = '(?s)RegisterOccurrenceDialog\(dao = dao, currentUser = currentUser, onDismiss = \{ showAddDialog = false \}, onConfirm = \{ title, desc, urgent ->.*?\}\)'
$replacement3 = 'RegisterOccurrenceDialog(currentUser = currentUser, dao = dao, onDismiss = { showAddDialog = false }, onConfirm = { title, desc, apt, urgent, uris, type -> scope.launch { dao.insertOccurrence(OccurrenceEntity(title = title, apartment = apt, createdByUsername = currentUser.username, isUrgent = urgent, occurrenceType = type)) } })'
$mainContent = [regex]::Replace($mainContent, $pattern3, $replacement3)

[System.IO.File]::WriteAllText($mainPath, $mainContent, [System.Text.Encoding]::UTF8)
Write-Output 'Patched successfully'
