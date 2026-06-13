package com.example.profilediscoveryapp.mvvm.model.repository

import android.content.Context
import android.util.Log
import com.example.profilediscoveryapp.mvvm.model.api.apiclient.RetrofitClient

import com.example.profilediscoveryapp.mvvm.model.dataclass.Profile
import com.example.profilediscoveryapp.mvvm.model.dataclass.SavedProfile
import com.example.profilediscoveryapp.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ProfileRepository(context: Context) {
    private val databaseHelper = DatabaseHelper(context)
    private val apiService = RetrofitClient.apiService
    private val TAG = "ProfileRepository"

    suspend fun syncProfilesWithApi(): List<Profile> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllProfiles()
            if (response.isSuccessful) {
                val apiProfiles = response.body() ?: emptyList()
                Log.d(TAG, "Synced ${apiProfiles.size} profiles from API")

                // Clear existing profiles? Or merge? Let's merge
                apiProfiles.forEach { apiProfile ->
                    try {
                        val localProfile = apiProfile.toProfile()
                        Log.d(
                            TAG,
                            "Processing profile: ID=${localProfile.id}, Name=${localProfile.fullName}"
                        )

                        val existingProfile = databaseHelper.getProfileById(localProfile.id)
                        if (existingProfile == null) {
                            val inserted = databaseHelper.insertProfile(localProfile)
                            Log.d(TAG, "Inserted new profile: $inserted")
                        } else {
                            val updated = databaseHelper.updateProfile(localProfile)
                            Log.d(TAG, "Updated existing profile: $updated")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing profile: ${e.message}")
                    }
                }

                // Return all profiles from local DB
                val allProfiles = databaseHelper.getAllProfiles()
                Log.d(TAG, "Total profiles in local DB: ${allProfiles.size}")
                allProfiles.forEach { profile ->
                    Log.d(TAG, "Local profile: ID=${profile.id}, Name=${profile.fullName}")
                }
                allProfiles
            } else {
                Log.e(TAG, "Sync failed: ${response.code()} - ${response.message()}")
                databaseHelper.getAllProfiles()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync error: ${e.message}")
            databaseHelper.getAllProfiles()
        }
    }

    suspend fun createOrUpdateProfile(profile: Profile): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "createOrUpdateProfile called for user: ${profile.userId}")

            // Check if profile exists locally
            val existingProfile = databaseHelper.getProfileByUserId(profile.userId)

            val result = if (existingProfile != null && existingProfile.id.isNotEmpty()) {
                // Update existing profile
                Log.d(TAG, "Updating existing profile with ID: ${existingProfile.id}")

                // Update locally first
                val profileToUpdate = profile.copy(id = existingProfile.id)
                val localUpdate = databaseHelper.updateProfile(profileToUpdate)
                Log.d(TAG, "Local update result: $localUpdate")

                // Update on API
                try {

                    val profileToUpdate = profile.copy(
                        id = existingProfile.id
                    )

                    val apiResponse = apiService.updateProfile(
                        existingProfile.id,
                        profileToUpdate
                    )

                    if (apiResponse.isSuccessful) {
                        Log.d(TAG, "API update successful")
                        true
                    } else {
                        Log.e(
                            TAG,
                            "API update failed: ${apiResponse.code()}"
                        )
                        localUpdate
                    }

                } catch (e: Exception) {

                    Log.e(
                        TAG,
                        "API update error: ${e.message}"
                    )

                    localUpdate
                }
            } else {
                // Create new profile
                Log.d(TAG, "Creating new profile for user: ${profile.userId}")

                try {
                    val apiResponse = apiService.createProfile(profile)
                    if (apiResponse.isSuccessful) {
                        val createdProfile = apiResponse.body()
                        createdProfile?.let {
                            Log.d(TAG, "API create successful, ID: ${it.apiId}")
                            val profileWithApiId = profile.copy(id = it.apiId)
                            val localInsert = databaseHelper.insertProfile(profileWithApiId)
                            Log.d(TAG, "Local insert result: $localInsert")
                            true
                        } ?: false
                    } else {
                        Log.e(
                            TAG,
                            "API create failed: ${apiResponse.code()} - ${apiResponse.message()}"
                        )
                        // Fallback to local only
                        val profileWithLocalId = profile.copy(id = UUID.randomUUID().toString())
                        databaseHelper.insertProfile(profileWithLocalId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "API create error: ${e.message}")
                    // Fallback to local only
                    val profileWithLocalId = profile.copy(id = UUID.randomUUID().toString())
                    databaseHelper.insertProfile(profileWithLocalId)
                }
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error in createOrUpdateProfile: ${e.message}")
            false
        }
    }

    suspend fun getProfile(userId: String): Profile? = withContext(Dispatchers.IO) {
        try {
            databaseHelper.getProfileByUserId(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile: ${e.message}")
            null
        }
    }

    suspend fun getAllProfiles(): List<Profile> = withContext(Dispatchers.IO) {
        try {
            val profiles = databaseHelper.getAllProfiles()
            Log.d(TAG, "getAllProfiles returning ${profiles.size} profiles")
            profiles
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all profiles: ${e.message}")
            emptyList()
        }
    }





    suspend fun saveProfileToFavorites(profileId: String, userId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                if (databaseHelper.isProfileSaved(profileId, userId)) {
                    return@withContext true
                }
                val savedProfile = SavedProfile(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    userId = userId
                )
                databaseHelper.saveProfile(savedProfile)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving to favorites: ${e.message}")
                false
            }
        }

    suspend fun unsaveProfileFromFavorites(profileId: String, userId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                databaseHelper.unsaveProfile(profileId, userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing from favorites: ${e.message}")
                false
            }
        }

    suspend fun isProfileSaved(profileId: String, userId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                databaseHelper.isProfileSaved(profileId, userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking saved status: ${e.message}")
                false
            }
        }

    suspend fun getSavedProfiles(userId: String): List<Profile> = withContext(Dispatchers.IO) {
        try {
            databaseHelper.getSavedProfiles(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting saved profiles: ${e.message}")
            emptyList()
        }
    }


    suspend fun getProfileById(profileId: String): Profile? = withContext(Dispatchers.IO) {
        try {
            databaseHelper.getProfileById(profileId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile by ID: ${e.message}")
            null
        }
    }

}