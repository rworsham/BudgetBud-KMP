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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                headers { tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") } }
            }
            val stats: HttpResponse = apiClient.client.get("https://api.budgetingbud.com/api/profile/stats/") {
                headers { tokens?.let { append(HttpHeaders.Authorization, "Bearer ${it.accessToken}") } }
            }
            userDetails = details.body()
            userStats = stats.body()
        } catch (e: Exception) {
            error = "Failed to fetch profile: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Surface(
                modifier = Modifier
                    .widthIn(max = 1200.dp)
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 0.dp,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .wrapContentHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Avatar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
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

                    HorizontalDivider(
                        thickness = 2.dp,
                        color = Color(0xFF1DB954)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "User Stats",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            HorizontalDivider(Modifier.padding(bottom = 16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(Modifier.weight(1f), "Transaction Count", userStats?.total_transactions)
                                StatItem(Modifier.weight(1f), "Date Joined", userStats?.joined_date)
                                StatItem(Modifier.weight(1f), "Goals Met", userStats?.savings_goals_met)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    modifier = Modifier.weight(1f),
                                    label = "Income Total",
                                    value = "$${userStats?.net_income ?: "0.00"}",
                                    valueColor = Color(0xFF1DB954)
                                )
                                StatItem(
                                    modifier = Modifier.weight(1f),
                                    label = "Expense Total",
                                    value = "$${userStats?.net_expense ?: "0.00"}",
                                    valueColor = MaterialTheme.colorScheme.error
                                )
                                StatItem(
                                    modifier = Modifier.weight(1f),
                                    label = "Lifetime Balance",
                                    value = "$${userStats?.net_balance ?: "0.00"}",
                                    valueColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: Any?,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = value?.toString() ?: "-",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = valueColor,
            textAlign = TextAlign.Center
        )
    }
}