package com.example.profilediscoveryapp.mvvm.model.dataclass

data class ApiProfileResponse(
    val apiId: String,
    val userId: String,
    val fullName: String,
    val age: Int,
    val email: String,
    val phoneNumber: Any,
    val occupation: String,
    val location: String,
    val aboutMe: String,
    val profilePictureUrl: String? = null,
    val createAt: Int
) {
    fun toProfile(): Profile {
        val phoneNumberStr = when (phoneNumber) {
            is Int -> phoneNumber.toString()
            is Long -> phoneNumber.toString()
            is String -> phoneNumber as String
            else -> ""
        }

        return Profile(
            id = this.apiId,
            userId = this.userId,
            fullName = this.fullName,
            age = this.age,
            email = this.email,
            phoneNumber = phoneNumberStr,
            occupation = this.occupation,
            location = this.location,
            aboutMe = this.aboutMe,
            profilePictureUrl = this.profilePictureUrl,
            createdAt = this.createAt.toLong()
        )
    }
}