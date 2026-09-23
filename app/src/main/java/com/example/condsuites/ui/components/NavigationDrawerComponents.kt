package com.example.condsuites.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.ui.navigation.NavigationItem
import com.example.condsuites.ui.navigation.Screen

@Composable
fun RecursiveNavItem(
    item: NavigationItem,
    currentScreen: Screen,
    expandedItems: Set<String>,
    level: Int = 0,
    onExpandToggle: (String) -> Unit,
    onScreenSelect: (Screen) -> Unit
) {
    val isExpanded = expandedItems.contains(item.screen.route)
    val hasSubItems = item.subItems.isNotEmpty()
    val isSelected = currentScreen.route == item.screen.route

    Column {
        NavigationDrawerItem(
            label = { Text(item.screen.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            selected = isSelected && !hasSubItems,
            onClick = {
                if (hasSubItems) {
                    onExpandToggle(item.screen.route)
                } else {
                    onScreenSelect(item.screen)
                }
            },
            icon = { Icon(item.screen.icon, null) },
            badge = {
                if (hasSubItems) {
                    Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(start = (level * 12).dp).padding(horizontal = 4.dp, vertical = 2.dp)
        )

        if (hasSubItems && isExpanded) {
            item.subItems.forEach { subItem ->
                RecursiveNavItem(
                    item = subItem,
                    currentScreen = currentScreen,
                    expandedItems = expandedItems,
                    level = level + 1,
                    onExpandToggle = onExpandToggle,
                    onScreenSelect = onScreenSelect
                )
            }
        }
    }
}

@Composable
fun QuickSwitchUserDialog(
    dao: AppDao,
    onDismiss: () -> Unit,
    onUserSelected: (UserEntity) -> Unit
) {
    val users by dao.getAllUsersFlow().collectAsState(initial = emptyList())
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Trocar Usuário") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecione um usuário para entrar sem fechar o app.", style = MaterialTheme.typography.bodySmall)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(users) { user ->
                        FilterChip(
                            selected = selectedUser == user,
                            onClick = { selectedUser = user; error = null },
                            label = { Text(user.username) },
                            leadingIcon = if (selectedUser == user) { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null
                        )
                    }
                }

                if (selectedUser != null) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = { Text("Senha para ${selectedUser!!.username}") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (error != null) {
                    Text(error!!, color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedUser != null) {
                        if (password == selectedUser!!.password) {
                            onUserSelected(selectedUser!!)
                        } else {
                            error = "Senha incorreta!"
                        }
                    }
                },
                enabled = selectedUser != null && password.isNotBlank()
            ) {
                Text("Entrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
