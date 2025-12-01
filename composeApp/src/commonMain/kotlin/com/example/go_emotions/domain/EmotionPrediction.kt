package com.example.go_emotions.domain

/**
 * Prediction function that executes on the device
 */
expect class EmotionPrediction() {
    suspend fun predict(text: String): String?
}