package com.example.condsuites.service.network

import android.content.Context
import com.example.condsuites.data.model.OccurrenceAttachmentEntity
import com.example.condsuites.data.model.OccurrenceAttachmentVoteEntity
import com.example.condsuites.data.model.OccurrenceEntity
import com.example.condsuites.data.model.OccurrenceMessageEntity
import com.example.condsuites.data.model.OccurrenceMessageReadEntity
import com.example.condsuites.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object OccurrenceApiClient {
    private val client = OkHttpClient()

    private fun getBaseUrl(context: Context): String {
        return "https://api.condsuites.com/v1" // Fallback, could be fetched from UserPreferences
    }

    suspend fun getOccurrence(context: Context, id: Long): OccurrenceEntity? = withContext(Dispatchers.IO) {
        try {
            val url = "${getBaseUrl(context)}/occurrences/$id"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext null
                val json = JSONObject(responseBody)
                return@withContext OccurrenceEntity(
                    id = json.optLong("id", id),
                    title = json.optString("title", ""),
                    apartment = json.optString("apartment", ""),
                    status = json.optString("status", "ABERTA"),
                    createdByUsername = json.optString("createdByUsername", ""),
                    date = json.optString("date", ""),
                    type = json.optString("type", "GERAL"),
                    occurrenceType = json.optString("occurrenceType", ""),
                    isUrgent = json.optBoolean("isUrgent", false)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun syncOccurrence(context: Context, entity: OccurrenceEntity, isDelete: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${getBaseUrl(context)}/occurrences/${entity.id}"
            val requestBuilder = Request.Builder().url(url)
            
            if (isDelete) {
                requestBuilder.delete()
            } else {
                val json = JSONObject().apply {
                    put("id", entity.id)
                    put("title", entity.title)
                    put("apartment", entity.apartment)
                    put("status", entity.status)
                    put("createdByUsername", entity.createdByUsername)
                    put("date", entity.date)
                    put("type", entity.type)
                    put("occurrenceType", entity.occurrenceType)
                    put("isUrgent", entity.isUrgent)
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                requestBuilder.put(body)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun syncOccurrenceMessage(context: Context, entity: OccurrenceMessageEntity, isDelete: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${getBaseUrl(context)}/occurrence_messages/${entity.id}"
            val requestBuilder = Request.Builder().url(url)
            
            if (isDelete) {
                requestBuilder.delete()
            } else {
                val json = JSONObject().apply {
                    put("id", entity.id)
                    put("occurrenceId", entity.occurrenceId)
                    put("senderUsername", entity.senderUsername)
                    put("text", entity.text)
                    put("date", entity.date)
                    put("isCouncilOnly", entity.isCouncilOnly)
                    put("isSindicoOnly", entity.isSindicoOnly)
                    put("isRead", entity.isRead)
                    put("isVotingClosed", entity.isVotingClosed)
                    put("isBudget", entity.isBudget)
                    put("isEdited", entity.isEdited)
                    put("timestamp", entity.timestamp)
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                requestBuilder.put(body)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun syncAttachment(context: Context, entity: OccurrenceAttachmentEntity, isDelete: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${getBaseUrl(context)}/occurrence_attachments/${entity.id}"
            val requestBuilder = Request.Builder().url(url)
            
            if (isDelete) {
                requestBuilder.delete()
            } else {
                val json = JSONObject().apply {
                    put("id", entity.id)
                    put("messageId", entity.messageId)
                    put("occurrenceId", entity.occurrenceId)
                    put("fileName", entity.fileName)
                    put("filePath", entity.filePath)
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                requestBuilder.put(body)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun syncAttachmentVote(context: Context, entity: OccurrenceAttachmentVoteEntity, isDelete: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${getBaseUrl(context)}/occurrence_attachment_votes/${entity.id}"
            val requestBuilder = Request.Builder().url(url)
            
            if (isDelete) {
                requestBuilder.delete()
            } else {
                val json = JSONObject().apply {
                    put("id", entity.id)
                    put("attachmentId", entity.attachmentId)
                    put("username", entity.username)
                    put("voteType", entity.voteType)
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                requestBuilder.put(body)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun syncOccurrenceMessageRead(context: Context, entity: OccurrenceMessageReadEntity, isDelete: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${getBaseUrl(context)}/occurrence_message_reads/${entity.id}"
            val requestBuilder = Request.Builder().url(url)
            
            if (isDelete) {
                requestBuilder.delete()
            } else {
                val json = JSONObject().apply {
                    put("id", entity.id)
                    put("messageId", entity.messageId)
                    put("username", entity.username)
                    put("timestamp", entity.timestamp)
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                requestBuilder.put(body)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}