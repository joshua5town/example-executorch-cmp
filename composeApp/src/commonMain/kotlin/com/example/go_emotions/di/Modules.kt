package com.example.go_emotions.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.example.go_emotions.MainViewModel
import com.example.go_emotions.data.DatabaseServiceImpl
import com.example.go_emotions.domain.AppContext
import com.example.go_emotions.domain.DatabaseService
import com.example.go_emotions.domain.EmotionPrediction


expect val platformModule: Module

fun sharedModule() = platformModule + module {
    includes(platformModule)
    single { EmotionPrediction() }
    single<DatabaseService> { DatabaseServiceImpl() }
    viewModelOf(::MainViewModel)
}