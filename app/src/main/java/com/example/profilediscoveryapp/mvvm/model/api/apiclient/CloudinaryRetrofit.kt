package com.example.profilediscoveryapp.mvvm.model.api.apiclient

import com.example.profilediscoveryapp.mvvm.model.api.apiservices.CloudinaryApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CloudinaryRetrofit {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: CloudinaryApi = Retrofit.Builder()
        .baseUrl("https://api.cloudinary.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CloudinaryApi::class.java)
}