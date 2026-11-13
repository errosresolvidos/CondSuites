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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

fun sanitizeLocalPath(path: String): String {
    var clean = path.trim()
    if (clean.startsWith("file://")) {
        clean = clean.removePrefix("file://")
    }
    if (clean.contains("%20")) {
        try {
            clean = java.net.URLDecoder.decode(clean, "UTF-8")
        } catch (_: Exception) {}
    }
    return clean
}

fun isPdfFile(fileName: String, filePath: String = ""): Boolean {
    val cleanName = fileName.lowercase()
    val cleanPath = filePath.substringBefore('?').lowercase()
    val nameExt = cleanName.substringAfterLast('.', "")
    val pathExt = cleanPath.substringAfterLast('.', "")
    
    return nameExt == "pdf" || 
           pathExt == "pdf" || 
           cleanName.endsWith(".pdf") || 
           cleanPath.endsWith(".pdf") ||
           cleanPath.contains("/pdf/") ||
           cleanPath.contains("application/pdf")
}

fun isImageFile(fileName: String, filePath: String = ""): Boolean {
    if (isPdfFile(fileName, filePath)) return false
    val nameExt = fileName.substringAfterLast('.', "").lowercase()
    val pathExt = filePath.substringBefore('?').substringAfterLast('.', "").lowercase()
    val imgExts = listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    return nameExt in imgExts || pathExt in imgExts || filePath.startsWith("data:image/") || (filePath.contains("/image/", ignoreCase = true) && !filePath.contains(".pdf", ignoreCase = true))
}

fun isVideoFile(fileName: String, filePath: String = ""): Boolean {
    val nameExt = fileName.substringAfterLast('.', "").lowercase()
    val pathExt = filePath.substringBefore('?').substringAfterLast('.', "").lowercase()
    val videoExts = listOf("mp4", "mkv", "webm", "avi", "3gp", "mov", "flv", "wmv", "m4v")
    return nameExt in videoExts || pathExt in videoExts || filePath.contains("/video/", ignoreCase = true) || (filePath.contains("video", ignoreCase = true) && filePath.contains("cloudinary.com", ignoreCase = true))
}

fun isExcelFile(fileName: String): Boolean {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return ext in listOf("xls", "xlsx", "csv")
}

fun isWordFile(fileName: String): Boolean {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return ext in listOf("doc", "docx", "odt")
}

fun isDocumentFile(fileName: String, filePath: String = ""): Boolean {
    return !isImageFile(fileName) && !isVideoFile(fileName, filePath)
}

fun executeCloudinaryDownloadToBytes(okHttpClient: OkHttpClient, originalUrl: String): ByteArray? {
    val urlsToTry = mutableListOf<String>()
    urlsToTry.add(originalUrl)

    if (originalUrl.contains("cloudinary.com", ignoreCase = true)) {
        if (originalUrl.contains("/raw/upload/")) {
            urlsToTry.add(originalUrl.replace("/raw/upload/", "/image/upload/"))
            urlsToTry.add(originalUrl.replace("/raw/upload/", "/auto/upload/"))
        }
        val noVersion = originalUrl.replace(Regex("/v\\d+/"), "/")
        if (noVersion != originalUrl) {
            urlsToTry.add(noVersion)
            if (noVersion.contains("/raw/upload/")) {
                urlsToTry.add(noVersion.replace("/raw/upload/", "/image/upload/"))
            }
        }
    }

    // First try clean GET requests without Authorization header (Cloudinary CDN is public)
    for (url in urlsToTry) {
        var response: okhttp3.Response? = null
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36")
                .get()
                .build()
            response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bytes = response.body?.bytes()
                response.close()
                if (bytes != null && bytes.isNotEmpty()) {
                    return bytes
                }
            } else {
                response.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try { response?.close() } catch (_: Exception) {}
        }
    }

    // Fallback try with Basic Auth if needed
    val apiKey = "267343724521561"
    val apiSecret = "hmCqGC81V8VncRrM1l0gabMN62M"
    val credential = okhttp3.Credentials.basic(apiKey, apiSecret)

    for (url in urlsToTry) {
        var response: okhttp3.Response? = null
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36")
                .header("Authorization", credential)
                .get()
                .build()
            response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bytes = response.body?.bytes()
                response.close()
                if (bytes != null && bytes.isNotEmpty()) {
                    return bytes
                }
            } else {
                response.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try { response?.close() } catch (_: Exception) {}
        }
    }

    return null
}

suspend fun downloadFile(
    context: Context,
    filePath: String,
    fileName: String,
    onProgress: ((Float) -> Unit)? = null
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            onProgress?.invoke(0.1f)
            val extension = fileName.substringAfterLast('.', "").lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: when (extension) {
                "pdf" -> "application/pdf"
                "doc", "docx" -> "application/msword"
                "xls", "xlsx" -> "application/vnd.ms-excel"
                else -> "*/*"
            }

            val bodyStream = when {
                filePath.startsWith("data:") -> {
                    try {
                        val pure = if (filePath.contains(",")) filePath.substringAfter(",") else filePath
                        val bytes = android.util.Base64.decode(pure, android.util.Base64.DEFAULT)
                        onProgress?.invoke(0.4f)
                        java.io.ByteArrayInputStream(bytes)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@withContext false
                    }
                }
                filePath.startsWith("http://") || filePath.startsWith("https://") -> {
                    onProgress?.invoke(0.2f)
                    val okHttpClient = OkHttpClient.Builder()
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build()
                    val bytes = executeCloudinaryDownloadToBytes(okHttpClient, filePath) ?: return@withContext false
                    onProgress?.invoke(0.6f)
                    java.io.ByteArrayInputStream(bytes)
                }
                else -> {
                    onProgress?.invoke(0.3f)
                    if (filePath.startsWith("content://")) {
                        context.contentResolver.openInputStream(Uri.parse(filePath)) ?: return@withContext false
                    } else {
                        val cleanPath = sanitizeLocalPath(filePath)
                        val file = File(cleanPath)
                        if (!file.exists()) return@withContext false
                        file.inputStream()
                    }
                }
            }

            val totalAvailable = try { bodyStream.available().toLong() } catch (_: Exception) { 0L }

            val copyStream: (java.io.InputStream, java.io.OutputStream) -> Unit = { input, output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead = 0L
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (totalAvailable > 0) {
                        val pct = 0.6f + (0.35f * (totalRead.toFloat() / totalAvailable.toFloat()))
                        onProgress?.invoke(pct.coerceIn(0f, 0.95f))
                    } else {
                        onProgress?.invoke(0.85f)
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CondSuites")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext false

                resolver.openOutputStream(uri)?.use { output ->
                    bodyStream.use { input ->
                        copyStream(input, output)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                onProgress?.invoke(1f)
                true
            } else {
                val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "CondSuites")
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                targetFile.outputStream().use { output ->
                    bodyStream.use { input ->
                        copyStream(input, output)
                    }
                }
                try {
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(targetFile))
                    context.sendBroadcast(mediaScanIntent)
                } catch (_: Exception) {}
                onProgress?.invoke(1f)
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



fun sha1Hex(input: String): String {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

fun showTechnicalLogDialog(context: Context, title: String, message: String) {
    try {
        AppErrorHandler.showError("$title\n\n$message")
        var activityContext = context
        while (activityContext is android.content.ContextWrapper && activityContext !is android.app.Activity) {
            activityContext = activityContext.baseContext
        }

        val formattedMsg = "Olá Samuel, ocorreu um erro ao abrir/baixar arquivo PDF no CondSuites:\n\n$title\n\n$message"

        val db = com.example.condsuites.data.db.AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val samuelUser = db.appDao().getUserByUsername("samuel")
            val rawPhone = samuelUser?.phone?.takeIf { it.isNotBlank() } ?: ""
            val cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
            val finalPhone = if (cleanPhone.length in 10..11 && !cleanPhone.startsWith("55")) "55$cleanPhone" else cleanPhone

            withContext(Dispatchers.Main) {
                android.app.AlertDialog.Builder(if (activityContext is android.app.Activity) activityContext else context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Enviar p/ Samuel (WhatsApp)") { dialog, _ ->
                        try {
                            val url = if (finalPhone.isNotBlank()) {
                                "https://api.whatsapp.com/send?phone=$finalPhone&text=${Uri.encode(formattedMsg)}"
                            } else {
                                "https://api.whatsapp.com/send?text=${Uri.encode(formattedMsg)}"
                            }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Não foi possível abrir o WhatsApp.", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    }
                    .setNeutralButton("Copiar Log") { dialog, _ ->
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Log Erro PDF", "$title\n\n$message")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Log copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Fechar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

fun fileUriToBase64(context: Context, uri: Uri): String? {
    return try {
        val mimeType = context.contentResolver.getType(uri) ?: when {
            uri.toString().endsWith(".pdf", true) -> "application/pdf"
            uri.toString().endsWith(".png", true) -> "image/png"
            else -> "image/jpeg"
        }
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()
        if (bytes.isEmpty()) return null

        val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        "data:$mimeType;base64,$base64String"
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun uploadFileToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val base64Data = fileUriToBase64(context, uri)
            withContext(Dispatchers.Main) {
                onComplete(base64Data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onComplete(null)
            }
        }
    }
}

fun uploadFileToFirebaseStorage(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    uploadFileToCloudinary(context, uri, onComplete)
}

fun uploadPdfAsImageToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    uploadFileToFirebaseStorage(context, uri, onComplete)
}

fun uploadImageToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    uploadFileToFirebaseStorage(context, uri, onComplete)
}

fun convertPdfToAllBitmaps(context: Context, uri: Uri): List<android.graphics.Bitmap> {
    val bitmaps = mutableListOf<android.graphics.Bitmap>()
    try {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return bitmaps
        val pdfRenderer = android.graphics.pdf.PdfRenderer(parcelFileDescriptor)
        val pageCount = pdfRenderer.pageCount
        for (i in 0 until pageCount) {
            val page = pdfRenderer.openPage(i)
            val width = page.width
            val height = page.height
            val bitmap = android.graphics.Bitmap.createBitmap(width * 2, height * 2, android.graphics.Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmaps.add(bitmap)
        }
        pdfRenderer.close()
        parcelFileDescriptor.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return bitmaps
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

        val cleanPath = sanitizeLocalPath(videoPath)
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

fun openFile(context: Context, filePath: String, fileName: String = "") {
    if (isVideoFile("", filePath)) {
        openVideoFile(context, filePath)
        return
    }

    val resolvedFileName = fileName.ifBlank { filePath.substringBefore('?').substringAfterLast('/').ifBlank { "documento.pdf" } }
    val extension = resolvedFileName.substringAfterLast('.', "").lowercase()
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: when (extension) {
        "pdf" -> "application/pdf"
        "doc", "docx" -> "application/msword"
        "xls", "xlsx" -> "application/vnd.ms-excel"
        "txt" -> "text/plain"
        else -> "*/*"
    }

    if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
        Toast.makeText(context, "Baixando arquivo para visualização...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val okHttpClient = OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
                val bytes = executeCloudinaryDownloadToBytes(okHttpClient, filePath)
                if (bytes == null || bytes.isEmpty()) {
                    val errorDetails = "Falha ao baixar arquivo da URL (401 Unauthorized / Erro de Rede): $filePath\n(Verifique se o arquivo existe e possui permissão pública no Cloudinary)."
                    android.util.Log.e("OPEN_FILE", errorDetails)
                    withContext(Dispatchers.Main) {
                        showTechnicalLogDialog(context, "Erro ao abrir arquivo PDF", errorDetails)
                    }
                    return@launch
                }
                val cacheDir = File(context.cacheDir, "documents")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val localFile = File(cacheDir, resolvedFileName)
                localFile.writeBytes(bytes)

                withContext(Dispatchers.Main) {
                    try {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", localFile)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val errorDetails = "Nenhum aplicativo encontrado para abrir este arquivo ou erro na Intent:\n${e.message}\n${e.stackTraceToString()}"
                        showTechnicalLogDialog(context, "Erro ao abrir arquivo PDF", errorDetails)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorDetails = "Exceção ao baixar arquivo: ${e.javaClass.simpleName} - ${e.message}\n${e.stackTraceToString()}"
                android.util.Log.e("OPEN_FILE", "Exception downloading file: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showTechnicalLogDialog(context, "Erro ao abrir arquivo PDF", errorDetails)
                }
            }
        }
        return
    }

    if (filePath.startsWith("content://")) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(filePath)).apply {
                setDataAndType(Uri.parse(filePath), mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
            val errorDetails = "Erro ao abrir URI de conteúdo ($filePath):\n${e.message}\n${e.stackTraceToString()}"
            showTechnicalLogDialog(context, "Erro ao abrir arquivo PDF", errorDetails)
            return
        }
    }

    val cleanPath = sanitizeLocalPath(filePath)
    val file = File(cleanPath)
    if (!file.exists()) {
        val errorDetails = "Arquivo local não encontrado no caminho: $cleanPath (Original: $filePath)"
        showTechnicalLogDialog(context, "Erro ao abrir arquivo PDF", errorDetails)
        return
    }
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        val errorDetails = "Não foi possível abrir o arquivo local (${file.name}):\n${e.message}\n${e.stackTraceToString()}"
        showTechnicalLogDialog(context, "Erro ao abrir arquivo PDF", errorDetails)
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

fun deleteCloudinaryFile(filePath: String) {
    if (filePath.isBlank() || !filePath.contains("cloudinary.com", ignoreCase = true)) return

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val regex = Regex("""cloudinary\.com/([^/]+)/(image|video|raw)/upload/(?:v\d+/)?(.+)""", RegexOption.IGNORE_CASE)
            val match = regex.find(filePath) ?: return@launch

            val cloudName = match.groupValues[1]
            val originalResourceType = match.groupValues[2].lowercase()
            val rawPublicId = match.groupValues[3]

            val apiKey = "267343724521561"
            val apiSecret = "hmCqGC81V8VncRrM1l0gabMN62M"
            val timestamp = (System.currentTimeMillis() / 1000).toString()

            val typesToTry = listOf(originalResourceType, "image", "raw", "video").distinct()

            for (resType in typesToTry) {
                val pid = if (resType == "raw") rawPublicId else rawPublicId.substringBeforeLast('.')
                val toSign = "public_id=$pid&timestamp=$timestamp$apiSecret"
                val signature = sha1Hex(toSign)

                val url = "https://api.cloudinary.com/v1_1/$cloudName/$resType/destroy"

                val formBody = okhttp3.FormBody.Builder()
                    .add("public_id", pid)
                    .add("timestamp", timestamp)
                    .add("api_key", apiKey)
                    .add("signature", signature)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                response.close()

                if (responseBody.contains("ok", ignoreCase = true)) {
                    android.util.Log.d("CLOUDINARY_DELETE", "Successfully destroyed Cloudinary asset: $pid (type=$resType)")
                    break
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CLOUDINARY_DELETE", "Error deleting Cloudinary file $filePath: ${e.message}", e)
        }
    }
}


