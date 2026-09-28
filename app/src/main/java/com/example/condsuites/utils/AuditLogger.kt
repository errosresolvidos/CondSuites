package com.example.condsuites.utils

import android.content.Context
import android.net.Uri
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.AuditLogEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AuditLogger {

    suspend fun log(
        dao: AppDao,
        user: UserEntity,
        action: String,
        category: String = "GERAL",
        details: String = ""
    ) {
        withContext(Dispatchers.IO) {
            try {
                val auditLog = AuditLogEntity(
                    timestamp = System.currentTimeMillis(),
                    formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()),
                    username = user.username.ifBlank { "admin" },
                    userRole = user.role.ifBlank { "ADMIN" },
                    action = action,
                    category = category,
                    details = details
                )
                dao.insertAuditLog(auditLog)
                FirestoreSyncManager.syncAuditLog(auditLog)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    data class LogStorageInfo(
        val auditLogCount: Int,
        val auditLogSizeBytes: Long,
        val notificationLogCount: Int,
        val notificationLogSizeBytes: Long,
        val occurrenceLogCount: Int,
        val occurrenceLogSizeBytes: Long,
        val totalSizeBytes: Long,
        val formattedTotalSize: String,
        val formattedAuditSize: String,
        val formattedNotificationSize: String,
        val formattedOccurrenceSize: String
    )

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format(Locale.getDefault(), "%.2f %cB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }

    suspend fun calculateLogStorageInfo(context: Context, dao: AppDao): LogStorageInfo {
        return withContext(Dispatchers.IO) {
            val auditLogs = try { dao.getAllAuditLogsList() } catch (_: Exception) { emptyList() }
            val notifLogs = try { dao.getNotificationLogsList() } catch (_: Exception) { emptyList() }
            val occLogs = try { dao.getAllOccurrenceLogsList() } catch (_: Exception) { emptyList() }

            var auditSize = 0L
            auditLogs.forEach {
                auditSize += (it.username.toByteArray(Charsets.UTF_8).size +
                        it.userRole.toByteArray(Charsets.UTF_8).size +
                        it.action.toByteArray(Charsets.UTF_8).size +
                        it.category.toByteArray(Charsets.UTF_8).size +
                        it.details.toByteArray(Charsets.UTF_8).size +
                        it.formattedDate.toByteArray(Charsets.UTF_8).size + 32)
            }

            var notifSize = 0L
            notifLogs.forEach {
                notifSize += (it.title.toByteArray(Charsets.UTF_8).size +
                        it.message.toByteArray(Charsets.UTF_8).size +
                        it.status.toByteArray(Charsets.UTF_8).size +
                        it.timestamp.toByteArray(Charsets.UTF_8).size + 24)
            }

            var occSize = 0L
            occLogs.forEach {
                occSize += (it.username.toByteArray(Charsets.UTF_8).size +
                        it.action.toByteArray(Charsets.UTF_8).size + 24)
            }

            val totalSize = auditSize + notifSize + occSize

            LogStorageInfo(
                auditLogCount = auditLogs.size,
                auditLogSizeBytes = auditSize,
                notificationLogCount = notifLogs.size,
                notificationLogSizeBytes = notifSize,
                occurrenceLogCount = occLogs.size,
                occurrenceLogSizeBytes = occSize,
                totalSizeBytes = totalSize,
                formattedTotalSize = formatBytes(totalSize),
                formattedAuditSize = formatBytes(auditSize),
                formattedNotificationSize = formatBytes(notifSize),
                formattedOccurrenceSize = formatBytes(occSize)
            )
        }
    }

    suspend fun exportAuditLogsToExcel(context: Context, dao: AppDao, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Logs de Auditoria")
                val header = sheet.createRow(0)
                val headers = listOf("ID", "Data e Hora", "Usuário", "Perfil", "Categoria", "Ação", "Detalhes")
                headers.forEachIndexed { index, title ->
                    header.createCell(index).setCellValue(title)
                }

                val logs = dao.getAllAuditLogsList()
                logs.forEachIndexed { rowIndex, log ->
                    val row = sheet.createRow(rowIndex + 1)
                    row.createCell(0).setCellValue(log.id.toDouble())
                    row.createCell(1).setCellValue(log.formattedDate)
                    row.createCell(2).setCellValue(log.username)
                    row.createCell(3).setCellValue(log.userRole)
                    row.createCell(4).setCellValue(log.category)
                    row.createCell(5).setCellValue(log.action)
                    row.createCell(6).setCellValue(log.details)
                }

                context.contentResolver.openOutputStream(uri)?.use { output ->
                    workbook.write(output)
                }
                workbook.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
