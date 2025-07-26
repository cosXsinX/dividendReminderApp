package com.example.mydividendreminder

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydividendreminder.data.database.AppDatabase
import com.example.mydividendreminder.data.repository.SectorRepository
import com.example.mydividendreminder.ui.screen.SectorListScreen
import com.example.mydividendreminder.ui.theme.MyDividendReminderTheme
import com.example.mydividendreminder.ui.viewmodel.SectorViewModel
import com.example.mydividendreminder.util.NavigationHelper
import android.content.Intent

class SectorActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyDividendReminderTheme {

                val database = AppDatabase.getDatabase(this)
                val sectorRepository = SectorRepository(database.sectorDao())
                val sectorViewModel: SectorViewModel = viewModel(
                    factory = SectorViewModel.Factory(sectorRepository)
                )

                // Initialize navigation helper
                val navigationHelper = NavigationHelper(this@SectorActivity)

                SectorListScreen(
                    viewModel = sectorViewModel,
                    navigationHelper = navigationHelper
                )
            }
        }
    }
} 