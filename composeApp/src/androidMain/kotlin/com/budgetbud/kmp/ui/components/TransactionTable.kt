package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.TransactionHistoryTableData
import com.budgetbud.kmp.utils.DateUtils
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch

@Composable
fun TransactionHistoryTable(
    familyView: Boolean,
    dataSource: TransactionTableDataSource,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var transactions by remember { mutableStateOf<List<TransactionHistoryTableData>>(emptyList()) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf(DateUtils.firstDayOfCurrentMonth()) }
    var endDate by remember { mutableStateOf(DateUtils.lastDayOfCurrentMonth()) }

    var editingRowId by remember { mutableStateOf<Int?>(null) }
    var editedTransaction by remember { mutableStateOf<TransactionHistoryTableData?>(null) }

    fun fetchTransactions() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                transactions = dataSource.fetchHistory(startDate.toString(), endDate.toString(), familyView)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load transactions"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect( startDate, endDate, familyView) {
        fetchTransactions()
    }

    fun handleSaveClick() {
        val updated = editedTransaction ?: return
        coroutineScope.launch {
            try {
                dataSource.update(updated)
                editingRowId = null
                fetchTransactions()
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

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        //DatePicker

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn {
                items(transactions, key = { it.id }) { tx ->
                    val isEditing = tx.id == editingRowId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (isEditing) {
                                var description by remember { mutableStateOf(tx.description ?: "") }
                                var amount by remember { mutableStateOf(tx.amount.toString()) }

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = {
                                        description = it
                                        editedTransaction = tx.copy(description = it)
                                    },
                                    label = { Text("Description") }
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = {
                                        amount = it
                                        editedTransaction = tx.copy(
                                            amount = it.toDoubleOrNull() ?: tx.amount
                                        )
                                    },
                                    label = { Text("Amount") }
                                )
                                Spacer(Modifier.height(8.dp))
                                Row {
                                    IconButton(onClick = { handleSaveClick() }) {
                                        Icon(Icons.Default.Check, contentDescription = "Save")
                                    }
                                    IconButton(onClick = {
                                        editingRowId = null
                                        editedTransaction = null
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                                    }
                                }
                            } else {
                                Text("ID: ${tx.id} | Date: ${tx.date} | Amount: $${tx.amount}")
                                Text("Type: ${tx.transaction_type} | Desc: ${tx.description}")
                                Text("Category: ${tx.category} | Budget: ${tx.budget}")
                                Text("Account: ${tx.account} | Recurring: ${tx.is_recurring}")
                                Row {
                                    IconButton(onClick = {
                                        editingRowId = tx.id
                                        editedTransaction = tx
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { handleDeleteClick(tx.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
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
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        errorMessage?.let {
            AlertHandler(alertMessage = it)
        }
    }
}
