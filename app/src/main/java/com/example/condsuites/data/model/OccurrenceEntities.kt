package com.example.condsuites.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "occurrences")
data class OccurrenceEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("apartment") @set:PropertyName("apartment") var apartment: String = "",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "ABERTA",
    @get:PropertyName("createdByUsername") @set:PropertyName("createdByUsername") var createdByUsername: String = "",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "GERAL",
    @get:PropertyName("occurrenceType") @set:PropertyName("occurrenceType") var occurrenceType: String = "",
    @get:PropertyName("isUrgent") @set:PropertyName("isUrgent") var isUrgent: Boolean = false
)

@Entity(
    tableName = "occurrence_messages",
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
    @get:PropertyName("isBudget") @set:PropertyName("isBudget") var isBudget: Boolean = false
)

@Entity(
    tableName = "occurrence_attachments",
    indices = [Index("messageId")]
)
data class OccurrenceAttachmentEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("messageId") @set:PropertyName("messageId") var messageId: Long = 0,
    @get:PropertyName("occurrenceId") @set:PropertyName("occurrenceId") var occurrenceId: Long = 0,
    @get:PropertyName("fileName") @set:PropertyName("fileName") var fileName: String = "",
    @get:PropertyName("filePath") @set:PropertyName("filePath") var filePath: String = ""
)

@Entity(
    tableName = "occurrence_attachment_votes",
    indices = [Index("attachmentId")]
)
data class OccurrenceAttachmentVoteEntity(
    @PrimaryKey @get:PropertyName("id") @set:PropertyName("id") var id: Long = System.currentTimeMillis(),
    @get:PropertyName("attachmentId") @set:PropertyName("attachmentId") var attachmentId: Long = 0,
    @get:PropertyName("username") @set:PropertyName("username") var username: String = ""
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
    val attachments: List<AttachmentWithVotes>
)

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
