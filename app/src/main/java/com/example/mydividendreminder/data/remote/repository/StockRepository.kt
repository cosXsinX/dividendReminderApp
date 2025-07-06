package com.example.mydividendreminder.data.remote.repository

import com.example.mydividendreminder.domain.model.Stock

interface StockRepository {
    suspend fun getStock(symbol: String): Stock
}