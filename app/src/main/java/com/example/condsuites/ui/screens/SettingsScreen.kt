package com.example.condsuites.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.AuditLogEntity
import com.example.condsuites.data.model.FloorEntity
import com.example.condsuites.data.model.OccurrenceTypeEntity
import com.example.condsuites.data.model.ProcessStatusEntity
import com.example.condsuites.data.model.ServiceDescriptionEntity
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.utils.AuditLogger
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
    val isAdmin = remember(currentUser) {
        currentUser.role.equals("ADMIN", ignoreCase = true) || currentUser.username.equals("admin", ignoreCase = true)
    }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isAdmin) {
        if (!isAdmin && selectedTabIndex != 0) {
            selectedTabIndex = 0
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = if (isAdmin) 0.dp else 16.dp
                )
            ) {
                Text(
                    "Configurações do Sistema",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )

                if (isAdmin) {
                    Spacer(Modifier.height(12.dp))

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Parâmetros", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Tune, contentDescription = null) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Usuários", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.People, contentDescription = null) }
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            text = { Text("Backup & Sistema", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Storage, contentDescription = null) }
                        )
                        Tab(
                            selected = selectedTabIndex == 3,
                            onClick = { selectedTabIndex = 3 },
                            text = { Text("Auditoria", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Security, contentDescription = null) }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (!isAdmin) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(48.dp)
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
                                        Text(currentUser.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Perfil: ${currentUser.role}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text("As configurações do sistema e gerenciamento de parâmetros são restritos aos administradores.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                when (selectedTabIndex) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                ManageFloorsSection(dao = dao, currentUser = currentUser, scope = scope)
                            }
                            item {
                                ManageServiceDescriptionsSection(dao = dao, currentUser = currentUser, scope = scope)
                            }
                            item {
                                ManageProcessStatusesSection(dao = dao, currentUser = currentUser, scope = scope)
                            }
                            item {
                                ManageOccurrenceTypesSection(dao = dao, currentUser = currentUser, scope = scope)
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
                    2 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                BackupRestoreSection(dao = dao, currentUser = currentUser, context = context, scope = scope)
                            }
                            item {
                                UnitsBackupSection(dao = dao, currentUser = currentUser, context = context, scope = scope)
                            }
                        }
                    }
                    3 -> {
                        AuditLogsSection(dao = dao, currentUser = currentUser, scope = scope)
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
            currentUser = currentUser,
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
                            AuditLogger.log(
                                dao = dao,
                                user = currentUser,
                                action = "Excluir Usuário",
                                category = "USUÁRIOS",
                                details = "Usuário '${user.username}' (${user.role}) foi excluído pelo administrador '${currentUser.username}'"
                            )
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
    currentUser: UserEntity,
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

                        AuditLogger.log(
                            dao = dao,
                            user = currentUser,
                            action = if (user == null) "Cadastro de Usuário" else "Edição de Usuário",
                            category = "USUÁRIOS",
                            details = "Usuário '$uName' ($uRole) foi ${if (user == null) "cadastrado" else "atualizado"} pelo admin '${currentUser.username}'"
                        )

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
fun BackupRestoreSection(dao: AppDao, currentUser: UserEntity, context: Context, scope: CoroutineScope) {
    var isLoading by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            isLoading = true
            scope.launch(Dispatchers.IO) {
                val success = ExcelHelper.importDatabase(context, dao, uri)
                if (success) {
                    AuditLogger.log(
                        dao = dao,
                        user = currentUser,
                        action = "Importação de Backup Completo",
                        category = "BACKUP",
                        details = "Backup completo do sistema restaurado a partir de planilha Excel"
                    )
                }
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
                if (success) {
                    AuditLogger.log(
                        dao = dao,
                        user = currentUser,
                        action = "Exportação de Backup Completo",
                        category = "BACKUP",
                        details = "Backup completo do sistema gerado em arquivo Excel"
                    )
                }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Backup & Restauração Completa do Sistema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Exporte ou restaure todo o banco de dados do condomínio em formato Excel. O backup inclui Usuários, Unidades, Notificações, Acordos, Processos, Manutenções, Ocorrências e Parâmetros.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "Tabelas incluídas no Backup Completo:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("• Cadastro de Usuários e Perfis", style = MaterialTheme.typography.bodySmall)
                    Text("• Cadastro de Unidades e Moradores", style = MaterialTheme.typography.bodySmall)
                    Text("• Notificações de Cobrança, Acordos e Processos Judiciais", style = MaterialTheme.typography.bodySmall)
                    Text("• Manutenções (Ordens de Serviço e Parcelas)", style = MaterialTheme.typography.bodySmall)
                    Text("• Ocorrências, Mensagens, Anexos e Logs", style = MaterialTheme.typography.bodySmall)
                    Text("• Tabelas de Configuração (Andares, Serviços, Status e Tipos)", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Processando backup do banco...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            exportLauncher.launch("CondSuites_Backup_Completo_$time.xlsx")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Exportar Backup Completo", style = MaterialTheme.typography.labelMedium)
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
                        Text("Importar Backup Completo", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun UnitsBackupSection(dao: AppDao, currentUser: UserEntity, context: Context, scope: CoroutineScope) {
    var isLoading by remember { mutableStateOf(false) }
    var importResultText by remember { mutableStateOf<String?>(null) }

    val exportUnitsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) {
            isLoading = true
            scope.launch(Dispatchers.IO) {
                val success = ExcelHelper.exportUnitsTable(context, dao, uri)
                if (success) {
                    AuditLogger.log(
                        dao = dao,
                        user = currentUser,
                        action = "Exportar Tabela de Unidades",
                        category = "UNIDADES",
                        details = "Planilha com dados das unidades exportada para Excel"
                    )
                }
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
                AuditLogger.log(
                    dao = dao,
                    user = currentUser,
                    action = "Importar Tabela de Unidades",
                    category = "UNIDADES",
                    details = "Importação de planilha de unidades concluída"
                )
                withContext(Dispatchers.Main) {
                    isLoading = false
                    importResultText = resultMsg
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Apartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Exportação & Importação Exclusiva das Unidades",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Exporte ou importe exclusivamente as informações das unidades e moradores em arquivo Excel. Essa operação afeta somente a tabela de cadastro de unidades.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(14.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "Campos de dados das Unidades:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("• Número do Apartamento e Andar", style = MaterialTheme.typography.bodySmall)
                    Text("• Nome do Proprietário / Morador", style = MaterialTheme.typography.bodySmall)
                    Text("• Telefone / Celular de Contato", style = MaterialTheme.typography.bodySmall)
                    Text("• E-mail do Condômino", style = MaterialTheme.typography.bodySmall)
                    Text("• Observações Gerais do Apto", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Processando planilha de unidades...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            Text("Exportar Unidades", style = MaterialTheme.typography.labelMedium)
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
                            Text("Baixar Modelo", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    OutlinedButton(
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
                        Text("Importar Apenas Unidades (Excel)", style = MaterialTheme.typography.labelMedium)
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

@Composable
fun ManageFloorsSection(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope) {
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
                                AuditLogger.log(
                                    dao = dao,
                                    user = currentUser,
                                    action = "Adicionar Andar",
                                    category = "PARÂMETROS",
                                    details = "Andar '$fl' adicionado aos parâmetros do sistema"
                                )
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
                                AuditLogger.log(
                                    dao = dao,
                                    user = currentUser,
                                    action = "Excluir Andar",
                                    category = "PARÂMETROS",
                                    details = "Andar '${fl.floor}' removido dos parâmetros"
                                )
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
fun ManageServiceDescriptionsSection(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope) {
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
                                val entity = ServiceDescriptionEntity(description = d)
                                dao.insertServiceDescription(entity)
                                FirestoreSyncManager.syncServiceDescription(entity)
                                AuditLogger.log(
                                    dao = dao,
                                    user = currentUser,
                                    action = "Adicionar Serviço",
                                    category = "PARÂMETROS",
                                    details = "Descrição de serviço '$d' cadastrada"
                                )
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
                                FirestoreSyncManager.syncServiceDescription(d, isDelete = true)
                                AuditLogger.log(
                                    dao = dao,
                                    user = currentUser,
                                    action = "Excluir Serviço",
                                    category = "PARÂMETROS",
                                    details = "Descrição de serviço '${d.description}' removida"
                                )
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
fun ManageProcessStatusesSection(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope) {
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
                                AuditLogger.log(
                                    dao = dao,
                                    user = currentUser,
                                    action = "Adicionar Status de Processo",
                                    category = "PARÂMETROS",
                                    details = "Status de processo '$st' adicionado"
                                )
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
                                AuditLogger.log(
                                    dao = dao,
                                    user = currentUser,
                                    action = "Excluir Status de Processo",
                                    category = "PARÂMETROS",
                                    details = "Status de processo '${st.status}' removido"
                                )
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
fun ManageOccurrenceTypesSection(dao: AppDao, currentUser: UserEntity, scope: CoroutineScope) {
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
                                val entity = OccurrenceTypeEntity(type = tp)
                                dao.insertOccurrenceType(entity)
                                FirestoreSyncManager.syncOccurrenceType(entity)
                                AuditLogger.log(
                                    dao = dao,
                                    user = currentUser,
                                    action = "Adicionar Tipo de Ocorrência",
                                    category = "PARÂMETROS",
                                    details = "Tipo de ocorrência '$tp' cadastrado"
                                )
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
                                FirestoreSyncManager.syncOccurrenceType(tp, isDelete = true)
                                AuditLogger.log(
                                    dao = dao,
                                    user = currentUser,
                                    action = "Excluir Tipo de Ocorrência",
                                    category = "PARÂMETROS",
                                    details = "Tipo de ocorrência '${tp.type}' removido"
                                )
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AuditLogsSection(
    dao: AppDao,
    currentUser: UserEntity,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val auditLogs by dao.getAllAuditLogs().collectAsState(initial = emptyList())
    var storageInfo by remember { mutableStateOf<AuditLogger.LogStorageInfo?>(null) }
    var isCalculatingStorage by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("TODOS") }

    var showClearAuditDialog by remember { mutableStateOf(false) }
    var showClearAllLogsDialog by remember { mutableStateOf(false) }
    var showManualLogDialog by remember { mutableStateOf(false) }

    val categories = listOf("TODOS", "USUÁRIOS", "SISTEMA", "FINANCEIRO", "OCORRÊNCIAS", "BACKUP", "PARÂMETROS", "SESSÃO", "UNIDADES")

    val refreshStorage = {
        isCalculatingStorage = true
        scope.launch {
            storageInfo = AuditLogger.calculateLogStorageInfo(context, dao)
            isCalculatingStorage = false
        }
    }

    LaunchedEffect(auditLogs) {
        refreshStorage()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = AuditLogger.exportAuditLogsToExcel(context, dao, uri)
                Toast.makeText(
                    context,
                    if (success) "Logs de auditoria exportados com sucesso!" else "Falha ao exportar logs.",
                    Toast.LENGTH_SHORT
                ).show()
                if (success) {
                    AuditLogger.log(
                        dao = dao,
                        user = currentUser,
                        action = "Exportação de Logs",
                        category = "BACKUP",
                        details = "Logs de auditoria exportados para Excel"
                    )
                }
            }
        }
    }

    val filteredLogs = remember(auditLogs, searchQuery, selectedCategory) {
        auditLogs.filter { log ->
            val matchesCategory = selectedCategory == "TODOS" || log.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    log.username.contains(searchQuery, ignoreCase = true) ||
                    log.action.contains(searchQuery, ignoreCase = true) ||
                    log.details.contains(searchQuery, ignoreCase = true) ||
                    log.category.contains(searchQuery, ignoreCase = true) ||
                    log.formattedDate.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Armazenamento & Espaço dos Logs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { refreshStorage() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recalcular espaço", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Monitore a quantidade de registros e o consumo de espaço em disco ocupado pelos dados de log e auditoria no banco de dados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(16.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Espaço Total Utilizado em Logs",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    storageInfo?.formattedTotalSize ?: "Calculando...",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "${storageInfo?.auditLogCount ?: 0} Regs Auditoria",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text("Auditoria", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(2.dp))
                                Text(storageInfo?.formattedAuditSize ?: "0 B", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("${storageInfo?.auditLogCount ?: 0} registros", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text("Notificações", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.height(2.dp))
                                Text(storageInfo?.formattedNotificationSize ?: "0 B", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("${storageInfo?.notificationLogCount ?: 0} registros", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text("Ocorrências", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                Spacer(Modifier.height(2.dp))
                                Text(storageInfo?.formattedOccurrenceSize ?: "0 B", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("${storageInfo?.occurrenceLogCount ?: 0} registros", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                exportLauncher.launch("Logs_Auditoria_CondSuites_$time.xlsx")
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Exportar Logs (Excel)", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { showManualLogDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Novo Log Manual", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { showClearAuditDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Limpar Logs Auditoria", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { showClearAllLogsDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Limpar Todos os Logs", style = MaterialTheme.typography.labelMedium)
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
                    Text(
                        "Histórico de Auditoria do Sistema",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Registros de atividades, alterações de segurança, gerenciamento de contas e operações de backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar log por ação, usuário, detalhes ou data...") },
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

                    Spacer(Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories.size) { idx ->
                            val cat = categories[idx]
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (filteredLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (searchQuery.isBlank() && selectedCategory == "TODOS")
                                        "Nenhum registro de auditoria armazenado."
                                    else
                                        "Nenhum log encontrado para os filtros selecionados.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                if (auditLogs.isEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                AuditLogger.log(
                                                    dao = dao,
                                                    user = currentUser,
                                                    action = "Inicialização de Logs",
                                                    category = "SISTEMA",
                                                    details = "Sistema de auditoria iniciado pelo administrador '${currentUser.username}'"
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Gerar Log de Inicialização")
                                    }
                                }
                            }
                        }
                    } else {
                        filteredLogs.forEach { logItem ->
                            AuditLogItemRow(
                                logItem = logItem,
                                onDelete = {
                                    scope.launch(Dispatchers.IO) {
                                        dao.deleteAuditLog(logItem.id)
                                        FirestoreSyncManager.syncAuditLog(logItem, isDelete = true)
                                    }
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    if (showClearAuditDialog) {
        AlertDialog(
            onDismissRequest = { showClearAuditDialog = false },
            title = { Text("Limpar Logs de Auditoria") },
            text = { Text("Tem certeza que deseja apagar todos os registros de auditoria? Esta ação irá liberar o espaço de armazenamento dos logs de auditoria no dispositivo.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAuditDialog = false
                        scope.launch(Dispatchers.IO) {
                            dao.clearAuditLogs()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Logs de auditoria apagados com sucesso.", Toast.LENGTH_SHORT).show()
                                refreshStorage()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Limpar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAuditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showClearAllLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllLogsDialog = false },
            title = { Text("Limpar TODOS os Logs do Sistema") },
            text = { Text("ATENÇÃO: Esta ação irá apagar completamente os logs de auditoria, histórico de notificações e logs de ocorrências. Deseja prosseguir para liberar o espaço total dos logs?") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAllLogsDialog = false
                        scope.launch(Dispatchers.IO) {
                            dao.clearAuditLogs()
                            dao.clearNotificationLogs()
                            dao.clearOccurrenceLogs()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Todos os logs foram apagados com sucesso.", Toast.LENGTH_SHORT).show()
                                refreshStorage()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Apagar Todos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllLogsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showManualLogDialog) {
        var actionInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("SISTEMA") }
        var detailsInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showManualLogDialog = false },
            title = { Text("Registrar Log de Auditoria Manual") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = actionInput,
                        onValueChange = { actionInput = it },
                        label = { Text("Título da Ação") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text("Categoria (ex: SISTEMA, MANUTENÇÃO, VISTORIA)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = detailsInput,
                        onValueChange = { detailsInput = it },
                        label = { Text("Detalhes / Observações") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (actionInput.isNotBlank()) {
                            showManualLogDialog = false
                            scope.launch {
                                AuditLogger.log(
                                    dao = dao,
                                    user = currentUser,
                                    action = actionInput.trim(),
                                    category = categoryInput.trim().uppercase(),
                                    details = detailsInput.trim()
                                )
                                Toast.makeText(context, "Log gravado com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Salvar Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualLogDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AuditLogItemRow(
    logItem: AuditLogEntity,
    onDelete: () -> Unit
) {
    val categoryColor = when (logItem.category.uppercase()) {
        "USUÁRIOS" -> Color(0xFF1976D2)
        "SISTEMA" -> Color(0xFF388E3C)
        "FINANCEIRO" -> Color(0xFFF57C00)
        "OCORRÊNCIAS" -> Color(0xFF7B1FA2)
        "BACKUP" -> Color(0xFF0097A7)
        "PARÂMETROS" -> Color(0xFFE64A19)
        "SESSÃO" -> Color(0xFF455A64)
        "UNIDADES" -> Color(0xFF5D4037)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = categoryColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            logItem.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        logItem.formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir Log",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                logItem.action,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (logItem.details.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    logItem.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(
                    "${logItem.username} (${logItem.userRole})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

