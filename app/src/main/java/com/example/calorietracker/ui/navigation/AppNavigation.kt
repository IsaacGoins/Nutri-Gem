package com.example.calorietracker.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calorietracker.ui.screens.*
import com.example.calorietracker.ui.viewmodels.MainViewModel
import com.example.calorietracker.ui.viewmodels.MainViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(application)
    )

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToMeals = { navController.navigate("meals") },
                onNavigateToWater = { navController.navigate("water") },
                onNavigateToAddMeal = { navController.navigate("add_meal") }
            )
        }
        composable("settings") {
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable("meals") {
            MealDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable("water") {
            WaterDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable("add_meal") {
            AddMealScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaveComplete = { navController.popBackStack() }
            )
        }
    }
}
