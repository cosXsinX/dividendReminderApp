package com.example.mydividendreminder.data.repository

import com.example.mydividendreminder.data.entity.Product
import com.example.mydividendreminder.data.entity.ProductWithSectors
import com.example.mydividendreminder.data.entity.ProductWithDividends
import com.example.mydividendreminder.data.entity.Sector
import com.example.mydividendreminder.data.entity.Dividend
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class CombinedRepository(
    private val productRepository: ProductRepository,
    private val sectorRepository: SectorRepository,
    private val dividendRepository: DividendRepository
) {
    
    // Product operations
    fun getAllProducts(): Flow<List<Product>> = productRepository.getAllProducts()
    
    fun getAllProductsWithSectors(): Flow<List<ProductWithSectors>> = 
        productRepository.getAllProductsWithSectors()
    
    fun getAllProductsWithDividends(): Flow<List<ProductWithDividends>> = 
        productRepository.getAllProductsWithDividends()
    
    suspend fun getProductById(id: Long): Product? = productRepository.getProductById(id)
    
    suspend fun getProductWithSectorsById(id: Long): ProductWithSectors? = 
        productRepository.getProductWithSectorsById(id)
    
    suspend fun getProductWithDividendsById(id: Long): ProductWithDividends? = 
        productRepository.getProductWithDividendsById(id)
    
    suspend fun getProductByTicker(ticker: String): Product? = productRepository.getProductByTicker(ticker)
    
    suspend fun insertProduct(product: Product): Long = productRepository.insertProduct(product)
    
    suspend fun updateProduct(product: Product) = productRepository.updateProduct(product)
    
    suspend fun deleteProduct(product: Product) = productRepository.deleteProduct(product)
    
    suspend fun deleteProductById(id: Long) = productRepository.deleteProductById(id)
    
    // Dividend operations
    fun getAllDividends(): Flow<List<Dividend>> = dividendRepository.getAllDividends()
    
    fun getDividendsByProductId(productId: Long): Flow<List<Dividend>> = 
        dividendRepository.getDividendsByProductId(productId)
    
    fun getFutureDividends(currentDate: LocalDate): Flow<List<Dividend>> = 
        dividendRepository.getFutureDividends(currentDate)
    
    fun getDividendsByDate(startDate: LocalDate): Flow<List<Dividend>> = 
        dividendRepository.getDividendsByDate(startDate)
    
    suspend fun getUpcomingDividends(startDate: LocalDate, endDate: LocalDate): List<Dividend> = 
        dividendRepository.getUpcomingDividends(startDate, endDate)
    
    suspend fun getDividendById(id: Long): Dividend? = dividendRepository.getDividendById(id)
    
    suspend fun dividendExists(productId: Long, dividendDate: LocalDate, dividendAmount: Double): Boolean =
        dividendRepository.dividendExists(productId, dividendDate, dividendAmount)
    
    suspend fun insertDividend(dividend: Dividend): Long = dividendRepository.insertDividend(dividend)
    
    suspend fun insertDividends(dividends: List<Dividend>) = dividendRepository.insertDividends(dividends)
    
    suspend fun updateDividend(dividend: Dividend) = dividendRepository.updateDividend(dividend)
    
    suspend fun deleteDividend(dividend: Dividend) = dividendRepository.deleteDividend(dividend)
    
    suspend fun deleteDividendById(id: Long) = dividendRepository.deleteDividendById(id)
    
    suspend fun deleteDividendsByProductId(productId: Long) = dividendRepository.deleteDividendsByProductId(productId)
    
    // Sector operations
    fun getAllSectors(): Flow<List<Sector>> = sectorRepository.getAllSectors()
    
    suspend fun getSectorById(id: Long): Sector? = sectorRepository.getSectorById(id)
    
    suspend fun insertSector(sector: Sector): Long = sectorRepository.insertSector(sector)
    
    suspend fun updateSector(sector: Sector) = sectorRepository.updateSector(sector)
    
    suspend fun deleteSector(sector: Sector) = sectorRepository.deleteSector(sector)
    
    suspend fun deleteSectorById(id: Long) = sectorRepository.deleteSectorById(id)
    
    // Combined operations
    suspend fun addProductWithSectors(
        product: Product,
        sectorIds: List<Long>
    ): Long {
        val productId = insertProduct(product)
        sectorIds.forEach { sectorId ->
            sectorRepository.addSectorToProduct(productId, sectorId)
        }
        return productId
    }
    
    suspend fun updateProductSectors(productId: Long, sectorIds: List<Long>) {
        // Remove all existing sector associations
        sectorRepository.removeAllSectorsFromProduct(productId)
        // Add new sector associations
        sectorIds.forEach { sectorId ->
            sectorRepository.addSectorToProduct(productId, sectorId)
        }
    }
    
    suspend fun getSectorsForProduct(productId: Long): List<Sector> = 
        sectorRepository.getSectorsForProduct(productId)
    
    fun getSectorsForProductFlow(productId: Long): Flow<List<Sector>> = 
        sectorRepository.getSectorsForProductFlow(productId)
    
    suspend fun addSectorToProduct(productId: Long, sectorId: Long) = 
        sectorRepository.addSectorToProduct(productId, sectorId)
    
    suspend fun removeSectorFromProduct(productId: Long, sectorId: Long) = 
        sectorRepository.removeSectorFromProduct(productId, sectorId)
} 