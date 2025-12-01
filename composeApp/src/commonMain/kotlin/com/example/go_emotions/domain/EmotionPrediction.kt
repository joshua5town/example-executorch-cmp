package com.example.go_emotions.domain

expect class EmotionPrediction() {
    suspend fun predict(text: String): String?
}