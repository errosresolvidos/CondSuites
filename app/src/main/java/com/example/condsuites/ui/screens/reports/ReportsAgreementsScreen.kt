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
import com.example.condsuites.data.model.AgreementWithInstallments
import com.example.condsuites.ui.components.AgreementsMetricCard
import com.example.condsuites.utils.calculateDaysDelay
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.naturalSortApartments
import com.example.condsuites.utils.printHtmlReport
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun shareAgreementReport(context: Context, agreement: AgreementWithInstallments, isDetailed: Boolean) {
    val report = StringBuilder()
    val typeName = if (isDetailed) "DETALHADO" else "RESUMIDO"
    report.append("📄 RELATÓRIO DE ACORDO ($typeName) - CONDSUITES\n==========================================\n\n")
    report.append("🏢 UNIDADE: ${agreement.agreement.apartment}\n👤 PROPRIETÁRIO: ${agreement.agreement.ownerName}\n")
    if (agreement.agreement.isLawsuit) {
        report.append("⚖️ STATUS: AJUIZADO\n")
    }
    if (agreement.agreement.n1Date != null || agreement.agreement.n2Date != null || agreement.agreement.n3Date != null) {
        report.append("📅 NOTIFICAÇÕES PRÉVIAS:\n")
        agreement.agreement.n1Date?.let { report.append("  - 1ª: $it\n") }
        agreement.agreement.n2Date?.let { report.append("  - 2ª: $it\n") }
        agreement.agreement.n3Date?.let { report.append("  - 3ª: $it\n") }
    }
    report.append("💰 DÍVIDA: R$ ${formatCurrency(agreement.agreement.totalDebt)}\n")
    val paid = agreement.installments.count { it.isPaid }
    val remaining = agreement.installments.filter { !it.isPaid }.sumOf { it.value }
    report.append("✅ PAGAS: $paid/${agreement.agreement.installmentsCount}\n🔻 RESTANTE: R$ ${formatCurrency(remaining)}\n")
    if (isDetailed) {
        report.append("\n📋 PARCELAS:\n")
        agreement.installments.sortedBy { it.number }.forEach { inst ->
            report.append("${inst.number}/${agreement.agreement.installmentsCount} | ${inst.dueDate} | R$ ${formatCurrency(inst.value)} | ${if (inst.isPaid) "PAGO" else "ABERTO"}\n")
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
}

fun generateElegantAgreementReportText(agreement: AgreementWithInstallments): String {
    val report = StringBuilder()
    report.append("════════════════════════════════════════\n")
    report.append("         🏢 CONDSUITES - GESTÃO CONDOMINIAL\n")
    report.append("         📄 COMPROVANTE / EXTRATO DE ACORDO\n")
    report.append("════════════════════════════════════════\n\n")
    report.append("📅 Data de Emissão: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    report.append("--------------------------------------------------\n")
    report.append("🚪 UNIDADE / APARTAMENTO: ${agreement.agreement.apartment}\n")
    report.append("👤 PROPRIETÁRIO: ${agreement.agreement.ownerName.uppercase()}\n")
    report.append("📝 Data do Acordo: ${agreement.agreement.date}\n")
    if (agreement.agreement.quotaMonths.isNotBlank()) {
        report.append("📋 Cotas Condominiais Acordadas:\n   ${agreement.agreement.quotaMonths.replace(";", ", ")}\n")
    }
    if (agreement.agreement.isLawsuit) {
        report.append("⚖️ Status Jurídico: 🚨 AJUIZADO\n")
    }
    report.append("--------------------------------------------------\n")
    report.append("💰 DÍVIDA TOTAL ACORDADA: R$ ${formatCurrency(agreement.agreement.totalDebt)}\n")
    val paid = agreement.installments.count { it.isPaid }
    val remaining = agreement.installments.filter { !it.isPaid }.sumOf { it.value }
    report.append("✅ Parcelas Pagas: $paid / ${agreement.agreement.installmentsCount}\n")
    report.append("🔻 Saldo Restante: R$ ${formatCurrency(remaining)}\n")
    report.append("--------------------------------------------------\n")
    report.append("📋 CRONOGRAMA DE PARCELAS:\n")
    agreement.installments.sortedBy { it.number }.forEach { inst ->
        val status = if (inst.isPaid) "✅ PAGO" else "⏳ ABERTO"
        report.append(" • Parcela ${inst.number}/${agreement.agreement.installmentsCount} | Venc: ${inst.dueDate} | R$ ${formatCurrency(inst.value)} | $status\n")
    }
    report.append("\n════════════════════════════════════════\n")
    report.append(" Assinatura do Condômino: ____________________\n\n")
    report.append(" Assinatura da Administração: _________________\n")
    report.append("════════════════════════════════════════\n")
    return report.toString()
}

fun generateElegantGeneralAgreementsReportText(agreements: List<AgreementWithInstallments>): String {
    val report = StringBuilder()
    report.append("════════════════════════════════════════\n")
    report.append("         🏢 CONDSUITES - GESTÃO CONDOMINIAL\n")
    report.append("         📊 RELATÓRIO GERAL DE ACORDOS\n")
    report.append("════════════════════════════════════════\n\n")
    report.append("📅 Data de Emissão: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
    val totalAgreed = agreements.sumOf { it.agreement.totalDebt }
    val totalRemaining = agreements.flatMap { it.installments }.filter { !it.isPaid }.sumOf { it.value }
    report.append("📋 Total de Acordos Ativos: ${agreements.size}\n")
    report.append("💰 Valor Total Acordado: R$ ${formatCurrency(totalAgreed)}\n")
    report.append("🔻 Valor Total Pendente: R$ ${formatCurrency(totalRemaining)}\n")
    report.append("--------------------------------------------------\n\n")

    agreements.sortedBy { naturalSortApartments(it.agreement.apartment) }.forEachIndexed { idx, item ->
        val paidCount = item.installments.count { it.isPaid }
        val rem = item.installments.filter { !it.isPaid }.sumOf { it.value }
        report.append("[${idx + 1}] Apto ${item.agreement.apartment} - ${item.agreement.ownerName.uppercase()}\n")
        report.append("    Dívida: R$ ${formatCurrency(item.agreement.totalDebt)} | Pago: $paidCount/${item.agreement.installmentsCount} | Restante: R$ ${formatCurrency(rem)}\n")
        if (item.agreement.quotaMonths.isNotBlank()) {
            report.append("    Cotas: ${item.agreement.quotaMonths.replace(";", ", ")}\n")
        }
        report.append("    ----------------------------------------------\n")
    }

    report.append("\n════════════════════════════════════════\n")
    report.append(" Responsável Financeiro: ____________________\n")
    report.append("════════════════════════════════════════\n")
    return report.toString()
}

fun shareElegantAgreementReport(context: Context, agreement: AgreementWithInstallments) {
    val text = generateElegantAgreementReportText(agreement)
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Imprimir / Compartilhar Relatório Elegante de Acordo"))
}

fun shareElegantGeneralAgreementsReport(context: Context, agreements: List<AgreementWithInstallments>) {
    val text = generateElegantGeneralAgreementsReportText(agreements)
    val intent = Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Imprimir / Compartilhar Relatório Geral de Acordos"))
}

fun generateAgreementsHtmlReport(
    agreements: List<AgreementWithInstallments>,
    statusFilterText: String,
    periodText: String,
    valueRangeText: String
): String {
    val totalCount = agreements.size
    val totalAgreedDebt = agreements.sumOf { it.agreement.totalDebt }
    val totalPaidValue = agreements.sumOf { item ->
        item.installments.filter { it.isPaid }.sumOf { it.value }
    }
    val totalRemainingValue = agreements.sumOf { item ->
        item.installments.filter { !it.isPaid }.sumOf { it.value }
    }
    val clearanceRate = if (totalAgreedDebt > 0) (totalPaidValue * 100 / totalAgreedDebt).toInt() else 0
    val lawsuitCount = agreements.count { it.agreement.isLawsuit }
    val overdueCount = agreements.count { item ->
        item.installments.any { !it.isPaid && calculateDaysDelay(it.dueDate) > 0 }
    }
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val html = StringBuilder()
    html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Relatório Gerencial de Acordos</title>
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 20px; line-height: 1.5; }
                .header { text-align: center; border-bottom: 2px solid #2E7D32; padding-bottom: 12px; margin-bottom: 20px; }
                .header h1 { margin: 0; color: #2E7D32; font-size: 20px; text-transform: uppercase; }
                .header h2 { margin: 4px 0 0 0; color: #555; font-size: 14px; font-weight: normal; }
                .meta-table { width: 100%; margin-bottom: 16px; font-size: 12px; background: #F8F9FA; border-radius: 6px; padding: 10px; border: 1px solid #E0E0E0; }
                .kpi-container { display: table; width: 100%; margin-bottom: 20px; table-layout: fixed; }
                .kpi-box { display: table-cell; text-align: center; padding: 10px; background: #E8F5E9; border: 1px solid #C8E6C9; border-radius: 6px; }
                .kpi-title { font-size: 10px; text-transform: uppercase; color: #1B5E20; font-weight: bold; }
                .kpi-value { font-size: 15px; font-weight: bold; color: #2E7D32; margin-top: 4px; }
                .section-title { font-size: 15px; color: #1B5E20; border-bottom: 1px solid #CCC; padding-bottom: 4px; margin-top: 20px; margin-bottom: 10px; font-weight: bold; }
                table.data-table { width: 100%; border-collapse: collapse; font-size: 11px; margin-top: 8px; }
                table.data-table th { background: #2E7D32; color: white; padding: 7px; text-align: left; }
                table.data-table td { border-bottom: 1px solid #DDD; padding: 7px; vertical-align: top; }
                table.data-table tr:nth-child(even) { background: #F9F9F9; }
                .badge { display: inline-block; padding: 2px 6px; font-size: 9px; font-weight: bold; border-radius: 4px; color: white; }
                .badge-quitado { background: #2E7D32; }
                .badge-emdia { background: #0288D1; }
                .badge-ematraso { background: #E65100; }
                .badge-ajuizado { background: #C62828; }
                .signatures { margin-top: 40px; width: 100%; page-break-inside: avoid; }
                .sig-box { width: 45%; display: inline-block; text-align: center; font-size: 11px; margin-top: 20px; }
                .sig-line { border-top: 1px solid #333; margin-bottom: 4px; width: 80%; margin-left: auto; margin-right: auto; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🏢 CONDSUITES - GESTÃO CONDOMINIAL</h1>
                <h2>Relatório Gerencial de Acordos & Diagnóstico Financeiro</h2>
            </div>

            <div class="meta-table">
                <strong>Emissão:</strong> $nowStr &nbsp;|&nbsp; 
                <strong>Status Filtro:</strong> $statusFilterText &nbsp;|&nbsp; 
                <strong>Período:</strong> $periodText &nbsp;|&nbsp;
                <strong>Faixa de Valor:</strong> $valueRangeText
            </div>

            <div class="kpi-container">
                <div class="kpi-box">
                    <div class="kpi-title">Total Acordos</div>
                    <div class="kpi-value">$totalCount</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Valor Acordado</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalAgreedDebt)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Arrecadado</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalPaidValue)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Saldo Pendente</div>
                    <div class="kpi-value">R$ ${formatCurrency(totalRemainingValue)}</div>
                </div>
                <div class="kpi-box">
                    <div class="kpi-title">Taxa Quitação</div>
                    <div class="kpi-value">$clearanceRate%</div>
                </div>
            </div>

            <div class="section-title">📊 Análise Profissional & Diagnóstico Financeiro</div>
            <p style="font-size: 11px; color: #444; margin: 4px 0 12px 0;">
                Este relatório analítico apresenta o desempenho da recuperação de crédito do condomínio.
                Atualmente, do total negociado de <strong>R$ ${formatCurrency(totalAgreedDebt)}</strong>, já foram arrecadados <strong>R$ ${formatCurrency(totalPaidValue)}</strong> (taxa de quitação de <strong>$clearanceRate%</strong>), restando um saldo devedor de <strong>R$ ${formatCurrency(totalRemainingValue)}</strong>.
                ${if (overdueCount > 0) "Observam-se <strong>$overdueCount acordo(s) com parcelas em atraso</strong> que necessitam de acompanhamento intensivo ou renegociação para evitar inadimplência persistente." else "Todos os acordos vigentes estão adimplentes e sem atraso registrado nas parcelas."}
                ${if (lawsuitCount > 0) " Adicionalmente, <strong>$lawsuitCount acordo(s)</strong> possuem cobrança judicial/ajuizada associada." else ""}
            </p>

            <div class="section-title">📋 Detalhamento dos Acordos</div>
    """.trimIndent())

    if (agreements.isEmpty()) {
        html.append("<p style='text-align:center; padding:16px; color:#777;'>Nenhum acordo encontrado para os filtros selecionados.</p>")
    } else {
        html.append("""
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 4%;">#</th>
                        <th style="width: 10%;">Apto</th>
                        <th style="width: 22%;">Proprietário</th>
                        <th style="width: 12%;">Data Acordo</th>
                        <th style="width: 16%;">Dívida Total</th>
                        <th style="width: 16%;">Valor Pago / Restante</th>
                        <th style="width: 10%;">Parcelas</th>
                        <th style="width: 10%;">Status</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent())

        agreements.sortedBy { naturalSortApartments(it.agreement.apartment) }.forEachIndexed { idx, item ->
            val agg = item.agreement
            val paidCount = item.installments.count { it.isPaid }
            val paidSum = item.installments.filter { it.isPaid }.sumOf { it.value }
            val remSum = item.installments.filter { !it.isPaid }.sumOf { it.value }
            val isAllPaid = item.installments.isNotEmpty() && item.installments.all { it.isPaid }
            val hasOverdue = item.installments.any { !it.isPaid && calculateDaysDelay(it.dueDate) > 0 }

            val statusText: String
            val badgeClass: String
            if (isAllPaid) {
                statusText = "QUITADO"
                badgeClass = "badge-quitado"
            } else if (agg.isLawsuit) {
                statusText = "AJUIZADO"
                badgeClass = "badge-ajuizado"
            } else if (hasOverdue) {
                statusText = "EM ATRASO"
                badgeClass = "badge-ematraso"
            } else {
                statusText = "EM DIA"
                badgeClass = "badge-emdia"
            }

            html.append("""
                <tr>
                    <td>${idx + 1}</td>
                    <td><strong>Apto ${agg.apartment}</strong></td>
                    <td>${agg.ownerName.uppercase()}</td>
                    <td>${agg.date}</td>
                    <td>R$ ${formatCurrency(agg.totalDebt)}</td>
                    <td><small style='color:#2E7D32'>R$ ${formatCurrency(paidSum)}</small><br><small style='color:#C62828'>Rest: R$ ${formatCurrency(remSum)}</small></td>
                    <td>$paidCount/${agg.installmentsCount}</td>
                    <td><span class="badge $badgeClass">$statusText</span></td>
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
                <strong>Responsável Financeiro</strong><br>
                Departamento de Cobrança / Acordos
            </div>
        </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun generateAgreementsTextReport(
    agreements: List<AgreementWithInstallments>,
    statusFilterText: String,
    periodText: String
): String {
    val totalCount = agreements.size
    val totalAgreed = agreements.sumOf { it.agreement.totalDebt }
    val totalPaid = agreements.sumOf { item -> item.installments.filter { it.isPaid }.sumOf { it.value } }
    val totalRemaining = agreements.sumOf { item -> item.installments.filter { !it.isPaid }.sumOf { it.value } }
    val clearanceRate = if (totalAgreed > 0) (totalPaid * 100 / totalAgreed).toInt() else 0
    val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val sb = StringBuilder()
    sb.append("══════════════════════════════════════════════════\n")
    sb.append("🏢 CONDSUITES - RELATÓRIO DE ACORDOS\n")
    sb.append("📋 GESTÃO CONDOMINIAL & RECUPERAÇÃO DE CRÉDITO\n")
    sb.append("══════════════════════════════════════════════════\n\n")
    sb.append("📅 Data de Emissão: $nowStr\n")
    sb.append("🔍 Status: $statusFilterText | Período: $periodText\n")
    sb.append("--------------------------------------------------\n")
    sb.append("📊 RESUMO FINANCEIRO / POSIÇÃO DE CRÉDITO:\n")
    sb.append(" • Total de Acordos Registrados: $totalCount\n")
    sb.append(" • Valor Total Negociado: R$ ${formatCurrency(totalAgreed)}\n")
    sb.append(" • Valor Arrecadado / Pago: R$ ${formatCurrency(totalPaid)}\n")
    sb.append(" • Saldo Pendente: R$ ${formatCurrency(totalRemaining)}\n")
    sb.append(" • Taxa de Quitação: $clearanceRate%\n")
    sb.append("--------------------------------------------------\n\n")

    if (agreements.isEmpty()) {
        sb.append("Nenhum acordo encontrado para os filtros selecionados.\n")
    } else {
        agreements.sortedBy { naturalSortApartments(it.agreement.apartment) }.forEachIndexed { idx, item ->
            val agg = item.agreement
            val paidCount = item.installments.count { it.isPaid }
            val remSum = item.installments.filter { !it.isPaid }.sumOf { it.value }
            sb.append("[${idx + 1}] Apto ${agg.apartment} - ${agg.ownerName.uppercase()}\n")
            sb.append("    Data: ${agg.date} | Total Dívida: R$ ${formatCurrency(agg.totalDebt)}\n")
            sb.append("    Parcelas: $paidCount/${agg.installmentsCount} pagas | Restante: R$ ${formatCurrency(remSum)}\n")
            if (agg.isLawsuit) {
                sb.append("    ⚖️ Status: AJUIZADO\n")
            }
            sb.append("    ----------------------------------------------\n")
        }
    }

    sb.append("\n==================================================\n")
    sb.append("Assinatura Síndico: ______________________________\n")
    sb.append("Assinatura Financeiro: ___________________________\n")
    sb.append("==================================================\n")

    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsAgreementsScreen(dao: AppDao, context: Context) {
    val agreements by dao.getActiveAgreements().collectAsState(initial = emptyList())

    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedPeriodFilter by remember { mutableStateOf("Todos") }
    var selectedValueRangeFilter by remember { mutableStateOf("Todos") }

    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCalendar.get(Calendar.YEAR)) }

    var showAdvancedFiltersDialog by remember { mutableStateOf(false) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }
    var expandedCardId by remember { mutableStateOf<Long?>(null) }

    val monthLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

    val filteredList = remember(
        agreements, searchText, selectedStatusFilter, selectedPeriodFilter, selectedValueRangeFilter, selectedMonth, selectedYear
    ) {
        agreements.filter { item ->
            val agg = item.agreement
            val isAllPaid = item.installments.isNotEmpty() && item.installments.all { it.isPaid }
            val hasOverdue = item.installments.any { !it.isPaid && calculateDaysDelay(it.dueDate) > 0 }

            val matchesStatus = when (selectedStatusFilter) {
                "Em Dia" -> !isAllPaid && !hasOverdue && !agg.isLawsuit
                "Em Atraso" -> hasOverdue && !isAllPaid
                "Quitados" -> isAllPaid
                "Ajuizados" -> agg.isLawsuit
                else -> true
            }

            val matchesPeriod = if (selectedPeriodFilter == "Todos") {
                true
            } else {
                val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(agg.date) } catch (_: Exception) { null }
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
                "Até R$ 1.000" -> agg.totalDebt <= 1000.0
                "R$ 1.000 a R$ 5.000" -> agg.totalDebt in 1000.0..5000.0
                "R$ 5.000 a R$ 10.000" -> agg.totalDebt in 5000.0..10000.0
                "Acima de R$ 10.000" -> agg.totalDebt > 10000.0
                else -> true
            }

            val matchesSearch = searchText.isBlank() ||
                agg.apartment.contains(searchText, ignoreCase = true) ||
                agg.ownerName.contains(searchText, ignoreCase = true) ||
                agg.quotaMonths.contains(searchText, ignoreCase = true)

            matchesStatus && matchesPeriod && matchesValue && matchesSearch
        }
    }

    val totalCount = filteredList.size
    val totalAgreed = filteredList.sumOf { it.agreement.totalDebt }
    val totalPaid = filteredList.sumOf { item -> item.installments.filter { it.isPaid }.sumOf { it.value } }
    val totalRemaining = filteredList.sumOf { item -> item.installments.filter { !it.isPaid }.sumOf { it.value } }
    val clearanceRate = if (totalAgreed > 0) (totalPaid * 100 / totalAgreed).toInt() else 0
    val lawsuitCount = filteredList.count { it.agreement.isLawsuit }

    val activeFilterCount = (if (selectedStatusFilter != "Todos") 1 else 0) +
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
                    Text("Painel de Indicadores de Acordos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AgreementsMetricCard("Total Acordos", totalCount.toString(), Icons.Filled.Handshake, Modifier.weight(1f))
                        AgreementsMetricCard("Total Acordado", "R$ ${formatCurrency(totalAgreed)}", Icons.Default.AccountBalance, Modifier.weight(1.2f))
                        AgreementsMetricCard("Arrecadado", "R$ ${formatCurrency(totalPaid)}", Icons.Default.CheckCircle, Modifier.weight(1.2f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AgreementsMetricCard("Saldo Pendente", "R$ ${formatCurrency(totalRemaining)}", Icons.Default.ReceiptLong, Modifier.weight(1f))
                        AgreementsMetricCard("Taxa Quitação", "$clearanceRate%", Icons.Default.Analytics, Modifier.weight(1f))
                        AgreementsMetricCard("Ajuizados ⚖️", lawsuitCount.toString(), Icons.Default.Gavel, Modifier.weight(1f))
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
                        placeholder = { Text("Buscar apto ou nome...") },
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
                    FilterChip(selected = selectedStatusFilter == "Em Dia", onClick = { selectedStatusFilter = "Em Dia" }, label = { Text("Em Dia", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Em Atraso", onClick = { selectedStatusFilter = "Em Atraso" }, label = { Text("Atraso", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedStatusFilter == "Quitados", onClick = { selectedStatusFilter = "Quitados" }, label = { Text("Quitados", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                }

                if (activeFilterCount > 0 || searchText.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Filtros ativos ($totalCount resultados)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        TextButton(onClick = {
                            searchText = ""
                            selectedStatusFilter = "Todos"
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
                            val textReport = generateAgreementsTextReport(filteredList, selectedStatusFilter, periodText)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, textReport)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Acordos"))
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
            Text("Acordos Encontrados (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum acordo encontrado para os filtros selecionados.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList.sortedBy { naturalSortApartments(it.agreement.apartment) }) { item ->
                val isExpanded = expandedCardId == item.agreement.id

                AgreementReportCard(
                    item = item,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedCardId = if (isExpanded) null else item.agreement.id
                    },
                    onShare = {
                        val text = generateElegantAgreementReportText(item)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartilhar Acordo"))
                    }
                )
            }
        }
    }

    if (showAdvancedFiltersDialog) {
        AgreementsAdvancedFiltersDialog(
            selectedStatusFilter = selectedStatusFilter,
            onStatusChange = { selectedStatusFilter = it },
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
        val htmlContent = generateAgreementsHtmlReport(filteredList, selectedStatusFilter, periodText, selectedValueRangeFilter)
        val textReport = generateAgreementsTextReport(filteredList, selectedStatusFilter, periodText)

        AgreementsReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredList.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Relatorio_Geral_Acordos")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório de Acordos"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun AgreementReportCard(
    item: AgreementWithInstallments,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onShare: () -> Unit
) {
    val agg = item.agreement
    val paidCount = item.installments.count { it.isPaid }
    val paidSum = item.installments.filter { it.isPaid }.sumOf { it.value }
    val remSum = item.installments.filter { !it.isPaid }.sumOf { it.value }
    val isAllPaid = item.installments.isNotEmpty() && item.installments.all { it.isPaid }
    val hasOverdue = item.installments.any { !it.isPaid && calculateDaysDelay(it.dueDate) > 0 }

    val (statusText, statusColor) = when {
        isAllPaid -> "QUITADO" to Color(0xFF2E7D32)
        agg.isLawsuit -> "AJUIZADO ⚖️" to Color(0xFFC62828)
        hasOverdue -> "EM ATRASO 🚨" to Color(0xFFE65100)
        else -> "EM DIA" to Color(0xFF0288D1)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Apto ${agg.apartment} • ${agg.ownerName.uppercase()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Dívida: R$ ${formatCurrency(agg.totalDebt)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text("Pago: R$ ${formatCurrency(paidSum)} | Resta: R$ ${formatCurrency(remSum)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Data do Acordo: ${agg.date}", style = MaterialTheme.typography.labelMedium)
                if (agg.quotaMonths.isNotBlank()) {
                    Text("Cotas Acordadas: ${agg.quotaMonths.replace(";", ", ")}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(Modifier.height(6.dp))
                Text("Cronograma de Parcelas ($paidCount/${agg.installmentsCount}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                item.installments.sortedBy { it.number }.forEach { inst ->
                    val instPaid = inst.isPaid
                    val instColor = if (instPaid) Color(0xFF2E7D32) else Color(0xFFC62828)
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(Modifier.padding(8.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("${inst.number}/${agg.installmentsCount} • Venc: ${inst.dueDate}", style = MaterialTheme.typography.bodySmall)
                            Text("R$ ${formatCurrency(inst.value)} • ${if (instPaid) "PAGO" else "EM ABERTO"}", style = MaterialTheme.typography.bodySmall, color = instColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgreementsAdvancedFiltersDialog(
    selectedStatusFilter: String,
    onStatusChange: (String) -> Unit,
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
        title = { Text("Busca Avançada de Acordos") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text("Status do Acordo:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val statuses = listOf("Todos", "Em Dia", "Em Atraso", "Quitados", "Ajuizados")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        statuses.take(3).forEach { st ->
                            FilterChip(selected = selectedStatusFilter == st, onClick = { onStatusChange(st) }, label = { Text(st, style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        statuses.drop(3).forEach { st ->
                            FilterChip(selected = selectedStatusFilter == st, onClick = { onStatusChange(st) }, label = { Text(st, style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Column {
                    Text("Faixa de Valor da Dívida:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val ranges = listOf("Todos", "Até R$ 1.000", "R$ 1.000 a R$ 5.000", "R$ 5.000 a R$ 10.000", "Acima de R$ 10.000")
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
fun AgreementsReportPreviewDialog(
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
                Text("Relatório gerado com $totalCount acordo(s). Próximo passo:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

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
