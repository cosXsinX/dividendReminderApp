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
import com.example.mydividendreminder.R
import com.example.mydividendreminder.data.entity.Product
import com.example.mydividendreminder.ui.viewmodel.ProductViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSectors: () -> Unit = {}
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
                    text = stringResource(R.string.upcoming_dividends),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = stringResource(R.string.sorted_by_nearest_dividend),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = onNavigateToSectors,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(stringResource(R.string.sectors))
            }
        }
        
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
                        text = stringResource(R.string.no_upcoming_dividends),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.add_products_with_future_dividends),
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
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }
        
        AddProductButton(
            sectors = sectors,
            onAddProduct = { ticker, name, isin, dividendDate, dividendAmount, selectedSectorIds ->
                viewModel.addProduct(ticker, name, isin, dividendDate, dividendAmount, selectedSectorIds)
            }
        )
    }
    

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductCard(
    product: Product,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val daysUntilDividend = java.time.temporal.ChronoUnit.DAYS.between(
        LocalDate.now(), 
        product.dividendDate
    )
    
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
                    Text(
                        text = "Dividend Date: ${product.dividendDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Amount: $${product.dividendAmount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (daysUntilDividend == 0L) stringResource(R.string.today_exclamation) else stringResource(R.string.days_until_dividend, daysUntilDividend),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (daysUntilDividend <= 7) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Text("×", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddProductButton(
    onAddProduct: (String, String, String, LocalDate, Double, List<Long>) -> Unit,
    sectors: List<com.example.mydividendreminder.data.entity.Sector>,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Button(
        onClick = { showDialog = true },
        modifier = modifier.padding(16.dp)
    ) {
        Text(stringResource(R.string.add_product))
    }
    
    if (showDialog) {
        AddProductDialog(
            sectors = sectors,
            onDismiss = { showDialog = false },
            onConfirm = { ticker, name, isin, dividendDate, dividendAmount, selectedSectorIds ->
                onAddProduct(ticker, name, isin, dividendDate, dividendAmount, selectedSectorIds)
                showDialog = false
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    sectors: List<com.example.mydividendreminder.data.entity.Sector>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, LocalDate, Double, List<Long>) -> Unit
) {
    var ticker by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isin by remember { mutableStateOf("") }
    var dayInput by remember { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }
    var monthInput by remember { mutableStateOf(LocalDate.now().monthValue.toString()) }
    var yearInput by remember { mutableStateOf(LocalDate.now().year.toString()) }
    var dividendAmount by remember { mutableStateOf("") }
    var selectedSectorIds by remember { mutableStateOf(setOf<Long>()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_new_product)) },
        text = {
            Column {
                TextField(
                    value = ticker,
                    onValueChange = { ticker = it },
                    label = { Text(stringResource(R.string.ticker)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = isin,
                    onValueChange = { isin = it },
                    label = { Text(stringResource(R.string.isin)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date selection
                Text(
                    text = stringResource(R.string.dividend_date),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Day input
                    TextField(
                        value = dayInput,
                        onValueChange = { 
                            if (it.length <= 2 && (it.isEmpty() || it.toIntOrNull() != null)) {
                                dayInput = it
                            }
                        },
                        label = { Text(stringResource(R.string.day)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    // Month input
                    TextField(
                        value = monthInput,
                        onValueChange = { 
                            if (it.length <= 2 && (it.isEmpty() || it.toIntOrNull() != null)) {
                                monthInput = it
                            }
                        },
                        label = { Text(stringResource(R.string.month)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    // Year input
                    TextField(
                        value = yearInput,
                        onValueChange = { 
                            if (it.length <= 4 && (it.isEmpty() || it.toIntOrNull() != null)) {
                                yearInput = it
                            }
                        },
                        label = { Text(stringResource(R.string.year)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = dividendAmount,
                    onValueChange = { dividendAmount = it },
                    label = { Text(stringResource(R.string.dividend_amount)) },
                    modifier = Modifier.fillMaxWidth()
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
                    try {
                        val day = dayInput.toIntOrNull() ?: LocalDate.now().dayOfMonth
                        val month = monthInput.toIntOrNull() ?: LocalDate.now().monthValue
                        val year = yearInput.toIntOrNull() ?: LocalDate.now().year
                        val selectedDate = LocalDate.of(year, month, day)
                        val amount = dividendAmount.toDouble()
                        onConfirm(ticker, name, isin, selectedDate, amount, selectedSectorIds.toList())
                    } catch (e: Exception) {
                        // Handle invalid input
                    }
                }
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