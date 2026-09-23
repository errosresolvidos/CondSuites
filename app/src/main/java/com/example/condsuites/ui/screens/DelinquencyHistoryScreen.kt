package com.example.condsuites.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.DelinquencyHistoryEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.ui.screens.reports.shareDelinquencyHistoryReport
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.naturalSortApartments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DelinquencyHistoryScreen(
    dao: AppDao,
    currentUser: UserEntity? = null
) {
    val historyList by dao.getDelinquencyHistory().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchText by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("Todos") }
    var historyToDelete by remember { mutableStateOf<DelinquencyHistoryEntity?>(null) }

    val eventTypes = remember(historyList) {
        listOf("Todos") + historyList.map { it.eventType }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredList = remember(historyList, searchText, selectedTypeFilter) {
        historyList.filter { item ->
            val matchesType = selectedTypeFilter == "Todos" || item.eventType.equals(selectedTypeFilter, ignoreCase = true)
            val fullText = "${item.apartment} ${item.ownerName} ${item.description} ${item.eventType} ${item.date}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesType && matchesSearch
        }
    }

    val totalValue = filteredList.sumOf { it.value }

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
                    Text("Histórico Geral de Inadimplência", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Registros de Histórico", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("${filteredList.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Valor Acumulado", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("R$ ${formatCurrency(totalValue)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
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
                        placeholder = { Text("Buscar por apto, nome ou descrição...") },
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

                    IconButton(
                        onClick = {
                            shareDelinquencyHistoryReport(context, filteredList)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Share, "Compartilhar", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (eventTypes.size > 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        eventTypes.take(4).forEach { type ->
                            FilterChip(
                                selected = selectedTypeFilter == type,
                                onClick = { selectedTypeFilter = type },
                                label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("Eventos Registrados (${filteredList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredList.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum histórico encontrado.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredList.sortedBy { naturalSortApartments(it.apartment) }) { item ->
                DelinquencyHistoryCard(
                    item = item,
                    currentUser = currentUser,
                    onDelete = { historyToDelete = item }
                )
            }
        }
    }

    if (historyToDelete != null) {
        val itemToDel = historyToDelete!!
        AlertDialog(
            onDismissRequest = { historyToDelete = null },
            title = { Text("Excluir do Histórico") },
            text = { Text("Deseja mesmo excluir este registro do histórico do Apto ${itemToDel.apartment}?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            dao.deleteHistory(itemToDel.id)
                            FirestoreSyncManager.syncDelinquencyHistory(itemToDel, isDelete = true)
                        }
                        historyToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { historyToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DelinquencyHistoryCard(
    item: DelinquencyHistoryEntity,
    currentUser: UserEntity? = null,
    onDelete: () -> Unit = {}
) {
    val (badgeColor, textColor) = when (item.eventType) {
        "Acordo" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Notificação" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "Processo" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
    }

    val canDelete = currentUser?.role == "ADMIN" || currentUser?.role == "Síndico"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Apto ${item.apartment} • ${item.ownerName.uppercase()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            item.eventType.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Histórico",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("📅 Data: ${item.date}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

            if (item.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(item.description, style = MaterialTheme.typography.bodySmall)
            }

            if (item.value > 0) {
                Spacer(Modifier.height(6.dp))
                Text("Valor do Evento: R$ ${formatCurrency(item.value)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
