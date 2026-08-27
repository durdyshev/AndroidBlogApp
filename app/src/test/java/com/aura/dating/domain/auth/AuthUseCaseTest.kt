package com.aura.dating.domain.auth

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.auth.model.UserSession
import com.aura.dating.domain.auth.repository.AuthRepository
import com.aura.dating.domain.auth.usecase.LoginUseCase
import com.aura.dating.domain.auth.usecase.RegisterUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthUseCaseTest {

    private val authRepository: AuthRepository = mockk(relaxed = true)

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var registerUseCase: RegisterUseCase

    @Before
    fun setUp() {
        loginUseCase = LoginUseCase(authRepository)
        registerUseCase = RegisterUseCase(authRepository)
    }

    @Test
    fun `login with valid credentials invokes repository login`() = runTest {
        // Given
        val email = "alex@example.com"
        val password = "securePassword123"
        val session = UserSession("user-1", email, "access-token", "refresh-token")
        coEvery { authRepository.login(email, password) } returns Result.Success(session)

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("user-1", (result as Result.Success).data.userId)
    }

    @Test
    fun `login with invalid email returns ValidationError without calling repository`() = runTest {
        // When
        val result = loginUseCase("not-an-email", "password123")

        // Then
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is AppError.ValidationError)
        assertEquals("email", (error as AppError.ValidationError).field)
    }

    @Test
    fun `register with short password returns ValidationError`() = runTest {
        // When
        val result = registerUseCase("alex@example.com", "123")

        // Then
        assertTrue(result is Result.Error)
        val error = (result as Result.Error).error
        assertTrue(error is AppError.ValidationError)
        assertEquals("password", (error as AppError.ValidationError).field)
    }
}
