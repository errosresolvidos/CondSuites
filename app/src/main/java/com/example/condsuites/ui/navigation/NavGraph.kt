package com.example.condsuites.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val title: String, val icon: ImageVector, val route: String) {
    object Home : Screen("Início", Icons.Filled.Dashboard, "home")
    object Units : Screen("Unidades", Icons.Filled.Apartment, "units_parent")
    object UnitsRegistry : Screen("Cadastro", Icons.Filled.ContactPage, "units_registry")
    object Elevators : Screen("Elevadores", Icons.Filled.Elevator, "elevators")
    object Finance : Screen("Financeiro", Icons.Filled.AccountBalance, "finance")
    object Accounts : Screen("Contas", Icons.Filled.ReceiptLong, "accounts")
    object Agreements : Screen("Acordos", Icons.Filled.Handshake, "agreements")
    object Lawsuits : Screen("Ajuizados", Icons.Filled.Gavel, "lawsuits")
    object Notices : Screen("Mural de Avisos", Icons.AutoMirrored.Filled.Announcement, "notices")
    object Settings : Screen("Configurações", Icons.Filled.Settings, "settings")
    object DelinquencyHistory : Screen("Histórico de Inadimplência", Icons.Filled.History, "del_hist")
    object Contracts : Screen("Contratos", Icons.Filled.Description, "contracts")
    object Maintenance : Screen("Manutenção", Icons.Filled.Handyman, "maint_parent")
    object Reports : Screen("Relatórios", Icons.AutoMirrored.Filled.ListAlt, "reports_parent")
    object ReportsOccurrences : Screen("Relatório de Ocorrências", Icons.Filled.Analytics, "rep_occurrences")
    object ReportsAgreements : Screen("Relatório de Acordos", Icons.Filled.Handshake, "rep_agg")
    object ReportsLawsuits : Screen("Relatório de Ajuizados", Icons.Filled.Gavel, "rep_lawsuits")
    object ReportsElevOccurrences : Screen("Ocorrências Elevadores", Icons.Filled.Analytics, "rep_elev_occ")
    object ReportsOvertime : Screen("Relatório de Horas Extras", Icons.Filled.AccessTime, "rep_overtime")
    object Occurrences : Screen("Operacional", Icons.Default.Assignment, "occurrences_parent")
    object OccurrencesListNav : Screen("Ocorrências", Icons.Default.Assignment, "occurrences_list")
    object RegisterOccurrenceNav : Screen("Cadastrar Ocorrência", Icons.Default.Add, "register_occurrence_nav")
    object Overtime : Screen("Lançar Horas Extras", Icons.Filled.AccessTime, "overtime")
    object UserManagement : Screen("Gestão de Usuários", Icons.Filled.People, "users_mgmt")
}

data class NavigationItem(val screen: Screen, val subItems: List<NavigationItem> = emptyList())
