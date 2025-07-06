package com.example.mydividendreminder.domain.usecase

import com.example.mydividendreminder.data.remote.repository.StockRepository
import com.example.mydividendreminder.domain.model.Stock

class GetStockInfoUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(symbol: String): Stock {
        return repository.getStock(symbol)
    }
}