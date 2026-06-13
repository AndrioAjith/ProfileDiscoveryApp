package com.example.profilediscoveryapp.mvvm.model.repository

import com.example.profilediscoveryapp.mvvm.model.api.apiclient.CloudinaryRetrofit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CloudinaryRepository {

    suspend fun uploadImage(imageFile: File): String? {

        val requestFile = imageFile.asRequestBody(
            "image/*".toMediaTypeOrNull()
        )

        val body = MultipartBody.Part.createFormData(
            "file",
            imageFile.name,
            requestFile
        )

        val preset = "profile_upload"
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val response = CloudinaryRetrofit.api.uploadImage(
            body,
            preset
        )

        return if (response.isSuccessful) {
            response.body()?.secureUrl
        } else {
            null
        }
    }


}