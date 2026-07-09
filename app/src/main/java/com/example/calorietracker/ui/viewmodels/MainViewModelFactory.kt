package com.example.calorietracker.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.calorietracker.data.local.AppDatabase
import com.example.calorietracker.data.repository.HealthRepository
import com.example.calorietracker.data.security.SecureStorage

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = HealthRepository(database.healthDao())
            val secureStorage = SecureStorage(application)
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, secureStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
