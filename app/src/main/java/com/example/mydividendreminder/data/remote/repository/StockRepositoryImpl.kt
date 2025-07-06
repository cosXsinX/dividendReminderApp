package com.example.mydividendreminder.data.remote.repository

import com.example.mydividendreminder.data.remote.YahooFinanceApi
import com.example.mydividendreminder.domain.model.Stock

class StockRepositoryImpl(private val api: YahooFinanceApi) : StockRepository {
    override suspend fun getStock(symbol: String): Stock {
        val response = api.fetchQuote(symbol)
        return Stock(
            symbol = response.symbol,
            name = response.longName ?: response.shortName ?: "N/A",
            price = response.regularMarketPrice ?: 0.0,
            isin = response.isin
        )
    }
}