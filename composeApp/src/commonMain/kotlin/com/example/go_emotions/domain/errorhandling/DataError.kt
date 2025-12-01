package com.example.go_emotions.domain.errorhandling

sealed interface DataError: Error {
    enum class Network: DataError {
        REQUEST_FAILED,
        REQUEST_TIMEOUT,
        NO_INTERNET_CONNECTION,
        UNKNOWN,
        CANCELED,
        SIGN_IN_FAILED,
        SUCCESS,
        AUTHENTICATION_FAILED,
        CREDENTIALS_NOT_FOUND,
        VERIFY_EMAIL,
        USER_NOT_FOUND,
        INVALID_SIGN_OUT_ATTEMPT,
        EMAIL_NOT_SENT,
        NO_CREDENTIALS_AVAILABLE,
        INVALID_EMAIL,
        USER_EXIST,
        FAILED_DELETE_REQUEST,
        DATA_NOT_FOUND,
        ACCESS_DENIED,
        MODEL_NOT_FOUND,
        MODEL_DID_NOT_RESPOND_JSON,
        MODEL_DID_NOT_RESPOND,
        UNAUTHORIZED,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERIALIZATION,
    }

    enum class Local: DataError {
        DISK_FULL,
        DATA_NOT_FOUND,
        MODULE_NOT_COMPLETED,
        REQUEST_FAILED
    }
}