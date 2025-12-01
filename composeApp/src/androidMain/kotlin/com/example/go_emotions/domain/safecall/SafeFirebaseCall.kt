package com.example.go_emotions.domain.safecall

import android.util.Log
import kotlinx.coroutines.CancellationException
import java.io.IOException
import com.example.go_emotions.domain.errorhandling.DataError
import com.example.go_emotions.domain.errorhandling.Result

// A helper to wrap Firebase calls and handle common exceptions
suspend fun <T> safeFirebaseCall(call: suspend () -> T): Result<T, DataError> {
    return try {
        Result.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Log.e("DatabaseServiceImpl", "Network error: IO Exception. ${e.message}")
        Result.Error(DataError.Network.NO_INTERNET_CONNECTION)
    } catch (e: Exception) {
        Log.e("DatabaseServiceImpl", "Generic Firebase error: ${e.message}", e)
        Result.Error(DataError.Network.REQUEST_FAILED)
    }
}