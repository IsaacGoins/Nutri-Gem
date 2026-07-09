package com.example.calorietracker.data.network

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GeminiResponse(
    val status: String,
    val clarification_question: String? = null,
    val data: GeminiData? = null
)

@Serializable
data class GeminiData(
    var meal_name: String = "",
    var fda_search_term: String = "",
    var total_calories: Int = 0,
    var macros: GeminiMacros = GeminiMacros(0,0,0), // Compiled later
    var items: List<GeminiItem> = emptyList(),      // Compiled later
    var fallback_ingredients: List<GeminiFallbackIngredient> = emptyList()
)

@Serializable
data class GeminiFallbackIngredient(
    var name: String,
    var fda_search_term: String,
    var estimated_macros: GeminiIngredientMacros
)

@Serializable
data class GeminiIngredientMacros(
    var calories: Int = 0,
    var protein_g: Int = 0,
    var carbs_g: Int = 0,
    var fat_g: Int = 0
)

@Serializable
data class GeminiMacros(
    var protein_g: Int,
    var carbs_g: Int,
    var fat_g: Int
)

@Serializable
data class GeminiItem(
    var name: String,
    var calories: Int,
    var protein_g: Int = 0,
    var carbs_g: Int = 0,
    var fat_g: Int = 0
)

class GeminiClient {
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true 
    }

    suspend fun analyzeMeal(apiKey: String, prompt: String): GeminiResponse {
        val jsonSchemaDefinition = """
        You must return ONLY a raw JSON object matching this exact schema (no markdown formatting, no backticks).
        CRITICAL: All numeric fields MUST be integers, NOT strings. DO NOT put quotes around numbers.
        {
            "status": "success | needs_clarification",
            "clarification_question": "string (optional)",
            "data": {
                "meal_name": "string",
                "fda_search_term": "string",
                "total_calories": 0,
                "fallback_ingredients": [
                    {
                        "name": "string",
                        "fda_search_term": "string",
                        "estimated_macros": {
                            "calories": 0,
                            "protein_g": 0,
                            "carbs_g": 0,
                            "fat_g": 0
                        }
                    }
                ]
            }
        }
        """.trimIndent()

        val config = com.google.ai.client.generativeai.type.generationConfig {
            responseMimeType = "application/json"
        }

        val generativeModel = GenerativeModel(
            modelName = "gemini-3.1-flash-lite",
            apiKey = apiKey,
            generationConfig = config,
            systemInstruction = com.google.ai.client.generativeai.type.content {
                text("You are a precise nutrition API and a natural language translator for the FDA database. The user will provide a meal description.\n" +
                     "Your job is to parse this and return a JSON object.\n" +
                     "Rule 1: Provide an optimized 'fda_search_term' for the full meal (translating natural language into generic terms, or keeping standard names for composite meals like 'Beef stew').\n" +
                     "Rule 2: Provide a list of 'fallback_ingredients'. If the user's meal is a simple, single item (like 'an apple' or 'glass of milk'), do NOT break it down into ingredients; leave fallback_ingredients empty. If it is a complex meal (like 'a cheeseburger' or 'chicken salad'), break it down into its constituent ingredients (e.g. bun, beef patty, cheese), providing an optimized fda_search_term and estimated_macros for each.\n" +
                     "Rule 3: Always provide the 'total_calories' for the entire meal.\n\n$jsonSchemaDefinition")
            }
        )

        val response = generativeModel.generateContent(prompt)
        
        val rawText = response.text?.trim()?.removePrefix("```json")?.removePrefix("```")?.removeSuffix("```")?.trim() ?: "{}"
        
        return json.decodeFromString(rawText)
    }
}
