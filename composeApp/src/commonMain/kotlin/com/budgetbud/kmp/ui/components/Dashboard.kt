package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.forms.*
import com.budgetbud.kmp.ui.components.profile.ProfileScreen
import com.budgetbud.kmp.ui.components.sections.*
import kotlinx.coroutines.launch

@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    initialSegment: String = "dashboard",
    apiClient: ApiClient
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var currentSegment by remember { mutableStateOf(initialSegment) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("") }

    val familyView = remember { mutableStateOf(false) }

    fun openDialog(type: String) {
        dialogType = type
        showDialog = true
    }

    fun closeDialog() {
        showDialog = false
        dialogType = ""
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawer(
                currentSegment = currentSegment,
                onSegmentSelected = { segment ->
                    currentSegment = segment
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "BudgetBud",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
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
                                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
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
            Box(modifier = modifier.padding(innerPadding).fillMaxSize()) {
                when (currentSegment) {
                    "dashboard" -> DashboardReports(familyView = familyView.value, apiClient = apiClient)
                    "budget" -> BudgetTransactionOverview(familyView = familyView.value, apiClient = apiClient)
                    "category" -> CategoryOverview(familyView = familyView.value, apiClient = apiClient)
                    "reports" -> ReportDashboard(familyView = familyView.value, apiClient = apiClient)
                    "transactions" -> TransactionTableView(familyView = familyView.value, apiClient = apiClient)
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
