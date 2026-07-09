package com.example.calorietracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calorietracker.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val apiKey by viewModel.apiKey.collectAsState()
    var inputKey by remember { mutableStateOf(apiKey) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Gemini API Key", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = inputKey,
                onValueChange = { inputKey = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Gemini API Key") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.saveApiKey(inputKey) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Gemini Key")
            }

            Spacer(modifier = Modifier.height(32.dp))

            val fdaApiKey by viewModel.fdaApiKey.collectAsState()
            var inputFdaKey by remember { mutableStateOf(fdaApiKey) }

            Text("FDA API Key", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = inputFdaKey,
                onValueChange = { inputFdaKey = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("FDA FoodData Central API Key") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.saveFdaApiKey(inputFdaKey) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save FDA Key")
            }
        }
    }
}
