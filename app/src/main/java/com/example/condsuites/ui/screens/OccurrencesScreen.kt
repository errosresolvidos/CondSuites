package com.example.condsuites.ui.screens

import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.*
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.service.sync.OccurrenceSyncScheduler
import com.example.condsuites.data.model.SyncStatus
import com.example.condsuites.service.NotificationUtils.cancelAppNotification
import com.example.condsuites.service.NotificationUtils.sendFcmPushNotification
import com.example.condsuites.ui.components.BudgetOptionChoiceDialog
import com.example.condsuites.ui.components.BudgetReplyDialog
import com.example.condsuites.ui.components.ChatBubble
import com.example.condsuites.ui.components.CloseVotingDialog
import com.example.condsuites.ui.components.ConfirmDeleteOccurrenceDialog
import com.example.condsuites.ui.components.ProfessionalBudgetActionButton
import com.example.condsuites.utils.CurrencyVisualTransformation
import com.example.condsuites.utils.copyFileToInternalStorage
import com.example.condsuites.utils.deleteCloudinaryFile
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.getFileName
import com.example.condsuites.utils.isImageFile
import com.example.condsuites.utils.isPdfFile
import com.example.condsuites.utils.isVideoFile
import com.example.condsuites.utils.uploadImageToCloudinary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import com.example.condsuites.data.model.isSuperAdminUser

@Composable
fun OccurrencesScreen(
    dao: AppDao,
    currentUser: UserEntity,
    scope: CoroutineScope,
    isCompact: Boolean = true,
    selectedMessage: OccurrenceMessageEntity? = null,
    onMessageSelected: (OccurrenceMessageEntity?) -> Unit = {},
    initialShowAddDialog: Boolean = false,
    initialExpandedOccurrenceId: Long? = null,
    onExpandedOccurrenceConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())
    val users by dao.getAllUsersFlow().collectAsState(initial = emptyList())
    val userRoleMap = remember(users) { users.associate { it.username.lowercase() to it.role } }
    val sortedOccList = remember(occurrences) { occurrences.sortedBy { it.occurrence.id } }

    val canSeeConselho = currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN"
    val isSuperAdmin = isSuperAdminUser(currentUser)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = buildList {
        add("Zelador")
        if (canSeeConselho) add("Conselho")
        if (canSeeConselho) add("Arquivadas")
        if (isSuperAdmin) add("Admin")
    }

    var showAddDialog by remember { mutableStateOf(initialShowAddDialog) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("Todas") }

    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var occToDelete by remember { mutableStateOf<OccurrenceEntity?>(null) }
    var occurrenceToToggleUrgent by remember { mutableStateOf<OccurrenceWithMessages?>(null) }
    var expandedOccurrenceId by remember { mutableStateOf<Long?>(initialExpandedOccurrenceId) }
    var currentSessionExpandedOccurrenceId by remember { mutableStateOf<Long?>(null) }
    var targetSelectedMessage by remember { mutableStateOf<OccurrenceMessageEntity?>(selectedMessage) }

    LaunchedEffect(initialExpandedOccurrenceId, occurrences) {
        if (initialExpandedOccurrenceId != null) {
            expandedOccurrenceId = initialExpandedOccurrenceId
            val targetOcc = occurrences.find { it.occurrence.id == initialExpandedOccurrenceId }
            if (targetOcc != null) {
                val isArchived = targetOcc.occurrence.status == "ARQUIVADA"
                val isConselho = targetOcc.occurrence.type == "CONSELHO"
                selectedTab = when {
                    isArchived && tabs.contains("Arquivadas") -> tabs.indexOf("Arquivadas")
                    isConselho && tabs.contains("Conselho") -> tabs.indexOf("Conselho")
                    else -> 0
                }
                val lastMsg = targetOcc.messages.maxByOrNull { it.message.id }?.message
                if (lastMsg != null) {
                    targetSelectedMessage = lastMsg
                }
            }
            onExpandedOccurrenceConsumed()
        }
    }

    val unreadCardCounts = remember(occurrences, currentUser) {
        var zeladorCount = 0
        var conselhoCount = 0
        var arquivadasCount = 0
        var adminCount = 0

        for (occWithMsgs in occurrences) {
            val occ = occWithMsgs.occurrence
            if (!canUserSeeOccurrence(occ, currentUser)) continue

            val hasUnread = occWithMsgs.messages.any { msgWithAtt ->
                isMessageUnreadForUser(msgWithAtt.message, occ, currentUser, msgWithAtt.reads)
            }

            if (hasUnread) {
                val isArchived = occ.status == "ARQUIVADA"
                val isConselho = occ.type == "CONSELHO"
                val isAdmin = occ.type == "ADMIN"
                
                when {
                    isAdmin -> adminCount++
                    isArchived -> arquivadasCount++
                    isConselho -> conselhoCount++
                    else -> zeladorCount++
                }
            }
        }

        mapOf(
            "Zelador" to zeladorCount,
            "Conselho" to conselhoCount,
            "Arquivadas" to arquivadasCount,
            "Admin" to adminCount
        )
    }

    val filteredOccurrences = occurrences.filter {
        val isArchived = it.occurrence.status == "ARQUIVADA"
        val isTargetConselho = it.occurrence.type == "CONSELHO"
        val isTargetZelador = it.occurrence.type == "GERAL"
        val isTargetAdmin = it.occurrence.type == "ADMIN"

        val matchesTab = when (tabs.getOrNull(selectedTab)) {
            "Arquivadas" -> isArchived && !isTargetAdmin
            "Conselho" -> isTargetConselho && !isArchived
            "Admin" -> isTargetAdmin
            else -> isTargetZelador && !isArchived
        }

        val canAccess = canUserSeeOccurrence(it.occurrence, currentUser)

        val matchesSearch = it.occurrence.title.contains(searchQuery, ignoreCase = true) ||
                it.occurrence.apartment.contains(searchQuery, ignoreCase = true) ||
                it.messages.any { msg -> msg.message.text.contains(searchQuery, ignoreCase = true) }

        val matchesStatusFilter = when (selectedStatusFilter) {
            "Urgentes" -> it.occurrence.isUrgent
            "Abertas" -> it.occurrence.status == "ABERTA" && !it.occurrence.isUrgent
            "Finalizadas" -> it.occurrence.status == "FINALIZADA"
            else -> true
        }

        matchesTab && canAccess && matchesStatusFilter && (searchQuery.isEmpty() || matchesSearch)
    }.sortedWith(
        compareBy<OccurrenceWithMessages> { 
            when {
                it.occurrence.isUrgent -> 0
                it.occurrence.status == "ABERTA" -> 1
                it.occurrence.status == "FINALIZADA" -> 2
                else -> 3
            }
        }.thenByDescending { 
            val occIndex = sortedOccList.indexOfFirst { sorted -> sorted.occurrence.id == it.occurrence.id }
            if (occIndex >= 0) (occIndex + 1).toLong() else it.occurrence.id
        }
    )

    Box(Modifier.fillMaxSize()) {
        DisposableEffect(Unit) {
            onDispose {
                currentSessionExpandedOccurrenceId = null
            }
        }
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

            if (tabs.isNotEmpty()) {
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        val unreadCount = unreadCardCounts[title] ?: 0
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(title)
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ) {
                                            Text("$unreadCount")
                                        }
                                    }
                                }
                            }
                        )
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterOptions = listOf(
                        Triple("Todas", Icons.Default.FormatListBulleted, "Todas as Ocorrências"),
                        Triple("Urgentes", Icons.Default.Warning, "Ocorrências Urgentes"),
                        Triple("Abertas", Icons.Default.LockOpen, "Ocorrências Abertas (sem urgência)"),
                        Triple("Finalizadas", Icons.Default.CheckCircle, "Ocorrências Finalizadas")
                    )
                    filterOptions.forEach { (key, icon, tooltipText) ->
                        val isSelected = selectedStatusFilter == key
                        val tintColor = when (key) {
                            "Urgentes" -> Color.Red
                            "Abertas" -> Color(0xFF1976D2)
                            "Finalizadas" -> Color(0xFF388E3C)
                            else -> MaterialTheme.colorScheme.primary
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatusFilter = key },
                            label = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tooltipText,
                                    tint = if (isSelected) tintColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (key == "Urgentes") Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                if (filteredOccurrences.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val message = when {
                            selectedStatusFilter != "Todas" -> "Nenhuma ocorrência (${selectedStatusFilter.lowercase()}) encontrada."
                            tabs.getOrNull(selectedTab) == "Arquivadas" -> "Nenhuma ocorrência arquivada."
                            tabs.getOrNull(selectedTab) == "Conselho" -> "Nenhuma pauta do Conselho."
                            tabs.getOrNull(selectedTab) == "Admin" -> "Nenhuma ocorrência."
                            else -> "Nenhuma ocorrência para o Zelador."
                        }
                        Text(message, color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredOccurrences, key = { it.occurrence.id }) { occWithMsgs ->
                            val isSelected = selectedIds.contains(occWithMsgs.occurrence.id)
                            val occIndex = sortedOccList.indexOfFirst { it.occurrence.id == occWithMsgs.occurrence.id }
                            val occNumber = if (occIndex >= 0) (occIndex + 1).toLong() else occWithMsgs.occurrence.id
                            val canPrioritize = currentUser.role == "Zelador" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "Síndico" || currentUser.role == "ADMIN" || isSuperAdmin
                            OccurrenceCard(
                                occWithMsgs = occWithMsgs,
                                occNumber = occNumber,
                                currentUser = currentUser,
                                userRoleMap = userRoleMap,
                                isCompact = isCompact,
                                isSelected = isSelected,
                                canPrioritize = canPrioritize,
                                onToggleUrgent = {
                                    occurrenceToToggleUrgent = occWithMsgs
                                },
                                initiallyExpanded = expandedOccurrenceId == occWithMsgs.occurrence.id,
                                onLongClickCard = {
                                    if (currentUser.role == "ADMIN" || currentUser.username.equals("admin", ignoreCase = true)) {
                                        occToDelete = occWithMsgs.occurrence
                                    }
                                },
                                selectedMessage = targetSelectedMessage ?: selectedMessage,
                                onMessageSelected = onMessageSelected,
                                onDefineBudget = { name, total, count, instValue, start, dur, attachedUri ->
                                    scope.launch(Dispatchers.IO) {
                                        val msgId = System.currentTimeMillis()
                                        val roleLabel = when (currentUser.role) {
                                            "Zelador" -> "Zelador"
                                            "Conselheiro Fiscal" -> "Conselheiro"
                                            else -> "Síndico"
                                        }

                                        val budgetText = StringBuilder().apply {
                                            append("Orçamento enviado pelo $roleLabel (Aguardando confirmação dos conselheiros e autorização do Síndico):\n\n")
                                            append("🏢 Prestador/Fornecedor: $name\n")
                                            if (total > 0.0) {
                                                append("💰 Valor Total: R$ ${formatCurrency(total)}\n")
                                                append("💳 Parcelas: $count x R$ ${formatCurrency(instValue)}\n")
                                            }
                                            if (!start.isNullOrBlank()) append("📅 Início: $start\n")
                                            if (!dur.isNullOrBlank()) append("⏱️ Duração: $dur")
                                        }.toString()

                                        val msg = OccurrenceMessageEntity(
                                            id = msgId,
                                            occurrenceId = occWithMsgs.occurrence.id,
                                            senderUsername = currentUser.username,
                                            text = budgetText,
                                            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                                            isCouncilOnly = true,
                                            isSindicoOnly = false,
                                            isBudget = true,
                                            isVotingClosed = false,
                                            syncStatus = SyncStatus.PENDING_UPDATE.name
                                        )
                                        dao.insertOccurrenceMessageReplace(msg)
                                        FirestoreSyncManager.syncOccurrenceMessage(msg)
                                        OccurrenceSyncScheduler.scheduleSync(context)

                                        val attId = System.currentTimeMillis() + 1
                                        val filePath = if (attachedUri != null) {
                                            copyFileToInternalStorage(context, attachedUri) ?: ""
                                        } else {
                                            ""
                                        }
                                        val fileName = if (attachedUri != null) {
                                            getFileName(context, attachedUri)
                                        } else {
                                            "Orçamento: $name (R$ ${formatCurrency(total)})"
                                        }
                                        val att = OccurrenceAttachmentEntity(
                                            id = attId,
                                            messageId = msgId,
                                            occurrenceId = occWithMsgs.occurrence.id,
                                            fileName = fileName,
                                            filePath = filePath,
                                            syncStatus = SyncStatus.PENDING_UPDATE.name
                                        )
                                        dao.insertAttachmentReplace(att)
                                        FirestoreSyncManager.syncAttachment(att)
                                        OccurrenceSyncScheduler.scheduleSync(context)

                                        val readEntity = OccurrenceMessageReadEntity(
                                            id = generateReadEntityId(msgId, currentUser.username),
                                            messageId = msgId,
                                            username = currentUser.username,
                                            timestamp = System.currentTimeMillis(),
                                            syncStatus = SyncStatus.PENDING_UPDATE.name
                                        )
                                        dao.insertOccurrenceMessageRead(readEntity)
                                        FirestoreSyncManager.syncOccurrenceMessageRead(readEntity)
                                        OccurrenceSyncScheduler.scheduleSync(context)

                                        val pOcc = dao.getOccurrenceEntityById(occWithMsgs.occurrence.id)
                                        if (pOcc != null) {
                                            pOcc.syncStatus = SyncStatus.PENDING_UPDATE.name
                                            dao.updateOccurrence(pOcc)
                                            FirestoreSyncManager.syncOccurrence(pOcc)
                                            OccurrenceSyncScheduler.scheduleSync(context)
                                        }

                                        sendFcmPushNotification(
                                            context = context,
                                            dao = dao,
                                            title = "Novo Orçamento do $roleLabel",
                                            body = "${occWithMsgs.occurrence.title}: Orçamento $name enviado aguardando confirmação dos conselheiros e autorização do Síndico.",
                                            senderUsername = currentUser.username,
                                            occurrenceId = occWithMsgs.occurrence.id
                                        )
                                        val logAction = "ORCAMENTO_${roleLabel.uppercase()}_DEFINIDO"
                                        dao.insertOccurrenceLog(OccurrenceLogEntity(occurrenceId = occWithMsgs.occurrence.id, username = currentUser.username, action = logAction))
                                    }
                                },
                                onReply = { text, isCouncilOnly, isSindicoOnly, uris, isBudget ->
                                    scope.launch(Dispatchers.IO) {
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
                                            isBudget = isBudget,
                                            syncStatus = SyncStatus.PENDING_UPDATE.name
                                        )
                                        dao.insertOccurrenceMessageReplace(msg)
                                        FirestoreSyncManager.syncOccurrenceMessage(msg)
                                        
                                        withContext(Dispatchers.Main) {
                                            currentSessionExpandedOccurrenceId = occWithMsgs.occurrence.id
                                            expandedOccurrenceId = occWithMsgs.occurrence.id
                                        }

                                        OccurrenceSyncScheduler.scheduleSync(context)
                                        
                                        val parentOcc = dao.getOccurrenceEntityById(occWithMsgs.occurrence.id)
                                        if (parentOcc != null) {
                                            parentOcc.syncStatus = SyncStatus.PENDING_UPDATE.name
                                            dao.updateOccurrence(parentOcc)
                                            FirestoreSyncManager.syncOccurrence(parentOcc)
                                            OccurrenceSyncScheduler.scheduleSync(context)
                                        }

                                        val readEntity = OccurrenceMessageReadEntity(
                                            id = generateReadEntityId(msgId, currentUser.username),
                                            messageId = msgId,
                                            username = currentUser.username,
                                            timestamp = System.currentTimeMillis(),
                                            syncStatus = SyncStatus.PENDING_UPDATE.name
                                        )
                                        dao.insertOccurrenceMessageRead(readEntity)
                                        FirestoreSyncManager.syncOccurrenceMessageRead(readEntity)

                                        sendFcmPushNotification(
                                            context = context,
                                            dao = dao,
                                            title = "Nova mensagem em Ocorrências",
                                            body = "${occWithMsgs.occurrence.title} - ${currentUser.username}: $text",
                                            senderUsername = currentUser.username,
                                            occurrenceId = occWithMsgs.occurrence.id
                                        )

                                        uris.forEachIndexed { index, uri ->
                                            val localPath = copyFileToInternalStorage(context, uri)
                                            uploadImageToCloudinary(context, uri) { path ->
                                                val finalPath = path ?: localPath
                                                if (finalPath != null) {
                                                    scope.launch(Dispatchers.IO) {
                                                        val attId = System.currentTimeMillis() + index + 1
                                                        val att = OccurrenceAttachmentEntity(
                                                            id = attId,
                                                            messageId = msgId,
                                                            occurrenceId = occWithMsgs.occurrence.id,
                                                            fileName = getFileName(context, uri),
                                                            filePath = finalPath,
                                                            syncStatus = SyncStatus.PENDING_UPDATE.name
                                                        )
                                                        dao.insertAttachmentReplace(att)
                                                        FirestoreSyncManager.syncAttachment(att)
                                                        OccurrenceSyncScheduler.scheduleSync(context)
                                                        val pOcc = dao.getOccurrenceEntityById(occWithMsgs.occurrence.id)
                                                        if (pOcc != null) {
                                                            pOcc.syncStatus = SyncStatus.PENDING_UPDATE.name
                                                            dao.updateOccurrence(pOcc)
                                                            FirestoreSyncManager.syncOccurrence(pOcc)
                                                        }
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
                                        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                        val updatedOcc = if (newStatus == "DESARQUIVAR") {
                                            val currentTitle = occWithMsgs.occurrence.title
                                            val newTitle = if (!currentTitle.endsWith(" (Desarquivada)")) {
                                                "$currentTitle (Desarquivada)"
                                            } else currentTitle
                                            occWithMsgs.occurrence.copy(status = "FINALIZADA", title = newTitle)
                                        } else {
                                            occWithMsgs.occurrence.copy(status = newStatus)
                                        }

                                        updatedOcc.syncStatus = SyncStatus.PENDING_UPDATE.name
dao.updateOccurrence(updatedOcc)
OccurrenceSyncScheduler.scheduleSync(context)

                                        val sysMsgText = when (newStatus) {
                                            "ARQUIVADA" -> "Ocorrência arquivada por ${currentUser.username}."
                                            "DESARQUIVAR" -> "Ocorrência desarquivada por ${currentUser.username}."
                                            "FINALIZADA" -> "Ocorrência finalizada por ${currentUser.username}."
                                            "ABERTA" -> "Ocorrência reaberta por ${currentUser.username}."
                                            else -> "Status alterado para $newStatus por ${currentUser.username}."
                                        }

                                        val sysMsgId = System.currentTimeMillis()
                                        val sysMsg = OccurrenceMessageEntity(
                                            id = sysMsgId,
                                            occurrenceId = occWithMsgs.occurrence.id,
                                            senderUsername = "SISTEMA",
                                            text = sysMsgText,
                                            date = dateStr
                                        )
                                        sysMsg.syncStatus = SyncStatus.PENDING_UPDATE.name
dao.insertOccurrenceMessageReplace(sysMsg)
OccurrenceSyncScheduler.scheduleSync(context)
                                        val sysRead = OccurrenceMessageReadEntity(
                                            id = generateReadEntityId(sysMsgId, currentUser.username),
                                            messageId = sysMsgId,
                                            username = currentUser.username,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        sysRead.syncStatus = SyncStatus.PENDING_UPDATE.name
dao.insertOccurrenceMessageRead(sysRead)
OccurrenceSyncScheduler.scheduleSync(context)
                                        dao.getOccurrenceEntityById(occWithMsgs.occurrence.id)?.let { it.syncStatus = SyncStatus.PENDING_UPDATE.name; dao.updateOccurrence(it); OccurrenceSyncScheduler.scheduleSync(context) }

                                        val actionName = if (newStatus == "DESARQUIVAR") "STATUS_DESARQUIVADA" else "STATUS_$newStatus"
                                        dao.insertOccurrenceLog(OccurrenceLogEntity(occurrenceId = occWithMsgs.occurrence.id, username = currentUser.username, action = actionName))
                                    }
                                },
                                onMarkAsRead = {
                                    scope.launch(Dispatchers.IO) {
                                        val unreadMsgs = occWithMsgs.messages.filter { msgWithAtt ->
                                            isMessageUnreadForUser(msgWithAtt.message, occWithMsgs.occurrence, currentUser, msgWithAtt.reads)
                                        }
                                        unreadMsgs.forEach { msgWithAtt ->
                                            val msg = msgWithAtt.message
                                            msg.isRead = true
                                            msg.syncStatus = SyncStatus.PENDING_UPDATE.name
dao.updateOccurrenceMessage(msg)
OccurrenceSyncScheduler.scheduleSync(context)

                                            val readEntity = OccurrenceMessageReadEntity(
                                                id = generateReadEntityId(msg.id, currentUser.username),
                                                messageId = msg.id,
                                                username = currentUser.username,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            dao.insertOccurrenceMessageRead(readEntity)
                                            try {
                                                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                com.google.android.gms.tasks.Tasks.await(
                                                    db.collection("occurrence_message_reads").document(readEntity.id).set(readEntity),
                                                    5,
                                                    java.util.concurrent.TimeUnit.SECONDS
                                                )
                                            } catch (e: Exception) {
                                                android.util.Log.e("FIRESTORE_SYNC", "Failed to sync message read: ${e.message}")
                                            }
                                        }
                                        withContext(Dispatchers.Main) {
                                            cancelAppNotification(context, occWithMsgs.occurrence.id.toInt())
                                        }
                                    }
                                },
                                onDeleteMessage = { msgId ->
                                    scope.launch {
                                        val targetMsg = dao.getOccurrenceMessageById(msgId)
                                        if (targetMsg != null) {
                                            val isAdmin = currentUser.role == "ADMIN" || currentUser.username.equals("admin", ignoreCase = true)
                                            val isSender = targetMsg.senderUsername.equals(currentUser.username, ignoreCase = true) ||
                                                    (targetMsg.senderUsername.equals("admin", ignoreCase = true) && currentUser.role == "ADMIN")
                                            val creationTime = if (targetMsg.timestamp > 0) targetMsg.timestamp else targetMsg.id
                                            val elapsed = System.currentTimeMillis() - creationTime
                                            val isWithin20Min = elapsed in 0..(20 * 60 * 1000L)

                                            if (isAdmin || (isSender && isWithin20Min)) {
                                                try {
                                                    val msgAtts = dao.getAllAttachments().filter { it.messageId == msgId }
                                                    msgAtts.forEach { att ->
                                                        deleteCloudinaryFile(att.filePath)
                                                        FirestoreSyncManager.syncAttachment(att, isDelete = true)
                                                    }
                                                } catch (_: Exception) {}
                                                FirestoreSyncManager.syncOccurrenceMessage(targetMsg, isDelete = true)
                                                dao.deleteOccurrenceMessage(msgId)
                                                dao.getOccurrenceEntityById(occWithMsgs.occurrence.id)?.let {
                                                    it.syncStatus = SyncStatus.PENDING_UPDATE.name
                                                    dao.updateOccurrence(it)
                                                    OccurrenceSyncScheduler.scheduleSync(context)
                                                }
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Não é mais possível remover esta mensagem pois o prazo de 20 minutos expirou.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                onEditMessage = { msgId, newText ->
                                    scope.launch {
                                        val targetMsg = dao.getOccurrenceMessageById(msgId)
                                        if (targetMsg != null) {
                                            val creationTime = if (targetMsg.timestamp > 0) targetMsg.timestamp else targetMsg.id
                                            val elapsed = System.currentTimeMillis() - creationTime
                                            if (elapsed <= 20 * 60 * 1000L) {
                                                val updatedMsg = targetMsg.copy(text = newText, isEdited = true)
                                                updatedMsg.syncStatus = SyncStatus.PENDING_UPDATE.name
dao.updateOccurrenceMessage(updatedMsg)
OccurrenceSyncScheduler.scheduleSync(context)
                                                dao.getOccurrenceEntityById(occWithMsgs.occurrence.id)?.let { it.syncStatus = SyncStatus.PENDING_UPDATE.name; dao.updateOccurrence(it); OccurrenceSyncScheduler.scheduleSync(context) }
                                                dao.insertOccurrenceLog(
                                                    OccurrenceLogEntity(
                                                        occurrenceId = occWithMsgs.occurrence.id,
                                                        username = currentUser.username,
                                                        action = "EDITOU_MENSAGEM"
                                                    )
                                                )
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "O tempo limite de 20 minutos para editar a mensagem já expirou.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                onToggleVote = { attId, voteType ->
                                    scope.launch {
                                        val existingVote = dao.getVoteByUserAndAttachment(attId, currentUser.username)
                                        if (existingVote != null && existingVote.voteType == voteType) {
                                            dao.deleteAttachmentVote(attId, currentUser.username)
                                            existingVote.syncStatus = if (true) SyncStatus.PENDING_DELETE.name else SyncStatus.PENDING_UPDATE.name
OccurrenceSyncScheduler.scheduleSync(context)
                                        } else {
                                            if (existingVote != null) {
                                                dao.deleteAttachmentVote(attId, currentUser.username)
                                                existingVote.syncStatus = if (true) SyncStatus.PENDING_DELETE.name else SyncStatus.PENDING_UPDATE.name
OccurrenceSyncScheduler.scheduleSync(context)
                                            }
                                            val vote = OccurrenceAttachmentVoteEntity(
                                                attachmentId = attId,
                                                username = currentUser.username,
                                                voteType = voteType
                                            )
                                            vote.syncStatus = SyncStatus.PENDING_UPDATE.name
dao.insertAttachmentVote(vote)
OccurrenceSyncScheduler.scheduleSync(context)
                                        }
                                    }
                                },
                                onCloseVoting = { msgId, name, total, count, instValue, start, dur ->
                                    scope.launch {
                                        val targetMsg = dao.getOccurrenceMessageById(msgId)
                                        val isAuthorizedToClose = currentUser.role == "Síndico" || currentUser.role == "ADMIN" || currentUser.username.equals("admin", ignoreCase = true) || (targetMsg != null && targetMsg.senderUsername.equals(currentUser.username, ignoreCase = true))
                                        if (!isAuthorizedToClose) return@launch
                                        dao.closeOccurrenceVoting(msgId)
                                        val closedMsg = dao.getOccurrenceMessageById(msgId)

                                        val isRejected = start == "Rejeitado" || name?.contains("Rejeitado", ignoreCase = true) == true

                                        val extractedName = if (!name.isNullOrBlank() && name != "Orçamento Aprovado" && name != "Orçamento Rejeitado") {
                                            name
                                        } else {
                                            val line = closedMsg?.text?.lines()?.firstOrNull { it.contains("Prestador/Fornecedor:") || it.contains("Orçamento:") }
                                            line?.substringAfter(":")?.trim() ?: "Orçamento"
                                        }

                                        val extractedTotal = if (total != null && total > 0.0) {
                                            total
                                        } else {
                                            val line = closedMsg?.text?.lines()?.firstOrNull { it.contains("Valor Total:", ignoreCase = true) }
                                            if (line != null) {
                                                val clean = line.replace("R$", "").replace(".", "").replace(",", ".").filter { it.isDigit() || it == '.' }
                                                clean.toDoubleOrNull() ?: 0.0
                                            } else {
                                                0.0
                                            }
                                        }

                                        val effectiveName = extractedName
                                        val effectiveTotal = extractedTotal
                                        val effectiveCount = if (count != null && count > 0) count else 1
                                        val effectiveInstValue = if (instValue != null && instValue > 0.0) instValue else if (effectiveTotal > 0.0) effectiveTotal / effectiveCount else 0.0

                                        if (closedMsg != null) {
                                            val updatedText = if (isRejected) {
                                                StringBuilder().apply {
                                                    append("❌ Status: Orçamento Rejeitado\n\n")
                                                    append("🏢 Orçamento: $effectiveName\n")
                                                    append("⚠️ Votação encerrada com rejeição.")
                                                }.toString()
                                            } else {
                                                StringBuilder().apply {
                                                    append("✅ Status: Orçamento Aprovado\n\n")
                                                    append("🏢 Prestador/Fornecedor: $effectiveName\n")
                                                    if (effectiveTotal > 0.0) {
                                                        append("💰 Valor Total: R$ ${formatCurrency(effectiveTotal)}\n")
                                                        append("💳 Parcelas: $effectiveCount x R$ ${formatCurrency(effectiveInstValue)}\n")
                                                    }
                                                    if (!start.isNullOrBlank()) append("📅 Início: $start\n")
                                                    if (!dur.isNullOrBlank()) append("⏱️ Duração: $dur")
                                                }.toString()
                                            }
                                            val finalMsg = closedMsg.copy(text = updatedText, isVotingClosed = true)
                                            finalMsg.syncStatus = SyncStatus.PENDING_UPDATE.name
dao.updateOccurrenceMessage(finalMsg)
OccurrenceSyncScheduler.scheduleSync(context)
                                        }

                                        if (isRejected) {
                                            val relatedTxs = dao.getTransactionsByRelatedId(occWithMsgs.occurrence.id)
                                            relatedTxs.forEach { tx ->
                                                dao.deleteTransaction(tx.id)
                                                FirestoreSyncManager.syncTransaction(tx, isDelete = true)
                                            }
                                            dao.deleteTransactionsByRelatedId(occWithMsgs.occurrence.id)
                                        } else if (effectiveTotal > 0.0) {
                                            val dueDateBase = Calendar.getInstance()
                                            for (i in 1..effectiveCount) {
                                                val dueDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDateBase.time)
                                                val tx = FinanceTransactionEntity(
                                                    title = "Orçamento: $effectiveName",
                                                    description = "Ocorrência #${occWithMsgs.occurrence.id} - Parcela $i/$effectiveCount",
                                                    type = "PAYABLE",
                                                    amount = effectiveInstValue,
                                                    dueDate = dueDateStr,
                                                    installmentNumber = i,
                                                    totalInstallments = effectiveCount,
                                                    groupId = "OCC_${occWithMsgs.occurrence.id}_$msgId",
                                                    category = "Ocorrência",
                                                    relatedId = occWithMsgs.occurrence.id
                                                )
                                                val insertedId = dao.insertTransaction(tx)
                                                FirestoreSyncManager.syncTransaction(tx.copy(id = insertedId))
                                                dueDateBase.add(Calendar.MONTH, 1)
                                            }
                                        }

                                        dao.getOccurrenceEntityById(occWithMsgs.occurrence.id)?.let { it.syncStatus = SyncStatus.PENDING_UPDATE.name; dao.updateOccurrence(it); OccurrenceSyncScheduler.scheduleSync(context) }
                                        sendFcmPushNotification(
                                            context = context,
                                            dao = dao,
                                            title = if (isRejected) "Orçamento Rejeitado" else "Orçamento Aprovado",
                                            body = "${occWithMsgs.occurrence.title}: Orçamento $effectiveName ${if (isRejected) "rejeitado" else "aprovado"}.",
                                            senderUsername = currentUser.username,
                                            occurrenceId = occWithMsgs.occurrence.id
                                        )
                                        dao.insertOccurrenceLog(OccurrenceLogEntity(occurrenceId = occWithMsgs.occurrence.id, username = currentUser.username, action = if (isRejected) "ORCAMENTO_REJEITADO" else "ORCAMENTO_APROVADO"))
                                    }
                                },
                                onDelete = {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val occAtts = dao.getAllAttachments().filter { it.occurrenceId == occWithMsgs.occurrence.id }
                                            occAtts.forEach { att ->
                                                deleteCloudinaryFile(att.filePath)
                                                FirestoreSyncManager.syncAttachment(att, isDelete = true)
                                            }
                                            val msgs = dao.getAllOccurrenceMessages().filter { it.occurrenceId == occWithMsgs.occurrence.id }
                                            msgs.forEach { msg ->
                                                FirestoreSyncManager.syncOccurrenceMessage(msg, isDelete = true)
                                            }
                                        } catch (_: Exception) {}
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
                                        updatedOcc.syncStatus = SyncStatus.PENDING_UPDATE.name
dao.updateOccurrence(updatedOcc)
OccurrenceSyncScheduler.scheduleSync(context)

                                        val firstMsg = occWithMsgs.messages.sortedBy { it.message.id }.firstOrNull()?.message
                                        if (firstMsg != null) {
                                            val updatedMsg = firstMsg.copy(text = newDescription, isEdited = true)
                                            updatedMsg.syncStatus = SyncStatus.PENDING_UPDATE.name
dao.updateOccurrenceMessage(updatedMsg)
OccurrenceSyncScheduler.scheduleSync(context)
                                            dao.getOccurrenceEntityById(occWithMsgs.occurrence.id)?.let { it.syncStatus = SyncStatus.PENDING_UPDATE.name; dao.updateOccurrence(it); OccurrenceSyncScheduler.scheduleSync(context) }
                                        } else {
                                            val newMsg = OccurrenceMessageEntity(
                                                id = System.currentTimeMillis(),
                                                occurrenceId = occWithMsgs.occurrence.id,
                                                senderUsername = currentUser.username,
                                                text = newDescription,
                                                date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                            )
                                            newMsg.syncStatus = SyncStatus.PENDING_UPDATE.name
dao.insertOccurrenceMessageReplace(newMsg)
OccurrenceSyncScheduler.scheduleSync(context)
                                            dao.getOccurrenceEntityById(occWithMsgs.occurrence.id)?.let { it.syncStatus = SyncStatus.PENDING_UPDATE.name; dao.updateOccurrence(it); OccurrenceSyncScheduler.scheduleSync(context) }
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

        if (occurrenceToToggleUrgent != null) {
            val targetOccWithMsgs = occurrenceToToggleUrgent!!
            val isUrgentNow = targetOccWithMsgs.occurrence.isUrgent
            AlertDialog(
                onDismissRequest = { occurrenceToToggleUrgent = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.NotificationImportant,
                        contentDescription = null,
                        tint = if (isUrgentNow) Color.Gray else Color.Red
                    )
                },
                title = {
                    Text(if (isUrgentNow) "Remover Urgência" else "Confirmar Urgência")
                },
                text = {
                    Text(
                        if (isUrgentNow)
                            "Tem certeza de que deseja remover a urgência da ocorrência #${targetOccWithMsgs.occurrence.id} (${targetOccWithMsgs.occurrence.title})?"
                        else
                            "Tem certeza de que deseja destacar a ocorrência #${targetOccWithMsgs.occurrence.id} (${targetOccWithMsgs.occurrence.title}) como URGENTE?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val occToProcess = targetOccWithMsgs
                            occurrenceToToggleUrgent = null
                            scope.launch(Dispatchers.IO) {
                                val occId = occToProcess.occurrence.id
                                com.example.condsuites.service.NotificationUtils.recordLocalAction(occId, "URGENT")
                                com.example.condsuites.service.NotificationUtils.recordLocalAction(occId, "EDIT")

                                val newUrgent = !occToProcess.occurrence.isUrgent
                                val updatedOcc = occToProcess.occurrence.copy(isUrgent = newUrgent)
                                updatedOcc.syncStatus = SyncStatus.PENDING_UPDATE.name
                                dao.updateOccurrence(updatedOcc)
                                com.example.condsuites.service.sync.UrgentOccurrenceScheduler.triggerImmediateCheck(context)
                                OccurrenceSyncScheduler.scheduleSync(context)

                                val sysMsgText = if (newUrgent) {
                                    "🚨 Chamada destacada como URGENTE por ${currentUser.username}."
                                } else {
                                    "Urgência da chamada removida por ${currentUser.username}."
                                }
                                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                val sysMsgId = System.currentTimeMillis()
                                val sysMsg = OccurrenceMessageEntity(
                                    id = sysMsgId,
                                    occurrenceId = occId,
                                    senderUsername = "SISTEMA",
                                    text = sysMsgText,
                                    date = dateStr,
                                    isRead = true,
                                    syncStatus = SyncStatus.PENDING_UPDATE.name
                                )
                                dao.insertOccurrenceMessageReplace(sysMsg)
                                FirestoreSyncManager.syncOccurrenceMessage(sysMsg)
                                OccurrenceSyncScheduler.scheduleSync(context)

                                val sysRead = OccurrenceMessageReadEntity(
                                    id = generateReadEntityId(sysMsgId, currentUser.username),
                                    messageId = sysMsgId,
                                    username = currentUser.username,
                                    timestamp = System.currentTimeMillis(),
                                    syncStatus = SyncStatus.PENDING_UPDATE.name
                                )
                                dao.insertOccurrenceMessageRead(sysRead)
                                FirestoreSyncManager.syncOccurrenceMessageRead(sysRead)
                                OccurrenceSyncScheduler.scheduleSync(context)

                                val pOcc = dao.getOccurrenceEntityById(occId)
                                if (pOcc != null) {
                                    pOcc.syncStatus = SyncStatus.PENDING_UPDATE.name
                                    dao.updateOccurrence(pOcc)
                                    FirestoreSyncManager.syncOccurrence(pOcc)
                                    OccurrenceSyncScheduler.scheduleSync(context)
                                }

                                val logAction = if (newUrgent) "PRIORIZOU_URGENTE" else "DESMARCOU_URGENTE"
                                dao.insertOccurrenceLog(
                                    OccurrenceLogEntity(
                                        occurrenceId = occId,
                                        username = currentUser.username,
                                        action = logAction
                                    )
                                )

                                if (newUrgent) {
                                    sendFcmPushNotification(
                                        context = context,
                                        dao = dao,
                                        title = "🚨 Ocorrência URGENTE",
                                        body = "'${occToProcess.occurrence.title}' foi destacada como URGENTE por ${currentUser.username}.",
                                        senderUsername = currentUser.username,
                                        occurrenceId = occId
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUrgentNow) MaterialTheme.colorScheme.primary else Color.Red
                        )
                    ) {
                        Text(if (isUrgentNow) "Remover Urgência" else "Confirmar Urgência")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { occurrenceToToggleUrgent = null }) {
                        Text("Cancelar")
                    }
                }
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
                                    try {
                                        val occAtts = dao.getAllAttachments().filter { it.occurrenceId == id }
                                        occAtts.forEach { att ->
                                            deleteCloudinaryFile(att.filePath)
                                            FirestoreSyncManager.syncAttachment(att, isDelete = true)
                                        }
                                        val msgs = dao.getAllOccurrenceMessages().filter { it.occurrenceId == id }
                                        msgs.forEach { msg ->
                                            FirestoreSyncManager.syncOccurrenceMessage(msg, isDelete = true)
                                        }
                                    } catch (_: Exception) {}
                                    val relatedTxs = dao.getTransactionsByRelatedId(id)
                                    relatedTxs.forEach { tx ->
                                        dao.deleteTransaction(tx.id)
                                        FirestoreSyncManager.syncTransaction(tx, isDelete = true)
                                    }
                                    dao.deleteTransactionsByRelatedId(id)
                                    val occToDelete = occurrences.firstOrNull { it.occurrence.id == id }?.occurrence
                                    dao.deleteOccurrence(id)
                                    if (occToDelete != null) {
                                        FirestoreSyncManager.syncOccurrence(occToDelete, isDelete = true)
                                    }
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
            val initialDest = if (tabs.getOrNull(selectedTab) == "Admin") "ADMIN" else null
            RegisterOccurrenceDialog(
                currentUser = currentUser, 
                dao = dao, 
                initialDestination = initialDest,
                onDismiss = { showAddDialog = false }, 
                onConfirm = { title, desc, apt, urgent, uris, type -> 
                    scope.launch(Dispatchers.IO) { 
                        val occId = System.currentTimeMillis()
                        com.example.condsuites.service.NotificationUtils.recordLocalAction(occId, "NEW_OCCURRENCE")
                        com.example.condsuites.service.NotificationUtils.recordLocalAction(occId, "EDIT")
                        val msgId = occId + 1
                        val newOcc = OccurrenceEntity(
                            id = occId,
                            title = title,
                            apartment = apt,
                            status = "ABERTA",
                            createdByUsername = currentUser.username,
                            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                            type = type,
                            isUrgent = urgent,
                            syncStatus = SyncStatus.PENDING_UPDATE.name
                        )
                        dao.insertOccurrenceReplace(newOcc)
                        FirestoreSyncManager.syncOccurrence(newOcc)
                        if (urgent) {
                            com.example.condsuites.service.sync.UrgentOccurrenceScheduler.triggerImmediateCheck(context)
                        }
                        OccurrenceSyncScheduler.scheduleSync(context)

                        val notifTitle = if (urgent) "🚨 URGENTE: Nova Ocorrência - $title" else "Nova Ocorrência: $title"
                        sendFcmPushNotification(
                            context = context,
                            dao = dao,
                            title = notifTitle,
                            body = "Unidade $apt: $desc",
                            senderUsername = currentUser.username,
                            occurrenceId = occId
                        )

                        val firstMsg = OccurrenceMessageEntity(
                            id = msgId,
                            occurrenceId = occId,
                            senderUsername = currentUser.username,
                            text = desc,
                            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                            syncStatus = SyncStatus.PENDING_UPDATE.name
                        )
                        dao.insertOccurrenceMessageReplace(firstMsg)
                        FirestoreSyncManager.syncOccurrenceMessage(firstMsg)
                        OccurrenceSyncScheduler.scheduleSync(context)

                        uris.forEachIndexed { index, uri -> 
                            val localPath = copyFileToInternalStorage(context, uri)
                            uploadImageToCloudinary(context, uri) { path -> 
                                val finalPath = path ?: localPath
                                if (finalPath != null) { 
                                    scope.launch(Dispatchers.IO) { 
                                        val attId = occId + 2 + index
                                        val att = OccurrenceAttachmentEntity(
                                            id = attId,
                                            occurrenceId = occId,
                                            messageId = msgId,
                                            fileName = getFileName(context, uri),
                                            filePath = finalPath,
                                            syncStatus = SyncStatus.PENDING_UPDATE.name
                                        )
                                        dao.insertAttachmentReplace(att)
                                        FirestoreSyncManager.syncAttachment(att)
                                        OccurrenceSyncScheduler.scheduleSync(context) 
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



@Composable
fun OccurrencePinnedHeader(
    occWithMsgs: OccurrenceWithMessages,
    occNumber: Any,
    currentUser: UserEntity,
    onClose: () -> Unit,
    onEditOccurrence: ((newApartment: String, newTitle: String, newDescription: String) -> Unit)? = null
) {
    val occ = occWithMsgs.occurrence
    val messages = occWithMsgs.messages.sortedBy { it.message.id }
    val isCondo = occ.apartment == "CONDOMÍNIO"
    var showEditHeaderDialog by remember { mutableStateOf(false) }

    val isAdmin = currentUser.role == "ADMIN" || currentUser.username.equals("admin", ignoreCase = true)
    val creatorUsername = occ.createdByUsername.ifBlank { messages.firstOrNull()?.message?.senderUsername ?: "" }
    val isCreator = creatorUsername.isNotBlank() && creatorUsername.equals(currentUser.username, ignoreCase = true)

    val context = LocalContext.current
    val creationMillis = remember(occ.id, occ.date, messages.firstOrNull()?.message?.id) {
        getOccurrenceCreationMillis(occ, messages)
    }
    val isWithinTwoHours = (System.currentTimeMillis() - creationMillis) <= (2 * 60 * 60 * 1000L)
    val canEditOccurrence = onEditOccurrence != null && (isAdmin || currentUser.canEditOccurrences || (isCreator && isWithinTwoHours))

    if (showEditHeaderDialog && canEditOccurrence) {
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

    Surface(
        color = when {
            isCondo -> Color(0xFFE8F4FD)
            else -> Color(0xFFF7F7F7)
        },
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
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
                                Surface(
                                    color = Color.Red,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.NotificationImportant,
                                            contentDescription = "Urgente",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "URGENTE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            if (occ.type == "CONSELHO") {
                                Icon(Icons.Default.Gavel, contentDescription = "Conselho", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (occ.title.isNotBlank()) {
                            Text(
                                text = occ.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (occ.title.endsWith("(Desarquivada)")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = if (isCondo) "🏢 CONDOMÍNIO" else "Unidade: ${occ.apartment}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isCondo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (occ.isUrgent) {
                                Icon(
                                    imageVector = Icons.Default.NotificationImportant,
                                    contentDescription = "Urgente",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (canEditOccurrence) {
                                IconButton(
                                    onClick = { showEditHeaderDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar Ocorrência",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.ExpandLess,
                                    contentDescription = "Fechar card",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

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
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (messages.isNotEmpty()) {
                    Text(
                        "💬 ${messages.size} ${if (messages.size == 1) "mensagem" else "mensagens"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    occ.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OccurrenceCard(
    occWithMsgs: OccurrenceWithMessages,
    occNumber: Any = occWithMsgs.occurrence.id,
    currentUser: UserEntity,
    userRoleMap: Map<String, String> = emptyMap(),
    isCompact: Boolean = true,
    isSelected: Boolean = false,
    hideHeader: Boolean = false,
    onExpand: (() -> Unit)? = null,
    initiallyExpanded: Boolean = false,
    modifier: Modifier = Modifier,
    onLongClickCard: () -> Unit = {},
    selectedMessage: OccurrenceMessageEntity? = null,
    onMessageSelected: (OccurrenceMessageEntity?) -> Unit = {},
    onReply: (String, Boolean, Boolean, List<Uri>, Boolean) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onMarkAsRead: () -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onEditMessage: ((Long, String) -> Unit)? = null,
    onToggleVote: (Long, String) -> Unit,
    onCloseVoting: (Long, String?, Double?, Int?, Double?, String?, String?) -> Unit,
    onDelete: () -> Unit,
    canPrioritize: Boolean = false,
    onToggleUrgent: (() -> Unit)? = null,
    onEditOccurrence: ((newApartment: String, newTitle: String, newDescription: String) -> Unit)? = null,
    onDefineBudget: (String, Double, Int, Double, String, String, Uri?) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    val occ = occWithMsgs.occurrence
    val messages = occWithMsgs.messages.sortedBy { it.message.id }
    val isCondo = occ.apartment == "CONDOMÍNIO"

    val isArchived = occ.status == "ARQUIVADA"
    val isDesarquivada = occ.title.contains("Desarquivada", ignoreCase = true)

    var replyText by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var isCouncilOnly by remember { mutableStateOf(false) }
    var isSindicoOnly by remember { mutableStateOf(false) }
    val selectedUris = remember { mutableStateListOf<Uri>() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        selectedUris.addAll(uris)
    }
    var showBudgetOptionChoiceDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showZeladorBudgetDialog by remember { mutableStateOf(false) }
    var showEditHeaderDialog by remember { mutableStateOf(false) }
    var showCloseConfirm by remember { mutableStateOf(false) }
    var messageWithOptions by remember { mutableStateOf<OccurrenceMessageEntity?>(null) }
    var messageToEdit by remember { mutableStateOf<OccurrenceMessageEntity?>(null) }
    var expanded by remember(occ.id, occ.status, occ.title, isCompact, initiallyExpanded) {
        mutableStateOf(initiallyExpanded || (if (isCompact) false else (!isArchived && isDesarquivada)))
    }

    LaunchedEffect(isCompact) {
        if (isCompact) {
            expanded = false
        }
    }

    val isAdmin = currentUser.role == "ADMIN" || currentUser.username.equals("admin", ignoreCase = true)
    val creatorUsername = occ.createdByUsername.ifBlank { messages.firstOrNull()?.message?.senderUsername ?: "" }
    val isCreator = creatorUsername.isNotBlank() && creatorUsername.equals(currentUser.username, ignoreCase = true)

    val creationMillis = remember(occ.id, occ.date, messages.firstOrNull()?.message?.id) {
        getOccurrenceCreationMillis(occ, messages)
    }
    val isWithinTwoHours = (System.currentTimeMillis() - creationMillis) <= (2 * 60 * 60 * 1000L)
    val canEditOccurrence = onEditOccurrence != null && (isAdmin || currentUser.canEditOccurrences || (isCreator && isWithinTwoHours))

    val unreadCount = messages.count { isMessageUnreadForUser(it.message, occ, currentUser, it.reads) }

    val lastMessage = messages.lastOrNull()?.message
    val lastMsgSender = lastMessage?.senderUsername ?: ""
    val lastMsgText = lastMessage?.text ?: ""

    val canSeeLastMsg = lastMessage?.let { msg ->
        when {
            msg.isSindicoOnly -> currentUser.role == "Síndico" || currentUser.role == "ADMIN" || msg.senderUsername == currentUser.username
            msg.isCouncilOnly -> currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN" || msg.senderUsername == currentUser.username
            msg.isBudget -> currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN" || currentUser.role == "Zelador" || msg.senderUsername == currentUser.username
            else -> true
        }
    } ?: true

    val lastMsgIsVoting = lastMessage?.senderUsername == "SISTEMA" && lastMessage.text.startsWith("Votação encerrada.")
    val canSeeVotingResult = currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN" || currentUser.role == "Zelador"

    val displayLastMsg = if (lastMessage == null) {
        ""
    } else if (!canSeeLastMsg) {
        if (lastMessage.isBudget && lastMessage.isVotingClosed && lastMessage.text.contains("Orçamento Aprovado", ignoreCase = true) && currentUser.role == "Zelador") {
            val lines = lastMessage.text.lines()
            val fornecedor = lines.firstOrNull { it.contains("Prestador/Fornecedor:") || it.contains("Orçamento:") }?.substringAfter(":")?.trim()
            val inicio = lines.firstOrNull { it.contains("Início:") }?.substringAfter(":")?.trim()
            val duracao = lines.firstOrNull { it.contains("Duração:") }?.substringAfter(":")?.trim()
            val parts = listOfNotNull(fornecedor, inicio, duracao).filter { it.isNotBlank() }
            if (parts.isNotEmpty()) {
                "✅ Serviço autorizado (${parts.joinToString(", ")})"
            } else {
                "✅ Serviço autorizado"
            }
        } else {
            "Mensagem sigilosa"
        }
    } else if (lastMessage.isBudget && lastMessage.isVotingClosed && lastMessage.text.contains("Orçamento Aprovado", ignoreCase = true) && currentUser.role == "Zelador" && lastMessage.senderUsername == currentUser.username) {
        "✅ Status: Orçamento Aprovado"
    } else if (lastMsgIsVoting && !canSeeVotingResult) {
        "Resultado da votação restrito ao Conselho Fiscal e Síndico."
    } else if (lastMsgIsVoting && currentUser.role == "Zelador") {
        val lines = lastMessage.text.lines()
        val fornecedor = lines.firstOrNull { it.contains("Prestador/Fornecedor:") || it.contains("Orçamento:") }?.substringAfter(":")?.trim()
        val inicio = lines.firstOrNull { it.contains("Início:") }?.substringAfter(":")?.trim()
        val duracao = lines.firstOrNull { it.contains("Duração:") }?.substringAfter(":")?.trim()
        val parts = listOfNotNull(fornecedor, inicio, duracao).filter { it.isNotBlank() }
        if (parts.isNotEmpty()) {
            "Votação encerrada (${parts.joinToString(", ")})"
        } else {
            "Votação encerrada"
        }
    } else {
        lastMsgText
    }

    LaunchedEffect(expanded, messages.size, unreadCount) {
        if (expanded && unreadCount > 0) {
            onMarkAsRead()
        }
    }

    if (showEditHeaderDialog && canEditOccurrence) {
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
        border = if (occ.isUrgent) BorderStroke(1.5.dp, Color.Red) else BorderStroke(1.5.dp, Color(0xFF2196F3)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (occ.isUrgent) 5.dp else 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                occ.isUrgent -> Color(0xFFFFF2F2)
                isCondo -> Color(0xFFE8F4FD)
                else -> Color(0xFFF7F7F7)
            }
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
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
                                Surface(
                                    color = Color.Red,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.NotificationImportant,
                                            contentDescription = "Urgente",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "URGENTE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            if (occ.type == "CONSELHO") {
                                Icon(Icons.Default.Gavel, contentDescription = "Conselho", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (occ.title.isNotBlank()) {
                            Text(
                                text = occ.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (occ.title.endsWith("(Desarquivada)")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = if (isCondo) "🏢 CONDOMÍNIO" else "Unidade: ${occ.apartment}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isCondo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (canPrioritize && onToggleUrgent != null) {
                                IconButton(
                                    onClick = { onToggleUrgent() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.NotificationImportant,
                                        contentDescription = if (occ.isUrgent) "Remover Urgência" else "Priorizar Chamada",
                                        tint = if (occ.isUrgent) Color.Red else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else if (occ.isUrgent) {
                                Icon(
                                    imageVector = Icons.Default.NotificationImportant,
                                    contentDescription = "Urgente",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (canEditOccurrence) {
                                IconButton(
                                    onClick = { showEditHeaderDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar Ocorrência",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

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

            if (!expanded && displayLastMsg.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (lastMessage != null && lastMessage.senderUsername.isNotBlank() && lastMessage.senderUsername != "SISTEMA") {
                                "${lastMessage.senderUsername}: $displayLastMsg"
                            } else {
                                displayLastMsg
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
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
                            msg.isBudget -> currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN" || currentUser.role == "Zelador" || msg.senderUsername == currentUser.username
                            else -> true
                        }

                        val isVotingResult = msg.senderUsername == "SISTEMA" && msg.text.startsWith("Votação encerrada.")
                        val canSeeVotingResult = currentUser.role == "Síndico" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN" || currentUser.role == "Zelador"

                        val displayMsg = if (!canSeeSecret) {
                            if (msg.isBudget && msg.isVotingClosed && msg.text.contains("Orçamento Aprovado", ignoreCase = true) && currentUser.role == "Zelador") {
                                val lines = msg.text.lines()
                                val fornecedor = lines.firstOrNull { it.contains("Prestador/Fornecedor:") || it.contains("Orçamento:") }
                                val inicio = lines.firstOrNull { it.contains("Início:") }
                                val duracao = lines.firstOrNull { it.contains("Duração:") }
                                val filtered = listOfNotNull("✅ Serviço autorizado", "", fornecedor, inicio, duracao)
                                filtered.joinToString("\n")
                            } else {
                                "Mensagem sigilosa"
                            }
                        } else if (msg.isBudget && msg.isVotingClosed && msg.text.contains("Orçamento Aprovado", ignoreCase = true) && currentUser.role == "Zelador" && msg.senderUsername == currentUser.username) {
                            val lines = msg.text.lines()
                            val status = lines.firstOrNull { it.contains("Status:") } ?: "✅ Status: Orçamento Aprovado"
                            val fornecedor = lines.firstOrNull { it.contains("Prestador/Fornecedor:") || it.contains("Orçamento:") }
                            val valor = lines.firstOrNull { it.contains("Valor Total:") }
                            val inicio = lines.firstOrNull { it.contains("Início:") }
                            val duracao = lines.firstOrNull { it.contains("Duração:") }
                            val filtered = listOfNotNull(status, "", fornecedor, valor, inicio, duracao)
                            filtered.joinToString("\n")
                        } else if (isVotingResult && !canSeeVotingResult) {
                            "Resultado da votação restrito ao Conselho Fiscal e Síndico."
                        } else if (isVotingResult && currentUser.role == "Zelador") {
                            val lines = msg.text.lines()
                            val header = lines.firstOrNull { it.contains("Votação encerrada.") } ?: "Votação encerrada."
                            val status = lines.firstOrNull { it.contains("Orçamento Aprovado", ignoreCase = true) }
                            val fornecedor = lines.firstOrNull { it.contains("Prestador/Fornecedor:") || it.contains("Orçamento:") }
                            val inicio = lines.firstOrNull { it.contains("Início:") }
                            val duracao = lines.firstOrNull { it.contains("Duração:") }
                            val filtered = listOfNotNull(status, fornecedor, inicio, duracao)
                            if (filtered.isNotEmpty()) {
                                "$header\n\n${filtered.joinToString("\n")}"
                            } else {
                                header
                            }
                        } else {
                            msg.text
                        }

                        val senderRole: String = when {
                            msg.senderUsername.equals("SISTEMA", ignoreCase = true) -> ""
                            msg.senderUsername.equals(currentUser.username, ignoreCase = true) -> currentUser.role
                            msg.senderUsername.equals("admin", ignoreCase = true) -> "ADMIN"
                            else -> userRoleMap[msg.senderUsername.lowercase()] ?: ""
                        }

                        val displayAttachments = if (!canSeeSecret) emptyList() else msgWithAtts.attachments

                        ChatBubble(
                            text = displayMsg,
                            author = msg.senderUsername,
                            date = msg.date,
                            role = senderRole,
                            isFromMe = msg.senderUsername == currentUser.username || (msg.senderUsername == "admin" && currentUser.role == "ADMIN"),
                            isConfidential = msg.isCouncilOnly,
                            isSindicoOnly = msg.isSindicoOnly,
                            isRead = msg.isRead,
                            isVotingClosed = msg.isVotingClosed,
                            isBudget = msg.isBudget,
                            isEdited = msg.isEdited,
                            attachments = displayAttachments,
                            reads = msgWithAtts.reads,
                            isSelected = selectedMessage?.id == msg.id || messageWithOptions?.id == msg.id,
                            onLongClick = {
                                messageWithOptions = msg
                                onMessageSelected(msg)
                            },
                            currentUser = currentUser,
                            onToggleVote = onToggleVote,
                            onCloseVoting = { name, total, count, instValue, start, dur ->
                                onCloseVoting(msg.id, name, total, count, instValue, start, dur)
                            },
                            userRoleMap = userRoleMap
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                val canReply = occ.status != "FINALIZADA" && occ.status != "ARQUIVADA" && currentUser.role != "Porteiro" && currentUser.role != "Zelador"
                val canZeladorReply = occ.status != "FINALIZADA" && occ.status != "ARQUIVADA" && currentUser.role == "Zelador" && occ.type == "GERAL"

                if (canReply || canZeladorReply) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .imePadding()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = { Text("Digite sua mensagem...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                minLines = 1,
                                maxLines = 4,
                                shape = RoundedCornerShape(24.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { fileLauncher.launch(arrayOf("*/*")) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AttachFile,
                                            contentDescription = "Anexar arquivos",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )

                            IconButton(
                                onClick = {
                                    if (replyText.isNotBlank() || selectedUris.isNotEmpty()) {
                                        val hasMedia = selectedUris.any {
                                            val fName = getFileName(context, it)
                                            isImageFile(fName) || isPdfFile(fName)
                                        }
                                        if (hasMedia) {
                                            isUploading = true
                                        }
                                        val urisToSend = selectedUris.toList()
                                        onReply(replyText, isCouncilOnly, isSindicoOnly, urisToSend, false)
                                        replyText = ""
                                        selectedUris.clear()
                                        if (hasMedia) {
                                            scope.launch {
                                                kotlinx.coroutines.delay(2500)
                                                isUploading = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(40.dp),
                                enabled = !isUploading && (replyText.isNotBlank() || selectedUris.isNotEmpty())
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Enviar",
                                    tint = if (replyText.isNotBlank() || selectedUris.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }

                        if (currentUser.role == "Síndico" || currentUser.role == "ADMIN" || (currentUser.role == "Conselheiro Fiscal" && occ.type == "CONSELHO")) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (currentUser.role == "Síndico" || currentUser.role == "ADMIN") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isCouncilOnly, onCheckedChange = { isCouncilOnly = it; if(it) isSindicoOnly = false })
                                        Text("Apenas Conselho", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                if (currentUser.role == "Conselheiro Fiscal" || currentUser.role == "ADMIN") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isSindicoOnly, onCheckedChange = { isSindicoOnly = it; if(it) isCouncilOnly = false })
                                        Text("Apenas Síndico", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        if (selectedUris.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text("Anexos (${selectedUris.size}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                selectedUris.forEach { uri ->
                                    val fName = getFileName(context, uri)
                                    val isVid = isVideoFile(fName, uri.toString())
                                    val isImg = isImageFile(fName)
                                    val isPdf = isPdfFile(fName)
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                        Icon(
                                            if (isImg) Icons.Default.Image else if (isVid) Icons.Default.Videocam else if (isPdf) Icons.Default.PictureAsPdf else Icons.AutoMirrored.Filled.InsertDriveFile,
                                            null,
                                            Modifier.size(16.dp),
                                            tint = if (isImg || isPdf) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(fName, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { selectedUris.removeIf { it == uri } }, modifier = Modifier.size(24.dp), enabled = !isUploading) {
                                            Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }

                        if (isUploading) {
                            Spacer(Modifier.height(4.dp))
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                Text(
                                    text = "Enviando imagem/PDF...",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primaryContainer
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (canPrioritize && onToggleUrgent != null && occ.status != "ARQUIVADA") {
                            if (occ.isUrgent) {
                                OutlinedButton(
                                    onClick = { onToggleUrgent() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    border = BorderStroke(1.dp, Color.Red),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.NotificationImportant, null, Modifier.size(14.dp), tint = Color.Red)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Remover Urgência", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onToggleUrgent() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.NotificationImportant, null, Modifier.size(14.dp), tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Priorizar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (currentUser.role == "Síndico" || currentUser.role == "ADMIN" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "Zelador") {
                            if (occ.status == "ARQUIVADA") {
                                TextButton(onClick = { onUpdateStatus("DESARQUIVAR") }) {
                                    Icon(Icons.Default.Unarchive, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Desarquivar")
                                }
                            } else if (occ.status != "FINALIZADA") {
                                if (currentUser.role == "Síndico" || currentUser.role == "ADMIN" || currentUser.role == "Conselheiro Fiscal" || currentUser.role == "Zelador") {
                                    ProfessionalBudgetActionButton(onClick = {
                                        showBudgetOptionChoiceDialog = true
                                    })
                                }
                            } else {
                                TextButton(onClick = { onUpdateStatus("ABERTA") }) {
                                    Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reabrir")
                                }
                                TextButton(onClick = { onUpdateStatus("ARQUIVADA") }) {
                                    Icon(Icons.Default.Archive, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Arquivar")
                                }
                            }
                        }
                    }

                    if (currentUser.role == "Síndico" || currentUser.role == "ADMIN" || currentUser.role == "Conselheiro Fiscal") {
                        if (occ.status != "FINALIZADA" && occ.status != "ARQUIVADA") {
                            TextButton(onClick = { showCloseConfirm = true }) {
                                Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Fechar")
                            }
                        }
                    }
                }
            }
        }
    }



    if (showBudgetOptionChoiceDialog) {
        BudgetOptionChoiceDialog(
            onDismiss = { showBudgetOptionChoiceDialog = false },
            onSelectSingle = { showZeladorBudgetDialog = true },
            onSelectMultiple = { showBudgetDialog = true }
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

    if (showZeladorBudgetDialog) {
        CloseVotingDialog(
            attachments = emptyList<AttachmentWithVotes>(),
            isNameReadOnly = false,
            showDocumentPicker = true,
            onDismiss = { showZeladorBudgetDialog = false },
            onConfirm = { name, total, count, instValue, start, dur, attachedUri ->
                showZeladorBudgetDialog = false
                onDefineBudget(name, total, count, instValue, start, dur, attachedUri)
            }
        )
    }

    if (showCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm = false },
            title = { Text("Fechar Ocorrência") },
            text = { Text("Deseja mesmo fechar a ocorrência \"${occ.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        showCloseConfirm = false
                        onUpdateStatus("FINALIZADA")
                    }
                ) {
                    Text("Fechar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (messageWithOptions != null) {
        val msg = messageWithOptions!!
        val isAdmin = currentUser.role == "ADMIN" || currentUser.username.equals("admin", ignoreCase = true)
        val isSender = msg.senderUsername.equals(currentUser.username, ignoreCase = true) ||
                (msg.senderUsername.equals("admin", ignoreCase = true) && currentUser.role == "ADMIN")
        val creationTime = if (msg.timestamp > 0) msg.timestamp else msg.id
        val elapsed = System.currentTimeMillis() - creationTime
        val isWithin20Min = elapsed in 0..(20 * 60 * 1000L)
        val canEdit = isSender && isWithin20Min && occ.status != "FINALIZADA" && occ.status != "ARQUIVADA" && msg.senderUsername != "SISTEMA"
        val canDelete = if (msg.senderUsername == "SISTEMA") isAdmin else (isAdmin || (isSender && isWithin20Min))

        val remainingMinutes = if (isWithin20Min) {
            val rem = ((20 * 60 * 1000L) - elapsed) / 60000L + 1
            if (rem > 20) 20 else rem
        } else 0

        AlertDialog(
            onDismissRequest = {
                messageWithOptions = null
                onMessageSelected(null)
            },
            title = { Text("Opções da Mensagem") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isSender && !isAdmin) {
                        if (isWithin20Min) {
                            Text(
                                text = "⏱️ Tempo restante para editar ou remover: $remainingMinutes min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Não é mais possível editar ou remover esta mensagem pois o prazo de 20 minutos expirou.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (canEdit) {
                        Button(
                            onClick = {
                                messageToEdit = msg
                                messageWithOptions = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Editar Mensagem")
                        }
                    }
                    if (canDelete) {
                        OutlinedButton(
                            onClick = {
                                onDeleteMessage(msg.id)
                                messageWithOptions = null
                                onMessageSelected(null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Excluir Mensagem")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        messageWithOptions = null
                        onMessageSelected(null)
                    }
                ) { Text("Cancelar") }
            }
        )
    }

    if (messageToEdit != null) {
        var editedText by remember(messageToEdit) { mutableStateOf(messageToEdit!!.text) }
        AlertDialog(
            onDismissRequest = {
                messageToEdit = null
                onMessageSelected(null)
            },
            title = { Text("Editar Mensagem") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Você pode editar sua mensagem dentro de até 20 minutos do envio.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = editedText,
                        onValueChange = { editedText = it },
                        label = { Text("Mensagem") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedText.isNotBlank()) {
                            onEditMessage?.invoke(messageToEdit!!.id, editedText.trim())
                            messageToEdit = null
                            onMessageSelected(null)
                        }
                    },
                    enabled = editedText.isNotBlank()
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        messageToEdit = null
                        onMessageSelected(null)
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterOccurrenceDialog(
    currentUser: UserEntity, 
    dao: AppDao, 
    initialDestination: String? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, String, Boolean, List<Uri>, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var apt by remember { mutableStateOf("") }
    var isCondo by remember { mutableStateOf(true) }
    var isUrgent by remember { mutableStateOf(false) }
    var showUrgentConfirmDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    
    var floor by remember { mutableStateOf("T") }
    var valueStr by remember { mutableStateOf("") }
    var isInstallment by remember { mutableStateOf(false) }
    var installmentsCountStr by remember { mutableStateOf("1") }

    val isSuperAdmin = isSuperAdminUser(currentUser)

    val defaultDest = initialDestination ?: when {
        currentUser.role == "Conselheiro Fiscal" -> "CONSELHO"
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

                if (currentUser.role != "Porteiro" && currentUser.role != "Zelador" || isSuperAdmin) {
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
                            if (isSuperAdmin) {
                                Spacer(Modifier.width(8.dp))
                                FilterChip(
                                    selected = destination == "ADMIN",
                                    onClick = { destination = "ADMIN" },
                                    label = { Text("Admin", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = if (destination == "ADMIN") { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
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
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (!isUrgent) {
                            showUrgentConfirmDialog = true
                        } else {
                            isUrgent = false
                        }
                    }
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isUrgent,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showUrgentConfirmDialog = true
                                } else {
                                    isUrgent = false
                                }
                            }
                        )
                        Column {
                            Text("Urgente", fontWeight = FontWeight.Bold, color = if (isUrgent) Color.Red else Color.Unspecified)
                            Text("Priorizar esta ocorrência (requer confirmação)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }

                if (showUrgentConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showUrgentConfirmDialog = false },
                        icon = {
                            Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = Color.Red)
                        },
                        title = { Text("Confirmar Marcação de Urgência") },
                        text = {
                            Text("Deseja destacar esta ocorrência como URGENTE ao cadastrar? Chamadas urgentes têm prioridade máxima.")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    isUrgent = true
                                    showUrgentConfirmDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Confirmar Urgência")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showUrgentConfirmDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
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
                                val fName = getFileName(context, uri)
                                val isVid = isVideoFile(fName, uri.toString())
                                val isImg = isImageFile(fName)
                                val isPdf = isPdfFile(fName)
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isImg) Icons.Default.Image else if (isVid) Icons.Default.Videocam else if (isPdf) Icons.Default.PictureAsPdf else Icons.AutoMirrored.Filled.InsertDriveFile,
                                            null,
                                            Modifier.size(20.dp),
                                            tint = if (isImg || isPdf) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(fName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { selectedUris.removeIf { it == uri } }, modifier = Modifier.size(32.dp), enabled = !isUploading) {
                                            Icon(Icons.Default.Close, null, Modifier.size(18.dp), tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isUploading) {
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Enviando imagem/PDF...",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
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
                                val hasMedia = selectedUris.any {
                                    val fName = getFileName(context, it)
                                    isImageFile(fName) || isPdfFile(fName)
                                }
                                if (hasMedia) {
                                    isUploading = true
                                }
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
                                if (hasMedia) {
                                    scope.launch {
                                        kotlinx.coroutines.delay(2500)
                                        isUploading = false
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isUploading && title.isNotBlank() && desc.isNotBlank() && (isCondo || apt.isNotBlank()),
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
