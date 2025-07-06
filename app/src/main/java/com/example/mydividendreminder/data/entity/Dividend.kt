package com.example.mydividendreminder.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "dividends",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Dividend(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val dividendDate: LocalDate,
    val dividendAmount: Double
) 