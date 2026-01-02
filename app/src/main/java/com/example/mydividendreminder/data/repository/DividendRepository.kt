package com.example.mydividendreminder.data.repository

import com.example.mydividendreminder.data.dao.DividendDao
import com.example.mydividendreminder.data.entity.Dividend
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class DividendRepository(private val dividendDao: DividendDao) {
    
    fun getAllDividends(): Flow<List<Dividend>> = dividendDao.getAllDividends()
    
    fun getDividendsByProductId(productId: Long): Flow<List<Dividend>> = 
        dividendDao.getDividendsByProductId(productId)
    
    fun getFutureDividends(currentDate: LocalDate): Flow<List<Dividend>> = 
        dividendDao.getFutureDividends(currentDate.toString())
    
    fun getDividendsByDate(startDate: LocalDate): Flow<List<Dividend>> = 
        dividendDao.getDividendsByDate(startDate.toString())
    
    suspend fun getUpcomingDividends(startDate: LocalDate, endDate: LocalDate): List<Dividend> = 
        dividendDao.getUpcomingDividends(startDate.toString(), endDate.toString())
    
    suspend fun getDividendById(id: Long): Dividend? = dividendDao.getDividendById(id)
    
    suspend fun dividendExists(productId: Long, dividendDate: LocalDate, dividendAmount: Double): Boolean =
        dividendDao.dividendExists(productId, dividendDate.toString(), dividendAmount)
    
    suspend fun insertDividend(dividend: Dividend): Long = dividendDao.insertDividend(dividend)
    
    suspend fun insertDividends(dividends: List<Dividend>) = dividendDao.insertDividends(dividends)
    
    suspend fun updateDividend(dividend: Dividend) = dividendDao.updateDividend(dividend)
    
    suspend fun deleteDividend(dividend: Dividend) = dividendDao.deleteDividend(dividend)
    
    suspend fun deleteDividendById(id: Long) = dividendDao.deleteDividendById(id)
    
    suspend fun deleteDividendsByProductId(productId: Long) = dividendDao.deleteDividendsByProductId(productId)
} 