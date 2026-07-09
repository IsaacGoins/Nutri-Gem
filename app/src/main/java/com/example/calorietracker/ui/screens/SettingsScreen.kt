package com.example.calorietracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calorietracker.ui.viewmodels.MainViewModel

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val apiKey by viewModel.apiKey.collectAsState()
    var inputKey by remember { mutableStateOf(apiKey) }
    val focusManager = LocalFocusManager.current

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
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
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

            Spacer(modifier = Modifier.height(32.dp))

            val age by viewModel.age.collectAsState()
            val height by viewModel.heightInches.collectAsState()
            val workouts by viewModel.workoutsPerWeek.collectAsState()
            val activeDays by viewModel.activeDays.collectAsState()

            var inputAge by remember { mutableStateOf(age.toString()) }
            var inputHeight by remember { mutableStateOf(height.toString()) }
            var inputWorkouts by remember { mutableStateOf(workouts.toString()) }
            var inputActiveDays by remember { mutableStateOf(activeDays) }

            Text("Personal Profile (For AI Scoring)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = inputAge,
                    onValueChange = { inputAge = it },
                    label = { Text("Age") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = inputHeight,
                    onValueChange = { inputHeight = it },
                    label = { Text("Height (inches)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = inputWorkouts,
                    onValueChange = { inputWorkouts = it },
                    label = { Text("Workouts / Week") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = inputActiveDays,
                    onValueChange = { inputActiveDays = it },
                    label = { Text("Active Days") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    viewModel.saveAge(inputAge.toIntOrNull() ?: 30)
                    viewModel.saveHeight(inputHeight.toIntOrNull() ?: 68)
                    viewModel.saveWorkoutsPerWeek(inputWorkouts.toIntOrNull() ?: 3)
                    viewModel.saveActiveDays(inputActiveDays)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }
        }
    }
}
