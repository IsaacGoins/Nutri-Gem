package com.example.calorietracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calorietracker.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToAddMeal: () -> Unit
) {
    val calories by viewModel.caloriesForDay.collectAsState()
    val protein by viewModel.proteinForDay.collectAsState()
    val carbs by viewModel.carbsForDay.collectAsState()
    val fat by viewModel.fatForDay.collectAsState()
    val water by viewModel.waterForDay.collectAsState()

    var waterDialExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            Row(verticalAlignment = Alignment.Bottom) {
                // Water Speed Dial using custom Compose state since standard API might require specific imports
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (waterDialExpanded) {
                        SmallFloatingActionButton(onClick = { viewModel.addWater(8); waterDialExpanded = false }) {
                            Text("+8oz")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SmallFloatingActionButton(onClick = { viewModel.addWater(16); waterDialExpanded = false }) {
                            Text("+16oz")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SmallFloatingActionButton(onClick = { /* TODO Custom Dialog */ waterDialExpanded = false }) {
                            Text("...")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    FloatingActionButton(onClick = { waterDialExpanded = !waterDialExpanded }) {
                        Icon(Icons.Default.LocalDrink, contentDescription = "Add Water")
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                FloatingActionButton(onClick = onNavigateToAddMeal, containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Icon(Icons.Default.Add, contentDescription = "Add Meal")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroSection(
                calories = calories ?: 0,
                protein = protein ?: 0,
                carbs = carbs ?: 0,
                fat = fat ?: 0,
                onClick = onNavigateToMeals
            )

            WaterBanner(water = water ?: 0, onClick = onNavigateToWater)
        }
    }
}

@Composable
fun HeroSection(calories: Int, protein: Int, carbs: Int, fat: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Daily Macros", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val stroke = Stroke(width = 30f, cap = StrokeCap.Round)
                    val totalGrams = (protein + carbs + fat).toFloat()
                    
                    if (totalGrams == 0f) {
                        drawArc(Color.LightGray, 0f, 360f, false, style = stroke)
                    } else {
                        val proteinAngle = (protein / totalGrams) * 360f
                        val carbsAngle = (carbs / totalGrams) * 360f
                        val fatAngle = (fat / totalGrams) * 360f
                        
                        var startAngle = -90f
                        
                        if (protein > 0) {
                            drawArc(Color.Red, startAngle, proteinAngle, false, style = stroke)
                            startAngle += proteinAngle
                        }
                        if (carbs > 0) {
                            drawArc(Color.Green, startAngle, carbsAngle, false, style = stroke)
                            startAngle += carbsAngle
                        }
                        if (fat > 0) {
                            drawArc(Color.Blue, startAngle, fatAngle, false, style = stroke)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$calories", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("kcal", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MacroLegend(color = Color.Red, label = "Protein", amount = "${protein}g")
                MacroLegend(color = Color.Green, label = "Carbs", amount = "${carbs}g")
                MacroLegend(color = Color.Blue, label = "Fat", amount = "${fat}g")
            }
        }
    }
}

@Composable
fun MacroLegend(color: Color, label: String, amount: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
        Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WaterBanner(water: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Water Intake", style = MaterialTheme.typography.titleMedium)
                Text("$water oz", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Default.LocalDrink, contentDescription = null, modifier = Modifier.size(48.dp))
        }
    }
}
