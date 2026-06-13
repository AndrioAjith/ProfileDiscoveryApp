package com.example.profilediscoveryapp.mvvm.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.profilediscoveryapp.mvvm.model.dataclass.AuthResponse
import com.example.profilediscoveryapp.mvvm.model.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _resetResult = MutableStateFlow<AuthResponse?>(null)
    val resetResult: StateFlow<AuthResponse?> = _resetResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _emailVerified = MutableStateFlow(false)
    val emailVerified: StateFlow<Boolean> = _emailVerified.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    fun verifyEmail(email: String) {
        if (email.isEmpty()) {
            _error.value = "Email is required"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = "Invalid email format"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userExists = authRepository.checkUserExists(email)
                if (userExists) {
                    _emailVerified.value = true
                    _userEmail.value = email
                    _error.value = null
                } else {
                    _error.value = "Email address not found. Please check and try again."
                    _emailVerified.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Verification failed"
                _emailVerified.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword(email: String, newPassword: String) {
        if (newPassword.isEmpty()) {
            _error.value = "Password is required"
            return
        }

        if (newPassword.length < 6) {
            _error.value = "Password must be at least 6 characters"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.updatePassword(email, newPassword)
                _resetResult.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Password reset failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }


}