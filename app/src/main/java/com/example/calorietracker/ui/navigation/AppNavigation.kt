package com.example.calorietracker.ui.navigation

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calorietracker.ui.screens.*
import com.example.calorietracker.ui.viewmodels.MainViewModel
import com.example.calorietracker.ui.viewmodels.MainViewModelFactory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(application)
    )

    var waterDialExpanded by remember { mutableStateOf(false) }
    var showCustomWaterDialog by remember { mutableStateOf(false) }
    var customWaterAmount by remember { mutableStateOf("") }
    var isEditingMeal by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (showCustomWaterDialog) {
        AlertDialog(
            onDismissRequest = { showCustomWaterDialog = false },
            title = { Text("Custom Amount") },
            text = {
                OutlinedTextField(
                    value = customWaterAmount,
                    onValueChange = { customWaterAmount = it },
                    label = { Text("Amount (oz)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = customWaterAmount.toIntOrNull()
                    if (amount != null && amount > 0) {
                        viewModel.addWater(amount)
                    }
                    showCustomWaterDialog = false
                    customWaterAmount = ""
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomWaterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToMeals = { navController.navigate("meals") },
                    onNavigateToWater = { navController.navigate("water") },
                    onNavigateToWeight = { navController.navigate("weight_detail") },
                    onNavigateToAddMeal = { navController.navigate("add_meal") },
                    onNavigateToScore = { navController.navigate("score_detail") }
                )
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("meals") {
                MealDetailScreen(
                    viewModel = viewModel, 
                    onBack = { navController.popBackStack() },
                    onEditingChange = { isEditingMeal = it }
                )
            }
            composable("water") {
                WaterDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("weight_detail") {
                WeightDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("add_meal") {
                AddMealScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaveComplete = { navController.popBackStack() }
                )
            }
            composable("score_detail") {
                ScoreDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        
        // Global FABs Overlay
        if (currentRoute != "add_meal" && !isEditingMeal && currentRoute != "weight_detail" && currentRoute != "settings") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                // Water FAB Menu
                if (currentRoute == "home" || currentRoute == "water") {
                Column(
                    modifier = Modifier.align(Alignment.BottomStart),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = waterDialExpanded
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SmallFloatingActionButton(
                                onClick = { showCustomWaterDialog = true; waterDialExpanded = false },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text("C", style = MaterialTheme.typography.labelMedium)
                            }
                            SmallFloatingActionButton(
                                onClick = { viewModel.addWater(16); waterDialExpanded = false },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text("16", style = MaterialTheme.typography.labelMedium)
                            }
                            SmallFloatingActionButton(
                                onClick = { viewModel.addWater(8); waterDialExpanded = false },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text("8", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    
                    FloatingActionButton(
                        onClick = { waterDialExpanded = !waterDialExpanded },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.LocalDrink, contentDescription = "Add Water")
                    }
                } // This closes the Column
                } // This closes the if (currentRoute == "home" || currentRoute == "water")

                // Add Meal FAB
                if (currentRoute == "home" || currentRoute == "meals") {
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate("add_meal") },
                        modifier = Modifier.align(Alignment.BottomEnd),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Add Meal") }
                    )
                }
            }
        }
    }
}
