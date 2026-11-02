package com.example.condsuites.service.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.condsuites.data.db.AppDatabase
import com.example.condsuites.data.model.SyncStatus
import com.example.condsuites.service.FirestoreSyncManager

class OccurrenceSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.appDao()

        var success = true

        try {
            // Sincronizar Ocorrências
            val pendingOccurrences = dao.getPendingOccurrences()
            for (occ in pendingOccurrences) {
                val isDelete = occ.syncStatus == SyncStatus.PENDING_DELETE.name
                val result = FirestoreSyncManager.syncOccurrenceAwait(occ, isDelete)
                if (result) {
                    if (isDelete) {
                        dao.deleteOccurrence(occ.id)
                    } else {
                        occ.syncStatus = SyncStatus.SYNCED.name
                        occ.lastSyncedTimestamp = System.currentTimeMillis()
                        dao.updateOccurrence(occ)
                    }
                } else {
                    success = false
                }
            }

            // Sincronizar Mensagens
            val pendingMessages = dao.getPendingMessages()
            for (msg in pendingMessages) {
                val isDelete = msg.syncStatus == SyncStatus.PENDING_DELETE.name
                val result = FirestoreSyncManager.syncOccurrenceMessageAwait(msg, isDelete)
                if (result) {
                    if (isDelete) {
                        dao.deleteOccurrenceMessage(msg.id)
                    } else {
                        msg.syncStatus = SyncStatus.SYNCED.name
                        msg.lastSyncedTimestamp = System.currentTimeMillis()
                        dao.updateOccurrenceMessage(msg)
                    }
                } else {
                    success = false
                }
            }

            // Sincronizar Anexos
            val pendingAttachments = dao.getPendingAttachments()
            for (att in pendingAttachments) {
                val isDelete = att.syncStatus == SyncStatus.PENDING_DELETE.name
                val result = FirestoreSyncManager.syncAttachmentAwait(att, isDelete)
                if (result) {
                    if (isDelete) {
                        // Assuming cascade delete or handle accordingly
                    } else {
                        att.syncStatus = SyncStatus.SYNCED.name
                        att.lastSyncedTimestamp = System.currentTimeMillis()
                        dao.updateOccurrenceAttachment(att)
                    }
                } else {
                    success = false
                }
            }

            // Sincronizar Votos
            val pendingVotes = dao.getPendingAttachmentVotes()
            for (vote in pendingVotes) {
                val isDelete = vote.syncStatus == SyncStatus.PENDING_DELETE.name
                val result = FirestoreSyncManager.syncAttachmentVoteAwait(vote, isDelete)
                if (result) {
                    if (isDelete) {
                        dao.deleteAttachmentVote(vote.attachmentId, vote.username)
                    } else {
                        vote.syncStatus = SyncStatus.SYNCED.name
                        vote.lastSyncedTimestamp = System.currentTimeMillis()
                        dao.updateOccurrenceAttachmentVote(vote)
                    }
                } else {
                    success = false
                }
            }

            // Sincronizar Message Reads
            val pendingReads = dao.getPendingMessageReads()
            for (read in pendingReads) {
                val isDelete = read.syncStatus == SyncStatus.PENDING_DELETE.name
                val result = FirestoreSyncManager.syncOccurrenceMessageReadAwait(read, isDelete)
                if (result) {
                    if (isDelete) {
                        dao.deleteOccurrenceMessageRead(read.messageId, read.username)
                    } else {
                        read.syncStatus = SyncStatus.SYNCED.name
                        read.lastSyncedTimestamp = System.currentTimeMillis()
                        dao.updateOccurrenceMessageRead(read)
                    }
                } else {
                    success = false
                }
            }

            return if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}