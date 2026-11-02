import java.io.File

val file = File("C:/Users/samue/AndroidStudioProjects/CondSuites/app/src/main/java/com/example/condsuites/ui/screens/OccurrencesScreen.kt")
var content = file.readText()

content = content.replace("import com.example.condsuites.service.FirestoreSyncManager", "import com.example.condsuites.service.FirestoreSyncManager\nimport com.example.condsuites.service.sync.OccurrenceSyncScheduler\nimport com.example.condsuites.data.model.SyncStatus")

// Update syncStatus before dao operation. Actually it's easier to just replace FirestoreSyncManager calls.
val regexOccurrence = Regex("FirestoreSyncManager\\.syncOccurrence\\((.*?), isDelete = (.*?)\\)")
content = content.replace(regexOccurrence) { match ->
    val obj = match.groupValues[1]
    val isDel = match.groupValues[2]
    "$obj.syncStatus = if ($isDel) SyncStatus.PENDING_DELETE.name else SyncStatus.PENDING_UPDATE.name\n" +
    "OccurrenceSyncScheduler.scheduleSync(context)"
}

val regexOccurrenceNoDel = Regex("FirestoreSyncManager\\.syncOccurrence\\((.*?)\\)")
content = content.replace(regexOccurrenceNoDel) { match ->
    val obj = match.groupValues[1]
    "$obj.syncStatus = SyncStatus.PENDING_UPDATE.name\n" +
    "OccurrenceSyncScheduler.scheduleSync(context)"
}

val regexMsg = Regex("FirestoreSyncManager\\.syncOccurrenceMessage\\((.*?), isDelete = (.*?)\\)")
content = content.replace(regexMsg) { match ->
    val obj = match.groupValues[1]
    val isDel = match.groupValues[2]
    "$obj.syncStatus = if ($isDel) SyncStatus.PENDING_DELETE.name else SyncStatus.PENDING_UPDATE.name\n" +
    "OccurrenceSyncScheduler.scheduleSync(context)"
}

val regexMsgNoDel = Regex("FirestoreSyncManager\\.syncOccurrenceMessage\\((.*?)\\)")
content = content.replace(regexMsgNoDel) { match ->
    val obj = match.groupValues[1]
    "$obj.syncStatus = SyncStatus.PENDING_UPDATE.name\n" +
    "OccurrenceSyncScheduler.scheduleSync(context)"
}

val regexAtt = Regex("FirestoreSyncManager\\.syncAttachment\\((.*?)\\)")
content = content.replace(regexAtt) { match ->
    val obj = match.groupValues[1]
    "$obj.syncStatus = SyncStatus.PENDING_UPDATE.name\n" +
    "OccurrenceSyncScheduler.scheduleSync(context)"
}

val regexVote = Regex("FirestoreSyncManager\\.syncAttachmentVote\\((.*?), isDelete = (.*?)\\)")
content = content.replace(regexVote) { match ->
    val obj = match.groupValues[1]
    val isDel = match.groupValues[2]
    "$obj.syncStatus = if ($isDel) SyncStatus.PENDING_DELETE.name else SyncStatus.PENDING_UPDATE.name\n" +
    "OccurrenceSyncScheduler.scheduleSync(context)"
}

val regexVoteNoDel = Regex("FirestoreSyncManager\\.syncAttachmentVote\\((.*?)\\)")
content = content.replace(regexVoteNoDel) { match ->
    val obj = match.groupValues[1]
    "$obj.syncStatus = SyncStatus.PENDING_UPDATE.name\n" +
    "OccurrenceSyncScheduler.scheduleSync(context)"
}

val regexRead = Regex("FirestoreSyncManager\\.syncOccurrenceMessageRead\\((.*?)\\)")
content = content.replace(regexRead) { match ->
    val obj = match.groupValues[1]
    "$obj.syncStatus = SyncStatus.PENDING_UPDATE.name\n" +
    "OccurrenceSyncScheduler.scheduleSync(context)"
}

val regexTouch = Regex("FirestoreSyncManager\\.touchParentOccurrence\\(dao, (.*?)\\)")
content = content.replace(regexTouch) { match ->
    val obj = match.groupValues[1]
    "dao.getOccurrenceEntityById($obj)?.let { it.syncStatus = SyncStatus.PENDING_UPDATE.name; dao.updateOccurrence(it); OccurrenceSyncScheduler.scheduleSync(context) }"
}

file.writeText(content)
