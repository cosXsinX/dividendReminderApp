package com.example.mydividendreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticker: String,
    val name: String,
    val isin: String,
    val dividendDate: LocalDate,
    val dividendAmount: Double
) 