package com.example.go_emotions

/**
 * Events that can be sent to the MainViewModel
 */
sealed interface MainScreenEvent {
    data class InputField(val text: String) : MainScreenEvent
    data class ShowProgression(val check: Boolean) : MainScreenEvent
    data object GetPrediction : MainScreenEvent
    data object ClearOutput : MainScreenEvent
}