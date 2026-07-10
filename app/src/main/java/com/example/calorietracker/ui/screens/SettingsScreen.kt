package com.example.calorietracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calorietracker.ui.viewmodels.MainViewModel
import com.example.calorietracker.ui.theme.AppColors

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
                .verticalScroll(rememberScrollState())
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
            val primaryBtnColor = AppColors.getPrimaryButtonColor(viewModel)
            Button(
                onClick = { 
                    viewModel.saveAge(inputAge.toIntOrNull() ?: 30)
                    viewModel.saveHeight(inputHeight.toIntOrNull() ?: 68)
                    viewModel.saveWorkoutsPerWeek(selectedDays.size)
                    viewModel.saveActiveDays(selectedDays.joinToString(","))
                    viewModel.saveProfileNotes(inputNotes)
                    Toast.makeText(context, "Profile has been saved", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBtnColor, contentColor = contentColorFor(primaryBtnColor))
            ) {
                Text("Save Profile")
            }
            Spacer(modifier = Modifier.height(32.dp))
            
            val devEnabled by viewModel.devSettingsEnabled.collectAsState()
            Row(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.saveDevSettingsEnabled(!devEnabled) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = devEnabled, onCheckedChange = { viewModel.saveDevSettingsEnabled(it) })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enable Developer Settings", style = MaterialTheme.typography.bodyMedium)
            }
            
            if (devEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Color Remapping", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                val prefs by viewModel.colorPreferences.collectAsState()
                
                val colorCategories = mapOf(
                    "Banners & Backgrounds" to listOf(
                        "COLOR_HERO_BANNER" to "Hero Banner",
                        "COLOR_SCORE_BANNER" to "Score Banner & Graph",
                        "COLOR_MEAL_BANNER" to "Meal Banner",
                        "COLOR_WATER_BANNER" to "Water Banner & Graph",
                        "COLOR_WEIGHT_BANNER" to "Weight Banner & Graph",
                        "COLOR_CARD_BACKGROUND" to "Card Background"
                    ),
                    "Buttons & Interactions" to listOf(
                        "COLOR_PRIMARY_BUTTON" to "Primary Button",
                        "COLOR_SECONDARY_BUTTON" to "Secondary Button",
                        "COLOR_SWIPE_DELETE_BACKGROUND" to "Swipe Delete Background"
                    ),
                    "Graphs & Data Visualization" to listOf(
                        "COLOR_GRAPH_HIGHLIGHT" to "Graph Highlight",
                        "COLOR_MACRO_WHEEL_EMPTY" to "Macro Wheel Empty",
                        "COLOR_SCORE_WHEEL_OVERALL" to "Score Wheel Overall",
                        "COLOR_SCORE_WHEEL_CATEGORY" to "Score Wheel Category",
                        "COLOR_SCORE_HISTORY_LINE" to "Score History Line",
                        "COLOR_WATER_GRAPH_BAR" to "Water Graph Bar",
                        "COLOR_WEIGHT_GRAPH_LINE" to "Weight Graph Line"
                    )
                )
                
                colorCategories.forEach { (categoryName, keys) ->
                    Text(categoryName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp))
                    
                    keys.forEach { (prefKey, label) ->
                    var expanded by remember { mutableStateOf(false) }
                    val currentSelection = prefs[prefKey] ?: MainViewModel.DEFAULT_TOKENS[prefKey] ?: "Primary"
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        
                        Box(modifier = Modifier.size(24.dp).clip(androidx.compose.foundation.shape.CircleShape).background(AppColors.getColorForToken(currentSelection)))
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text(currentSelection)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                AppColors.colorOptions.forEach { token ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(16.dp).clip(androidx.compose.foundation.shape.CircleShape).background(AppColors.getColorForToken(token)))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(token) 
                                            }
                                        },
                                        onClick = {
                                            viewModel.saveColorPreference(prefKey, token)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.resetColorPreferences() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset Developer Settings to Defaults")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
}
