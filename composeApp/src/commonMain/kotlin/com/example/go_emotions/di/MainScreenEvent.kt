package com.example.go_emotions.di

sealed interface MainScreenEvent {
    data class InputField(val text: String) : MainScreenEvent
    data class ShowProgression(val check: Boolean) : MainScreenEvent
    data object GetPrediction : MainScreenEvent
    data object DownloadModels : MainScreenEvent
    data object DownloadTokenizer : MainScreenEvent
    data object ClearOutput : MainScreenEvent
}