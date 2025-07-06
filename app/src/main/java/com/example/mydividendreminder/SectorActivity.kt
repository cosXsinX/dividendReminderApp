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

class SectorActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            MyDividendReminderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val database = AppDatabase.getDatabase(this)
                    val sectorRepository = SectorRepository(database.sectorDao())
                    val sectorViewModel: SectorViewModel = viewModel(
                        factory = SectorViewModel.Factory(sectorRepository)
                    )
                    
                    SectorListScreen(
                        viewModel = sectorViewModel,
                        onBackPressed = { finish() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
} 