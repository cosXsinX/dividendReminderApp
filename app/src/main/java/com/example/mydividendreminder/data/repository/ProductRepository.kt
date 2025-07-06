package com.example.mydividendreminder.data.repository

import com.example.mydividendreminder.data.dao.ProductDao
import com.example.mydividendreminder.data.entity.Product
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ProductRepository(private val productDao: ProductDao) {
    
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    
    suspend fun getProductByTicker(ticker: String): Product? = productDao.getProductByTicker(ticker)
    
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    
    suspend fun deleteProductById(id: Long) = productDao.deleteProductById(id)
    
    fun getProductsByDividendDate(startDate: LocalDate): Flow<List<Product>> = 
        productDao.getProductsByDividendDate(startDate.toString())
    
    suspend fun getUpcomingDividends(daysAhead: Int): List<Product> {
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(daysAhead.toLong())
        return productDao.getUpcomingDividends(startDate.toString(), endDate.toString())
    }
} 