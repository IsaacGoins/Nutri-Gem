package com.example.calorietracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val timestamp: Long,
    val itemsJson: String = "[]"
)

@Entity(tableName = "water_intake")
data class WaterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountOz: Int,
    val timestamp: Long
)

@Entity(tableName = "weight_log")
data class WeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightLbs: Float,
    val timestamp: Long
)

@Entity(tableName = "daily_scores")
data class DailyScoreEntity(
    @PrimaryKey val dateTimestamp: Long, // Start of day timestamp
    val overallScore: Int,
    val cleanScore: Int,
    val macroScore: Int,
    val calorieScore: Int,
    val waterScore: Int,
    val balanceScore: Int,
    val feedback: String
)
