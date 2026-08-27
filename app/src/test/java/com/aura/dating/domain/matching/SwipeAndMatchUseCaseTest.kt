package com.aura.dating.domain.matching

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.discovery.repository.DiscoveryRepository
import com.aura.dating.domain.matching.model.Match
import com.aura.dating.domain.matching.model.SwipeActionType
import com.aura.dating.domain.matching.model.SwipeResult
import com.aura.dating.domain.matching.repository.MatchingRepository
import com.aura.dating.domain.matching.usecase.SwipeUserUseCase
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SwipeAndMatchUseCaseTest {

    private val matchingRepository: MatchingRepository = mockk(relaxed = true)
    private val discoveryRepository: DiscoveryRepository = mockk(relaxed = true)

    private lateinit var swipeUserUseCase: SwipeUserUseCase

    @Before
    fun setUp() {
        swipeUserUseCase = SwipeUserUseCase(matchingRepository, discoveryRepository)
    }

    @Test
    fun `when user swipes right and reciprocal like exists, match is returned exactly once`() = runTest {
        // Given
        val targetUserId = "user-b"
        val matchedProfile = UserProfile(
            id = targetUserId,
            displayName = "Elena",
            birthDateMillis = System.currentTimeMillis() - 24L * 365 * 24 * 3600 * 1000,
            gender = Gender.WOMAN
        )
        val expectedMatchResult = SwipeResult(
            isMatch = true,
            matchId = "match-ab-123",
            matchedUser = matchedProfile
        )

        coEvery { matchingRepository.swipe(targetUserId, SwipeActionType.LIKE) } returns Result.Success(expectedMatchResult)

        // When
        val result = swipeUserUseCase(targetUserId, SwipeActionType.LIKE)

        // Then
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertTrue(data.isMatch)
        assertEquals("match-ab-123", data.matchId)
        assertNotNull(data.matchedUser)
        assertEquals("Elena", data.matchedUser?.displayName)

        // Verify candidate removed from discovery stack locally
        coVerify(exactly = 1) { discoveryRepository.removeCandidateLocally(targetUserId) }
        coVerify(exactly = 1) { matchingRepository.swipe(targetUserId, SwipeActionType.LIKE) }
    }

    @Test
    fun `when user swipes left (PASS), match is false`() = runTest {
        // Given
        val targetUserId = "user-c"
        val expectedPassResult = SwipeResult(
            isMatch = false,
            matchId = null,
            matchedUser = null
        )

        coEvery { matchingRepository.swipe(targetUserId, SwipeActionType.PASS) } returns Result.Success(expectedPassResult)

        // When
        val result = swipeUserUseCase(targetUserId, SwipeActionType.PASS)

        // Then
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertFalse(data.isMatch)
        assertEquals(null, data.matchId)

        coVerify(exactly = 1) { discoveryRepository.removeCandidateLocally(targetUserId) }
        coVerify(exactly = 1) { matchingRepository.swipe(targetUserId, SwipeActionType.PASS) }
    }
}
