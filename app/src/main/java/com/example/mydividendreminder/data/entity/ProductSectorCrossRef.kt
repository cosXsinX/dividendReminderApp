package com.example.mydividendreminder.data.entity

import androidx.room.Entity

@Entity(
    tableName = "product_sector_cross_ref",
    primaryKeys = ["productId", "sectorId"]
)
data class ProductSectorCrossRef(
    val productId: Long,
    val sectorId: Long
) 