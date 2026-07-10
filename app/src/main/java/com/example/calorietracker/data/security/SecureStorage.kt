package com.example.calorietracker.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(key: String) {
        sharedPreferences.edit().putString("API_KEY", key).apply()
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString("API_KEY", null)
    }

    fun saveFdaApiKey(key: String) {
        sharedPreferences.edit().putString("FDA_API_KEY", key).apply()
    }

    fun getFdaApiKey(): String? {
        return sharedPreferences.getString("FDA_API_KEY", null)
    }

    fun saveWeightGoal(weight: Float) {
        sharedPreferences.edit().putFloat("WEIGHT_GOAL", weight).apply()
    }

    fun getWeightGoal(): Float {
        return sharedPreferences.getFloat("WEIGHT_GOAL", 0f)
    }

    fun saveAge(age: Int) {
        sharedPreferences.edit().putInt("AGE", age).apply()
    }
    
    fun getAge(): Int {
        return sharedPreferences.getInt("AGE", 30) // Default 30
    }

    fun saveHeight(heightInches: Int) {
        sharedPreferences.edit().putInt("HEIGHT", heightInches).apply()
    }

    fun getHeight(): Int {
        return sharedPreferences.getInt("HEIGHT", 68) // Default 5'8"
    }

    fun saveWorkoutsPerWeek(count: Int) {
        sharedPreferences.edit().putInt("WORKOUTS", count).apply()
    }

    fun getWorkoutsPerWeek(): Int {
        return sharedPreferences.getInt("WORKOUTS", 3) // Default 3
    }

    fun saveActiveDays(days: String) {
        sharedPreferences.edit().putString("ACTIVE_DAYS", days).apply()
    }

    fun getActiveDays(): String {
        return sharedPreferences.getString("ACTIVE_DAYS", "Mon, Wed, Fri") ?: "Mon, Wed, Fri"
    }

    fun saveProfileNotes(notes: String) {
        sharedPreferences.edit().putString("PROFILE_NOTES", notes).apply()
    }

    fun getProfileNotes(): String {
        return sharedPreferences.getString("PROFILE_NOTES", "") ?: ""
    }

    fun saveDevSettingsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("DEV_SETTINGS_ENABLED", enabled).apply()
    }

    fun getDevSettingsEnabled(): Boolean {
        return sharedPreferences.getBoolean("DEV_SETTINGS_ENABLED", false)
    }

    fun saveColorPreference(key: String, token: String) {
        sharedPreferences.edit().putString(key, token).apply()
    }

    fun getColorPreference(key: String, defaultToken: String): String {
        return sharedPreferences.getString(key, defaultToken) ?: defaultToken
    }
}

