package com.example.go_emotions.di

import com.example.go_emotions.domain.DatabaseService
import com.example.go_emotions.domain.PredictionService
import org.koin.core.module.Module
import org.koin.dsl.module

var predictionIOS: PredictionService? = null
var databaseServiceIOS: DatabaseService? = null
actual val platformModule: Module
    get() = module {

    }