package com.example.go_emotions

import com.example.go_emotions.di.initKoin
import androidx.compose.ui.window.ComposeUIViewController
import com.example.go_emotions.domain.PredictionService
import org.koin.dsl.module

fun MainViewController(
    predictionService: PredictionService
) = ComposeUIViewController(
    configure = {
        // 1. Define a dynamic module for the dependencies coming from Swift
        val swiftModule = module {
            single<PredictionService> { predictionService }
        }

        // 2. Start Koin with your standard modules + this specific Swift module
        initKoin {
            modules(swiftModule)
        }
    }
) {
    App()
}