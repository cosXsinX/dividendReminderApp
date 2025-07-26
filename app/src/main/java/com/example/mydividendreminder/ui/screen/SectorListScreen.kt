package com.example.mydividendreminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mydividendreminder.R
import com.example.mydividendreminder.data.entity.Sector
import com.example.mydividendreminder.ui.viewmodel.SectorViewModel
import com.example.mydividendreminder.ui.theme.DefaultMainAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorListScreen(
    viewModel: SectorViewModel,
    navigationHelper: com.example.mydividendreminder.util.NavigationHelper,
    modifier: Modifier = Modifier
) {
    val sectors by viewModel.sectors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DefaultMainAppBar(
            navigationHelper = navigationHelper
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sectors) { sector ->
                    SectorCard(
                        sector = sector,
                        onDelete = { viewModel.deleteSector(sector) },
                        onEdit = { name, providerName ->
                            viewModel.updateSector(sector.copy(name = name, providerName = providerName))
                        }
                    )
                }
            }
        }
        
        AddSectorButton(
            onAddSector = { name, providerName ->
                viewModel.addSector(name, providerName)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorCard(
    sector: Sector,
    onDelete: () -> Unit,
    onEdit: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sector.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Provider: ${sector.providerName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row {
                    IconButton(onClick = { showEditDialog = true }) {
                        Text("✏️", style = MaterialTheme.typography.bodyLarge)
                    }
                    IconButton(onClick = onDelete) {
                        Text("×", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
    
    if (showEditDialog) {
        EditSectorDialog(
            sector = sector,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, providerName ->
                onEdit(name, providerName)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSectorButton(
    onAddSector: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Button(
        onClick = { showDialog = true },
        modifier = modifier.padding(16.dp)
    ) {
        Text(stringResource(R.string.add_sector))
    }
    
    if (showDialog) {
        AddSectorDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, providerName ->
                onAddSector(name, providerName)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSectorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var providerName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_new_sector)) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.sector_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = providerName,
                    onValueChange = { providerName = it },
                    label = { Text(stringResource(R.string.provider_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && providerName.isNotBlank()) {
                        onConfirm(name, providerName)
                    }
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSectorDialog(
    sector: Sector,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(sector.name) }
    var providerName by remember { mutableStateOf(sector.providerName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_sector)) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.sector_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = providerName,
                    onValueChange = { providerName = it },
                    label = { Text(stringResource(R.string.provider_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && providerName.isNotBlank()) {
                        onConfirm(name, providerName)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 