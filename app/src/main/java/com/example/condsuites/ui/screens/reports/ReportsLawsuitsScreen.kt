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
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.LawsuitEntity
import com.example.condsuites.data.model.LawsuitWithProgress
import com.example.condsuites.ui.components.LawsuitsMetricCard
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.naturalSortApartments
import com.example.condsuites.utils.printHtmlReport
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun shareLawsuitReport(context: Context, lawsuit: LawsuitEntity) {
    val report = StringBuilder()
    report.append("⚖️ RELATÓRIO JURÍDICO - CONDSUITES\n==========================================\n\n")
    report.append("🏢 UNIDADE: ${lawsuit.apartment}\n👤 PROPRIETÁRIO: ${lawsuit.ownerName}\n")
    report.append("📁 Nº PROCESSO: ${lawsuit.processNumber}\n🏛️ FÓRUM: ${lawsuit.forum}\n")
    report.append("💰 DÍVIDA AJUIZADA: R$ ${formatCurrency(lawsuit.totalDebt)}\n")
    report.append("📅 DATA AJUIZAMENTO: ${lawsuit.registrationDate}\n📊 STATUS: ${lawsuit.status}\n")
    val sv = lawsuit.successValue
    if (sv != null) {
        report.append("✅ VALOR DE ÊXITO: R$ ${formatCurrency(sv)}\n")
    }
    report.append("\n==========================================\n")
    report.append("Gerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório Jurídico"))
}

fun generateLawsuitsHtmlReport(
    lawsuits: List<LawsuitWithProgress>,
    statusFilterText: String,
    forumFilterText: String,
    periodText: String
): String {
    val totalCount = lawsuits.size
    val totalDebt = lawsuits.sumOf { it.lawsuit.totalDebt }
    val totalSuccess = lawsuits.sumOf { it.lawsuit.successValue ?: 0.0 }
    val finishedCount = lawsuits.count { it.lawsuit.isFinished || it.lawsuit.status.contains("Conclu", ignoreCase = true) || it.lawsuit.status.contains("Êxito", ignoreCase = true) }
    val ongoingCount = totalCount - finishedCount
    val successRate = if (totalCount > 0) (finishedCount * 100 / totalCount) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório Jurídico de Processos Ajuizados</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #4A148C; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #4A148C; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #F3E5F5; border: 1px solid #E1BEE7; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #4A148C; font-weight: bold; }
                .kpi-value { font-size: 15px; font-weight: bold; color: #6A1B9A; margin-top: 4px; }
                .section-title { font-size: 15px; color: #4A148C; border-bottom: 1px solid #CCC; padding-bottom: 4px; margin-top: 20px; margin-bottom: 10px; font-weight: bold; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #4A148C; color: white; padding: 7px; text-align: left; }
                table.data-table td { border-bottom: 1px solid #DDD; padding: 7px; vertical-align: top; }
                table.data-table tr:nth-child(even) { background: #F9F9F9; }
                .badge { display: inline-block; padding: 2px 6px; font-size: 9px; font-weight: bold; border-radius: 4px; color: white; }
                .badge-andamento { background: #0288D1; }
                .badge-exito { background: #2E7D32; }
                .badge-acordo { background: #E65100; }
                .badge-suspenso { background: #757575; }
                .signatures { margin-top: 40px; width: 100%; page-break-inside: avoid; }
                .sig-box { width: 45%; display: inline-block; text-align: center; font-size: 11px; margin-top: 20px; }
                .sig-line { border-top: 1px solid #333; margin-bottom: 4px; width: 80%; margin-left: auto; margin-right: auto; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🏢 CONDSUITES - GESTÃO CONDOMINIAL</h1>
                <h2>Relatório Executivo e Parecer Jurídico de Processos Ajuizados</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Status Filtro:</strong> $statusFilterText &nbsp;|&nbsp; 
                <strong>Fórum:</strong> $forumFilterText &nbsp;|&nbsp; 
                <strong>Período:</strong> $periodText
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Ações</div>
                    <div class="kpi-value">$totalCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Dívida Ajuizada</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalDebt)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Recuperado / Êxito</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalSuccess)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Em Andamento</div>
                    <div class="kpi-value">$ongoingCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Taxa de Êxito</div>
                    <div class="kpi-value">$successRate%</div>
                </div>
            </div>

            <div class="section-title">📊 Análise Profissional & Parecer Jurídico</div>
            <p style="font-size: 11px; color: #444; margin: 4px 0 12px 0;">
                O contencioso judicial do condomínio é composto por <strong>$totalCount processo(s) ajuizado(s)</strong>, perfazendo um montante total em discussão de <strong>R$ ${formatCurrency(totalDebt)}</strong>.
                Até a presente data, obteve-se êxito/recuperação judicial no montante de <strong>R$ ${formatCurrency(totalSuccess)}</strong>, representando uma taxa de resolução favorável de <strong>$successRate%</strong> ($finishedCount processo(s) finalizado(s) e $ongoingCount em andamento).
            </p>

            <div class="section-title">📋 Detalhamento dos Processos Ajuizados</div>
    """.trimIndent())

    if (lawsuits.isEmpty()) {
        html.append("<p style='text-align:center; padding:16px; color:#777;'>Nenhum processo ajuizado encontrado para os filtros selecionados.</p>")
    } else {
        html.append("""
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 4%;">#</th>
                        <th style="width: 10%;">Apto</th>
                        <th style="width: 20%;">Proprietário</th>
                        <th style="width: 18%;">Nº Processo / Fórum</th>
                        <th style="width: 14%;">Dívida Original</th>
                        <th style="width: 14%;">Valor Êxito</th>
                        <th style="width: 10%;">Data Ajuiz.</th>
                        <th style="width: 10%;">Status</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        lawsuits.sortedBy { naturalSortApartments(it.lawsuit.apartment) }.forEachIndexed { idx, item ->
            val law = item.lawsuit
            val badgeClass = when {
                law.isFinished || law.status.contains("Conclu", ignoreCase = true) || law.status.contains("Êxito", ignoreCase = true) -> "badge-exito"
                law.status.contains("Acordo", ignoreCase = true) -> "badge-acordo"
                law.status.contains("Suspens", ignoreCase = true) -> "badge-suspenso"
                else -> "badge-andamento"
            }

            val successDisplay = if (law.successValue != null && law.successValue!! > 0) "R$ ${formatCurrency(law.successValue!!)}" else "-"

            html.append("""
                <tr>
                    <td>${idx + 1}</td>
                    <td><strong>Apto ${law.apartment}</strong></td>
                    <td>${law.ownerName.uppercase()}</td>
                    <td><strong>${law.processNumber.ifBlank { "N/I" }}</strong><br><small style='color:#666'>${law.forum}</small></td>
                    <td>R$ ${formatCurrency(law.totalDebt)}</td>
                    <td><strong style='color:#2E7D32'>$successDisplay</strong></td>
                    <td>${law.registrationDate}</td>
                    <td><span class="badge $badgeClass">${law.status}</span></td>
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
                <strong>Assessoria Jurídica / Advogado</strong><br>
                OAB Responsável pelo Contencioso
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateLawsuitsTextReport(
    lawsuits: List<LawsuitWithProgress>,
    statusFilterText: String,
    forumFilterText: String,
    periodText: String
): String {
    val totalCount = lawsuits.size
    val totalDebt = lawsuits.sumOf { it.lawsuit.totalDebt }
    val totalSuccess = lawsuits.sumOf { it.lawsuit.successValue ?: 0.0 }
    val finishedCount = lawsuits.count { it.lawsuit.isFinished || it.lawsuit.status.contains("Conclu", ignoreCase = true) || it.lawsuit.status.contains("Êxito", ignoreCase = true) }
    val successRate = if (totalCount > 0) (finishedCount * 100 / totalCount) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - RELATÓRIO DE PROCESSOS AJUIZADOS\n")
    sb.append("⚖️ CONTENCIOSO JUDICIAL & COBRANÇA JURÍDICA\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("🔍 Status: $statusFilterText | Fórum: $forumFilterText | Período: $periodText\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO JURÍDICO / INDICADORES:\n")
    sb.append(" • Total de Ações Ajuizadas: $totalCount\n")
    sb.append(" • Dívida Total Ajuizada: R$ ${formatCurrency(totalDebt)}\n")
    sb.append(" • Valor Recuperado / Êxito: R$ ${formatCurrency(totalSuccess)}\n")
    sb.append(" • Processos Concluídos com Êxito: $finishedCount\n")
    sb.append(" • Taxa de Êxito Judicial: $successRate%\n")
    sb.append("--------------------------------------------------\n\n")

    if (lawsuits.isEmpty()) {
        sb.append("Nenhum processo ajuizado encontrado para os filtros selecionados.\n")
    } else {
        lawsuits.sortedBy { naturalSortApartments(it.lawsuit.apartment) }.forEachIndexed { idx, item ->
            val law = item.lawsuit
            sb.append("[${idx + 1}] Apto ${law.apartment} - ${law.ownerName.uppercase()}\n")
            sb.append("    Nº Processo: ${law.processNumber.ifBlank { "Não informado" }}\n")
            sb.append("    Fórum: ${law.forum} | Data Ajuizamento: ${law.registrationDate}\n")
            sb.append("    Dívida: R$ ${formatCurrency(law.totalDebt)} | Status: ${law.status}\n")
            if (law.successValue != null && law.successValue!! > 0) {
                sb.append("    Valor Êxito: R$ ${formatCurrency(law.successValue!!)}\n")
            }
            if (item.progress.isNotEmpty()) {
                val lastProg = item.progress.last()
                sb.append("    Último Andamento [${lastProg.date}]: ${lastProg.description}\n")
            }
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Síndico: ______________________________\n")
    sb.append("Assinatura Advogado/OAB: _________________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsLawsuitsScreen(dao: AppDao, context: Context) {
    val lawsuitsState by dao.getLawsuitsWithProgress().collectAsState(initial = emptyList())

    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedForumFilter by remember { mutableStateOf("Todos") }
    var selectedPeriodFilter by remember { mutableStateOf("Todos") }
    var selectedValueRangeFilter by remember { mutableStateOf("Todos") }

    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }

    var showAdvancedFiltersDialog by remember { mutableStateOf(false) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }
    var expandedCardId by remember { mutableStateOf<Long?>(null) }

    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
    val uniqueForums = remember(lawsuitsState) {
        listOf("Todos") + lawsuitsState.map { it.lawsuit.forum }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredList = remember(
        lawsuitsState, searchText, selectedStatusFilter, selectedForumFilter,
        selectedPeriodFilter, selectedValueRangeFilter, selectedMonth, selectedYear
    ) {
        lawsuitsState.filter { item ->
            val law = item.lawsuit

            val matchesStatus = when (selectedStatusFilter) {
                "Em Andamento" -> !law.isFinished && !law.status.contains("Conclu", ignoreCase = true)
                "Concluído / Êxito" -> law.isFinished || law.status.contains("Conclu", ignoreCase = true) || law.status.contains("Êxito", ignoreCase = true)
                "Acordo Judicial" -> law.status.contains("Acordo", ignoreCase = true)
                "Suspenso" -> law.status.contains("Suspens", ignoreCase = true)
                else -> true
            }

            val matchesForum = selectedForumFilter == "Todos" || law.forum.equals(selectedForumFilter, ignoreCase = true)

            val matchesPeriod = if (selectedPeriodFilter == "Todos") {
                true
            } else {
                val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(law.registrationDate) } catch (_: Exception) { null }
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

            val matchesValue = when (selectedValueRangeFilter) {
                "Até R$ 5.000" -> law.totalDebt <= 5000.0
                "R$ 5.000 a R$ 15.000" -> law.totalDebt in 5000.0..15000.0
                "Acima de R$ 15.000" -> law.totalDebt > 15000.0
                else -> true
            }

            val fullText = "${law.apartment} ${law.ownerName} ${law.processNumber} ${law.forum} ${law.status} ${item.progress.joinToString(" ") { it.description }}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesStatus && matchesForum && matchesPeriod && matchesValue && matchesSearch
        }
    }

    val totalCount = filteredList.size
    val totalDebt = filteredList.sumOf { it.lawsuit.totalDebt }
    val totalSuccess = filteredList.sumOf { it.lawsuit.successValue ?: 0.0 }
    val finishedCount = filteredList.count { it.lawsuit.isFinished || it.lawsuit.status.contains("Conclu", ignoreCase = true) || it.lawsuit.status.contains("Êxito", ignoreCase = true) }
    val ongoingCount = totalCount - finishedCount
    val successRate = if (totalCount > 0) (finishedCount * 100 / totalCount) else 0

    val activeFilterCount = (if (selectedStatusFilter != "Todos") 1 else 0) +
            (if (selectedForumFilter != "Todos") 1 else 0) +
            (if (selectedPeriodFilter != "Todos") 1 else 0) +
            (if (selectedValueRangeFilter != "Todos") 1 else 0)

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
                    Text("Painel de Indicadores do Contencioso Jurídico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LawsuitsMetricCard("Total Ações", totalCount.toString(), Icons.Filled.Gavel, Modifier.weight(1f))
                        LawsuitsMetricCard("Dívida Ajuizada", "R$ ${formatCurrency(totalDebt)}", Icons.Default.AccountBalance, Modifier.weight(1.2f))
                        LawsuitsMetricCard("Recuperado / Êxito", "R$ ${formatCurrency(totalSuccess)}", Icons.Default.CheckCircle, Modifier.weight(1.2f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LawsuitsMetricCard("Em Andamento", ongoingCount.toString(), Icons.Default.History, Modifier.weight(1f))
                        LawsuitsMetricCard("Concluídos", finishedCount.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
                        LawsuitsMetricCard("Taxa Êxito", "$successRate%", Icons.Default.Analytics, Modifier.weight(1f))
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
                        placeholder = { Text("Buscar processo, apto, fórum...") },
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

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = selectedStatusFilter == "Todos", onClick = { selectedStatusFilter = "Todos" }, label = { Text("Todos", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Em Andamento", onClick = { selectedStatusFilter = "Em Andamento" }, label = { Text("Andamento", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Concluído / Êxito", onClick = { selectedStatusFilter = "Concluído / Êxito" }, label = { Text("Êxito", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                }

                if (activeFilterCount > 0 || searchText.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Filtros ativos ($totalCount resultados)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        TextButton(onClick = {
                            searchText = ""
                            selectedStatusFilter = "Todos"
                            selectedForumFilter = "Todos"
                            selectedPeriodFilter = "Todos"
                            selectedValueRangeFilter = "Todos"
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
                            val textReport = generateLawsuitsTextReport(filteredList, selectedStatusFilter, selectedForumFilter, periodText)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textReport)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Ajuizados"))
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
            Text("Processos Ajuizados Encontrados (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum processo ajuizado encontrado para os filtros selecionados.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList.sortedBy { naturalSortApartments(it.lawsuit.apartment) }) { item ->
                val isExpanded = expandedCardId == item.lawsuit.id

                LawsuitReportCard(
                    item = item,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedCardId = if (isExpanded) null else item.lawsuit.id
                    },
                    onShare = {
                        shareLawsuitReport(context, item.lawsuit)
                    }
                )
            }
        }
    }

    if (showAdvancedFiltersDialog) {
        LawsuitsAdvancedFiltersDialog(
            selectedStatusFilter = selectedStatusFilter,
            onStatusChange = { selectedStatusFilter = it },
            selectedForumFilter = selectedForumFilter,
            onForumChange = { selectedForumFilter = it },
            uniqueForums = uniqueForums,
            selectedValueRangeFilter = selectedValueRangeFilter,
            onValueRangeChange = { selectedValueRangeFilter = it },
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
        val htmlContent = generateLawsuitsHtmlReport(filteredList, selectedStatusFilter, selectedForumFilter, periodText)
        val textReport = generateLawsuitsTextReport(filteredList, selectedStatusFilter, selectedForumFilter, periodText)

        LawsuitsReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredList.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Relatorio_Processos_Ajuizados")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Ajuizados"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun LawsuitReportCard(
    item: LawsuitWithProgress,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onShare: () -> Unit
) {
    val law = item.lawsuit
    val isFinished = law.isFinished || law.status.contains("Conclu", ignoreCase = true) || law.status.contains("Êxito", ignoreCase = true)

    val (statusText, statusColor) = when {
        isFinished -> "CONCLUÍDO / ÊXITO 🏆" to Color(0xFF2E7D32)
        law.status.contains("Acordo", ignoreCase = true) -> "ACORDO JUDICIAL 🤝" to Color(0xFFE65100)
        law.status.contains("Suspens", ignoreCase = true) -> "SUSPENSO" to Color(0xFF757575)
        else -> "EM ANDAMENTO ⚖️" to Color(0xFF0288D1)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Apto ${law.apartment} • ${law.ownerName.uppercase()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("Processo: ${law.processNumber.ifBlank { "Não informado" }} • Fórum: ${law.forum}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Dívida Ajuizada: R$ ${formatCurrency(law.totalDebt)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    if (law.successValue != null && law.successValue!! > 0) {
                        Text("Valor Recuperado: R$ ${formatCurrency(law.successValue!!)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Data de Ajuizamento: ${law.registrationDate}", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Text("Andamentos Processuais (${item.progress.size}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                if (item.progress.isEmpty()) {
                    Text("Nenhum andamento registrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    item.progress.forEach { prog ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text("[${prog.date}]:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(prog.description, style = MaterialTheme.typography.bodySmall)
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
fun LawsuitsAdvancedFiltersDialog(
    selectedStatusFilter: String,
    onStatusChange: (String) -> Unit,
    selectedForumFilter: String,
    onForumChange: (String) -> Unit,
    uniqueForums: List<String>,
    selectedValueRangeFilter: String,
    onValueRangeChange: (String) -> Unit,
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
        title = { Text("Busca Avançada de Ajuizados") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text("Status da Ação Judicial:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val statuses = listOf("Todos", "Em Andamento", "Concluído / Êxito", "Acordo Judicial", "Suspenso")
                    var expandedStatus by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = !expandedStatus }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedStatusFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione o status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                            statuses.forEach { st ->
                                DropdownMenuItem(text = { Text(st) }, onClick = { onStatusChange(st); expandedStatus = false })
                            }
                        }
                    }
                }

                Column {
                    Text("Fórum / Comarca:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    var expandedForum by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedForum, onExpandedChange = { expandedForum = !expandedForum }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedForumFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione o fórum") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedForum) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedForum, onDismissRequest = { expandedForum = false }) {
                            uniqueForums.forEach { f ->
                                DropdownMenuItem(text = { Text(f) }, onClick = { onForumChange(f); expandedForum = false })
                            }
                        }
                    }
                }

                Column {
                    Text("Faixa de Valor Ajuizado:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val ranges = listOf("Todos", "Até R$ 5.000", "R$ 5.000 a R$ 15.000", "Acima de R$ 15.000")
                    var expandedVal by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(expanded = expandedVal, onExpandedChange = { expandedVal = !expandedVal }, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedValueRangeFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione a faixa") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVal) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedVal, onDismissRequest = { expandedVal = false }) {
                            ranges.forEach { r ->
                                DropdownMenuItem(text = { Text(r) }, onClick = { onValueRangeChange(r); expandedVal = false })
                            }
                        }
                    }
                }

                Column {
                    Text("Período de Ajuizamento:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
fun LawsuitsReportPreviewDialog(
    htmlContent: String,
    textReport: String,
    totalCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pré-visualização do Relatório Jurídico") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Relatório gerado com $totalCount ação(ões) ajuizada(s). Próximo passo:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

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
