package com.example.profilediscoveryapp



import android.app.Application

class ProfileDiscoveryApplication : Application() {
    companion object {
        lateinit var instance: ProfileDiscoveryApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}