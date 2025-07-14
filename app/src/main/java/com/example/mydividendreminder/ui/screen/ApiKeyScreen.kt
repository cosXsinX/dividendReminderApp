package com.example.mydividendreminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mydividendreminder.ui.viewmodel.ApiKeyViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.example.mydividendreminder.R
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyScreen(
    viewModel: ApiKeyViewModel,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit
) {
    val openAiKey by viewModel.openAiKey.collectAsState()
    val geminiKey by viewModel.geminiKey.collectAsState()
    var openAiInput by remember { mutableStateOf(openAiKey) }
    var geminiInput by remember { mutableStateOf(geminiKey) }

    LaunchedEffect(openAiKey) { openAiInput = openAiKey }
    LaunchedEffect(geminiKey) { geminiInput = geminiKey }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(start = 4.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "API Keys",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlinedTextField(
                value = openAiInput,
                onValueChange = { openAiInput = it },
                label = { Text("OpenAI API Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.saveOpenAiKey(openAiInput) },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Save OpenAI Key") }

            val openAiModel by viewModel.openAiModel.collectAsState()
            val modelOptions = listOf("gpt-4o", "o3", "o4-mini", "o4-mini-high", "gpt-4.5", "gpt-4.1", "gpt-4.1-mini")
            var expanded by remember { mutableStateOf(false) }
            var selectedModel by remember { mutableStateOf(openAiModel.ifBlank { modelOptions.first() }) }
            LaunchedEffect(openAiModel) { selectedModel = openAiModel.ifBlank { modelOptions.first() } }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("OpenAI Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    modelOptions.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                selectedModel = model
                                viewModel.saveOpenAiModel(model)
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = geminiInput,
                onValueChange = { geminiInput = it },
                label = { Text("Gemini API Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.saveGeminiKey(geminiInput) },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Save Gemini Key") }
        }
    }
} 