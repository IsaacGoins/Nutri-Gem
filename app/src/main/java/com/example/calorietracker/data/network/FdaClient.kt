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

class FdaClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun searchFood(apiKey: String, query: String): String? = withContext(Dispatchers.IO) {
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
                
                // Format the nutrients nicely for Gemini to save tokens
                val nutrientsText = firstFood.foodNutrients.joinToString(", ") { 
                    "${it.nutrientName}: ${it.value}${it.unitName}"
                }
                
                return@withContext "FDA Data for '${firstFood.description}': $nutrientsText"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
