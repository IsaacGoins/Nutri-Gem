package com.example.calorietracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState
import com.example.calorietracker.ui.viewmodels.MainViewModel

object AppColors {
    @Composable
    fun getColorForToken(token: String): Color {
        return when (token) {
            "Primary" -> MaterialTheme.colorScheme.primary
            "OnPrimary" -> MaterialTheme.colorScheme.onPrimary
            "PrimaryContainer" -> MaterialTheme.colorScheme.primaryContainer
            "OnPrimaryContainer" -> MaterialTheme.colorScheme.onPrimaryContainer
            "Secondary" -> MaterialTheme.colorScheme.secondary
            "OnSecondary" -> MaterialTheme.colorScheme.onSecondary
            "SecondaryContainer" -> MaterialTheme.colorScheme.secondaryContainer
            "OnSecondaryContainer" -> MaterialTheme.colorScheme.onSecondaryContainer
            "Tertiary" -> MaterialTheme.colorScheme.tertiary
            "OnTertiary" -> MaterialTheme.colorScheme.onTertiary
            "TertiaryContainer" -> MaterialTheme.colorScheme.tertiaryContainer
            "OnTertiaryContainer" -> MaterialTheme.colorScheme.onTertiaryContainer
            "Error" -> MaterialTheme.colorScheme.error
            "OnError" -> MaterialTheme.colorScheme.onError
            "ErrorContainer" -> MaterialTheme.colorScheme.errorContainer
            "OnErrorContainer" -> MaterialTheme.colorScheme.onErrorContainer
            "Background" -> MaterialTheme.colorScheme.background
            "OnBackground" -> MaterialTheme.colorScheme.onBackground
            "Surface" -> MaterialTheme.colorScheme.surface
            "OnSurface" -> MaterialTheme.colorScheme.onSurface
            "SurfaceVariant" -> MaterialTheme.colorScheme.surfaceVariant
            "OnSurfaceVariant" -> MaterialTheme.colorScheme.onSurfaceVariant
            "Outline" -> MaterialTheme.colorScheme.outline
            "InverseOnSurface" -> MaterialTheme.colorScheme.inverseOnSurface
            "InverseSurface" -> MaterialTheme.colorScheme.inverseSurface
            "InversePrimary" -> MaterialTheme.colorScheme.inversePrimary
            else -> MaterialTheme.colorScheme.primary
        }
    }

    val colorOptions = listOf(
        "Primary", "OnPrimary", "PrimaryContainer", "OnPrimaryContainer",
        "Secondary", "OnSecondary", "SecondaryContainer", "OnSecondaryContainer",
        "Tertiary", "OnTertiary", "TertiaryContainer", "OnTertiaryContainer",
        "Error", "OnError", "ErrorContainer", "OnErrorContainer",
        "Background", "OnBackground", "Surface", "OnSurface",
        "SurfaceVariant", "OnSurfaceVariant", "Outline", "InverseOnSurface",
        "InverseSurface", "InversePrimary"
    )

    @Composable
    fun getScoreBannerColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_SCORE_BANNER"] ?: "TertiaryContainer")
    }

    @Composable
    fun getMealBannerColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_MEAL_BANNER"] ?: "SecondaryContainer")
    }

    @Composable
    fun getWaterBannerColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_WATER_BANNER"] ?: "PrimaryContainer")
    }

    @Composable
    fun getWeightBannerColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_WEIGHT_BANNER"] ?: "Secondary")
    }

    @Composable
    fun getPrimaryButtonColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_PRIMARY_BUTTON"] ?: "Primary")
    }

    @Composable
    fun getSecondaryButtonColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_SECONDARY_BUTTON"] ?: "Secondary")
    }

    @Composable
    fun getCardBackgroundColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_CARD_BACKGROUND"] ?: "SurfaceVariant")
    }

    @Composable
    fun getGraphHighlightColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_GRAPH_HIGHLIGHT"] ?: "Primary")
    }

    @Composable
    fun getMacroWheelEmptyColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_MACRO_WHEEL_EMPTY"] ?: "Outline")
    }

    @Composable
    fun getScoreWheelOverallColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_SCORE_WHEEL_OVERALL"] ?: "Primary")
    }

    @Composable
    fun getScoreWheelCategoryColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_SCORE_WHEEL_CATEGORY"] ?: "Tertiary")
    }
    
    @Composable
    fun getScoreHistoryLineColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_SCORE_HISTORY_LINE"] ?: "Tertiary")
    }

    @Composable
    fun getWaterGraphBarColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_WATER_GRAPH_BAR"] ?: "Primary")
    }

    @Composable
    fun getWeightGraphLineColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_WEIGHT_GRAPH_LINE"] ?: "Primary")
    }

    @Composable
    fun getSwipeDeleteBackgroundColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_SWIPE_DELETE_BACKGROUND"] ?: "Error")
    }
    
    @Composable
    fun getSwipeEditBackgroundColor(viewModel: MainViewModel): Color {
        val prefs = viewModel.colorPreferences.collectAsState().value
        return getColorForToken(prefs["COLOR_SWIPE_EDIT_BACKGROUND"] ?: "Primary")
    }
}
