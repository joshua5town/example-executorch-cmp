package com.example.go_emotions.domain.errorhandling

typealias RootError = Error

// Pass in any type of data and any type of error
sealed interface Result<out D, out E: RootError> {
    data class Success<out D, out E: RootError>(val data: D): Result<D, E>
    data class Error<out D, out E: RootError>(val error: E): Result<D, E>
}