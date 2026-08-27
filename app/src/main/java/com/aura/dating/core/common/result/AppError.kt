package com.aura.dating.core.common.result

sealed interface AppError {
    val message: String
    val cause: Throwable?

    data class NetworkError(
        override val message: String = "Network connection unavailable. Please check your internet connection.",
        override val cause: Throwable? = null
    ) : AppError

    data class Unauthorized(
        override val message: String = "Session expired or unauthorized. Please sign in again.",
        override val cause: Throwable? = null
    ) : AppError

    data class Forbidden(
        override val message: String = "You do not have permission to perform this action.",
        override val cause: Throwable? = null
    ) : AppError

    data class NotFound(
        override val message: String = "Requested item or profile was not found.",
        override val cause: Throwable? = null
    ) : AppError

    data class ValidationError(
        override val message: String,
        val field: String? = null,
        override val cause: Throwable? = null
    ) : AppError

    data class ServerError(
        override val message: String = "Server encountered an error. Please try again later.",
        val statusCode: Int? = null,
        override val cause: Throwable? = null
    ) : AppError

    data class UnknownError(
        override val message: String = "An unexpected error occurred. Please try again.",
        override val cause: Throwable? = null
    ) : AppError
}
