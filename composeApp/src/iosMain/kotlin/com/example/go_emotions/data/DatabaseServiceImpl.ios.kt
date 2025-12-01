package com.example.go_emotions.data

import com.example.go_emotions.di.databaseServiceIOS
import com.example.go_emotions.domain.DatabaseService
import com.example.go_emotions.domain.errorhandling.DataError
import com.example.go_emotions.domain.errorhandling.Result

actual class DatabaseServiceImpl actual constructor() :
    DatabaseService {
    actual override suspend fun getTokenizer(): Result<Boolean, DataError>  {
        return databaseServiceIOS?.getTokenizer() ?: Result.Error(DataError.Network.REQUEST_FAILED)
    }

    actual override suspend fun getModel(): Result<Boolean, DataError> {
        return databaseServiceIOS?.getModel() ?: Result.Error(DataError.Network.REQUEST_FAILED)
    }
}