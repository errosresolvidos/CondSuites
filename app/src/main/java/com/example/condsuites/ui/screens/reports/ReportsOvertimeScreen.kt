package com.example.condsuites.ui.screens.reports

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.OvertimeEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.ui.components.OvertimeMetricCard
import com.example.condsuites.ui.screens.OvertimeCard
import com.example.condsuites.ui.screens.OvertimeReportPreviewDialog
import com.example.condsuites.utils.printHtmlReport
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun generateOvertimeHtmlReport(
    overtimes: List<OvertimeEntity>,
    statusFilterText: String,
    periodText: String
): String {
    val totalCount = overtimes.size
    val totalHours = overtimes.sumOf { it.totalHours }
    val approvedHours = overtimes.filter { it.status == "APROVADO" || it.status == "PAGO" }.sumOf { it.totalHours }
    val pendingHours = overtimes.filter { it.status == "PENDENTE" }.sumOf { it.totalHours }
    val approvedCount = overtimes.count { it.status == "APROVADO" || it.status == "PAGO" }
    val pendingCount = overtimes.count { it.status == "PENDENTE" }
    val approvalRate = if (totalCount > 0) (approvedCount * 100 / totalCount) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório Gerencial de Horas Extras</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #E65100; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #E65100; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #FFF3E0; border: 1px solid #FFE0B2; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #E65100; font-weight: bold; }
                .kpi-value { font-size: 16px; font-weight: bold; color: #BF360C; margin-top: 4px; }
                .section-title { font-size: 15px; color: #E65100; border-bottom: 1px solid #CCC; padding-bottom: 4px; margin-top: 20px; margin-bottom: 10px; font-weight: bold; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #E65100; color: white; padding: 7px; text-align: left; }
                table.data-table td { border-bottom: 1px solid #DDD; padding: 7px; vertical-align: top; }
                table.data-table tr:nth-child(even) { background: #F9F9F9; }
                .badge { display: inline-block; padding: 2px 6px; font-size: 9px; font-weight: bold; border-radius: 4px; color: white; }
                .badge-aprovado { background: #2E7D32; }
                .badge-pago { background: #0288D1; }
                .badge-pendente { background: #F57C00; }
                .badge-rejeitado { background: #C62828; }
                .signatures { margin-top: 40px; width: 100%; page-break-inside: avoid; }
                .sig-box { width: 45%; display: inline-block; text-align: center; font-size: 11px; margin-top: 20px; }
                .sig-line { border-top: 1px solid #333; margin-bottom: 4px; width: 80%; margin-left: auto; margin-right: auto; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🏢 CONDSUITES - GESTÃO CONDOMINIAL</h1>
                <h2>Relatório Gerencial e Controle de Horas Extras</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Status Filtro:</strong> $statusFilterText &nbsp;|&nbsp; 
                <strong>Período:</strong> $periodText &nbsp;|&nbsp;
                <strong>Total Registros:</strong> $totalCount
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Registros</div>
                    <div class="kpi-value">$totalCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Horas Acumuladas</div>
                    <div class="kpi-value">${String.format(Locale.getDefault(), "%.1f", totalHours)}h</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Horas Aprovadas</div>
                    <div class="kpi-value">${String.format(Locale.getDefault(), "%.1f", approvedHours)}h</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Horas Pendentes</div>
                    <div class="kpi-value">${String.format(Locale.getDefault(), "%.1f", pendingHours)}h</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Taxa Aprovação</div>
                    <div class="kpi-value">$approvalRate%</div>
                </div>
            </div>

            <div class="section-title">📊 Análise Profissional & Controle de Jornada</div>
            <p style="font-size: 11px; color: #444; margin: 4px 0 12px 0;">
                O acompanhamento do banco de horas extras registra um total acumulado de <strong>${String.format(Locale.getDefault(), "%.1f", totalHours)} horas</strong> divididas em <strong>$totalCount solicitação(ões)</strong>.
                Atualmente, <strong>${String.format(Locale.getDefault(), "%.1f", approvedHours)} horas</strong> foram validadas/pagas ($approvedCount registros), enquanto <strong>${String.format(Locale.getDefault(), "%.1f", pendingHours)} horas</strong> aguardam deliberação do síndico/administração ($pendingCount pendentes).
            </p>

            <div class="section-title">📋 Detalhamento dos Lançamentos de Horas Extras</div>
    """.trimIndent())

    if (overtimes.isEmpty()) {
        html.append("<p style='text-align:center; padding:16px; color:#777;'>Nenhum lançamento de hora extra encontrado para os filtros selecionados.</p>")
    } else {
        html.append("""
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 4%;">#</th>
                        <th style="width: 20%;">Colaborador / Cargo</th>
                        <th style="width: 12%;">Data</th>
                        <th style="width: 14%;">Horário (Início-Fim)</th>
                        <th style="width: 8%;">Horas</th>
                        <th style="width: 28%;">Motivo da Hora Extra</th>
                        <th style="width: 14%;">Status</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        overtimes.forEachIndexed { idx, item ->
            val badgeClass = when (item.status) {
                "APROVADO" -> "badge-aprovado"
                "PAGO" -> "badge-pago"
                "REJEITADO" -> "badge-rejeitado"
                else -> "badge-pendente"
            }

            html.append("""
                <tr>
                    <td>${idx + 1}</td>
                    <td><strong>${item.employeeName.uppercase()}</strong><br><small style='color:#666'>${item.employeeRole}</small></td>
                    <td>${item.date}</td>
                    <td>${item.startTime} às ${item.endTime}</td>
                    <td><strong>${String.format(Locale.getDefault(), "%.1f", item.totalHours)}h</strong></td>
                    <td>${item.reason}</td>
                    <td><span class="badge $badgeClass">${item.status}</span></td>
                </tr>
            """.trimIndent())
        }

        html.append("</tbody></table>")
    }

    html.append("""
        <div class="signatures">
            <div class="sig-box">
                <div class="sig-line"></div>
                <strong>Síndico / Administração</strong><br>
                Condomínio CondSuites
            </div>
            <div class="sig-box" style="float: right;">
                <div class="sig-line"></div>
                <strong>Recursos Humanos / Gestão Pessoal</strong><br>
                Aprovação de Horas Extras
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateOvertimeTextReport(
    overtimes: List<OvertimeEntity>,
    statusFilterText: String,
    periodText: String
): String {
    val totalCount = overtimes.size
    val totalHours = overtimes.sumOf { it.totalHours }
    val approvedHours = overtimes.filter { it.status == "APROVADO" || it.status == "PAGO" }.sumOf { it.totalHours }
    val pendingHours = overtimes.filter { it.status == "PENDENTE" }.sumOf { it.totalHours }
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - RELATÓRIO DE HORAS EXTRAS\n")
    sb.append("📋 CONTROLE DE JORNADA & BANCO DE HORAS\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("🔍 Status: $statusFilterText | Período: $periodText\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO DE JORNADA:\n")
    sb.append(" • Total de Solicitações: $totalCount\n")
    sb.append(" • Horas Totais Acumuladas: ${String.format(Locale.getDefault(), "%.1f", totalHours)}h\n")
    sb.append(" • Horas Aprovadas/Pagas: ${String.format(Locale.getDefault(), "%.1f", approvedHours)}h\n")
    sb.append(" • Horas Pendentes: ${String.format(Locale.getDefault(), "%.1f", pendingHours)}h\n")
    sb.append("--------------------------------------------------\n\n")

    if (overtimes.isEmpty()) {
        sb.append("Nenhum lançamento de hora extra encontrado para os filtros selecionados.\n")
    } else {
        overtimes.forEachIndexed { idx, item ->
            sb.append("[${idx + 1}] ${item.employeeName.uppercase()} (${item.employeeRole})\n")
            sb.append("    Data: ${item.date} | Horário: ${item.startTime} às ${item.endTime} (${String.format(Locale.getDefault(), "%.1f", item.totalHours)}h)\n")
            sb.append("    Motivo: ${item.reason}\n")
            sb.append("    Status: ${item.status}\n")
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Síndico: ______________________________\n")
    sb.append("Assinatura Colaborador: __________________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsOvertimeScreen(dao: AppDao, context: Context) {
    val overtimes by dao.getAllOvertimeFlow().collectAsState(initial = emptyList())

    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedPeriodFilter by remember { mutableStateOf("Todos") }

    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }

    var showPrintPreviewDialog by remember { mutableStateOf(false) }
    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

    val filteredList = remember(overtimes, searchText, selectedStatusFilter, selectedPeriodFilter, selectedMonth, selectedYear) {
        overtimes.filter { item ->
            val matchesStatus = when (selectedStatusFilter) {
                "Pendentes" -> item.status == "PENDENTE"
                "Aprovados" -> item.status == "APROVADO"
                "Pagos" -> item.status == "PAGO"
                "Rejeitados" -> item.status == "REJEITADO"
                else -> true
            }

            val matchesPeriod = if (selectedPeriodFilter == "Todos") {
                true
            } else {
                val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(item.date) } catch (_: Exception) { null }
                if (date == null) true
                else {
                    val cal = Calendar.getInstance().apply { time = date }
                    val diffDays = (System.currentTimeMillis() - date.time) / (1000 * 60 * 60 * 24)

                    when (selectedPeriodFilter) {
                        "Últimos 30 Dias" -> diffDays in 0..30
                        "Últimos 90 Dias" -> diffDays in 0..90
                        "Este Ano" -> cal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                        "Mês/Ano Específico" -> cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth
                        else -> true
                    }
                }
            }

            val fullText = "${item.employeeName} ${item.employeeRole} ${item.reason} ${item.date}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesStatus && matchesPeriod && matchesSearch
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
                    Text("Relatório Analítico de Horas Extras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OvertimeMetricCard("Registros", totalCount.toString(), Icons.Default.AccessTime, Modifier.weight(1f))
                        OvertimeMetricCard("Horas Totais", "${String.format(Locale.getDefault(), "%.1f", totalHours)}h", Icons.Default.AccessTime, Modifier.weight(1f))
                        OvertimeMetricCard("Aprovadas", "${String.format(Locale.getDefault(), "%.1f", approvedHours)}h", Icons.Default.CheckCircle, Modifier.weight(1f))
                        OvertimeMetricCard("Pendentes", "${String.format(Locale.getDefault(), "%.1f", pendingHours)}h", Icons.Default.NotificationImportant, Modifier.weight(1f))
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
                        onClick = { showPrintPreviewDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Imprimir Relatório (PDF)", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
                            val textReport = generateOvertimeTextReport(filteredList, selectedStatusFilter, periodText)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textReport)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Horas Extras"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Compartilhar Texto", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        item {
            Text("Registros Encontrados (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum registro encontrado para os filtros selecionados.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList) { item ->
                OvertimeCard(
                    item = item,
                    currentUser = UserEntity(role = "PORTEIRO"),
                    onEdit = {},
                    onDelete = {},
                    onStatusChange = {}
                )
            }
        }
    }

    if (showPrintPreviewDialog) {
        val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
        val htmlContent = generateOvertimeHtmlReport(filteredList, selectedStatusFilter, periodText)
        val textReport = generateOvertimeTextReport(filteredList, selectedStatusFilter, periodText)

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
