package com.example.condsuites.ui.screens

import android.content.Context
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
import com.example.condsuites.data.model.ContractEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.utils.CurrencyVisualTransformation
import com.example.condsuites.utils.formatCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractsScreen(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope) {
    val contracts by dao.getContracts().collectAsState(initial = emptyList())

    var searchText by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }
    var showAddDialog by remember { mutableStateOf(false) }
    var contractToEdit by remember { mutableStateOf<ContractEntity?>(null) }

    val categories = remember(contracts) {
        listOf("Todos") + contracts.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredContracts = remember(contracts, searchText, selectedCategoryFilter) {
        contracts.filter { contract ->
            val matchesCategory = selectedCategoryFilter == "Todos" || contract.category.equals(selectedCategoryFilter, ignoreCase = true)
            val fullText = "${contract.companyName} ${contract.serviceType} ${contract.category} ${contract.notes} ${contract.contactName}".lowercase()
            val matchesSearch = searchText.isBlank() || fullText.contains(searchText.lowercase())

            matchesCategory && matchesSearch
        }
    }

    val totalValue = filteredContracts.sumOf { it.monthlyValue }

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
                    Text("Gestão de Contratos de Prestadores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total de Contratos", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("${filteredContracts.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Custo Mensal Estimado", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("R$ ${formatCurrency(totalValue)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por empresa, serviço ou contato...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Clear, null) }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (categories.size > 1) {
                    var expandedCat by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedCat,
                        onExpandedChange = { expandedCat = !expandedCat },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategoryFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar por Categoria") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCat,
                            onDismissRequest = { expandedCat = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategoryFilter = cat
                                        expandedCat = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Novo Contrato")
            }
        }

        item {
            Text("Contratos Registrados (${filteredContracts.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (filteredContracts.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum contrato cadastrado.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredContracts) { contract ->
                ContractCard(
                    contract = contract,
                    currentUser = currentUser,
                    onEdit = { contractToEdit = contract },
                    onDelete = {
                        scope.launch(Dispatchers.IO) {
                            dao.deleteContract(contract.id)
                            FirestoreSyncManager.syncContract(contract, isDelete = true)
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        RegisterContractDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newContract ->
                scope.launch(Dispatchers.IO) {
                    dao.insertContract(newContract)
                    FirestoreSyncManager.syncContract(newContract)
                }
                showAddDialog = false
            }
        )
    }

    if (contractToEdit != null) {
        EditContractDialog(
            contract = contractToEdit!!,
            onDismiss = { contractToEdit = null },
            onConfirm = { updated ->
                scope.launch(Dispatchers.IO) {
                    dao.updateContract(updated)
                    FirestoreSyncManager.syncContract(updated)
                }
                contractToEdit = null
            }
        )
    }
}

@Composable
fun ContractCard(
    contract: ContractEntity,
    currentUser: UserEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(contract.companyName.uppercase(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        contract.category.ifBlank { "Geral" },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Serviço: ${contract.serviceType}", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("R$ ${formatCurrency(contract.monthlyValue)} / mês", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                if (currentUser.role == "ADMIN" || currentUser.role == "Síndico") {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text("Contato: ${contract.contactName.ifBlank { "Não informado" }}", style = MaterialTheme.typography.bodySmall)
                Text("Telefone: ${contract.phone.ifBlank { "Não informado" }}", style = MaterialTheme.typography.bodySmall)
                Text("E-mail: ${contract.email.ifBlank { "Não informado" }}", style = MaterialTheme.typography.bodySmall)
                Text("Vigência: ${contract.startDate} a ${contract.endDate.ifBlank { "Indeterminado" }}", style = MaterialTheme.typography.bodySmall)

                if (contract.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Observações: ${contract.notes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContractDialog(
    onDismiss: () -> Unit,
    onConfirm: (ContractEntity) -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Manutenção") }
    var monthlyValueStr by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var endDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val monthlyValue = (monthlyValueStr.toDoubleOrNull() ?: 0.0) / 100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Contrato") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Nome da Empresa / Prestador *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Tipo de Serviço (ex: Manutenção Elevadores) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria (ex: Segurança, Limpeza)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = monthlyValueStr,
                    onValueChange = { if (it.length <= 12) monthlyValueStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Valor Mensal *") },
                    prefix = { Text("R$ ") },
                    visualTransformation = CurrencyVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Pessoa de Contato") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Início") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Fim (Opcional)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (companyName.isNotBlank() && serviceType.isNotBlank()) {
                        onConfirm(
                            ContractEntity(
                                id = System.currentTimeMillis(),
                                companyName = companyName.trim(),
                                serviceType = serviceType.trim(),
                                category = category.trim(),
                                monthlyValue = monthlyValue,
                                contactName = contactName.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                startDate = startDate.trim(),
                                endDate = endDate.trim(),
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = companyName.isNotBlank() && serviceType.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cadastrar Contrato")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContractDialog(
    contract: ContractEntity,
    onDismiss: () -> Unit,
    onConfirm: (ContractEntity) -> Unit
) {
    var companyName by remember { mutableStateOf(contract.companyName) }
    var serviceType by remember { mutableStateOf(contract.serviceType) }
    var category by remember { mutableStateOf(contract.category) }
    var monthlyValueStr by remember { mutableStateOf((contract.monthlyValue * 100).toLong().toString()) }
    var contactName by remember { mutableStateOf(contract.contactName) }
    var phone by remember { mutableStateOf(contract.phone) }
    var email by remember { mutableStateOf(contract.email) }
    var startDate by remember { mutableStateOf(contract.startDate) }
    var endDate by remember { mutableStateOf(contract.endDate) }
    var notes by remember { mutableStateOf(contract.notes) }

    val monthlyValue = (monthlyValueStr.toDoubleOrNull() ?: 0.0) / 100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Contrato") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Nome da Empresa *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Tipo de Serviço *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = monthlyValueStr,
                    onValueChange = { if (it.length <= 12) monthlyValueStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Valor Mensal *") },
                    prefix = { Text("R$ ") },
                    visualTransformation = CurrencyVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Contato") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Início") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Fim") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        contract.copy(
                            companyName = companyName.trim(),
                            serviceType = serviceType.trim(),
                            category = category.trim(),
                            monthlyValue = monthlyValue,
                            contactName = contactName.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            startDate = startDate.trim(),
                            endDate = endDate.trim(),
                            notes = notes.trim()
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
