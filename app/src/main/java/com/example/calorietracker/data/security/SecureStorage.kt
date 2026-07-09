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
}
