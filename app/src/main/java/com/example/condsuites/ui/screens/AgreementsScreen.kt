package com.example.condsuites.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.*
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.ui.screens.reports.shareAgreementReport
import com.example.condsuites.utils.CurrencyVisualTransformation
import com.example.condsuites.utils.calculateDaysDelay
import com.example.condsuites.utils.calculateMonthlyDates
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.naturalSortApartments
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgreementsManagementScreen(
    agreements: List<AgreementWithInstallments>, 
    delinquents: List<DelinquentWithProgress>, 
    lawsuits: List<LawsuitWithProgress>, 
    dao: AppDao, 
    scope: CoroutineScope,
    currentUser: UserEntity,
    isCompact: Boolean = true
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Notificações", "Acordos", "Ajuizados")
    
    var showAddAgreement by remember { mutableStateOf(false) }
    var showAddDelinquent by remember { mutableStateOf(false) }
    var showAddLawsuit by remember { mutableStateOf(false) }
    
    var agreementToEdit by remember { mutableStateOf<AgreementWithInstallments?>(null) }
    var delinquentToEdit by remember { mutableStateOf<DelinquentEntity?>(null) }
    var lawsuitToEdit by remember { mutableStateOf<LawsuitEntity?>(null) }
    
    var agreementToRenew by remember { mutableStateOf<AgreementWithInstallments?>(null) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var showHistoryForApt by remember { mutableStateOf<String?>(null) }
    var reportChoiceFor by remember { mutableStateOf<AgreementWithInstallments?>(null) }
    var showNotificationDetails by remember { mutableStateOf<AgreementWithInstallments?>(null) }
    var lawsuitForSuccess by remember { mutableStateOf<LawsuitEntity?>(null) }
    var lawsuitForDelete by remember { mutableStateOf<LawsuitEntity?>(null) }
    var lawsuitForEditWithPwd by remember { mutableStateOf<LawsuitEntity?>(null) }
    
    var statusFilter by remember { mutableStateOf("Todos") }
    var expandedFilter by remember { mutableStateOf(false) }
    
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val selected = agreements.find { it.agreement.id == selectedId }
    val context = LocalContext.current

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedIds = emptySet() }) {
                                Icon(Icons.Default.Close, null)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("${selectedIds.size} selecionados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                }
            }

            SecondaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }) }
            }
            
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTab) {
                    0 -> {
                        val sortedDelinquents = delinquents.sortedBy { naturalSortApartments(it.delinquent.apartment) }
                        LazyColumn(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(sortedDelinquents) { item -> 
                                val isSelected = selectedIds.contains(item.delinquent.id)
                                DelinquentCard(
                                    item = item,
                                    isSelected = isSelected,
                                    modifier = Modifier.combinedClickable(
                                        onClick = {
                                            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                                                selectedIds = if (isSelected) selectedIds - item.delinquent.id else selectedIds + item.delinquent.id
                                            } else {
                                                delinquentToEdit = item.delinquent
                                            }
                                        },
                                        onLongClick = {
                                            if (currentUser.role == "ADMIN") {
                                                selectedIds = selectedIds + item.delinquent.id
                                            }
                                        }
                                    ),
                                    onEdit = { 
                                        if (selectedIds.isEmpty()) delinquentToEdit = item.delinquent 
                                    },
                                    onArchive = {
                                        scope.launch {
                                            val updated = item.delinquent.copy(isArchived = true)
                                            dao.updateDelinquent(updated)
                                            FirestoreSyncManager.syncDelinquent(updated)
                                        }
                                    },
                                    onAddProgress = { date: String, desc: String ->
                                        scope.launch { dao.insertDelinquentProgress(DelinquentProgressEntity(delinquentId = item.delinquent.id, apartment = item.delinquent.apartment, date = date, description = desc)) }
                                    },
                                    onUpdateProgress = { progId: Long, date: String, desc: String ->
                                        scope.launch { dao.updateDelinquentProgress(DelinquentProgressEntity(id = progId, delinquentId = item.delinquent.id, apartment = item.delinquent.apartment, date = date, description = desc)) }
                                    },
                                    onDeleteProgress = { progId: Long ->
                                        scope.launch { dao.deleteDelinquentProgress(progId) }
                                    }
                                )
                            }
                        }
                    }
                    1 -> {
                        val sortedAgreements = agreements.sortedBy { naturalSortApartments(it.agreement.apartment) }
                        LazyColumn(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(sortedAgreements) { item -> 
                                val isSelected = selectedIds.contains(item.agreement.id)
                                AgreementCard(
                                    agreement = item,
                                    isCompact = isCompact,
                                    isSelected = isSelected,
                                    modifier = Modifier.combinedClickable(
                                        onClick = {
                                            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                                                selectedIds = if (isSelected) selectedIds - item.agreement.id else selectedIds + item.agreement.id
                                            } else {
                                                agreementToEdit = item
                                            }
                                        },
                                        onLongClick = {
                                            if (currentUser.role == "ADMIN") {
                                                selectedIds = selectedIds + item.agreement.id
                                            }
                                        }
                                    ),
                                    onRenew = { agreementToRenew = item },
                                    onViewHistory = { showHistoryForApt = item.agreement.apartment },
                                    onShare = { reportChoiceFor = item },
                                    onShowNotifs = { showNotificationDetails = item },
                                    onAjuizar = {
                                        lawsuitToEdit = LawsuitEntity(apartment = item.agreement.apartment, ownerName = item.agreement.ownerName, totalDebt = item.agreement.totalDebt, registrationDate = item.agreement.date, processNumber = "", forum = "")
                                        selectedTab = 2
                                    },
                                    onArchive = {
                                        scope.launch {
                                            val updated = item.agreement.copy(isArchived = true)
                                            dao.updateAgreement(updated)
                                            FirestoreSyncManager.syncAgreement(updated)
                                        }
                                    },
                                    onDetails = { selectedId = item.agreement.id },
                                    onAddProgress = { date: String, desc: String ->
                                        scope.launch {
                                            dao.insertAgreementProgress(AgreementProgressEntity(agreementId = item.agreement.id, apartment = item.agreement.apartment, date = date, description = desc))

                                            val delinquent = dao.getDelinquentByApartment(item.agreement.apartment)
                                            if (delinquent != null) {
                                                dao.insertDelinquentProgress(DelinquentProgressEntity(delinquentId = delinquent.id, apartment = delinquent.apartment, date = date, description = desc))
                                                dao.updateDelinquent(delinquent.copy(
                                                    ownerName = item.agreement.ownerName,
                                                    totalDebt = item.agreement.totalDebt,
                                                    hasMadeAgreement = true
                                                ))
                                            }
                                        }
                                    },
                                    onUpdateProgress = { progId: Long, date: String, desc: String ->
                                        scope.launch { dao.updateAgreementProgress(AgreementProgressEntity(id = progId, agreementId = item.agreement.id, apartment = item.agreement.apartment, date = date, description = desc)) }
                                    },
                                    onDeleteProgress = { progId: Long ->
                                        scope.launch { dao.deleteAgreementProgress(progId) }
                                    }
                                )
                            }
                            if (sortedAgreements.isNotEmpty()) item { SummarySection(sortedAgreements) }
                        }
                    }
                    2 -> {
                        val uniqueStatuses = remember(lawsuits) { listOf("Todos") + lawsuits.map { it.lawsuit.status }.distinct().sorted() }
                        
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

                        val filteredLawsuits = if (statusFilter == "Todos") lawsuits 
                                               else lawsuits.filter { it.lawsuit.status == statusFilter }
                        
                        val sortedLawsuits = filteredLawsuits.sortedBy { naturalSortApartments(it.lawsuit.apartment) }
                        LazyColumn(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(sortedLawsuits) { item -> 
                                val isSelected = selectedIds.contains(item.lawsuit.id)
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
                                    isSelected = isSelected,
                                    modifier = Modifier.combinedClickable(
                                        onClick = {
                                            if (selectedIds.isNotEmpty() && currentUser.role == "ADMIN") {
                                                selectedIds = if (isSelected) selectedIds - item.lawsuit.id else selectedIds + item.lawsuit.id
                                            } else {
                                                if (item.lawsuit.isFinished) lawsuitForEditWithPwd = item.lawsuit
                                                else lawsuitToEdit = item.lawsuit
                                            }
                                        },
                                        onLongClick = {
                                            if (currentUser.role == "ADMIN") {
                                                selectedIds = selectedIds + item.lawsuit.id
                                            }
                                        }
                                    ),
                                    onEdit = {
                                        if (selectedIds.isEmpty()) {
                                            if (item.lawsuit.isFinished) lawsuitForEditWithPwd = item.lawsuit
                                            else lawsuitToEdit = item.lawsuit
                                        }
                                    },
                                    onShare = { com.example.condsuites.ui.screens.reports.shareLawsuitReport(context, item.lawsuit) },
                                    onSuccess = { lawsuitForSuccess = item.lawsuit },
                                    onAddProgress = { date: String, desc: String ->
                                        scope.launch { dao.insertLawsuitProgress(LawsuitProgressEntity(lawsuitId = item.lawsuit.id, apartment = item.lawsuit.apartment, date = date, description = desc)) }
                                    },
                                    onDeleteProgress = { progId: Long ->
                                        scope.launch { dao.deleteLawsuitProgress(progId) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        FloatingActionButton(onClick = { 
            when (selectedTab) {
                0 -> showAddDelinquent = true
                1 -> showAddAgreement = true
                2 -> showAddLawsuit = true
            }
        }, Modifier.align(Alignment.BottomEnd).padding(24.dp)) { Icon(Icons.Filled.Add, null) }
        
        if (showAddAgreement || agreementToEdit != null) RegisterAgreementDialog(
            editingAgreement = agreementToEdit, 
            dao = dao,
            onDismiss = { showAddAgreement = false; agreementToEdit = null }, 
            onConfirm = { agg, insts -> 
                val isNewAgreement = agreements.none { it.agreement.id == agg.id }
                scope.launch { 
                    dao.insertAgreement(agg)
                    dao.insertInstallments(insts)
                    FirestoreSyncManager.syncAgreement(agg)
                    
                    val delinquent = dao.getDelinquentByApartment(agg.apartment)
                    if (delinquent != null) {
                        dao.updateDelinquent(delinquent.copy(
                            ownerName = agg.ownerName,
                            totalDebt = agg.totalDebt,
                            hasMadeAgreement = true
                        ))
                    }

                    if (isNewAgreement) {
                        dao.insertHistory(DelinquencyHistoryEntity(
                            apartment = agg.apartment,
                            ownerName = agg.ownerName,
                            eventType = "Acordo",
                            description = "Acordo",
                            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                            value = agg.totalDebt
                        ))
                    }
                }
                showAddAgreement = false; agreementToEdit = null 
            },
            onAjuizar = { agg ->
                lawsuitToEdit = LawsuitEntity(apartment = agg.apartment, ownerName = agg.ownerName, totalDebt = agg.totalDebt, registrationDate = agg.date, processNumber = "", forum = "")
                selectedTab = 2
                showAddAgreement = false; agreementToEdit = null
            }
        )
        if (showAddDelinquent || delinquentToEdit != null) RegisterDelinquentDialog(editingDelinquent = delinquentToEdit, dao = dao, onDismiss = { showAddDelinquent = false; delinquentToEdit = null }, onConfirm = { del -> 
            val isNew = delinquentToEdit == null
            
            scope.launch { 
                if (isNew) {
                    dao.insertDelinquent(del)
                    dao.insertHistory(DelinquencyHistoryEntity(
                        apartment = del.apartment,
                        ownerName = del.ownerName,
                        eventType = "Notificação",
                        description = "Notificação",
                        date = del.registrationDate,
                        value = del.totalDebt
                    ))
                } else {
                    dao.updateDelinquent(del)
                }
                FirestoreSyncManager.syncDelinquent(del)
            }
            if (del.hasMadeAgreement) {
                val prefill = AgreementEntity(System.currentTimeMillis(), del.apartment, del.ownerName, del.totalDebt, SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()), 1, del.totalDebt, false, null, false, del.notification1Date, del.notification2Date, del.notification3Date)
                agreementToEdit = AgreementWithInstallments(prefill, emptyList())
                selectedTab = 1
            }
            showAddDelinquent = false; delinquentToEdit = null 
        })
        if (showAddLawsuit || lawsuitToEdit != null) RegisterLawsuitDialog(lawsuitToEdit, dao, { showAddLawsuit = false; lawsuitToEdit = null }, { lawsuit: LawsuitEntity -> 
            val isNew = lawsuitToEdit == null || lawsuitToEdit!!.processNumber.isEmpty()
            scope.launch { 
                dao.insertLawsuit(lawsuit)
                FirestoreSyncManager.syncLawsuit(lawsuit)
                if (isNew) {
                    dao.insertHistory(DelinquencyHistoryEntity(
                        apartment = lawsuit.apartment,
                        ownerName = lawsuit.ownerName,
                        eventType = "Processo",
                        description = "Processo",
                        date = lawsuit.registrationDate,
                        value = lawsuit.totalDebt
                    ))
                }
                agreements.find { it.agreement.apartment == lawsuit.apartment }?.let {
                    dao.updateAgreement(it.agreement.copy(isLawsuit = true))
                }
            }
            showAddLawsuit = false; lawsuitToEdit = null 
        })
        
        if (agreementToRenew != null) {
            val agreementToCapture = agreementToRenew!!
            RegisterAgreementDialog(title = "Renovar Acordo", editingAgreement = agreementToCapture, isRenewal = true, dao = dao, onDismiss = { agreementToRenew = null }, onConfirm = { agg, insts -> 
                scope.launch { 
                    dao.updateAgreement(agreementToCapture.agreement.copy(isArchived = true))
                    dao.insertAgreement(agg.copy(originalAgreementId = agreementToCapture.agreement.id))
                    dao.insertInstallments(insts)
                    
                    val delinquent = dao.getDelinquentByApartment(agg.apartment)
                    if (delinquent != null) {
                        dao.updateDelinquent(delinquent.copy(
                            ownerName = agg.ownerName,
                            totalDebt = agg.totalDebt,
                            hasMadeAgreement = true
                        ))
                    }
    
                    dao.insertHistory(DelinquencyHistoryEntity(
                        apartment = agg.apartment,
                        ownerName = agg.ownerName,
                        eventType = "Acordo",
                        description = "Acordo",
                        date = agg.date,
                        value = agg.totalDebt
                    ))
                }
                agreementToRenew = null 
            })
        }
        if (selected != null) InstallmentsDetailsDialog(selected, { selectedId = null }, { inst -> scope.launch { dao.updateInstallment(inst) } })
        if (showHistoryForApt != null) { val history by dao.getAgreementHistory(showHistoryForApt!!).collectAsState(initial = emptyList()); HistoryDialog(showHistoryForApt!!, history, onDismiss = { showHistoryForApt = null }) }
        if (reportChoiceFor != null) { ReportTypeDialog({ reportChoiceFor = null }, { det -> shareAgreementReport(context, reportChoiceFor!!, det); reportChoiceFor = null }) }
        if (showNotificationDetails != null) NotificationHistoryDialog(showNotificationDetails!!, onDismiss = { showNotificationDetails = null })
        if (lawsuitForSuccess != null) LawsuitSuccessDialog(lawsuitForSuccess!!, { lawsuitForSuccess = null }, { lawsuitUpdated: LawsuitEntity -> 
            val updated = lawsuitUpdated.copy(isFinished = true)
            scope.launch { 
                dao.updateLawsuit(updated)
                dao.insertHistory(DelinquencyHistoryEntity(
                    apartment = updated.apartment,
                    ownerName = updated.ownerName,
                    eventType = "Processo",
                    description = "Processo finalizado com êxito. Valor recuperado: R$ ${formatCurrency(updated.successValue ?: 0.0)}",
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
                    FirestoreSyncManager.syncLawsuit(lawsuitToCapture, isDelete = true)
                    agreements.find { it.agreement.apartment == lawsuitToCapture.apartment }?.let {
                        val updated = it.agreement.copy(isLawsuit = false)
                        dao.updateAgreement(updated)
                        FirestoreSyncManager.syncAgreement(updated)
                    }
                }
                lawsuitForDelete = null 
            })
        }
        if (lawsuitForEditWithPwd != null) PasswordUndoDialog({ lawsuitForEditWithPwd = null }, { 
            lawsuitToEdit = lawsuitForEditWithPwd
            lawsuitForEditWithPwd = null 
        })

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Deseja excluir os ${selectedIds.size} itens selecionados?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                selectedIds.forEach { id ->
                                    when (selectedTab) {
                                        0 -> {
                                            val del = delinquents.find { it.delinquent.id == id }?.delinquent
                                            dao.deleteDelinquent(id)
                                            if (del != null) FirestoreSyncManager.syncDelinquent(del, isDelete = true)
                                        }
                                        1 -> {
                                            val agg = agreements.find { it.agreement.id == id }?.agreement
                                            dao.deleteAgreement(id)
                                            if (agg != null) FirestoreSyncManager.syncAgreement(agg, isDelete = true)
                                        }
                                        2 -> {
                                            val lawsuit = lawsuits.find { it.lawsuit.id == id }?.lawsuit
                                            dao.deleteLawsuit(id)
                                            if (lawsuit != null) {
                                                FirestoreSyncManager.syncLawsuit(lawsuit, isDelete = true)
                                                agreements.find { it.agreement.apartment == lawsuit.apartment }?.let { found ->
                                                    val updated = found.agreement.copy(isLawsuit = false)
                                                    dao.updateAgreement(updated)
                                                    FirestoreSyncManager.syncAgreement(updated)
                                                }
                                            }
                                        }
                                    }
                                }
                                selectedIds = emptySet()
                                showDeleteConfirm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
fun NotificationHistoryDialog(agreement: AgreementWithInstallments, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Notificações Prévias") },
        text = {
            Column {
                Text("Unidade: ${agreement.agreement.apartment}")
                Spacer(Modifier.height(8.dp))
                NotificationLine("1ª Notificação", agreement.agreement.n1Date)
                NotificationLine("2ª Notificação", agreement.agreement.n2Date)
                NotificationLine("3ª Notificação", agreement.agreement.n3Date)
                if (agreement.agreement.n1Date == null && agreement.agreement.n2Date == null && agreement.agreement.n3Date == null) {
                    Text("Nenhum histórico de notificação encontrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        },
        confirmButton = { TextButton(onDismiss) { Text("Fechar") } }
    )
}

@Composable
fun SummarySection(active: List<AgreementWithInstallments>) {
    val totalAgreed = active.sumOf { it.agreement.totalDebt }; val totalPending = active.flatMap { it.installments }.filter { !it.isPaid }.sumOf { it.value }
    Card(Modifier.fillMaxWidth().padding(vertical = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text("Resumo Financeiro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Total Acordado:"); Text("R$ ${formatCurrency(totalAgreed)}", fontWeight = FontWeight.Bold) }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Pendente:"); Text("R$ ${formatCurrency(totalPending)}", fontWeight = FontWeight.Bold, color = Color(0xFFE91E63)) }
        }
    }
}

@Composable
fun AgreementCard(
    agreement: AgreementWithInstallments, 
    isCompact: Boolean = true,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onRenew: () -> Unit, 
    onViewHistory: () -> Unit, 
    onShare: () -> Unit, 
    onShowNotifs: () -> Unit, 
    onAjuizar: () -> Unit, 
    onArchive: () -> Unit, 
    onDetails: () -> Unit = {},
    onAddProgress: (String, String) -> Unit,
    onUpdateProgress: (Long, String, String) -> Unit,
    onDeleteProgress: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(!isCompact) }
    
    LaunchedEffect(isCompact) {
        expanded = !isCompact
    }
    var showAddProgressDialog by remember { mutableStateOf(false) }
    var progressToEdit by remember { mutableStateOf<AgreementProgressEntity?>(null) }
    val firstUnpaid = agreement.installments.sortedBy { it.number }.firstOrNull { !it.isPaid }
    val delay = firstUnpaid?.let { calculateDaysDelay(it.dueDate) } ?: 0
    val paidCount = agreement.installments.count { it.isPaid }
    val totalCount = agreement.agreement.installmentsCount
    val remaining = agreement.installments.filter { !it.isPaid }.sumOf { it.value }
    val progress = if (totalCount > 0) paidCount.toFloat() / totalCount else 0f

    val isCondo = agreement.agreement.apartment == "CONDOMÍNIO"

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
                        agreement.agreement.ownerName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = if (isCondo) Color(0xFFBBDEFB) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (isCondo) "🏢 CONDOMÍNIO" else "🚪 UNIDADE: ${agreement.agreement.apartment}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCondo) Color(0xFF0D47A1) else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (agreement.agreement.isLawsuit) {
                        Spacer(Modifier.height(4.dp))
                        Surface(color = Color.Red.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Gavel, null, Modifier.size(12.dp), tint = Color.Red)
                                Spacer(Modifier.width(4.dp))
                                Text("AJUIZADO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Red)
                            }
                        }
                    }
                }
                
                Surface(
                    color = when {
                        delay > 0 -> Color(0xFFFFCDD2).copy(alpha = 0.5f)
                        else -> Color(0xFFC8E6C9).copy(alpha = 0.5f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (delay <= 0) "EM DIA" else "ATRASADO ($delay d)",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (delay <= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Acordo firmado em ${agreement.agreement.date}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column {
                    Text("Dívida Total", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("R$ ${formatCurrency(agreement.agreement.totalDebt)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Red)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Restante", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("R$ ${formatCurrency(remaining)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(16.dp))

            Column {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Pagamento: $paidCount/$totalCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (progress >= 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }

            if (expanded) {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Andamentos do Acordo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { showAddProgressDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Novo Andamento")
                    }
                }
                
                if (agreement.progress.isEmpty()) {
                    Text("Nenhum andamento registrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    agreement.progress.sortedByDescending { it.id }.forEach { progressItem ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(progressItem.date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text(progressItem.description, style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    IconButton(onClick = { progressToEdit = progressItem }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Edit, "Editar Andamento", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(onClick = { onDeleteProgress(progressItem.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        if (progressItem != agreement.progress.last()) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Row {
                    IconButton({ onShare() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                    IconButton({ onViewHistory() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                    IconButton({ onShowNotifs() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(36.dp)) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Andamentos", tint = MaterialTheme.colorScheme.primary) }
                    IconButton({ onDetails() }, modifier = Modifier.size(36.dp)) { Icon(Icons.AutoMirrored.Filled.List, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                    if (!agreement.agreement.isLawsuit) {
                        IconButton({ onAjuizar() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Gavel, null, tint = Color.Red, modifier = Modifier.size(20.dp)) }
                    }
                    IconButton({ onRenew() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Autorenew, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = { onArchive() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Archive, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                }
            }
        }
    }

    if (showAddProgressDialog) {
        AddAgreementProgressDialog(
            apartment = agreement.agreement.apartment,
            ownerName = agreement.agreement.ownerName,
            onDismiss = { showAddProgressDialog = false },
            onConfirm = { date, desc ->
                onAddProgress(date, desc)
                showAddProgressDialog = false
            }
        )
    }
}

@Composable
fun DelinquentCard(
    item: DelinquentWithProgress, 
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onAddProgress: (String, String) -> Unit,
    onUpdateProgress: (Long, String, String) -> Unit,
    onDeleteProgress: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddProgressDialog by remember { mutableStateOf(false) }
    var progressToEdit by remember { mutableStateOf<DelinquentProgressEntity?>(null) }

    val isCondo = item.delinquent.apartment == "CONDOMÍNIO"

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
                        item.delinquent.ownerName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = if (isCondo) Color(0xFFBBDEFB) else Color(0xFFE1BEE7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (isCondo) "🏢 CONDOMÍNIO" else "🚪 UNIDADE: ${item.delinquent.apartment}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCondo) Color(0xFF0D47A1) else Color(0xFF4A148C)
                        )
                    }
                }
                
                if (item.delinquent.hasMadeAgreement) {
                    Surface(
                        color = Color(0xFFC8E6C9).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "ACORDO FEITO",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            val regDateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.delinquent.id))
            Text("Inscrito em $regDateFormatted", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Ano da Dívida: ${item.delinquent.registrationDate}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            
            Spacer(Modifier.height(8.dp))
            Surface(
                color = Color.Red.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.1f))
            ) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Dívida Atual", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                        Text("R$ ${formatCurrency(item.delinquent.totalDebt)}", color = Color.Red, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))

            Text("Fluxo de Notificações", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), Arrangement.spacedBy(8.dp)) {
                DelinquentNotificationBadge("1ª Notificação", item.delinquent.notification1Date != null)
                DelinquentNotificationBadge("2ª Notificação", item.delinquent.notification2Date != null)
                DelinquentNotificationBadge("3ª Notificação", item.delinquent.notification3Date != null)
            }

            if (expanded) {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Andamentos da Notificação", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { showAddProgressDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Novo Andamento")
                    }
                }
                
                if (item.progress.isEmpty()) {
                    Text("Nenhum andamento registrado.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    item.progress.sortedByDescending { it.id }.forEach { progressItem ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(progressItem.date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text(progressItem.description, style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    IconButton(onClick = { progressToEdit = progressItem }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Edit, "Editar Andamento", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(onClick = { onDeleteProgress(progressItem.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        if (progressItem != item.progress.last()) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.End) {
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(36.dp)) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Andamentos", tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onArchive, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Archive, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
            }
        }
    }

    if (showAddProgressDialog) {
        AddAgreementProgressDialog(
            apartment = item.delinquent.apartment,
            ownerName = item.delinquent.ownerName,
            onDismiss = { showAddProgressDialog = false },
            onConfirm = { date, desc ->
                onAddProgress(date, desc)
                showAddProgressDialog = false
            }
        )
    }

    if (progressToEdit != null) {
        EditProgressDialog(
            initialDate = progressToEdit!!.date,
            initialDesc = progressToEdit!!.description,
            onDismiss = { progressToEdit = null },
            onConfirm = { date, desc ->
                onUpdateProgress(progressToEdit!!.id, date, desc)
                progressToEdit = null
            }
        )
    }
}

@Composable
fun DelinquentNotificationBadge(label: String, active: Boolean) {
    Surface(
        color = if (active) Color(0xFF4CAF50).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (active) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                null,
                Modifier.size(14.dp),
                tint = if (active) Color(0xFF2E7D32) else Color.Gray
            )
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (active) Color(0xFF2E7D32) else Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ArchivedNotificationsDialog(archived: List<DelinquentWithProgress>, onDelete: (DelinquentEntity) -> Unit, onUnarchive: (DelinquentEntity) -> Unit, onDismiss: () -> Unit) {
    val sortedArchived = archived.sortedBy { naturalSortApartments(it.delinquent.apartment) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Notificações Arquivadas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                if (sortedArchived.isEmpty()) {
                    Text("Nenhuma notificação arquivada.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 400.dp)) {
                    items(sortedArchived) { item ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text(item.delinquent.apartment, fontWeight = FontWeight.Bold)
                                    Row {
                                        IconButton({ onUnarchive(item.delinquent) }, Modifier.size(32.dp)) { Icon(Icons.Default.Unarchive, null, tint = MaterialTheme.colorScheme.primary) }
                                        IconButton({ onDelete(item.delinquent) }, Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                                    }
                                }
                                Text(item.delinquent.ownerName, style = MaterialTheme.typography.bodySmall)
                                Text("Dívida: R$ ${formatCurrency(item.delinquent.totalDebt)}", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Fechar") }
            }
        }
    }
}

@Composable
fun NotificationLine(label: String, date: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(if (date != null) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, Modifier.size(14.dp), tint = if (date != null) Color(0xFF4CAF50) else Color.Gray)
        Spacer(Modifier.width(4.dp)); Text("$label: ${date ?: "Pendente"}", style = MaterialTheme.typography.labelSmall, color = if (date != null) Color.Unspecified else Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDelinquentDialog(editingDelinquent: DelinquentEntity? = null, dao: AppDao, onDismiss: () -> Unit, onConfirm: (DelinquentEntity) -> Unit) {
    var apto by remember { mutableStateOf(editingDelinquent?.apartment ?: "") }
    var nome by remember { mutableStateOf(editingDelinquent?.ownerName ?: "") }
    var divDigits by remember { mutableStateOf(editingDelinquent?.totalDebt?.let { (it * 100).toLong().toString() } ?: "") }
    
    val units by dao.getAllUnits().collectAsState(initial = emptyList())

    LaunchedEffect(apto) {
        if (apto.isNotBlank() && editingDelinquent == null) {
            val matched = units.find { it.apartment.trim().equals(apto.trim(), ignoreCase = true) }
            if (matched != null && matched.ownerName.isNotBlank()) {
                nome = matched.ownerName
            }
        }
    }
    
    val initialYears = editingDelinquent?.registrationDate?.split(Regex("[,/\\s]+"))?.filter { it.isNotBlank() } 
        ?: listOf(Calendar.getInstance().get(Calendar.YEAR).toString())
    val debtYearsList = remember { mutableStateListOf(*initialYears.toTypedArray()) }

    var hasMadeAgreement by remember { mutableStateOf(editingDelinquent?.hasMadeAgreement ?: false) }
    var n1Date by remember { mutableStateOf(editingDelinquent?.notification1Date ?: "") }
    var n2Date by remember { mutableStateOf(editingDelinquent?.notification2Date ?: "") }
    var n3Date by remember { mutableStateOf(editingDelinquent?.notification3Date ?: "") }
    var activePickerField by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingDelinquent != null) "Editar Unidade" else "Cadastrar para Notificação") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 500.dp)) {
                item { OutlinedTextField(apto, { apto = it }, label = { Text("Apto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
                item { OutlinedTextField(nome, { nome = it }, label = { Text("Nome do Proprietário") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
                item { OutlinedTextField(divDigits, { if (it.length <= 12) divDigits = it.filter { char -> char.isDigit() } }, label = { Text("Dívida Total") }, prefix = { Text("R$ ") }, visualTransformation = CurrencyVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) }
                
                item {
                    Text("Anos da Dívida:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                items(debtYearsList.size) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = debtYearsList[index],
                            onValueChange = { debtYearsList[index] = it },
                            label = { Text("Ano ${index + 1}") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (debtYearsList.size > 1) {
                            IconButton(onClick = { debtYearsList.removeAt(index) }) {
                                Icon(Icons.Default.Delete, "Remover Ano", tint = Color.Red)
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { debtYearsList.add(Calendar.getInstance().get(Calendar.YEAR).toString()) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Adicionar Outro Ano da Dívida (+)")
                    }
                }

                item { Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(hasMadeAgreement, { hasMadeAgreement = it }); Text("Fez acordo (Acordo Feito)") } }
                item { Text("Datas de Notificação:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                item { NotificationDateField("1ª Notificação", n1Date, { n1Date = "" }, { activePickerField = 1 }) }
                item { NotificationDateField("2ª Notificação", n2Date, { n2Date = "" }, { activePickerField = 2 }) }
                item { NotificationDateField("3ª Notificação", n3Date, { n3Date = "" }, { activePickerField = 3 }) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val total = (divDigits.toDoubleOrNull() ?: 0.0) / 100
                val combinedYears = debtYearsList.filter { it.isNotBlank() }.joinToString(", ")
                onConfirm(
                    DelinquentEntity(
                        id = editingDelinquent?.id ?: System.currentTimeMillis(),
                        apartment = apto,
                        ownerName = nome,
                        totalDebt = total,
                        registrationDate = if (combinedYears.isNotBlank()) combinedYears else Calendar.getInstance().get(Calendar.YEAR).toString(),
                        notification1Date = n1Date.ifBlank { null },
                        notification2Date = n2Date.ifBlank { null },
                        notification3Date = n3Date.ifBlank { null },
                        hasMadeAgreement = hasMadeAgreement,
                        isArchived = editingDelinquent?.isArchived ?: false
                    )
                )
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onDismiss) {
                Text("Cancelar")
            }
        }
    )
    if (activePickerField != null) {
        val state = rememberDatePickerState(initialSelectedDateMillis = try { 
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(
                when (activePickerField) { 1 -> n1Date; 2 -> n2Date; 3 -> n3Date; else -> "" }
            )?.time 
        } catch(_: Exception) { null })
        DatePickerDialog({ activePickerField = null }, { 
            TextButton({ 
                state.selectedDateMillis?.let { 
                    val f = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(it)) 
                    when (activePickerField) { 1 -> n1Date = f; 2 -> n2Date = f; 3 -> n3Date = f } 
                }
                activePickerField = null 
            }) { Text("OK") } 
        }) { DatePicker(state) }
    }
}

@Composable
fun NotificationDateField(label: String, value: String, onClear: () -> Unit, onPick: () -> Unit) {
    OutlinedTextField(value = value, onValueChange = {}, label = { Text(label) }, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { onPick() }, trailingIcon = { Row { if (value.isNotBlank()) IconButton(onClear) { Icon(Icons.Default.Clear, null) } ; IconButton(onPick) { Icon(Icons.Default.CalendarToday, null) } } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAgreementDialog(
    title: String = "Configurar Acordo", 
    editingAgreement: AgreementWithInstallments? = null, 
    isRenewal: Boolean = false, 
    dao: AppDao, 
    onDismiss: () -> Unit, 
    onConfirm: (AgreementEntity, List<InstallmentEntity>) -> Unit, 
    onAjuizar: (AgreementEntity) -> Unit = {}
) {
    var apto by remember { mutableStateOf(editingAgreement?.agreement?.apartment ?: "") }
    var nome by remember { mutableStateOf(editingAgreement?.agreement?.ownerName ?: "") }
    var divDigits by remember { mutableStateOf(editingAgreement?.agreement?.totalDebt?.let { (it * 100).toLong().toString() } ?: "") }
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var dataAcordo by remember { mutableStateOf(editingAgreement?.agreement?.date ?: sdf.format(Date())) }
    var qtd by remember { mutableStateOf(editingAgreement?.agreement?.installmentsCount?.toString() ?: "1") }; var showPicker by remember { mutableStateOf(false) }
    
    val units by dao.getAllUnits().collectAsState(initial = emptyList())

    LaunchedEffect(apto) {
        if (apto.isNotBlank() && (editingAgreement == null || isRenewal)) {
            val matched = units.find { it.apartment.trim().equals(apto.trim(), ignoreCase = true) }
            if (matched != null && matched.ownerName.isNotBlank()) {
                nome = matched.ownerName
            }
        }
    }

    var showQuotaDialog by remember { mutableStateOf(false) }
    val initialQuotas = editingAgreement?.agreement?.quotaMonths?.split(Regex("[,;\\s]+"))?.filter { it.isNotBlank() } ?: emptyList()
    val quotaList = remember { mutableStateListOf<String>().apply { addAll(initialQuotas) } }

    val pickerState = rememberDatePickerState(initialSelectedDateMillis = try { 
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(dataAcordo)?.time 
    } catch(_: Exception) { System.currentTimeMillis() })
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (editingAgreement != null && !isRenewal) "Editar Acordo" else title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(apto, { apto = it }, label = { Text("Apto") }, enabled = !isRenewal && editingAgreement == null, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome do Proprietário") }, enabled = !isRenewal && editingAgreement == null, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(divDigits, { it -> if (it.length <= 12) divDigits = it.filter { it.isDigit() } }, label = { Text("Dívida") }, prefix = { Text("R$ ") }, visualTransformation = CurrencyVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(dataAcordo, {}, label = { Text("Data da 1ª parcela") }, readOnly = true, trailingIcon = { IconButton({ showPicker = true }) { Icon(Icons.Default.CalendarToday, null) } }, modifier = Modifier.clickable { showPicker = true }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(qtd, { qtd = it }, label = { Text("Parcelas") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Cotas Condominiais", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { showQuotaDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (quotaList.isEmpty()) "Selecionar Cotas Condominiais (+)" else "Cotas Selecionadas (${quotaList.size}) (+)")
                    }

                    if (quotaList.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quotaList.sorted()) { quota ->
                                InputChip(
                                    selected = true,
                                    onClick = { quotaList.remove(quota) },
                                    label = { Text(quota) },
                                    trailingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = "Remover", modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                        }
                    }
                }

                val totalCalc = (divDigits.toDoubleOrNull() ?: 0.0) / 100
                val countCalc = qtd.toIntOrNull() ?: 1
                val valParcela = if (countCalc > 0) totalCalc / countCalc else 0.0
                
                OutlinedTextField(
                    value = formatCurrency(valParcela),
                    onValueChange = {},
                    label = { Text("Valor das Parcelas") },
                    readOnly = true,
                    prefix = { Text("R$ ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (editingAgreement != null) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val id = editingAgreement.agreement.id
                            val total = (divDigits.toDoubleOrNull() ?: 0.0) / 100
                            val count = qtd.toIntOrNull() ?: 1
                            val valParcelaCalc = total / count
                            val ownerName = editingAgreement.agreement.ownerName
                            val combinedQuotas = quotaList.filter { it.isNotBlank() }.joinToString(";")
                            onAjuizar(AgreementEntity(id, apto, ownerName, total, dataAcordo, count, valParcelaCalc, false, editingAgreement.agreement.originalAgreementId, true, editingAgreement.agreement.n1Date, editingAgreement.agreement.n2Date, editingAgreement.agreement.n3Date, combinedQuotas))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Gavel, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Encaminhar para Jurídico")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val total = (divDigits.toDoubleOrNull() ?: 0.0) / 100
                val count = (qtd.toIntOrNull() ?: 1).let { if (it <= 0) 1 else it }
                val totalBD = BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP)
                val valParcelaBD = totalBD.divide(BigDecimal.valueOf(count.toLong()), 2, RoundingMode.DOWN)
                val dates = calculateMonthlyDates(dataAcordo, count)
                var acc = BigDecimal.ZERO
                val insts = mutableListOf<InstallmentEntity>()
                val id = if (isRenewal) System.currentTimeMillis() else (editingAgreement?.agreement?.id ?: System.currentTimeMillis())
                for (i in 1..count) { 
                    val vBD = if (i == count) totalBD.subtract(acc) else valParcelaBD
                    acc = acc.add(vBD)
                    insts.add(InstallmentEntity(id + i + System.nanoTime(), id, i, vBD.toDouble(), dates[i-1])) 
                }
                val ownerName = if (nome.isNotBlank()) nome else (editingAgreement?.agreement?.ownerName ?: "Proprietário")
                val combinedQuotas = quotaList.filter { it.isNotBlank() }.joinToString(";")
                onConfirm(AgreementEntity(id, apto, ownerName, total, dataAcordo, count, valParcelaBD.toDouble(), false, if (isRenewal) editingAgreement?.agreement?.id else editingAgreement?.agreement?.originalAgreementId, editingAgreement?.agreement?.isLawsuit ?: false, editingAgreement?.agreement?.n1Date, editingAgreement?.agreement?.n2Date, editingAgreement?.agreement?.n3Date, combinedQuotas), insts)
            }, shape = RoundedCornerShape(12.dp)) { Text("Salvar") }
        }, dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
    if (showPicker) DatePickerDialog({ showPicker = false }, { 
        TextButton({ 
            pickerState.selectedDateMillis?.let { 
                dataAcordo = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(it)) 
            }
            showPicker = false 
        }) { Text("OK") } 
    }) { DatePicker(pickerState) }

    if (showQuotaDialog) {
        QuotaSelectionDialog(quotaList) {
            showQuotaDialog = false
        }
    }
}

@Composable
fun QuotaSelectionDialog(
    selectedQuotas: MutableList<String>,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    val years = listOf("2024", "2025", "2026", "2027", "2028")
    val months = listOf(
        Pair("01", "Janeiro"),
        Pair("02", "Fevereiro"),
        Pair("03", "Março"),
        Pair("04", "Abril"),
        Pair("05", "Maio"),
        Pair("06", "Junho"),
        Pair("07", "Julho"),
        Pair("08", "Agosto"),
        Pair("09", "Setembro"),
        Pair("10", "Outubro"),
        Pair("11", "Novembro"),
        Pair("12", "Dezembro")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Cotas Condominiais") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Escolha o ano e selecione os meses (ex: 06/2024):", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    years.forEach { yr ->
                        FilterChip(
                            selected = selectedYear == yr,
                            onClick = { selectedYear = yr },
                            label = { Text(yr) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                months.forEach { pair ->
                    val num = pair.first
                    val name = pair.second
                    val quotaKey = "$num/$selectedYear"
                    val isChecked = selectedQuotas.contains(quotaKey)
                    Surface(
                        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (isChecked) selectedQuotas.remove(quotaKey)
                            else selectedQuotas.add(quotaKey)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$name ($num/$selectedYear)", fontWeight = FontWeight.Medium)
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (!selectedQuotas.contains(quotaKey)) selectedQuotas.add(quotaKey)
                                    } else {
                                        selectedQuotas.remove(quotaKey)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Concluir")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentsDetailsDialog(agreement: AgreementWithInstallments, onDismiss: () -> Unit, onInstallmentUpdate: (InstallmentEntity) -> Unit) {
    var editDateFor by remember { mutableStateOf<InstallmentEntity?>(null) }; var undoFor by remember { mutableStateOf<InstallmentEntity?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Parcelas - ${agreement.agreement.apartment}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nº", Modifier.weight(0.5f), fontWeight = FontWeight.Bold); Text("Vencimento", Modifier.weight(1.1f), fontWeight = FontWeight.Bold); Text("Valor", Modifier.weight(0.9f), fontWeight = FontWeight.Bold); Text("Ação", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
                LazyColumn(Modifier.heightIn(max = 400.dp)) {
                    items(agreement.installments.sortedBy { it.number }) { inst ->
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${inst.number}/${agreement.agreement.installmentsCount}", Modifier.weight(0.5f), style = MaterialTheme.typography.bodySmall)
                            Row(Modifier.weight(1.1f), verticalAlignment = Alignment.CenterVertically) { Text(inst.dueDate, style = MaterialTheme.typography.bodySmall); if (!inst.isPaid) IconButton({ editDateFor = inst }, Modifier.size(24.dp)) { Icon(Icons.Default.EditCalendar, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) } }
                            Text("R$ ${formatCurrency(inst.value)}", Modifier.weight(0.9f), style = MaterialTheme.typography.bodySmall)
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (!inst.isPaid) Button({ onInstallmentUpdate(inst.copy(isPaid = true)) }, Modifier.height(28.dp), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Pagar", style = MaterialTheme.typography.labelSmall) }
                                else IconButton({ undoFor = inst }, Modifier.size(28.dp)) { Icon(Icons.AutoMirrored.Filled.Undo, null, tint = Color(0xFF4CAF50)) }
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
                Spacer(Modifier.height(16.dp)); Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Fechar") }
            }
        }
        if (editDateFor != null) {
            val state = rememberDatePickerState(initialSelectedDateMillis = try { 
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(editDateFor!!.dueDate)?.time 
            } catch(_: Exception) { null })
            DatePickerDialog(onDismissRequest = { editDateFor = null }, confirmButton = { 
                TextButton({ 
                    state.selectedDateMillis?.let { 
                        val f = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(it))
                        onInstallmentUpdate(editDateFor!!.copy(dueDate = f)) 
                    }
                    editDateFor = null 
                }) { Text("OK") } 
            }, dismissButton = { TextButton({ editDateFor = null }) { Text("Cancelar") } }) { DatePicker(state) }
        }
        if (undoFor != null) PasswordUndoDialog({ undoFor = null }, { onInstallmentUpdate(undoFor!!.copy(isPaid = false)); undoFor = null })
    }
}

@Composable
fun ArchivedAgreementsDialog(archived: List<AgreementWithInstallments>, onDelete: (AgreementWithInstallments) -> Unit, onUnarchive: (AgreementWithInstallments) -> Unit, onDismiss: () -> Unit) {
    val sortedArchived = archived.sortedBy { naturalSortApartments(it.agreement.apartment) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Acordos Arquivados", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                if (sortedArchived.isEmpty()) {
                    Text("Nenhum acordo arquivado.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 400.dp)) {
                    items(sortedArchived) { item ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text(item.agreement.apartment, fontWeight = FontWeight.Bold)
                                    Row {
                                        IconButton({ onUnarchive(item) }, Modifier.size(32.dp)) { Icon(Icons.Default.Unarchive, null, tint = MaterialTheme.colorScheme.primary) }
                                        IconButton({ onDelete(item) }, Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                                    }
                                }
                                Text(item.agreement.ownerName, style = MaterialTheme.typography.bodySmall)
                                Text("Dívida Original: R$ ${formatCurrency(item.agreement.totalDebt)}", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                                Text("Data: ${item.agreement.date}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Fechar") }
            }
        }
    }
}

@Composable
fun PasswordUndoDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var pwd by remember { mutableStateOf("") }; var isErr by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Desfazer Pagamento") }, text = { Column { Text("Digite a senha:"); OutlinedTextField(value = pwd, onValueChange = { pwd = it; isErr = false }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), isError = isErr); if (isErr) Text("Senha incorreta!", color = Color.Red, style = MaterialTheme.typography.labelSmall) } },
        confirmButton = { Button({ if (pwd == "123456") onConfirm() else isErr = true }) { Text("Confirmar") } }, dismissButton = { TextButton(onDismiss) { Text("Cancelar") } })
}

@Composable
fun HistoryDialog(apartment: String, history: List<AgreementWithInstallments>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Histórico: $apartment", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (history.isEmpty()) Text("Nenhum acordo anterior encontrado.")
                LazyColumn(Modifier.heightIn(max = 400.dp)) { items(history) { old -> Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Column(modifier = Modifier.padding(12.dp)) { Text("Data: ${old.agreement.date}", fontWeight = FontWeight.Bold); Text("Dívida: R$ ${formatCurrency(old.agreement.totalDebt)}"); val paid = old.installments.count { it.isPaid }; Text("Pagamento: $paid de ${old.agreement.installmentsCount} pagas", style = MaterialTheme.typography.bodySmall) } } } }
                Spacer(Modifier.height(16.dp)); Button(onDismiss, Modifier.align(Alignment.End)) { Text("Fechar") }
            }
        }
    }
}

@Composable
fun ReportTypeDialog(onDismiss: () -> Unit, onSelect: (Boolean) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tipo de Relatório") }, text = { Text("Deseja um relatório resumido ou detalhado?") },
        confirmButton = { Button({ onSelect(true) }) { Text("Detalhado") } }, dismissButton = { TextButton({ onSelect(false) }) { Text("Resumido") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDialog = true
                false
            } else {
                false
            }
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja excluir este registro?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(item)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) { Text("Excluir", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = Color.Gray.copy(alpha = 0.8f)
            Box(
                Modifier.fillMaxSize().background(color, RoundedCornerShape(8.dp)).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, null, tint = Color.White)
            }
        },
        content = { content(item) }
    )
}
