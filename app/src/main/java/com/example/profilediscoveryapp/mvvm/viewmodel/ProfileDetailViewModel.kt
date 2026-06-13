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

class ProfileDetailViewModel(
    private val profileRepository: ProfileRepository,
    private val userId: String,
    private val profileId: String
) : ViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

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
        checkIfSaved()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = profileRepository.getProfileById(profileId)
                _profile.value = profile
                Log.d("ProfileDetailVM", "Loaded profile: ${profile?.fullName}")
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load profile"
                Log.e("ProfileDetailVM", "Error loading profile: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun checkIfSaved() {
        viewModelScope.launch {
            try {
                val saved = profileRepository.isProfileSaved(profileId, userId)
                _isSaved.value = saved
                Log.d("ProfileDetailVM", "Profile saved status: $saved")
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to check saved status"
                Log.e("ProfileDetailVM", "Error checking saved: ${e.message}")
            }
        }
    }

    fun toggleSaveProfile() {
        viewModelScope.launch {
            try {
                if (_isSaved.value) {
                    profileRepository.unsaveProfileFromFavorites(profileId, userId)
                    _isSaved.value = false
                    Log.d("ProfileDetailVM", "Profile unsaved")
                } else {
                    profileRepository.saveProfileToFavorites(profileId, userId)
                    _isSaved.value = true
                    Log.d("ProfileDetailVM", "Profile saved")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save/unsave profile"
                Log.e("ProfileDetailVM", "Error toggling save: ${e.message}")
            }
        }
    }

    fun updateProfilePicture(imageFile: File) {
        viewModelScope.launch {
            try {
                _imageUploadProgress.value = 10
                Log.d("ProfileDetailVM", "Starting Cloudinary upload...")

                val cloudinaryUrl = cloudinaryRepository.uploadImage(imageFile)
                _imageUploadProgress.value = 50

                if (cloudinaryUrl.isNullOrEmpty()) {
                    _error.value = "Cloudinary upload failed"
                    _imageUploadProgress.value = 0
                    return@launch
                }

                _imageUploadProgress.value = 80
                Log.d("ProfileDetailVM", "Cloudinary upload successful: $cloudinaryUrl")

                val currentProfile = _profile.value
                if (currentProfile != null) {
                    val updatedProfile = currentProfile.copy(profilePictureUrl = cloudinaryUrl)
                    val success = profileRepository.createOrUpdateProfile(updatedProfile)
                    _imageUploadProgress.value = 100

                    if (success) {
                        _profile.value = updatedProfile
                        _updateSuccess.value = true
                        Log.d("ProfileDetailVM", "Profile updated with Cloudinary URL")
                    } else {
                        _error.value = "Failed to save profile with new image"
                    }
                }

                delay(500)
                _imageUploadProgress.value = 0

            } catch (e: Exception) {
                Log.e("ProfileDetailVM", "Error uploading to Cloudinary: ${e.message}")
                _error.value = e.message ?: "Upload failed"
                _imageUploadProgress.value = 0
            }
        }
    }

    fun updateProfile(updatedProfile: Profile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("ProfileDetailVM", "Updating profile: ${updatedProfile.fullName}")
                Log.d("ProfileDetailVM", "With image URL: ${updatedProfile.profilePictureUrl}")

                val success = profileRepository.createOrUpdateProfile(updatedProfile)
                _updateSuccess.value = success

                if (success) {
                    _profile.value = updatedProfile
                    Log.d("ProfileDetailVM", "Profile updated successfully")
                } else {
                    _error.value = "Failed to update profile"
                    Log.e("ProfileDetailVM", "Profile update failed")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Update failed"
                Log.e("ProfileDetailVM", "Error updating profile: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}