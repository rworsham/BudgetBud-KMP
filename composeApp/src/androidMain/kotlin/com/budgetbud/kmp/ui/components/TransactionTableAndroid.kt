package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.TransactionHistoryTableData
import com.budgetbud.kmp.ui.components.forms.DateRangeFilterForm
import com.budgetbud.kmp.utils.DateUtils
import kotlinx.coroutines.launch

@Composable
actual fun TransactionTable(
    familyView: Boolean,
    apiClient: ApiClient,
    modifier: Modifier,
    maxHeight: Dp?,
    startDate: String?,
    endDate: String?
) {
    val coroutineScope = rememberCoroutineScope()
    val dataSource = remember { TransactionTableDataSource(apiClient = apiClient) }

    fun String?.toLocalDateOrDefault(default: LocalDate): LocalDate =
        this?.let { LocalDate.parse(it) } ?: default

    var transactions by remember { mutableStateOf<List<TransactionHistoryTableData>>(emptyList()) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var currentStartDate by remember { mutableStateOf(startDate.toLocalDateOrDefault(DateUtils.firstDayOfCurrentMonth())) }
    var currentEndDate by remember { mutableStateOf(endDate.toLocalDateOrDefault(DateUtils.lastDayOfCurrentMonth())) }

    var editingRowId by remember { mutableStateOf<Int?>(null) }
    var editedTransaction by remember { mutableStateOf<TransactionHistoryTableData?>(null) }

    fun fetchTransactions() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                transactions = dataSource.fetchHistory(
                    currentStartDate.toString(),
                    currentEndDate.toString(),
                    familyView
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load transactions"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(currentStartDate, currentEndDate, familyView) {
        fetchTransactions()
    }

    fun handleSaveClick() {
        val updated = editedTransaction ?: return
        coroutineScope.launch {
            try {
                dataSource.update(updated)
                editingRowId = null
                fetchTransactions()
                showSuccessDialog = true
            } catch (e: Exception) {
                errorMessage = "Failed to update transaction"
            }
        }
    }

    fun handleDeleteClick(id: Int) {
        coroutineScope.launch {
            try {
                dataSource.delete(id)
                fetchTransactions()
            } catch (e: Exception) {
                errorMessage = "Failed to delete transaction"
            }
        }
    }

    val columnModifier = if (maxHeight != null) {
        Modifier.heightIn(max = maxHeight)
    } else {
        Modifier.fillMaxSize()
    }

    Column(
        modifier = modifier.then(columnModifier).padding(16.dp)
    ) {
        if (startDate == null && endDate == null) {
            DateRangeFilterForm(
                startDate = currentStartDate,
                endDate = currentEndDate,
                onStartDateChange = { newStartDate ->
                    currentStartDate = newStartDate
                    fetchTransactions()
                },
                onEndDateChange = { newEndDate ->
                    currentEndDate = newEndDate
                    fetchTransactions()
                },
                modifier = Modifier
            )
            Spacer(Modifier.height(16.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                thickness = 2.dp
            )

            Spacer(Modifier.height(16.dp))
        }

        when {
            errorMessage != null -> Text(
                "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )

            transactions.isEmpty() -> ChartDataError(message = "No transactions found for the selected range.")

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(transactions, key = { it.id }) { tx ->
                        val isEditing = tx.id == editingRowId

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            shadowElevation = 2.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tx.date,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                    )

                                    Text(
                                        text = if (tx.transaction_type == "income")
                                            "+$${tx.amount}"
                                        else
                                            "-$${tx.amount}",
                                        color = if (tx.transaction_type == "income")
                                            Color(0xFF1DB954)
                                        else
                                            MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(tx.category, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text("Budget", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(tx.budget, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Spacer(Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Account", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(tx.account, fontSize = 13.sp)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text("Recurring", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(
                                            if (tx.is_recurring) "Yes" else "No",
                                            fontSize = 13.sp,
                                            color = if (tx.is_recurring) Color(0xFF1DB954)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                if (isEditing) {
                                    var newDescription by remember { mutableStateOf(tx.description) }
                                    var newAmount by remember { mutableStateOf(tx.amount) }

                                    OutlinedTextField(
                                        value = newDescription,
                                        onValueChange = {
                                            newDescription = it
                                            editedTransaction = tx.copy(description = it)
                                        },
                                        label = { Text("Description") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = newAmount,
                                        onValueChange = {
                                            newAmount = it
                                            editedTransaction = tx.copy(amount = it)
                                        },
                                        label = { Text("Amount") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(onClick = { handleSaveClick() }) {
                                            Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            editingRowId = null
                                            editedTransaction = null
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = tx.description.ifEmpty { "(No description)" },
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(onClick = {
                                            editingRowId = tx.id
                                            editedTransaction = tx
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                                        }
                                        IconButton(onClick = { handleDeleteClick(tx.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showSuccessDialog) {
            SuccessDialog(onDismiss = { showSuccessDialog = false })
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator(modifier = Modifier
                    .padding(16.dp)
                    .size(144.dp))
            }
        }

        errorMessage?.let { AlertHandler(alertMessage = it) }
    }
}
