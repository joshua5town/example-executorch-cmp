package com.example.go_emotions.domain

import com.example.go_emotions.di.predictionIOS
import kotlinx.cinterop.ExperimentalForeignApi

interface PredictionService {
    suspend fun predict(text: String): String
}

actual class EmotionPrediction actual constructor()  {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun predict(text: String): String? {
        return predictionIOS?.predict(text) ?: ""
    }
}