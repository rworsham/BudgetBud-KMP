package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.UserStatsData
import com.budgetbud.kmp.models.UserDetailsData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders

@Composable
actual fun ProfileScreen(
    modifier: Modifier,
    apiClient: ApiClient
) {
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var userStats by remember { mutableStateOf<UserStatsData?>(null) }
    var userDetails by remember { mutableStateOf<UserDetailsData?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val tokens = apiClient.getTokens()
            val details: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/user/") {
                headers {
                    tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") }
                }
            }
            val stats: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/profile/stats/") {
                headers {
                    tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") }
                }
            }
            userDetails = details.body()
            userStats = stats.body()
        } catch (e: Exception) {
            error = "Failed to fetch profile: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User Avatar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                )
                Column {
                    Text(
                        text = userDetails?.username ?: "N/A",
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        text = userDetails?.email ?: "N/A",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(Modifier.weight(1f)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("General Stats", style = MaterialTheme.typography.titleLarge)
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        StatRow("Transactions", userStats?.total_transactions)
                        StatRow("Joined Date", userStats?.joined_date)
                        StatRow("Goals Met", userStats?.savings_goals_met)
                    }
                }

                Card(
                    Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Financial Overview", style = MaterialTheme.typography.titleLarge)
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        StatRow("Net Income", "$${userStats?.net_income}")
                        StatRow("Net Expense", "$${userStats?.net_expense}")
                        StatRow("Net Balance", "$${userStats?.net_balance}")
                    }
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: Any?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            value?.toString() ?: "-",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}