package com.example.condsuites.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.condsuites.data.model.FloorEntity
import com.example.condsuites.data.model.OccurrenceTypeEntity
import com.example.condsuites.data.model.ProcessStatusEntity
import com.example.condsuites.data.model.ServiceDescriptionEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.utils.ExcelHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    dao: AppDao,
    currentUser: UserEntity,
    scope: CoroutineScope
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Configurações do Sistema", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        item {
            BackupRestoreSection(dao = dao, context = context, scope = scope)
        }

        item {
            ManageFloorsSection(dao = dao, scope = scope)
        }

        item {
            ManageServiceDescriptionsSection(dao = dao, scope = scope)
        }

        item {
            ManageProcessStatusesSection(dao = dao, scope = scope)
        }

        item {
            ManageOccurrenceTypesSection(dao = dao, scope = scope)
        }
    }
}

@Composable
fun BackupRestoreSection(dao: AppDao, context: Context, scope: CoroutineScope) {
    var isLoading by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            isLoading = true
            scope.launch(Dispatchers.IO) {
                val success = ExcelHelper.importDatabase(context, dao, uri)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(context, if (success) "Dados importados com sucesso!" else "Falha ao importar dados.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        if (uri != null) {
            isLoading = true
            scope.launch(Dispatchers.IO) {
                val success = ExcelHelper.exportDatabase(context, dao, uri)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(context, if (success) "Exportado com sucesso!" else "Falha na exportação.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Backup & Restauração de Dados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Exporte todos os dados do condomínio para uma planilha Excel ou importe de um arquivo existente.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            exportLauncher.launch("CondSuites_Backup_$time.xlsx")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Exportar Excel", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Importar Excel", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun ManageFloorsSection(dao: AppDao, scope: CoroutineScope) {
    val floors by dao.getFloors().collectAsState(initial = emptyList())
    var newFloorText by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Gerenciar Andares Cadastrados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newFloorText,
                    onValueChange = { newFloorText = it },
                    label = { Text("Novo Andar (ex: 16º Andar)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newFloorText.isNotBlank()) {
                            val fl = newFloorText.trim()
                            scope.launch(Dispatchers.IO) {
                                dao.insertFloor(FloorEntity(floor = fl))
                            }
                            newFloorText = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(Modifier.height(12.dp))

            floors.forEach { fl ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(fl.floor, style = MaterialTheme.typography.bodyMedium)
                    IconButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                dao.deleteFloor(fl.floor)
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Excluir", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ManageServiceDescriptionsSection(dao: AppDao, scope: CoroutineScope) {
    val descs by dao.getServiceDescriptions().collectAsState(initial = emptyList())
    var newDescText by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Gerenciar Descrições de Serviços", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newDescText,
                    onValueChange = { newDescText = it },
                    label = { Text("Nova Descrição de Serviço") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newDescText.isNotBlank()) {
                            val d = newDescText.trim()
                            scope.launch(Dispatchers.IO) {
                                dao.insertServiceDescription(ServiceDescriptionEntity(description = d))
                            }
                            newDescText = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(Modifier.height(12.dp))

            descs.forEach { d ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(d.description, style = MaterialTheme.typography.bodyMedium)
                    IconButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                dao.deleteServiceDescription(d.description)
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Excluir", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ManageProcessStatusesSection(dao: AppDao, scope: CoroutineScope) {
    val statuses by dao.getProcessStatuses().collectAsState(initial = emptyList())
    var newStatusText by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Gerenciar Status de Processos Judicial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newStatusText,
                    onValueChange = { newStatusText = it },
                    label = { Text("Novo Status de Processo") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newStatusText.isNotBlank()) {
                            val st = newStatusText.trim()
                            scope.launch(Dispatchers.IO) {
                                dao.insertProcessStatus(ProcessStatusEntity(status = st))
                            }
                            newStatusText = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(Modifier.height(12.dp))

            statuses.forEach { st ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(st.status, style = MaterialTheme.typography.bodyMedium)
                    IconButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                dao.deleteProcessStatus(st.status)
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Excluir", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ManageOccurrenceTypesSection(dao: AppDao, scope: CoroutineScope) {
    val types by dao.getOccurrenceTypes().collectAsState(initial = emptyList())
    var newTypeText by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Gerenciar Tipos de Ocorrências", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newTypeText,
                    onValueChange = { newTypeText = it },
                    label = { Text("Novo Tipo de Ocorrência") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newTypeText.isNotBlank()) {
                            val tp = newTypeText.trim()
                            scope.launch(Dispatchers.IO) {
                                dao.insertOccurrenceType(OccurrenceTypeEntity(type = tp))
                            }
                            newTypeText = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }

            Spacer(Modifier.height(12.dp))

            types.forEach { tp ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tp.type, style = MaterialTheme.typography.bodyMedium)
                    IconButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                dao.deleteOccurrenceType(tp.type)
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Excluir", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
