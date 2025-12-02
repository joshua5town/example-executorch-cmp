package com.example.go_emotions.domain

import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatform

/**
 * Will bridge kotlin to swift using koin
 */
interface PredictionService {
    suspend fun predict(text: String): String
}

actual class EmotionPrediction actual constructor()  {

    private val nativeService: PredictionService by lazy {
        KoinPlatform.getKoin().get(named("NativePredictionService"))
    }
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun predict(text: String): String? {
        return nativeService.predict(text)
    }
}