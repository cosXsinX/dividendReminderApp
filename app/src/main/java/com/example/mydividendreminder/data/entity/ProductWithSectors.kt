package com.example.mydividendreminder.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ProductWithSectors(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProductSectorCrossRef::class,
            parentColumn = "productId",
            entityColumn = "sectorId"
        )
    )
    val sectors: List<Sector>
) 