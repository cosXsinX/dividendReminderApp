package com.example.mydividendreminder.domain.model

data class Stock(
    val symbol: String,
    val name: String,
    val price: Double,
    val isin: String? = null
)