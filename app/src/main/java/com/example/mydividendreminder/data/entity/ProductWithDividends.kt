package com.example.mydividendreminder.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ProductWithDividends(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val dividends: List<Dividend>
) 