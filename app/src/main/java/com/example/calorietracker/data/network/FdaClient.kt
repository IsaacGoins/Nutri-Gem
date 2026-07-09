package com.example.calorietracker.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Serializable
data class FdaSearchResponse(
    val foods: List<FdaFood> = emptyList()
)

@Serializable
data class FdaFood(
    val description: String,
    val foodNutrients: List<FdaNutrient> = emptyList()
)

@Serializable
data class FdaNutrient(
    val nutrientName: String,
    val unitName: String,
    val value: Double = 0.0
)

data class FdaMacros(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

class FdaClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun searchFoodStructured(apiKey: String, query: String): FdaMacros? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || query.isBlank()) return@withContext null

        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://api.nal.usda.gov/fdc/v1/foods/search?api_key=$apiKey&query=$encodedQuery&pageSize=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<FdaSearchResponse>(responseText)
                
                val firstFood = response.foods.firstOrNull() ?: return@withContext null
                
                var calories = 0
                var protein = 0
                var carbs = 0
                var fat = 0

                firstFood.foodNutrients.forEach { nutrient ->
                    val name = nutrient.nutrientName.lowercase()
                    val value = nutrient.value.toInt()
                    if (name.contains("energy") && nutrient.unitName.equals("kcal", ignoreCase = true)) {
                        calories = value
                    } else if (name.contains("protein")) {
                        protein = value
                    } else if (name.contains("carbohydrate")) {
                        carbs = value
                    } else if (name.contains("lipid") || name.contains("fat")) {
                        fat = value
                    }
                }
                
                // If it returned 0 for everything, maybe it's not a valid match?
                if (calories == 0 && protein == 0 && carbs == 0 && fat == 0) {
                    return@withContext null
                }

                return@withContext FdaMacros(calories, protein, carbs, fat)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
