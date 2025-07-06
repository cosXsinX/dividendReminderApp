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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun MainDashboardScreen(
    onNavigateToProducts: () -> Unit = {},
    onNavigateToSectors: () -> Unit = {},
    onNavigateToAddDividend: () -> Unit = {},
    onExportDividends: () -> Unit = {},
    productsWithDividends: List<ProductWithDividends> = emptyList(),
    onDeleteDividend: (Dividend) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(32.dp)
        )
        
        // Upcoming Dividends Section
        UpcomingDividendsSection(
            productsWithDividends = productsWithDividends,
            onDeleteDividend = onDeleteDividend,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Button(
                    onClick = onNavigateToProducts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(stringResource(R.string.view_products))
                }
                
                Button(
                    onClick = onNavigateToSectors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(stringResource(R.string.sectors))
                }
                
                Button(
                    onClick = onNavigateToAddDividend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(stringResource(R.string.add_dividend))
                }
                
                Button(
                    onClick = onExportDividends,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.export_dividends))
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(upcomingProducts) { productWithDividends ->
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