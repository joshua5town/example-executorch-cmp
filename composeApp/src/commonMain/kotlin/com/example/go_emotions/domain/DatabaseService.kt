package com.example.go_emotions.domain

import com.example.go_emotions.domain.errorhandling.DataError
import com.example.go_emotions.domain.errorhandling.Result


interface DatabaseService {
    suspend fun getTokenizer(): Result<Boolean, DataError>
    suspend fun getModel(): Result<Boolean, DataError>
}