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
import okhttp3.RequestBody.Companion.toRequestBody
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

fun isPdfFile(fileName: String): Boolean {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return ext == "pdf"
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
    val urlsToTry = mutableListOf(originalUrl)
    if (originalUrl.contains("cloudinary.com", ignoreCase = true)) {
        if (originalUrl.contains("/raw/upload/")) {
            urlsToTry.add(originalUrl.replace("/raw/upload/", "/auto/upload/"))
            urlsToTry.add(originalUrl.replace("/raw/upload/", "/raw/public/"))
        }
        val noVersion = originalUrl.replace(Regex("/v\\d+/"), "/")
        if (noVersion != originalUrl) {
            urlsToTry.add(noVersion)
            if (noVersion.contains("/raw/upload/")) {
                urlsToTry.add(noVersion.replace("/raw/upload/", "/auto/upload/"))
            }
        }
    }

    val apiKey = "267343724521561"
    val apiSecret = "hmCqGC81V8VncRrM1l0gabMN62M"
    val credential = okhttp3.Credentials.basic(apiKey, apiSecret)

    for (url in urlsToTry) {
        var response: okhttp3.Response? = null
        try {
            val requestBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36")
            
            if (url.contains("cloudinary.com", ignoreCase = true)) {
                requestBuilder.header("Authorization", credential)
            }
            
            val request = requestBuilder.get().build()
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

    var fallbackResponse: okhttp3.Response? = null
    try {
        val request = Request.Builder()
            .url(originalUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36")
            .get()
            .build()
        fallbackResponse = okHttpClient.newCall(request).execute()
        if (fallbackResponse.isSuccessful) {
            val bytes = fallbackResponse.body?.bytes()
            fallbackResponse.close()
            if (bytes != null && bytes.isNotEmpty()) return bytes
        } else {
            fallbackResponse.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        try { fallbackResponse?.close() } catch (_: Exception) {}
    }

    return null
}

suspend fun downloadFile(context: Context, filePath: String, fileName: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: when (extension) {
                "pdf" -> "application/pdf"
                "doc", "docx" -> "application/msword"
                "xls", "xlsx" -> "application/vnd.ms-excel"
                else -> "*/*"
            }

            val bodyStream = if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                val okHttpClient = OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
                val bytes = executeCloudinaryDownloadToBytes(okHttpClient, filePath) ?: return@withContext false
                java.io.ByteArrayInputStream(bytes)
            } else {
                if (filePath.startsWith("content://")) {
                    context.contentResolver.openInputStream(Uri.parse(filePath)) ?: return@withContext false
                } else {
                    val cleanPath = if (filePath.startsWith("file://")) filePath.removePrefix("file://") else filePath
                    val file = File(cleanPath)
                    if (!file.exists()) return@withContext false
                    file.inputStream()
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
                        input.copyTo(output)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                true
            } else {
                val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "CondSuites")
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                targetFile.outputStream().use { output ->
                    bodyStream.use { input ->
                        input.copyTo(output)
                    }
                }
                try {
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(targetFile))
                    context.sendBroadcast(mediaScanIntent)
                } catch (_: Exception) {}
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
        val localPath = com.example.condsuites.service.media.MediaUploadManager.saveMediaLocally(context, uri)
        if (localPath != null) {
            withContext(Dispatchers.Main) {
                onComplete(localPath) // Returns local path for immediate display
            }
        } else {
            withContext(Dispatchers.Main) {
                onComplete(null)
            }
        }
    }
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

fun uploadPdfAsImageToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    uploadFileToCloudinary(context, uri, onComplete)
}

fun uploadImageToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    uploadPdfAsImageToCloudinary(context, uri, onComplete)
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

    val cleanPath = if (filePath.startsWith("file://")) filePath.removePrefix("file://") else filePath
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
