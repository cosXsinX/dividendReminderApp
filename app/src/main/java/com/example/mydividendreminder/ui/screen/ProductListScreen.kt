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
import androidx.compose.ui.unit.dp
import com.example.mydividendreminder.data.entity.Product
import com.example.mydividendreminder.ui.viewmodel.ProductViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Dividend Products",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
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
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
            }
        }
        
        AddProductButton(
            onAddProduct = { ticker, name, isin, dividendDate, dividendAmount ->
                viewModel.addProduct(ticker, name, isin, dividendDate, dividendAmount)
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
    onAddProduct: (String, String, String, LocalDate, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Button(
        onClick = { showDialog = true },
        modifier = modifier.padding(16.dp)
    ) {
        Text("Add Product")
    }
    
    if (showDialog) {
        AddProductDialog(
            onDismiss = { showDialog = false },
            onConfirm = { ticker, name, isin, dividendDate, dividendAmount ->
                onAddProduct(ticker, name, isin, dividendDate, dividendAmount)
                showDialog = false
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, LocalDate, Double) -> Unit
) {
    var ticker by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isin by remember { mutableStateOf("") }
    var dayInput by remember { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }
    var monthInput by remember { mutableStateOf(LocalDate.now().monthValue.toString()) }
    var yearInput by remember { mutableStateOf(LocalDate.now().year.toString()) }
    var dividendAmount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Product") },
        text = {
            Column {
                TextField(
                    value = ticker,
                    onValueChange = { ticker = it },
                    label = { Text("Ticker") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = isin,
                    onValueChange = { isin = it },
                    label = { Text("ISIN") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date selection
                Text(
                    text = "Dividend Date",
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
                        label = { Text("Day") },
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
                        label = { Text("Month") },
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
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = dividendAmount,
                    onValueChange = { dividendAmount = it },
                    label = { Text("Dividend Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
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
                        onConfirm(ticker, name, isin, selectedDate, amount)
                    } catch (e: Exception) {
                        // Handle invalid input
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
                    text = "Notification Settings",
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(onClick = { showNotificationInfo = !showNotificationInfo }) {
                    Text(
                        text = if (showNotificationInfo) "−" else "+",
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