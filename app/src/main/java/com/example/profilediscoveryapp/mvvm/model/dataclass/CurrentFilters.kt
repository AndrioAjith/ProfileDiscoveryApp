package com.example.profilediscoveryapp.mvvm.model.dataclass

data class CurrentFilters(
    val minAge: Int?,
    val maxAge: Int?,
    val occupation: String?,
    val location: String?
)