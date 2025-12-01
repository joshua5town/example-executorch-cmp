package com.example.go_emotions.domain.retrypolicy

import kotlinx.coroutines.delay
import kotlin.ranges.coerceAtMost
import kotlin.time.Duration.Companion.seconds

/**
 * Retry policy for suspend functions.
 */
class RetryPolicy(
    private val maxRetries: Int = 3,
    private val initialDelay: Long = 5,
    private val maxDelay: Long = 10,
    private val delayFactor: Double = 2.0
) {
    suspend fun <T> retry(
        block: suspend () -> T,
        retryIf: (Exception) -> Boolean = { true }
    ): Result<T> {
        var currentDelay = initialDelay.seconds
        var lastException: Exception? = null
        for (attempt in 1..maxRetries) {
            try {
                return Result.success(block())
            } catch (e: Exception) {
                lastException = e
                if (attempt == maxRetries || !retryIf(e)) {
                    return Result.failure(e)
                }
                delay(currentDelay)
                currentDelay = (currentDelay * delayFactor).coerceAtMost(maxDelay.seconds)
            }
        }
        return Result.failure(lastException ?: Exception("Unknown error"))
    }
}