package com.example.condsuites.ui.components

import com.example.condsuites.data.model.OccurrenceMessageReadEntity

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.widget.VideoView
import kotlinx.coroutines.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.condsuites.data.model.AttachmentWithVotes
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.utils.CurrencyVisualTransformation
import com.example.condsuites.utils.downloadFile
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.getFileName
import com.example.condsuites.utils.isImageFile
import com.example.condsuites.utils.isPdfFile
import com.example.condsuites.utils.isVideoFile
import com.example.condsuites.utils.openFile
import com.example.condsuites.utils.openVideoFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun StorageSourceBadge(
    filePath: String,
    modifier: Modifier = Modifier
) {
    val isLocal = filePath.startsWith("/") || filePath.startsWith("file://") || filePath.startsWith("content://") || !filePath.startsWith("http")
    if (isLocal) return

    val isCloudinary = filePath.contains("cloudinary.com", ignoreCase = true)
    val isFirebase = filePath.contains("firebasestorage", ignoreCase = true) || filePath.contains("firebase", ignoreCase = true)

    val sourceName = when {
        isFirebase -> "Firebase"
        isCloudinary -> "Cloudinary (Legado)"
        else -> "Servidor"
    }

    val badgeBg = Color(0xFF3448C5)
    val icon = Icons.Default.CloudUpload

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = badgeBg.copy(alpha = 0.15f),
        border = BorderStroke(0.5.dp, badgeBg.copy(alpha = 0.5f)),
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = sourceName,
                tint = badgeBg,
                modifier = Modifier.size(11.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = sourceName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = badgeBg
            )
        }
    }
}

@Composable
fun VideoThumbnailImage(
    filePath: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var bitmap by remember(filePath) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(filePath) { mutableStateOf(true) }

    val isCloudinaryVideo = filePath.contains("cloudinary.com") && (filePath.contains("/video/") || isVideoFile("", filePath))
    val posterUrl = if (isCloudinaryVideo) {
        filePath.replace(Regex("/(image|auto)/upload/"), "/video/upload/")
            .replace(Regex("\\.(mp4|mov|mkv|webm|avi|3gp|flv|wmv|m4v)($|\\?)", RegexOption.IGNORE_CASE), ".jpg$2")
    } else null

    LaunchedEffect(filePath) {
        if (!isCloudinaryVideo) {
            withContext(Dispatchers.IO) {
                var retriever: MediaMetadataRetriever? = null
                try {
                    retriever = MediaMetadataRetriever()
                    when {
                        filePath.startsWith("http://") || filePath.startsWith("https://") -> {
                            retriever.setDataSource(filePath, HashMap<String, String>())
                        }
                        filePath.startsWith("content://") -> {
                            retriever.setDataSource(context, Uri.parse(filePath))
                        }
                        else -> {
                            val file = File(filePath)
                            if (file.exists()) {
                                retriever.setDataSource(file.absolutePath)
                            }
                        }
                    }
                    val frame = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: retriever.frameAtTime
                    bitmap = frame
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try { retriever?.release() } catch (_: Exception) {}
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = "Miniatura do vídeo",
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Miniatura do vídeo",
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Vídeo",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.55f),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f)),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Reproduzir vídeo",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun FullScreenVideoDialog(
    videoPath: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(
        onDismissRequest = {
            try { videoViewRef?.stopPlayback() } catch (_: Exception) {}
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (!hasError) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            videoViewRef = this
                            try {
                                if (videoPath.startsWith("http://") || videoPath.startsWith("https://") || videoPath.startsWith("content://")) {
                                    setVideoURI(Uri.parse(videoPath))
                                } else {
                                    val file = File(videoPath)
                                    if (file.exists()) {
                                        setVideoPath(file.absolutePath)
                                    } else {
                                        setVideoURI(Uri.parse(videoPath))
                                    }
                                }
                                setOnPreparedListener { mp ->
                                    isPrepared = true
                                    mp.isLooping = true
                                    start()
                                    isPlaying = true
                                }
                                setOnErrorListener { _, _, _ ->
                                    hasError = true
                                    openVideoFile(ctx, videoPath)
                                    onDismiss()
                                    true
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                hasError = true
                                openVideoFile(ctx, videoPath)
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clickable {
                            videoViewRef?.let { vv ->
                                try {
                                    if (vv.isPlaying) {
                                        vv.pause()
                                        isPlaying = false
                                    } else {
                                        vv.start()
                                        isPlaying = true
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                )
            }

            if (!isPrepared && !hasError) {
                CircularProgressIndicator(color = Color.White)
            }

            if (isPrepared && !isPlaying && !hasError) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(64.dp)
                        .clickable {
                            videoViewRef?.let { vv ->
                                try {
                                    vv.start()
                                    isPlaying = true
                                } catch (_: Exception) {}
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reproduzir",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    try { videoViewRef?.stopPlayback() } catch (_: Exception) {}
                    openVideoFile(context, videoPath)
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Abrir em aplicativo externo",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Vídeo ",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    StorageSourceBadge(filePath = videoPath)
                }

                IconButton(onClick = {
                    try { videoViewRef?.stopPlayback() } catch (_: Exception) {}
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProfessionalBudgetBadgeIcon(
    modifier: Modifier = Modifier,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    size: Dp = 36.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(10.dp),
        color = badgeColor,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "Orçamento",
                tint = iconColor,
                modifier = Modifier.size(size * 0.58f)
            )
        }
    }
}

@Composable
fun ProfessionalBudgetActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "Orçamento",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Orçamento",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun BudgetOptionChoiceDialog(
    onDismiss: () -> Unit,
    onSelectSingle: () -> Unit,
    onSelectMultiple: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfessionalBudgetBadgeIcon(size = 32.dp)
                Spacer(Modifier.width(10.dp))
                Text("Escolha o tipo de Orçamento")
            }
        },
        text = {
            Text("Deseja enviar apenas um orçamento ou vários para aprovação do conselho?")
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onSelectSingle()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apenas um")
                }
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onSelectMultiple()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vários")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BudgetReplyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<Uri>) -> Unit
) {
    var note by remember { mutableStateOf("") }
    val selectedUris = remember { mutableStateListOf<Uri>() }
    val context = LocalContext.current
    
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        selectedUris.addAll(uris)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfessionalBudgetBadgeIcon(size = 32.dp)
                Spacer(Modifier.width(10.dp))
                Text("Enviar Orçamentos")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Esta mensagem e anexos serão visíveis apenas para o Síndico e o Conselho Fiscal.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Text("Anexe os documentos dos orçamentos.", style = MaterialTheme.typography.bodySmall)
                
                OutlinedButton(
                    onClick = { fileLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Anexar Orçamentos")
                }
                
                if (selectedUris.isNotEmpty()) {
                    Column(Modifier.fillMaxWidth().heightIn(max = 120.dp).verticalScroll(rememberScrollState())) {
                        selectedUris.forEach { uri ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Text(getFileName(context, uri), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                IconButton(onClick = { selectedUris.removeIf { it == uri } }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Mensagem/Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(note, selectedUris.toList())
            }) {
                Text("Enviar ao Conselho")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}



data class BudgetDetails(
    val name: String,
    val total: Double,
    val installments: Int,
    val startDate: String,
    val duration: String
)

fun extractBudgetDetails(text: String, defaultName: String): BudgetDetails {
    var name = defaultName
    var total = 0.0
    var installments = 1
    var startDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    var duration = ""
    try {
        text.lines().forEach { line ->
            when {
                line.contains("Orçamento:", ignoreCase = true) || line.contains("Prestador/Fornecedor:", ignoreCase = true) || line.contains("Fornecedor:", ignoreCase = true) -> {
                    val extracted = line.substringAfter(":").trim()
                    if (extracted.isNotBlank()) name = extracted
                }
                line.contains("Valor Total:", ignoreCase = true) -> {
                    val clean = line.replace("R$", "").replace(".", "").replace(",", ".").filter { it.isDigit() || it == '.' }
                    total = clean.toDoubleOrNull() ?: 0.0
                }
                line.contains("Parcelas:", ignoreCase = true) -> {
                    val countStr = line.substringBefore("x").filter { it.isDigit() }
                    installments = countStr.toIntOrNull() ?: 1
                }
                line.contains("Início:", ignoreCase = true) -> {
                    val extracted = line.substringAfter(":").trim()
                    if (extracted.isNotBlank()) startDate = extracted
                }
                line.contains("Duração:", ignoreCase = true) -> {
                    val extracted = line.substringAfter(":").trim()
                    if (extracted.isNotBlank()) duration = extracted
                }
            }
        }
    } catch (_: Exception) {}
    return BudgetDetails(name, total, installments, startDate, duration)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetVotingCard(
    text: String,
    author: String,
    date: String,
    isFromMe: Boolean,
    currentUser: UserEntity,
    role: String = "",
    isConfidential: Boolean = false,
    isSindicoOnly: Boolean = false,
    isRead: Boolean = false,
    isVotingClosed: Boolean = false,
    isEdited: Boolean = false,
    attachments: List<AttachmentWithVotes> = emptyList(),
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onToggleVote: (Long, String) -> Unit,
    onCloseVoting: (String?, Double?, Int?, Double?, String?, String?) -> Unit = { _, _, _, _, _, _ -> },
    onOpenImage: (String) -> Unit,
    userRoleMap: Map<String, String> = emptyMap(),
    reads: List<OccurrenceMessageReadEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fullScreenVideoPath by remember { mutableStateOf<String?>(null) }
    var viewingDocument by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showAuthorizeDialog by remember { mutableStateOf(false) }

    val totalVotes = attachments.sumOf { it.votes.size }
    val canVote = (currentUser.role == "Conselheiro Fiscal" || currentUser.role == "Síndico" || currentUser.role == "ADMIN") && !isVotingClosed

    val allVotes = attachments.flatMap { it.votes }
    val acceptVotes = allVotes.filter { it.voteType == "ACCEPT" }

    val councilAcceptVoters = acceptVotes.map { it.username }.distinct().filter { username ->
        val vRole = if (username.equals("admin", ignoreCase = true)) "ADMIN" else userRoleMap[username.lowercase()] ?: ""
        vRole == "Conselheiro Fiscal"
    }
    val councilAcceptCount = councilAcceptVoters.size
    val totalCouncil = userRoleMap.values.count { it == "Conselheiro Fiscal" }
    val canSindicoAuthorize = councilAcceptCount >= 2 || (totalCouncil > 0 && councilAcceptCount >= totalCouncil)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth(0.92f)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
    ) {
        Column {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            ProfessionalBudgetBadgeIcon(size = 36.dp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Aprovação de Orçamento",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                val senderText = if (role.isNotBlank() && author.uppercase() != "SISTEMA") {
                                    "${author.uppercase()} • $role"
                                } else {
                                    author.uppercase()
                                }
                                Text(
                                    text = "Enviado por $senderText • $date${if (isEdited) " (editada)" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isConfidential || isSindicoOnly) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Confidencial",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            if (isFromMe) {
                                var showReadersDialog by remember { mutableStateOf(false) }
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Visualizadores de lida",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { showReadersDialog = true },
                                    tint = if (isRead) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                if (showReadersDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showReadersDialog = false },
                                        title = { Text("Visualizadores da Mensagem") },
                                        text = {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (reads.isEmpty()) {
                                                    Text("Ainda não visualizada por outros usuários.", style = MaterialTheme.typography.bodyMedium)
                                                } else {
                                                    reads.forEach { read ->
                                                        val timeStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(read.timestamp))
                                                        Text("• ${read.username} às $timeStr", style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = { showReadersDialog = false }) {
                                                Text("Fechar")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (text.isNotEmpty() && text != "Mensagem sigilosa") {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (text == "Mensagem sigilosa") {
                    Text(
                        text = "Mensagem sigilosa",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = Color.Gray
                    )
                } else if (attachments.isEmpty()) {
                    Text(
                        text = "Nenhum orçamento anexado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                } else {
                    if (attachments.size > 1) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Comparativo com ${attachments.size} documentos de orçamento",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    val maxAcceptVotes = attachments.maxOfOrNull { a -> a.votes.count { it.voteType == "ACCEPT" } } ?: 0
                    val isEntireCardRejected = text.contains("Rejeitado", ignoreCase = true)
                    val chosenName = extractBudgetDetails(text, "").name.lowercase().trim()

                    val chosenAttachmentId = if (isEntireCardRejected) {
                        null
                    } else if (attachments.size == 1) {
                        attachments.firstOrNull()?.attachment?.id
                    } else {
                        val match = attachments.firstOrNull { a ->
                            val cleanAtt = a.attachment.fileName.lowercase().trim()
                            cleanAtt.isNotBlank() && chosenName.isNotBlank() && (
                                cleanAtt == chosenName || 
                                cleanAtt.contains(chosenName) || 
                                chosenName.contains(cleanAtt.substringBeforeLast("."))
                            )
                        }
                        match?.attachment?.id ?: attachments.maxByOrNull { a -> a.votes.count { it.voteType == "ACCEPT" } }?.attachment?.id
                    }

                    attachments.forEachIndexed { index, attWithVotes ->
                        val att = attWithVotes.attachment
                        val votesCount = attWithVotes.votes.size
                        val acceptVotesCount = attWithVotes.votes.count { it.voteType == "ACCEPT" }
                        val voteFraction = if (totalVotes > 0) votesCount.toFloat() / totalVotes else 0f
                        val votePercent = (voteFraction * 100).toInt()
                        val hasUserAccepted = attWithVotes.votes.any { it.username == currentUser.username && it.voteType == "ACCEPT" }
                        val hasUserRejected = attWithVotes.votes.any { it.username == currentUser.username && it.voteType == "REJECT" }
                        val isLeading = acceptVotesCount > 0 && acceptVotesCount == maxAcceptVotes && attachments.size > 1
                        val isThisChosen = isVotingClosed && (att.id == chosenAttachmentId) && !isEntireCardRejected
                        val isImg = isImageFile(att.fileName) || isPdfFile(att.fileName)
                        val isVid = isVideoFile(att.fileName, att.filePath)
                        val ext = att.fileName.substringAfterLast('.', "").uppercase()

                        Surface(
                            color = when {
                                isThisChosen -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                isVotingClosed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                hasUserAccepted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (hasUserAccepted || isLeading || isThisChosen) 1.5.dp else 0.5.dp,
                                color = when {
                                    isThisChosen -> MaterialTheme.colorScheme.primary
                                    isVotingClosed -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                                    hasUserAccepted -> MaterialTheme.colorScheme.primary
                                    isLeading -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.InsertDriveFile,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (attachments.size > 1) "OPÇÃO ${index + 1}" else "DOCUMENTO",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (isLeading && !isVotingClosed) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(11.dp),
                                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                                    )
                                                    Spacer(Modifier.width(3.dp))
                                                    Text(
                                                        text = "Mais Votado",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                        if (hasUserAccepted && !isVotingClosed) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "✓ Votado",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                if (att.filePath.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = when {
                                                ext == "PDF" -> Color(0xFFFFF0F0)
                                                ext in listOf("XLS", "XLSX", "CSV") -> Color(0xFFF0FFF4)
                                                ext in listOf("DOC", "DOCX") -> Color(0xFFF0F7FF)
                                                else -> MaterialTheme.colorScheme.surface
                                            },
                                            border = BorderStroke(0.5.dp, Color.LightGray),
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clickable {
                                                    if (isImg) onOpenImage(att.filePath)
                                                    else if (isVid) fullScreenVideoPath = att.filePath
                                                    else openFile(context, att.filePath, att.fileName)
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isImg) {
                                                    val imageModel: Any = when {
                                                        att.filePath.startsWith("data:") -> {
                                                            try {
                                                                val pure = if (att.filePath.contains(",")) att.filePath.substringAfter(",") else att.filePath
                                                                android.util.Base64.decode(pure, android.util.Base64.DEFAULT)
                                                            } catch (_: Exception) { att.filePath }
                                                        }
                                                        att.filePath.startsWith("http") -> att.filePath
                                                        else -> File(att.filePath)
                                                    }
                                                    AsyncImage(
                                                        model = imageModel,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else if (isVid) {
                                                    VideoThumbnailImage(
                                                        filePath = att.filePath,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    val docIcon = when {
                                                        ext == "PDF" -> Icons.Default.PictureAsPdf
                                                        ext in listOf("XLS", "XLSX", "CSV") -> Icons.Default.TableChart
                                                        ext in listOf("DOC", "DOCX") -> Icons.Default.Description
                                                        else -> Icons.AutoMirrored.Filled.InsertDriveFile
                                                    }
                                                    val docColor = when {
                                                        ext == "PDF" -> Color(0xFFD32F2F)
                                                        ext in listOf("XLS", "XLSX", "CSV") -> Color(0xFF2E7D32)
                                                        ext in listOf("DOC", "DOCX") -> Color(0xFF1976D2)
                                                        else -> MaterialTheme.colorScheme.primary
                                                    }
                                                    Icon(
                                                        docIcon,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(26.dp),
                                                        tint = docColor
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = att.fileName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            StorageSourceBadge(filePath = att.filePath)
                                        }

                                        IconButton(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    val success = downloadFile(context, att.filePath, att.fileName)
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, if (success) "Download concluído!" else "Falha no download", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(18.dp), tint = Color.Gray)
                                        }
                                    }
                                }

                                if (!isVotingClosed && canVote) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = { onToggleVote(att.id, "ACCEPT") },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (hasUserAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                contentColor = if (hasUserAccepted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Aceitar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { onToggleVote(att.id, "REJECT") },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, if (hasUserRejected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (hasUserRejected) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                                                contentColor = if (hasUserRejected) MaterialTheme.colorScheme.error else Color.Gray
                                            ),
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Rejeitar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$acceptVotesCount voto(s) de aprovação",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "$votePercent%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    progress = { voteFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(Modifier.height(6.dp))

                                val accepts = attWithVotes.votes.filter { it.voteType == "ACCEPT" }
                                val rejects = attWithVotes.votes.filter { it.voteType == "REJECT" }

                                if (accepts.isNotEmpty() || rejects.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        if (accepts.isNotEmpty()) {
                                            val acceptedList = accepts.joinToString(", ") { it.username }
                                            Text(
                                                text = "Aceito por: $acceptedList",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (rejects.isNotEmpty()) {
                                            val rejectedList = rejects.joinToString(", ") { it.username }
                                            Text(
                                                text = "Rejeitado por: $rejectedList",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Nenhum voto registrado para este documento",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        fontStyle = FontStyle.Italic
                                    )
                                }

                                if (isVotingClosed) {
                                    Spacer(Modifier.height(6.dp))
                                    Surface(
                                        color = if (isThisChosen) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (isThisChosen) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                                contentDescription = null,
                                                tint = if (isThisChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = if (isThisChosen) "Status: Orçamento Aprovado" else "Status: Rejeitado",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isThisChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                if (!isVotingClosed && (currentUser.role == "Síndico" || currentUser.role == "ADMIN" || currentUser.username.equals("admin", ignoreCase = true))) {
                    Spacer(Modifier.height(8.dp))
                    if (canSindicoAuthorize) {
                        Button(
                            onClick = { showAuthorizeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Autorizar Orçamento", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Aguardando aceite de pelo menos 2 conselheiros ($councilAcceptCount/2 conselheiros aceitaram)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                if (isVotingClosed) {
                    val isRejectedStatus = text.contains("Rejeitado", ignoreCase = true)
                    Surface(
                        color = if (isRejectedStatus) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                if (isRejectedStatus) Icons.Default.Cancel else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isRejectedStatus) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isRejectedStatus) "❌ Status: Orçamento Rejeitado" else "✅ Status: Orçamento Aprovado",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isRejectedStatus) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAuthorizeDialog) {
        CloseVotingDialog(
            attachments = attachments,
            initialText = text,
            isNameReadOnly = false,
            showDocumentPicker = false,
            onDismiss = { showAuthorizeDialog = false },
            onConfirm = { name, total, count, instValue, start, dur, _ ->
                showAuthorizeDialog = false
                onCloseVoting(name, total, count, instValue, start, dur)
            }
        )
    }

    if (fullScreenVideoPath != null) {
        FullScreenVideoDialog(
            videoPath = fullScreenVideoPath!!,
            onDismiss = { fullScreenVideoPath = null }
        )
    }

    if (viewingDocument != null) {
        DocumentViewerDialog(
            filePath = viewingDocument!!.first,
            fileName = viewingDocument!!.second,
            onDismiss = { viewingDocument = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    text: String,
    author: String,
    date: String,
    isFromMe: Boolean,
    currentUser: UserEntity,
    role: String = "",
    isConfidential: Boolean = false,
    isSindicoOnly: Boolean = false,
    isRead: Boolean = false,
    isVotingClosed: Boolean = false,
    isBudget: Boolean = false,
    isEdited: Boolean = false,
    attachments: List<AttachmentWithVotes> = emptyList(),
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onToggleVote: (Long, String) -> Unit,
    onCloseVoting: (String?, Double?, Int?, Double?, String?, String?) -> Unit = { _, _, _, _, _, _ -> },
    userRoleMap: Map<String, String> = emptyMap(),
    reads: List<OccurrenceMessageReadEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }
    var fullScreenVideoPath by remember { mutableStateOf<String?>(null) }
    var viewingDocument by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showCloseVotingDialog by remember { mutableStateOf(false) }

    val alignment = if (isFromMe) Alignment.End else Alignment.Start

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        if (isBudget) {
            BudgetVotingCard(
                text = text,
                author = author,
                date = date,
                isFromMe = isFromMe,
                currentUser = currentUser,
                role = role,
                isConfidential = isConfidential,
                isSindicoOnly = isSindicoOnly,
                isRead = isRead,
                isVotingClosed = isVotingClosed,
                isEdited = isEdited,
                attachments = attachments,
                isSelected = isSelected,
                onLongClick = onLongClick,
                onToggleVote = onToggleVote,
                onCloseVoting = onCloseVoting,
                onOpenImage = { fullScreenImagePath = it },
                userRoleMap = userRoleMap,
                reads = reads
            )
        } else {
            val bubbleColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else if (isFromMe) Color(0xFFDCF8C6) else Color.White
            val shape = if (isFromMe) {
                RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
            } else {
                RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
            }

            Surface(
                color = bubbleColor,
                shape = shape,
                shadowElevation = 1.dp,
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = onLongClick
                )
            ) {
                Column(Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val authorText = if (role.isNotBlank() && author.uppercase() != "SISTEMA") {
                            "${author.uppercase()} • $role"
                        } else {
                            author.uppercase()
                        }
                        Text(
                            authorText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isFromMe) Color(0xFF075E54) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isConfidential || isSindicoOnly) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Lock, 
                                contentDescription = "Confidencial", 
                                Modifier.size(12.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    if (text.isNotEmpty()) {
                        Text(
                            text = text, 
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = if (text == "Mensagem sigilosa") FontStyle.Italic else FontStyle.Normal,
                            color = if (text == "Mensagem sigilosa") Color.Gray else Color.Unspecified
                        )
                    }
                    
                    if (attachments.isNotEmpty() && text != "Mensagem sigilosa") {
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            attachments.forEach { attWithVotes ->
                                val att = attWithVotes.attachment
                                val isImg = isImageFile(att.fileName) || isPdfFile(att.fileName)
                                val isVid = isVideoFile(att.fileName, att.filePath)

                                Surface(
                                    color = if (isFromMe) Color(0xFFC3E8B0) else Color(0xFFF0F0F0),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable {
                                        if (isImg) fullScreenImagePath = att.filePath
                                        else if (isVid) fullScreenVideoPath = att.filePath
                                        else openFile(context, att.filePath, att.fileName)
                                    }
                                ) {
                                    Column {
                                        if (isImg) {
                                            val imageModel: Any = when {
                                                att.filePath.startsWith("data:") -> {
                                                    try {
                                                        val pure = if (att.filePath.contains(",")) att.filePath.substringAfter(",") else att.filePath
                                                        android.util.Base64.decode(pure, android.util.Base64.DEFAULT)
                                                    } catch (_: Exception) { att.filePath }
                                                }
                                                att.filePath.startsWith("http") -> att.filePath
                                                else -> File(att.filePath)
                                            }

                                            AsyncImage(
                                                model = imageModel,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxWidth(0.7f)
                                                    .heightIn(max = 200.dp)
                                                    .background(Color.Black.copy(alpha = 0.05f)),
                                                contentScale = ContentScale.Crop
                                            )
                                            StorageSourceBadge(
                                                filePath = att.filePath,
                                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                            )
                                        } else if (isVid) {
                                            VideoThumbnailImage(
                                                filePath = att.filePath,
                                                modifier = Modifier
                                                    .fillMaxWidth(0.7f)
                                                    .height(180.dp)
                                                    .background(Color.Black)
                                            )
                                            StorageSourceBadge(
                                                filePath = att.filePath,
                                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.7f)
                                                    .height(110.dp)
                                                    .background(if (isFromMe) Color(0xFFB5DBA0) else Color(0xFFE2E2E2)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier.padding(8.dp)
                                                ) {
                                                    val ext = att.fileName.substringAfterLast('.', "").uppercase()
                                                    val docIcon = when {
                                                        ext == "PDF" -> Icons.Default.PictureAsPdf
                                                        ext in listOf("XLS", "XLSX", "CSV") -> Icons.Default.TableChart
                                                        ext in listOf("DOC", "DOCX") -> Icons.Default.Description
                                                        else -> Icons.AutoMirrored.Filled.InsertDriveFile
                                                    }
                                                    Icon(
                                                        docIcon,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(36.dp),
                                                        tint = if (isFromMe) Color(0xFF075E54) else MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                                    ) {
                                                        Text(
                                                            text = if (ext.isNotEmpty()) ext else "ARQUIVO",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                            StorageSourceBadge(
                                                filePath = att.filePath,
                                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                            )
                                        }

                                        Row(
                                            Modifier.padding(8.dp).fillMaxWidth(0.7f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (isImg) Icons.Default.Image else if (isVid) Icons.Default.Videocam else {
                                                    val ext = att.fileName.substringAfterLast('.', "").uppercase()
                                                    when {
                                                        ext == "PDF" -> Icons.Default.PictureAsPdf
                                                        ext in listOf("XLS", "XLSX", "CSV") -> Icons.Default.TableChart
                                                        ext in listOf("DOC", "DOCX") -> Icons.Default.Description
                                                        else -> Icons.AutoMirrored.Filled.InsertDriveFile
                                                    }
                                                },
                                                null,
                                                Modifier.size(20.dp),
                                                tint = if (isFromMe) Color(0xFF075E54) else Color.Gray
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                att.fileName,
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )

                                            val canDownload = true
                                            if (canDownload) {
                                                IconButton(
                                                    onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    val success = downloadFile(context, att.filePath, att.fileName)
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, if (success) "Download concluído!" else "Falha no download", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Download, null, Modifier.size(16.dp), tint = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.End)) {
                        if (isEdited) {
                            Text(
                                "(editada)",
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
                            )
                        }
                        Text(
                            date, 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Gray, 
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        if (isFromMe) {
                            Spacer(Modifier.width(4.dp))
                            var showReadersDialog by remember { mutableStateOf(false) }
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Visualizadores de lida",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { showReadersDialog = true },
                                tint = if (isRead) Color(0xFF34B7F1) else Color.Gray
                            )
                            if (showReadersDialog) {
                                AlertDialog(
                                    onDismissRequest = { showReadersDialog = false },
                                    title = { Text("Visualizadores da Mensagem") },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (reads.isEmpty()) {
                                                Text("Ainda não visualizada por outros usuários.", style = MaterialTheme.typography.bodyMedium)
                                            } else {
                                                reads.forEach { read ->
                                                    val timeStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(read.timestamp))
                                                    Text("• ${read.username} às $timeStr", style = MaterialTheme.typography.bodyMedium)
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { showReadersDialog = false }) {
                                            Text("Fechar")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCloseVotingDialog) {
        CloseVotingDialog(
            attachments = attachments,
            isNameReadOnly = true,
            showDocumentPicker = false,
            onDismiss = { showCloseVotingDialog = false },
            onConfirm = { name, total, count, instValue, start, dur, _ ->
                onCloseVoting(name, total, count, instValue, start, dur)
                showCloseVotingDialog = false
            }
        )
    }

    if (fullScreenImagePath != null) {
        FullScreenImageDialog(
            filePath = fullScreenImagePath!!,
            onDismiss = { fullScreenImagePath = null }
        )
    }

    if (fullScreenVideoPath != null) {
        FullScreenVideoDialog(
            videoPath = fullScreenVideoPath!!,
            onDismiss = { fullScreenVideoPath = null }
        )
    }

    if (viewingDocument != null) {
        DocumentViewerDialog(
            filePath = viewingDocument!!.first,
            fileName = viewingDocument!!.second,
            onDismiss = { viewingDocument = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseVotingDialog(
    attachments: List<AttachmentWithVotes> = emptyList(),
    initialText: String = "",
    isNameReadOnly: Boolean = false,
    showDocumentPicker: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (budgetName: String, totalValue: Double, installmentsCount: Int, installmentValue: Double, startDate: String, duration: String, attachedUri: Uri?) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    val parsedDetails = remember(initialText) {
        if (initialText.isNotBlank()) extractBudgetDetails(initialText, "") else null
    }

    var budgetName by remember { 
        mutableStateOf(
            parsedDetails?.name?.takeIf { it.isNotBlank() }
                ?: attachments.maxByOrNull { it.votes.size }?.attachment?.fileName
                ?: ""
        ) 
    }
    var totalValueStr by remember { 
        mutableStateOf(
            if (parsedDetails != null && parsedDetails.total > 0) (parsedDetails.total * 100).toLong().toString() else ""
        ) 
    }
    var installmentsCountStr by remember { 
        mutableStateOf(parsedDetails?.installments?.toString() ?: "1") 
    }
    var startDate by remember { 
        mutableStateOf(
            parsedDetails?.startDate?.takeIf { it.isNotBlank() }
                ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        ) 
    }
    var duration by remember { 
        mutableStateOf(parsedDetails?.duration ?: "") 
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val singleFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedUri = uri
        }
    }
    
    val totalValue = (totalValueStr.toDoubleOrNull() ?: 0.0) / 100
    val installmentsCount = installmentsCountStr.toIntOrNull() ?: 1
    val installmentValue = if (installmentsCount > 0) totalValue / installmentsCount else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Definir Orçamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = budgetName,
                    onValueChange = { if (!isNameReadOnly) budgetName = it },
                    label = { Text("Nome do Orçamento/Fornecedor") },
                    readOnly = isNameReadOnly,
                    enabled = !isNameReadOnly,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = totalValueStr,
                    onValueChange = { if (it.length <= 12) totalValueStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Valor Total do Serviço") },
                    prefix = { Text("R$ ") },
                    visualTransformation = CurrencyVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = installmentsCountStr,
                    onValueChange = { installmentsCountStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantidade de Parcelas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formatCurrency(installmentValue),
                    onValueChange = {},
                    label = { Text("Valor da Parcela (Calculado)") },
                    readOnly = true,
                    prefix = { Text("R$ ") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    label = { Text("Data de Início") },
                    readOnly = true,
                    trailingIcon = { IconButton({ showDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Tempo de Execução (ex: 5 dias)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (showDocumentPicker) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Documento do Orçamento",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { singleFileLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (selectedUri != null) "Alterar Documento" else "Adicionar Documento")
                        }
                        if (selectedUri != null) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.InsertDriveFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = getFileName(context, selectedUri!!),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { selectedUri = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(budgetName, totalValue, installmentsCount, installmentValue, startDate, duration, selectedUri)
                },
                enabled = totalValue > 0 && budgetName.isNotBlank()
            ) {
                Text("Confirmar e Encerrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { 
                            timeZone = TimeZone.getTimeZone("UTC") 
                        }.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageDialog(filePath: String, onDismiss: () -> Unit) {
    val pageUrls = if (filePath.contains(",")) {
        filePath.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    } else {
        listOf(filePath)
    }

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { pageUrls.size })
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val url = pageUrls[pageIndex]
                val imageModel: Any = when {
                    url.startsWith("data:") -> {
                        try {
                            val pure = if (url.contains(",")) url.substringAfter(",") else url
                            android.util.Base64.decode(pure, android.util.Base64.DEFAULT)
                        } catch (_: Exception) { url }
                    }
                    url.startsWith("http") -> url
                    else -> File(url)
                }

                var scale by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                val minScale = 1f
                val maxScale = 5f

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(minScale, maxScale)
                                if (scale <= 1f) {
                                    offset = Offset.Zero
                                } else {
                                    val maxOffsetX = (size.width * (scale - 1)) / 2
                                    val maxOffsetY = (size.height * (scale - 1)) / 2
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = { _ ->
                                    if (scale > 1f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                },
                                onTap = {
                                    if (scale == 1f) {
                                        onDismiss()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Página ${pageIndex + 1} de ${pageUrls.size}",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            if (pageUrls.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Página ${pagerState.currentPage + 1} de ${pageUrls.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val currentUrl = pageUrls[pagerState.currentPage]
                                val pageFileName = "documento_p${pagerState.currentPage + 1}.jpg"
                                val success = downloadFile(context, currentUrl, pageFileName)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, if (success) "Página baixada para Downloads/CondSuites" else "Erro ao baixar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Baixar página atual", tint = Color.White)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StorageSourceBadge(filePath = filePath)
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun DocumentViewerDialog(filePath: String, fileName: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val extension = fileName.substringAfterLast('.', "").lowercase()
    val viewerUrl = if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
        if (extension in listOf("doc", "docx", "xls", "xlsx", "ppt", "pptx")) {
            "https://docs.google.com/gview?embedded=true&url=" + android.net.Uri.encode(filePath)
        } else {
            filePath
        }
    } else {
        filePath
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            fileName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                val success = downloadFile(context, filePath, fileName)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, if (success) "Baixado para Downloads/CondSuites" else "Erro ao baixar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Baixar")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                webViewClient = WebViewClient()
                                loadUrl(viewerUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
