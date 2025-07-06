package com.example.mydividendreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mydividendreminder.data.entity.Product
import com.example.mydividendreminder.data.entity.ProductWithSectors
import com.example.mydividendreminder.data.entity.Sector
import com.example.mydividendreminder.data.repository.CombinedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ProductViewModel(private val repository: CombinedRepository) : ViewModel() {
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _sectors = MutableStateFlow<List<Sector>>(emptyList())
    val sectors: StateFlow<List<Sector>> = _sectors.asStateFlow()
    
    init {
        loadProducts()
        loadSectors()
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            repository.getFutureDividendProducts().collect { productList ->
                _products.value = productList
                if (_isLoading.value) {
                    _isLoading.value = false
                }
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
    
    fun addProduct(ticker: String, name: String, isin: String, dividendDate: LocalDate, dividendAmount: Double, selectedSectorIds: List<Long> = emptyList()) {
        viewModelScope.launch {
            val product = Product(
                ticker = ticker,
                name = name,
                isin = isin,
                dividendDate = dividendDate,
                dividendAmount = dividendAmount
            )
            repository.addProductWithSectors(product, selectedSectorIds)
            loadProducts()
        }
    }
    
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            loadProducts()
        }
    }
    
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }
    
    class Factory(private val repository: CombinedRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 