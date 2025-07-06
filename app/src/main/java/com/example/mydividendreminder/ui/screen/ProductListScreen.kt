package com.example.mydividendreminder.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import com.example.mydividendreminder.R
import com.example.mydividendreminder.data.entity.Product
import com.example.mydividendreminder.data.entity.ProductWithDividends
import com.example.mydividendreminder.data.entity.Dividend
import com.example.mydividendreminder.ui.viewmodel.ProductViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onNavigateToAddDividend: () -> Unit = {}
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sectors by viewModel.sectors.collectAsState()
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.all_products),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = stringResource(R.string.all_products_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = onBackPressed
            ) {
                Text(stringResource(R.string.back))
            }
        }
        
        // Add Product Button at the top
        AddProductButton(
            sectors = sectors,
            onAddProduct = { ticker, name, isin, selectedSectorIds ->
                viewModel.addProduct(ticker, name, isin, selectedSectorIds)
            },
            viewModel = viewModel,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Notification Settings Section
        NotificationSettingsSection()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        } else {
            if (products.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_products),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.add_products_to_get_started),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onDeleteProduct = { viewModel.deleteProduct(product) },
                            onNavigateToAddDividend = onNavigateToAddDividend
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductCard(
    product: Product,
    onDeleteProduct: () -> Unit,
    onNavigateToAddDividend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${product.ticker} - ${product.name}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "ISIN: ${product.isin}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    IconButton(onClick = onDeleteProduct) {
                        Text("×", style = MaterialTheme.typography.headlineMedium)
                    }
                    IconButton(onClick = onNavigateToAddDividend) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddProductButton(
    onAddProduct: (String, String, String, List<Long>) -> Unit,
    sectors: List<com.example.mydividendreminder.data.entity.Sector>,
    viewModel: ProductViewModel,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Button(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        Text(stringResource(R.string.add_product))
    }
    
    if (showDialog) {
        AddProductDialog(
            sectors = sectors,
            onDismiss = { showDialog = false },
            onConfirm = { ticker, name, isin, selectedSectorIds ->
                onAddProduct(ticker, name, isin, selectedSectorIds)
                showDialog = false
            },
            viewModel = viewModel
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    sectors: List<com.example.mydividendreminder.data.entity.Sector>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, List<Long>) -> Unit,
    viewModel: ProductViewModel
) {
    var ticker by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isin by remember { mutableStateOf("") }
    var selectedSectorIds by remember { mutableStateOf(setOf<Long>()) }
    
    val stockInfo by viewModel.stockInfo.collectAsState()
    val isLoadingStock by viewModel.isLoadingStock.collectAsState()
    val stockError by viewModel.stockError.collectAsState()
    
    // Auto-fill name and ISIN when stock info is fetched
    LaunchedEffect(stockInfo) {
        stockInfo?.let { stock ->
            name = stock.name
            isin = stock.isin ?: ""
        }
    }
    
    // Debounce ticker input to avoid too many API calls
    LaunchedEffect(ticker) {
        if (ticker.isNotEmpty() && ticker.length >= 2) {
            kotlinx.coroutines.delay(500) // 500ms debounce
            if (ticker.length >= 2) { // Check again after delay
                viewModel.fetchStockInfo(ticker.uppercase())
            }
        } else {
            viewModel.clearStockInfo()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_new_product)) },
        text = {
            Column {
                TextField(
                    value = ticker,
                    onValueChange = { newTicker ->
                        ticker = newTicker
                    },
                    label = { Text(stringResource(R.string.ticker)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = stockError != null
                )
                
                // Show loading indicator for stock info
                if (isLoadingStock) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fetching stock information...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Show error message
                stockError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Show stock info preview
                stockInfo?.let { stock ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Stock Information",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Name: ${stock.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (stock.isin != null) {
                                Text(
                                    text = "ISIN: ${stock.isin}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "Price: $${stock.price}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingStock
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = isin,
                    onValueChange = { isin = it },
                    label = { Text(stringResource(R.string.isin)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingStock
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sectors),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                if (sectors.isNotEmpty()) {
                    sectors.forEach { sector ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedSectorIds.contains(sector.id),
                                onCheckedChange = { checked ->
                                    selectedSectorIds = if (checked) {
                                        selectedSectorIds + sector.id
                                    } else {
                                        selectedSectorIds - sector.id
                                    }
                                }
                            )
                            Text(
                                text = "${sector.name} (${sector.providerName})",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No sectors available. Please add sectors first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(ticker, name, isin, selectedSectorIds.toList())
                },
                enabled = ticker.isNotEmpty() && name.isNotEmpty() && !isLoadingStock
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationSettingsSection(
    modifier: Modifier = Modifier
) {
    var showNotificationInfo by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.notification_settings),
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(onClick = { showNotificationInfo = !showNotificationInfo }) {
                    Text(
                        text = if (showNotificationInfo) stringResource(R.string.collapse) else stringResource(R.string.expand),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            
            if (showNotificationInfo) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The app will automatically check for upcoming dividends daily and send notifications for dividends due within the next 7 days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Make sure to grant notification permissions when prompted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 