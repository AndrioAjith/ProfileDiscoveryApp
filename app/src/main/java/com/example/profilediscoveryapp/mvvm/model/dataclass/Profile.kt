package com.example.profilediscoveryapp.mvvm.model.dataclass


data class Profile(
    val id: String = "",
    val userId: String,
    val fullName: String,
    val age: Int,
    val email: String,
    val phoneNumber: String,
    val occupation: String,
    val location: String,
    val aboutMe: String,
    val profilePictureUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)






