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

    private val _weightGoal = MutableStateFlow(secureStorage.getWeightGoal())
    val weightGoal: StateFlow<Float> = _weightGoal

    private val _devSettingsEnabled = MutableStateFlow(secureStorage.getDevSettingsEnabled())
    val devSettingsEnabled: StateFlow<Boolean> = _devSettingsEnabled

    private val _colorPreferences = MutableStateFlow<Map<String, String>>(emptyMap())
    val colorPreferences: StateFlow<Map<String, String>> = _colorPreferences

    init {
        loadColorPreferences()
    }

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

    val allMeals = repository.getAllMeals().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allWater = repository.getAllWater().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allWeight = repository.getAllWeight().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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

    fun saveWeightGoal(weight: Float) {
        secureStorage.saveWeightGoal(weight)
        _weightGoal.value = weight
    }

    fun saveDevSettingsEnabled(enabled: Boolean) {
        secureStorage.saveDevSettingsEnabled(enabled)
        _devSettingsEnabled.value = enabled
    }

    companion object {
        val DEFAULT_TOKENS = mapOf(
            "COLOR_SCORE_BANNER" to "TertiaryContainer",
            "COLOR_MEAL_BANNER" to "SecondaryContainer",
            "COLOR_WATER_BANNER" to "PrimaryContainer",
            "COLOR_WEIGHT_BANNER" to "Secondary",
            "COLOR_PRIMARY_BUTTON" to "Primary",
            "COLOR_SECONDARY_BUTTON" to "Secondary",
            "COLOR_CARD_BACKGROUND" to "SurfaceVariant",
            "COLOR_GRAPH_HIGHLIGHT" to "Primary",
            "COLOR_MACRO_WHEEL_EMPTY" to "Outline",
            "COLOR_SCORE_WHEEL_OVERALL" to "Primary",
            "COLOR_SCORE_WHEEL_CATEGORY" to "Tertiary",
            "COLOR_SCORE_HISTORY_LINE" to "Tertiary",
            "COLOR_WATER_GRAPH_BAR" to "OnPrimaryContainer",
            "COLOR_WEIGHT_GRAPH_LINE" to "Primary",
            "COLOR_SWIPE_DELETE_BACKGROUND" to "Error",
            "COLOR_SWIPE_EDIT_BACKGROUND" to "Primary"
        )
    }

    private fun loadColorPreferences() {
        val map = DEFAULT_TOKENS.keys.associateWith { key ->
            secureStorage.getColorPreference(key, DEFAULT_TOKENS[key] ?: "Primary")
        }
        _colorPreferences.value = map
    }

    fun saveColorPreference(key: String, token: String) {
        secureStorage.saveColorPreference(key, token)
        _colorPreferences.value = _colorPreferences.value.toMutableMap().apply { put(key, token) }
    }

    fun resetColorPreferences() {
        secureStorage.clearColorPreferences(DEFAULT_TOKENS.keys.toList())
        loadColorPreferences()
    }

    private val _age = MutableStateFlow(secureStorage.getAge())
    val age: StateFlow<Int> = _age

    private val _heightInches = MutableStateFlow(secureStorage.getHeight())
    val heightInches: StateFlow<Int> = _heightInches

    private val _workoutsPerWeek = MutableStateFlow(secureStorage.getWorkoutsPerWeek())
    val workoutsPerWeek: StateFlow<Int> = _workoutsPerWeek

    private val _activeDays = MutableStateFlow(secureStorage.getActiveDays())
    val activeDays: StateFlow<String> = _activeDays

    private val _profileNotes = MutableStateFlow(secureStorage.getProfileNotes())
    val profileNotes: StateFlow<String> = _profileNotes

    fun saveAge(age: Int) {
        secureStorage.saveAge(age)
        _age.value = age
    }

    fun saveHeight(height: Int) {
        secureStorage.saveHeight(height)
        _heightInches.value = height
    }

    fun saveWorkoutsPerWeek(count: Int) {
        secureStorage.saveWorkoutsPerWeek(count)
        _workoutsPerWeek.value = count
    }

    fun saveActiveDays(days: String) {
        secureStorage.saveActiveDays(days)
        _activeDays.value = days
    }

    fun saveProfileNotes(notes: String) {
        secureStorage.saveProfileNotes(notes)
        _profileNotes.value = notes
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

    fun updateWater(water: WaterEntity) {
        viewModelScope.launch {
            repository.updateWater(water)
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
                // Initial Gemini Request
                val response = geminiClient.analyzeMeal(key, prompt)
                
                if (response.status == "needs_clarification") {
                    _geminiState.value = GeminiState.NeedsClarification(response.clarification_question ?: "Please provide more details.")
                    return@launch
                }

                val data = response.data
                if (data != null) {
                    val compiledItems = compileItems(data, fdaKey).toMutableList()
                    compiledItems.addAll(manualItems)
                    
                    data.items = compiledItems
                    data.total_calories = compiledItems.sumOf { it.calories }
                    data.macros = com.example.calorietracker.data.network.GeminiMacros(
                        protein_g = compiledItems.sumOf { it.protein_g },
                        carbs_g = compiledItems.sumOf { it.carbs_g },
                        fat_g = compiledItems.sumOf { it.fat_g }
                    )

                    _geminiState.value = GeminiState.Success(response)
                } else {
                    _geminiState.value = GeminiState.Error("Failed to parse meal details. Try again.")
                }
            } catch (e: Exception) {
                _geminiState.value = GeminiState.Error("An error occurred: ${e.message}")
            }
        }
    }

    suspend fun analyzeSingleItem(prompt: String): List<com.example.calorietracker.data.network.GeminiItem> {
        val key = _apiKey.value
        val fdaKey = _fdaApiKey.value
        if (key.isBlank()) return emptyList()
        return try {
            val response = geminiClient.analyzeMeal(key, prompt)
            val data = response.data ?: return emptyList()
            compileItems(data, fdaKey)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun compileItems(data: com.example.calorietracker.data.network.GeminiData, fdaKey: String): List<com.example.calorietracker.data.network.GeminiItem> {
        val compiledItems = mutableListOf<com.example.calorietracker.data.network.GeminiItem>()
        var mealResolved = false
        
        // Tier 1: Full Meal FDA Search
        if (fdaKey.isNotBlank() && data.fda_search_term.isNotBlank()) {
            val fdaData = fdaClient.searchFoodStructured(fdaKey, data.fda_search_term)
            if (fdaData != null) {
                val fdaCal = fdaData.calories.takeIf { it > 0 } ?: 1
                val ratio = data.total_calories.toDouble() / fdaCal
                
                compiledItems.add(
                    com.example.calorietracker.data.network.GeminiItem(
                        name = data.meal_name,
                        calories = data.total_calories, // Use Gemini's portion-adjusted calories
                        protein_g = (fdaData.protein * ratio).toInt(),
                        carbs_g = (fdaData.carbs * ratio).toInt(),
                        fat_g = (fdaData.fat * ratio).toInt(),
                        isFdaVerified = true
                    )
                )
                mealResolved = true
            }
        }

        if (!mealResolved) {
            if (data.fallback_ingredients.isEmpty()) {
                // Single item, FDA failed, fallback to Gemini's total estimate
                compiledItems.add(
                    com.example.calorietracker.data.network.GeminiItem(
                        name = data.meal_name,
                        calories = data.total_calories,
                        protein_g = 0, // Fallback macros are rough
                        carbs_g = 0,
                        fat_g = 0,
                        isFdaVerified = false
                    )
                )
            } else {
                // Tier 2 & Tier 3: Ingredient Level
                for (ingredient in data.fallback_ingredients) {
                    var ingredientResolved = false
                    
                    // Tier 2: FDA Lookup
                    if (fdaKey.isNotBlank() && ingredient.fda_search_term.isNotBlank()) {
                        val fdaData = fdaClient.searchFoodStructured(fdaKey, ingredient.fda_search_term)
                        if (fdaData != null) {
                            val fdaCal = fdaData.calories.takeIf { it > 0 } ?: 1
                            val ratio = ingredient.estimated_macros.calories.toDouble() / fdaCal

                            compiledItems.add(
                                com.example.calorietracker.data.network.GeminiItem(
                                    name = ingredient.name,
                                    calories = ingredient.estimated_macros.calories, // Use Gemini's portion-adjusted calories
                                    protein_g = (fdaData.protein * ratio).toInt(),
                                    carbs_g = (fdaData.carbs * ratio).toInt(),
                                    fat_g = (fdaData.fat * ratio).toInt(),
                                    isFdaVerified = true
                                )
                            )
                            ingredientResolved = true
                        }
                    }
                    
                    // Tier 3: Gemini Fallback
                    if (!ingredientResolved) {
                        compiledItems.add(
                            com.example.calorietracker.data.network.GeminiItem(
                                name = ingredient.name,
                                calories = ingredient.estimated_macros.calories,
                                protein_g = ingredient.estimated_macros.protein_g,
                                carbs_g = ingredient.estimated_macros.carbs_g,
                                fat_g = ingredient.estimated_macros.fat_g,
                                isFdaVerified = false
                            )
                        )
                    }
                }
            }
        }
        return compiledItems
    }

    fun saveMeal(response: GeminiResponse, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            response.data?.let {
                val itemsJson = Json.encodeToString(it.items)
                val meal = MealEntity(
                    name = it.meal_name,
                    calories = it.total_calories,
                    proteinG = it.macros.protein_g,
                    carbsG = it.macros.carbs_g,
                    fatG = it.macros.fat_g,
                    timestamp = timestamp,
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

    val allScores = repository.getAllScores().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    private val _isScoring = MutableStateFlow(false)
    val isScoring: StateFlow<Boolean> = _isScoring
    
    fun generateScoreForYesterday(force: Boolean = false) {
        val key = _apiKey.value
        if (key.isBlank()) return
        
        viewModelScope.launch {
            _isScoring.value = true
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val startOfYesterday = calendar.timeInMillis
                
                if (!force) {
                    val existing = allScores.value.find { it.dateTimestamp == startOfYesterday }
                    if (existing != null) {
                        _isScoring.value = false
                        return@launch
                    }
                }
                
                val yesterdaysMeals = allMeals.value.filter { it.timestamp in startOfYesterday until startOfToday }
                val yesterdaysWater = allWater.value.filter { it.timestamp in startOfYesterday until startOfToday }
                
                if (yesterdaysMeals.isEmpty() && yesterdaysWater.isEmpty()) {
                    _isScoring.value = false
                    return@launch 
                }
                
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val dayBeforeYesterday = calendar.timeInMillis
                val prevScore = allScores.value.find { it.dateTimestamp == dayBeforeYesterday }
                
                val mealsJsonArray = yesterdaysMeals.joinToString(",") { 
                    "{\"name\": \"${it.name.replace("\"", "\\\"")}\", \"calories\": ${it.calories}, \"protein\": ${it.proteinG}, \"carbs\": ${it.carbsG}, \"fat\": ${it.fatG}, \"timestamp\": ${it.timestamp}}" 
                }
                
                val payloadJson = """
                {
                  "profile": { "age": ${age.value}, "heightInches": ${heightInches.value}, "weightGoalLbs": ${weightGoal.value}, "workoutsPerWeek": ${workoutsPerWeek.value}, "activeDays": "${activeDays.value}", "notes": "${profileNotes.value.replace("\"", "\\\"").replace("\n", " ")}" },
                  "yesterdaysMeals": [$mealsJsonArray],
                  "yesterdaysWaterOz": ${yesterdaysWater.sumOf { it.amountOz }},
                  "previousDayOverallScore": ${prevScore?.overallScore ?: -1}
                }
                """.trimIndent()
                
                val scoreResponse = geminiClient.generateDailyScore(key, payloadJson)
                
                val scoreEntity = com.example.calorietracker.data.local.DailyScoreEntity(
                    dateTimestamp = startOfYesterday,
                    overallScore = scoreResponse.overallScore,
                    cleanScore = scoreResponse.cleanScore,
                    macroScore = scoreResponse.macroScore,
                    calorieScore = scoreResponse.calorieScore,
                    waterScore = scoreResponse.waterScore,
                    balanceScore = scoreResponse.balanceScore,
                    feedback = scoreResponse.feedback
                )
                
                repository.addScore(scoreEntity)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScoring.value = false
            }
        }
    }
}

sealed class GeminiState {
    object Idle : GeminiState()
    object Loading : GeminiState()
    data class Success(val response: GeminiResponse) : GeminiState()
    data class NeedsClarification(val question: String) : GeminiState()
    data class Error(val message: String) : GeminiState()
}
