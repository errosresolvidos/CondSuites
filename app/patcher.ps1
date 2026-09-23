$ErrorActionPreference = 'Stop'
$mainPath = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\MainActivity.kt'
$mainContent = [System.IO.File]::ReadAllText($mainPath, [System.Text.Encoding]::UTF8)

# Fix saveFileToAppStorage
$pattern1 = '(?s)if \(Build\.VERSION\.SDK_INT >= Build\.VERSION_CODES\.Q\) \{\s*saveFileToAppStorage\(context, uri\)\?\.let \{ savedFile ->\s*if \(savedFile\.exists\(\)\) \{\s*val fileName = getFileName\(context, uri\)\s*val att = OccurrenceAttachmentEntity\(\s*messageId = messageId,\s*fileName = fileName,\s*filePath = savedFile\.absolutePath,\s*date = dateStr\s*\)\s*val attId = dao\.insertAttachmentReplace\(att\)\s*FirestoreSyncManager\.syncAttachment\(att\.copy\(id = attId\)\)\s*\}\s*\}\s*\}'
$replacement1 = 'uploadImageToCloudinary(context, uri) { path -> if (path != null) { val att = OccurrenceAttachmentEntity(messageId = messageId, occurrenceId = occWithMsgs.occurrence.id, fileName = getFileName(context, uri), filePath = path); val attId = dao.insertAttachmentReplace(att); FirestoreSyncManager.syncAttachment(att.copy(id = attId)) } }'
$mainContent = [regex]::Replace($mainContent, $pattern1, $replacement1)

# Fix getOccurrenceById
$pattern2 = '(?s)val occToDelete = dao\.getOccurrenceById\(id\)(.*?)FirestoreSyncManager\.syncOccurrence\(occToDelete, isDelete = true\)'
$replacement2 = 'val occToDelete = occurrences.firstOrNull { it.occurrence.id == id }?.occurrence$1if (occToDelete != null) FirestoreSyncManager.syncOccurrence(occToDelete, isDelete = true)'
$mainContent = [regex]::Replace($mainContent, $pattern2, $replacement2)

# Fix RegisterOccurrenceDialog
$pattern3 = '(?s)RegisterOccurrenceDialog\(\s*dao = dao,\s*currentUser = currentUser,\s*onDismiss = \{ showAddDialog = false \},\s*scope = scope,\s*prefilledType = if \(selectedTab == 1\) "CONSELHO" else "GERAL"\s*\)'
$replacement3 = 'RegisterOccurrenceDialog(dao = dao, currentUser = currentUser, onDismiss = { showAddDialog = false }, onConfirm = { title, desc, urgent -> scope.launch { dao.insertOccurrence(OccurrenceEntity(title = title, apartment = currentUser.username, createdByUsername = currentUser.username, isUrgent = urgent, occurrenceType = if (selectedTab == 1) "CONSELHO" else "GERAL")) } })'
$mainContent = [regex]::Replace($mainContent, $pattern3, $replacement3)

# Fix Warning icon
$mainContent = $mainContent.Replace('Icons.Default.Warning', 'Icons.Default.NotificationImportant')

[System.IO.File]::WriteAllText($mainPath, $mainContent, [System.Text.Encoding]::UTF8)
Write-Output 'Patched successfully'
