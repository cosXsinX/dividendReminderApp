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
import com.example.mydividendreminder.ui.viewmodel.ProductViewModel

import com.example.mydividendreminder.ui.theme.DefaultMainAppBar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    navigationHelper: com.example.mydividendreminder.util.NavigationHelper,
    productsWithDividends: List<com.example.mydividendreminder.data.entity.ProductWithDividends> = emptyList(),
    modifier: Modifier = Modifier,
    onNavigateToAddDividendForProduct: (Long) -> Unit = {}
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sectors by viewModel.sectors.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DefaultMainAppBar(
            navigationHelper = navigationHelper,
            productsWithDividends = productsWithDividends
        )
        
        // Notification Settings Section
        NotificationSettingsSection()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onEditProduct = { 
                            productToEdit = it
                            showEditDialog = true
                        },
                        onDeleteProduct = { viewModel.deleteProduct(product) },
                        onNavigateToAddDividend = { onNavigateToAddDividendForProduct(product.id) }
                    )
                }
            }
        }
        
        // Add Product Button at the bottom
        AddProductButton(
            sectors = sectors,
            onAddProduct = { ticker, name, isin, selectedSectorIds ->
                viewModel.addProduct(ticker, name, isin, selectedSectorIds)
            },
            viewModel = viewModel
        )
    }
    
    // Edit Dialog
    if (showEditDialog && productToEdit != null) {
        EditProductDialog(
            product = productToEdit!!,
            sectors = sectors,
            onDismiss = { 
                viewModel.clearStockInfo()
                showEditDialog = false 
                productToEdit = null
            },
            onConfirm = { ticker, name, isin, selectedSectorIds ->
                viewModel.updateProduct(productToEdit!!.copy(ticker = ticker, name = name, isin = isin))
                viewModel.clearStockInfo()
                // Update sectors if needed
                showEditDialog = false
                productToEdit = null
            },
            viewModel = viewModel
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductCard(
    product: Product,
    onDeleteProduct: () -> Unit,
    onNavigateToAddDividend: () -> Unit,
    onEditProduct: (Product) -> Unit,
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
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onEditProduct(product) }) {
                        Text("✏️", style = MaterialTheme.typography.bodyLarge)
                    }
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
        modifier = modifier.padding(top = 0.dp, bottom = 60.dp)
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
    var selectedSectorIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    sectors: List<com.example.mydividendreminder.data.entity.Sector>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, List<Long>) -> Unit,
    viewModel: ProductViewModel
) {
    var ticker by remember { mutableStateOf(product.ticker) }
    var name by remember { mutableStateOf(product.name) }
    var isin by remember { mutableStateOf(product.isin) }
    var selectedSectorIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    
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
        if (ticker.isNotEmpty() && ticker.length >= 2 && ticker != product.ticker) {
            kotlinx.coroutines.delay(500) // 500ms debounce
            if (ticker.length >= 2 && ticker != product.ticker) { // Check again after delay
                viewModel.fetchStockInfo(ticker.uppercase())
            }
        } else if (ticker.isEmpty() || ticker == product.ticker) {
            viewModel.clearStockInfo()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_product)) },
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
                    onValueChange = { newName ->
                        name = newName
                    },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingStock
                )
                TextField(
                    value = isin,
                    onValueChange = { newIsin ->
                        isin = newIsin
                    },
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 