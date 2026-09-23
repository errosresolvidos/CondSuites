package com.example.condsuites.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.FinanceTransactionEntity
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.data.model.OccurrenceWithMessages
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.utils.CurrencyVisualTransformation
import com.example.condsuites.utils.formatCurrency
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(dao: AppDao, currentUser: UserEntity) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val transactions by dao.getAllTransactionsFlow().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    val tabs = listOf("A Pagar", "A Receber", "Pagas")
    
    val grouped = transactions.groupBy { if (it.groupId.isNotBlank()) it.groupId else it.id.toString() }

    val filteredGroups = grouped.values.filter { groupList ->
        if (groupList.isEmpty()) return@filter false
        val firstTx = groupList.first()
        val allPaid = groupList.all { it.isPaid }
        when (selectedTab) {
            0 -> firstTx.type == "PAYABLE" && !allPaid
            1 -> firstTx.type == "RECEIVABLE" && !allPaid
            2 -> allPaid
            else -> true
        }
    }.sortedBy { it.first().dueDate }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Lançamento")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredGroups) { group ->
                    TransactionGroupCard(
                        group = group,
                        dao = dao,
                        currentUser = currentUser
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            dao = dao,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun TransactionGroupCard(
    group: List<FinanceTransactionEntity>,
    dao: AppDao,
    currentUser: UserEntity
) {
    var expanded by remember { mutableStateOf(false) }
    val firstTx = group.first()
    val isGroup = group.size > 1
    val scope = rememberCoroutineScope()
    var showOccurrenceDialog by remember { mutableStateOf(false) }

    val totalAmount = group.sumOf { it.amount }
    val paidAmount = group.filter { it.isPaid }.sumOf { it.amount }
    val isAllPaid = group.all { it.isPaid }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isGroup || firstTx.relatedId != null) expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = firstTx.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (firstTx.category.isNotEmpty()) {
                        Text(
                            text = firstTx.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Text(
                    text = "R$ ${formatCurrency(totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (firstTx.type == "PAYABLE") Color(0xFFD32F2F) else Color(0xFF388E3C)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isGroup) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vencimento: ${firstTx.dueDate}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!firstTx.isPaid) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val now = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                                        val updated = firstTx.copy(isPaid = true, paidDate = now)
                                        dao.updateTransaction(updated)
                                        FirestoreSyncManager.syncTransaction(updated)
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(if (firstTx.type == "RECEIVABLE") "Receber" else "Pagar", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            Text(
                                text = if (firstTx.type == "RECEIVABLE") "Recebido em ${firstTx.paidDate ?: ""}" else "Pago em ${firstTx.paidDate ?: ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF388E3C),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (currentUser.role == "ADMIN" || currentUser.role == "Síndico") {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        dao.deleteTransaction(firstTx.id)
                                        FirestoreSyncManager.syncTransaction(firstTx, isDelete = true)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${group.count { it.isPaid }}/${group.size} parcelas pagas (R$ ${formatCurrency(paidAmount)})",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expandir"
                    )
                }
            }

            if (firstTx.relatedId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showOccurrenceDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Ocorrência Vinculada", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (expanded && isGroup) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                group.sortedBy { it.installmentNumber }.forEach { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Parcela ${tx.installmentNumber}/${tx.totalInstallments}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Venc: ${tx.dueDate} - R$ ${formatCurrency(tx.amount)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!tx.isPaid) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val now = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                                            val updated = tx.copy(isPaid = true, paidDate = now)
                                            dao.updateTransaction(updated)
                                            FirestoreSyncManager.syncTransaction(updated)
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(if (tx.type == "RECEIVABLE") "Receber" else "Pagar", style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Icon(Icons.Default.Check, contentDescription = "Pago", tint = Color(0xFF388E3C), modifier = Modifier.size(20.dp))
                            }

                            if (currentUser.role == "ADMIN" || currentUser.role == "Síndico") {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            dao.deleteTransaction(tx.id)
                                            FirestoreSyncManager.syncTransaction(tx, isDelete = true)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val relId = firstTx.relatedId
    if (showOccurrenceDialog && relId != null) {
        var occWithMsgs by remember { mutableStateOf<OccurrenceWithMessages?>(null) }

        LaunchedEffect(relId) {
            occWithMsgs = dao.getOccurrenceWithMessagesById(relId)
        }

        AlertDialog(
            onDismissRequest = { showOccurrenceDialog = false },
            title = { Text("Ocorrência Vinculada") },
            text = {
                if (occWithMsgs != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Apto / Local: ${occWithMsgs!!.occurrence.apartment}", fontWeight = FontWeight.Bold)
                        Text("Título: ${occWithMsgs!!.occurrence.title}")
                        Text("Status: ${occWithMsgs!!.occurrence.status}")
                        Text("Descrição: ${occWithMsgs!!.messages.firstOrNull()?.message?.text ?: ""}")
                    }
                } else {
                    Text("Carregando detalhes...")
                }
            },
            confirmButton = {
                TextButton(onClick = { showOccurrenceDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    dao: AppDao,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PAYABLE") } // PAYABLE or RECEIVABLE
    var amountStr by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var category by remember { mutableStateOf("Geral") }
    var installmentsStr by remember { mutableStateOf("1") }
    var isInstallment by remember { mutableStateOf(false) }
    var isPaid by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Lançamento Financeiro") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "PAYABLE",
                        onClick = { type = "PAYABLE" },
                        label = { Text("A Pagar (Despesa)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "RECEIVABLE",
                        onClick = { type = "RECEIVABLE" },
                        label = { Text("A Receber (Receita)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título / Fornecedor *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { if (it.length <= 12) amountStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Valor Total *") },
                    prefix = { Text("R$ ") },
                    visualTransformation = CurrencyVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Data de Vencimento (1ª Parcela) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPaid, onCheckedChange = { isPaid = it })
                    Text(if (type == "PAYABLE") "Marcar como Já Paga" else "Marcar como Já Recebida")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isInstallment, onCheckedChange = { isInstallment = it })
                    Text("Parcelar")
                }

                if (isInstallment) {
                    OutlinedTextField(
                        value = installmentsStr,
                        onValueChange = { installmentsStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Número de Parcelas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Observações (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = (amountStr.toDoubleOrNull() ?: 0.0) / 100
                    val totalInst = if (isInstallment) (installmentsStr.toIntOrNull() ?: 1) else 1
                    
                    if (title.isNotBlank() && amount > 0) {
                        scope.launch {
                            val groupId = UUID.randomUUID().toString()
                            val instAmount = amount / totalInst

                            for (i in 1..totalInst) {
                                val cal = Calendar.getInstance()
                                try {
                                    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dueDate)
                                    if (date != null) cal.time = date
                                } catch (_: Exception) {}
                                cal.add(Calendar.MONTH, i - 1)
                                val currentDueDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)

                                val now = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                                val newTx = FinanceTransactionEntity(
                                    title = if (totalInst > 1) "$title ($i/$totalInst)" else title,
                                    description = description,
                                    type = type,
                                    amount = instAmount,
                                    dueDate = currentDueDate,
                                    isPaid = isPaid,
                                    paidDate = if (isPaid) now else null,
                                    installmentNumber = i,
                                    totalInstallments = totalInst,
                                    groupId = groupId,
                                    category = category
                                )
                                val insertedId = dao.insertTransaction(newTx)
                                FirestoreSyncManager.syncTransaction(newTx.copy(id = insertedId))
                            }
                            onDismiss()
                        }
                    }
                },
                enabled = title.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
