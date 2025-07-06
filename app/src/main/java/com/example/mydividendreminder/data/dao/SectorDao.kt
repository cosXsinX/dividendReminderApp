package com.example.mydividendreminder.data.dao

import androidx.room.*
import com.example.mydividendreminder.data.entity.Sector
import com.example.mydividendreminder.data.entity.ProductSectorCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface SectorDao {
    @Query("SELECT * FROM sectors ORDER BY name ASC")
    fun getAllSectors(): Flow<List<Sector>>

    @Query("SELECT * FROM sectors WHERE id = :id")
    suspend fun getSectorById(id: Long): Sector?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSector(sector: Sector): Long

    @Update
    suspend fun updateSector(sector: Sector)

    @Delete
    suspend fun deleteSector(sector: Sector)

    @Query("DELETE FROM sectors WHERE id = :id")
    suspend fun deleteSectorById(id: Long)

    // Cross-reference operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductSectorCrossRef(crossRef: ProductSectorCrossRef)

    @Delete
    suspend fun deleteProductSectorCrossRef(crossRef: ProductSectorCrossRef)

    @Query("DELETE FROM product_sector_cross_ref WHERE productId = :productId")
    suspend fun removeAllSectorsFromProduct(productId: Long)

    @Query("SELECT * FROM sectors WHERE id IN (SELECT sectorId FROM product_sector_cross_ref WHERE productId = :productId)")
    suspend fun getSectorsForProduct(productId: Long): List<Sector>

    @Query("SELECT * FROM sectors WHERE id IN (SELECT sectorId FROM product_sector_cross_ref WHERE productId = :productId)")
    fun getSectorsForProductFlow(productId: Long): Flow<List<Sector>>
} 