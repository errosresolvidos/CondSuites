package com.example.condsuites.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.OccurrenceWithMessages
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOccurrencesScreen(
    dao: AppDao,
    currentUser: UserEntity,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val occurrences by dao.getAllOccurrences().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: Todas, 1: Geral, 2: Conselho, 3: Arquivadas
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val filteredOccurrences = remember(occurrences, searchQuery, selectedFilter) {
        occurrences.filter { occWithMsgs ->
            val occ = occWithMsgs.occurrence
            val isArchived = occ.status == "ARQUIVADA"
            val matchesFilter = when (selectedFilter) {
                1 -> occ.type == "GERAL" && !isArchived
                2 -> occ.type == "CONSELHO" && !isArchived
                3 -> isArchived
                else -> true
            }

            val matchesSearch = searchQuery.isBlank() ||
                    occ.title.contains(searchQuery, ignoreCase = true) ||
                    occ.apartment.contains(searchQuery, ignoreCase = true) ||
                    occ.createdByUsername.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    val allFilteredSelected = remember(filteredOccurrences, selectedIds) {
        filteredOccurrences.isNotEmpty() && filteredOccurrences.all { selectedIds.contains(it.occurrence.id) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Excluir Ocorrências",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Gestão do Administrador: Selecione para excluir permanentemente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por título, unidade ou autor") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Todas", "Zelador", "Conselho", "Arquivadas").forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedFilter == index,
                        onClick = { selectedFilter = index },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${filteredOccurrences.size} ocorrência(s) encontrada(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                if (filteredOccurrences.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            selectedIds = if (allFilteredSelected) {
                                emptySet()
                            } else {
                                filteredOccurrences.map { it.occurrence.id }.toSet()
                            }
                        }
                    ) {
                        Text(if (allFilteredSelected) "Desmarcar Todas" else "Marcar Todas")
                    }
                }
            }

            if (selectedIds.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${selectedIds.size} selecionada(s)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Excluir Selecionadas")
                        }
                    }
                }
            }

            if (filteredOccurrences.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma ocorrência encontrada.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredOccurrences) { occWithMsgs ->
                        val occ = occWithMsgs.occurrence
                        val isChecked = selectedIds.contains(occ.id)

                        AdminOccurrenceCard(
                            occWithMsgs = occWithMsgs,
                            isChecked = isChecked,
                            onToggleCheck = { checked ->
                                selectedIds = if (checked) selectedIds + occ.id else selectedIds - occ.id
                            },
                            onDeleteSingle = {
                                selectedIds = setOf(occ.id)
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog && selectedIds.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir ${selectedIds.size} Ocorrência(s)") },
            text = {
                Text("Esta ação irá remover permanentemente as ocorrências e todas as suas mensagens/anexos do aplicativo e da nuvem Firebase. Deseja continuar?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idsToDelete = selectedIds.toList()
                        scope.launch(Dispatchers.IO) {
                            idsToDelete.forEach { id ->
                                val occ = occurrences.firstOrNull { it.occurrence.id == id }?.occurrence
                                dao.deleteOccurrence(id)
                                if (occ != null) {
                                    FirestoreSyncManager.syncOccurrence(occ, isDelete = true)
                                }
                            }
                        }
                        selectedIds = emptySet()
                        showDeleteDialog = false
                        Toast.makeText(context, "${idsToDelete.size} ocorrência(s) excluída(s).", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir Permanentemente")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AdminOccurrenceCard(
    occWithMsgs: OccurrenceWithMessages,
    isChecked: Boolean,
    onToggleCheck: (Boolean) -> Unit,
    onDeleteSingle: () -> Unit
) {
    val occ = occWithMsgs.occurrence
    val messagesCount = occWithMsgs.messages.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleCheck(!isChecked) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggleCheck(it) }
            )

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (occ.apartment == "CONDOMÍNIO") "🏢 CONDOMÍNIO" else "Unidade: ${occ.apartment}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        color = when (occ.status) {
                            "ABERTA" -> Color(0xFFE3F2FD)
                            "EM_ESPERA" -> Color(0xFFFFF3E0)
                            "FINALIZADA" -> Color(0xFFE8F5E9)
                            "ARQUIVADA" -> Color(0xFFECEFF1)
                            else -> Color.LightGray
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = occ.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (occ.status) {
                                "ABERTA" -> Color(0xFF1976D2)
                                "EM_ESPERA" -> Color(0xFFF57C00)
                                "FINALIZADA" -> Color(0xFF388E3C)
                                "ARQUIVADA" -> Color(0xFF546E7A)
                                else -> Color.DarkGray
                            }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = occ.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (occ.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Descrição: ${occ.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val firstMsgText = occWithMsgs.messages.sortedBy { it.message.id }.firstOrNull()?.message?.text
                if (!firstMsgText.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = firstMsgText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Por: ${occ.createdByUsername} • ${occ.date}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    Text(
                        text = "💬 $messagesCount msg(s)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDeleteSingle) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir Ocorrência",
                    tint = Color.Red
                )
            }
        }
    }
}
