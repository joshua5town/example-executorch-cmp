package com.example.go_emotions

/**
 * State of the main screen
 */
data class MainScreenState (
    val inputText: String = "",
    val predictionText: String = "",
    val showProgressionBar: Boolean = false
)