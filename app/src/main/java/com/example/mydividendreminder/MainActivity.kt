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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import android.content.Intent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydividendreminder.service.DividendNotificationScheduler
import com.example.mydividendreminder.ui.screen.MainDashboardScreen
import com.example.mydividendreminder.ui.theme.MyDividendReminderTheme
import com.example.mydividendreminder.util.NotificationPermissionHelper
import com.example.mydividendreminder.data.database.AppDatabase
import com.example.mydividendreminder.data.repository.ProductRepository
import com.example.mydividendreminder.data.repository.SectorRepository
import com.example.mydividendreminder.data.repository.DividendRepository
import com.example.mydividendreminder.data.repository.CombinedRepository
import com.example.mydividendreminder.ui.viewmodel.ProductViewModel
import com.example.mydividendreminder.util.CsvExportUtil
import android.widget.Toast

class MainActivity : FragmentActivity() {
    private lateinit var notificationScheduler: DividendNotificationScheduler
    private lateinit var permissionHelper: NotificationPermissionHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize notification services
        notificationScheduler = DividendNotificationScheduler(this)
        permissionHelper = NotificationPermissionHelper(this)
        
        // Setup notification permission handling
        setupNotificationPermissions()
        
        enableEdgeToEdge()
        setContent {
            MyDividendReminderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val database = AppDatabase.getDatabase(this)
                    val productRepository = ProductRepository(database.productDao())
                    val sectorRepository = SectorRepository(database.sectorDao())
                    val dividendRepository = DividendRepository(database.dividendDao())
                    val combinedRepository = CombinedRepository(productRepository, sectorRepository, dividendRepository)
                    val productViewModel: ProductViewModel = viewModel(
                        factory = ProductViewModel.Factory(combinedRepository)
                    )
                    
                    val productsWithDividends by productViewModel.productsWithDividends.collectAsState()
                    
                    MainDashboardScreen(
                        modifier = Modifier.padding(innerPadding),
                        productsWithDividends = productsWithDividends,
                        onDeleteDividend = { dividend -> productViewModel.deleteDividend(dividend) },
                        onNavigateToProducts = {
                            val intent = Intent(this@MainActivity, ProductListActivity::class.java)
                            startActivity(intent)
                        },
                        onNavigateToSectors = {
                            val intent = Intent(this@MainActivity, SectorActivity::class.java)
                            startActivity(intent)
                        },
                        onNavigateToAddDividend = {
                            val intent = Intent(this@MainActivity, AddDividendActivity::class.java)
                            startActivity(intent)
                        },
                        onExportDividends = {
                            exportDividendsToCsv(productsWithDividends)
                        }
                    )
                }
            }
        }
    }

    private fun setupNotificationPermissions() {
        permissionHelper.onPermissionGranted = {
            // Schedule daily notification checks
            notificationScheduler.scheduleDailyNotificationCheck()
        }
        
        permissionHelper.onPermissionDenied = {
            // Handle permission denied - could show a dialog explaining why notifications are needed
        }
        
        // Check and request notification permission
        permissionHelper.checkAndRequestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        // Ensure notifications are scheduled when app resumes
        if (permissionHelper.hasNotificationPermission()) {
            notificationScheduler.scheduleDailyNotificationCheck()
        }
    }
    
    private fun exportDividendsToCsv(productsWithDividends: List<com.example.mydividendreminder.data.entity.ProductWithDividends>) {
        try {
            if (productsWithDividends.isEmpty()) {
                Toast.makeText(this, getString(R.string.no_dividends_to_export), Toast.LENGTH_SHORT).show()
                return
            }
            
            val csvUri = CsvExportUtil.exportDividendsToCsv(this, productsWithDividends)
            if (csvUri != null) {
                val shareIntent = CsvExportUtil.createShareIntent(this, csvUri)
                startActivity(Intent.createChooser(shareIntent, getString(R.string.export_dividends)))
            } else {
                Toast.makeText(this, getString(R.string.failed_to_create_csv), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_exporting_dividends, e.message), Toast.LENGTH_SHORT).show()
        }
    }
}

