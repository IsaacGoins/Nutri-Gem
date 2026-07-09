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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

class MainViewModel(
    private val repository: HealthRepository,
    private val secureStorage: SecureStorage,
    private val geminiClient: GeminiClient = GeminiClient()
) : ViewModel() {

    private val _apiKey = MutableStateFlow(secureStorage.getApiKey() ?: "")
    val apiKey: StateFlow<String> = _apiKey

    private val _fdaApiKey = MutableStateFlow(secureStorage.getFdaApiKey() ?: "")
    val fdaApiKey: StateFlow<String> = _fdaApiKey

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
    val allWeight = repository.getAllWeight().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _geminiState = MutableStateFlow<GeminiState>(GeminiState.Idle)
    val geminiState: StateFlow<GeminiState> = _geminiState

    fun saveApiKey(key: String) {
        secureStorage.saveApiKey(key)
        _apiKey.value = key
    }

    fun saveFdaApiKey(key: String) {
        secureStorage.saveFdaApiKey(key)
        _fdaApiKey.value = key
    }

    fun addWater(amountOz: Int) {
        viewModelScope.launch {
            repository.addWater(WaterEntity(amountOz = amountOz, timestamp = System.currentTimeMillis()))
        }
    }

    fun deleteWater(water: WaterEntity) {
        viewModelScope.launch {
            repository.deleteWater(water)
        }
    }

    fun addWeight(weightLbs: Float) {
        viewModelScope.launch {
            repository.addWeight(com.example.calorietracker.data.local.WeightEntity(weightLbs = weightLbs, timestamp = System.currentTimeMillis()))
        }
    }

    fun deleteWeight(weight: com.example.calorietracker.data.local.WeightEntity) {
        viewModelScope.launch {
            repository.deleteWeight(weight)
        }
    }

    fun updateMeal(meal: MealEntity) {
        viewModelScope.launch {
            repository.updateMeal(meal)
        }
    }

    fun deleteMeal(meal: MealEntity) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
        }
    }

    private val fdaClient = com.example.calorietracker.data.network.FdaClient()

    fun analyzeMeal(
        prompt: String, 
        manualItems: List<com.example.calorietracker.data.network.GeminiItem> = emptyList(),
        aiItemNames: List<String> = emptyList()
    ) {
        val key = _apiKey.value
        val fdaKey = _fdaApiKey.value
        if (key.isBlank()) {
            _geminiState.value = GeminiState.Error("API Key is missing. Please set it in Settings.")
            return
        }

        _geminiState.value = GeminiState.Loading
        viewModelScope.launch {
            try {
                var finalPrompt = prompt
                
                // Cross-reference with FDA if API key is set
                if (fdaKey.isNotBlank() && aiItemNames.isNotEmpty()) {
                    val fdaContexts = aiItemNames.mapNotNull { fdaClient.searchFood(fdaKey, it) }
                    if (fdaContexts.isNotEmpty()) {
                        finalPrompt += "\n\nCRITICAL CONTEXT: Use the following FDA FoodData Central API data to make your macros highly accurate:\n" + fdaContexts.joinToString("\n")
                    }
                }

                val response = geminiClient.analyzeMeal(key, finalPrompt)
                if (response.status == "success" && response.data != null) {
                    val combinedItems = response.data.items + manualItems
                    val combinedData = response.data.copy(
                        items = combinedItems,
                        total_calories = combinedItems.sumOf { it.calories },
                        macros = com.example.calorietracker.data.network.GeminiMacros(
                            protein_g = combinedItems.sumOf { it.protein_g },
                            carbs_g = combinedItems.sumOf { it.carbs_g },
                            fat_g = combinedItems.sumOf { it.fat_g }
                        )
                    )
                    _geminiState.value = GeminiState.Success(response.copy(data = combinedData))
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

    suspend fun analyzeSingleItem(prompt: String): List<com.example.calorietracker.data.network.GeminiItem> {
        val key = _apiKey.value
        if (key.isBlank()) return emptyList()
        return try {
            val response = geminiClient.analyzeMeal(key, prompt)
            response.data?.items ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveMeal(response: GeminiResponse) {
        viewModelScope.launch {
            response.data?.let {
                val itemsJson = Json.encodeToString(it.items)
                val meal = MealEntity(
                    name = it.meal_name,
                    calories = it.total_calories,
                    proteinG = it.macros.protein_g,
                    carbsG = it.macros.carbs_g,
                    fatG = it.macros.fat_g,
                    timestamp = System.currentTimeMillis(),
                    itemsJson = itemsJson
                )
                repository.addMeal(meal)
                _geminiState.value = GeminiState.Idle
            }
        }
    }

    fun resetGeminiState() {
        _geminiState.value = GeminiState.Idle
    }

    fun setGeminiSuccessState(data: com.example.calorietracker.data.network.GeminiData) {
        _geminiState.value = GeminiState.Success(
            com.example.calorietracker.data.network.GeminiResponse(
                status = "success",
                data = data
            )
        )
    }
}

sealed class GeminiState {
    object Idle : GeminiState()
    object Loading : GeminiState()
    data class Success(val response: GeminiResponse) : GeminiState()
    data class NeedsClarification(val question: String) : GeminiState()
    data class Error(val message: String) : GeminiState()
}
