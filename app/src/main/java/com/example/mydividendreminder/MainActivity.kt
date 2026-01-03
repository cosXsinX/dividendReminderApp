package com.example.mydividendreminder

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydividendreminder.data.database.AppDatabase
import com.example.mydividendreminder.data.repository.CombinedRepository
import com.example.mydividendreminder.data.repository.DividendRepository
import com.example.mydividendreminder.data.repository.ProductRepository
import com.example.mydividendreminder.data.repository.SectorRepository
import com.example.mydividendreminder.service.DividendNotificationScheduler
import com.example.mydividendreminder.ui.screen.MainDashboardScreen
import com.example.mydividendreminder.ui.theme.MyDividendReminderTheme
import com.example.mydividendreminder.ui.viewmodel.ProductViewModel
import com.example.mydividendreminder.util.NavigationHelper
import com.example.mydividendreminder.util.NotificationPermissionHelper
import kotlinx.coroutines.launch

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
                val isSyncing by productViewModel.isSyncing.collectAsState()
                val syncError by productViewModel.syncError.collectAsState()
                val syncSuccess by productViewModel.syncSuccess.collectAsState()
                
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Show sync status messages
                LaunchedEffect(syncError) {
                    syncError?.let { error ->
                        scope.launch {
                            snackbarHostState.showSnackbar(error)
                            productViewModel.clearSyncMessages()
                        }
                    }
                }

                LaunchedEffect(syncSuccess) {
                    syncSuccess?.let { success ->
                        scope.launch {
                            snackbarHostState.showSnackbar(success)
                            productViewModel.clearSyncMessages()
                        }
                    }
                }

                // Initialize navigation helper
                val navigationHelper = NavigationHelper(this@MainActivity)

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    MainDashboardScreen(
                        navigationHelper = navigationHelper,
                        productsWithDividends = productsWithDividends,
                        onDeleteDividend = { dividend -> productViewModel.deleteDividend(dividend) },
                        onSyncClick = { productViewModel.syncDividendsFromUrl() },
                        isSyncing = isSyncing,
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

