package com.example.calorietracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorietracker.data.local.MealEntity
import com.example.calorietracker.data.local.WaterEntity
import com.example.calorietracker.data.network.GeminiClient
import com.example.calorietracker.data.network.GeminiResponse
import com.example.calorietracker.data.repository.HealthRepository
import com.example.calorietracker.data.security.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(
    private val repository: HealthRepository,
    private val secureStorage: SecureStorage,
    private val geminiClient: GeminiClient = GeminiClient()
) : ViewModel() {

    private val _apiKey = MutableStateFlow(secureStorage.getApiKey() ?: "")
    val apiKey: StateFlow<String> = _apiKey

    private val startOfDay: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

    val caloriesForDay = repository.getCaloriesForDay(startOfDay).stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val proteinForDay = repository.getProteinForDay(startOfDay).stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val carbsForDay = repository.getCarbsForDay(startOfDay).stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val fatForDay = repository.getFatForDay(startOfDay).stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val waterForDay = repository.getWaterForDay(startOfDay).stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val allMeals = repository.getAllMeals().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allWater = repository.getAllWater().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _geminiState = MutableStateFlow<GeminiState>(GeminiState.Idle)
    val geminiState: StateFlow<GeminiState> = _geminiState

    fun saveApiKey(key: String) {
        secureStorage.saveApiKey(key)
        _apiKey.value = key
    }

    fun addWater(amountOz: Int) {
        viewModelScope.launch {
            repository.addWater(WaterEntity(amountOz = amountOz, timestamp = System.currentTimeMillis()))
        }
    }

    fun analyzeMeal(prompt: String) {
        val key = _apiKey.value
        if (key.isBlank()) {
            _geminiState.value = GeminiState.Error("API Key is missing. Please set it in Settings.")
            return
        }

        _geminiState.value = GeminiState.Loading
        viewModelScope.launch {
            try {
                val response = geminiClient.analyzeMeal(key, prompt)
                if (response.status == "success" && response.data != null) {
                    _geminiState.value = GeminiState.Success(response)
                } else if (response.status == "needs_clarification") {
                    _geminiState.value = GeminiState.NeedsClarification(response.clarification_question ?: "Please provide more details.")
                } else {
                    _geminiState.value = GeminiState.Error("Unexpected response format.")
                }
            } catch (e: Exception) {
                _geminiState.value = GeminiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun saveMeal(response: GeminiResponse) {
        viewModelScope.launch {
            response.data?.let {
                val meal = MealEntity(
                    name = it.meal_name,
                    calories = it.total_calories,
                    proteinG = it.macros.protein_g,
                    carbsG = it.macros.carbs_g,
                    fatG = it.macros.fat_g,
                    timestamp = System.currentTimeMillis()
                )
                repository.addMeal(meal)
                _geminiState.value = GeminiState.Idle
            }
        }
    }

    fun resetGeminiState() {
        _geminiState.value = GeminiState.Idle
    }
}

sealed class GeminiState {
    object Idle : GeminiState()
    object Loading : GeminiState()
    data class Success(val response: GeminiResponse) : GeminiState()
    data class NeedsClarification(val question: String) : GeminiState()
    data class Error(val message: String) : GeminiState()
}
