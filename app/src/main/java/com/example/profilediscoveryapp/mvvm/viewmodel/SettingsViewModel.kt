package com.example.profilediscoveryapp.mvvm.viewmodel


import androidx.lifecycle.ViewModel
import com.example.profilediscoveryapp.mvvm.model.database.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _isDarkModeEnabled = MutableStateFlow(false)
    val isDarkModeEnabled: StateFlow<Boolean> = _isDarkModeEnabled.asStateFlow()

    init {
        _isDarkModeEnabled.value = userPreferences.isDarkModeEnabled()
    }

    fun toggleDarkMode(enabled: Boolean) {
        userPreferences.setDarkModeEnabled(enabled)
        _isDarkModeEnabled.value = enabled
    }
}