package com.example.condsuites.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

fun isImageFile(fileName: String): Boolean {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
}

fun isVideoFile(fileName: String, filePath: String = ""): Boolean {
    val nameExt = fileName.substringAfterLast('.', "").lowercase()
    val pathExt = filePath.substringBefore('?').substringAfterLast('.', "").lowercase()
    val videoExts = listOf("mp4", "mkv", "webm", "avi", "3gp", "mov", "flv", "wmv", "m4v")
    return nameExt in videoExts || pathExt in videoExts || filePath.contains("/video/", ignoreCase = true) || (filePath.contains("video", ignoreCase = true) && filePath.contains("cloudinary.com", ignoreCase = true))
}

@RequiresApi(Build.VERSION_CODES.Q)
suspend fun downloadFile(context: Context, filePath: String, fileName: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val extension = fileName.substringAfterLast('.', "")
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/CondSuites")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext false

            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                val url = URL(filePath)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                conn.connect()

                resolver.openOutputStream(uri)?.use { output ->
                    conn.inputStream.use { input ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                val file = File(filePath)
                if (!file.exists()) return@withContext false
                resolver.openOutputStream(uri)?.use { output ->
                    file.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    try {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "Documento"
}

fun copyFileToInternalStorage(context: Context, uri: Uri): String? {
    val fileName = getFileName(context, uri)
    val storageDir = File(context.filesDir, "attachments")
    if (!storageDir.exists()) storageDir.mkdirs()
    
    val targetFile = File(storageDir, "${System.currentTimeMillis()}_$fileName")
    try {
        try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {}

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        return targetFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun extractJsonValue(json: String, key: String): String? {
    val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
    val match = pattern.find(json)
    return match?.groupValues?.get(1)?.replace("\\/", "/")
}

fun getStreamLength(context: Context, uri: Uri): Long {
    try {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { pfd ->
            val len = pfd.length
            if (len > 0) return len
        }
    } catch (_: Exception) {}
    try {
        if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (file.exists()) return file.length()
        }
    } catch (_: Exception) {}
    return -1L
}

fun sha1Hex(input: String): String {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

fun showTechnicalLogDialog(context: Context, title: String, message: String) {
    try {
        var activityContext = context
        while (activityContext is android.content.ContextWrapper && activityContext !is android.app.Activity) {
            activityContext = activityContext.baseContext
        }

        android.app.AlertDialog.Builder(if (activityContext is android.app.Activity) activityContext else context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Copiar Log") { dialog, _ ->
                try {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Log Tecnico Cloudinary", message)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Log técnico copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Fechar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

fun uploadFileToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        var resultUrl: String? = null
        val cloudName = "ngpjqgbv"
        val apiKey = "267343724521561"
        val apiSecret = "hmCqGC81V8VncRrM1l0gabMN62M"
        val uploadPreset = "condsuites"

        val fileName = getFileName(context, uri)
        val mimeType = try { context.contentResolver.getType(uri) } catch (_: Exception) { null } ?: "application/octet-stream"
        val isVid = mimeType.startsWith("video/") || isVideoFile(fileName, uri.toString())
        val isImg = mimeType.startsWith("image/") || isImageFile(fileName)

        val fileSize = getStreamLength(context, uri)
        val fileSizeMb = if (fileSize > 0) String.format(java.util.Locale.getDefault(), "%.2f MB", fileSize / (1024.0 * 1024.0)) else "Desconhecido"
        val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

        val logBuilder = StringBuilder().apply {
            append("📋 LOG TÉCNICO DE UPLOAD CLOUDINARY\n\n")
            append("📁 Arquivo: $fileName\n")
            append("📄 Tipo MIME: $mimeType\n")
            append("💾 Tamanho: $fileSizeMb ($fileSize bytes)\n")
            append("☁️ Nuvem: $cloudName (API Key: $apiKey)\n")
            append("⏰ Horário: $dateStr\n\n")
            append("----------------------------------------\n")
            append("TENTATIVAS DE ENVIO:\n")
        }

        val typeEndpoint = if (isVid) "video/upload" else if (isImg) "image/upload" else "raw/upload"

        data class UploadAttempt(
            val endpoint: String,
            val isSigned: Boolean,
            val includeFolder: Boolean,
            val includePreset: Boolean
        )

        val uploadAttempts = listOf(
            UploadAttempt("https://api.cloudinary.com/v1_1/$cloudName/$typeEndpoint", isSigned = true, includeFolder = true, includePreset = false),
            UploadAttempt("https://api.cloudinary.com/v1_1/$cloudName/auto/upload", isSigned = true, includeFolder = true, includePreset = false),
            UploadAttempt("https://api.cloudinary.com/v1_1/$cloudName/$typeEndpoint", isSigned = true, includeFolder = false, includePreset = false),
            UploadAttempt("https://api.cloudinary.com/v1_1/$cloudName/auto/upload", isSigned = true, includeFolder = false, includePreset = false),
            UploadAttempt("https://api.cloudinary.com/v1_1/$cloudName/auto/upload", isSigned = false, includeFolder = true, includePreset = true),
            UploadAttempt("https://api.cloudinary.com/v1_1/$cloudName/auto/upload", isSigned = false, includeFolder = false, includePreset = true)
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()

        var attemptCount = 0
        for (attempt in uploadAttempts) {
            attemptCount++
            logBuilder.append("\n[Tentativa $attemptCount]\n")
            logBuilder.append("URL: ${attempt.endpoint}\n")
            logBuilder.append("Modo: ${if (attempt.isSigned) "Assinado (API Key)" else "Não assinado (Preset)"}\n")
            logBuilder.append("Pasta Assets: ${if (attempt.includeFolder) "Sim" else "Não"}\n")

            try {
                val checkStream = context.contentResolver.openInputStream(uri)
                if (checkStream == null) {
                    logBuilder.append("Status: Erro - openInputStream retornou NULL\n")
                    continue
                }
                checkStream.close()

                val timestamp = (System.currentTimeMillis() / 1000).toString()

                val streamRequestBody = object : RequestBody() {
                    override fun contentType() = mimeType.toMediaTypeOrNull()
                    override fun contentLength() = if (fileSize > 0) fileSize else -1L

                    override fun writeTo(sink: BufferedSink) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val buffer = ByteArray(32768)
                            var bytesRead: Int
                            while (stream.read(buffer).also { bytesRead = it } != -1) {
                                sink.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                }

                val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

                if (attempt.isSigned) {
                    multipartBuilder.addFormDataPart("api_key", apiKey)
                    multipartBuilder.addFormDataPart("timestamp", timestamp)

                    if (attempt.includeFolder) {
                        multipartBuilder.addFormDataPart("folder", "assets")
                        val stringToSign = "folder=assets&timestamp=$timestamp$apiSecret"
                        val signature = sha1Hex(stringToSign)
                        multipartBuilder.addFormDataPart("signature", signature)
                    } else {
                        val stringToSign = "timestamp=$timestamp$apiSecret"
                        val signature = sha1Hex(stringToSign)
                        multipartBuilder.addFormDataPart("signature", signature)
                    }
                }

                if (attempt.includePreset) {
                    multipartBuilder.addFormDataPart("upload_preset", uploadPreset)
                    if (attempt.includeFolder) {
                        multipartBuilder.addFormDataPart("folder", "assets")
                    }
                }

                multipartBuilder.addFormDataPart("file", fileName, streamRequestBody)

                val request = Request.Builder()
                    .url(attempt.endpoint)
                    .post(multipartBuilder.build())
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseText = response.body?.string() ?: ""

                logBuilder.append("HTTP Código: ${response.code}\n")
                if (responseText.isNotBlank()) {
                    val trimmed = if (responseText.length > 300) responseText.take(300) + "..." else responseText
                    logBuilder.append("Resposta API: $trimmed\n")
                }

                if (response.isSuccessful) {
                    try {
                        val json = org.json.JSONObject(responseText)
                        val secureUrl = json.optString("secure_url", "").ifBlank { json.optString("url", "") }
                        if (secureUrl.isNotBlank()) {
                            android.util.Log.d("CLOUDINARY_UPLOAD", "Successfully uploaded to Cloudinary (${attempt.endpoint}): $secureUrl")
                            resultUrl = secureUrl
                            break
                        }
                    } catch (_: Exception) {
                        val secureUrl = extractJsonValue(responseText, "secure_url") ?: extractJsonValue(responseText, "url")
                        if (!secureUrl.isNullOrBlank()) {
                            resultUrl = secureUrl
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logBuilder.append("Exceção: ${e.javaClass.simpleName} - ${e.message}\n")
                android.util.Log.e("CLOUDINARY_UPLOAD", "Exception uploading to ${attempt.endpoint}: ${e.message}", e)
            }
        }

        if (resultUrl != null) {
            onComplete(resultUrl)
        } else {
            logBuilder.append("\n----------------------------------------\n")
            logBuilder.append("RESULTADO: Todas as $attemptCount tentativas falharam.")

            val fullLog = logBuilder.toString()
            android.util.Log.e("CLOUDINARY_UPLOAD", fullLog)

            withContext(Dispatchers.Main) {
                showTechnicalLogDialog(context, "Log Técnico - Falha no Envio", fullLog)
            }
            onComplete(null)
        }
    }
}

fun uploadImageToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    uploadFileToCloudinary(context, uri, onComplete)
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (bitmap == null) return null

        val maxWidth = 800
        val maxHeight = 800
        val width = bitmap.width
        val height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()
        val finalWidth: Int
        val finalHeight: Int
        if (width > height) {
            finalWidth = maxWidth
            finalHeight = (maxWidth / ratio).toInt()
        } else {
            finalHeight = maxHeight
            finalWidth = (maxHeight * ratio).toInt()
        }
        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
        
        val outputStream = java.io.ByteArrayOutputStream()
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
        val bytes = outputStream.toByteArray()
        val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64String"
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun openVideoFile(context: Context, videoPath: String) {
    try {
        if (videoPath.startsWith("http://") || videoPath.startsWith("https://")) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoPath)).apply {
                    setDataAndType(Uri.parse(videoPath), "video/*")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoPath)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            return
        }

        if (videoPath.startsWith("content://")) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoPath)).apply {
                    setDataAndType(Uri.parse(videoPath), "video/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoPath)).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val cleanPath = if (videoPath.startsWith("file://")) videoPath.removePrefix("file://") else videoPath
        val file = File(cleanPath)
        if (file.exists()) {
            val uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            } catch (_: Exception) {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        try {
            val parsedUri = Uri.parse(videoPath)
            if (parsedUri.scheme != null) {
                val intent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
                    setDataAndType(parsedUri, "video/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
        } catch (_: Exception) {}

        Toast.makeText(context, "Arquivo de vídeo não encontrado", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Não foi possível abrir o vídeo", Toast.LENGTH_SHORT).show()
    }
}

fun openFile(context: Context, filePath: String) {
    if (isVideoFile("", filePath)) {
        openVideoFile(context, filePath)
        return
    }

    if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(filePath)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Não foi possível abrir o link do arquivo", Toast.LENGTH_SHORT).show()
        }
        return
    }

    if (filePath.startsWith("content://")) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(filePath)).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val cleanPath = if (filePath.startsWith("file://")) filePath.removePrefix("file://") else filePath
    val file = File(cleanPath)
    if (!file.exists()) {
        Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val type = context.contentResolver.getType(uri) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, type)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Não foi possível abrir o arquivo", Toast.LENGTH_SHORT).show()
    }
}

fun downloadContractFile(context: Context, filePath: String, fileName: String) {
    if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            CoroutineScope(Dispatchers.Main).launch {
                val success = downloadFile(context, filePath, fileName)
                if (success) {
                    Toast.makeText(context, "Documento baixado com sucesso para Downloads/CondSuites!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Erro ao baixar documento", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(filePath)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
        return
    }

    val file = File(filePath)
    if (!file.exists()) {
        Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        CoroutineScope(Dispatchers.Main).launch {
            val success = downloadFile(context, filePath, fileName)
            if (success) {
                Toast.makeText(context, "Documento baixado com sucesso para Downloads/CondSuites!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Erro ao baixar documento", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val destFile = File(downloadsDir, fileName)
            file.copyTo(destFile, overwrite = true)
            Toast.makeText(context, "Documento baixado na pasta Downloads!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Não foi possível baixar o arquivo", Toast.LENGTH_SHORT).show()
        }
    }
}

fun printHtmlReport(context: Context, htmlContent: String, jobName: String = "Relatorio_Manutencao_Elevadores") {
    try {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager != null) {
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
                }
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
