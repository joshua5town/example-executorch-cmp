package com.example.go_emotions.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.example.go_emotions.MainViewModel
import com.example.go_emotions.domain.EmotionPrediction

expect val platformModule: Module

fun sharedModule() = platformModule + module {
    includes(platformModule)
    factory { EmotionPrediction() }
    viewModelOf(::MainViewModel)
}