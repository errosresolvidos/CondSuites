package com.example.condsuites.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(dao: AppDao, onLoginSuccess: (UserEntity) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberUser by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    val rememberedUsers by UserPreferences.getRememberedUsernames(context).collectAsState(initial = emptySet())

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Filled.Apartment, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Text("CondSuites", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Administração de Condomínio", style = MaterialTheme.typography.titleMedium)
                
                if (rememberedUsers.isNotEmpty()) {
                    Text("Usuários recentes:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(rememberedUsers.toList()) { user ->
                            InputChip(
                                selected = username == user,
                                onClick = { username = user },
                                label = { Text(user) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remover",
                                        modifier = Modifier.size(16.dp).clickable {
                                            scope.launch { UserPreferences.removeUsername(context, user) }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMessage = null },
                    label = { Text("Usuário") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Senha") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                            )
                        }
                    }
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = rememberUser, onCheckedChange = { rememberUser = it })
                    Text("Lembrar usuário", style = MaterialTheme.typography.bodySmall)
                }
                
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val user = withContext(Dispatchers.IO) {
                                    dao.getUserByUsername(username)
                                }
                                if (user != null && user.password == password) {
                                    if (rememberUser) {
                                        try {
                                            UserPreferences.saveUsername(context, username)
                                        } catch (_: Exception) {}
                                    }
                                    onLoginSuccess(user)
                                } else {
                                    errorMessage = "Usuário ou senha incorretos"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Erro ao fazer login: ${e.localizedMessage}"
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ENTRAR")
                }
            }
        }
    }
}
