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
import com.example.mydividendreminder.data.repository.CombinedRepository
import com.example.mydividendreminder.service.DividendNotificationScheduler
import com.example.mydividendreminder.ui.screen.ProductListScreen
import com.example.mydividendreminder.ui.theme.MyDividendReminderTheme
import com.example.mydividendreminder.ui.viewmodel.ProductViewModel
import com.example.mydividendreminder.util.NotificationPermissionHelper

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
                    val combinedRepository = CombinedRepository(productRepository, sectorRepository)
                    val productViewModel: ProductViewModel = viewModel(
                        factory = ProductViewModel.Factory(combinedRepository)
                    )
                    
                    ProductListScreen(
                        viewModel = productViewModel,
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToSectors = {
                            val intent = Intent(this@MainActivity, SectorActivity::class.java)
                            startActivity(intent)
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
}

