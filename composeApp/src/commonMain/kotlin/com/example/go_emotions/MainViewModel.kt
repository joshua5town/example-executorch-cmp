package com.example.go_emotions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.go_emotions.domain.EmotionPrediction
import com.example.go_emotions.di.MainScreenEvent
import com.example.go_emotions.domain.DatabaseService
import com.example.go_emotions.domain.errorhandling.Result
import com.example.go_emotions.domain.retrypolicy.RetryPolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val emotionPrediction: EmotionPrediction,
    private val databaseService: DatabaseService
): ViewModel() {

    private val retryPolicy = RetryPolicy(
        maxRetries = 3,
        initialDelay = 5,
        maxDelay = 30,
        delayFactor = 2.0
    )

    private val _state = MutableStateFlow(MainScreenState())
    val state = _state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        MainScreenState()
    )

    fun onEvent(
        event: MainScreenEvent
    ) {
        when (event) {
            is MainScreenEvent.InputField -> {
                _state.update {
                    it.copy(
                        inputText = event.text
                    )
                }
            }

            is MainScreenEvent.ShowProgression -> {
                _state.update {
                    it.copy(
                        showProgressionBar = event.check
                    )
                }
            }

            is MainScreenEvent.GetPrediction -> {
                viewModelScope.launch(
                    Dispatchers.Default
                ){
                    try {
                        val response = emotionPrediction.predict(_state.value.inputText)
                        _state.update {
                            it.copy(
                                predictionText = response?.ifEmpty { "No prediction" } ?: "No prediction" ,
                                showProgressionBar = false
                            )
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                        _state.update {
                            it.copy(
                                showProgressionBar = false
                            )
                        }
                    }
                }
            }
            MainScreenEvent.DownloadModels -> {
                viewModelScope.launch {
                    try {
                        val result = retryPolicy.retry(
                            block = {
                                val result = databaseService.getModel()

                                if(result is Result.Error){
                                    throw CancellationException("Error getting model")
                                }
                            },
                            retryIf = { e ->
                                e is CancellationException || e is IllegalStateException
                            }
                        )

                    }catch (e: Exception){
                        throw CancellationException("Error getting model")
                    }
                }
            }
            MainScreenEvent.DownloadTokenizer -> {
                viewModelScope.launch {
                    try {
                        val result = retryPolicy.retry(
                            block = {
                                val result = databaseService.getTokenizer()

                                if(result is Result.Error){
                                    throw CancellationException("Error getting tokenizer")
                                }
                            },
                            retryIf = { e ->
                                e is CancellationException || e is IllegalStateException
                            }
                        )

                    }catch (e: Exception){
                        throw CancellationException("Error getting tokenizer")
                    }
                }
            }
            MainScreenEvent.ClearOutput -> {
                _state.update {
                    it.copy(
                        predictionText = ""
                    )
                }
            }
        }
    }
}