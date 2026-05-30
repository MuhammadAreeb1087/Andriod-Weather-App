package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.UserEntity
import com.example.database.UserRepository
import com.example.database.WeatherDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WeatherDatabase.getDatabase(application)
    private val repo = UserRepository(db.userDao())

    val activeSession: StateFlow<UserEntity?> = repo.activeSession
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    var isDarkMode = MutableStateFlow(true) // Dark mode enabled by default

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun registerUser(email: String, name: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val success = repo.registerUser(
                UserEntity(
                    email = email,
                    fullName = name,
                    passwordHash = pass
                )
            )
            if (success) {
                onResult(true, "Registration Successful!")
            } else {
                onResult(false, "Email address is already registered.")
            }
        }
    }

    fun login(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val success = repo.loginWithEmail(email, pass)
            if (success) {
                onResult(true, "Logged in successfully!")
            } else {
                onResult(false, "Invalid credentials or user does not exist. Please registration-create first!")
            }
        }
    }

    fun loginWithGoogle(email: String, name: String) {
        viewModelScope.launch {
            repo.loginWithGoogle(
                email = email,
                fullName = name,
                avatarUrl = ""
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
        }
    }
}
