package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val passwordHash: String, // will store raw password or simple hash for comparison
    val isGoogleLogin: Boolean = false,
    val avatarUrl: String = "",
    val isLoggedIn: Boolean = false,
    val lastSelectedCity: String = "Karachi"
)
