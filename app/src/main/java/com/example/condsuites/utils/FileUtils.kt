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
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

fun isImageFile(fileName: String): Boolean {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
}

fun isVideoFile(fileName: String, filePath: String = ""): Boolean {
    val nameExt = fileName.substringAfterLast('.', "").lowercase()
    val pathExt = filePath.substringBefore('?').substringAfterLast('.', "").lowercase()
    val videoExts = listOf("mp4", "mkv", "webm", "avi", "3gp", "mov", "flv", "wmv", "m4v")
    return nameExt in videoExts || pathExt in videoExts || filePath.contains("/video/upload/", ignoreCase = true)
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

fun uploadFileToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val cloudName = "dcondsuites"
            val uploadPreset = "condsuites"

            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                onComplete(copyFileToInternalStorage(context, uri))
                return@launch
            }

            val fileName = getFileName(context, uri)
            val bytes = inputStream.readBytes()
            inputStream.close()

            val boundary = "----CloudinaryBoundary" + System.currentTimeMillis()
            val url = URL("https://api.cloudinary.com/v1_1/$cloudName/auto/upload")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                doInput = true
                useCaches = false
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                connectTimeout = 30000
                readTimeout = 30000
            }

            val dos = java.io.DataOutputStream(conn.outputStream)

            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
            dos.writeBytes("$uploadPreset\r\n")

            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
            dos.writeBytes("Content-Type: application/octet-stream\r\n\r\n")
            dos.write(bytes)
            dos.writeBytes("\r\n")

            dos.writeBytes("--$boundary--\r\n")
            dos.flush()
            dos.close()

            val responseCode = conn.responseCode
            if (responseCode == 200 || responseCode == 201) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val secureUrl = extractJsonValue(responseText, "secure_url") ?: extractJsonValue(responseText, "url")
                if (!secureUrl.isNullOrBlank()) {
                    android.util.Log.d("CLOUDINARY_UPLOAD", "Uploaded successfully to Cloudinary: $secureUrl")
                    onComplete(secureUrl)
                    return@launch
                }
            } else {
                val errText = try { conn.errorStream?.bufferedReader()?.use { it.readText() } } catch (_: Exception) { "" }
                android.util.Log.e("CLOUDINARY_UPLOAD", "Cloudinary upload HTTP $responseCode: $errText")
            }

            val base64 = uriToBase64(context, uri)
            onComplete(base64 ?: copyFileToInternalStorage(context, uri))
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("CLOUDINARY_UPLOAD", "Error in Cloudinary upload: ${e.message}", e)
            val base64 = uriToBase64(context, uri)
            onComplete(base64 ?: copyFileToInternalStorage(context, uri))
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

        val file = File(videoPath)
        if (!file.exists()) {
            Toast.makeText(context, "Arquivo de vídeo não encontrado", Toast.LENGTH_SHORT).show()
            return
        }

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

    val file = File(filePath)
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
