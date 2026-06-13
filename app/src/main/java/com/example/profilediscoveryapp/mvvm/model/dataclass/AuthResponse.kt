package com.example.profilediscoveryapp.mvvm.model.dataclass


data class AuthResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null
)