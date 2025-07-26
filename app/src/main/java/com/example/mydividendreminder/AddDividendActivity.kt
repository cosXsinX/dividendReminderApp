package com.example.mydividendreminder

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.mydividendreminder.data.database.AppDatabase
import com.example.mydividendreminder.data.repository.ProductRepository
import com.example.mydividendreminder.data.repository.SectorRepository
import com.example.mydividendreminder.data.repository.DividendRepository
import com.example.mydividendreminder.data.repository.CombinedRepository
import com.example.mydividendreminder.data.entity.Product
import com.example.mydividendreminder.data.remote.YahooFinanceApiImpl
import com.example.mydividendreminder.data.remote.repository.StockRepositoryImpl
import com.example.mydividendreminder.domain.usecase.GetStockInfoUseCase
import com.example.mydividendreminder.ui.theme.MyDividendReminderTheme
import com.example.mydividendreminder.ui.viewmodel.ProductViewModel

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.content.Intent
import com.example.mydividendreminder.ui.theme.DefaultMainAppBar
import com.example.mydividendreminder.util.NavigationHelper

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
class AddDividendActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyDividendReminderTheme {
                // Initialize navigation helper
                val navigationHelper = NavigationHelper(this@AddDividendActivity)

                AddDividendContent(
                    navigationHelper = navigationHelper,
                    onDividendAdded = { finish() },
                    preselectedProductId = intent.getLongExtra("PRODUCT_ID", -1L)
                        .let { if (it != -1L) it else null }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDividendContent(
    navigationHelper: com.example.mydividendreminder.util.NavigationHelper,
    onDividendAdded: () -> Unit = {},
    preselectedProductId: Long? = null
) {
    val database = AppDatabase.getDatabase(LocalContext.current as android.app.Activity)
    val productRepository = ProductRepository(database.productDao())
    val sectorRepository = SectorRepository(database.sectorDao())
    val dividendRepository = DividendRepository(database.dividendDao())
    val combinedRepository =
        CombinedRepository(productRepository, sectorRepository, dividendRepository)

    // Setup Yahoo Finance API
    val yahooFinanceApi = YahooFinanceApiImpl()
    val stockRepository = StockRepositoryImpl(yahooFinanceApi)
    val getStockInfoUseCase = GetStockInfoUseCase(stockRepository)

    val productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.Factory(combinedRepository, getStockInfoUseCase)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DefaultMainAppBar(
            navigationHelper = navigationHelper
        )

        AddDividendScreen(
            viewModel = productViewModel,
            modifier = Modifier.padding(16.dp),
            onDividendAdded = onDividendAdded,
            preselectedProductId = preselectedProductId
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDividendScreen(
    viewModel: ProductViewModel,
    modifier: Modifier = Modifier,
    onDividendAdded: () -> Unit = {},
    preselectedProductId: Long? = null
) {
    val products by viewModel.products.collectAsState()
    val productsWithDividends by viewModel.productsWithDividends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showProductSelection by remember { mutableStateOf(false) }
    var showAddDividendDialog by remember { mutableStateOf(false) }

    // Pre-select product if product ID is provided
    LaunchedEffect(preselectedProductId, products) {
        if (preselectedProductId != null && selectedProduct == null) {
            val product = products.find { it.id == preselectedProductId }
            if (product != null) {
                selectedProduct = product
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Product Selection Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_product),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedProduct != null) {
                    // Show selected product
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = selectedProduct!!.ticker,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = selectedProduct!!.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.no_product_selected),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showProductSelection = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedProduct != null)
                            stringResource(R.string.change_product)
                        else
                            stringResource(R.string.select_product)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upcoming Dividends Section
        UpcomingDividendsSection(
            productsWithDividends = productsWithDividends,
            onDeleteDividend = { dividend -> viewModel.deleteDividend(dividend) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add Dividend Button
        Button(
            onClick = {
                if (selectedProduct != null) {
                    showAddDividendDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedProduct != null
        ) {
            Text(stringResource(R.string.add_dividend))
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Product Selection Dialog
    if (showProductSelection) {
        AlertDialog(
            onDismissRequest = { showProductSelection = false },
            title = { Text(stringResource(R.string.select_product)) },
            text = {
                LazyColumn {
                    items(products) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                selectedProduct = product
                                showProductSelection = false
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = product.ticker,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProductSelection = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Add Dividend Dialog
    if (showAddDividendDialog && selectedProduct != null) {
        AddDividendDialog(
            product = selectedProduct!!,
            onDismiss = { showAddDividendDialog = false },
            onConfirm = { dividendDate, dividendAmount ->
                viewModel.addDividend(
                    productId = selectedProduct!!.id,
                    dividendDate = dividendDate,
                    dividendAmount = dividendAmount
                )
                showAddDividendDialog = false
                onDividendAdded()
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDividendDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, Double) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var dividendAmount by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.add_dividend_for_product, product.ticker)
            )
        },
        text = {
            Column {
                // Date Selection
                Text(
                    text = stringResource(R.string.dividend_date),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input
                TextField(
                    value = dividendAmount,
                    onValueChange = { dividendAmount = it },
                    label = { Text(stringResource(R.string.dividend_amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = dividendAmount.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(selectedDate, amount)
                    }
                },
                enabled = dividendAmount.isNotEmpty() && dividendAmount.toDoubleOrNull() != null
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

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpcomingDividendsSection(
    productsWithDividends: List<com.example.mydividendreminder.data.entity.ProductWithDividends>,
    onDeleteDividend: (com.example.mydividendreminder.data.entity.Dividend) -> Unit,
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
                                    val daysUntilDividend =
                                        java.time.temporal.ChronoUnit.DAYS.between(
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
                                                text = "Date: ${
                                                    dividend.dividendDate.format(
                                                        DateTimeFormatter.ISO_LOCAL_DATE
                                                    )
                                                }",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = "Amount: €${
                                                    String.format(
                                                        "%.2f",
                                                        dividend.dividendAmount
                                                    )
                                                }",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = if (daysUntilDividend == 0L)
                                                    stringResource(R.string.today_exclamation)
                                                else
                                                    stringResource(
                                                        R.string.days_until_dividend,
                                                        daysUntilDividend
                                                    ),
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