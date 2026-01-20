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

    val generatePdf = rememberPdfGenerator(
        fileName = "Transactions_${currentStartDate}_${currentEndDate}.pdf",
        onResult = { success, message ->
            if (success) showSuccessDialog = true else errorMessage = message
        },
        onDraw = {}
    )

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

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            if (startDate == null && endDate == null) {
                DateRangeFilterForm(
                    startDate = currentStartDate,
                    endDate = currentEndDate,
                    onStartDateChange = { currentStartDate = it; fetchTransactions() },
                    onEndDateChange = { currentEndDate = it; fetchTransactions() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 2.dp)
                Spacer(Modifier.height(16.dp))
            }

            when {
                errorMessage != null -> Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                transactions.isEmpty() && !isLoading -> ChartDataError(message = "No transactions found.")
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(tx.date, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = if (tx.transaction_type == "income") "+$${tx.amount}" else "-$${tx.amount}",
                                            color = if (tx.transaction_type == "income") Color(0xFF1DB954) else MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Row(Modifier.fillMaxWidth()) {
                                        Column(Modifier.weight(1f)) {
                                            Text("Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            Text(tx.category, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Column(Modifier.weight(1f)) {
                                            Text("Budget", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            Text(tx.budget, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    if (isEditing) {
                                        var newDescription by remember { mutableStateOf(tx.description) }
                                        var newAmount by remember { mutableStateOf(tx.amount) }

                                        OutlinedTextField(
                                            value = newDescription,
                                            onValueChange = { newDescription = it; editedTransaction = tx.copy(description = it) },
                                            label = { Text("Description") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = newAmount,
                                            onValueChange = { newAmount = it; editedTransaction = tx.copy(amount = it) },
                                            label = { Text("Amount") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                            IconButton(onClick = { handleSaveClick() }) { Icon(Icons.Default.Check, "Save", tint = MaterialTheme.colorScheme.primary) }
                                            IconButton(onClick = { editingRowId = null }) { Icon(Icons.Default.Close, "Cancel", tint = MaterialTheme.colorScheme.error) }
                                        }
                                    } else {
                                        Text(tx.description.ifEmpty { "(No description)" }, fontSize = 13.sp)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                            IconButton(onClick = { editingRowId = tx.id; editedTransaction = tx }) { Icon(Icons.Default.Edit, "Edit") }
                                            IconButton(onClick = { handleDeleteClick(tx.id) }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { generatePdf() },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            enabled = transactions.isNotEmpty() && !isLoading
        ) {
            Text("Download as PDF")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (showSuccessDialog) SuccessDialog(onDismiss = { showSuccessDialog = false })
        errorMessage?.let { AlertHandler(alertMessage = it) }
    }
}