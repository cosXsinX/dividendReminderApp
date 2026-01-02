package com.example.mydividendreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mydividendreminder.data.entity.Product
import com.example.mydividendreminder.data.entity.ProductWithSectors
import com.example.mydividendreminder.data.entity.ProductWithDividends
import com.example.mydividendreminder.data.entity.Sector
import com.example.mydividendreminder.data.entity.Dividend
import com.example.mydividendreminder.data.repository.CombinedRepository
import com.example.mydividendreminder.domain.model.Stock
import com.example.mydividendreminder.domain.usecase.GetStockInfoUseCase
import com.example.mydividendreminder.service.DividendSyncService
import com.example.mydividendreminder.service.DividendSyncServiceImpl
import com.example.mydividendreminder.service.DividendData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ProductViewModel(
    private val repository: CombinedRepository,
    private val getStockInfo: GetStockInfoUseCase? = null
) : ViewModel() {
    
    private val syncService: DividendSyncService = DividendSyncServiceImpl()
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _productsWithDividends = MutableStateFlow<List<ProductWithDividends>>(emptyList())
    val productsWithDividends: StateFlow<List<ProductWithDividends>> = _productsWithDividends.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _sectors = MutableStateFlow<List<Sector>>(emptyList())
    val sectors: StateFlow<List<Sector>> = _sectors.asStateFlow()
    
    private val _dividends = MutableStateFlow<List<Dividend>>(emptyList())
    val dividends: StateFlow<List<Dividend>> = _dividends.asStateFlow()
    
    private val _stockInfo = MutableStateFlow<Stock?>(null)
    val stockInfo: StateFlow<Stock?> = _stockInfo.asStateFlow()
    
    private val _isLoadingStock = MutableStateFlow(false)
    val isLoadingStock: StateFlow<Boolean> = _isLoadingStock.asStateFlow()
    
    private val _stockError = MutableStateFlow<String?>(null)
    val stockError: StateFlow<String?> = _stockError.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()
    
    private val _syncSuccess = MutableStateFlow<String?>(null)
    val syncSuccess: StateFlow<String?> = _syncSuccess.asStateFlow()
    
    init {
        loadProducts()
        loadProductsWithDividends()
        loadSectors()
        loadDividends()
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            repository.getAllProducts().collect { productList ->
                _products.value = productList
                if (_isLoading.value) {
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun loadProductsWithDividends() {
        viewModelScope.launch {
            repository.getAllProductsWithDividends().collect { productList ->
                _productsWithDividends.value = productList
            }
        }
    }
    
    fun loadSectors() {
        viewModelScope.launch {
            repository.getAllSectors().collect { sectorList ->
                _sectors.value = sectorList
            }
        }
    }
    
    fun loadDividends() {
        viewModelScope.launch {
            repository.getAllDividends().collect { dividendList ->
                _dividends.value = dividendList
            }
        }
    }
    
    fun fetchStockInfo(ticker: String) {
        if (getStockInfo == null) {
            _stockError.value = "Stock fetching not available"
            return
        }
        
        viewModelScope.launch {
            _isLoadingStock.value = true
            _stockError.value = null
            try {
                val stock = getStockInfo(ticker)
                _stockInfo.value = stock
            } catch (e: Exception) {
                _stockError.value = "Failed to fetch stock info: ${e.message}"
                _stockInfo.value = null
            } finally {
                _isLoadingStock.value = false
            }
        }
    }
    
    fun clearStockInfo() {
        _stockInfo.value = null
        _stockError.value = null
    }
    
    fun addProduct(ticker: String, name: String, isin: String, selectedSectorIds: List<Long> = emptyList()) {
        viewModelScope.launch {
            val product = Product(
                ticker = ticker,
                name = name,
                isin = isin
            )
            repository.addProductWithSectors(product, selectedSectorIds)
            loadProducts()
            clearStockInfo()
        }
    }
    
    fun addDividend(productId: Long, dividendDate: LocalDate, dividendAmount: Double) {
        viewModelScope.launch {
            val dividend = Dividend(
                productId = productId,
                dividendDate = dividendDate,
                dividendAmount = dividendAmount
            )
            repository.insertDividend(dividend)
            loadDividends()
            loadProductsWithDividends()
        }
    }
    
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            loadProducts()
            loadProductsWithDividends()
        }
    }
    
    fun deleteDividend(dividend: Dividend) {
        viewModelScope.launch {
            repository.deleteDividend(dividend)
            loadDividends()
            loadProductsWithDividends()
        }
    }
    
    fun updateProduct(product: Product, selectedSectorIds: List<Long> = emptyList()) {
        viewModelScope.launch {
            repository.updateProduct(product)
            if (selectedSectorIds.isNotEmpty()) {
                repository.updateProductSectors(product.id, selectedSectorIds)
            }
            loadProducts()
            clearStockInfo()
        }
    }
    
    fun updateDividend(dividend: Dividend) {
        viewModelScope.launch {
            repository.updateDividend(dividend)
            loadDividends()
            loadProductsWithDividends()
        }
    }
    
    fun getFutureDividends(currentDate: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            repository.getFutureDividends(currentDate).collect { dividendList ->
                _dividends.value = dividendList
            }
        }
    }
    
    fun syncDividendsFromUrl(url: String = "https://www.maximilienzakowski.org/2026/01/02/dividends-2026-2/") {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            _syncSuccess.value = null
            
            try {
                // Fetch dividend data from URL
                val dividendDataList = syncService.fetchDividendsFromUrl(url)
                
                if (dividendDataList.isEmpty()) {
                    _syncError.value = "No dividend data found on the page"
                    _isSyncing.value = false
                    return@launch
                }
                
                var productsCreated = 0
                var dividendsAdded = 0
                var dividendsSkipped = 0
                
                // Process each dividend
                for (dividendData in dividendDataList) {
                    try {
                        // Find or create product by ticker
                        var product = repository.getProductByTicker(dividendData.ticker)
                        
                        if (product == null) {
                            // Create new product
                            product = Product(
                                ticker = dividendData.ticker,
                                name = dividendData.company.ifBlank { dividendData.ticker },
                                isin = dividendData.isin.ifBlank { "" }
                            )
                            val productId = repository.insertProduct(product)
                            product = product.copy(id = productId)
                            productsCreated++
                        }
                        
                        // Check if dividend already exists (same product, date, and amount)
                        val dividendExists = repository.dividendExists(
                            productId = product.id,
                            dividendDate = dividendData.paymentDate,
                            dividendAmount = dividendData.amount
                        )

                        if (!dividendExists) {
                            // Add dividend using payment date (as that's when the user receives it)
                            val dividend = Dividend(
                                productId = product.id,
                                dividendDate = dividendData.paymentDate,
                                dividendAmount = dividendData.amount
                            )
                            repository.insertDividend(dividend)
                            dividendsAdded++
                        } else {
                            dividendsSkipped++
                        }
                    } catch (e: Exception) {
                        // Continue with next dividend if one fails
                        continue
                    }
                }
                
                // Refresh data
                loadProducts()
                loadDividends()
                loadProductsWithDividends()
                
                _syncSuccess.value = "Sync completed: $productsCreated products created, $dividendsAdded dividends added, $dividendsSkipped skipped"
            } catch (e: Exception) {
                _syncError.value = "Sync failed: ${e.message}"
            } finally {
                _isSyncing.value = false
            }
        }
    }
    
    fun clearSyncMessages() {
        _syncError.value = null
        _syncSuccess.value = null
    }
    
    class Factory(
        private val repository: CombinedRepository,
        private val getStockInfo: GetStockInfoUseCase? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(repository, getStockInfo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 