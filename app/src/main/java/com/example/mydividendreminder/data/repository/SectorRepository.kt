package com.example.mydividendreminder.data.repository

import com.example.mydividendreminder.data.dao.SectorDao
import com.example.mydividendreminder.data.entity.Sector
import com.example.mydividendreminder.data.entity.ProductSectorCrossRef
import kotlinx.coroutines.flow.Flow

class SectorRepository(private val sectorDao: SectorDao) {
    
    fun getAllSectors(): Flow<List<Sector>> = sectorDao.getAllSectors()
    
    suspend fun getSectorById(id: Long): Sector? = sectorDao.getSectorById(id)
    
    suspend fun insertSector(sector: Sector): Long = sectorDao.insertSector(sector)
    
    suspend fun updateSector(sector: Sector) = sectorDao.updateSector(sector)
    
    suspend fun deleteSector(sector: Sector) = sectorDao.deleteSector(sector)
    
    suspend fun deleteSectorById(id: Long) = sectorDao.deleteSectorById(id)
    
    // Cross-reference operations
    suspend fun addSectorToProduct(productId: Long, sectorId: Long) {
        sectorDao.insertProductSectorCrossRef(ProductSectorCrossRef(productId, sectorId))
    }
    
    suspend fun removeSectorFromProduct(productId: Long, sectorId: Long) {
        sectorDao.deleteProductSectorCrossRef(ProductSectorCrossRef(productId, sectorId))
    }
    
    suspend fun removeAllSectorsFromProduct(productId: Long) {
        sectorDao.removeAllSectorsFromProduct(productId)
    }
    
    suspend fun getSectorsForProduct(productId: Long): List<Sector> = 
        sectorDao.getSectorsForProduct(productId)
    
    fun getSectorsForProductFlow(productId: Long): Flow<List<Sector>> = 
        sectorDao.getSectorsForProductFlow(productId)
} 