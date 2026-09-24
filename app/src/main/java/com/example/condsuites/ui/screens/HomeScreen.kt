package com.example.condsuites.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.OccurrenceEntity
import com.example.condsuites.data.model.OccurrenceMessageEntity
import com.example.condsuites.data.model.OccurrenceAttachmentEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.service.NotificationUtils.sendFcmPushNotification
import com.example.condsuites.ui.components.SummaryCard
import com.example.condsuites.ui.navigation.Screen
import com.example.condsuites.ui.screens.RegisterOccurrenceDialog
import com.example.condsuites.utils.getFileName
import com.example.condsuites.utils.uploadImageToCloudinary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope, onNavigate: (Screen) -> Unit) {
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())
    var showAddOccurrenceDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val filteredOccurrences = occurrences.filter { 
        if (currentUser.role == "Zelador") {
            it.occurrence.type == "GERAL"
        } else {
            true
        }
    }
    
    val openOccurrences = filteredOccurrences.filter { it.occurrence.status == "ABERTA" }
    val finishedOccurrences = filteredOccurrences.filter { it.occurrence.status == "FINALIZADA" }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Painel Administrativo",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Olá, ${currentUser.username.uppercase()} • ${currentUser.role}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Data: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Dashboard, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            val canAccessFinanceHome = currentUser.role in listOf("ADMIN", "Síndico", "Conselheiro Fiscal")
            if (canAccessFinanceHome) {
                val pendingTransactions by dao.getPendingTransactionsFlow().collectAsState(initial = emptyList())
                val today = Date()
                val (overdue, dueSoon) = pendingTransactions.filter { it.type == "PAYABLE" }.partition { tx ->
                    try {
                        val parsed = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(tx.dueDate)
                        parsed != null && parsed.before(today)
                    } catch (e: Exception) { false }
                }
                
                if (overdue.isNotEmpty() || dueSoon.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigate(Screen.Accounts) },
                        colors = CardDefaults.cardColors(containerColor = if (overdue.isNotEmpty()) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = if (overdue.isNotEmpty()) Color.Red else Color(0xFFF57C00), modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Atenção Financeira", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (overdue.isNotEmpty()) Color.Red else Color(0xFFF57C00))
                                if (overdue.isNotEmpty()) {
                                    Text("${overdue.size} conta(s) a pagar VENCIDA(S)!", style = MaterialTheme.typography.bodyMedium, color = Color.Red)
                                }
                                if (dueSoon.isNotEmpty()) {
                                    Text("${dueSoon.size} conta(s) a pagar pendentes.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }

            Text("Resumo de Ocorrências e Chamados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard(
                    title = "Abertas",
                    count = openOccurrences.size,
                    color = Color(0xFF2196F3),
                    icon = Icons.Default.ChatBubbleOutline,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.OccurrencesListNav) }
                )
                SummaryCard(
                    title = "Finalizadas",
                    count = finishedOccurrences.size,
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.OccurrencesListNav) }
                )
            }

            Text("Ações Rápidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeQuickActionButton(
                    title = "Nova Ocorrência",
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f),
                    onClick = { showAddOccurrenceDialog = true }
                )
                HomeQuickActionButton(
                    title = "Lançar Horas",
                    icon = Icons.Default.AccessTime,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Overtime) }
                )
                HomeQuickActionButton(
                    title = "Unidades",
                    icon = Icons.Default.Apartment,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.UnitsRegistry) }
                )
            }
        
            if (openOccurrences.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("Últimas Ocorrências Abertas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                openOccurrences.take(3).forEach { occWithMsgs ->
                    val occ = occWithMsgs.occurrence
                    val isCondo = occ.apartment == "CONDOMÍNIO"
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onNavigate(Screen.Occurrences) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCondo) Color(0xFFE8F4FD) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isCondo) Icons.Default.Apartment else Icons.Default.NotificationImportant, null, tint = Color(0xFF2196F3))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(occ.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text(if (isCondo) "🏢 CONDOMÍNIO • ${occ.date}" else "🚪 Unidade ${occ.apartment} • ${occ.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(70.dp))
        }

        FloatingActionButton(
            onClick = { showAddOccurrenceDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Abrir Ocorrência")
                Text("Abrir Ocorrência", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddOccurrenceDialog) {
        RegisterOccurrenceDialog(
            currentUser = currentUser,
            dao = dao,
            onDismiss = { showAddOccurrenceDialog = false },
            onConfirm = { title, desc, msgText, apt, isUrgent, uris, dest ->
                scope.launch(Dispatchers.IO) {
                    val occId = System.currentTimeMillis()
                    val msgId = occId + 1
                    val newOcc = OccurrenceEntity(
                        id = occId,
                        title = title,
                        description = desc,
                        apartment = apt,
                        status = "ABERTA",
                        createdByUsername = currentUser.username,
                        date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                        type = dest,
                        isUrgent = isUrgent
                    )
                    dao.insertOccurrenceReplace(newOcc)
                    sendFcmPushNotification(
                        dao = dao,
                        title = "Nova Ocorrência: $title",
                        body = if (msgText.isNotBlank()) "Unidade $apt: $msgText" else "Unidade $apt: $desc",
                        senderUsername = currentUser.username,
                        occurrenceId = occId
                    )
                    FirestoreSyncManager.syncOccurrence(newOcc)

                    if (msgText.isNotBlank()) {
                        val firstMsg = OccurrenceMessageEntity(
                            id = msgId,
                            occurrenceId = occId,
                            senderUsername = currentUser.username,
                            text = msgText,
                            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                        )
                        dao.insertOccurrenceMessageReplace(firstMsg)
                        FirestoreSyncManager.syncOccurrenceMessage(firstMsg)
                    }

                    uris.forEachIndexed { index, uri ->
                        uploadImageToCloudinary(context, uri) { path ->
                            if (path != null) {
                                scope.launch(Dispatchers.IO) {
                                    val attId = occId + 2 + index
                                    val att = OccurrenceAttachmentEntity(
                                        id = attId,
                                        occurrenceId = occId,
                                        messageId = if (msgText.isNotBlank()) msgId else 0,
                                        fileName = getFileName(context, uri),
                                        filePath = path
                                    )
                                    dao.insertAttachmentReplace(att)
                                    FirestoreSyncManager.syncAttachment(att)
                                }
                            }
                        }
                    }
                }
                showAddOccurrenceDialog = false
            }
        )
    }
}

@Composable
fun HomeQuickActionButton(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
        }
    }
}
