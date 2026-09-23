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
import com.example.condsuites.data.model.ServiceInstallmentEntity
import com.example.condsuites.data.model.ServiceOrderEntity
import com.example.condsuites.data.model.ServiceOrderWithInstallments
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.ui.components.ElevatorMetricCard
import com.example.condsuites.ui.screens.reports.shareServiceOrderReport
import com.example.condsuites.utils.CurrencyVisualTransformation
import com.example.condsuites.utils.calculateMonthlyDates
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.printHtmlReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateServiceOrdersHtmlReport(orders: List<ServiceOrderWithInstallments>, filterText: String): String {
    val totalVal = orders.sumOf { it.order.totalValue }
    val totalPaid = orders.flatMap { it.installments }.filter { it.isPaid }.sumOf { it.value }
    val totalRemaining = orders.flatMap { it.installments }.filter { !it.isPaid }.sumOf { it.value }
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório de Ordens de Serviço - Elevadores</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #1565C0; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #1565C0; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #E3F2FD; border: 1px solid #BBDEFB; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #1565C0; font-weight: bold; }
                .kpi-value { font-size: 16px; font-weight: bold; color: #0D47A1; margin-top: 4px; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #1565C0; color: white; padding: 7px; text-align: left; }
                table.data-table td { border-bottom: 1px solid #DDD; padding: 7px; vertical-align: top; }
                table.data-table tr:nth-child(even) { background: #F9F9F9; }
                .signatures { margin-top: 40px; width: 100%; page-break-inside: avoid; }
                .sig-box { width: 45%; display: inline-block; text-align: center; font-size: 11px; margin-top: 20px; }
                .sig-line { border-top: 1px solid #333; margin-bottom: 4px; width: 80%; margin-left: auto; margin-right: auto; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🏢 CONDSUITES - GESTÃO CONDOMINIAL</h1>
                <h2>Relatório de Manutenção & Ordens de Serviço de Elevadores</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Filtro:</strong> $filterText &nbsp;|&nbsp;
                <strong>Total Ordens:</strong> ${orders.size}
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Ordens</div>
                    <div class="kpi-value">${orders.size}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Valor Total</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalVal)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Valor Pago</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalPaid)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Saldo Pendente</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalRemaining)}</div>
                </div>
            </div>

            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 6%;"># OS</th>
                        <th style="width: 14%;">Elevador</th>
                        <th style="width: 12%;">Data</th>
                        <th style="width: 10%;">Andar</th>
                        <th style="width: 34%;">Descrição dos Serviços / Peças</th>
                        <th style="width: 12%;">Valor Total</th>
                        <th style="width: 12%;">Condição</th>
                    </tr>
                </thead>
                <tbody>
    """.trimIndent())

    orders.forEach { item ->
        val ord = item.order
        val cond = if (ord.isInstallment) "${item.installments.size}x Parcela(s)" else "À Vista"
        html.append("""
            <tr>
                <td>#${ord.id}</td>
                <td><strong>${ord.type.uppercase()}</strong></td>
                <td>${ord.date}</td>
                <td>${ord.floor}º Andar</td>
                <td>${ord.description}</td>
                <td><strong>R$ ${formatCurrency(ord.totalValue)}</strong></td>
                <td>$cond</td>
            </tr>
        """.trimIndent())
    }

    html.append("""
            </tbody>
        </table>

        <div class="signatures">
            <div class="sig-box">
                <div class="sig-line"></div>
                <strong>Síndico / Administração</strong><br>
                Condomínio CondSuites
            </div>
            <div class="sig-box" style="float: right;">
                <div class="sig-line"></div>
                <strong>Empresa de Manutenção de Elevadores</strong><br>
                Responsável Técnico
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateServiceOrdersTextReport(orders: List<ServiceOrderWithInstallments>, filterText: String): String {
    val totalVal = orders.sumOf { it.order.totalValue }
    val totalPaid = orders.flatMap { it.installments }.filter { it.isPaid }.sumOf { it.value }
    val totalRemaining = orders.flatMap { it.installments }.filter { !it.isPaid }.sumOf { it.value }
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - RELATÓRIO DE ELEVADORES\n")
    sb.append("📋 ORDENS DE SERVIÇO DE MANUTENÇÃO\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("🔍 Filtro: $filterText\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO DE MANUTENÇÃO:\n")
    sb.append(" • Total de Ordens de Serviço: ${orders.size}\n")
    sb.append(" • Valor Total Acumulado: R$ ${formatCurrency(totalVal)}\n")
    sb.append(" • Valor Pago: R$ ${formatCurrency(totalPaid)}\n")
    sb.append(" • Saldo Pendente: R$ ${formatCurrency(totalRemaining)}\n")
    sb.append("--------------------------------------------------\n\n")

    if (orders.isEmpty()) {
        sb.append("Nenhuma ordem de serviço registrada.\n")
    } else {
        orders.forEach { item ->
            val ord = item.order
            sb.append("🆔 OS #${ord.id} - Elevador ${ord.type.uppercase()} (Andar: ${ord.floor}º)\n")
            sb.append("    Data: ${ord.date} | Valor Total: R$ ${formatCurrency(ord.totalValue)}\n")
            sb.append("    Descrição: ${ord.description}\n")
            if (ord.isInstallment) {
                val paidCount = item.installments.count { it.isPaid }
                sb.append("    Parcelamento: $paidCount de ${item.installments.size} parcelas pagas\n")
            } else {
                sb.append("    Pagamento: À Vista\n")
            }
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Síndico: ______________________________\n")
    sb.append("Assinatura Técnico: ______________________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElevatorsScreen(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope) {
    val serviceOrders by dao.getServiceOrders().collectAsState(initial = emptyList())
    val context = LocalContext.current

    var selectedElevatorFilter by remember { mutableStateOf("Todos") }
    var searchText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var orderToEdit by remember { mutableStateOf<ServiceOrderWithInstallments?>(null) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }

    val filteredOrders = remember(serviceOrders, selectedElevatorFilter, searchText) {
        serviceOrders.filter { item ->
            val matchesType = when (selectedElevatorFilter) {
                "Social" -> item.order.type.equals("Social", ignoreCase = true)
                "Serviço" -> item.order.type.equals("Serviço", ignoreCase = true) || item.order.type.equals("Servico", ignoreCase = true)
                else -> true
            }

            val fullText = "${item.order.description} ${item.order.type} ${item.order.floor} ${item.order.date}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesType && matchesSearch
        }
    }

    val totalCount = filteredOrders.size
    val totalValue = filteredOrders.sumOf { it.order.totalValue }
    val totalPaid = filteredOrders.flatMap { it.installments }.filter { it.isPaid }.sumOf { it.value }
    val totalRemaining = filteredOrders.flatMap { it.installments }.filter { !it.isPaid }.sumOf { it.value }

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
                    Text("Gestão & Manutenção de Elevadores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatorMetricCard("Total Ordens", totalCount.toString(), Icons.Default.Elevator, Modifier.weight(1f))
                        ElevatorMetricCard("Investimento", "R$ ${formatCurrency(totalValue)}", Icons.Default.AccountBalance, Modifier.weight(1.2f))
                        ElevatorMetricCard("Pago", "R$ ${formatCurrency(totalPaid)}", Icons.Default.CheckCircle, Modifier.weight(1.2f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatorMetricCard("Pendente", "R$ ${formatCurrency(totalRemaining)}", Icons.Default.ReceiptLong, Modifier.weight(1f))
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
                    placeholder = { Text("Buscar por descrição, andar, tipo...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = selectedElevatorFilter == "Todos",
                        onClick = { selectedElevatorFilter = "Todos" },
                        label = { Text("Todos Elevadores") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedElevatorFilter == "Social",
                        onClick = { selectedElevatorFilter = "Social" },
                        label = { Text("Social") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedElevatorFilter == "Serviço",
                        onClick = { selectedElevatorFilter = "Serviço" },
                        label = { Text("Serviço") },
                        modifier = Modifier.weight(1f)
                    )
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
                        Text("Nova Ordem de Serviço", style = MaterialTheme.typography.labelMedium)
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
            Text("Ordens de Serviço Encontradas (${filteredOrders.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredOrders.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma ordem de serviço encontrada.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredOrders) { item ->
                ServiceOrderCard(
                    item = item,
                    currentUser = currentUser,
                    onEdit = { orderToEdit = item },
                    onDelete = {
                        scope.launch(Dispatchers.IO) {
                            dao.deleteServiceOrder(item.order.id)
                            FirestoreSyncManager.syncServiceOrder(item.order, isDelete = true)
                        }
                    },
                    onToggleInstallmentPaid = { inst ->
                        scope.launch(Dispatchers.IO) {
                            val updated = inst.copy(isPaid = !inst.isPaid)
                            dao.updateServiceInstallment(updated)
                        }
                    },
                    onShare = {
                        shareServiceOrderReport(context, item, isDetailed = true)
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddServiceOrderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newOrder, installments ->
                scope.launch(Dispatchers.IO) {
                    dao.insertServiceOrder(newOrder)
                    if (installments.isNotEmpty()) {
                        dao.insertServiceInstallments(installments)
                    }
                    FirestoreSyncManager.syncServiceOrder(newOrder)
                }
                showAddDialog = false
            }
        )
    }

    if (orderToEdit != null) {
        EditServiceOrderDialog(
            orderWithInstallments = orderToEdit!!,
            onDismiss = { orderToEdit = null },
            onConfirm = { updatedOrder, installments ->
                scope.launch(Dispatchers.IO) {
                    dao.updateServiceOrder(updatedOrder)
                    dao.deleteServiceInstallmentsByOrderId(updatedOrder.id)
                    if (installments.isNotEmpty()) {
                        dao.insertServiceInstallments(installments)
                    }
                    FirestoreSyncManager.syncServiceOrder(updatedOrder)
                }
                orderToEdit = null
            }
        )
    }

    if (showPrintPreviewDialog) {
        val htmlContent = generateServiceOrdersHtmlReport(filteredOrders, selectedElevatorFilter)
        val textReport = generateServiceOrdersTextReport(filteredOrders, selectedElevatorFilter)

        ServiceOrdersReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredOrders.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Relatorio_Ordens_Servico_Elevadores")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Ordens de Serviço"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun ServiceOrderCard(
    item: ServiceOrderWithInstallments,
    currentUser: UserEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleInstallmentPaid: (ServiceInstallmentEntity) -> Unit,
    onShare: () -> Unit
) {
    val ord = item.order
    var expanded by remember { mutableStateOf(false) }
    val paidCount = item.installments.count { it.isPaid }
    val totalInst = item.installments.size

    val badgeColor = when (ord.type) {
        "Social" -> Color(0xFF1976D2)
        "Serviço", "Servico" -> Color(0xFFE65100)
        else -> Color(0xFF616161)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "ELEVADOR ${ord.type.uppercase()}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text("📅 ${ord.date} • ${ord.floor}º Andar", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Spacer(Modifier.height(8.dp))
            Text(ord.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Valor Total: R$ ${formatCurrency(ord.totalValue)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (ord.isInstallment && totalInst > 0) {
                        Text("Parcelado: $paidCount de $totalInst pagas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        Text("Pagamento: À Vista", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                Row {
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, "Compartilhar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    if (currentUser.role == "ADMIN" || currentUser.role == "Síndico") {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (expanded && ord.isInstallment && item.installments.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Cronograma de Parcelamento:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                item.installments.sortedBy { it.number }.forEach { inst ->
                    val instColor = if (inst.isPaid) Color(0xFF2E7D32) else Color(0xFFC62828)
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(Modifier.padding(8.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("${inst.number}/$totalInst • Venc: ${inst.dueDate}", style = MaterialTheme.typography.bodySmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("R$ ${formatCurrency(inst.value)}", style = MaterialTheme.typography.bodySmall, color = instColor, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Checkbox(
                                    checked = inst.isPaid,
                                    onCheckedChange = { onToggleInstallmentPaid(inst) },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (ServiceOrderEntity, List<ServiceInstallmentEntity>) -> Unit
) {
    var type by remember { mutableStateOf("Social") }
    var floorText by remember { mutableStateOf("0") }
    var dateText by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var description by remember { mutableStateOf("") }
    var totalValueStr by remember { mutableStateOf("") }
    var isInstallment by remember { mutableStateOf(false) }
    var installmentsCountStr by remember { mutableStateOf("1") }
    var expandedType by remember { mutableStateOf(false) }

    val elevatorTypes = listOf("Social", "Serviço")
    val totalValue = (totalValueStr.toDoubleOrNull() ?: 0.0) / 100
    val installmentsCount = installmentsCountStr.toIntOrNull() ?: 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Ordem de Serviço") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Elevador *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        elevatorTypes.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expandedType = false })
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { floorText = it },
                        label = { Text("Andar *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Data *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição dos Serviços / Peças Substituídas *") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = totalValueStr,
                    onValueChange = { if (it.length <= 12) totalValueStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Valor Total *") },
                    prefix = { Text("R$ ") },
                    visualTransformation = CurrencyVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isInstallment, onCheckedChange = { isInstallment = it })
                    Text("Pagamento Parcelado")
                }

                if (isInstallment) {
                    OutlinedTextField(
                        value = installmentsCountStr,
                        onValueChange = { installmentsCountStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantidade de Parcelas *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && totalValue > 0) {
                        val orderId = System.currentTimeMillis()

                        val newOrder = ServiceOrderEntity(
                            id = orderId,
                            type = type,
                            floor = floorText.trim(),
                            description = description.trim(),
                            totalValue = totalValue,
                            date = dateText.trim(),
                            isInstallment = isInstallment
                        )

                        val installmentsList = mutableListOf<ServiceInstallmentEntity>()
                        if (isInstallment && installmentsCount > 0) {
                            val totalBD = BigDecimal.valueOf(totalValue).setScale(2, RoundingMode.HALF_UP)
                            val valParcelaBD = totalBD.divide(BigDecimal.valueOf(installmentsCount.toLong()), 2, RoundingMode.DOWN)
                            val dates = calculateMonthlyDates(dateText, installmentsCount)
                            var acc = BigDecimal.ZERO

                            for (i in 1..installmentsCount) {
                                val vBD = if (i == installmentsCount) totalBD.subtract(acc) else valParcelaBD
                                acc = acc.add(vBD)
                                installmentsList.add(
                                    ServiceInstallmentEntity(
                                        id = orderId + i + System.nanoTime(),
                                        serviceOrderId = orderId,
                                        number = i,
                                        value = vBD.toDouble(),
                                        dueDate = dates[i - 1],
                                        isPaid = false
                                    )
                                )
                            }
                        }

                        onConfirm(newOrder, installmentsList)
                    }
                },
                enabled = description.isNotBlank() && totalValue > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar OS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceOrderDialog(
    orderWithInstallments: ServiceOrderWithInstallments,
    onDismiss: () -> Unit,
    onConfirm: (ServiceOrderEntity, List<ServiceInstallmentEntity>) -> Unit
) {
    val order = orderWithInstallments.order
    var type by remember { mutableStateOf(order.type) }
    var floorText by remember { mutableStateOf(order.floor) }
    var dateText by remember { mutableStateOf(order.date) }
    var description by remember { mutableStateOf(order.description) }
    var totalValueStr by remember { mutableStateOf((order.totalValue * 100).toLong().toString()) }
    var isInstallment by remember { mutableStateOf(order.isInstallment) }
    var installmentsCountStr by remember { mutableStateOf(if (orderWithInstallments.installments.isNotEmpty()) orderWithInstallments.installments.size.toString() else "1") }
    var expandedType by remember { mutableStateOf(false) }

    val elevatorTypes = listOf("Social", "Serviço")
    val totalValue = (totalValueStr.toDoubleOrNull() ?: 0.0) / 100
    val installmentsCount = installmentsCountStr.toIntOrNull() ?: 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Ordem de Serviço #${order.id}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Elevador *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        elevatorTypes.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expandedType = false })
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { floorText = it },
                        label = { Text("Andar *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Data *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição *") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = totalValueStr,
                    onValueChange = { if (it.length <= 12) totalValueStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Valor Total *") },
                    prefix = { Text("R$ ") },
                    visualTransformation = CurrencyVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isInstallment, onCheckedChange = { isInstallment = it })
                    Text("Pagamento Parcelado")
                }

                if (isInstallment) {
                    OutlinedTextField(
                        value = installmentsCountStr,
                        onValueChange = { installmentsCountStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantidade de Parcelas *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedOrder = order.copy(
                        type = type,
                        floor = floorText.trim(),
                        description = description.trim(),
                        totalValue = totalValue,
                        date = dateText.trim(),
                        isInstallment = isInstallment
                    )

                    val installmentsList = mutableListOf<ServiceInstallmentEntity>()
                    if (isInstallment && installmentsCount > 0) {
                        val totalBD = BigDecimal.valueOf(totalValue).setScale(2, RoundingMode.HALF_UP)
                        val valParcelaBD = totalBD.divide(BigDecimal.valueOf(installmentsCount.toLong()), 2, RoundingMode.DOWN)
                        val dates = calculateMonthlyDates(dateText, installmentsCount)
                        var acc = BigDecimal.ZERO

                        for (i in 1..installmentsCount) {
                            val vBD = if (i == installmentsCount) totalBD.subtract(acc) else valParcelaBD
                            acc = acc.add(vBD)
                            installmentsList.add(
                                ServiceInstallmentEntity(
                                    id = order.id + i + System.nanoTime(),
                                    serviceOrderId = order.id,
                                    number = i,
                                    value = vBD.toDouble(),
                                    dueDate = dates[i - 1],
                                    isPaid = false
                                )
                            )
                        }
                    }

                    onConfirm(updatedOrder, installmentsList)
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
fun ServiceOrdersReportPreviewDialog(
    htmlContent: String,
    textReport: String,
    totalCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pré-visualização do Relatório de OS") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Relatório gerado com $totalCount ordem(ns) de serviço. Escolha a forma de exportação:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

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
