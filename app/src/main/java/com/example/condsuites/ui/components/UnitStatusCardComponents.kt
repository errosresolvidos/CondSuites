package com.example.condsuites.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.model.AgreementWithInstallments
import com.example.condsuites.data.model.DelinquentWithProgress
import com.example.condsuites.data.model.DelinquencyHistoryEntity
import com.example.condsuites.data.model.LawsuitWithProgress
import com.example.condsuites.ui.navigation.Screen
import com.example.condsuites.utils.formatCurrency

@Composable
fun ActiveDebtorBanner(
    delinquent: DelinquentWithProgress?,
    agreement: AgreementWithInstallments?,
    lawsuit: LawsuitWithProgress?,
    onNavigate: (Screen) -> Unit,
    onNavigateToAgreements: (apartment: String, tab: Int) -> Unit = { _, _ -> }
) {
    Surface(
        color = Color(0xFFFFF0F0),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Histórico de Cobrança",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = Color(0xFFD32F2F),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "UNIDADE DEVEDORA",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (delinquent != null) {
                val notifLevel = when {
                    delinquent.delinquent.notification3Date != null -> "3ª Notificação Prévias"
                    delinquent.delinquent.notification2Date != null -> "2ª Notificação Prévias"
                    delinquent.delinquent.notification1Date != null -> "1ª Notificação Prévias"
                    else -> "Em Notificação"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• Notificação: $notifLevel (R$ ${formatCurrency(delinquent.delinquent.totalDebt)})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB71C1C),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            onNavigateToAgreements(delinquent.delinquent.apartment, 0)
                            onNavigate(Screen.Agreements)
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver Notificação", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (agreement != null) {
                val paid = agreement.installments.count { it.isPaid }
                val total = agreement.agreement.installmentsCount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• Acordo: Ativo ($paid/$total parcelas pagas)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            onNavigateToAgreements(agreement.agreement.apartment, 1)
                            onNavigate(Screen.Agreements)
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver Acordo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (lawsuit != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• Ajuizado: Processo ${lawsuit.lawsuit.processNumber.ifBlank { "Sem número" }} (${lawsuit.lawsuit.status})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            onNavigateToAgreements(lawsuit.lawsuit.apartment, 2)
                            onNavigate(Screen.Agreements)
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver Ajuizado", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ArchivedProcessBanner(
    hasArchivedDelinquent: Boolean,
    hasArchivedAgreement: Boolean,
    archivedLawsuit: LawsuitWithProgress?,
    delinquencyHistory: List<DelinquencyHistoryEntity>,
    onNavigate: (Screen) -> Unit,
    onNavigateToAgreements: (apartment: String, tab: Int) -> Unit = { _, _ -> }
) {
    Surface(
        color = Color(0xFFECEFF1),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFCFD8DC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF455A64), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Histórico de Processos Arquivados / Concluídos",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = Color(0xFF546E7A),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "HISTÓRICO ARQUIVADO",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (hasArchivedDelinquent) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• Notificação Prévias Arquivada",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF455A64),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            delinquencyHistory.firstOrNull()?.apartment?.let { onNavigateToAgreements(it, 0) }
                            onNavigate(Screen.Agreements)
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver Arquivados", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (hasArchivedAgreement) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• Acordo de Cobrança Arquivado / Quitado",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF455A64),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            delinquencyHistory.firstOrNull()?.apartment?.let { onNavigateToAgreements(it, 1) }
                            onNavigate(Screen.Agreements)
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver Arquivados", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (archivedLawsuit != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• Processo Ajuizado Arquivado / Finalizado (${archivedLawsuit.lawsuit.status})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF455A64),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            onNavigateToAgreements(archivedLawsuit.lawsuit.apartment, 2)
                            onNavigate(Screen.Agreements)
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver Ajuizados", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (delinquencyHistory.isNotEmpty() && !hasArchivedDelinquent && !hasArchivedAgreement && archivedLawsuit == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${delinquencyHistory.size} evento(s) no Histórico de Inadimplência",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF455A64),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onNavigate(Screen.DelinquencyHistory) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver Histórico", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
