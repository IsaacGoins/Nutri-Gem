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
