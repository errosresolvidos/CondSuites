package com.example.condsuites.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.OvertimeEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.service.NotificationUtils.sendFcmPushNotification
import com.example.condsuites.ui.components.OvertimeMetricCard
import com.example.condsuites.ui.screens.reports.generateOvertimeHtmlReport
import com.example.condsuites.ui.screens.reports.generateOvertimeTextReport
import com.example.condsuites.utils.calculateOvertimeHours
import com.example.condsuites.utils.isOvertimeEditable
import com.example.condsuites.utils.printHtmlReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OvertimeManagementScreen(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope) {
    val overtimes by dao.getAllOvertimeFlow().collectAsState(initial = emptyList())
    val context = LocalContext.current

    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }

    var showAddDialog by remember { mutableStateOf(false) }
    var overtimeToEdit by remember { mutableStateOf<OvertimeEntity?>(null) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }

    val filteredList = remember(overtimes, searchText, selectedStatusFilter) {
        overtimes.filter { item ->
            val matchesStatus = when (selectedStatusFilter) {
                "Pendentes" -> item.status == "PENDENTE"
                "Aprovados" -> item.status == "APROVADO"
                "Pagos" -> item.status == "PAGO"
                "Rejeitados" -> item.status == "REJEITADO"
                else -> true
            }

            val fullText = "${item.employeeName} ${item.employeeRole} ${item.reason} ${item.date} ${item.status}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesStatus && matchesSearch
        }
    }

    val totalCount = filteredList.size
    val totalHours = filteredList.sumOf { it.totalHours }
    val pendingHours = filteredList.filter { it.status == "PENDENTE" }.sumOf { it.totalHours }
    val approvedHours = filteredList.filter { it.status == "APROVADO" || it.status == "PAGO" }.sumOf { it.totalHours }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Painel de Controle de Horas Extras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OvertimeMetricCard("Lançamentos", totalCount.toString(), Icons.Default.AccessTime, Modifier.weight(1f))
                        OvertimeMetricCard("Total Horas", "${String.format(Locale.getDefault(), "%.1f", totalHours)}h", Icons.Default.AccessTime, Modifier.weight(1f))
                        OvertimeMetricCard("Pendentes", "${String.format(Locale.getDefault(), "%.1f", pendingHours)}h", Icons.Default.NotificationImportant, Modifier.weight(1f))
                        OvertimeMetricCard("Aprovadas", "${String.format(Locale.getDefault(), "%.1f", approvedHours)}h", Icons.Default.CheckCircle, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por nome, cargo ou motivo...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = selectedStatusFilter == "Todos", onClick = { selectedStatusFilter = "Todos" }, label = { Text("Todos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Pendentes", onClick = { selectedStatusFilter = "Pendentes" }, label = { Text("Pendentes", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Aprovados", onClick = { selectedStatusFilter = "Aprovados" }, label = { Text("Aprovados", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Pagos", onClick = { selectedStatusFilter = "Pagos" }, label = { Text("Pagos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Lançar Hora Extra", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = { showPrintPreviewDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Imprimir PDF", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            Text("Lançamentos de Horas Extras (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum lançamento de hora extra encontrado.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList) { item ->
                OvertimeCard(
                    item = item,
                    currentUser = currentUser,
                    onEdit = { overtimeToEdit = item },
                    onDelete = {
                        scope.launch(Dispatchers.IO) {
                            dao.deleteOvertime(item.id)
                            FirestoreSyncManager.syncOvertime(item, isDelete = true)
                        }
                    },
                    onStatusChange = { newStatus ->
                        scope.launch(Dispatchers.IO) {
                            val updated = item.copy(status = newStatus, approvedByUsername = currentUser.username)
                            dao.updateOvertime(updated)
                            FirestoreSyncManager.syncOvertime(updated)
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        RegisterOvertimeDialog(
            currentUser = currentUser,
            onDismiss = { showAddDialog = false },
            onConfirm = { newOvertime ->
                scope.launch(Dispatchers.IO) {
                    dao.insertOvertime(newOvertime)
                    FirestoreSyncManager.syncOvertime(newOvertime)
                    sendFcmPushNotification(
                        dao = dao,
                        title = "Nova Hora Extra Lançada",
                        body = "${newOvertime.employeeName} (${newOvertime.employeeRole}) - ${newOvertime.date} (${newOvertime.startTime} às ${newOvertime.endTime}, ${String.format(Locale.getDefault(), "%.1f", newOvertime.totalHours)}h)",
                        senderUsername = currentUser.username,
                        occurrenceId = newOvertime.id
                    )
                }
                showAddDialog = false
            }
        )
    }

    if (overtimeToEdit != null) {
        EditOvertimeDialog(
            overtime = overtimeToEdit!!,
            currentUser = currentUser,
            onDismiss = { overtimeToEdit = null },
            onConfirm = { updated ->
                scope.launch(Dispatchers.IO) {
                    dao.updateOvertime(updated)
                    FirestoreSyncManager.syncOvertime(updated)
                }
                overtimeToEdit = null
            }
        )
    }

    if (showPrintPreviewDialog) {
        val htmlContent = generateOvertimeHtmlReport(filteredList, selectedStatusFilter, "Geral")
        val textReport = generateOvertimeTextReport(filteredList, selectedStatusFilter, "Geral")

        OvertimeReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredList.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Relatorio_Horas_Extras_CondSuites")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Horas Extras"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun OvertimeCard(
    item: OvertimeEntity,
    currentUser: UserEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    val (statusText, statusColor) = when (item.status) {
        "APROVADO" -> "APROVADO ✅" to Color(0xFF2E7D32)
        "PAGO" -> "PAGO 💰" to Color(0xFF0288D1)
        "REJEITADO" -> "REJEITADO ❌" to Color(0xFFC62828)
        else -> "PENDENTE ⏳" to Color(0xFFE65100)
    }

    val isAdmin = currentUser.role == "ADMIN"
    val isSindico = currentUser.role == "Síndico"
    val canApprove = isAdmin || isSindico
    val isWithin15Min = isOvertimeEditable(item.registrationDate)

    val canEditOvertime = when {
        isAdmin -> true
        isSindico -> false
        else -> isWithin15Min
    }

    val canDeleteOvertime = when {
        isAdmin -> true
        isSindico -> false
        else -> isWithin15Min
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.employeeName.uppercase(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                        Text(item.employeeRole, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text(statusText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("📅 Data: ${item.date} • Horário: ${item.startTime} às ${item.endTime} (${String.format(Locale.getDefault(), "%.1f", item.totalHours)}h)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text("Motivo da Hora Extra:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(item.reason, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (item.approvedByUsername.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Aprovado/Atualizado por: ${item.approvedByUsername}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                if (canApprove) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (item.status != "APROVADO") {
                            TextButton(onClick = { onStatusChange("APROVADO") }) {
                                Text("Aprovar", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                            }
                        }
                        if (item.status != "REJEITADO") {
                            TextButton(onClick = { onStatusChange("REJEITADO") }) {
                                Text("Rejeitar", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (canEditOvertime) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (canDeleteOvertime) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterOvertimeDialog(
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onConfirm: (OvertimeEntity) -> Unit
) {
    var employeeName by remember { mutableStateOf(currentUser.username) }
    var employeeRole by remember { mutableStateOf(if (currentUser.role.isNotBlank()) currentUser.role else "Zelador") }
    var dateText by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var startTime by remember { mutableStateOf("18:00") }
    var endTime by remember { mutableStateOf("21:00") }
    var reason by remember { mutableStateOf("") }
    var expandedRole by remember { mutableStateOf(false) }

    val roles = listOf("Zelador", "Porteiro", "Manutenção", "Limpeza", "Administração", "Síndico")
    val totalHours = calculateOvertimeHours(startTime, endTime)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lançar Hora Extra") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = employeeName,
                    onValueChange = { employeeName = it },
                    label = { Text("Nome do Colaborador *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = !expandedRole },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = employeeRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cargo / Função *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { employeeRole = r; expandedRole = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Data (DD/MM/AAAA) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Início (ex: 18:00) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Fim (ex: 21:00) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Total Calculado: ${String.format(Locale.getDefault(), "%.1f", totalHours)} hora(s)",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo Detalhado da Hora Extra *") },
                    placeholder = { Text("Ex: Acompanhamento emergencial da equipe técnica de manutenção do elevador social no período noturno...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (employeeName.isNotBlank() && reason.isNotBlank()) {
                        onConfirm(
                            OvertimeEntity(
                                id = System.currentTimeMillis(),
                                employeeName = employeeName.trim(),
                                employeeRole = employeeRole.trim(),
                                date = dateText.trim(),
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                totalHours = totalHours,
                                reason = reason.trim(),
                                status = "PENDENTE",
                                approvedByUsername = ""
                            )
                        )
                    }
                },
                enabled = employeeName.isNotBlank() && reason.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar Lançamento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOvertimeDialog(
    overtime: OvertimeEntity,
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onConfirm: (OvertimeEntity) -> Unit
) {
    var employeeName by remember { mutableStateOf(overtime.employeeName) }
    var employeeRole by remember { mutableStateOf(overtime.employeeRole) }
    var dateText by remember { mutableStateOf(overtime.date) }
    var startTime by remember { mutableStateOf(overtime.startTime) }
    var endTime by remember { mutableStateOf(overtime.endTime) }
    var reason by remember { mutableStateOf(overtime.reason) }
    var status by remember { mutableStateOf(overtime.status) }
    var expandedRole by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    val roles = listOf("Zelador", "Porteiro", "Manutenção", "Limpeza", "Administração", "Síndico")
    val statuses = listOf("PENDENTE", "APROVADO", "PAGO", "REJEITADO")
    val totalHours = calculateOvertimeHours(startTime, endTime)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Hora Extra") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = employeeName,
                    onValueChange = { employeeName = it },
                    label = { Text("Nome do Colaborador *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = !expandedRole },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = employeeRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cargo / Função *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { employeeRole = r; expandedRole = false })
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Início *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Fim *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status do Lançamento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        statuses.forEach { st ->
                            DropdownMenuItem(text = { Text(st) }, onClick = { status = st; expandedStatus = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo Detalhado *") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        overtime.copy(
                            employeeName = employeeName.trim(),
                            employeeRole = employeeRole.trim(),
                            date = dateText.trim(),
                            startTime = startTime.trim(),
                            endTime = endTime.trim(),
                            totalHours = totalHours,
                            reason = reason.trim(),
                            status = status,
                            approvedByUsername = currentUser.username
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun OvertimeReportPreviewDialog(
    htmlContent: String,
    textReport: String,
    totalCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pré-visualização do Relatório de Horas Extras") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Relatório gerado com $totalCount registro(s). Escolha a forma de exportação:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = textReport,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onShareText, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Texto", style = MaterialTheme.typography.labelSmall)
                }
                Button(onClick = onPrintPdf, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Imprimir PDF", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}
