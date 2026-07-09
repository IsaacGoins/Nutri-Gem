package com.example.calorietracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Insert
    suspend fun insertMeal(meal: MealEntity)

    @androidx.room.Update
    suspend fun updateMeal(meal: MealEntity)

    @androidx.room.Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Insert
    suspend fun insertWater(water: WaterEntity)

    @androidx.room.Delete
    suspend fun deleteWater(water: WaterEntity)

    @Query("SELECT * FROM water_intake ORDER BY timestamp DESC")
    fun getAllWater(): Flow<List<WaterEntity>>

    @Query("SELECT SUM(calories) FROM meals WHERE timestamp >= :startOfDay")
    fun getCaloriesForDay(startOfDay: Long): Flow<Int?>

    @Query("SELECT SUM(proteinG) FROM meals WHERE timestamp >= :startOfDay")
    fun getProteinForDay(startOfDay: Long): Flow<Int?>

    @Query("SELECT SUM(carbsG) FROM meals WHERE timestamp >= :startOfDay")
    fun getCarbsForDay(startOfDay: Long): Flow<Int?>

    @Query("SELECT SUM(fatG) FROM meals WHERE timestamp >= :startOfDay")
    fun getFatForDay(startOfDay: Long): Flow<Int?>

    @Query("SELECT SUM(amountOz) FROM water_intake WHERE timestamp >= :startOfDay")
    fun getWaterForDay(startOfDay: Long): Flow<Int?>
}
