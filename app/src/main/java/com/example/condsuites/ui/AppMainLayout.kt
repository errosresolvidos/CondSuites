package com.example.condsuites.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.unit.dp
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.ui.components.*
import com.example.condsuites.ui.navigation.NavigationItem
import com.example.condsuites.ui.navigation.Screen
import com.example.condsuites.ui.screens.*
import com.example.condsuites.ui.screens.reports.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainLayout(
    dao: AppDao,
    currentUser: UserEntity,
    onLogout: () -> Unit,
    onUserSwitched: (UserEntity) -> Unit,
    context: Context,
    scope: CoroutineScope
) {
    val canAccessFinance = currentUser.role != "Zelador" && currentUser.role != "Porteiro"
    val canAccessMaintenance = true

    val navItems = remember(currentUser.role) {
        val list = mutableListOf(
            NavigationItem(Screen.Home),
            NavigationItem(Screen.Units, listOf(
                NavigationItem(Screen.UnitsRegistry)
            )),
            NavigationItem(Screen.Occurrences, listOf(
                NavigationItem(Screen.OccurrencesListNav),
                NavigationItem(Screen.Overtime)
            ))
        )

        if (canAccessFinance) {
            list.add(
                NavigationItem(Screen.Finance, listOf(
                    NavigationItem(Screen.Accounts),
                    NavigationItem(Screen.Agreements),
                    NavigationItem(Screen.Lawsuits),
                    NavigationItem(Screen.DelinquencyHistory),
                    NavigationItem(Screen.Contracts)
                ))
            )
        }

        val reportSubItems = mutableListOf<NavigationItem>()
        if (canAccessFinance) {
            reportSubItems.add(NavigationItem(Screen.ReportsAgreements))
            reportSubItems.add(NavigationItem(Screen.ReportsLawsuits))
        }
        if (canAccessMaintenance) {
            reportSubItems.add(NavigationItem(Screen.ReportsElevOccurrences))
        }
        reportSubItems.add(NavigationItem(Screen.ReportsOvertime))

        list.add(NavigationItem(Screen.Reports, reportSubItems))
        list.add(NavigationItem(Screen.Notices))
        list
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var expandedItems by remember { mutableStateOf(setOf<String>()) }

    val occurrencesWithMessages by dao.getAllOccurrences().collectAsState(initial = emptyList())
    val agreements by dao.getActiveAgreements().collectAsState(initial = emptyList())
    val delinquents by dao.getDelinquents().collectAsState(initial = emptyList())
    val lawsuits by dao.getLawsuitsWithProgress().collectAsState(initial = emptyList())

    val unreadOccurrencesCount = remember(occurrencesWithMessages, currentUser) {
        occurrencesWithMessages.count { item ->
            item.messages.any { !it.message.isRead && it.message.senderUsername != currentUser.username }
        }
    }

    var showSwitchUserDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    if (showSwitchUserDialog) {
        QuickSwitchUserDialog(
            dao = dao,
            onDismiss = { showSwitchUserDialog = false },
            onUserSelected = { newUser ->
                onUserSwitched(newUser)
                showSwitchUserDialog = false
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Sair do Aplicativo") },
            text = { Text("Deseja realmente sair da sua conta?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sair")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Apartment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "CondSuites",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Gestão Condominial",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        currentUser.username,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            currentUser.role,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(navItems) { item ->
                        RecursiveNavItem(
                            item = item,
                            currentScreen = currentScreen,
                            expandedItems = expandedItems,
                            level = 0,
                            onExpandToggle = { route ->
                                expandedItems = if (expandedItems.contains(route)) expandedItems - route else expandedItems + route
                            },
                            onScreenSelect = { screen ->
                                currentScreen = screen
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }

                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showExitDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sair do Aplicativo",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Sair do Aplicativo",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }

                    IconButton(
                        onClick = {
                            currentScreen = Screen.Settings
                            scope.launch { drawerState.close() }
                        }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (currentScreen is Screen.Settings) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Configurações",
                                    tint = if (currentScreen is Screen.Settings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = currentScreen.title,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { currentScreen = Screen.Occurrences }) {
                            BadgedBox(
                                badge = {
                                    if (unreadOccurrencesCount > 0) {
                                        Badge { Text("$unreadOccurrencesCount") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "Mensagens")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    Screen.Home -> HomeScreen(dao, currentUser, scope, onNavigate = { screen -> currentScreen = screen })
                    Screen.Agreements -> AgreementsManagementScreen(agreements, delinquents, lawsuits, dao, scope, currentUser)
                    Screen.Lawsuits -> LawsuitsManagementScreen(lawsuits, agreements, dao, scope)
                    Screen.Occurrences, Screen.OccurrencesListNav -> OccurrencesScreen(dao, currentUser, scope, isCompact = false)
                    Screen.Units, Screen.UnitsRegistry -> UnitsRegistryScreen(dao, context, onNavigate = { screen -> currentScreen = screen })
                    Screen.Overtime -> OvertimeManagementScreen(dao, currentUser, scope)
                    Screen.Elevators -> ElevatorsScreen(dao, currentUser, scope)
                    Screen.Contracts -> ContractsScreen(dao, currentUser, scope)
                    Screen.ReportsOccurrences -> ReportsOccurrencesScreen(dao, context)
                    Screen.ReportsAgreements -> ReportsAgreementsScreen(dao, context)
                    Screen.ReportsLawsuits -> ReportsLawsuitsScreen(dao, context)
                    Screen.ReportsElevOccurrences -> ReportsElevatorOccurrencesScreen(dao, context)
                    Screen.ReportsOvertime -> ReportsOvertimeScreen(dao, context)
                    Screen.DelinquencyHistory -> DelinquencyHistoryScreen(dao, currentUser)
                    Screen.Finance, Screen.Accounts -> FinanceScreen(dao, currentUser)
                    Screen.Settings -> SettingsScreen(dao, currentUser, scope)
                    else -> HomeScreen(dao, currentUser, scope, onNavigate = { screen -> currentScreen = screen })
                }
            }
        }
    }
}
