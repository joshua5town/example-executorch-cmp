package com.example.go_emotions.domain

import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.mp.KoinPlatform

/**
 * Will
 */
interface PredictionService {
    suspend fun predict(text: String): String
}

actual class EmotionPrediction actual constructor()  {

    private val service: PredictionService? by lazy {
        try {
            KoinPlatform.getKoin().get<PredictionService>()
        } catch (e: Exception) {
            println("PredictionService not found in Koin: ${e.message}")
            null
        }
    }
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun predict(text: String): String? {
        return service?.predict(text) ?: ""
    }
}