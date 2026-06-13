package com.example.profilediscoveryapp.mvvm.model.dataclass

data class SavedProfile(
    val id: String,
    val profileId: String,
    val userId: String,
    val savedAt: Long = System.currentTimeMillis()
)