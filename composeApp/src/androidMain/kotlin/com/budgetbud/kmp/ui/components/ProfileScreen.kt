package com.budgetbud.kmp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.budgetbud.kmp.auth.ApiClient
import com.budgetbud.kmp.models.UserStatsData
import com.budgetbud.kmp.models.UserDetailsData
import io.ktor.client.call.*
import io.ktor.client.request.*

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
            userDetails = apiClient.client.get("/user/").body()
            userStats = apiClient.client.get("/profile/stats/").body()
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter("https://via.placeholder.com/80"),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Column {
                Text(
                    text = userDetails?.username ?: "N/A",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = userDetails?.email ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        Divider()

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("User Stats", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Transactions", userStats?.totalTransactions)
                    StatItem("Joined", userStats?.joinedDate)
                    StatItem("Goals Met", userStats?.savingsGoalsMet)
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Income", "$${userStats?.netIncome}")
                    StatItem("Expense", "$${userStats?.netExpense}")
                    StatItem("Balance", "$${userStats?.netBalance}")
                }
            }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun StatItem(label: String, value: Any?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value?.toString() ?: "-", style = MaterialTheme.typography.titleMedium)
    }
}
