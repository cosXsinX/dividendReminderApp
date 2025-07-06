package com.example.mydividendreminder

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import com.example.mydividendreminder.data.database.AppDatabase
import com.example.mydividendreminder.data.repository.ProductRepository
import com.example.mydividendreminder.data.repository.SectorRepository
import com.example.mydividendreminder.data.repository.DividendRepository
import com.example.mydividendreminder.data.repository.CombinedRepository
import com.example.mydividendreminder.data.remote.YahooFinanceApiImpl
import com.example.mydividendreminder.data.remote.repository.StockRepositoryImpl
import com.example.mydividendreminder.domain.usecase.GetStockInfoUseCase
import com.example.mydividendreminder.ui.screen.ProductListScreen
import com.example.mydividendreminder.ui.theme.MyDividendReminderTheme
import com.example.mydividendreminder.ui.viewmodel.ProductViewModel

class ProductListActivity : FragmentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            MyDividendReminderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val database = AppDatabase.getDatabase(this)
                    val productRepository = ProductRepository(database.productDao())
                    val sectorRepository = SectorRepository(database.sectorDao())
                    val dividendRepository = DividendRepository(database.dividendDao())
                    val combinedRepository = CombinedRepository(productRepository, sectorRepository, dividendRepository)
                    
                    // Setup Yahoo Finance API
                    val yahooFinanceApi = YahooFinanceApiImpl()
                    val stockRepository = StockRepositoryImpl(yahooFinanceApi)
                    val getStockInfoUseCase = GetStockInfoUseCase(stockRepository)
                    
                    val productViewModel: ProductViewModel = viewModel(
                        factory = ProductViewModel.Factory(combinedRepository, getStockInfoUseCase)
                    )
                    
                    ProductListScreen(
                        viewModel = productViewModel,
                        modifier = Modifier.padding(innerPadding),
                        onBackPressed = {
                            finish()
                        },
                        onNavigateToAddDividend = { productId ->
                            val intent = Intent(this@ProductListActivity, AddDividendActivity::class.java)
                            intent.putExtra("PRODUCT_ID", productId)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
} 