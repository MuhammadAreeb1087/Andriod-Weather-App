package com.example.database

import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    val activeSession: Flow<UserEntity?> = userDao.getActiveSession()

    suspend fun getActiveUserSync() = userDao.getActiveSessionSync()

    suspend fun findUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    suspend fun registerUser(user: UserEntity): Boolean {
        // Check if user already exists
        val existing = userDao.getUserByEmail(user.email)
        if (existing != null) return false
        
        userDao.clearActiveSessions()
        userDao.insertUser(user.copy(isLoggedIn = true))
        return true
    }

    suspend fun loginWithEmail(email: String, rawPasswordHash: String): Boolean {
        val user = userDao.getUserByEmail(email) ?: return false
        if (user.passwordHash == rawPasswordHash && !user.isGoogleLogin) {
            userDao.clearActiveSessions()
            userDao.insertUser(user.copy(isLoggedIn = true))
            return true
        }
        return false
    }

    suspend fun loginWithGoogle(email: String, fullName: String, avatarUrl: String): Boolean {
        userDao.clearActiveSessions()
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            // Already registered - sign in
            userDao.insertUser(existing.copy(isLoggedIn = true, fullName = fullName, avatarUrl = avatarUrl))
        } else {
            // New user registration via Google auth path
            val newUser = UserEntity(
                email = email,
                fullName = fullName,
                passwordHash = "GOOGLE_AUTH",
                isGoogleLogin = true,
                avatarUrl = avatarUrl,
                isLoggedIn = true,
                lastSelectedCity = "Karachi"
            )
            userDao.insertUser(newUser)
        }
        return true
    }

    suspend fun updateSelectedCity(email: String, city: String) {
        val user = userDao.getUserByEmail(email)
        if (user != null) {
            userDao.insertUser(user.copy(lastSelectedCity = city))
        }
    }

    suspend fun logout() {
        userDao.clearActiveSessions()
    }
}
