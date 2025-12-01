package com.example.go_emotions.domain.errorhandling

/**
 * Errors that can occur during data access.
 */
sealed interface DataError: Error {
    enum class Network: DataError {
        REQUEST_FAILED,
        REQUEST_TIMEOUT,
        NO_INTERNET_CONNECTION,
        UNKNOWN,
        CANCELED,
        UNAUTHORIZED
    }

    enum class Local: DataError {
        DISK_FULL,
        DATA_NOT_FOUND,
        MODULE_NOT_COMPLETED,
        REQUEST_FAILED
    }
}