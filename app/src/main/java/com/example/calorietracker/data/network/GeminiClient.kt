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
    val meal_name: String,
    val total_calories: Int,
    val macros: GeminiMacros,
    val items: List<GeminiItem>
)

@Serializable
data class GeminiMacros(
    val protein_g: Int,
    val carbs_g: Int,
    val fat_g: Int
)

@Serializable
data class GeminiItem(
    val name: String,
    val calories: Int
)

class GeminiClient {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeMeal(apiKey: String, prompt: String): GeminiResponse {
        val jsonSchemaDefinition = """
        You must return ONLY a raw JSON object matching this exact schema (no markdown formatting, no backticks):
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
                    { "name": "string", "calories": 0 }
                ]
            }
        }
        """.trimIndent()

        val config = com.google.ai.client.generativeai.type.generationConfig {
            responseMimeType = "application/json"
        }

        val generativeModel = GenerativeModel(
            modelName = "gemini-flash-latest",
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
