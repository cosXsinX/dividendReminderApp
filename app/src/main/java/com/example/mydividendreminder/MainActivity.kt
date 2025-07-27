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
import com.example.mydividendreminder.util.DividendExportHelper
import com.example.mydividendreminder.util.NavigationHelper
import android.widget.Toast
import com.example.mydividendreminder.ApiKeyActivity
import com.example.mydividendreminder.PromptActivity
import com.example.mydividendreminder.HelpActivity
import com.example.mydividendreminder.ui.theme.DefaultMainAppBar

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

        setContent {

            MyDividendReminderTheme {
                val database = AppDatabase.getDatabase(this)
                val productRepository = ProductRepository(database.productDao())
                val sectorRepository = SectorRepository(database.sectorDao())
                val dividendRepository = DividendRepository(database.dividendDao())
                val combinedRepository =
                    CombinedRepository(productRepository, sectorRepository, dividendRepository)
                val productViewModel: ProductViewModel = viewModel(
                    factory = ProductViewModel.Factory(combinedRepository)
                )

                val productsWithDividends by productViewModel.productsWithDividends.collectAsState()

                // Initialize navigation helper
                val navigationHelper = NavigationHelper(this@MainActivity)

                MainDashboardScreen(
                    navigationHelper = navigationHelper,
                    productsWithDividends = productsWithDividends,
                    onDeleteDividend = { dividend -> productViewModel.deleteDividend(dividend) }
                )
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

