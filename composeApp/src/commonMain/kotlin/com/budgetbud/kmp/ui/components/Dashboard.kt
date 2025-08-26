package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.forms.*
import com.budgetbud.kmp.ui.components.profile.ProfileScreen
import com.budgetbud.kmp.ui.components.sections.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    initialSegment: String = "dashboard",
    apiClient: ApiClient,
    transactionTableDataSource: TransactionTableDataSource
) {
    var currentSegment by remember { mutableStateOf(initialSegment) }
    var drawerOpen by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("") }
    val familyView = remember { mutableStateOf(false) }

    fun openDialog(type: String) {
        dialogType = type
        showDialog = true
    }

    fun closeDialog() {
        dialogType = ""
        showDialog = false
    }

    Row(modifier = Modifier.fillMaxSize()) {
        MiniDrawer(
            expanded = drawerOpen,
            currentSegment = currentSegment,
            onSegmentSelected = { currentSegment = it },
            onToggle = { drawerOpen = !drawerOpen }
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier
                        .zIndex(1f)
                        .statusBarsPadding(),
                    navigationIcon = {
                        IconButton(onClick = { drawerOpen = !drawerOpen }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Toggle Drawer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "BudgetBud",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF1DB954), Color(0xFF006400))
                                    )
                                )
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Switch(
                                checked = familyView.value,
                                onCheckedChange = { familyView.value = it }
                            )
                            IconButton(onClick = { openDialog("Profile") }) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("New") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    onClick = { openDialog("FAB") }
                )
            }
        ) { innerPadding ->
            Box(modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
            ) {
                when (currentSegment) {
                    "dashboard" -> DashboardReports(familyView = familyView.value, apiClient = apiClient, transactionTableDataSource = transactionTableDataSource)
                    "budget" -> BudgetTransactionOverview(familyView = familyView.value, apiClient = apiClient)
                    "category" -> CategoryOverview(familyView = familyView.value, apiClient = apiClient)
                    "reports" -> ReportDashboard(familyView = familyView.value, apiClient = apiClient)
                    "transactions" -> TransactionTable(familyView = familyView.value, dataSource = transactionTableDataSource)
                    "accounts" -> AccountOverview(familyView = familyView.value, apiClient = apiClient)
                    "family" -> FamilyOverview(apiClient = apiClient)
                }

                if (showDialog) {
                    Dialog(onDismissRequest = { closeDialog() }) {
                        Surface {
                            when (dialogType) {
                                "Transaction" -> TransactionForm(
                                    apiClient = apiClient,
                                    onSuccess = { closeDialog() },
                                    familyView = familyView.value
                                )
                                "Budget" -> BudgetForm(
                                    apiClient = apiClient,
                                    onSuccess = { closeDialog() },
                                    familyView = familyView.value
                                )
                                "Category" -> CategoryForm(
                                    apiClient = apiClient,
                                    onSuccess = { closeDialog() },
                                    familyView = familyView.value
                                )
                                "Profile" -> ProfileScreen()
                                "FAB" -> FabDialog(
                                    onSelect = { type -> openDialog(type) },
                                    onDismiss = { closeDialog() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
