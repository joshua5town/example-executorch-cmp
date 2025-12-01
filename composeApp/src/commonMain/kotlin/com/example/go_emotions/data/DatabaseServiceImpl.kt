package com.example.go_emotions.data

import com.example.go_emotions.domain.DatabaseService
import com.example.go_emotions.domain.errorhandling.DataError
import com.example.go_emotions.domain.errorhandling.Result

expect class DatabaseServiceImpl(): DatabaseService {
    override suspend fun getTokenizer(): Result<Boolean, DataError>
    override suspend fun getModel(): Result<Boolean, DataError>
}