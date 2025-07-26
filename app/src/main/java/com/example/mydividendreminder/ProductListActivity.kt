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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.mydividendreminder.util.NavigationHelper

class ProductListActivity : FragmentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyDividendReminderTheme {
                val database = AppDatabase.getDatabase(this)
                val productRepository = ProductRepository(database.productDao())
                val sectorRepository = SectorRepository(database.sectorDao())
                val dividendRepository = DividendRepository(database.dividendDao())
                val combinedRepository =
                    CombinedRepository(productRepository, sectorRepository, dividendRepository)

                // Setup Yahoo Finance API
                val yahooFinanceApi = YahooFinanceApiImpl()
                val stockRepository = StockRepositoryImpl(yahooFinanceApi)
                val getStockInfoUseCase = GetStockInfoUseCase(stockRepository)

                val productViewModel: ProductViewModel = viewModel(
                    factory = ProductViewModel.Factory(combinedRepository, getStockInfoUseCase)
                )

                val productsWithDividends by productViewModel.productsWithDividends.collectAsState()

                // Initialize navigation helper
                val navigationHelper = NavigationHelper(this@ProductListActivity)

                ProductListScreen(
                    viewModel = productViewModel,
                    navigationHelper = navigationHelper,
                    productsWithDividends = productsWithDividends,
                    onNavigateToAddDividendForProduct = navigationHelper.navigateToAddDividendForProduct()
                )
            }
        }
    }
} 