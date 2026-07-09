package com.example.calorietracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calorietracker.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val meals by viewModel.allMeals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal History & Analytics") },
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
        ) {
            // Analytics Placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                if (meals.isEmpty()) {
                    Box(contentAlignment = androidx.compose.ui.Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("No meal data yet", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        val recentMeals = meals.take(7).reversed()
                        val maxCalories = recentMeals.maxOfOrNull { it.calories }?.toFloat() ?: 1f
                        val xStep = if (recentMeals.size > 1) size.width / (recentMeals.size - 1) else size.width / 2f
                        
                        val path = androidx.compose.ui.graphics.Path()

                        recentMeals.forEachIndexed { index, meal ->
                            val x = if (recentMeals.size == 1) size.width / 2f else index * xStep
                            val y = size.height - ((meal.calories / maxCalories) * size.height)
                            
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        
                        if (recentMeals.size > 1) {
                            drawPath(
                                path = path,
                                color = androidx.compose.ui.graphics.Color(0xFFFF5252), // Red Accent
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 8f, 
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round, 
                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                )
                            )
                        }
                        
                        recentMeals.forEachIndexed { index, meal ->
                            val x = if (recentMeals.size == 1) size.width / 2f else index * xStep
                            val y = size.height - ((meal.calories / maxCalories) * size.height)
                            drawCircle(
                                color = androidx.compose.ui.graphics.Color.White,
                                radius = 16f,
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                            drawCircle(
                                color = androidx.compose.ui.graphics.Color(0xFFFF5252),
                                radius = 10f,
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                        }
                    }
                }
            }
            Text(
                "Meal Log",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            val df = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(meals) { meal ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(meal.name, style = MaterialTheme.typography.titleMedium)
                            Text(df.format(Date(meal.timestamp)), style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${meal.calories} kcal | ${meal.proteinG}g P | ${meal.carbsG}g C | ${meal.fatG}g F")
                        }
                    }
                }
            }
        }
    }
}
