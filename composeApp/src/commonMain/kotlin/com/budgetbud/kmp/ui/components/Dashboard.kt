package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.ui.components.forms.BudgetForm
import com.budgetbud.kmp.ui.components.forms.TransactionForm
import com.budgetbud.kmp.ui.components.forms.CategoryForm
import com.budgetbud.kmp.ui.components.profile.ProfileScreen
import com.budgetbud.kmp.ui.components.sections.*
import kotlinx.coroutines.launch

@Composable
fun Dashboard(
    modifier: Modifier = Modifier,
    initialSegment: String = "dashboard",
    apiClient: ApiClient
) {
    val scaffoldState = rememberScaffoldState()
    val drawerOpen = remember { mutableStateOf(true) }
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

    Scaffold(
        scaffoldState = scaffoldState,
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
                        coroutineScope.launch { scaffoldState.drawerState.open() }
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
        drawerContent = {
            NavigationDrawer(
                currentSegment = currentSegment,
                onSegmentSelected = { segment ->
                    currentSegment = segment
                    coroutineScope.launch { scaffoldState.drawerState.close() }
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
                "dashboard" -> DashboardReports(familyView = familyView.value)
                "budget" -> BudgetTransactionOverview(familyView = familyView.value)
                "category" -> CategoryOverview(familyView = familyView.value)
                "reports" -> ReportDashboard(familyView = familyView.value)
                "transactions" -> TransactionTableView(familyView = familyView.value)
                "accounts" -> AccountOverview(familyView = familyView.value)
                "family" -> FamilyOverview()
            }

            if (showDialog) {
                when (dialogType) {
                    "Transaction" -> Dialog(onDismissRequest = { closeDialog() }) {
                        Surface {
                            TransactionForm(onSuccess = { closeDialog() }, familyView = familyView.value)
                        }
                    }
                    "Budget" -> Dialog(onDismissRequest = { closeDialog() }) {
                        Surface {
                            BudgetForm(
                                apiClient = apiClient,
                                onSuccess = { closeDialog() },
                                familyView = familyView.value
                            )
                        }
                    }
                    "Category" -> Dialog(onDismissRequest = { closeDialog() }) {
                        Surface {
                            CategoryForm(onSuccess = { closeDialog() }, familyView = familyView.value)
                        }
                    }
                    "Profile" -> Dialog(onDismissRequest = { closeDialog() }) {
                        Surface {
                            ProfileScreen()
                        }
                    }
                    "FAB" -> FabDialog(
                        onSelect = { type -> openDialog(type) },
                        onDismiss = { closeDialog() }
                    )
                }
            }
        }
    }
}
