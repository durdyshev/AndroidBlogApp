package com.aura.dating.domain.moderation.usecase

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.discovery.repository.DiscoveryRepository
import com.aura.dating.domain.moderation.model.BlockedUser
import com.aura.dating.domain.moderation.model.ReportRequest
import com.aura.dating.domain.moderation.repository.ModerationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BlockUserUseCase @Inject constructor(
    private val moderationRepository: ModerationRepository,
    private val discoveryRepository: DiscoveryRepository
) {
    suspend operator fun invoke(
        blockedUserId: String,
        displayName: String,
        photoUrl: String?
    ): Result<Unit> {
        discoveryRepository.removeCandidateLocally(blockedUserId)
        return moderationRepository.blockUser(blockedUserId, displayName, photoUrl)
    }
}

class UnblockUserUseCase @Inject constructor(
    private val moderationRepository: ModerationRepository
) {
    suspend operator fun invoke(blockedUserId: String): Result<Unit> {
        return moderationRepository.unblockUser(blockedUserId)
    }
}

class GetBlockedUsersUseCase @Inject constructor(
    private val moderationRepository: ModerationRepository
) {
    val blockedUsersFlow: Flow<List<BlockedUser>> = moderationRepository.blockedUsersFlow
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<BlockedUser>> {
        return moderationRepository.getBlockedUsers(forceRefresh)
    }
}

class ReportUserUseCase @Inject constructor(
    private val moderationRepository: ModerationRepository,
    private val discoveryRepository: DiscoveryRepository
) {
    suspend operator fun invoke(request: ReportRequest): Result<Unit> {
        discoveryRepository.removeCandidateLocally(request.reportedUserId)
        return moderationRepository.reportUser(request)
    }
}

class DeleteAccountUseCase @Inject constructor(
    private val moderationRepository: ModerationRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return moderationRepository.deleteAccount()
    }
}
