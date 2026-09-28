package com.example.condsuites.ui.screens.reports

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.DelinquencyHistoryEntity
import com.example.condsuites.data.model.OccurrenceWithMessages
import com.example.condsuites.data.model.UnitEntity
import com.example.condsuites.data.model.UnitStatus
import com.example.condsuites.data.model.status
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.naturalSortApartments
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun generateUnitsHtmlReport(units: List<UnitEntity>): String {
    val total = units.size
    val registered = units.count { it.status == UnitStatus.CADASTRADO }
    val incomplete = units.count { it.status == UnitStatus.INCOMPLETO }
    val pending = units.count { it.status == UnitStatus.PENDENTE }
    val occupancyRate = if (total > 0) (registered * 100 / total) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório de Cadastro de Unidades</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #1E88E5; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #1E88E5; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #E3F2FD; border: 1px solid #BBDEFB; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #1565C0; font-weight: bold; }
                .kpi-value { font-size: 16px; font-weight: bold; color: #0D47A1; margin-top: 4px; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #1E88E5; color: white; padding: 7px; text-align: left; }
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
                <h2>Cadastro Profissional & Consulta de Unidades</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Estrutura:</strong> 12 Unidades/Andar (Do 2º ao 12º Andar) &nbsp;|&nbsp;
                <strong>Total Unidades:</strong> $total
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Unidades</div>
                    <div class="kpi-value">$total</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Cadastradas</div>
                    <div class="kpi-value">$registered</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Incompletas</div>
                    <div class="kpi-value">$incomplete</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Pendentes</div>
                    <div class="kpi-value">$pending</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Preenchimento</div>
                    <div class="kpi-value">$occupancyRate%</div>
                </div>
            </div>

            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 6%;">#</th>
                        <th style="width: 12%;">Unidade</th>
                        <th style="width: 10%;">Andar</th>
                        <th style="width: 32%;">Nome Morador / Proprietário</th>
                        <th style="width: 20%;">Telefone / Celular</th>
                        <th style="width: 20%;">E-mail</th>
                    </tr>
                </thead>
                <tbody>
    """.trimIndent())

    units.sortedWith(compareBy({ it.floor }, { naturalSortApartments(it.apartment) })).forEachIndexed { idx, unit ->
        val name = if (unit.ownerName.isNotBlank()) unit.ownerName.uppercase() else "-"
        val phone = if (unit.phone.isNotBlank()) unit.phone else "-"
        val email = if (unit.email.isNotBlank()) unit.email else "-"

        html.append("""
            <tr>
                <td>${idx + 1}</td>
                <td><strong>Apto ${unit.apartment}</strong></td>
                <td>${unit.floor}º Andar</td>
                <td>$name</td>
                <td>$phone</td>
                <td>$email</td>
            </tr>
        """.trimIndent())
    }

    html.append("""
            </tbody>
        </table>

        <div class="signatures">
            <div class="sig-box">
                <div class="sig-line"></div>
                <strong>Administração Condominial</strong><br>
                Condomínio CondSuites
            </div>
            <div class="sig-box" style="float: right;">
                <div class="sig-line"></div>
                <strong>Portaria / Recepção</strong><br>
                Consulta de Unidades
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateUnitsTextReport(units: List<UnitEntity>): String {
    val total = units.size
    val registered = units.count { it.status == UnitStatus.CADASTRADO }
    val incomplete = units.count { it.status == UnitStatus.INCOMPLETO }
    val pending = units.count { it.status == UnitStatus.PENDENTE }
    val occupancyRate = if (total > 0) (registered * 100 / total) else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - CADASTRO DE UNIDADES\n")
    sb.append("📋 ESTRUTURA: 12 UNIDADES POR ANDAR (DO 2º AO 12º ANDAR)\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO DE PREENCHIMENTO:\n")
    sb.append(" • Total de Unidades Mapeadas: $total\n")
    sb.append(" • Unidades Cadastradas: $registered\n")
    sb.append(" • Unidades Incompletas: $incomplete\n")
    sb.append(" • Unidades Pendentes: $pending\n")
    sb.append(" • Taxa de Preenchimento: $occupancyRate%\n")
    sb.append("--------------------------------------------------\n\n")

    if (units.isEmpty()) {
        sb.append("Nenhuma unidade cadastrada.\n")
    } else {
        units.sortedWith(compareBy({ it.floor }, { naturalSortApartments(it.apartment) })).forEachIndexed { idx, unit ->
            val statusStr = when (unit.status) {
                UnitStatus.CADASTRADO -> "✅ CADASTRADO"
                UnitStatus.INCOMPLETO -> "⚠️ INCOMPLETO"
                UnitStatus.PENDENTE -> "⏳ PENDENTE"
            }
            sb.append("[${idx + 1}] Apto ${unit.apartment} (${unit.floor}º Andar) | $statusStr\n")
            sb.append("    Morador/Proprietário: ${if (unit.ownerName.isNotBlank()) unit.ownerName.uppercase() else "Não informado"}\n")
            sb.append("    Fone: ${unit.phone.ifBlank { "Não informado" }} | E-mail: ${unit.email.ifBlank { "Não informado" }}\n")
            if (unit.notes.isNotBlank()) {
                sb.append("    Obs: ${unit.notes}\n")
            }
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Administração: ________________________\n")
    sb.append("Assinatura Portaria/Recepção: ____________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

fun generateElegantOccurrenceReportText(item: OccurrenceWithMessages): String {
    val occ = item.occurrence
    val report = StringBuilder()
    report.append("════════════════════════════════════════\n")
    report.append("         🏢 CONDSUITES - GESTÃO CONDOMINIAL\n")
    report.append("         📋 RELATÓRIO DE OCORRÊNCIA\n")
    report.append("════════════════════════════════════════\n\n")
    report.append("📅 Data de Emissão: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    report.append("--------------------------------------------------\n")
    report.append("🚪 UNIDADE / LOCAL: ${occ.apartment}\n")
    report.append("👤 CRIADO POR: ${occ.createdByUsername.uppercase()}\n")
    report.append("📌 ASSUNTO / TÍTULO: ${occ.title}\n")
    report.append("📅 Data da Ocorrência: ${occ.date}\n")
    report.append("📊 Status: ${occ.status}\n")
    report.append("🚨 Urgência: ${if (occ.isUrgent) "⚡ URGENTE" else "Normal"}\n")
    report.append("--------------------------------------------------\n")
    report.append("💬 MENSAGENS E INTERAÇÕES:\n")
    if (item.messages.isEmpty()) {
        report.append("   Nenhuma mensagem registrada.\n")
    } else {
        item.messages.sortedBy { it.message.id }.forEach { msg ->
            report.append(" • [${msg.message.date}] ${msg.message.senderUsername.uppercase()}:\n   ${msg.message.text}\n\n")
        }
    }
    report.append("════════════════════════════════════════\n")
    report.append(" Responsável Técnico / Administração: _____\n")
    report.append("════════════════════════════════════════\n")
    return report.toString()
}

fun generateElegantGeneralOccurrencesReportText(title: String, occurrences: List<OccurrenceWithMessages>): String {
    val report = StringBuilder()
    report.append("════════════════════════════════════════\n")
    report.append("         🏢 CONDSUITES - GESTÃO CONDOMINIAL\n")
    report.append("         📊 $title\n")
    report.append("════════════════════════════════════════\n\n")
    report.append("📅 Data de Emissão: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    report.append("📋 Total de Ocorrências: ${occurrences.size}\n")
    report.append("--------------------------------------------------\n\n")

    occurrences.forEachIndexed { idx, item ->
        val occ = item.occurrence
        report.append("[${idx + 1}] Apto ${occ.apartment} - ${occ.title}\n")
        report.append("    Status: ${occ.status} | Urgência: ${if (occ.isUrgent) "URGENTE" else "Normal"} | Data: ${occ.date}\n")
        report.append("    Criado por: ${occ.createdByUsername} | Mensagens: ${item.messages.size}\n")
        report.append("    ----------------------------------------------\n")
    }

    report.append("\n════════════════════════════════════════\n")
    report.append(" Administração do Condomínio: ______________\n")
    report.append("════════════════════════════════════════\n")
    return report.toString()
}

fun shareDelinquencyHistoryReport(context: Context, historyList: List<DelinquencyHistoryEntity>, apartment: String? = null) {
    val report = StringBuilder()
    val title = if (apartment != null) "HISTÓRICO DE INADIMPLÊNCIA - APTO $apartment" else "RELATÓRIO GERAL DE INADIMPLÊNCIA"
    report.append("📋 $title\n==========================================\n\n")
    
    historyList.forEach { item ->
        report.append("📅 Data: ${item.date}\n")
        report.append("🏢 Unidade: ${item.apartment} | Proprietário: ${item.ownerName}\n")
        report.append("📌 Evento: ${item.eventType}\n")
        report.append("💰 Valor: R$ ${formatCurrency(item.value)}\n")
        report.append("------------------------------------------\n")
    }
    
    report.append("\nGerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Histórico"))
}

fun shareOccurrencesListReport(context: Context, title: String, items: List<OccurrenceWithMessages>) {
    val report = StringBuilder()
    report.append("📄 $title - CONDSUITES\n==========================================\n\n")
    if (items.isEmpty()) {
        report.append("Nenhuma ocorrência encontrada.\n")
    } else {
        items.forEach { item ->
            report.append("🆔 #${item.occurrence.id} | 🏢 ${item.occurrence.apartment}\n")
            report.append("📝 ${item.occurrence.title}\n")
            report.append("📅 Data: ${item.occurrence.date} | Status: ${item.occurrence.status}\n")
            if (item.occurrence.isUrgent) report.append("🚨 URGENTE\n")
            report.append("------------------------------------------\n")
        }
    }
    report.append("\nGerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
}

fun shareOccurrenceReport(context: Context, item: OccurrenceWithMessages) {
    val report = StringBuilder()
    report.append("📄 RELATÓRIO DE OCORRÊNCIA - CONDSUITES\n==========================================\n\n")
    report.append("🆔 OCORRÊNCIA: #${item.occurrence.id}\n")
    report.append("🏢 LOCAL: ${item.occurrence.apartment}\n")
    report.append("📝 ASSUNTO: ${item.occurrence.title}\n")
    report.append("📅 DATA: ${item.occurrence.date}\n")
    report.append("📊 STATUS: ${item.occurrence.status}\n")
    report.append("👤 CRIADO POR: ${item.occurrence.createdByUsername}\n")
    if (item.occurrence.isUrgent) {
        report.append("🚨 PRIORIDADE: URGENTE\n")
    }
    report.append("\n💬 CONVERSA:\n")
    item.messages.sortedBy { it.message.id }.forEach { msg ->
        report.append("------------------------------------------\n")
        report.append("[${msg.message.date}] ${msg.message.senderUsername}:\n")
        report.append("${msg.message.text}\n")
    }
    report.append("\n==========================================\n")
    report.append("Gerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Ocorrência"))
}

@Composable
fun ReportsOccurrencesScreen(dao: AppDao, context: Context) {
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }
    var showProfessionalDialog by remember { mutableStateOf(false) }
    var previewText by remember { mutableStateOf<String?>(null) }
    var previewTitle by remember { mutableStateOf("Relatório de Ocorrências") }
    val tabs = listOf("Abertas", "Fechadas", "Elevadores")
    
    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }
    var isYearly by remember { mutableStateOf(false) }

    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

    val openOccurrences = occurrences.filter { it.occurrence.status == "ABERTA" }
    val closedOccurrences = occurrences.filter { it.occurrence.status == "FINALIZADA" }
    val elevatorOccurrences = occurrences.filter { item ->
        val isElevator = item.occurrence.title.contains("Elevador", ignoreCase = true) || 
                       item.occurrence.occurrenceType == "Elevador" ||
                       item.occurrence.apartment == "CONDOMÍNIO"
        val date = try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(item.occurrence.date)
        } catch (_: Exception) { null }
        val cal = Calendar.getInstance()
        if (date != null) cal.time = date
        val matchesYear = cal.get(Calendar.YEAR) == selectedYear
        val matchesMonth = isYearly || cal.get(Calendar.MONTH) == selectedMonth
        isElevator && matchesYear && matchesMonth
    }

    val filtered = when (selectedTab) {
        0 -> openOccurrences
        1 -> closedOccurrences
        2 -> elevatorOccurrences
        else -> emptyList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Relatórios de Ocorrências", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Central de Relatórios e Compartilhamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Ocorrências Abertas (${openOccurrences.size})", fontWeight = FontWeight.SemiBold)
                            Text("Relatório de chamados abertos", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Row {
                            OutlinedButton(onClick = { selectedTab = 0 }) {
                                Text("Acessar")
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                previewText = generateElegantGeneralOccurrencesReportText("RELATÓRIO DE OCORRÊNCIAS ABERTAS", openOccurrences)
                                previewTitle = "Relatório de Ocorrências Abertas"
                            }) {
                                Icon(Icons.Default.Description, "Imprimir Abertas", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Ocorrências Fechadas (${closedOccurrences.size})", fontWeight = FontWeight.SemiBold)
                            Text("Relatório de chamados finalizados", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Row {
                            OutlinedButton(onClick = { selectedTab = 1 }) {
                                Text("Acessar")
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                previewText = generateElegantGeneralOccurrencesReportText("RELATÓRIO DE OCORRÊNCIAS FECHADAS", closedOccurrences)
                                previewTitle = "Relatório de Ocorrências Fechadas"
                            }) {
                                Icon(Icons.Default.Description, "Imprimir Fechadas", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Elevadores (${if (isYearly) "Anual: $selectedYear" else "Mensal: ${monthLabels[selectedMonth]}/$selectedYear"} - ${elevatorOccurrences.size})", fontWeight = FontWeight.SemiBold)
                            Text("Relatório de ocorrências em elevadores", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Row {
                            OutlinedButton(onClick = { selectedTab = 2 }) {
                                Text("Acessar")
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = {
                                val period = if (isYearly) "ANUAL ($selectedYear)" else "MENSAIS (${monthLabels[selectedMonth]}/$selectedYear)"
                                previewText = generateElegantGeneralOccurrencesReportText("RELATÓRIO DE OCORRÊNCIAS EM ELEVADORES - $period", elevatorOccurrences)
                                previewTitle = "Relatório de Ocorrências em Elevadores"
                            }) {
                                Icon(Icons.Default.Description, "Imprimir Elevadores", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { showProfessionalDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Description, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Relatório Profissional de Impressão (Filtros Avançados)")
                    }
                }
            }
        }

        item {
            Text("Visualização Detalhada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Column {
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                    }
                }
                
                if (selectedTab == 2) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Visão:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = !isYearly,
                            onClick = { isYearly = false },
                            label = { Text("Mensal") }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = isYearly,
                            onClick = { isYearly = true },
                            label = { Text("Anual") }
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        if (!isYearly) {
                            var expandedMonth by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { expandedMonth = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Mês: ${monthLabels[selectedMonth]}")
                                }
                                DropdownMenu(expanded = expandedMonth, onDismissRequest = { expandedMonth = false }) {
                                    monthLabels.forEachIndexed { index, label ->
                                        DropdownMenuItem(text = { Text(label) }, onClick = { selectedMonth = index; expandedMonth = false })
                                    }
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        
                        var expandedYear by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { expandedYear = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Ano: $selectedYear")
                            }
                            DropdownMenu(expanded = expandedYear, onDismissRequest = { expandedYear = false }) {
                                (2024..2030).forEach { year ->
                                    DropdownMenuItem(text = { Text(year.toString()) }, onClick = { selectedYear = year; expandedYear = false })
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma ocorrência encontrada nesta categoria.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filtered) { item ->
                ReportItemCard(
                    title = item.occurrence.title,
                    subtitle = "Local: ${item.occurrence.apartment} | Status: ${item.occurrence.status}",
                    value = "-",
                    date = item.occurrence.date
                ) {
                    shareOccurrenceReport(context, item)
                }
            }
        }
    }

    if (showProfessionalDialog) {
        ProfessionalReportDialog(dao, context) {
            showProfessionalDialog = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalReportDialog(dao: AppDao, context: Context, onDismiss: () -> Unit) {
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())
    
    var filterLocationType by remember { mutableStateOf("Todos") }
    var specificApto by remember { mutableStateOf("") }
    var filterSubject by remember { mutableStateOf("Todos") }
    var filterUrgent by remember { mutableStateOf("Todos") }
    var filterDate by remember { mutableStateOf("") }

    val allSubjects = remember(occurrences) { listOf("Todos") + occurrences.map { it.occurrence.title }.distinct().sorted() }
    var expandedSubject by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Relatório Profissional de Ocorrências") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Escolha os filtros para impressão e exportação:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Column {
                    Text("Local:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = filterLocationType == "Todos",
                            onClick = { filterLocationType = "Todos" },
                            label = { Text("Todos", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filterLocationType == "Unidade Específica",
                            onClick = { filterLocationType = "Unidade Específica" },
                            label = { Text("Unidade", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filterLocationType == "Condomínio",
                            onClick = { filterLocationType = "Condomínio" },
                            label = { Text("Condomínio", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (filterLocationType == "Unidade Específica") {
                    OutlinedTextField(
                        value = specificApto,
                        onValueChange = { specificApto = it },
                        label = { Text("Nº do Apartamento / Unidade") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expandedSubject,
                    onExpandedChange = { expandedSubject = !expandedSubject },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = filterSubject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assunto / Título") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSubject,
                        onDismissRequest = { expandedSubject = false }
                    ) {
                        allSubjects.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj) },
                                onClick = {
                                    filterSubject = subj
                                    expandedSubject = false
                                }
                            )
                        }
                    }
                }

                Column {
                    Text("Urgência:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = filterUrgent == "Todos",
                            onClick = { filterUrgent = "Todos" },
                            label = { Text("Todas", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filterUrgent == "Apenas Urgentes",
                            onClick = { filterUrgent = "Apenas Urgentes" },
                            label = { Text("Apenas Urgentes", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = filterUrgent == "Apenas Normais",
                            onClick = { filterUrgent = "Apenas Normais" },
                            label = { Text("Apenas Normais", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = filterDate,
                    onValueChange = { filterDate = it },
                    label = { Text("Data (ex: 14/09/2026 ou vazio para todas)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val filteredList = occurrences.filter { item ->
                        val matchesLocation = when (filterLocationType) {
                            "Unidade Específica" -> item.occurrence.apartment.equals(specificApto.trim(), ignoreCase = true)
                            "Condomínio" -> item.occurrence.apartment.equals("CONDOMÍNIO", ignoreCase = true)
                            else -> true
                        }
                        val matchesSubject = filterSubject == "Todos" || item.occurrence.title.equals(filterSubject, ignoreCase = true)
                        val matchesUrgency = when (filterUrgent) {
                            "Apenas Urgentes" -> item.occurrence.isUrgent
                            "Apenas Normais" -> !item.occurrence.isUrgent
                            else -> true
                        }
                        val matchesDate = filterDate.isBlank() || item.occurrence.date.contains(filterDate.trim())

                        matchesLocation && matchesSubject && matchesUrgency && matchesDate
                    }

                    val report = StringBuilder()
                    report.append("==================================================\n")
                    report.append("🏢 CONDSUITES - RELATÓRIO PROFISSIONAL DE OCORRÊNCIAS\n")
                    report.append("==================================================\n")
                    report.append("Gerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
                    report.append("--------------------------------------------------\n")
                    report.append("FILTROS SELECIONADOS:\n")
                    report.append("- Local: $filterLocationType ${if (filterLocationType == "Unidade Específica") "(Apto: $specificApto)" else ""}\n")
                    report.append("- Assunto: $filterSubject\n")
                    report.append("- Urgência: $filterUrgent\n")
                    report.append("- Data: ${if (filterDate.isBlank()) "Todas" else filterDate}\n")
                    report.append("Total de Ocorrências: ${filteredList.size}\n")
                    report.append("==================================================\n\n")

                    if (filteredList.isEmpty()) {
                        report.append("Nenhuma ocorrência encontrada com os filtros selecionados.\n")
                    } else {
                        filteredList.forEachIndexed { index, item ->
                            report.append("[${index + 1}] ID #${item.occurrence.id} | Local: ${item.occurrence.apartment}\n")
                            report.append("Assunto: ${item.occurrence.title}\n")
                            report.append("Data: ${item.occurrence.date} | Status: ${item.occurrence.status}\n")
                            report.append("Prioridade: ${if (item.occurrence.isUrgent) "🚨 URGENTE" else "Normal"}\n")
                            report.append("Criado por: ${item.occurrence.createdByUsername}\n")
                            if (item.messages.isNotEmpty()) {
                                report.append("Mensagens/Interações: ${item.messages.size}\n")
                            }
                            report.append("--------------------------------------------------\n")
                        }
                    }

                    report.append("\n==================================================\n")
                    report.append("Assinatura do Responsável: ________________________\n")
                    report.append("==================================================\n")

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, report.toString())
                    }
                    context.startActivity(Intent.createChooser(intent, "Imprimir / Compartilhar Relatório Profissional"))
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Description, null)
                Spacer(Modifier.width(8.dp))
                Text("Gerar e Imprimir Relatório")
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
fun ReportItemCard(title: String, subtitle: String, value: String, date: String, onShare: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodySmall); Text("Valor: R$ $value | Data: $date", style = MaterialTheme.typography.labelSmall) }
            IconButton(onShare) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary) }
        }
    }
}
