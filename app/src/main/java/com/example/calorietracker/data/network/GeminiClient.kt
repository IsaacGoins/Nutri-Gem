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
    var meal_name: String,
    var total_calories: Int,
    var macros: GeminiMacros,
    var items: List<GeminiItem>
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
        CRITICAL: All numeric fields (total_calories, protein_g, carbs_g, fat_g, calories) MUST be integers, NOT strings. DO NOT put quotes around numbers.
        {
            "status": "success | needs_clarification",
            "clarification_question": "string (optional)",
            "data": {
                "meal_name": "string",
                "total_calories": 0,
                "macros": {
                    "protein_g": 0,
                    "carbs_g": 0,
                    "fat_g": 0
                },
                "items": [
                    { "name": "string", "calories": 0, "protein_g": 0, "carbs_g": 0, "fat_g": 0 }
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
                text("You are a precise nutrition API. The user will provide meal items. If the items are detailed enough to estimate accurately, return a status of `success` and the calculated data. If the user provides something too vague, return a status of `needs_clarification` and ask for specifics.\n\n$jsonSchemaDefinition")
            }
        )

        val response = generativeModel.generateContent(prompt)
        
        // Strip markdown backticks if the model accidentally includes them
        val rawText = response.text?.trim()?.removePrefix("```json")?.removePrefix("```")?.removeSuffix("```")?.trim() ?: "{}"
        
        return json.decodeFromString(rawText)
    }
}
