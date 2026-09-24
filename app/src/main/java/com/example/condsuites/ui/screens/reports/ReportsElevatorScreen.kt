package com.example.condsuites.ui.screens.reports

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.OccurrenceWithMessages
import com.example.condsuites.data.model.ServiceOrderWithInstallments
import com.example.condsuites.ui.components.ElevatorMetricCard
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.printHtmlReport
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun shareServiceOrderReport(context: Context, item: ServiceOrderWithInstallments, isDetailed: Boolean) {
    val report = StringBuilder()
    val typeName = if (isDetailed) "DETALHADO" else "RESUMIDO"
    report.append("📄 RELATÓRIO DE MANUTENÇÃO ($typeName) - ELEVADOR ${item.order.type.uppercase()}\n==========================================\n\n")
    report.append("🆔 OS: ${item.order.id}\n📅 DATA: ${item.order.date}\n📝 DESCRIÇÃO: ${item.order.description}\n🏢 ANDAR: ${item.order.floor}\n💰 VALOR: R$ ${formatCurrency(item.order.totalValue)}\n")
    if (isDetailed && item.order.isInstallment) {
        report.append("\n📋 DETALHAMENTO DE PAGAMENTO:\n")
        item.installments.sortedBy { it.number }.forEach { inst ->
            report.append("${inst.number}/${item.installments.size} | ${inst.dueDate} | R$ ${formatCurrency(inst.value)} | ${if (inst.isPaid) "PAGO" else "ABERTO"}\n")
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
}

fun isElevatorOccurrence(item: OccurrenceWithMessages): Boolean {
    val fullText = "${item.occurrence.title} ${item.occurrence.occurrenceType} ${item.occurrence.apartment}".lowercase()
    return fullText.contains("elevador") || fullText.contains("elevadores") ||
           fullText.contains("social") || fullText.contains("serviço") || fullText.contains("servico")
}

fun getElevatorTypeForOccurrence(item: OccurrenceWithMessages): String {
    val fullText = "${item.occurrence.title} ${item.occurrence.occurrenceType} ${item.occurrence.apartment} ${item.messages.joinToString(" ") { it.message.text }}".lowercase()
    val isSocial = fullText.contains("social")
    val isService = fullText.contains("serviço") || fullText.contains("servico")
    
    return when {
        isSocial && !isService -> "Elevador Social"
        isService && !isSocial -> "Elevador Serviço"
        isSocial && isService -> "Elevador Social e Serviço"
        else -> "Elevador (Geral)"
    }
}

fun generateElevatorMaintenanceHtmlReport(
    occurrences: List<OccurrenceWithMessages>,
    filterElevatorText: String,
    periodText: String,
    statusText: String,
    urgencyText: String
): String {
    val totalCount = occurrences.size
    val socialCount = occurrences.count { getElevatorTypeForOccurrence(it) == "Elevador Social" }
    val serviceCount = occurrences.count { getElevatorTypeForOccurrence(it) == "Elevador Serviço" }
    val openCount = occurrences.count { it.occurrence.status == "ABERTA" }
    val closedCount = occurrences.count { it.occurrence.status == "FINALIZADA" }
    val urgentCount = occurrences.count { it.occurrence.isUrgent }
    val resRate = if (totalCount > 0) (closedCount * 100 / totalCount) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório de Manutenção dos Elevadores</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #1E88E5; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #1E88E5; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #E3F2FD; border: 1px solid #BBDEFB; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #1565C0; font-weight: bold; }
                .kpi-value { font-size: 18px; font-weight: bold; color: #0D47A1; margin-top: 4px; }
                .section-title { font-size: 15px; color: #1565C0; border-bottom: 1px solid #CCC; padding-bottom: 4px; margin-top: 20px; margin-bottom: 10px; font-weight: bold; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #1E88E5; color: white; padding: 7px; text-align: left; }
                table.data-table td { border-bottom: 1px solid #DDD; padding: 7px; vertical-align: top; }
                table.data-table tr:nth-child(even) { background: #F9F9F9; }
                .badge { display: inline-block; padding: 2px 6px; font-size: 9px; font-weight: bold; border-radius: 4px; color: white; }
                .badge-social { background: #1976D2; }
                .badge-service { background: #E65100; }
                .badge-general { background: #757575; }
                .badge-open { background: #0288D1; }
                .badge-closed { background: #388E3C; }
                .badge-urgent { background: #D32F2F; }
                .signatures { margin-top: 40px; width: 100%; page-break-inside: avoid; }
                .sig-box { width: 45%; display: inline-block; text-align: center; font-size: 11px; margin-top: 20px; }
                .sig-line { border-top: 1px solid #333; margin-bottom: 4px; width: 80%; margin-left: auto; margin-right: auto; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🏢 CONDSUITES - GESTÃO CONDOMINIAL</h1>
                <h2>Relatório Técnico de Manutenção & Ocorrências dos Elevadores</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Filtro Elevador:</strong> $filterElevatorText &nbsp;|&nbsp; 
                <strong>Período:</strong> $periodText<br>
                <strong>Status:</strong> $statusText &nbsp;|&nbsp; 
                <strong>Urgência:</strong> $urgencyText
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Chamados</div>
                    <div class="kpi-value">$totalCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Elev. Social</div>
                    <div class="kpi-value">$socialCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Elev. Serviço</div>
                    <div class="kpi-value">$serviceCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Urgentes</div>
                    <div class="kpi-value">$urgentCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Solucionadas</div>
                    <div class="kpi-value">$resRate%</div>
                </div>
            </div>

            <div class="section-title">📊 Análise de Manutenção e Diagnóstico</div>
            <p style="font-size: 11px; color: #444; margin: 4px 0 12px 0;">
                Relatório analítico do histórico operacional dos elevadores para controle da gestão predial e acompanhamento da empresa conservadora de elevadores.
                ${if (socialCount > serviceCount) "Maior incidência observada no <strong>Elevador Social</strong> ($socialCount ocorrências)." else if (serviceCount > socialCount) "Maior incidência observada no <strong>Elevador de Serviço</strong> ($serviceCount ocorrências)." else "Volume de chamados equivalente entre Elevador Social e de Serviço ($socialCount chamados cada)."}
                Taxa de resolução atual de <strong>$resRate%</strong> ($closedCount finalizadas e $openCount em aberto de $totalCount registradas).
            </p>

            <div class="section-title">📋 Detalhamento das Ocorrências</div>
    """.trimIndent())

    if (occurrences.isEmpty()) {
        html.append("<p style='text-align:center; padding:16px; color:#777;'>Nenhuma ocorrência registrada com os filtros aplicados.</p>")
    } else {
        html.append("""
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 4%;">#</th>
                        <th style="width: 16%;">Elevador</th>
                        <th style="width: 11%;">Data</th>
                        <th style="width: 28%;">Assunto / Motivo</th>
                        <th style="width: 11%;">Status</th>
                        <th style="width: 11%;">Prioridade</th>
                        <th style="width: 19%;">Solicitante / Local</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        occurrences.forEachIndexed { idx, item ->
            val type = getElevatorTypeForOccurrence(item)
            val badgeClass = when (type) {
                "Elevador Social" -> "badge-social"
                "Elevador Serviço" -> "badge-service"
                else -> "badge-general"
            }
            val statusClass = if (item.occurrence.status == "FINALIZADA") "badge-closed" else "badge-open"
            val urgentBadge = if (item.occurrence.isUrgent) "<span class='badge badge-urgent'>URGENTE</span>" else "Normal"
            val msgCount = item.messages.size

            html.append("""
                <tr>
                    <td>${idx + 1}</td>
                    <td><span class="badge $badgeClass">$type</span></td>
                    <td>${item.occurrence.date}</td>
                    <td><strong>${item.occurrence.title}</strong></td>
                    <td><span class="badge $statusClass">${item.occurrence.status}</span></td>
                    <td>$urgentBadge</td>
                    <td>${item.occurrence.createdByUsername}<br><small>Local: ${item.occurrence.apartment} ($msgCount msgs)</small></td>
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
                <strong>Técnico / Empresa Conservadora</strong><br>
                Manutenção dos Elevadores
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateElevatorMaintenanceTextReport(
    occurrences: List<OccurrenceWithMessages>,
    filterElevatorText: String,
    periodText: String
): String {
    val totalCount = occurrences.size
    val socialCount = occurrences.count { getElevatorTypeForOccurrence(it) == "Elevador Social" }
    val serviceCount = occurrences.count { getElevatorTypeForOccurrence(it) == "Elevador Serviço" }
    val openCount = occurrences.count { it.occurrence.status == "ABERTA" }
    val closedCount = occurrences.count { it.occurrence.status == "FINALIZADA" }
    val urgentCount = occurrences.count { it.occurrence.isUrgent }
    val resRate = if (totalCount > 0) (closedCount * 100 / totalCount) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - RELATÓRIO DE ELEVADORES\n")
    sb.append("📋 ELEVADOR SOCIAL & ELEVADOR DE SERVIÇO\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("🔍 Elevador: $filterElevatorText | Período: $periodText\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO TÉCNICO / MANUTENÇÃO:\n")
    sb.append(" • Total de Chamados: $totalCount\n")
    sb.append(" • Elevador Social: $socialCount | Elevador Serviço: $serviceCount\n")
    sb.append(" • Chamados Urgentes: $urgentCount\n")
    sb.append(" • Status: $closedCount Concluídas / $openCount Em Aberto ($resRate% Solucionadas)\n")
    sb.append("--------------------------------------------------\n\n")

    if (occurrences.isEmpty()) {
        sb.append("Nenhuma ocorrência encontrada para os filtros selecionados.\n")
    } else {
        occurrences.forEachIndexed { idx, item ->
            val type = getElevatorTypeForOccurrence(item)
            val occ = item.occurrence
            sb.append("[${idx + 1}] $type | Data: ${occ.date}\n")
            sb.append("    Assunto: ${occ.title}\n")
            sb.append("    Status: ${occ.status} | Urgência: ${if (occ.isUrgent) "🚨 URGENTE" else "Normal"}\n")
            sb.append("    Solicitante: ${occ.createdByUsername} (Local: ${occ.apartment})\n")
            if (item.messages.isNotEmpty()) {
                val lastMsg = item.messages.last().message
                sb.append("    Última Interação: [${lastMsg.senderUsername}] ${lastMsg.text}\n")
            }
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Síndico: ______________________________\n")
    sb.append("Assinatura Téc. Elevadores: ______________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsElevatorOccurrencesScreen(dao: AppDao, context: Context) {
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())

    var searchText by remember { mutableStateOf("") }
    var selectedElevatorFilter by remember { mutableStateOf("Todos") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedUrgencyFilter by remember { mutableStateOf("Todas") }
    var selectedComponentFilter by remember { mutableStateOf("Todos") }
    var selectedPeriodFilter by remember { mutableStateOf("Todos") }

    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }

    var showAdvancedFiltersDialog by remember { mutableStateOf(false) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }
    var expandedCardId by remember { mutableStateOf<Long?>(null) }

    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

    val allElevatorOccurrences = remember(occurrences) {
        occurrences.filter { isElevatorOccurrence(it) }
    }

    val filteredList = remember(
        allElevatorOccurrences, searchText, selectedElevatorFilter, selectedStatusFilter,
        selectedUrgencyFilter, selectedComponentFilter, selectedPeriodFilter, selectedMonth, selectedYear
    ) {
        allElevatorOccurrences.filter { item ->
            val type = getElevatorTypeForOccurrence(item)

            val matchesElevator = when (selectedElevatorFilter) {
                "Elevador Social" -> type == "Elevador Social"
                "Elevador Serviço" -> type == "Elevador Serviço"
                else -> true
            }

            val matchesStatus = when (selectedStatusFilter) {
                "ABERTA" -> item.occurrence.status == "ABERTA"
                "EM_ESPERA" -> item.occurrence.status == "EM_ESPERA"
                "FINALIZADA" -> item.occurrence.status == "FINALIZADA"
                else -> true
            }

            val matchesUrgency = when (selectedUrgencyFilter) {
                "Apenas Urgentes" -> item.occurrence.isUrgent
                "Normais" -> !item.occurrence.isUrgent
                else -> true
            }

            val fullContent = "${item.occurrence.title} ${item.messages.joinToString(" ") { it.message.text }}".lowercase()
            val matchesComponent = when (selectedComponentFilter) {
                "Porta/Dictador" -> fullContent.contains("porta") || fullContent.contains("dictador") || fullContent.contains("tranco")
                "Botões/Painel" -> fullContent.contains("bot") || fullContent.contains("painel") || fullContent.contains("chamada")
                "Barulho/Vibração" -> fullContent.contains("barulho") || fullContent.contains("ruido") || fullContent.contains("vibra")
                "Parada/Retenção" -> fullContent.contains("parad") || fullContent.contains("retenc") || fullContent.contains("nivel")
                "Placa/Comando" -> fullContent.contains("placa") || fullContent.contains("comando") || fullContent.contains("eletri")
                else -> true
            }

            val matchesPeriod = if (selectedPeriodFilter == "Todos") {
                true
            } else {
                val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(item.occurrence.date) } catch (_: Exception) { null }
                if (date == null) true
                else {
                    val cal = Calendar.getInstance().apply { time = date }
                    val itemMillis = date.time
                    val nowMillis = System.currentTimeMillis()
                    val diffDays = (nowMillis - itemMillis) / (1000 * 60 * 60 * 24)

                    when (selectedPeriodFilter) {
                        "Últimos 30 Dias" -> diffDays in 0..30
                        "Últimos 90 Dias" -> diffDays in 0..90
                        "Este Ano" -> cal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                        "Mês/Ano Específico" -> cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth
                        else -> true
                    }
                }
            }

            val matchesSearch = searchText.isBlank() ||
                item.occurrence.title.contains(searchText, ignoreCase = true) ||
                item.occurrence.apartment.contains(searchText, ignoreCase = true) ||
                item.occurrence.createdByUsername.contains(searchText, ignoreCase = true) ||
                item.messages.any { it.message.text.contains(searchText, ignoreCase = true) }

            matchesElevator && matchesStatus && matchesUrgency && matchesComponent && matchesPeriod && matchesSearch
        }
    }

    val totalCount = filteredList.size
    val socialCount = filteredList.count { getElevatorTypeForOccurrence(it) == "Elevador Social" }
    val serviceCount = filteredList.count { getElevatorTypeForOccurrence(it) == "Elevador Serviço" }
    val urgentCount = filteredList.count { it.occurrence.isUrgent }
    val closedCount = filteredList.count { it.occurrence.status == "FINALIZADA" }
    val resRate = if (totalCount > 0) (closedCount * 100 / totalCount) else 0

    val activeFilterCount = (if (selectedElevatorFilter != "Todos") 1 else 0) +
            (if (selectedStatusFilter != "Todos") 1 else 0) +
            (if (selectedUrgencyFilter != "Todas") 1 else 0) +
            (if (selectedComponentFilter != "Todos") 1 else 0) +
            (if (selectedPeriodFilter != "Todos") 1 else 0)

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
                    Text("Painel de Indicadores de Manutenção", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatorMetricCard("Total Chamados", totalCount.toString(), Icons.Default.Elevator, Modifier.weight(1f))
                        ElevatorMetricCard("Elev. Social", socialCount.toString(), Icons.Default.Groups, Modifier.weight(1f))
                        ElevatorMetricCard("Elev. Serviço", serviceCount.toString(), Icons.Default.Build, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatorMetricCard("Urgentes 🚨", urgentCount.toString(), Icons.Default.NotificationImportant, Modifier.weight(1f))
                        ElevatorMetricCard("Taxa Solução", "$resRate%", Icons.Default.CheckCircle, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { showAdvancedFiltersDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Filtros ${if (activeFilterCount > 0) "($activeFilterCount)" else ""}")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = selectedElevatorFilter == "Todos",
                        onClick = { selectedElevatorFilter = "Todos" },
                        label = { Text("Todos Elevadores") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedElevatorFilter == "Elevador Social",
                        onClick = { selectedElevatorFilter = "Elevador Social" },
                        label = { Text("Social") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedElevatorFilter == "Elevador Serviço",
                        onClick = { selectedElevatorFilter = "Elevador Serviço" },
                        label = { Text("Serviço") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (activeFilterCount > 0 || searchText.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Filtros ativos aplicados ($totalCount resultados)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        TextButton(onClick = {
                            searchText = ""
                            selectedElevatorFilter = "Todos"
                            selectedStatusFilter = "Todos"
                            selectedUrgencyFilter = "Todas"
                            selectedComponentFilter = "Todos"
                            selectedPeriodFilter = "Todos"
                        }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Limpar Filtros", style = MaterialTheme.typography.labelSmall)
                        }
                    }
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
                            val textReport = generateElevatorMaintenanceTextReport(filteredList, selectedElevatorFilter, periodText)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textReport)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Elevadores"))
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
            Text("Ocorrências Registradas (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma ocorrência encontrada para os filtros selecionados.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList) { item ->
                val type = getElevatorTypeForOccurrence(item)
                val isExpanded = expandedCardId == item.occurrence.id

                ElevatorOccurrenceCard(
                    item = item,
                    elevatorType = type,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedCardId = if (isExpanded) null else item.occurrence.id
                    },
                    onShare = {
                        shareOccurrenceReport(context, item)
                    }
                )
            }
        }
    }

    if (showAdvancedFiltersDialog) {
        ElevatorAdvancedFiltersDialog(
            selectedStatusFilter = selectedStatusFilter,
            onStatusChange = { selectedStatusFilter = it },
            selectedUrgencyFilter = selectedUrgencyFilter,
            onUrgencyChange = { selectedUrgencyFilter = it },
            selectedComponentFilter = selectedComponentFilter,
            onComponentChange = { selectedComponentFilter = it },
            selectedPeriodFilter = selectedPeriodFilter,
            onPeriodChange = { selectedPeriodFilter = it },
            selectedMonth = selectedMonth,
            onMonthChange = { selectedMonth = it },
            selectedYear = selectedYear,
            onYearChange = { selectedYear = it },
            monthLabels = monthLabels,
            onDismiss = { showAdvancedFiltersDialog = false }
        )
    }

    if (showPrintPreviewDialog) {
        val periodText = if (selectedPeriodFilter == "Mês/Ano Específico") "${monthLabels[selectedMonth]}/$selectedYear" else selectedPeriodFilter
        val htmlContent = generateElevatorMaintenanceHtmlReport(filteredList, selectedElevatorFilter, periodText, selectedStatusFilter, selectedUrgencyFilter)
        val textReport = generateElevatorMaintenanceTextReport(filteredList, selectedElevatorFilter, periodText)

        ElevatorReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredList.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Relatorio_Manutencao_Elevadores")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Elevadores"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun ElevatorOccurrenceCard(
    item: OccurrenceWithMessages,
    elevatorType: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onShare: () -> Unit
) {
    val badgeColor = when (elevatorType) {
        "Elevador Social" -> Color(0xFF1976D2)
        "Elevador Serviço" -> Color(0xFFE65100)
        else -> Color(0xFF616161)
    }

    val statusColor = when (item.occurrence.status) {
        "FINALIZADA" -> Color(0xFF2E7D32)
        "EM_ESPERA" -> Color(0xFFF57C00)
        else -> Color(0xFF0288D1)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggleExpand() },
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
                        elevatorType.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            item.occurrence.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (item.occurrence.isUrgent) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFD32F2F),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "🚨 URGENTE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(item.occurrence.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (item.occurrence.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Descrição: ${item.occurrence.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            val firstMsgText = item.messages.sortedBy { it.message.id }.firstOrNull()?.message?.text
            if (!firstMsgText.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = firstMsgText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    "Local: ${item.occurrence.apartment} • Data: ${item.occurrence.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Autor: ${item.occurrence.createdByUsername}", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text("Histórico de Interações (${item.messages.size}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                if (item.messages.isEmpty()) {
                    Text("Nenhuma mensagem registrada.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    item.messages.forEach { msg ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text("[${msg.message.date}] ${msg.message.senderUsername}:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(msg.message.text, style = MaterialTheme.typography.bodySmall)
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
fun ElevatorAdvancedFiltersDialog(
    selectedStatusFilter: String,
    onStatusChange: (String) -> Unit,
    selectedUrgencyFilter: String,
    onUrgencyChange: (String) -> Unit,
    selectedComponentFilter: String,
    onComponentChange: (String) -> Unit,
    selectedPeriodFilter: String,
    onPeriodChange: (String) -> Unit,
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    monthLabels: List<String>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Busca Avançada de Ocorrências") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text("Status do Chamado:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = selectedStatusFilter == "Todos", onClick = { onStatusChange("Todos") }, label = { Text("Todos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedStatusFilter == "ABERTA", onClick = { onStatusChange("ABERTA") }, label = { Text("Abertas", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedStatusFilter == "FINALIZADA", onClick = { onStatusChange("FINALIZADA") }, label = { Text("Fechadas", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    }
                }

                Column {
                    Text("Urgência:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = selectedUrgencyFilter == "Todas", onClick = { onUrgencyChange("Todas") }, label = { Text("Todas", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedUrgencyFilter == "Apenas Urgentes", onClick = { onUrgencyChange("Apenas Urgentes") }, label = { Text("Urgentes 🚨", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedUrgencyFilter == "Normais", onClick = { onUrgencyChange("Normais") }, label = { Text("Normais", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    }
                }

                Column {
                    Text("Componente / Motivo da Falha:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val components = listOf("Todos", "Porta/Dictador", "Botões/Painel", "Barulho/Vibração", "Parada/Retenção", "Placa/Comando")
                    var expandedComp by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedComp, onExpandedChange = { expandedComp = !expandedComp }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedComponentFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione o motivo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedComp) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedComp, onDismissRequest = { expandedComp = false }) {
                            components.forEach { comp ->
                                DropdownMenuItem(text = { Text(comp) }, onClick = { onComponentChange(comp); expandedComp = false })
                            }
                        }
                    }
                }

                Column {
                    Text("Período de Análise:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val periods = listOf("Todos", "Últimos 30 Dias", "Últimos 90 Dias", "Este Ano", "Mês/Ano Específico")
                    var expandedPeriod by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedPeriod, onExpandedChange = { expandedPeriod = !expandedPeriod }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedPeriodFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar por período") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriod) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedPeriod, onDismissRequest = { expandedPeriod = false }) {
                            periods.forEach { p ->
                                DropdownMenuItem(text = { Text(p) }, onClick = { onPeriodChange(p); expandedPeriod = false })
                            }
                        }
                    }

                    if (selectedPeriodFilter == "Mês/Ano Específico") {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var expM by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expM = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Mês: ${monthLabels[selectedMonth]}")
                                }
                                DropdownMenu(expanded = expM, onDismissRequest = { expM = false }) {
                                    monthLabels.forEachIndexed { idx, m ->
                                        DropdownMenuItem(text = { Text(m) }, onClick = { onMonthChange(idx); expM = false })
                                    }
                                }
                            }

                            var expY by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expY = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Ano: $selectedYear")
                                }
                                DropdownMenu(expanded = expY, onDismissRequest = { expY = false }) {
                                    (2024..2030).forEach { yr ->
                                        DropdownMenuItem(text = { Text(yr.toString()) }, onClick = { onYearChange(yr); expY = false })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Aplicar Filtros")
            }
        }
    )
}

@Composable
fun ElevatorReportPreviewDialog(
    htmlContent: String,
    textReport: String,
    totalCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pré-visualização do Relatório Profissional") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Relatório gerado com $totalCount ocorrência(s). Escolha a forma de exportação:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

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
