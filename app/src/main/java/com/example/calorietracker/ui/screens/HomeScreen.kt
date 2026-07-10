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
import androidx.compose.material.icons.filled.MonitorWeight
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
import com.example.calorietracker.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToWeight: () -> Unit,
    onNavigateToAddMeal: () -> Unit,
    onNavigateToScore: () -> Unit
) {
    val calories by viewModel.caloriesForDay.collectAsState()
    val protein by viewModel.proteinForDay.collectAsState()
    val carbs by viewModel.carbsForDay.collectAsState()
    val fat by viewModel.fatForDay.collectAsState()
    val water by viewModel.waterForDay.collectAsState()
    
    val weightLogs by viewModel.allWeight.collectAsState()
    val currentWeight = weightLogs.firstOrNull()?.weightLbs ?: 0f

    val scores by viewModel.allScores.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.generateScoreForYesterday()
    }

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
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Score Banner
                if (scores.isNotEmpty()) {
                    val latestScore = scores.first()
                    ScoreBanner(score = latestScore.overallScore, backgroundColor = AppColors.getScoreBannerColor(viewModel), onClick = onNavigateToScore)
                }

                HeroSection(
                    calories = calories ?: 0,
                    protein = protein ?: 0,
                    carbs = carbs ?: 0,
                    fat = fat ?: 0,
                    backgroundColor = AppColors.getHeroBannerColor(viewModel),
                    viewModel = viewModel,
                    onClick = onNavigateToMeals
                )

                WaterBanner(water = water ?: 0, backgroundColor = AppColors.getWaterBannerColor(viewModel), onClick = onNavigateToWater)
                
                WeightBanner(weight = currentWeight, backgroundColor = AppColors.getWeightBannerColor(viewModel), onClick = onNavigateToWeight)
            }
        }
    }
}

@Composable
fun ScoreBanner(score: Int, backgroundColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Yesterday's Score", style = MaterialTheme.typography.titleMedium, color = contentColorFor(backgroundColor = backgroundColor))
                Text("View Insights", style = MaterialTheme.typography.bodyMedium, color = contentColorFor(backgroundColor = backgroundColor).copy(alpha = 0.7f))
            }
            Text("$score/100", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = contentColorFor(backgroundColor = backgroundColor))
        }
    }
}


@Composable
fun HeroSection(calories: Int, protein: Int, carbs: Int, fat: Int, backgroundColor: Color, viewModel: MainViewModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Daily Macros", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            val emptyColor = AppColors.getMacroWheelEmptyColor(viewModel)
            val proteinColor = AppColors.getMacroProteinColor(viewModel)
            val carbsColor = AppColors.getMacroCarbsColor(viewModel)
            val fatColor = AppColors.getMacroFatColor(viewModel)

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val stroke = Stroke(width = 30f, cap = StrokeCap.Round)
                    val totalGrams = (protein + carbs + fat).toFloat()
                    
                    if (totalGrams == 0f) {
                        drawArc(emptyColor, 0f, 360f, false, style = stroke)
                    } else {
                        val proteinAngle = (protein / totalGrams) * 360f
                        val carbsAngle = (carbs / totalGrams) * 360f
                        val fatAngle = (fat / totalGrams) * 360f
                        
                        var startAngle = -90f
                        
                        if (protein > 0) {
                            drawArc(proteinColor, startAngle, proteinAngle, false, style = stroke)
                            startAngle += proteinAngle
                        }
                        if (carbs > 0) {
                            drawArc(carbsColor, startAngle, carbsAngle, false, style = stroke)
                            startAngle += carbsAngle
                        }
                        if (fat > 0) {
                            drawArc(fatColor, startAngle, fatAngle, false, style = stroke)
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
                MacroLegend(color = Color(0xFFEF5350), label = "Protein", amount = "${protein}g")
                MacroLegend(color = Color(0xFF66BB6A), label = "Carbs", amount = "${carbs}g")
                MacroLegend(color = Color(0xFF42A5F5), label = "Fat", amount = "${fat}g")
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
fun WaterBanner(water: Int, backgroundColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColorFor(backgroundColor = backgroundColor)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Water Intake", style = MaterialTheme.typography.titleMedium)
                Text("$water oz", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Default.LocalDrink, contentDescription = null, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun WeightBanner(weight: Float, backgroundColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColorFor(backgroundColor = backgroundColor)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Weight", style = MaterialTheme.typography.titleMedium)
                val weightStr = if (weight > 0f) "$weight lbs" else "-- lbs"
                Text(weightStr, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Default.MonitorWeight, contentDescription = null, modifier = Modifier.size(48.dp))
        }
    }
}
