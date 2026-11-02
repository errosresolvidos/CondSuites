package com.example.condsuites.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Locale

enum class SyncStatus {
    SYNCED, PENDING_INSERT, PENDING_UPDATE, PENDING_DELETE, FAILED
}

@Entity(tableName = "occurrences")
data class OccurrenceEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("apartment") @set:PropertyName("apartment") var apartment: String = "",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "ABERTA",
    @get:PropertyName("createdByUsername") @set:PropertyName("createdByUsername") var createdByUsername: String = "",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "GERAL",
    @get:PropertyName("occurrenceType") @set:PropertyName("occurrenceType") var occurrenceType: String = "",
    @get:PropertyName("isUrgent") @set:PropertyName("isUrgent") var isUrgent: Boolean = false,
    var syncStatus: String = SyncStatus.SYNCED.name,
    var lastSyncedTimestamp: Long = 0
)

@Entity(
    tableName = "occurrence_messages",
    foreignKeys = [
        ForeignKey(
            entity = OccurrenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["occurrenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("occurrenceId")]
)
data class OccurrenceMessageEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("occurrenceId") @set:PropertyName("occurrenceId") var occurrenceId: Long = 0,
    @get:PropertyName("senderUsername") @set:PropertyName("senderUsername") var senderUsername: String = "",
    @get:PropertyName("text") @set:PropertyName("text") var text: String = "",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("isCouncilOnly") @set:PropertyName("isCouncilOnly") var isCouncilOnly: Boolean = false,
    @get:PropertyName("isSindicoOnly") @set:PropertyName("isSindicoOnly") var isSindicoOnly: Boolean = false,
    @get:PropertyName("isRead") @set:PropertyName("isRead") var isRead: Boolean = false,
    @get:PropertyName("isVotingClosed") @set:PropertyName("isVotingClosed") var isVotingClosed: Boolean = false,
    @get:PropertyName("isBudget") @set:PropertyName("isBudget") var isBudget: Boolean = false,
    @get:PropertyName("isEdited") @set:PropertyName("isEdited") var isEdited: Boolean = false,
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = System.currentTimeMillis(),
    var syncStatus: String = SyncStatus.SYNCED.name,
    var lastSyncedTimestamp: Long = 0
)

@Entity(
    tableName = "occurrence_attachments",
    foreignKeys = [
        ForeignKey(
            entity = OccurrenceMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("messageId")]
)
data class OccurrenceAttachmentEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("messageId") @set:PropertyName("messageId") var messageId: Long = 0,
    @get:PropertyName("occurrenceId") @set:PropertyName("occurrenceId") var occurrenceId: Long = 0,
    @get:PropertyName("fileName") @set:PropertyName("fileName") var fileName: String = "",
    @get:PropertyName("filePath") @set:PropertyName("filePath") var filePath: String = "",
    var syncStatus: String = SyncStatus.SYNCED.name,
    var lastSyncedTimestamp: Long = 0
)

@Entity(
    tableName = "occurrence_attachment_votes",
    foreignKeys = [
        ForeignKey(
            entity = OccurrenceAttachmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["attachmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("attachmentId")]
)
data class OccurrenceAttachmentVoteEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("attachmentId") @set:PropertyName("attachmentId") var attachmentId: Long = 0,
    @get:PropertyName("username") @set:PropertyName("username") var username: String = "",
    @get:PropertyName("voteType") @set:PropertyName("voteType") var voteType: String = "ACCEPT",
    var syncStatus: String = SyncStatus.SYNCED.name,
    var lastSyncedTimestamp: Long = 0
)

data class AttachmentWithVotes(
    @Embedded val attachment: OccurrenceAttachmentEntity,
    @Relation(parentColumn = "id", entityColumn = "attachmentId")
    val votes: List<OccurrenceAttachmentVoteEntity>
)

data class MessageWithAttachments(
    @Embedded val message: OccurrenceMessageEntity,
    @Relation(
        entity = OccurrenceAttachmentEntity::class,
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val attachments: List<AttachmentWithVotes>,
    @Relation(
        entity = OccurrenceMessageReadEntity::class,
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val reads: List<OccurrenceMessageReadEntity>
)

@Entity(
    tableName = "occurrence_message_reads",
    foreignKeys = [
        ForeignKey(
            entity = OccurrenceMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("messageId"), Index("username")]
)
data class OccurrenceMessageReadEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("messageId") @set:PropertyName("messageId") var messageId: Long = 0,
    @get:PropertyName("username") @set:PropertyName("username") var username: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = System.currentTimeMillis(),
    var syncStatus: String = SyncStatus.SYNCED.name,
    var lastSyncedTimestamp: Long = 0
)

fun generateReadEntityId(messageId: Long, username: String): String {
    return "${messageId}_${username.trim().lowercase()}"
}

data class OccurrenceWithMessages(
    @Embedded val occurrence: OccurrenceEntity,
    @Relation(
        entity = OccurrenceMessageEntity::class,
        parentColumn = "id",
        entityColumn = "occurrenceId"
    )
    val messages: List<MessageWithAttachments>
)

@Entity(tableName = "occurrence_logs")
data class OccurrenceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val occurrenceId: Long = 0,
    val username: String = "",
    val action: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

fun isSuperAdminUser(currentUser: UserEntity): Boolean {
    val username = currentUser.username
    return username.equals("samuel", ignoreCase = true) || 
           username.equals("admin", ignoreCase = true) || 
           username.equals("usuario", ignoreCase = true) ||
           username.equals("usuario2", ignoreCase = true) ||
           username.equals("usuario3", ignoreCase = true) ||
           username.equals("usuario 3", ignoreCase = true)
}

fun canUserSeeOccurrence(occ: OccurrenceEntity, currentUser: UserEntity): Boolean {
    val role = currentUser.role
    if (occ.type == "ADMIN") {
        return isSuperAdminUser(currentUser)
    }
    if (occ.type == "CONSELHO") {
        return role == "Síndico" || role == "Conselheiro Fiscal" || role == "ADMIN" || currentUser.username.equals("admin", ignoreCase = true)
    }
    if (role == "Zelador" || role == "Porteiro") {
        return occ.type == "GERAL" && occ.status != "ARQUIVADA"
    }
    return true
}

fun canUserSeeMessage(msg: OccurrenceMessageEntity, currentUser: UserEntity): Boolean {
    val role = currentUser.role
    val username = currentUser.username
    if (msg.senderUsername.equals(username, ignoreCase = true)) {
        return true
    }
    if (role == "ADMIN" || username.equals("admin", ignoreCase = true)) {
        return true
    }
    if (msg.isSindicoOnly) {
        return role == "Síndico"
    }
    if (msg.isCouncilOnly || msg.isBudget) {
        return role == "Síndico" || role == "Conselheiro Fiscal" || role == "Zelador"
    }
    return true
}

fun isMessageUnreadForUser(
    msg: OccurrenceMessageEntity,
    occ: OccurrenceEntity,
    currentUser: UserEntity,
    reads: List<OccurrenceMessageReadEntity> = emptyList()
): Boolean {
    if (msg.senderUsername.equals(currentUser.username, ignoreCase = true)) return false
    if (msg.senderUsername == "admin" && currentUser.role == "ADMIN") return false
    if (msg.senderUsername.equals("SISTEMA", ignoreCase = true) && msg.text.contains("por ${currentUser.username}", ignoreCase = true)) return false
    if (!canUserSeeOccurrence(occ, currentUser)) return false
    if (!canUserSeeMessage(msg, currentUser)) return false
    if (msg.isRead) return false
    val hasRead = reads.any { it.username.equals(currentUser.username, ignoreCase = true) }
    if (hasRead) return false
    return true
}

fun getOccurrenceCreationMillis(occ: OccurrenceEntity, messages: List<MessageWithAttachments>): Long {
    if (occ.id > 1_000_000_000_000L) {
        return occ.id
    }
    if (occ.date.isNotBlank()) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val parsed = sdf.parse(occ.date)
            if (parsed != null) return parsed.time
        } catch (_: Exception) {}
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsed = sdf.parse(occ.date)
            if (parsed != null) return parsed.time
        } catch (_: Exception) {}
    }
    val firstMsg = messages.sortedBy { it.message.id }.firstOrNull()?.message
    if (firstMsg != null) {
        if (firstMsg.id > 1_000_000_000_000L) {
            return firstMsg.id
        }
        if (firstMsg.date.isNotBlank()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val parsed = sdf.parse(firstMsg.date)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
    }
    return System.currentTimeMillis()
}

fun getOccurrenceLastActivityMillis(occ: OccurrenceEntity, messages: List<MessageWithAttachments>): Long {
    val lastMsg = messages.maxByOrNull { it.message.id }?.message
    if (lastMsg != null) {
        if (lastMsg.id > 1_000_000_000_000L) {
            return lastMsg.id
        }
        if (lastMsg.date.isNotBlank()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val parsed = sdf.parse(lastMsg.date)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
    }
    return getOccurrenceCreationMillis(occ, messages)
}

