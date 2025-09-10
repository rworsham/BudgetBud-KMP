package com.budgetbud.kmp.ui.components

import androidx.compose.runtime.Composable
import com.budgetbud.kmp.models.AccountData

@Composable
expect fun AccountCardList(
    accounts: List<AccountData>,
    onViewHistory: (Int) -> Unit,
    onSetGoal: (Int) -> Unit
)