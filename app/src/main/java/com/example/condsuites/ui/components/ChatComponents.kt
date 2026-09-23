package com.example.condsuites.ui.components

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.condsuites.data.model.AttachmentWithVotes
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.utils.CurrencyVisualTransformation
import com.example.condsuites.utils.downloadFile
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.getFileName
import com.example.condsuites.utils.isImageFile
import com.example.condsuites.utils.openFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetVotingCard(
    text: String,
    author: String,
    date: String,
    isFromMe: Boolean,
    currentUser: UserEntity,
    isConfidential: Boolean = false,
    isSindicoOnly: Boolean = false,
    isRead: Boolean = false,
    isVotingClosed: Boolean = false,
    attachments: List<AttachmentWithVotes> = emptyList(),
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onToggleVote: (Long, Boolean) -> Unit,
    onShowCloseVotingDialog: () -> Unit,
    onOpenImage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val totalVotes = attachments.sumOf { it.votes.size }
    val canVote = (currentUser.role == "Conselheiro Fiscal" || currentUser.role == "Síndico" || currentUser.role == "ADMIN") && !isVotingClosed

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
                                    text = "Votação de Orçamentos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Enviado por ${author.uppercase()} • $date",
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
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isRead) MaterialTheme.colorScheme.primary else Color.Gray
                                )
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
                    attachments.forEach { attWithVotes ->
                        val att = attWithVotes.attachment
                        val votesCount = attWithVotes.votes.size
                        val voteFraction = if (totalVotes > 0) votesCount.toFloat() / totalVotes else 0f
                        val votePercent = (voteFraction * 100).toInt()
                        val hasVoted = attWithVotes.votes.any { it.username == currentUser.username }
                        val isImg = isImageFile(att.fileName)

                        Surface(
                            color = if (hasVoted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (hasVoted) 1.5.dp else 0.5.dp,
                                color = if (hasVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(0.5.dp, Color.LightGray),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clickable {
                                                if (isImg) onOpenImage(att.filePath)
                                                else openFile(context, att.filePath)
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
                                            } else {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.InsertDriveFile,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                    tint = MaterialTheme.colorScheme.primary
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
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Toque no arquivo para visualizar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }

                                    val canDownload = currentUser.role == "Conselheiro Fiscal" || currentUser.role == "Síndico" || currentUser.role == "ADMIN"
                                    if (canDownload) {
                                        IconButton(
                                            onClick = {
                                                scope.launch(Dispatchers.IO) {
                                                    val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                        downloadFile(context, att.filePath, att.fileName)
                                                    } else false
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

                                    if (!isVotingClosed && canVote) {
                                        Spacer(Modifier.width(4.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = if (hasVoted) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            border = BorderStroke(
                                                width = 1.5.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable {
                                                    onToggleVote(att.id, hasVoted)
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (hasVoted) Icons.Default.Check else Icons.Default.HowToVote,
                                                    contentDescription = if (hasVoted) "Voto confirmado" else "Votar neste orçamento",
                                                    tint = if (hasVoted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
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
                                        text = "$votesCount voto(s)",
                                        style = MaterialTheme.typography.labelMedium,
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
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(Modifier.height(6.dp))

                                if (attWithVotes.votes.isNotEmpty()) {
                                    val votersList = attWithVotes.votes.joinToString(", ") { it.username }
                                    Text(
                                        text = "Votado por: $votersList",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Nenhum voto registrado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                if (isVotingClosed) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "⚠️ Esta votação foi encerrada",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    val canCloseVoting = currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN"
                    if (canCloseVoting) {
                        Button(
                            onClick = onShowCloseVotingDialog,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.LockClock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Encerrar Votação", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
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
    isConfidential: Boolean = false,
    isSindicoOnly: Boolean = false,
    isRead: Boolean = false,
    isVotingClosed: Boolean = false,
    isBudget: Boolean = false,
    attachments: List<AttachmentWithVotes> = emptyList(),
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onToggleVote: (Long, Boolean) -> Unit,
    onCloseVoting: (String?, Double?, Int?, Double?, String?, String?) -> Unit = { _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fullScreenImagePath by remember { mutableStateOf<String?>(null) }
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
                isConfidential = isConfidential,
                isSindicoOnly = isSindicoOnly,
                isRead = isRead,
                isVotingClosed = isVotingClosed,
                attachments = attachments,
                isSelected = isSelected,
                onLongClick = onLongClick,
                onToggleVote = onToggleVote,
                onShowCloseVotingDialog = { showCloseVotingDialog = true },
                onOpenImage = { fullScreenImagePath = it }
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
                        Text(
                            author.uppercase(), 
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
                                val isImg = isImageFile(att.fileName)

                                Surface(
                                    color = if (isFromMe) Color(0xFFC3E8B0) else Color(0xFFF0F0F0),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable {
                                        if (isImg) fullScreenImagePath = att.filePath
                                        else openFile(context, att.filePath)
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
                                        }
                                        Row(
                                            Modifier.padding(8.dp).fillMaxWidth(0.7f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (isImg) Icons.Default.Image else Icons.AutoMirrored.Filled.InsertDriveFile,
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

                                            val canDownload = currentUser.role == "Conselheiro Fiscal" || currentUser.role == "Síndico" || currentUser.role == "ADMIN"
                                            if (canDownload) {
                                                IconButton(
                                                    onClick = {
                                                        scope.launch(Dispatchers.IO) {
                                                            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                                downloadFile(context, att.filePath, att.fileName)
                                                            } else {
                                                                false
                                                            }
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
                        Text(
                            date, 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Gray, 
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        if (isFromMe) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isRead) Color(0xFF34B7F1) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCloseVotingDialog) {
        CloseVotingDialog(
            attachments = attachments,
            onDismiss = { showCloseVotingDialog = false },
            onConfirm = { name, total, count, instValue, start, dur ->
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseVotingDialog(
    attachments: List<AttachmentWithVotes>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int, Double, String, String) -> Unit
) {
    var budgetName by remember { 
        mutableStateOf(attachments.maxByOrNull { it.votes.size }?.attachment?.fileName ?: "") 
    }
    var totalValueStr by remember { mutableStateOf("") }
    var installmentsCountStr by remember { mutableStateOf("1") }
    var startDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var duration by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
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
                    onValueChange = { budgetName = it },
                    label = { Text("Nome do Orçamento/Fornecedor") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(budgetName, totalValue, installmentsCount, installmentValue, startDate, duration)
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

@Composable
fun FullScreenImageDialog(filePath: String, onDismiss: () -> Unit) {
    val imageModel: Any = when {
        filePath.startsWith("data:") -> {
            try {
                val pure = if (filePath.contains(",")) filePath.substringAfter(",") else filePath
                android.util.Base64.decode(pure, android.util.Base64.DEFAULT)
            } catch (_: Exception) { filePath }
        }
        filePath.startsWith("http") -> filePath
        else -> File(filePath)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = "Imagem em tela cheia",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            
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
