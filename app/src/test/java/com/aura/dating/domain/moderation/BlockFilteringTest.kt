package com.aura.dating.domain.moderation

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.discovery.repository.DiscoveryRepository
import com.aura.dating.domain.moderation.repository.ModerationRepository
import com.aura.dating.domain.moderation.usecase.BlockUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlockFilteringTest {

    private val moderationRepository: ModerationRepository = mockk(relaxed = true)
    private val discoveryRepository: DiscoveryRepository = mockk(relaxed = true)

    private lateinit var blockUserUseCase: BlockUserUseCase

    @Before
    fun setUp() {
        blockUserUseCase = BlockUserUseCase(moderationRepository, discoveryRepository)
    }

    @Test
    fun `blocking user immediately removes them from discovery candidates locally`() = runTest {
        // Given
        val blockedUserId = "toxic-user-1"
        coEvery { moderationRepository.blockUser(blockedUserId, any(), any()) } returns Result.Success(Unit)

        // When
        val result = blockUserUseCase(blockedUserId, "Bad Actor", null)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { discoveryRepository.removeCandidateLocally(blockedUserId) }
        coVerify(exactly = 1) { moderationRepository.blockUser(blockedUserId, "Bad Actor", null) }
    }
}
