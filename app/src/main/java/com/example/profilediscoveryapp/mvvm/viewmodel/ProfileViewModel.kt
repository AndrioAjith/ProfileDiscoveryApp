package com.example.profilediscoveryapp.mvvm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


import com.example.profilediscoveryapp.mvvm.model.dataclass.Profile
import com.example.profilediscoveryapp.mvvm.model.repository.CloudinaryRepository
import com.example.profilediscoveryapp.mvvm.model.repository.ProfileRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val userId: String
) : ViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _imageUploadProgress = MutableStateFlow(0)
    val imageUploadProgress: StateFlow<Int> = _imageUploadProgress.asStateFlow()

    private val cloudinaryRepository = CloudinaryRepository()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userProfile = profileRepository.getProfile(userId)
                _profile.value = userProfile
                Log.d(
                    "ProfileViewModel",
                    "Loaded profile with image: ${userProfile?.profilePictureUrl}"
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load profile"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createProfile(profile: Profile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(
                    "ProfileViewModel",
                    "Creating profile with image URL: ${profile.profilePictureUrl}"
                )
                val success = profileRepository.createOrUpdateProfile(profile)
                _updateSuccess.value = success
                if (success) {
                    _profile.value = profile
                    Log.d("ProfileViewModel", "Profile created successfully")
                } else {
                    _error.value = "Failed to create profile"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Creation failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(
                    "ProfileViewModel",
                    "Updating profile with image URL: ${profile.profilePictureUrl}"
                )
                val success = profileRepository.createOrUpdateProfile(profile)
                _updateSuccess.value = success
                if (success) {
                    _profile.value = profile
                    Log.d("ProfileViewModel", "Profile updated successfully")
                } else {
                    _error.value = "Failed to update profile"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Update failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfilePicture(imageFile: File) {
        viewModelScope.launch {
            try {
                _imageUploadProgress.value = 10
                Log.d("ProfileViewModel", "Starting Cloudinary upload...")

                // Upload to Cloudinary
                val cloudinaryUrl = cloudinaryRepository.uploadImage(imageFile)
                _imageUploadProgress.value = 50

                if (cloudinaryUrl.isNullOrEmpty()) {
                    _error.value = "Cloudinary upload failed"
                    _imageUploadProgress.value = 0
                    return@launch
                }

                _imageUploadProgress.value = 80
                Log.d("ProfileViewModel", "Cloudinary upload successful: $cloudinaryUrl")

                // Update profile with Cloudinary URL
                val currentProfile = _profile.value
                if (currentProfile != null) {
                    val updatedProfile = currentProfile.copy(profilePictureUrl = cloudinaryUrl)
                    val success = profileRepository.createOrUpdateProfile(updatedProfile)
                    _imageUploadProgress.value = 100

                    if (success) {
                        _profile.value = updatedProfile
                        _updateSuccess.value = true
                        Log.d("ProfileViewModel", "Profile updated with Cloudinary URL")
                    } else {
                        _error.value = "Failed to save profile with new image"
                    }
                } else {
                    // For new profile, just store the URL
                    uploadedImageUrl = cloudinaryUrl
                    Log.d(
                        "ProfileViewModel",
                        "Cloudinary URL stored for new profile: $cloudinaryUrl"
                    )
                }

                delay(500)
                _imageUploadProgress.value = 0

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error uploading to Cloudinary: ${e.message}")
                _error.value = e.message ?: "Upload failed"
                _imageUploadProgress.value = 0
            }
        }
    }

    // Store Cloudinary URL temporarily for new profiles
    private var uploadedImageUrl: String? = null

    fun getUploadedImageUrl(): String? = uploadedImageUrl

    fun clearUploadedImageUrl() {
        uploadedImageUrl = null
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}