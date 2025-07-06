package com.example.mydividendreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mydividendreminder.data.entity.Sector
import com.example.mydividendreminder.data.repository.SectorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SectorViewModel(private val sectorRepository: SectorRepository) : ViewModel() {
    
    private val _sectors = MutableStateFlow<List<Sector>>(emptyList())
    val sectors: StateFlow<List<Sector>> = _sectors.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadSectors()
        // Add some sample sectors if none exist
        viewModelScope.launch {
            val currentSectors = sectorRepository.getAllSectors().first()
            if (currentSectors.isEmpty()) {
                addSector("Technology", "NASDAQ")
                addSector("Healthcare", "NYSE")
                addSector("Finance", "NYSE")
                addSector("Energy", "NYSE")
                addSector("Consumer Goods", "NASDAQ")
            }
        }
    }
    
    private fun loadSectors() {
        viewModelScope.launch {
            _isLoading.value = true
            sectorRepository.getAllSectors().collect { sectorsList ->
                _sectors.value = sectorsList
                _isLoading.value = false
            }
        }
    }
    
    fun addSector(name: String, providerName: String) {
        viewModelScope.launch {
            val sector = Sector(name = name, providerName = providerName)
            sectorRepository.insertSector(sector)
        }
    }
    
    fun updateSector(sector: Sector) {
        viewModelScope.launch {
            sectorRepository.updateSector(sector)
        }
    }
    
    fun deleteSector(sector: Sector) {
        viewModelScope.launch {
            sectorRepository.deleteSector(sector)
        }
    }
    
    fun addSectorToProduct(productId: Long, sectorId: Long) {
        viewModelScope.launch {
            sectorRepository.addSectorToProduct(productId, sectorId)
        }
    }
    
    fun removeSectorFromProduct(productId: Long, sectorId: Long) {
        viewModelScope.launch {
            sectorRepository.removeSectorFromProduct(productId, sectorId)
        }
    }
    
    fun getSectorsForProduct(productId: Long, onResult: (List<Sector>) -> Unit) {
        viewModelScope.launch {
            val sectors = sectorRepository.getSectorsForProduct(productId)
            onResult(sectors)
        }
    }
    
    class Factory(private val sectorRepository: SectorRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SectorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SectorViewModel(sectorRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 