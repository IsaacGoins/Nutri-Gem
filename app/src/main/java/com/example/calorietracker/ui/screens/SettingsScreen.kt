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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val apiKey by viewModel.apiKey.collectAsState()
    var inputKey by remember { mutableStateOf(apiKey) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

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
            OutlinedButton(
                onClick = { 
                    viewModel.saveApiKey(inputKey) 
                    Toast.makeText(context, "Gemini Key has been saved", Toast.LENGTH_SHORT).show()
                },
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
            OutlinedButton(
                onClick = { 
                    viewModel.saveFdaApiKey(inputFdaKey) 
                    Toast.makeText(context, "FDA Key has been saved", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save FDA Key")
            }

            Spacer(modifier = Modifier.height(32.dp))

            val age by viewModel.age.collectAsState()
            val height by viewModel.heightInches.collectAsState()
            val activeDays by viewModel.activeDays.collectAsState()
            val profileNotes by viewModel.profileNotes.collectAsState()

            var inputAge by remember { mutableStateOf(age.toString()) }
            var inputHeight by remember { mutableStateOf(height.toString()) }
            var inputNotes by remember { mutableStateOf(profileNotes) }
            
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            var selectedDays by remember { 
                mutableStateOf(
                    activeDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                )
            }

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
            Spacer(modifier = Modifier.height(16.dp))
            Text("Active Days", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEach { day ->
                    val isSelected = selectedDays.contains(day)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                            }
                    ) {
                        Text(
                            text = day.take(1),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inputNotes,
                onValueChange = { inputNotes = it },
                label = { Text("Notes / Context for AI (e.g. Intermittent fasting)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    viewModel.saveAge(inputAge.toIntOrNull() ?: 30)
                    viewModel.saveHeight(inputHeight.toIntOrNull() ?: 68)
                    viewModel.saveWorkoutsPerWeek(selectedDays.size)
                    viewModel.saveActiveDays(selectedDays.joinToString(","))
                    viewModel.saveProfileNotes(inputNotes)
                    Toast.makeText(context, "Profile has been saved", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }
        }
    }
}
