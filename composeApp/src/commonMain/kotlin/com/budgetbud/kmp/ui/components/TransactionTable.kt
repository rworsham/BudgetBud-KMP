package com.budgetbud.kmp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.budgetbud.kmp.auth.ApiClient

@Composable
expect fun TransactionTable(
    familyView: Boolean,
    apiClient: ApiClient,
    modifier: Modifier = Modifier
)