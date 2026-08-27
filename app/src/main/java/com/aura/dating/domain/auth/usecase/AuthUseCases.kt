package com.aura.dating.domain.auth.usecase

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.auth.model.UserSession
import com.aura.dating.domain.auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserSession> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !EMAIL_REGEX.matches(trimmedEmail)) {
            return Result.Error(AppError.ValidationError("Please enter a valid email address", field = "email"))
        }
        if (password.length < 6) {
            return Result.Error(AppError.ValidationError("Password must be at least 6 characters", field = "password"))
        }
        return authRepository.login(trimmedEmail, password)
    }
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserSession> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !EMAIL_REGEX.matches(trimmedEmail)) {
            return Result.Error(AppError.ValidationError("Please enter a valid email address", field = "email"))
        }
        if (password.length < 6) {
            return Result.Error(AppError.ValidationError("Password must be at least 6 characters", field = "password"))
        }
        return authRepository.register(trimmedEmail, password)
    }
}

class VerifyEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, token: String): Result<Unit> {
        if (token.isBlank()) {
            return Result.Error(AppError.ValidationError("Verification code cannot be blank", field = "token"))
        }
        return authRepository.verifyEmail(email.trim(), token.trim())
    }
}

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.Error(AppError.ValidationError("Please enter a valid email address", field = "email"))
        }
        return authRepository.sendPasswordReset(trimmedEmail)
    }
}

class GetSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    val sessionFlow: Flow<UserSession?> = authRepository.currentSessionFlow
    suspend fun getCachedSession(): UserSession? = authRepository.getCurrentSession()
    suspend fun restoreSession(): Result<UserSession> = authRepository.restoreSession()
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}
