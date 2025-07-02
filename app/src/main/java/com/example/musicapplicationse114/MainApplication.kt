package com.example.musicapplicationse114

import android.app.Application
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        FirebaseApp.initializeApp(this)
    }
}
