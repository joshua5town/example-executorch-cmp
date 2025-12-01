package com.example.go_emotions.di

import android.app.Application
import com.example.go_emotions.domain.AppContext
import org.koin.android.ext.koin.androidContext

class InjectionApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@InjectionApplication)
        }

        AppContext.setUp(applicationContext)
    }
}