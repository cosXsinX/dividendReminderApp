package com.example.mydividendreminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mydividendreminder.R
import com.example.mydividendreminder.data.entity.ProductWithDividends
import com.example.mydividendreminder.data.entity.Dividend
import com.example.mydividendreminder.ui.theme.DefaultMainAppBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Help

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    navigationHelper: com.example.mydividendreminder.util.NavigationHelper,
    productsWithDividends: List<ProductWithDividends> = emptyList(),
    onDeleteDividend: (Dividend) -> Unit = {},
    onSyncClick: (() -> Unit)? = null,
    isSyncing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DefaultMainAppBar(
            navigationHelper = navigationHelper,
            productsWithDividends = productsWithDividends,
            onSyncClick = onSyncClick,
            isSyncing = isSyncing
        )

        
        LazyColumn(
            modifier = Modifier.weight(1f),
        ) {
            // Upcoming Dividends Section
            item {
                UpcomingDividendsSection(
                    productsWithDividends = productsWithDividends,
                    onDeleteDividend = onDeleteDividend
                )
            }
            
            // Welcome message card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome to Dividend Reminder",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "Manage your dividend investments and get notified about upcoming payments",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingDividendsSection(
    productsWithDividends: List<ProductWithDividends>,
    onDeleteDividend: (Dividend) -> Unit,
    modifier: Modifier = Modifier
) {
    val upcomingProducts = productsWithDividends.filter { productWithDividends ->
        productWithDividends.dividends.any { it.dividendDate >= LocalDate.now() }
    }.sortedBy { productWithDividends ->
        productWithDividends.dividends
            .filter { it.dividendDate >= LocalDate.now() }
            .minOfOrNull { it.dividendDate } ?: LocalDate.MAX
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.upcoming_dividends),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (upcomingProducts.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    upcomingProducts.forEach { productWithDividends ->
                        val product = productWithDividends.product
                        val dividends = productWithDividends.dividends
                            .filter { it.dividendDate >= LocalDate.now() }
                            .sortedBy { it.dividendDate }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "${product.ticker} - ${product.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                
                                dividends.forEach { dividend ->
                                    val daysUntilDividend = ChronoUnit.DAYS.between(
                                        LocalDate.now(), 
                                        dividend.dividendDate
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Date: ${dividend.dividendDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = "Amount: €${String.format("%.2f", dividend.dividendAmount)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = if (daysUntilDividend == 0L) 
                                                    stringResource(R.string.today_exclamation) 
                                                else 
                                                    stringResource(R.string.days_until_dividend, daysUntilDividend),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (daysUntilDividend <= 7) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        
                                        IconButton(
                                            onClick = { onDeleteDividend(dividend) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text(
                                                "×", 
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                    
                                    if (dividend != dividends.last()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No upcoming dividends",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 