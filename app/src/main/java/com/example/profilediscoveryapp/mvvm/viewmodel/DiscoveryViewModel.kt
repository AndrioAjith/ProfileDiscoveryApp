package com.example.profile_discovery.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.profilediscoveryapp.mvvm.model.dataclass.CurrentFilters
import com.example.profilediscoveryapp.mvvm.model.dataclass.Profile
import com.example.profilediscoveryapp.mvvm.model.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    private val profileRepository: ProfileRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _allProfiles = MutableStateFlow<List<Profile>>(emptyList())
    val allProfiles: StateFlow<List<Profile>> = _allProfiles.asStateFlow()

    private val _filteredProfiles = MutableStateFlow<List<Profile>>(emptyList())
    val filteredProfiles: StateFlow<List<Profile>> = _filteredProfiles.asStateFlow()

    private val _savedProfileIds = MutableStateFlow<Set<String>>(emptySet())
    val savedProfileIds: StateFlow<Set<String>> = _savedProfileIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private var currentSearchQuery = ""
    private var currentAgeRange: IntRange? = null
    private var currentOccupation: String? = null
    private var currentLocation: String? = null

    init {
        loadProfiles()
        loadSavedProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profiles = profileRepository.getAllProfiles()
                _allProfiles.value = profiles
                applyFilters()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load profiles"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProfiles(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    fun filterProfiles(ageRange: IntRange?, occupation: String?, location: String?) {
        currentAgeRange = ageRange
        currentOccupation = occupation
        currentLocation = location
        applyFilters()
    }

    fun getCurrentFilters(): CurrentFilters {
        return CurrentFilters(
            minAge = currentAgeRange?.first,
            maxAge = currentAgeRange?.last,
            occupation = currentOccupation,
            location = currentLocation
        )
    }

    private fun applyFilters() {
        var profiles = _allProfiles.value

        // Apply search filter
        if (currentSearchQuery.isNotEmpty()) {
            profiles = profiles.filter { profile ->
                profile.fullName.contains(currentSearchQuery, ignoreCase = true) ||
                        profile.occupation.contains(currentSearchQuery, ignoreCase = true) ||
                        profile.location.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        // Apply age range filter
        currentAgeRange?.let { ageRange ->
            profiles = profiles.filter { it.age in ageRange }
        }

        // Apply occupation filter
        currentOccupation?.let { occupation ->
            if (occupation.isNotEmpty()) {
                profiles = profiles.filter {
                    it.occupation.contains(occupation, ignoreCase = true)
                }
            }
        }

        // Apply location filter
        currentLocation?.let { location ->
            if (location.isNotEmpty()) {
                profiles = profiles.filter {
                    it.location.contains(location, ignoreCase = true)
                }
            }
        }

        _filteredProfiles.value = profiles
    }

    private fun loadSavedProfiles() {
        viewModelScope.launch {
            try {
                val savedIds =
                    profileRepository.getSavedProfiles(currentUserId).map { it.id }.toSet()
                _savedProfileIds.value = savedIds
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load saved profiles"
            }
        }
    }

    fun toggleSaveProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                val isSaved = _savedProfileIds.value.contains(profile.id)

                if (isSaved) {
                    profileRepository.unsaveProfileFromFavorites(profile.id, currentUserId)
                    _savedProfileIds.value = _savedProfileIds.value.minus(profile.id)
                    _syncMessage.value = "Profile removed from favorites"
                } else {
                    profileRepository.saveProfileToFavorites(profile.id, currentUserId)
                    _savedProfileIds.value = _savedProfileIds.value.plus(profile.id)
                    _syncMessage.value = "Profile saved to favorites"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save/unsave profile"
            }
        }
    }

    fun syncWithApi() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                profileRepository.syncProfilesWithApi()
                loadProfiles()
                _syncMessage.value = "Synced with server successfully!"
            } catch (e: Exception) {
                _error.value = "Sync failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearFilters() {
        currentSearchQuery = ""
        currentAgeRange = null
        currentOccupation = null
        currentLocation = null
        applyFilters()
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }
}

