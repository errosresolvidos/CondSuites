package com.example.condsuites.service.media

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

object MediaUploadManager {
    private val client = OkHttpClient()

    private fun getBaseUrl(context: Context): String {
        return "https://api.condsuites.com/v1"
    }

    suspend fun saveMediaLocally(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val fileDir = File(context.filesDir, "media")
            if (!fileDir.exists()) fileDir.mkdirs()
            
            var ext = "jpg"
            val mimeType = context.contentResolver.getType(uri)
            if (mimeType != null) {
                when {
                    mimeType.contains("pdf") -> ext = "pdf"
                    mimeType.contains("png") -> ext = "png"
                    mimeType.contains("video") || mimeType.contains("mp4") -> ext = "mp4"
                }
            } else {
                val path = uri.path ?: ""
                val pathExt = path.substringAfterLast('.', "")
                if (pathExt.isNotBlank()) ext = pathExt
            }

            val fileName = "media_${System.currentTimeMillis()}.$ext"
            val localFile = File(fileDir, fileName)
            
            val outputStream = FileOutputStream(localFile)
            inputStream.copyTo(outputStream)
            
            inputStream.close()
            outputStream.close()
            
            localFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadMedia(context: Context, localPath: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(localPath)
            if (!file.exists()) return@withContext null

            val url = "${getBaseUrl(context)}/upload"
            
            val mimeType = when {
                file.name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                file.name.endsWith(".png", ignoreCase = true) -> "image/png"
                file.name.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
                else -> "image/jpeg"
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val orgJson = org.json.JSONObject(responseBody)
                return@withContext orgJson.optString("url", "").takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
}