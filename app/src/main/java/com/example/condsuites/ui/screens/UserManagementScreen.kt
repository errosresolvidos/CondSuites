package com.example.condsuites.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.service.FirestoreSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    dao: AppDao,
    currentUser: UserEntity,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val users by dao.getAllUsersFlow().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    val roles = listOf("ADMIN", "Síndico", "Conselheiro Fiscal", "Zelador", "Porteiro")

    val filteredUsers = remember(users, searchQuery) {
        users.filter {
            it.username.contains(searchQuery, ignoreCase = true) ||
                    it.role.contains(searchQuery, ignoreCase = true)
        }
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
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Gestão de Usuários",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Gerencie os acessos do condomínio (${users.size} usuário(s) cadastrado(s))",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar usuário por nome ou função") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum usuário encontrado.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredUsers) { user ->
                        UserCard(
                            user = user,
                            onEdit = { editingUser = user },
                            onDelete = { userToDelete = user }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddUserDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Cadastrar Usuário")
        }
    }

    if (showAddUserDialog) {
        UserEditDialog(
            user = null,
            roles = roles,
            onDismiss = { showAddUserDialog = false },
            onConfirm = { username, password, role ->
                if (users.any { it.username.equals(username, ignoreCase = true) }) {
                    Toast.makeText(context, "Usuário \"$username\" já cadastrado!", Toast.LENGTH_SHORT).show()
                } else {
                    scope.launch(Dispatchers.IO) {
                        val newUser = UserEntity(
                            username = username,
                            password = password,
                            role = role
                        )
                        dao.insertUser(newUser)
                        FirestoreSyncManager.syncUser(newUser)
                    }
                    showAddUserDialog = false
                    Toast.makeText(context, "Usuário adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (editingUser != null) {
        UserEditDialog(
            user = editingUser,
            roles = roles,
            onDismiss = { editingUser = null },
            onConfirm = { username, password, role ->
                scope.launch(Dispatchers.IO) {
                    val updatedUser = editingUser!!.copy(
                        username = username,
                        password = if (password.isNotBlank()) password else editingUser!!.password,
                        role = role
                    )
                    dao.insertUser(updatedUser)
                    FirestoreSyncManager.syncUser(updatedUser)
                }
                editingUser = null
                Toast.makeText(context, "Usuário atualizado!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Excluir Usuário") },
            text = { Text("Deseja realmente remover o usuário \"${userToDelete?.username}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        val u = userToDelete!!
                        scope.launch(Dispatchers.IO) {
                            dao.deleteUser(u.id)
                            FirestoreSyncManager.syncUser(u, isDelete = true)
                        }
                        userToDelete = null
                        Toast.makeText(context, "Usuário excluído.", Toast.LENGTH_SHORT).show()
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
fun UserCard(
    user: UserEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
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
                    Text(
                        user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = when (user.role) {
                            "ADMIN" -> Color(0xFFFFEBEE)
                            "Síndico" -> Color(0xFFE8F5E9)
                            "Conselheiro Fiscal" -> Color(0xFFE3F2FD)
                            "Zelador" -> Color(0xFFFFF3E0)
                            else -> Color(0xFFF5F5F5)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            user.role,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (user.role) {
                                "ADMIN" -> Color(0xFFC62828)
                                "Síndico" -> Color(0xFF2E7D32)
                                "Conselheiro Fiscal" -> Color(0xFF1565C0)
                                "Zelador" -> Color(0xFFEF6C00)
                                else -> Color.DarkGray
                            }
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Usuário", tint = MaterialTheme.colorScheme.primary)
                }

                if (!user.username.equals("admin", ignoreCase = true)) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Usuário", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditDialog(
    user: UserEntity?,
    roles: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var username by remember { mutableStateOf(user?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(user?.role ?: roles.first()) }
    var showPassword by remember { mutableStateOf(false) }
    var expandedRoleDropdown by remember { mutableStateOf(false) }

    val isEditing = user != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Usuário" else "Cadastrar Novo Usuário") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nome de Usuário") },
                    singleLine = true,
                    enabled = !isEditing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isEditing) "Nova Senha (deixe em branco p/ manter)" else "Senha") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedRoleDropdown,
                    onExpandedChange = { expandedRoleDropdown = !expandedRoleDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Função / Perfil") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoleDropdown) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRoleDropdown,
                        onDismissRequest = { expandedRoleDropdown = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    selectedRole = role
                                    expandedRoleDropdown = false
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
                    if (username.isNotBlank() && (isEditing || password.isNotBlank())) {
                        onConfirm(username.trim(), password.trim(), selectedRole)
                    }
                },
                enabled = username.isNotBlank() && (isEditing || password.isNotBlank())
            ) {
                Text(if (isEditing) "Salvar" else "Cadastrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
