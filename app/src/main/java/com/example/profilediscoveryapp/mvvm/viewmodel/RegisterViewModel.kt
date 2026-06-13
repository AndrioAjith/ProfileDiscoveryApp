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

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registrationResult = MutableStateFlow<AuthResponse?>(null)
    val registrationResult: StateFlow<AuthResponse?> = _registrationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun register(fullName: String, email: String, password: String, confirmPassword: String) {
        if (!validateInputs(fullName, email, password, confirmPassword)) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.register(email, password, fullName)
                _registrationResult.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInputs(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (fullName.isEmpty()) {
            _error.value = "Full name is required"
            return false
        }
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
        if (password != confirmPassword) {
            _error.value = "Passwords do not match"
            return false
        }
        return true
    }

    fun clearError() {
        _error.value = null
    }
}