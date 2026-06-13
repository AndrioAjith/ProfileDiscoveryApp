package com.example.profilediscoveryapp.mvvm.model.api.apiservices

import com.example.profilediscoveryapp.mvvm.model.dataclass.ApiProfileResponse
import com.example.profilediscoveryapp.mvvm.model.dataclass.Profile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("profiles")
    suspend fun getAllProfiles(): Response<List<ApiProfileResponse>>

    @GET("profiles/{id}")
    suspend fun getProfileById(@Path("id") id: String): Response<ApiProfileResponse>

    @POST("profiles")
    suspend fun createProfile(@Body profile: Profile): Response<ApiProfileResponse>

    @PUT("profiles/{id}")
    suspend fun updateProfile(@Path("id") id: String, @Body profile: Profile): Response<ApiProfileResponse>

    @DELETE("profiles/{id}")
    suspend fun deleteProfile(@Path("id") id: String): Response<Unit>
}