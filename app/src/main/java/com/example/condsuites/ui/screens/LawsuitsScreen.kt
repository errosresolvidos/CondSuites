package com.example.condsuites.ui.screens

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
import com.example.condsuites.data.model.AgreementWithInstallments
import com.example.condsuites.data.model.LawsuitEntity
import com.example.condsuites.data.model.LawsuitProgressEntity
import com.example.condsuites.data.model.LawsuitWithProgress
import com.example.condsuites.ui.screens.reports.shareLawsuitReport
import com.example.condsuites.utils.CurrencyVisualTransformation
import com.example.condsuites.utils.ProcessNumberVisualTransformation
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.naturalSortApartments
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LawsuitsManagementScreen(lawsuits: List<LawsuitWithProgress>, agreements: List<AgreementWithInstallments>, dao: AppDao, scope: CoroutineScope) {
    var showAddDialog by remember { mutableStateOf(false) }
    var lawsuitToEdit by remember { mutableStateOf<LawsuitEntity?>(null) }
    var lawsuitForSuccess by remember { mutableStateOf<LawsuitEntity?>(null) }
    var lawsuitForDelete by remember { mutableStateOf<LawsuitEntity?>(null) }
    var lawsuitForEditWithPwd by remember { mutableStateOf<LawsuitEntity?>(null) }
    
    var statusFilter by remember { mutableStateOf("Todos") }
    var expandedFilter by remember { mutableStateOf(false) }

    val context = LocalContext.current
    
    val uniqueStatuses = remember(lawsuits) { listOf("Todos") + lawsuits.map { it.lawsuit.status }.distinct().sorted() }
    val filteredLawsuits = if (statusFilter == "Todos") lawsuits else lawsuits.filter { it.lawsuit.status == statusFilter }
    val sortedLawsuits = filteredLawsuits.sortedBy { naturalSortApartments(it.lawsuit.apartment) }
    
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expandedFilter,
                onExpandedChange = { expandedFilter = !expandedFilter },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = statusFilter,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filtrar por Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFilter) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedFilter,
                    onDismissRequest = { expandedFilter = false }
                ) {
                    uniqueStatuses.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                statusFilter = selectionOption
                                expandedFilter = false
                            }
                        )
                    }
                }
            }
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(sortedLawsuits) { item -> 
                    SwipeToDeleteContainer(item, onDelete = {
                        if (item.lawsuit.isFinished) lawsuitForDelete = item.lawsuit
                        else {
                            scope.launch {
                                dao.deleteLawsuit(item.lawsuit.id)
                                agreements.find { it.agreement.apartment == item.lawsuit.apartment }?.let {
                                    dao.updateAgreement(it.agreement.copy(isLawsuit = false))
                                }
                            }
                        }
                    }) {
                        LawsuitCard(
                            apartment = item.lawsuit.apartment,
                            ownerName = item.lawsuit.ownerName,
                            processNumber = item.lawsuit.processNumber,
                            forum = item.lawsuit.forum,
                            totalDebt = item.lawsuit.totalDebt,
                            registrationDate = item.lawsuit.registrationDate,
                            status = item.lawsuit.status,
                            successValue = item.lawsuit.successValue,
                            isFinished = item.lawsuit.isFinished,
                            progressUpdates = item.progress,
                            onAddProgress = { date, desc ->
                                scope.launch { dao.insertLawsuitProgress(LawsuitProgressEntity(lawsuitId = item.lawsuit.id, date = date, description = desc)) }
                            },
                            onDeleteProgress = { progId ->
                                scope.launch { dao.deleteLawsuitProgress(progId) }
                            },
                            onEdit = {
                                if (item.lawsuit.isFinished) lawsuitForEditWithPwd = item.lawsuit
                                else lawsuitToEdit = item.lawsuit
                            },
                            onShare = { shareLawsuitReport(context, item.lawsuit) },
                            onSuccess = { lawsuitForSuccess = item.lawsuit }
                        )
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showAddDialog = true }, Modifier.align(Alignment.BottomEnd).padding(24.dp)) { Icon(Icons.Filled.Add, null) }
        
        if (showAddDialog || lawsuitToEdit != null) RegisterLawsuitDialog(lawsuitToEdit, dao, { showAddDialog = false; lawsuitToEdit = null }, { lawsuit -> scope.launch { dao.insertLawsuit(lawsuit) }; showAddDialog = false; lawsuitToEdit = null })
        
        if (lawsuitForSuccess != null) LawsuitSuccessDialog(lawsuitForSuccess!!, { lawsuitForSuccess = null }, { 
            val updated = it.copy(isFinished = true)
            scope.launch { 
                dao.updateLawsuit(updated)
                dao.insertHistory(com.example.condsuites.data.model.DelinquencyHistoryEntity(
                    apartment = updated.apartment,
                    ownerName = updated.ownerName,
                    eventType = "Processo",
                    description = "Processo",
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                    value = updated.successValue ?: 0.0
                ))
            }
            lawsuitForSuccess = null 
        })
        if (lawsuitForDelete != null) {
            val lawsuitToCapture = lawsuitForDelete!!
            PasswordUndoDialog({ lawsuitForDelete = null }, { 
                scope.launch { 
                    dao.deleteLawsuit(lawsuitToCapture.id)
                    agreements.find { it.agreement.apartment == lawsuitToCapture.apartment }?.let {
                        dao.updateAgreement(it.agreement.copy(isLawsuit = false))
                    }
                }
                lawsuitForDelete = null 
            })
        }
        if (lawsuitForEditWithPwd != null) PasswordUndoDialog({ lawsuitForEditWithPwd = null }, { 
            lawsuitToEdit = lawsuitForEditWithPwd
            lawsuitForEditWithPwd = null 
        })
    }
}

@Composable
fun LawsuitCard(
    apartment: String, 
    ownerName: String, 
    processNumber: String, 
    forum: String, 
    totalDebt: Double, 
    registrationDate: String, 
    status: String,
    successValue: Double?,
    isFinished: Boolean,
    progressUpdates: List<LawsuitProgressEntity> = emptyList(),
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onSuccess: () -> Unit,
    onAddProgress: (String, String) -> Unit,
    onDeleteProgress: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddProgressDialog by remember { mutableStateOf(false) }

    val isCondo = apartment == "CONDOMÍNIO"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCondo -> Color(0xFFE8F4FD)
                else -> Color(0xFFF7F7F7)
            }
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        ownerName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = if (isCondo) Color(0xFFBBDEFB) else Color(0xFFFFCDD2).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (isCondo) "🏢 CONDOMÍNIO" else "🚪 UNIDADE: $apartment",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCondo) Color(0xFF0D47A1) else Color(0xFFB71C1C)
                        )
                    }
                }
                
                Surface(
                    color = if (isFinished) Color(0xFFC8E6C9).copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) { 
                    Text(
                        text = if (isFinished) "FINALIZADO" else status.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isFinished) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                } 
            }
            
            Spacer(Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(12.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, null, Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Processo: $processNumber", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, null, Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Fórum: $forum", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Ajuizado em: $registrationDate", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Dívida Ajuizada: R$ ${formatCurrency(totalDebt)}", color = Color.Red, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            
            if (successValue != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("VALOR RECUPERADO", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text("R$ ${formatCurrency(successValue)}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            if (expanded) {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Andamentos do Processo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { showAddProgressDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Novo Andamento")
                    }
                }
                
                if (progressUpdates.isEmpty()) {
                    Text("Nenhum andamento registrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    progressUpdates.sortedByDescending { it.id }.forEach { progress ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(progress.date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text(progress.description, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { onDeleteProgress(progress.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        if (progress != progressUpdates.last()) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.End) {
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(36.dp)) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Andamentos", tint = MaterialTheme.colorScheme.primary) }
                if (!isFinished) {
                    IconButton(onSuccess, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Stars, "Êxito", tint = Color(0xFFFFC107), modifier = Modifier.size(24.dp)) }
                }
                IconButton(onShare, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
            }
        }
    }

    if (showAddProgressDialog) {
        AddLawsuitProgressDialog(
            onDismiss = { showAddProgressDialog = false },
            onConfirm = { date, desc ->
                onAddProgress(date, desc)
                showAddProgressDialog = false
            }
        )
    }
}

@Composable
fun AddLawsuitProgressDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Andamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && date.isNotBlank()) {
                        onConfirm(date, description)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun AddAgreementProgressDialog(apartment: String, ownerName: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Andamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Unidade: $apartment", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Proprietário: $ownerName", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && date.isNotBlank()) {
                        onConfirm(date, description)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun EditProgressDialog(initialDate: String, initialDesc: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var description by remember { mutableStateOf(initialDesc) }
    var date by remember { mutableStateOf(initialDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Andamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && date.isNotBlank()) {
                        onConfirm(date, description)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun LawsuitSuccessDialog(lawsuit: LawsuitEntity, onDismiss: () -> Unit, onConfirm: (LawsuitEntity) -> Unit) {
    var valDigits by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, 
        title = { Text("Declarar Êxito") }, 
        text = {
            Column {
                Text("Informe o valor obtido no processo:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = valDigits, 
                    onValueChange = { if (it.length <= 12) valDigits = it.filter { char -> char.isDigit() } }, 
                    label = { Text("Valor do Êxito") }, 
                    prefix = { Text("R$ ") }, 
                    visualTransformation = CurrencyVisualTransformation(), 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { 
            Button(onClick = { 
                val value = (valDigits.toDoubleOrNull() ?: 0.0) / 100
                onConfirm(lawsuit.copy(successValue = value)) 
            }) { Text("Confirmar") } 
        }, 
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterLawsuitDialog(editingLawsuit: LawsuitEntity? = null, dao: AppDao, onDismiss: () -> Unit, onConfirm: (LawsuitEntity) -> Unit) {
    var apto by remember { mutableStateOf(editingLawsuit?.apartment ?: "") }; var nome by remember { mutableStateOf(editingLawsuit?.ownerName ?: "") }
    var procNumDigits by remember { mutableStateOf(editingLawsuit?.processNumber?.replace(Regex("\\D"), "") ?: "") }
    var forum by remember { mutableStateOf(editingLawsuit?.forum ?: "") }
    var divDigits by remember { mutableStateOf(editingLawsuit?.totalDebt?.let { (it * 100).toLong().toString() } ?: "") }
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); var date by remember { mutableStateOf(editingLawsuit?.registrationDate ?: sdf.format(Date())) }
    
    val units by dao.getAllUnits().collectAsState(initial = emptyList())

    LaunchedEffect(apto) {
        if (apto.isNotBlank() && editingLawsuit == null) {
            val matched = units.find { it.apartment.trim().equals(apto.trim(), ignoreCase = true) }
            if (matched != null && matched.ownerName.isNotBlank()) {
                nome = matched.ownerName
            }
        }
    }
    
    val statusOptionsDb by dao.getProcessStatuses().collectAsState(initial = emptyList())
    val statusOptions = remember(statusOptionsDb) {
        val list = statusOptionsDb.map { it.status }.toMutableStateList()
        if (list.isEmpty()) {
            list.addAll(listOf("Enviado para ajuizar", "Em andamento", "Exito", "Perda"))
        }
        list
    }
    editingLawsuit?.status?.let { if (it.isNotBlank() && !statusOptions.contains(it)) statusOptions.add(it) }
    
    var status by remember { mutableStateOf(editingLawsuit?.status ?: if (statusOptions.isNotEmpty()) statusOptions[1] else "") }
    var showPicker by remember { mutableStateOf(false) }
    var showAddCustomStatus by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (editingLawsuit != null) "Editar Processo" else "Cadastrar Processo") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(apto, { apto = it }, label = { Text("Apto") }); OutlinedTextField(nome, { nome = it }, label = { Text("Proprietário") })
                OutlinedTextField(
                    value = procNumDigits,
                    onValueChange = { it -> if (it.length <= 20) procNumDigits = it.filter { it.isDigit() } },
                    label = { Text("Nº do Processo") },
                    placeholder = { Text("0000000-00.0000.0.00.0000") },
                    visualTransformation = ProcessNumberVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                OutlinedTextField(forum, { forum = it }, label = { Text("Fórum / Vara") })
                OutlinedTextField(divDigits, { it -> if (it.length <= 12) divDigits = it.filter { it.isDigit() } }, label = { Text("Dívida Ajuizada") }, prefix = { Text("R$ ") }, visualTransformation = CurrencyVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
                OutlinedTextField(date, {}, label = { Text("Data do Ajuizamento") }, readOnly = true, trailingIcon = { IconButton({ showPicker = true }) { Icon(Icons.Default.CalendarToday, null) } }, modifier = Modifier.clickable { showPicker = true })
                
                var expandedStatus by remember { mutableStateOf(false) }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = !expandedStatus },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status do Processo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            statusOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        status = selectionOption
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { showAddCustomStatus = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, "Adicionar Status", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = { 
            Button(onClick = { 
                val total = (divDigits.toDoubleOrNull() ?: 0.0) / 100
                val formattedProc = StringBuilder()
                for (i in procNumDigits.indices) {
                    formattedProc.append(procNumDigits[i])
                    when (i) {
                        6 -> formattedProc.append("-")
                        8, 12, 13, 15 -> formattedProc.append(".")
                    }
                }
                onConfirm(LawsuitEntity(editingLawsuit?.id ?: System.currentTimeMillis(), apto, nome, formattedProc.toString(), forum, total, date, status)) 
            }) { Text("Salvar") } 
        }, dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )

    if (showAddCustomStatus) {
        var newStatus by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCustomStatus = false },
            title = { Text("Cadastrar Status") },
            text = {
                OutlinedTextField(
                    value = newStatus,
                    onValueChange = { newStatus = it },
                    label = { Text("Novo Status") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newStatus.isNotBlank()) {
                        statusOptions.add(newStatus)
                        status = newStatus
                    }
                    showAddCustomStatus = false
                }) { Text("Adicionar") }
            },
            dismissButton = { TextButton({ showAddCustomStatus = false }) { Text("Cancelar") } }
        )
    }

    if (showPicker) { 
        val state = rememberDatePickerState(initialSelectedDateMillis = try { 
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(date)?.time 
        } catch(_: Exception) { null })
        DatePickerDialog({ showPicker = false }, { 
            TextButton({ 
                state.selectedDateMillis?.let { 
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(it)) 
                }
                showPicker = false 
            }) { Text("OK") } 
        }) { DatePicker(state) } 
    }
}
