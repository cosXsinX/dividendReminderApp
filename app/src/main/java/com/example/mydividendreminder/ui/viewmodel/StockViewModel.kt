package com.example.mydividendreminder.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydividendreminder.domain.model.Stock
import com.example.mydividendreminder.domain.usecase.GetStockInfoUseCase
import kotlinx.coroutines.launch

class StockViewModel(private val getStockInfo: GetStockInfoUseCase) : ViewModel() {
    var stockState by mutableStateOf<Stock?>(null)
        private set

    var isLoading by mutableStateOf(false)

    fun loadStock(symbol: String) {
        viewModelScope.launch {
            isLoading = true
            stockState = getStockInfo(symbol)
            isLoading = false
        }
    }
}