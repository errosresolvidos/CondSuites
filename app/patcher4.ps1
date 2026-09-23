$ErrorActionPreference = 'Stop'
$mainPath = 'C:\Users\samue\AndroidStudioProjects\CondSuites\app\src\main\java\com\example\condsuites\MainActivity.kt'
$mainContent = [System.IO.File]::ReadAllText($mainPath, [System.Text.Encoding]::UTF8)

# Replace RegisterOccurrenceDialog onConfirm
$pattern = '(?s)RegisterOccurrenceDialog\(currentUser = currentUser, dao = dao, onDismiss = \{ showAddDialog = false \}, onConfirm = \{ title, desc, apt, urgent, uris, type -> scope\.launch \{ dao\.insertOccurrence\(OccurrenceEntity\(title = title, apartment = apt, createdByUsername = currentUser\.username, isUrgent = urgent, occurrenceType = type\)\) \} \}\)'
$replacement = 'RegisterOccurrenceDialog(currentUser = currentUser, dao = dao, onDismiss = { showAddDialog = false }, onConfirm = { title, desc, apt, urgent, uris, type -> scope.launch(Dispatchers.IO) { val newOcc = OccurrenceEntity(title = title, apartment = apt, status = "ABERTA", createdByUsername = currentUser.username, date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()), type = type, isUrgent = urgent); val id = dao.insertOccurrence(newOcc); val savedOcc = newOcc.copy(id = id); sendFcmPushNotification(dao = dao, title = "Nova Ocorrência: $$title", body = "Unidade $$apt: $$desc", senderUsername = currentUser.username, occurrenceId = id); FirestoreSyncManager.syncOccurrence(savedOcc); val firstMsg = OccurrenceMessageEntity(occurrenceId = id, senderUsername = currentUser.username, text = desc, date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())); val messageId = dao.insertOccurrenceMessage(firstMsg); FirestoreSyncManager.syncOccurrenceMessage(firstMsg.copy(id = messageId)); uris.forEach { uri -> uploadImageToCloudinary(context, uri) { path -> if (path != null) { scope.launch(Dispatchers.IO) { val att = OccurrenceAttachmentEntity(occurrenceId = id, messageId = messageId, fileName = getFileName(context, uri), filePath = path); val attId = dao.insertAttachmentReplace(att); FirestoreSyncManager.syncAttachment(att.copy(id = attId)) } } } }; showAddDialog = false } })'

$mainContent = [regex]::Replace($mainContent, $pattern, $replacement)

# Also fix the floating action button to show for Zelador
$patternFab = 'if \(currentUser\.role != "Porteiro" && currentUser\.role != "Zelador"\) \{\s*FloatingActionButton\(\s*onClick = \{ showAddDialog = true \},'
$replacementFab = 'if (currentUser.role != "Porteiro") { FloatingActionButton(onClick = { showAddDialog = true },'
$mainContent = [regex]::Replace($mainContent, $patternFab, $replacementFab)

[System.IO.File]::WriteAllText($mainPath, $mainContent, [System.Text.Encoding]::UTF8)
Write-Output 'Patched successfully'