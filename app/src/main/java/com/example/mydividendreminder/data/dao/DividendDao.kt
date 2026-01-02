package com.example.mydividendreminder.data.dao

import androidx.room.*
import com.example.mydividendreminder.data.entity.Dividend
import kotlinx.coroutines.flow.Flow

@Dao
interface DividendDao {
    @Query("SELECT * FROM dividends ORDER BY dividendDate ASC")
    fun getAllDividends(): Flow<List<Dividend>>

    @Query("SELECT * FROM dividends WHERE productId = :productId ORDER BY dividendDate ASC")
    fun getDividendsByProductId(productId: Long): Flow<List<Dividend>>

    @Query("SELECT * FROM dividends WHERE dividendDate > :currentDate ORDER BY dividendDate ASC")
    fun getFutureDividends(currentDate: String): Flow<List<Dividend>>

    @Query("SELECT * FROM dividends WHERE id = :id")
    suspend fun getDividendById(id: Long): Dividend?

    @Query("SELECT * FROM dividends WHERE dividendDate >= :startDate ORDER BY dividendDate ASC")
    fun getDividendsByDate(startDate: String): Flow<List<Dividend>>

    @Query("SELECT * FROM dividends WHERE dividendDate BETWEEN :startDate AND :endDate ORDER BY dividendDate ASC")
    suspend fun getUpcomingDividends(startDate: String, endDate: String): List<Dividend>

    @Query("SELECT COUNT(*) > 0 FROM dividends WHERE productId = :productId AND dividendDate = :dividendDate AND ABS(dividendAmount - :dividendAmount) < 0.01")
    suspend fun dividendExists(productId: Long, dividendDate: String, dividendAmount: Double): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDividend(dividend: Dividend): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDividends(dividends: List<Dividend>)

    @Update
    suspend fun updateDividend(dividend: Dividend)

    @Delete
    suspend fun deleteDividend(dividend: Dividend)

    @Query("DELETE FROM dividends WHERE id = :id")
    suspend fun deleteDividendById(id: Long)

    @Query("DELETE FROM dividends WHERE productId = :productId")
    suspend fun deleteDividendsByProductId(productId: Long)
} 