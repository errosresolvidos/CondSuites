package com.example.condsuites.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.FloorEntity
import com.example.condsuites.data.model.OccurrenceTypeEntity
import com.example.condsuites.data.model.ProcessStatusEntity
import com.example.condsuites.data.model.ServiceDescriptionEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.utils.ExcelHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dao: AppDao,
    currentUser: UserEntity,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                Text(
                    "Configurações do Sistema",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Geral", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Usuários", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.People, contentDescription = null) }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
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
                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ManageUsersSection(dao = dao, currentUser = currentUser, scope = scope)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersSection(
    dao: AppDao,
    currentUser: UserEntity,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val users by dao.getAllUsersFlow().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showUserDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) {
            users
        } else {
            users.filter {
                it.username.contains(searchQuery, ignoreCase = true) ||
                it.role.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Cadastro e Edição de Usuários",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Gerencie as contas e permissões dos usuários do sistema.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        editingUser = null
                        showUserDialog = true
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Novo Usuário", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar usuário por nome ou perfil...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isBlank()) "Nenhum usuário cadastrado." else "Nenhum usuário encontrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                filteredUsers.forEach { user ->
                    UserItemRow(
                        user = user,
                        isCurrentUser = user.username.equals(currentUser.username, ignoreCase = true),
                        onEdit = {
                            editingUser = user
                            showUserDialog = true
                        },
                        onDelete = {
                            userToDelete = user
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showUserDialog) {
        UserFormDialog(
            user = editingUser,
            dao = dao,
            scope = scope,
            onDismiss = {
                showUserDialog = false
                editingUser = null
            },
            onSaved = {
                showUserDialog = false
                editingUser = null
            }
        )
    }

    if (userToDelete != null) {
        val targetUser = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Excluir Usuário") },
            text = { Text("Tem certeza que deseja excluir o usuário '${targetUser.username}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        val user = targetUser
                        userToDelete = null
                        if (user.username.equals("admin", ignoreCase = true)) {
                            Toast.makeText(context, "Não é possível excluir o usuário admin principal.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (user.username.equals(currentUser.username, ignoreCase = true)) {
                            Toast.makeText(context, "Não é possível excluir sua própria conta enquanto estiver conectado.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch(Dispatchers.IO) {
                            dao.deleteUser(user.id)
                            FirestoreSyncManager.syncUser(user, isDelete = true)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Usuário '${user.username}' excluído com sucesso.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserItemRow(
    user: UserEntity,
    isCurrentUser: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            user.username,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (isCurrentUser) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Você",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                user.role,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (showPassword) user.password else "••••••••",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            IconButton(
                                onClick = { showPassword = !showPassword },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Ocultar senha" else "Mostrar senha",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(
                    onClick = onDelete,
                    enabled = !user.username.equals("admin", ignoreCase = true) && !isCurrentUser
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = if (!user.username.equals("admin", ignoreCase = true) && !isCurrentUser) Color.Red else Color.LightGray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormDialog(
    user: UserEntity?,
    dao: AppDao,
    scope: CoroutineScope,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf(user?.username ?: "") }
    var password by remember { mutableStateOf(user?.password ?: "") }
    var role by remember { mutableStateOf(user?.role ?: "PORTEIRO") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expandedRole by remember { mutableStateOf(false) }

    val rolesList = listOf("ADMIN", "Síndico", "Conselheiro Fiscal", "Porteiro", "Zelador", "Manutenção", "Limpeza", "Administração")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Cadastrar Novo Usuário" else "Editar Usuário") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = null
                    },
                    label = { Text("Nome de Usuário") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Senha") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = !expandedRole }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perfil / Cargo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        rolesList.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    expandedRole = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uName = username.trim()
                    val uPass = password.trim()
                    val uRole = role.trim()

                    if (uName.isBlank()) {
                        errorMessage = "Informe o nome de usuário."
                        return@Button
                    }
                    if (uPass.isBlank()) {
                        errorMessage = "Informe a senha."
                        return@Button
                    }

                    scope.launch(Dispatchers.IO) {
                        val existing = dao.getUserByUsername(uName)
                        if (existing != null && (user == null || existing.id != user.id)) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Já existe um usuário cadastrado com este nome."
                            }
                            return@launch
                        }

                        // If user renamed username, clean up old Firestore document
                        if (user != null && !user.username.equals(uName, ignoreCase = true)) {
                            FirestoreSyncManager.syncUser(user, isDelete = true)
                        }

                        val userToSave = UserEntity(
                            id = user?.id ?: 0,
                            username = uName,
                            password = uPass,
                            role = uRole
                        )

                        dao.insertUser(userToSave)
                        FirestoreSyncManager.syncUser(userToSave)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                if (user == null) "Usuário cadastrado com sucesso!" else "Usuário atualizado com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSaved()
                        }
                    }
                }
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
