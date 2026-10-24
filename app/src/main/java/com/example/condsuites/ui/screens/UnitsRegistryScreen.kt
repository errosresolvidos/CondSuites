package com.example.condsuites.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.AgreementWithInstallments
import com.example.condsuites.data.model.DelinquentWithProgress
import com.example.condsuites.data.model.DelinquencyHistoryEntity
import com.example.condsuites.data.model.LawsuitWithProgress
import com.example.condsuites.data.model.UnitEntity
import com.example.condsuites.data.model.UnitStatus
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.data.model.isComplete
import com.example.condsuites.data.model.status
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.ui.components.ActiveDebtorBanner
import com.example.condsuites.ui.components.ArchivedProcessBanner
import com.example.condsuites.ui.components.UnitMetricCard
import com.example.condsuites.ui.navigation.Screen
import com.example.condsuites.ui.screens.reports.generateUnitsHtmlReport
import com.example.condsuites.ui.screens.reports.generateUnitsTextReport
import com.example.condsuites.utils.ExcelHelper
import com.example.condsuites.utils.formatCurrency
import com.example.condsuites.utils.getUnitIdForApartment
import com.example.condsuites.utils.naturalSortApartments
import com.example.condsuites.utils.printHtmlReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

suspend fun deduplicateUnitsInDatabase(dao: AppDao) {
    try {
        val allUnits = dao.getAllUnitsList()
        if (allUnits.isEmpty()) return

        val grouped = allUnits.groupBy { it.apartment.trim().lowercase() }
        for ((_, list) in grouped) {
            if (list.size > 1) {
                val best = list.maxByOrNull {
                    (if (it.ownerName.isNotBlank()) 10 else 0) +
                    (if (it.phone.isNotBlank()) 5 else 0) +
                    (if (it.email.isNotBlank()) 5 else 0)
                } ?: list.first()

                val targetId = getUnitIdForApartment(best.apartment)
                val finalUnit = best.copy(id = targetId)

                list.forEach { dao.deleteUnit(it.id) }
                dao.insertUnit(finalUnit)
                FirestoreSyncManager.syncUnit(finalUnit)
            } else if (list.size == 1) {
                val unit = list.first()
                val targetId = getUnitIdForApartment(unit.apartment)
                if (unit.id != targetId) {
                    dao.deleteUnit(unit.id)
                    val updated = unit.copy(id = targetId)
                    dao.insertUnit(updated)
                    FirestoreSyncManager.syncUnit(updated)
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("DEDUP_UNITS", "Error deduplicating units: ${e.message}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitsRegistryScreen(
    dao: AppDao,
    context: Context,
    currentUser: UserEntity? = null,
    onNavigate: (Screen) -> Unit = {},
    onNavigateToAgreements: (apartment: String, tab: Int) -> Unit = { _, _ -> },
    showDashboard: Boolean = false,
    onDismissDashboard: () -> Unit = {}
) {
    val isAdmin = remember(currentUser) {
        currentUser == null || currentUser.role.equals("ADMIN", ignoreCase = true) || currentUser.username.equals("admin", ignoreCase = true)
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isAdmin) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Cadastro de Unidades", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Apartment, contentDescription = null) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Exportar / Importar", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.SwapVert, contentDescription = null) }
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (if (isAdmin) selectedTabIndex else 0) {
                0 -> {
                    UnitsListContent(
                        dao = dao,
                        context = context,
                        currentUser = currentUser,
                        onNavigate = onNavigate,
                        onNavigateToAgreements = onNavigateToAgreements,
                        showDashboard = showDashboard,
                        onDismissDashboard = onDismissDashboard
                    )
                }
                1 -> {
                    UnitsExportImportTab(
                        dao = dao,
                        context = context
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitsListContent(
    dao: AppDao,
    context: Context,
    currentUser: UserEntity? = null,
    onNavigate: (Screen) -> Unit = {},
    onNavigateToAgreements: (apartment: String, tab: Int) -> Unit = { _, _ -> },
    showDashboard: Boolean = false,
    onDismissDashboard: () -> Unit = {}
) {
    val units by dao.getAllUnits().collectAsState(initial = emptyList())
    val delinquents by dao.getDelinquents().collectAsState(initial = emptyList())
    val agreements by dao.getActiveAgreements().collectAsState(initial = emptyList())
    val lawsuits by dao.getLawsuitsWithProgress().collectAsState(initial = emptyList())

    val archivedDelinquents by dao.getArchivedDelinquents().collectAsState(initial = emptyList())
    val archivedAgreements by dao.getArchivedAgreements().collectAsState(initial = emptyList())
    val delinquencyHistory by dao.getDelinquencyHistory().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    val canSeeDebtors = remember(currentUser) {
        if (currentUser == null) return@remember true
        val role = currentUser.role.trim()
        val username = currentUser.username.trim()
        role.equals("ADMIN", ignoreCase = true) ||
        username.equals("admin", ignoreCase = true) ||
        role.equals("Síndico", ignoreCase = true) ||
        role.equals("Conselheiro Fiscal", ignoreCase = true) ||
        role.contains("Conselho", ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            deduplicateUnitsInDatabase(dao)

            if (dao.getUnitCount() == 0) {
                val defaultList = mutableListOf<UnitEntity>()
                for (floor in 2..12) {
                    for (u in 1..12) {
                        val apt = "${floor}${String.format(Locale.getDefault(), "%02d", u)}"
                        val fixedId = getUnitIdForApartment(apt)
                        defaultList.add(
                            UnitEntity(
                                id = fixedId,
                                apartment = apt,
                                floor = floor,
                                ownerName = "",
                                phone = "",
                                email = "",
                                notes = ""
                            )
                        )
                    }
                }
                dao.insertUnits(defaultList)
                defaultList.forEach { FirestoreSyncManager.syncUnit(it) }
            } else {
                val over12Unassigned = dao.getAllUnitsList().filter { it.floor > 12 && it.ownerName.isBlank() && it.phone.isBlank() }
                if (over12Unassigned.isNotEmpty()) {
                    over12Unassigned.forEach {
                        dao.deleteUnit(it.id)
                        FirestoreSyncManager.syncUnit(it, isDelete = true)
                    }
                }
            }
        }
    }

    var searchText by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var selectedFloorFilter by remember { mutableStateOf("Todos Os Andares") }

    LaunchedEffect(canSeeDebtors) {
        if (!canSeeDebtors && selectedStatusFilter == "Devedoras") {
            selectedStatusFilter = "Todos"
        }
    }

    val isAdmin = remember(currentUser) {
        currentUser == null || currentUser.role.equals("ADMIN", ignoreCase = true) || currentUser.username.equals("admin", ignoreCase = true)
    }

    var unitToEdit by remember { mutableStateOf<UnitEntity?>(null) }
    var unitToDelete by remember { mutableStateOf<UnitEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }

    val floorOptions = remember(units) {
        val floors = units.map { "${it.floor}º Andar" }.distinct().sortedBy {
            it.replace("º Andar", "").toIntOrNull() ?: 0
        }
        listOf("Todos Os Andares") + floors
    }

    val filteredUnits = remember(
        units, delinquents, agreements, lawsuits, searchText, selectedStatusFilter, selectedFloorFilter
    ) {
        units.filter { unit ->
            val aptClean = unit.apartment.trim()
            val hasDelinquent = delinquents.any { it.delinquent.apartment.trim().equals(aptClean, ignoreCase = true) }
            val hasAgreement = agreements.any { it.agreement.apartment.trim().equals(aptClean, ignoreCase = true) }
            val hasLawsuit = lawsuits.any { it.lawsuit.apartment.trim().equals(aptClean, ignoreCase = true) }
            val isDebtor = hasDelinquent || hasAgreement || hasLawsuit

            val matchesStatus = when (selectedStatusFilter) {
                "Cadastrados" -> unit.status == UnitStatus.CADASTRADO
                "Incompletos" -> unit.status == UnitStatus.INCOMPLETO
                "Pendentes" -> unit.status == UnitStatus.PENDENTE
                "Devedoras" -> canSeeDebtors && isDebtor
                else -> true
            }

            val matchesFloor = if (selectedFloorFilter == "Todos Os Andares") {
                true
            } else {
                val floorNum = selectedFloorFilter.replace("º Andar", "").toIntOrNull()
                unit.floor == floorNum
            }

            val fullText = "${unit.apartment} ${unit.ownerName} ${unit.residentType} ${unit.phone} ${unit.email} ${unit.notes}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesStatus && matchesFloor && matchesSearch
        }
    }

    val totalCount = units.size
    val registeredCount = units.count { it.status == UnitStatus.CADASTRADO }
    val incompleteCount = units.count { it.status == UnitStatus.INCOMPLETO }
    val pendingCount = units.count { it.status == UnitStatus.PENDENTE }
    val occupancyRate = if (totalCount > 0) (registeredCount * 100 / totalCount) else 0

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
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

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = selectedStatusFilter == "Todos",
                            onClick = { selectedStatusFilter = "Todos" },
                            label = {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = "Todos",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedStatusFilter == "Cadastrados",
                            onClick = { selectedStatusFilter = "Cadastrados" },
                            label = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Cadastrados",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedStatusFilter == "Incompletos",
                            onClick = { selectedStatusFilter = "Incompletos" },
                            label = {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Incompletos",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF57C00),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedStatusFilter == "Pendentes",
                            onClick = { selectedStatusFilter = "Pendentes" },
                            label = {
                                Icon(
                                    imageVector = Icons.Default.NotificationImportant,
                                    contentDescription = "Pendentes",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD32F2F),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (canSeeDebtors) {
                            FilterChip(
                                selected = selectedStatusFilter == "Devedoras",
                                onClick = { selectedStatusFilter = "Devedoras" },
                                label = {
                                    Icon(
                                        imageVector = Icons.Default.Gavel,
                                        contentDescription = "Devedoras",
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFB71C1C),
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtro: $selectedStatusFilter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${filteredUnits.size} unidade(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    var expandedFloorMenu by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedFloorMenu,
                        onExpandedChange = { expandedFloorMenu = !expandedFloorMenu },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedFloorFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar por Andar") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFloorMenu) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFloorMenu,
                            onDismissRequest = { expandedFloorMenu = false }
                        ) {
                            floorOptions.forEach { fl ->
                                DropdownMenuItem(
                                    text = { Text(fl) },
                                    onClick = {
                                        selectedFloorFilter = fl
                                        expandedFloorMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Unidades Encontradas (${filteredUnits.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (filteredUnits.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Nenhuma unidade encontrada para os filtros selecionados.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(filteredUnits.sortedWith(compareBy({ it.floor }, { naturalSortApartments(it.apartment) }))) { unit ->
                    val aptClean = unit.apartment.trim()

                    // Active
                    val delMatch = delinquents.find { it.delinquent.apartment.trim().equals(aptClean, ignoreCase = true) }
                    val aggMatch = agreements.find { it.agreement.apartment.trim().equals(aptClean, ignoreCase = true) }
                    val activeLawMatch = lawsuits.find {
                        it.lawsuit.apartment.trim().equals(aptClean, ignoreCase = true) &&
                        it.lawsuit.status != "Encerrado" && it.lawsuit.status != "Arquivado" && it.lawsuit.status != "Finalizado"
                    }

                    // Archived
                    val hasArchivedDel = archivedDelinquents.any { it.delinquent.apartment.trim().equals(aptClean, ignoreCase = true) }
                    val hasArchivedAgg = archivedAgreements.any { it.agreement.apartment.trim().equals(aptClean, ignoreCase = true) }
                    val archivedLawMatch = lawsuits.find {
                        it.lawsuit.apartment.trim().equals(aptClean, ignoreCase = true) &&
                        (it.lawsuit.status == "Encerrado" || it.lawsuit.status == "Arquivado" || it.lawsuit.status == "Finalizado")
                    }
                    val unitDelHistory = delinquencyHistory.filter { it.apartment.trim().equals(aptClean, ignoreCase = true) }

                    UnitCard(
                        unit = unit,
                        delinquent = delMatch,
                        agreement = aggMatch,
                        lawsuit = activeLawMatch,
                        hasArchivedDelinquent = hasArchivedDel,
                        hasArchivedAgreement = hasArchivedAgg,
                        archivedLawsuit = archivedLawMatch,
                        delinquencyHistory = unitDelHistory,
                        canSeeDebtors = canSeeDebtors,
                        isAdmin = isAdmin,
                        onEdit = { unitToEdit = unit },
                        onDelete = { unitToDelete = unit },
                        onNavigate = onNavigate,
                        onNavigateToAgreements = onNavigateToAgreements,
                        context = context
                    )
                }
            }

            // Espaço no final para evitar que o botão + flutuante sobreponha o último card
            item {
                Spacer(Modifier.height(88.dp))
            }
        }

        // Botão + Flutuante (Nova Unidade)
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nova Unidade")
        }
    }

    if (showDashboard) {
        AlertDialog(
            onDismissRequest = onDismissDashboard,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Dashboard das Unidades", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Resumo do preenchimento e situação das unidades:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UnitMetricCard("Total", totalCount.toString(), Icons.Default.Apartment, Modifier.weight(1f))
                        UnitMetricCard("Cadastras", registeredCount.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
                        UnitMetricCard("Incompletas", incompleteCount.toString(), Icons.Default.Warning, Modifier.weight(1f))
                        UnitMetricCard("Pendentes", pendingCount.toString(), Icons.Default.NotificationImportant, Modifier.weight(1f))
                        UnitMetricCard("Preenchimento", "$occupancyRate%", Icons.Default.Analytics, Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissDashboard,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Fechar")
                }
            }
        )
    }

    if (unitToEdit != null) {
        EditUnitDialog(
            unit = unitToEdit!!,
            isAdmin = isAdmin,
            onDismiss = { unitToEdit = null },
            onConfirm = { updated ->
                scope.launch {
                    dao.updateUnit(updated)
                    FirestoreSyncManager.syncUnit(updated)
                }
                unitToEdit = null
            },
            onDelete = {
                unitToDelete = unitToEdit
                unitToEdit = null
            }
        )
    }

    if (unitToDelete != null) {
        val targetUnit = unitToDelete!!
        AlertDialog(
            onDismissRequest = { unitToDelete = null },
            title = { Text("Excluir Unidade", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir a unidade Apto ${targetUnit.apartment} (${targetUnit.floor}º Andar)? Esta ação removerá a unidade do cadastro local e na nuvem.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            dao.deleteUnit(targetUnit.id)
                            FirestoreSyncManager.syncUnit(targetUnit, isDelete = true)
                        }
                        unitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir Unidade", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { unitToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAddDialog) {
        AddUnitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newUnit ->
                scope.launch {
                    dao.insertUnit(newUnit)
                    FirestoreSyncManager.syncUnit(newUnit)
                }
                showAddDialog = false
            }
        )
    }

    if (showPrintPreviewDialog) {
        val htmlContent = generateUnitsHtmlReport(filteredUnits)
        val textReport = generateUnitsTextReport(filteredUnits)

        UnitsReportPreviewDialog(
            htmlContent = htmlContent,
            textReport = textReport,
            totalCount = filteredUnits.size,
            onDismiss = { showPrintPreviewDialog = false },
            onPrintPdf = {
                printHtmlReport(context, htmlContent, "Cadastro_Unidades_CondSuites")
                showPrintPreviewDialog = false
            },
            onShareText = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Compartilhar Cadastro de Unidades"))
                showPrintPreviewDialog = false
            }
        )
    }
}

@Composable
fun UnitsExportImportTab(
    dao: AppDao,
    context: Context
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var importResultText by remember { mutableStateOf<String?>(null) }

    val units by dao.getAllUnits().collectAsState(initial = emptyList())
    val totalCount = units.size
    val registeredCount = units.count { it.status == UnitStatus.CADASTRADO }
    val incompleteCount = units.count { it.status == UnitStatus.INCOMPLETO }
    val pendingCount = units.count { it.status == UnitStatus.PENDENTE }

    val exportUnitsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) {
            isLoading = true
            scope.launch(Dispatchers.IO) {
                val success = ExcelHelper.exportUnitsTable(context, dao, uri)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(
                        context,
                        if (success) "Unidades exportadas com sucesso!" else "Falha ao exportar unidades.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val exportTemplateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) {
            isLoading = true
            scope.launch(Dispatchers.IO) {
                val success = ExcelHelper.exportUnitsTemplate(context, uri)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(
                        context,
                        if (success) "Modelo gerado com sucesso!" else "Falha ao gerar modelo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val importUnitsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isLoading = true
            scope.launch(Dispatchers.IO) {
                val resultMsg = ExcelHelper.importUnitsTable(context, dao, uri)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    importResultText = resultMsg
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Exportação e Importação das Unidades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Gerencie os dados das unidades do condomínio em lote utilizando planilhas Excel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        UnitMetricCard("Total", totalCount.toString(), Icons.Default.Apartment, Modifier.weight(1f))
                        UnitMetricCard("Cadastradas", registeredCount.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
                        UnitMetricCard("Incompletas", incompleteCount.toString(), Icons.Default.Warning, Modifier.weight(1f))
                        UnitMetricCard("Pendentes", pendingCount.toString(), Icons.Default.NotificationImportant, Modifier.weight(1f))
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Processando arquivo Excel...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Exportar Cadastro de Unidades",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Gere uma planilha Excel (.xlsx) contendo todas as unidades e dados dos moradores (Número, Andar, Morador, Telefone, E-mail e Observações).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                    exportUnitsLauncher.launch("Unidades_CondSuites_$time.xlsx")
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Exportar Unidades")
                            }

                            OutlinedButton(
                                onClick = {
                                    exportTemplateLauncher.launch("Modelo_Unidades_CondSuites.xlsx")
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Description, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Baixar Modelo")
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Importar Dados das Unidades",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Importe um arquivo Excel (.xlsx) para cadastrar ou atualizar os moradores das unidades em massa no sistema.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(12.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    "Formatos e Colunas da Planilha:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("• Coluna 1: ID ou Número do Apartamento (ex: 201, 304, 1212)", style = MaterialTheme.typography.bodySmall)
                                Text("• Coluna 2: Número do Apartamento (caso ID esteja na Coluna 1)", style = MaterialTheme.typography.bodySmall)
                                Text("• Coluna 3: Andar (ex: 2, 3, 12)", style = MaterialTheme.typography.bodySmall)
                                Text("• Coluna 4: Nome do Proprietário / Morador", style = MaterialTheme.typography.bodySmall)
                                Text("• Coluna 5: Telefone / Celular", style = MaterialTheme.typography.bodySmall)
                                Text("• Coluna 6: E-mail do Condômino", style = MaterialTheme.typography.bodySmall)
                                Text("• Coluna 7: Observações Gerais", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = {
                                importUnitsLauncher.launch(
                                    arrayOf(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/vnd.ms-excel"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Selecionar e Importar Planilha de Unidades")
                        }
                    }
                }
            }
        }
    }

    if (importResultText != null) {
        AlertDialog(
            onDismissRequest = { importResultText = null },
            title = { Text("Resultado da Importação de Unidades") },
            text = {
                Text(
                    importResultText!!,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { importResultText = null }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun parseContactList(input: String): List<String> {
    if (input.isBlank()) return listOf("")
    val list = input.split(",", "/", ";", "\n").map { it.trim() }.filter { it.isNotBlank() }
    return list.ifEmpty { listOf("") }
}

private fun joinContactList(list: List<String>): String {
    return list.map { it.trim() }.filter { it.isNotBlank() }.joinToString(", ")
}

@Composable
fun UnitCard(
    unit: UnitEntity,
    delinquent: DelinquentWithProgress? = null,
    agreement: AgreementWithInstallments? = null,
    lawsuit: LawsuitWithProgress? = null,
    hasArchivedDelinquent: Boolean = false,
    hasArchivedAgreement: Boolean = false,
    archivedLawsuit: LawsuitWithProgress? = null,
    delinquencyHistory: List<DelinquencyHistoryEntity> = emptyList(),
    canSeeDebtors: Boolean = true,
    isAdmin: Boolean = false,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onNavigate: (Screen) -> Unit = {},
    onNavigateToAgreements: (apartment: String, tab: Int) -> Unit = { _, _ -> },
    context: Context
) {
    val (statusText, statusColor) = when (unit.status) {
        UnitStatus.CADASTRADO -> "CADASTRADO" to Color(0xFF2E7D32)
        UnitStatus.INCOMPLETO -> "INCOMPLETO" to Color(0xFFF57C00)
        UnitStatus.PENDENTE -> "PENDENTE" to Color(0xFFD32F2F)
    }

    val isDebtor = delinquent != null || agreement != null || lawsuit != null
    val hasArchived = hasArchivedDelinquent || hasArchivedAgreement || archivedLawsuit != null || delinquencyHistory.isNotEmpty()

    val phones = remember(unit.phone) { parseContactList(unit.phone) }
    val emails = remember(unit.email) { parseContactList(unit.email) }

    var showPhoneMenu by remember { mutableStateOf(false) }
    var showWhatsappMenu by remember { mutableStateOf(false) }
    var showEmailMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "APTO ${unit.apartment}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val residentTypeTag = unit.residentType.ifBlank { "Proprietário" }.uppercase()
                    Text(residentTypeTag, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                }

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

            Spacer(Modifier.height(8.dp))
            val hasOwner = unit.ownerName.isNotBlank()
            Text(
                text = if (hasOwner) unit.ownerName else "Morador / Proprietário não cadastrado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (hasOwner) MaterialTheme.colorScheme.onSurface else Color.Gray
            )

            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    val firstPhone = phones.firstOrNull()?.ifBlank { null } ?: "Não informado"
                    val firstEmail = emails.firstOrNull()?.ifBlank { null } ?: "Não informado"
                    Text("📞 Fone: $firstPhone", style = MaterialTheme.typography.bodySmall)
                    Text("✉️ E-mail: $firstEmail", style = MaterialTheme.typography.bodySmall)
                    if (unit.notes.isNotBlank()) {
                        Text("📝 Obs: ${unit.notes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            if (canSeeDebtors) {
                if (isDebtor) {
                    Spacer(Modifier.height(10.dp))
                    ActiveDebtorBanner(
                        delinquent = delinquent,
                        agreement = agreement,
                        lawsuit = lawsuit,
                        onNavigate = onNavigate,
                        onNavigateToAgreements = onNavigateToAgreements
                    )
                } else if (hasArchived) {
                    Spacer(Modifier.height(10.dp))
                    ArchivedProcessBanner(
                        hasArchivedDelinquent = hasArchivedDelinquent,
                        hasArchivedAgreement = hasArchivedAgreement,
                        archivedLawsuit = archivedLawsuit,
                        delinquencyHistory = delinquencyHistory,
                        onNavigate = onNavigate,
                        onNavigateToAgreements = onNavigateToAgreements
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (unit.phone.isNotBlank()) {
                        Box {
                            IconButton(
                                onClick = {
                                    if (phones.size <= 1) {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phones.firstOrNull() ?: ""}"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    } else {
                                        showPhoneMenu = true
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Phone, "Ligar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }

                            if (phones.size > 1) {
                                DropdownMenu(
                                    expanded = showPhoneMenu,
                                    onDismissRequest = { showPhoneMenu = false }
                                ) {
                                    phones.forEach { ph ->
                                        DropdownMenuItem(
                                            text = { Text("Ligar para $ph") },
                                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                                            onClick = {
                                                showPhoneMenu = false
                                                try {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$ph"))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Box {
                            IconButton(
                                onClick = {
                                    if (phones.size <= 1) {
                                        try {
                                            val clean = (phones.firstOrNull() ?: "").replace(Regex("[^0-9]"), "")
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=55$clean"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    } else {
                                        showWhatsappMenu = true
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChatBubbleOutline, "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                            }

                            if (phones.size > 1) {
                                DropdownMenu(
                                    expanded = showWhatsappMenu,
                                    onDismissRequest = { showWhatsappMenu = false }
                                ) {
                                    phones.forEach { ph ->
                                        DropdownMenuItem(
                                            text = { Text("WhatsApp $ph") },
                                            leadingIcon = { Icon(Icons.Default.ChatBubbleOutline, null, tint = Color(0xFF25D366)) },
                                            onClick = {
                                                showWhatsappMenu = false
                                                try {
                                                    val clean = ph.replace(Regex("[^0-9]"), "")
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=55$clean"))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (unit.email.isNotBlank()) {
                        Box {
                            IconButton(
                                onClick = {
                                    if (emails.size <= 1) {
                                        try {
                                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${emails.firstOrNull() ?: ""}"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    } else {
                                        showEmailMenu = true
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Email, "E-mail", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }

                            if (emails.size > 1) {
                                DropdownMenu(
                                    expanded = showEmailMenu,
                                    onDismissRequest = { showEmailMenu = false }
                                ) {
                                    emails.forEach { em ->
                                        DropdownMenuItem(
                                            text = { Text("E-mail para $em") },
                                            leadingIcon = { Icon(Icons.Default.Email, null) },
                                            onClick = {
                                                showEmailMenu = false
                                                try {
                                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$em"))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isAdmin && onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Excluir Unidade", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }

                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar Cadastro", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUnitDialog(
    unit: UnitEntity,
    isAdmin: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (UnitEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var ownerName by remember { mutableStateOf(unit.ownerName) }
    var residentType by remember { mutableStateOf(unit.residentType.ifBlank { "Proprietário" }) }
    var phoneList by remember { mutableStateOf(parseContactList(unit.phone)) }
    var emailList by remember { mutableStateOf(parseContactList(unit.email)) }
    var notes by remember { mutableStateOf(unit.notes) }

    var phoneIndexToRemove by remember { mutableStateOf<Int?>(null) }
    var emailIndexToRemove by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastro do Apto ${unit.apartment} (${unit.floor}º Andar)") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Nome do Morador / Proprietário *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tipo de Morador *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = residentType == "Proprietário",
                            onClick = { residentType = "Proprietário" },
                            label = { Text("Proprietário", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (residentType == "Proprietário") { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = residentType == "Inquilino",
                            onClick = { residentType = "Inquilino" },
                            label = { Text("Inquilino", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (residentType == "Inquilino") { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Telefone / Celular (com DDD)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    phoneList.forEachIndexed { index, phoneVal ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = phoneVal,
                                onValueChange = { newVal ->
                                    val updated = phoneList.toMutableList()
                                    updated[index] = newVal
                                    phoneList = updated
                                },
                                label = { Text(if (phoneList.size > 1) "Telefone ${index + 1}" else "Telefone / Celular") },
                                placeholder = { Text("(11) 99999-8888") },
                                leadingIcon = { Icon(Icons.Default.Phone, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (phoneList.size > 1) {
                                IconButton(
                                    onClick = {
                                        phoneIndexToRemove = index
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.RemoveCircleOutline,
                                        contentDescription = "Remover Telefone",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = { phoneList = phoneList + "" },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Adicionar outro telefone", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("E-mail do Condômino", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    emailList.forEachIndexed { index, emailVal ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = emailVal,
                                onValueChange = { newVal ->
                                    val updated = emailList.toMutableList()
                                    updated[index] = newVal
                                    emailList = updated
                                },
                                label = { Text(if (emailList.size > 1) "E-mail ${index + 1}" else "E-mail do Condômino") },
                                placeholder = { Text("morador@email.com") },
                                leadingIcon = { Icon(Icons.Default.Email, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (emailList.size > 1) {
                                IconButton(
                                    onClick = {
                                        emailIndexToRemove = index
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.RemoveCircleOutline,
                                        contentDescription = "Remover E-mail",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = { emailList = emailList + "" },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Adicionar outro e-mail", style = MaterialTheme.typography.labelMedium)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações Gerais (Opcional)") },
                    placeholder = { Text("Ex: Vaga 42, Inquilino, Contato de emergência") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        unit.copy(
                            ownerName = ownerName.trim(),
                            residentType = residentType,
                            phone = joinContactList(phoneList),
                            email = joinContactList(emailList),
                            notes = notes.trim()
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar Cadastro")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isAdmin && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Excluir")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )

    if (phoneIndexToRemove != null) {
        val idx = phoneIndexToRemove!!
        val phoneVal = phoneList.getOrNull(idx)?.trim() ?: ""
        AlertDialog(
            onDismissRequest = { phoneIndexToRemove = null },
            title = { Text("Remover Telefone", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja remover ${if (phoneVal.isNotBlank()) "o telefone \"$phoneVal\"" else "este campo de telefone"}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = phoneList.toMutableList()
                        if (idx in updated.indices) {
                            updated.removeAt(idx)
                            phoneList = updated
                        }
                        phoneIndexToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remover", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { phoneIndexToRemove = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (emailIndexToRemove != null) {
        val idx = emailIndexToRemove!!
        val emailVal = emailList.getOrNull(idx)?.trim() ?: ""
        AlertDialog(
            onDismissRequest = { emailIndexToRemove = null },
            title = { Text("Remover E-mail", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja remover ${if (emailVal.isNotBlank()) "o e-mail \"$emailVal\"" else "este campo de e-mail"}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = emailList.toMutableList()
                        if (idx in updated.indices) {
                            updated.removeAt(idx)
                            emailList = updated
                        }
                        emailIndexToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remover", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { emailIndexToRemove = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUnitDialog(
    onDismiss: () -> Unit,
    onConfirm: (UnitEntity) -> Unit
) {
    var apartment by remember { mutableStateOf("") }
    var floorText by remember { mutableStateOf("2") }
    var ownerName by remember { mutableStateOf("") }
    var residentType by remember { mutableStateOf("Proprietário") }
    var phoneList by remember { mutableStateOf(listOf("")) }
    var emailList by remember { mutableStateOf(listOf("")) }
    var notes by remember { mutableStateOf("") }

    var phoneIndexToRemove by remember { mutableStateOf<Int?>(null) }
    var emailIndexToRemove by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Unidade") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = apartment,
                        onValueChange = { apartment = it },
                        label = { Text("Número (ex: 201) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { floorText = it },
                        label = { Text("Andar *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Nome do Morador") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tipo de Morador *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = residentType == "Proprietário",
                            onClick = { residentType = "Proprietário" },
                            label = { Text("Proprietário", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (residentType == "Proprietário") { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = residentType == "Inquilino",
                            onClick = { residentType = "Inquilino" },
                            label = { Text("Inquilino", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (residentType == "Inquilino") { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Telefone / Celular (com DDD)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    phoneList.forEachIndexed { index, phoneVal ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = phoneVal,
                                onValueChange = { newVal ->
                                    val updated = phoneList.toMutableList()
                                    updated[index] = newVal
                                    phoneList = updated
                                },
                                label = { Text(if (phoneList.size > 1) "Telefone ${index + 1}" else "Telefone") },
                                placeholder = { Text("(11) 99999-8888") },
                                leadingIcon = { Icon(Icons.Default.Phone, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (phoneList.size > 1) {
                                IconButton(
                                    onClick = {
                                        phoneIndexToRemove = index
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.RemoveCircleOutline,
                                        contentDescription = "Remover Telefone",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = { phoneList = phoneList + "" },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Adicionar outro telefone", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("E-mail do Condômino", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    emailList.forEachIndexed { index, emailVal ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = emailVal,
                                onValueChange = { newVal ->
                                    val updated = emailList.toMutableList()
                                    updated[index] = newVal
                                    emailList = updated
                                },
                                label = { Text(if (emailList.size > 1) "E-mail ${index + 1}" else "E-mail") },
                                placeholder = { Text("morador@email.com") },
                                leadingIcon = { Icon(Icons.Default.Email, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (emailList.size > 1) {
                                IconButton(
                                    onClick = {
                                        emailIndexToRemove = index
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.RemoveCircleOutline,
                                        contentDescription = "Remover E-mail",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = { emailList = emailList + "" },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Adicionar outro e-mail", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (apartment.isNotBlank()) {
                        val apt = apartment.trim()
                        val fl = floorText.toIntOrNull() ?: 2
                        val fixedId = getUnitIdForApartment(apt)
                        onConfirm(
                            UnitEntity(
                                id = fixedId,
                                apartment = apt,
                                floor = fl,
                                residentType = residentType,
                                ownerName = ownerName.trim(),
                                phone = joinContactList(phoneList),
                                email = joinContactList(emailList),
                                notes = notes.trim()
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (phoneIndexToRemove != null) {
        val idx = phoneIndexToRemove!!
        val phoneVal = phoneList.getOrNull(idx)?.trim() ?: ""
        AlertDialog(
            onDismissRequest = { phoneIndexToRemove = null },
            title = { Text("Remover Telefone", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja remover ${if (phoneVal.isNotBlank()) "o telefone \"$phoneVal\"" else "este campo de telefone"}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = phoneList.toMutableList()
                        if (idx in updated.indices) {
                            updated.removeAt(idx)
                            phoneList = updated
                        }
                        phoneIndexToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remover", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { phoneIndexToRemove = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (emailIndexToRemove != null) {
        val idx = emailIndexToRemove!!
        val emailVal = emailList.getOrNull(idx)?.trim() ?: ""
        AlertDialog(
            onDismissRequest = { emailIndexToRemove = null },
            title = { Text("Remover E-mail", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja remover ${if (emailVal.isNotBlank()) "o e-mail \"$emailVal\"" else "este campo de e-mail"}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = emailList.toMutableList()
                        if (idx in updated.indices) {
                            updated.removeAt(idx)
                            emailList = updated
                        }
                        emailIndexToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remover", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { emailIndexToRemove = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UnitsReportPreviewDialog(
    htmlContent: String,
    textReport: String,
    totalCount: Int,
    onDismiss: () -> Unit,
    onPrintPdf: () -> Unit,
    onShareText: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pré-visualização do Relatório de Cadastro") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Relatório com $totalCount unidade(s). Escolha a forma de exportação:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

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
