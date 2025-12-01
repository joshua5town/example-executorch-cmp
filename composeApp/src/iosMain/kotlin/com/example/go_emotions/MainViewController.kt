package com.example.go_emotions

import com.example.go_emotions.di.initKoin
import androidx.compose.ui.window.ComposeUIViewController
import com.example.go_emotions.di.databaseServiceIOS
import com.example.go_emotions.di.predictionIOS
import com.example.go_emotions.domain.DatabaseService
import com.example.go_emotions.domain.PredictionService

fun MainViewController(
    predictionService: PredictionService,
    databaseService: DatabaseService
) = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    predictionIOS = predictionService
    databaseServiceIOS = databaseService
    App()
}