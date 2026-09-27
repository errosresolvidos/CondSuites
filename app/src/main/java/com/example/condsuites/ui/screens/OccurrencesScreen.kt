package com.example.condsuites.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.*
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.service.NotificationUtils.cancelAppNotification
import com.example.condsuites.service.NotificationUtils.sendFcmPushNotification
import com.example.condsuites.ui.components.BudgetReplyDialog
import com.example.condsuites.ui.components.ChatBubble
import com.example.condsuites.ui.components.ConfirmDeleteOccurrenceDialog
import com.example.condsuites.ui.components.ProfessionalBudgetActionButton
import com.example.condsuites.utils.CurrencyVisualTransformation
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.getFileName
import com.example.condsuites.utils.uploadImageToCloudinary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun OccurrencesScreen(
    dao: AppDao,
    currentUser: UserEntity,
    scope: CoroutineScope,
    isCompact: Boolean = true,
    selectedMessage: OccurrenceMessageEntity? = null,
    onMessageSelected: (OccurrenceMessageEntity?) -> Unit = {},
    initialShowAddDialog: Boolean = false
) {
    val context = LocalContext.current
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())
    val sortedOccList = remember(occurrences) { occurrences.sortedBy { it.occurrence.id } }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            FirestoreSyncManager.pullAllFromCloud(dao, scope)
        }
    }

    val canSeeConselho = currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN"

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = if (canSeeConselho) listOf("Zelador", "Conselho", "Arquivadas") else listOf("Zelador")

    var showAddDialog by remember { mutableStateOf(initialShowAddDialog) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var occToDelete by remember { mutableStateOf<OccurrenceEntity?>(null) }

    val filteredOccurrences = occurrences.filter {
        val isArchived = it.occurrence.status == "ARQUIVADA"
        val isTargetConselho = it.occurrence.type == "CONSELHO"
        val isTargetZelador = it.occurrence.type == "GERAL"

        val matchesTab = when {
            tabs.getOrNull(selectedTab) == "Arquivadas" -> isArchived
            tabs.getOrNull(selectedTab) == "Conselho" -> isTargetConselho && !isArchived
            else -> isTargetZelador && !isArchived
        }

        val canAccess = when (currentUser.role) {
            "Zelador" -> isTargetZelador && !isArchived
            "Conselheiro Fiscal", "Síndico", "ADMIN" -> true
            else -> isTargetZelador && !isArchived
        }

        val matchesSearch = it.occurrence.title.contains(searchQuery, ignoreCase = true) ||
                it.occurrence.apartment.contains(searchQuery, ignoreCase = true) ||
                it.messages.any { msg -> msg.message.text.contains(searchQuery, ignoreCase = true) }

        matchesTab && canAccess && (searchQuery.isEmpty() || matchesSearch)
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedIds = emptySet() }) {
                                Icon(Icons.Default.Close, null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("${selectedIds.size} selecionados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                }
            }

            if (tabs.size > 1) {
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                    }
                }
            }

            Column(Modifier.fillMaxSize().padding(16.dp)) {
                if (isSearchVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar ocorrência") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (filteredOccurrences.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val message = when (tabs.getOrNull(selectedTab)) {
                            "Arquivadas" -> "Nenhuma ocorrência arquivada."
                            "Conselho" -> "Nenhuma pauta do Conselho."
                            else -> "Nenhuma ocorrência para o Zelador."
                        }
                        Text(message, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredOccurrences) { occWithMsgs ->
                            val isSelected = selectedIds.contains(occWithMsgs.occurrence.id)
                            val occIndex = sortedOccList.indexOfFirst { it.occurrence.id == occWithMsgs.occurrence.id }
                            val occNumber = if (occIndex >= 0) (occIndex + 1).toLong() else occWithMsgs.occurrence.id
                            OccurrenceCard(
                                occWithMsgs = occWithMsgs,
                                occNumber = occNumber,
                                currentUser = currentUser,
                                isCompact = isCompact,
                                isSelected = isSelected,
                                onLongClickCard = {
                                    if (currentUser.role == "ADMIN" || currentUser.username.equals("admin", ignoreCase = true)) {
                                        occToDelete = occWithMsgs.occurrence
                                    }
                                },
                                selectedMessage = selectedMessage,
                                onMessageSelected = onMessageSelected,
                                onReply = { text, isCouncilOnly, isSindicoOnly, uris, isBudget ->
                                    scope.launch {
                                        val msgId = System.currentTimeMillis()
                                        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                        val msg = OccurrenceMessageEntity(
                                            id = msgId,
                                            occurrenceId = occWithMsgs.occurrence.id,
                                            senderUsername = currentUser.username,
                                            text = text,
                                            date = dateStr,
                                            isCouncilOnly = isCouncilOnly,
                                            isSindicoOnly = isSindicoOnly,
                                            isBudget = isBudget
                                        )
                                        dao.insertOccurrenceMessageReplace(msg)
                                        FirestoreSyncManager.syncOccurrenceMessage(msg)
                                        FirestoreSyncManager.touchParentOccurrence(dao, occWithMsgs.occurrence.id)

                                        uris.forEachIndexed { index, uri ->
                                            uploadImageToCloudinary(context, uri) { path ->
                                                if (path != null) {
                                                    scope.launch {
                                                        val attId = System.currentTimeMillis() + index + 1
                                                        val att = OccurrenceAttachmentEntity(
                                                            id = attId,
                                                            messageId = msgId,
                                                            occurrenceId = occWithMsgs.occurrence.id,
                                                            fileName = getFileName(context, uri),
                                                            filePath = path
                                                        )
                                                        dao.insertAttachmentReplace(att)
                                                        FirestoreSyncManager.syncAttachment(att)
                                                        FirestoreSyncManager.touchParentOccurrence(dao, occWithMsgs.occurrence.id)
                                                    }
                                                }
                                            }
                                        }

                                        val action = when {
                                            isSindicoOnly -> "REPLICOU_SIGILOSO_SINDICO"
                                            isCouncilOnly -> "REPLICOU_SIGILOSO_CONSELHO"
                                            else -> "REPLICOU"
                                        }
                                        dao.insertOccurrenceLog(OccurrenceLogEntity(occurrenceId = occWithMsgs.occurrence.id, username = currentUser.username, action = action))
                                    }
                                },
                                onUpdateStatus = { newStatus ->
                                    scope.launch(Dispatchers.IO) {
                                        val updatedOcc = if (newStatus == "DESARQUIVAR") {
                                            val currentTitle = occWithMsgs.occurrence.title
                                            val newTitle = if (!currentTitle.endsWith(" (Desarquivada)")) {
                                                "$currentTitle (Desarquivada)"
                                            } else currentTitle
                                            occWithMsgs.occurrence.copy(status = "FINALIZADA", title = newTitle)
                                        } else {
                                            occWithMsgs.occurrence.copy(status = newStatus)
                                        }

                                        dao.updateOccurrence(updatedOcc)
                                        FirestoreSyncManager.syncOccurrence(updatedOcc)

                                        val actionName = if (newStatus == "DESARQUIVAR") "STATUS_DESARQUIVADA" else "STATUS_$newStatus"
                                        dao.insertOccurrenceLog(OccurrenceLogEntity(occurrenceId = occWithMsgs.occurrence.id, username = currentUser.username, action = actionName))
                                    }
                                },
                                onMarkAsRead = {
                                    scope.launch {
                                        dao.markOccurrenceMessagesAsRead(occWithMsgs.occurrence.id, currentUser.username)
                                        cancelAppNotification(context, occWithMsgs.occurrence.id.toInt())
                                        val unreadMsgs = occWithMsgs.messages.map { it.message }.filter { !it.isRead && it.senderUsername != currentUser.username }
                                        unreadMsgs.forEach { msg ->
                                            FirestoreSyncManager.syncOccurrenceMessage(msg.copy(isRead = true))
                                        }
                                    }
                                },
                                onDeleteMessage = { msgId ->
                                    scope.launch {
                                        dao.deleteOccurrenceMessage(msgId)
                                        FirestoreSyncManager.syncOccurrenceMessage(OccurrenceMessageEntity(id = msgId), isDelete = true)
                                    }
                                },
                                onToggleVote = { attId, hasVoted ->
                                    scope.launch {
                                        if (hasVoted) {
                                            dao.deleteAttachmentVote(attId, currentUser.username)
                                            FirestoreSyncManager.syncAttachmentVote(
                                                OccurrenceAttachmentVoteEntity(attachmentId = attId, username = currentUser.username),
                                                isDelete = true
                                            )
                                        } else {
                                            val vote = OccurrenceAttachmentVoteEntity(attachmentId = attId, username = currentUser.username)
                                            dao.insertAttachmentVote(vote)
                                            FirestoreSyncManager.syncAttachmentVote(vote)
                                        }
                                    }
                                },
                                onCloseVoting = { msgId, name, total, count, instValue, start, dur ->
                                    scope.launch {
                                        dao.closeOccurrenceVoting(msgId)
                                        val closedMsg = dao.getOccurrenceMessageById(msgId)
                                        if (closedMsg != null) {
                                            FirestoreSyncManager.syncOccurrenceMessage(closedMsg)
                                        }
                                        if (name != null && total != null && count != null && instValue != null) {
                                            val resultText = StringBuilder().apply {
                                                append("Votação encerrada.\n\n")
                                                append("✅ Orçamento Aprovado: $name\n")
                                                append("💰 Valor Total: R$ ${formatCurrency(total)}\n")
                                                append("💳 Parcelas: $count x R$ ${formatCurrency(instValue)}\n")
                                                if (!start.isNullOrBlank()) append("📅 Início: $start\n")
                                                if (!dur.isNullOrBlank()) append("⏱️ Duração: $dur")
                                            }.toString()
                                            val sysMsgId = System.currentTimeMillis()
                                            val msg = OccurrenceMessageEntity(
                                                id = sysMsgId,
                                                occurrenceId = occWithMsgs.occurrence.id,
                                                senderUsername = "SISTEMA",
                                                text = resultText,
                                                date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                                                isCouncilOnly = false,
                                                isSindicoOnly = false
                                            )
                                            dao.insertOccurrenceMessageReplace(msg)
                                            FirestoreSyncManager.syncOccurrenceMessage(msg)
                                            FirestoreSyncManager.touchParentOccurrence(dao, occWithMsgs.occurrence.id)
                                            sendFcmPushNotification(
                                                dao = dao,
                                                title = "Votação Encerrada na Ocorrência",
                                                body = "${occWithMsgs.occurrence.title}: Votação encerrada no orçamento $name.",
                                                senderUsername = currentUser.username,
                                                occurrenceId = occWithMsgs.occurrence.id
                                            )
                                            dao.insertOccurrenceLog(OccurrenceLogEntity(occurrenceId = occWithMsgs.occurrence.id, username = currentUser.username, action = "VOTACAO_ENCERRADA"))

                                            val dueDateBase = Calendar.getInstance()
                                            for (i in 1..count) {
                                                val dueDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDateBase.time)
                                                val tx = FinanceTransactionEntity(
                                                    title = "Orçamento: $name",
                                                    description = "Ocorrência #${occWithMsgs.occurrence.id} - Parcela $i/$count",
                                                    type = "PAYABLE",
                                                    amount = instValue,
                                                    dueDate = dueDateStr,
                                                    installmentNumber = i,
                                                    totalInstallments = count,
                                                    groupId = "OCC_${occWithMsgs.occurrence.id}_$msgId",
                                                    category = "Ocorrência",
                                                    relatedId = occWithMsgs.occurrence.id
                                                )
                                                val insertedId = dao.insertTransaction(tx)
                                                FirestoreSyncManager.syncTransaction(tx.copy(id = insertedId))
                                                dueDateBase.add(Calendar.MONTH, 1)
                                            }
                                        }
                                    }
                                },
                                onDelete = {
                                    scope.launch(Dispatchers.IO) {
                                        val relatedTxs = dao.getTransactionsByRelatedId(occWithMsgs.occurrence.id)
                                        relatedTxs.forEach { tx ->
                                            dao.deleteTransaction(tx.id)
                                            FirestoreSyncManager.syncTransaction(tx, isDelete = true)
                                        }
                                        dao.deleteTransactionsByRelatedId(occWithMsgs.occurrence.id)
                                        dao.deleteOccurrence(occWithMsgs.occurrence.id)
                                        FirestoreSyncManager.syncOccurrence(occWithMsgs.occurrence, isDelete = true)
                                    }
                                },
                                onEditOccurrence = { newApartment, newTitle, newDescription ->
                                    scope.launch(Dispatchers.IO) {
                                        val updatedOcc = occWithMsgs.occurrence.copy(
                                            apartment = newApartment,
                                            title = newTitle
                                        )
                                        dao.updateOccurrence(updatedOcc)
                                        FirestoreSyncManager.syncOccurrence(updatedOcc)

                                        val firstMsg = occWithMsgs.messages.sortedBy { it.message.id }.firstOrNull()?.message
                                        if (firstMsg != null) {
                                            val updatedMsg = firstMsg.copy(text = newDescription)
                                            dao.insertOccurrenceMessageReplace(updatedMsg)
                                            FirestoreSyncManager.syncOccurrenceMessage(updatedMsg)
                                            FirestoreSyncManager.touchParentOccurrence(dao, occWithMsgs.occurrence.id)
                                        } else {
                                            val newMsg = OccurrenceMessageEntity(
                                                id = System.currentTimeMillis(),
                                                occurrenceId = occWithMsgs.occurrence.id,
                                                senderUsername = currentUser.username,
                                                text = newDescription,
                                                date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                            )
                                            dao.insertOccurrenceMessageReplace(newMsg)
                                            FirestoreSyncManager.syncOccurrenceMessage(newMsg)
                                            FirestoreSyncManager.touchParentOccurrence(dao, occWithMsgs.occurrence.id)
                                        }

                                        dao.insertOccurrenceLog(
                                            OccurrenceLogEntity(
                                                occurrenceId = occWithMsgs.occurrence.id,
                                                username = currentUser.username,
                                                action = "EDITOU_OCORRENCIA"
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Row(Modifier.align(Alignment.BottomEnd).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FloatingActionButton(
                onClick = { isSearchVisible = !isSearchVisible },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Default.Search, "Buscar")
            }
            if (currentUser.role != "Porteiro") { 
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Adicionar Ocorrência")
                }
            }
        }

        if (occToDelete != null) {
            ConfirmDeleteOccurrenceDialog(
                occurrence = occToDelete!!,
                dao = dao,
                currentUser = currentUser,
                context = context,
                scope = scope,
                onDismiss = { occToDelete = null }
            )
        }

        if (showDeleteConfirm) {
            var hasBudgetsInSelected by remember { mutableStateOf(false) }
            LaunchedEffect(selectedIds) {
                kotlinx.coroutines.withContext(Dispatchers.IO) {
                    val count = selectedIds.sumOf { id ->
                        dao.getTransactionsByRelatedId(id).size
                    }
                    hasBudgetsInSelected = count > 0
                }
            }

            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Excluir ${selectedIds.size} ocorrência(s)?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Esta ação não pode ser desfeita.")
                        if (hasBudgetsInSelected) {
                            Text(
                                "⚠️ Atenção: Uma ou mais ocorrências selecionadas possuem orçamento(s) associado(s). A exclusão também removerá o(s) card(s) correspondente(s) em Contas.",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                selectedIds.forEach { id ->
                                    val relatedTxs = dao.getTransactionsByRelatedId(id)
                                    relatedTxs.forEach { tx ->
                                        dao.deleteTransaction(tx.id)
                                        FirestoreSyncManager.syncTransaction(tx, isDelete = true)
                                    }
                                    dao.deleteTransactionsByRelatedId(id)
                                    dao.deleteOccurrence(id)
                                    val occToDelete = occurrences.firstOrNull { it.occurrence.id == id }?.occurrence
                                    if (occToDelete != null) FirestoreSyncManager.syncOccurrence(occToDelete, isDelete = true)
                                }
                                selectedIds = emptySet()
                                showDeleteConfirm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
                }
            )
        }

        if (showAddDialog) {
            RegisterOccurrenceDialog(
                currentUser = currentUser, 
                dao = dao, 
                onDismiss = { showAddDialog = false }, 
                onConfirm = { title, desc, apt, urgent, uris, type -> 
                    scope.launch(Dispatchers.IO) { 
                        val occId = System.currentTimeMillis()
                        val msgId = occId + 1
                        val newOcc = OccurrenceEntity(
                            id = occId,
                            title = title,
                            apartment = apt,
                            status = "ABERTA",
                            createdByUsername = currentUser.username,
                            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                            type = type,
                            isUrgent = urgent
                        )
                        dao.insertOccurrenceReplace(newOcc)
                        sendFcmPushNotification(
                            dao = dao,
                            title = "Nova Ocorrência: $title",
                            body = "Unidade $apt: $desc",
                            senderUsername = currentUser.username,
                            occurrenceId = occId
                        )
                        FirestoreSyncManager.syncOccurrence(newOcc)

                        val firstMsg = OccurrenceMessageEntity(
                            id = msgId,
                            occurrenceId = occId,
                            senderUsername = currentUser.username,
                            text = desc,
                            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                        )
                        dao.insertOccurrenceMessageReplace(firstMsg)
                        FirestoreSyncManager.syncOccurrenceMessage(firstMsg)

                        uris.forEachIndexed { index, uri -> 
                            uploadImageToCloudinary(context, uri) { path -> 
                                if (path != null) { 
                                    scope.launch(Dispatchers.IO) { 
                                        val attId = occId + 2 + index
                                        val att = OccurrenceAttachmentEntity(
                                            id = attId,
                                            occurrenceId = occId,
                                            messageId = msgId,
                                            fileName = getFileName(context, uri),
                                            filePath = path
                                        )
                                        dao.insertAttachmentReplace(att)
                                        FirestoreSyncManager.syncAttachment(att) 
                                    } 
                                } 
                            } 
                        }
                        showAddDialog = false 
                    } 
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OccurrenceCard(
    occWithMsgs: OccurrenceWithMessages,
    occNumber: Any = occWithMsgs.occurrence.id,
    currentUser: UserEntity,
    isCompact: Boolean = true,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onLongClickCard: () -> Unit = {},
    selectedMessage: OccurrenceMessageEntity? = null,
    onMessageSelected: (OccurrenceMessageEntity?) -> Unit = {},
    onReply: (String, Boolean, Boolean, List<Uri>, Boolean) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onMarkAsRead: () -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onToggleVote: (Long, Boolean) -> Unit,
    onCloseVoting: (Long, String?, Double?, Int?, Double?, String?, String?) -> Unit,
    onDelete: () -> Unit,
    onEditOccurrence: ((newApartment: String, newTitle: String, newDescription: String) -> Unit)? = null
) {
    var showReplyDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showEditHeaderDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(isCompact) {
        if (isCompact) expanded = false
    }

    val occ = occWithMsgs.occurrence
    val messages = occWithMsgs.messages.sortedBy { it.message.id }
    val isCondo = occ.apartment == "CONDOMÍNIO"

    LaunchedEffect(expanded, messages.size) {
        if (expanded) {
            onMarkAsRead()
        }
    }

    if (showEditHeaderDialog && onEditOccurrence != null) {
        val firstMsgText = messages.firstOrNull()?.message?.text ?: ""
        EditOccurrenceDialog(
            occurrence = occ,
            occNumber = occNumber,
            firstMessageText = firstMsgText,
            onDismiss = { showEditHeaderDialog = false },
            onConfirm = { newApt, newTitle, newDesc ->
                onEditOccurrence(newApt, newTitle, newDesc)
                showEditHeaderDialog = false
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(
                onClick = { expanded = !expanded },
                onLongClick = { onLongClickCard() }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCondo -> Color(0xFFE8F4FD)
                else -> Color(0xFFF7F7F7)
            }
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (onEditOccurrence != null) {
                            showEditHeaderDialog = true
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Nº $occNumber",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (occ.isUrgent) {
                                Icon(Icons.Default.NotificationImportant, contentDescription = "Urgente", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                            if (occ.type == "CONSELHO") {
                                Icon(Icons.Default.Gavel, contentDescription = "Conselho", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text(
                            text = if (isCondo) "🏢 CONDOMÍNIO" else "Unidade: ${occ.apartment}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCondo) MaterialTheme.colorScheme.primary else Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (occ.title.isNotBlank()) {
                            Text(
                                text = "Assunto: ${occ.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (occ.title.endsWith("(Desarquivada)")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = when (occ.status) {
                                "ABERTA" -> Color(0xFFE3F2FD)
                                "EM_ESPERA" -> Color(0xFFFFF3E0)
                                "FINALIZADA" -> Color(0xFFE8F5E9)
                                "ARQUIVADA" -> Color(0xFFECEFF1)
                                else -> Color.LightGray
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = occ.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (occ.status) {
                                    "ABERTA" -> Color(0xFF1976D2)
                                    "EM_ESPERA" -> Color(0xFFF57C00)
                                    "FINALIZADA" -> Color(0xFF388E3C)
                                    "ARQUIVADA" -> Color(0xFF546E7A)
                                    else -> Color.DarkGray
                                }
                            )
                        }

                        if (onEditOccurrence != null) {
                            IconButton(
                                onClick = { showEditHeaderDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar Ocorrência",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (messages.isNotEmpty()) {
                        Text(
                            "💬 ${messages.size} ${if (messages.size == 1) "mensagem" else "mensagens"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    val unreadCount = messages.count { !it.message.isRead && it.message.senderUsername != currentUser.username }
                    if (!expanded && unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "$unreadCount nova(s)",
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    occ.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    messages.forEach { msgWithAtts ->
                        val msg = msgWithAtts.message

                        val canSeeSecret = when {
                            msg.isSindicoOnly -> currentUser.role == "Síndico" || currentUser.role == "ADMIN" || msg.senderUsername == currentUser.username
                            msg.isCouncilOnly -> currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN" || msg.senderUsername == currentUser.username
                            msg.isBudget -> currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN" || msg.senderUsername == currentUser.username
                            else -> true
                        }

                        val isVotingResult = msg.senderUsername == "SISTEMA" && msg.text.startsWith("Votação encerrada.")
                        val canSeeVotingResult = currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN" || currentUser.role == "Zelador"

                        val displayMsg = if (!canSeeSecret) {
                            "Mensagem sigilosa"
                        } else if (isVotingResult && !canSeeVotingResult) {
                            "Resultado da votação restrito ao Conselho Fiscal e Síndico."
                        } else if (isVotingResult && currentUser.role == "Zelador") {
                            val lines = msg.text.lines()
                            val header = lines.firstOrNull { it.contains("Votação encerrada.") } ?: "Votação encerrada."
                            val orcamento = lines.firstOrNull { it.contains("Orçamento Aprovado:") }
                            val inicio = lines.firstOrNull { it.contains("Início:") }
                            val duracao = lines.firstOrNull { it.contains("Duração:") }
                            val filtered = listOfNotNull(orcamento, inicio, duracao)
                            if (filtered.isNotEmpty()) {
                                "$header\n\n${filtered.joinToString("\n")}"
                            } else {
                                header
                            }
                        } else {
                            msg.text
                        }

                        ChatBubble(
                            text = displayMsg,
                            author = msg.senderUsername,
                            date = msg.date,
                            isFromMe = msg.senderUsername == currentUser.username || (msg.senderUsername == "admin" && currentUser.role == "ADMIN"),
                            isConfidential = msg.isCouncilOnly,
                            isSindicoOnly = msg.isSindicoOnly,
                            isRead = msg.isRead,
                            isVotingClosed = msg.isVotingClosed,
                            isBudget = msg.isBudget,
                            attachments = msgWithAtts.attachments,
                            isSelected = selectedMessage?.id == msg.id,
                            onLongClick = {
                                if (selectedMessage?.id == msg.id) onMessageSelected(null)
                                else onMessageSelected(msg)
                            },
                            currentUser = currentUser,
                            onToggleVote = onToggleVote,
                            onCloseVoting = { name, total, count, instValue, start, dur ->
                                onCloseVoting(msg.id, name, total, count, instValue, start, dur)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val canReply = occ.status != "FINALIZADA" && occ.status != "ARQUIVADA" && currentUser.role != "Porteiro" && currentUser.role != "Zelador"
                    val canZeladorReply = occ.status != "FINALIZADA" && occ.status != "ARQUIVADA" && currentUser.role == "Zelador" && occ.type == "GERAL"

                    if (canReply || canZeladorReply) {
                        TextButton(onClick = { showReplyDialog = true }) {
                            Icon(Icons.Default.Reply, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Responder")
                        }
                    }

                    if (currentUser.role == "Síndico" || currentUser.role == "ADMIN" || currentUser.role == "Conselheiro Fiscal") {
                        if (occ.status == "ARQUIVADA") {
                            TextButton(onClick = { onUpdateStatus("DESARQUIVAR") }) {
                                Icon(Icons.Default.Unarchive, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Desarquivar")
                            }
                        } else if (occ.status != "FINALIZADA") {
                            if (currentUser.role == "Síndico" || currentUser.role == "ADMIN") {
                                ProfessionalBudgetActionButton(onClick = { showBudgetDialog = true })
                                Spacer(Modifier.width(6.dp))
                            }

                            TextButton(onClick = { onUpdateStatus("FINALIZADA") }) {
                                Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Fechar")
                            }
                        } else {
                            TextButton(onClick = { onUpdateStatus("ABERTA") }) {
                                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reabrir")
                            }
                            Spacer(Modifier.width(4.dp))
                            TextButton(onClick = { onUpdateStatus("ARQUIVADA") }) {
                                Icon(Icons.Default.Archive, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Arquivar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReplyDialog) {
        var replyText by remember { mutableStateOf("") }
        var isCouncilOnly by remember { mutableStateOf(false) }
        var isSindicoOnly by remember { mutableStateOf(false) }
        val selectedUris = remember { mutableStateListOf<Uri>() }
        val context = LocalContext.current

        val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            selectedUris.addAll(uris)
        }

        AlertDialog(
            onDismissRequest = { showReplyDialog = false },
            title = { Text("Continuar Conversa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(replyText, { replyText = it }, label = { Text("Sua mensagem") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                    if (currentUser.role == "Síndico" || currentUser.role == "ADMIN" || (currentUser.role == "Conselheiro Fiscal" && occ.type == "CONSELHO")) {
                        Column {
                            if (currentUser.role == "Síndico" || currentUser.role == "ADMIN") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = isCouncilOnly, onCheckedChange = { isCouncilOnly = it; if(it) isSindicoOnly = false })
                                    Text("Sigiloso (Apenas Conselho)", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            if (currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = isSindicoOnly, onCheckedChange = { isSindicoOnly = it; if(it) isCouncilOnly = false })
                                    Text("Sigiloso (Apenas Síndico)", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = { fileLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Anexar Documentos")
                    }

                    if (selectedUris.isNotEmpty()) {
                        Text("Anexos (${selectedUris.size}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Column {
                            selectedUris.forEach { uri ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (replyText.isNotBlank() || selectedUris.isNotEmpty()) {
                        onReply(replyText, isCouncilOnly, isSindicoOnly, selectedUris.toList(), false)
                        showReplyDialog = false
                    }
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { showReplyDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showBudgetDialog) {
        BudgetReplyDialog(
            onDismiss = { showBudgetDialog = false },
            onConfirm = { text, uris ->
                onReply(text, true, false, uris, true)
                showBudgetDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterOccurrenceDialog(
    currentUser: UserEntity, 
    dao: AppDao, 
    onDismiss: () -> Unit, 
    onConfirm: (String, String, String, Boolean, List<Uri>, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var apt by remember { mutableStateOf("") }
    var isCondo by remember { mutableStateOf(true) }
    var isUrgent by remember { mutableStateOf(false) }
    
    var floor by remember { mutableStateOf("T") }
    var valueStr by remember { mutableStateOf("") }
    var isInstallment by remember { mutableStateOf(false) }
    var installmentsCountStr by remember { mutableStateOf("1") }

    val defaultDest = when (currentUser.role) {
        "Conselheiro Fiscal" -> "CONSELHO"
        else -> "GERAL"
    }
    var destination by remember { mutableStateOf(defaultDest) } 
    
    val selectedUris = remember { mutableStateListOf<Uri>() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val serviceDescriptionsList by dao.getServiceDescriptions().collectAsState(initial = emptyList())
    val dbOccurrenceTypes by dao.getOccurrenceTypes().collectAsState(initial = emptyList())
    
    val defaultDescs = remember { listOf("TROCA DE DICTADOR", "TROCA DE BOTÃO", "TROCA DE VENTILADOR") }
    val allDescs = remember(serviceDescriptionsList) {
        val list = serviceDescriptionsList.map { it.description }
        if (list.isEmpty()) defaultDescs else (defaultDescs + list).distinct()
    }
    
    val defaultSubjects = remember { listOf("Elevador Social", "Elevador de Serviço", "Piscina", "Jardim", "Vazamento", "Caixa D'água", "Câmeras", "Barulho", "Infiltração", "Limpeza") }
    val allSubjects = remember(dbOccurrenceTypes) {
        val list = dbOccurrenceTypes.map { it.type }
        if (list.isEmpty()) defaultSubjects else (defaultSubjects + list).distinct()
    }
    
    var showAddDescDialog by remember { mutableStateOf(false) }
    var newDescText by remember { mutableStateOf("") }
    var descToDelete by remember { mutableStateOf<String?>(null) }
    
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectText by remember { mutableStateOf("") }
    var subjectToDelete by remember { mutableStateOf<String?>(null) }
    var expandedTitle by remember { mutableStateOf(false) }
    
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        selectedUris.addAll(uris)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Nova Ocorrência", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(thickness = 0.5.dp)

                if (currentUser.role != "Porteiro" && currentUser.role != "Zelador") {
                    Column {
                        Text("Encaminhar para:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = destination == "GERAL",
                                onClick = { destination = "GERAL" },
                                label = { Text("Zelador", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = if (destination == "GERAL") { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            val destLabel = if (currentUser.role == "Conselheiro Fiscal") "Síndico" else "Conselho"
                            FilterChip(
                                selected = destination == "CONSELHO",
                                onClick = { destination = "CONSELHO" },
                                label = { Text(destLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = if (destination == "CONSELHO") { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Column {
                    Text("Local da Ocorrência:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = !isCondo,
                            onClick = { isCondo = false },
                            label = { Text("Unidade", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (!isCondo) { { Icon(Icons.Default.Apartment, null, Modifier.size(18.dp)) } } else null,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = isCondo,
                            onClick = { isCondo = true },
                            label = { Text("Condomínio", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (isCondo) { { Icon(Icons.Default.Groups, null, Modifier.size(18.dp)) } } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (!isCondo) {
                    OutlinedTextField(
                        value = apt, 
                        onValueChange = { apt = it }, 
                        label = { Text("Nº do Apartamento") }, 
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Apartment, null) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedTitle,
                        onExpandedChange = { expandedTitle = !expandedTitle },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Assunto / Título") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTitle) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Description, null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTitle,
                            onDismissRequest = { expandedTitle = false }
                        ) {
                            allSubjects.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(selectionOption, modifier = Modifier.weight(1f))
                                            IconButton(
                                                onClick = {
                                                    expandedTitle = false
                                                    subjectToDelete = selectionOption
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Excluir Assunto",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        title = selectionOption
                                        expandedTitle = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { showAddSubjectDialog = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar Assunto", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }

                val isElevatorTitle = title == "Elevador Social" || title == "Elevador de Serviço" || title == "Elevador"

                if (isCondo && isElevatorTitle) {
                    Spacer(Modifier.height(8.dp))
                    
                    var expandedDescDropdown by remember { mutableStateOf(false) }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedDescDropdown,
                            onExpandedChange = { expandedDescDropdown = !expandedDescDropdown },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = desc,
                                onValueChange = { desc = it },
                                label = { Text("Descrição") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDescDropdown) },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Build, null) },
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDescDropdown,
                                onDismissRequest = { expandedDescDropdown = false }
                            ) {
                                allDescs.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(selectionOption, modifier = Modifier.weight(1f))
                                                IconButton(
                                                    onClick = {
                                                        expandedDescDropdown = false
                                                        descToDelete = selectionOption
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Excluir Descrição",
                                                        tint = Color.Red,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            desc = selectionOption
                                            expandedDescDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { showAddDescDialog = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, contentDescription = "Adicionar Descrição", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    var expandedFloor by remember { mutableStateOf(false) }
                    val floors = listOf("12", "11", "10", "9", "8", "7", "6", "5", "4", "3", "2", "T", "1S", "2S", "3S", "4S")
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedFloor,
                        onExpandedChange = { expandedFloor = !expandedFloor },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = floor,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Andar") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFloor) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Elevator, null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFloor,
                            onDismissRequest = { expandedFloor = false }
                        ) {
                            floors.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        floor = selectionOption
                                        expandedFloor = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (currentUser.role != "Zelador") {
                        OutlinedTextField(
                            value = valueStr,
                            onValueChange = { if (it.length <= 12) valueStr = it.filter { c -> c.isDigit() } },
                            label = { Text("Valor (Opcional)") },
                            prefix = { Text("R$ ") },
                            visualTransformation = CurrencyVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable { isInstallment = !isInstallment }
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = isInstallment, onCheckedChange = { isInstallment = it })
                                    Text("Parcelado", fontWeight = FontWeight.Bold)
                                }
                                
                                if (isInstallment) {
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = installmentsCountStr,
                                        onValueChange = { installmentsCountStr = it.filter { c -> c.isDigit() } },
                                        label = { Text("Quantidade de Parcelas") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    
                                    val totalVal = (valueStr.toDoubleOrNull() ?: 0.0) / 100
                                    val count = installmentsCountStr.toIntOrNull() ?: 1
                                    val instValue = if (count > 0) totalVal / count else 0.0
                                    
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = formatCurrency(instValue),
                                        onValueChange = {},
                                        label = { Text("Valor da Parcela") },
                                        readOnly = true,
                                        prefix = { Text("R$ ") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = desc, 
                        onValueChange = { desc = it }, 
                        label = { Text("Descrição detalhada") }, 
                        modifier = Modifier.fillMaxWidth(), 
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (showAddDescDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddDescDialog = false; newDescText = "" },
                        title = { Text("Adicionar Nova Descrição") },
                        text = {
                            OutlinedTextField(
                                value = newDescText,
                                onValueChange = { newDescText = it },
                                label = { Text("Descrição") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newDescText.isNotBlank()) {
                                        val trimmed = newDescText.trim()
                                        scope.launch {
                                            val entity = ServiceDescriptionEntity(description = trimmed)
                                            dao.insertServiceDescription(entity)
                                            FirestoreSyncManager.syncServiceDescription(entity)
                                        }
                                        desc = trimmed
                                        newDescText = ""
                                        showAddDescDialog = false
                                    }
                                }
                            ) {
                                Text("Adicionar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddDescDialog = false; newDescText = "" }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                if (showAddSubjectDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddSubjectDialog = false; newSubjectText = "" },
                        title = { Text("Adicionar Novo Assunto") },
                        text = {
                            OutlinedTextField(
                                value = newSubjectText,
                                onValueChange = { newSubjectText = it },
                                label = { Text("Nome do Assunto") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newSubjectText.isNotBlank()) {
                                        val trimmed = newSubjectText.trim()
                                        scope.launch {
                                            val entity = OccurrenceTypeEntity(type = trimmed)
                                            dao.insertOccurrenceType(entity)
                                            FirestoreSyncManager.syncOccurrenceType(entity)
                                        }
                                        title = trimmed
                                        newSubjectText = ""
                                        showAddSubjectDialog = false
                                    }
                                }
                            ) {
                                Text("Adicionar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddSubjectDialog = false; newSubjectText = "" }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                if (subjectToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { subjectToDelete = null },
                        title = { Text("Excluir Assunto") },
                        text = { Text("Deseja mesmo excluir o assunto \"$subjectToDelete\"?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val toDelete = subjectToDelete!!
                                    scope.launch {
                                        dao.deleteOccurrenceType(toDelete)
                                        FirestoreSyncManager.syncOccurrenceType(OccurrenceTypeEntity(type = toDelete), isDelete = true)
                                    }
                                    if (title == toDelete) title = ""
                                    subjectToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Excluir")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { subjectToDelete = null }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                if (descToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { descToDelete = null },
                        title = { Text("Excluir Descrição") },
                        text = { Text("Deseja mesmo excluir a descrição \"$descToDelete\"?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val toDelete = descToDelete!!
                                    scope.launch {
                                        dao.deleteServiceDescription(toDelete)
                                        FirestoreSyncManager.syncServiceDescription(ServiceDescriptionEntity(description = toDelete), isDelete = true)
                                    }
                                    if (desc == toDelete) desc = ""
                                    descToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Excluir")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { descToDelete = null }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                Surface(
                    color = if (isUrgent) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { isUrgent = !isUrgent }
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                        Column {
                            Text("Urgente", fontWeight = FontWeight.Bold, color = if (isUrgent) Color.Red else Color.Unspecified)
                            Text("Priorizar esta ocorrência", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }

                Column {
                    Text("Anexos:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { fileLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Upload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adicionar Documentos/Fotos")
                    }
                    
                    if (selectedUris.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            selectedUris.forEach { uri ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, Modifier.size(20.dp), tint = Color.Gray)
                                        Spacer(Modifier.width(8.dp))
                                        Text(getFileName(context, uri), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { selectedUris.removeIf { it == uri } }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Close, null, Modifier.size(18.dp), tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { 
                        Text("Cancelar", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium) 
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            if (title.isNotBlank() && desc.isNotBlank() && (isCondo || apt.isNotBlank())) {
                                val finalApt = if (isCondo) "CONDOMÍNIO" else apt
                                
                                val finalTitle = if (isCondo && isElevatorTitle) {
                                    val totalVal = (valueStr.toDoubleOrNull() ?: 0.0) / 100
                                    val payMethod = if (isInstallment) {
                                        val count = (installmentsCountStr.toIntOrNull() ?: 1).let { if (it <= 0) 1 else it }
                                        "Parcelado ($count x R$ ${formatCurrency(totalVal / count)})"
                                    } else "À vista"

                                    StringBuilder().apply {
                                        append(title)
                                        append(" - Andar: $floor")
                                        if (currentUser.role != "Zelador" && valueStr.isNotBlank() && totalVal > 0) {
                                            append(" - R$ ${formatCurrency(totalVal)} ($payMethod)")
                                        }
                                    }.toString()
                                } else {
                                    title
                                }

                                val finalDesc = desc

                                onConfirm(finalTitle, finalDesc, finalApt, isUrgent, selectedUris.toList(), destination)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = title.isNotBlank() && desc.isNotBlank() && (isCondo || apt.isNotBlank()),
                        modifier = Modifier.weight(2f)
                    ) {
                        Text("Registrar", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun OccurrenceLogsDialog(dao: AppDao, onDismiss: () -> Unit) {
    val logs by dao.getAllOccurrenceLogs().collectAsState(initial = emptyList())
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Logs de Auditoria", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 400.dp)) {
                    items(logs) { log ->
                        val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("${log.username} ${log.action} a ocorrência #${log.occurrenceId}", fontWeight = FontWeight.Bold)
                            Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            HorizontalDivider(Modifier.padding(top = 4.dp), thickness = 0.5.dp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDismiss, Modifier.align(Alignment.End)) { Text("Fechar") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOccurrenceDialog(
    occurrence: OccurrenceEntity,
    occNumber: Any = occurrence.id,
    firstMessageText: String,
    onDismiss: () -> Unit,
    onConfirm: (newApartment: String, newTitle: String, newDescription: String) -> Unit
) {
    var isCondo by remember { mutableStateOf(occurrence.apartment == "CONDOMÍNIO" || occurrence.apartment.isBlank()) }
    var apartment by remember { mutableStateOf(if (occurrence.apartment == "CONDOMÍNIO") "" else occurrence.apartment) }
    var title by remember { mutableStateOf(occurrence.title) }
    var description by remember { mutableStateOf(firstMessageText) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Editar Ocorrência", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Nº $occNumber", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(thickness = 0.5.dp)

                Column {
                    Text("Local da Ocorrência:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = !isCondo,
                            onClick = { isCondo = false },
                            label = { Text("Unidade", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (!isCondo) { { Icon(Icons.Default.Apartment, null, Modifier.size(18.dp)) } } else null,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = isCondo,
                            onClick = { isCondo = true },
                            label = { Text("Condomínio", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (isCondo) { { Icon(Icons.Default.Groups, null, Modifier.size(18.dp)) } } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (!isCondo) {
                    OutlinedTextField(
                        value = apartment,
                        onValueChange = { apartment = it },
                        label = { Text("Nº do Apartamento") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Apartment, null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assunto / Título") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Build, null) },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancelar", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && (isCondo || apartment.isNotBlank())) {
                                val finalApt = if (isCondo) "CONDOMÍNIO" else apartment
                                onConfirm(finalApt, title.trim(), description.trim())
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = title.isNotBlank() && (isCondo || apartment.isNotBlank()),
                        modifier = Modifier.weight(2f)
                    ) {
                        Text("Salvar Alterações", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
