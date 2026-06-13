package com.example.profilediscoveryapp.mvvm.model.repository

import android.content.Context
import com.example.profilediscoveryapp.mvvm.model.database.SessionManager
import com.example.profilediscoveryapp.mvvm.model.dataclass.AuthResponse

import com.example.profilediscoveryapp.DatabaseHelper
import java.util.UUID

class AuthRepository(context: Context) {
    private val databaseHelper = DatabaseHelper(context)
    private val sessionManager = SessionManager(context)

    fun register(email: String, password: String, fullName: String): AuthResponse {
        return if (databaseHelper.checkUserExists(email)) {
            AuthResponse(false, "User already exists with this email", null)
        } else {
            val userId = UUID.randomUUID().toString()
            val success = databaseHelper.insertUser(userId, email, password, fullName)
            if (success) {
                AuthResponse(true, "Registration successful! Please login.", userId)
            } else {
                AuthResponse(false, "Registration failed. Please try again.", null)
            }
        }
    }

    fun login(email: String, password: String): AuthResponse {
        val userId = databaseHelper.checkUser(email, password)
        return if (userId != null) {
            sessionManager.saveSession(userId, email)
            AuthResponse(true, "Login successful!", userId)
        } else {
            AuthResponse(false, "Invalid email or password", null)
        }
    }

    fun checkUserExists(email: String): Boolean {
        return databaseHelper.checkUserExists(email)
    }

    fun updatePassword(email: String, newPassword: String): AuthResponse {
        val success = databaseHelper.updatePassword(email, newPassword)
        return if (success) {
            AuthResponse(
                true, "Password updated successfully! Please login with your new password.", null
            )
        } else {
            AuthResponse(false, "Failed to update password. Please try again.", null)
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }


}