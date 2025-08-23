package com.budgetbud.kmp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun TransactionTable(
    budgetId: Int,
    familyView: Boolean,
    dataSource: TransactionTableDataSource,
    modifier: Modifier = Modifier
)