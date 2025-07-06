package com.example.mydividendreminder

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydividendreminder.data.database.AppDatabase
import com.example.mydividendreminder.data.repository.ProductRepository
import com.example.mydividendreminder.service.DividendNotificationScheduler
import com.example.mydividendreminder.ui.screen.ProductListScreen
import com.example.mydividendreminder.ui.theme.MyDividendReminderTheme
import com.example.mydividendreminder.ui.viewmodel.ProductViewModel
import com.example.mydividendreminder.util.NotificationPermissionHelper

class MainActivity : FragmentActivity() {
    private lateinit var notificationScheduler: DividendNotificationScheduler
    private lateinit var permissionHelper: NotificationPermissionHelper

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
                    val repository = ProductRepository(database.productDao())
                    val viewModel: ProductViewModel = viewModel(
                        factory = ProductViewModel.Factory(repository)
                    )
                    
                    ProductListScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
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

