package com.example.profile_discovery.mvvm.viewmodel


import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.profilediscoveryapp.mvvm.model.dataclass.AuthResponse
import com.example.profilediscoveryapp.mvvm.model.repository.AuthRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginResult = MutableStateFlow<AuthResponse?>(null)
    val loginResult: StateFlow<AuthResponse?> = _loginResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun login(email: String, password: String) {
        if (!validateInputs(email, password)) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.login(email, password)
                _loginResult.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            _error.value = "Email is required"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = "Invalid email format"
            return false
        }
        if (password.isEmpty()) {
            _error.value = "Password is required"
            return false
        }
        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters"
            return false
        }
        return true
    }

    fun clearError() {
        _error.value = null
    }
}