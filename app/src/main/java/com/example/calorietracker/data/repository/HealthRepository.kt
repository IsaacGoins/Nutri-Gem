package com.example.calorietracker.data.repository

import com.example.calorietracker.data.local.HealthDao
import com.example.calorietracker.data.local.MealEntity
import com.example.calorietracker.data.local.WaterEntity
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val healthDao: HealthDao) {
    fun getAllMeals(): Flow<List<MealEntity>> = healthDao.getAllMeals()
    fun getAllWater(): Flow<List<WaterEntity>> = healthDao.getAllWater()
    
    fun getCaloriesForDay(startOfDay: Long): Flow<Int?> = healthDao.getCaloriesForDay(startOfDay)
    fun getProteinForDay(startOfDay: Long): Flow<Int?> = healthDao.getProteinForDay(startOfDay)
    fun getCarbsForDay(startOfDay: Long): Flow<Int?> = healthDao.getCarbsForDay(startOfDay)
    fun getFatForDay(startOfDay: Long): Flow<Int?> = healthDao.getFatForDay(startOfDay)
    fun getWaterForDay(startOfDay: Long): Flow<Int?> = healthDao.getWaterForDay(startOfDay)

    suspend fun addMeal(meal: MealEntity) {
        healthDao.insertMeal(meal)
    }

    suspend fun addWater(water: WaterEntity) {
        healthDao.insertWater(water)
    }
}
