package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.models.BudgetData

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetCardList(
    budgets: List<BudgetData>,
    onViewHistory: (Int) -> Unit,
    onSetBudgetGoal: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        budgets.forEach { budget ->
            OutlinedCard(
                modifier = Modifier.widthIn(min = 280.dp, max = 350.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = budget.name,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = Color(0xFF1DB954)
                    )

                    val amount = budget.total_amount.toDoubleOrNull() ?: 0.0
                    Text(
                        text = "Balance: $${"%,.2f".format(amount)}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        Button(
                            onClick = { onViewHistory(budget.id) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("History", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = { onSetBudgetGoal(budget.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Set Goal", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}